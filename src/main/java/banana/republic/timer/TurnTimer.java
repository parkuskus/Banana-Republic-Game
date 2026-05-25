package banana.republic.timer;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import javafx.application.Platform;

/**
 * Turn Timer (Timer Giliran).
 *
 * 90-detik countdown timer untuk phase Trade/Build per giliran.
 * Berjalan di background thread terpisah dari UI thread.
 * Non-blocking UI updates via Platform.runLater().
 *
 * State management:
 * - running: flag untuk kontrol thread
 * - remainingSeconds: counter yang di-update per 1 detik
 * - thread: reference ke background thread
 *
 * Callbacks:
 * - onTick: dipanggil setiap detik (update UI timer display)
 * - onExpire: dipanggil ketika timer habis (auto end turn)
 */
public class TurnTimer implements Runnable {
    private static final int DURATION_SECONDS = 90;

    private final AtomicInteger remainingSeconds;
    private volatile boolean running;
    private volatile boolean paused;
    private Thread thread;

    private Runnable onExpire;
    private Consumer<Integer> onTick;

    /**
     * Constructor untuk TurnTimer.
     */
    public TurnTimer() {
        this.remainingSeconds = new AtomicInteger(DURATION_SECONDS);
        this.running = false;
        this.paused = false;
        this.thread = null;
        this.onExpire = null;
        this.onTick = null;
    }

    /**
     * Mulai timer di background thread.
     * Jika sudah running, tidak ada action.
     */
    public void start() {
        if (running) {
            return;
        }

        running = true;
        paused = false;
        remainingSeconds.set(DURATION_SECONDS);

        thread = new Thread(this);
        thread.setDaemon(true); // Set sebagai daemon thread
        thread.setName("TurnTimer-" + System.nanoTime());
        thread.start();
    }

    /**
     * Stop timer.
     */
    public void stop() {
        running = false;
        paused = false;

        if (thread != null) {
            try {
                // Interrupt the thread to wake it if sleeping, then join
                thread.interrupt();
                thread.join(1000); // Wait max 1 detik untuk thread terminate
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Pause timer (tetap running, tapi clock di-pause).
     */
    public void pause() {
        this.paused = true;
        // Wake thread if it's sleeping so pause takes effect immediately
        if (thread != null) {
            thread.interrupt();
        }
    }

    /**
     * Resume timer dari pause.
     */
    public void resume() {
        this.paused = false;
        // Wake thread if it's sleeping so resume takes effect immediately
        if (thread != null) {
            thread.interrupt();
        }
    }

    /**
     * Reset timer ke 90 detik tanpa stop/start.
     */
    public void reset() {
        remainingSeconds.set(DURATION_SECONDS);
    }

    /**
     * Dapatkan sisa waktu (dalam detik).
     */
    public int getRemainingSeconds() {
        return remainingSeconds.get();
    }

    /**
     * Cek apakah timer sedang berjalan.
     */
    public boolean isRunning() {
        return running;
    }

    /**
     * Set callback ketika timer expire (habis).
     * Callback dijalankan via Platform.runLater() untuk thread-safety UI.
     */
    public void setOnExpire(Runnable callback) {
        this.onExpire = callback;
    }

    /**
     * Set callback untuk setiap tick (setiap 1 detik).
     * Consumer menerima sisa waktu dalam detik.
     * Callback dijalankan via Platform.runLater() untuk thread-safety UI.
     */
    public void setOnTick(Consumer<Integer> callback) {
        this.onTick = callback;
    }

    /**
     * Run method (dijalankan di background thread).
     * Loop setiap 1 detik, decrement counter, dan trigger callbacks.
     */
    @Override
    public void run() {
        try {
            while (running && remainingSeconds.get() > 0) {
                if (!paused) {
                    // Tunggu 1 detik, dengan handling interrupt untuk responsive pause/stop
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ie) {
                        // If interrupted due to pause/stop, continue loop to re-evaluate flags
                        if (!running) break;
                        continue;
                    }

                    // Decrement counter
                    int updated = remainingSeconds.decrementAndGet();

                    // Trigger onTick via UI thread (non-blocking)
                    if (onTick != null) {
                        safeRunLater(() -> onTick.accept(updated));
                    }

                    // Cek apakah timer sudah habis
                    if (updated <= 0) {
                        running = false;
                        if (onExpire != null) {
                            safeRunLater(onExpire);
                        }
                    }
                } else {
                    // Pause: sleep tanpa decrement, wakeable by interrupt
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException ie) {
                        if (!running) break;
                        // continue and re-check paused flag
                    }
                }
            }
        } finally {
            running = false;
        }
    }

    /**
     * Helper untuk menjalankan callback di UI thread jika tersedia,
     * atau langsung jika JavaFX Platform belum di-initialize.
     */
    private void safeRunLater(Runnable r) {
        try {
            Platform.runLater(r);
        } catch (IllegalStateException ex) {
            // Toolkit not initialized (tests/headless). Jalankan langsung.
            try {
                r.run();
            } catch (Exception e) {
                // swallow to avoid breaking timer loop
            }
        }
    }
}
