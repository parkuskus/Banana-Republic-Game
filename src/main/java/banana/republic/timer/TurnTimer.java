package banana.republic.timer;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * Countdown timer 90 detik untuk fase Trade/Build setiap giliran.
 *
 * <p>Berjalan di <strong>background daemon thread</strong> agar tidak memblokir
 * UI thread (spesifikasi multithreading). Mendukung dua pola penggunaan:
 *
 * <ul>
 *   <li><strong>Constructor injection</strong> — dipakai oleh {@code TurnManager}:
 *       <pre>{@code new TurnTimer(() -> game.endTurn(), secs -> updateLabel(secs))}</pre>
 *   </li>
 *   <li><strong>Setter callbacks</strong> — dipakai oleh UI controller:
 *       <pre>{@code timer.setOnExpire(() -> ...); timer.setOnTick(s -> ...);}</pre>
 *   </li>
 * </ul>
 *
 * <p>Fitur:
 * <ul>
 *   <li>Pause / Resume tanpa stop dan restart timer</li>
 *   <li>Stop menghentikan thread dan menunggu terminasi (max 1 detik)</li>
 *   <li>{@code safeRunLater} — callback aman di lingkungan headless (test) maupun JavaFX</li>
 * </ul>
 */
public class TurnTimer implements Runnable {

    /** Durasi giliran dalam detik (sesuai spesifikasi: 90 detik). */
    public static final int TURN_DURATION_SECONDS = 90;

    // -------------------------------------------------------------------------
    // Callback interfaces
    // -------------------------------------------------------------------------

    /** Dipanggil saat timer mencapai nol (bukan saat di-cancel/stop). */
    @FunctionalInterface
    public interface OnTimerEndCallback {
        void onTimerEnd();
    }

    /** Dipanggil setiap detik dengan sisa waktu. */
    @FunctionalInterface
    public interface OnTickCallback {
        void onTick(int secondsRemaining);
    }

    // -------------------------------------------------------------------------
    // State
    // -------------------------------------------------------------------------

    private final AtomicBoolean running   = new AtomicBoolean(false);
    private final AtomicBoolean cancelled = new AtomicBoolean(false);
    private final AtomicInteger remaining = new AtomicInteger(TURN_DURATION_SECONDS);

    private volatile boolean paused = false;
    private Thread timerThread;

    // Callbacks — bisa diset via constructor atau setter
    private OnTimerEndCallback onEnd;
    private OnTickCallback     onTick;

    // Legacy-compatible setter-based callbacks (Consumer<Integer> for onTick)
    private Runnable           onExpire;
    private Consumer<Integer>  onTickConsumer;

    // -------------------------------------------------------------------------
    // Konstruktor
    // -------------------------------------------------------------------------

    /**
     * Constructor injection — dipakai oleh {@link banana.republic.core.TurnManager}.
     *
     * @param onEnd  dipanggil saat waktu habis; tidak boleh {@code null}
     * @param onTick dipanggil setiap detik; boleh {@code null}
     */
    public TurnTimer(OnTimerEndCallback onEnd, OnTickCallback onTick) {
        if (onEnd == null) {
            throw new IllegalArgumentException("OnTimerEndCallback cannot be null");
        }
        this.onEnd  = onEnd;
        this.onTick = onTick;
    }

    /** Constructor tanpa tick callback. */
    public TurnTimer(OnTimerEndCallback onEnd) {
        this(onEnd, null);
    }

    /**
     * Default constructor — gunakan setter {@link #setOnExpire} dan {@link #setOnTick}
     * sebelum memanggil {@link #start()}.
     */
    public TurnTimer() {
        // callbacks diset via setter
    }

    // -------------------------------------------------------------------------
    // Setter callbacks (pola alternatif — kompatibel dengan UI controller lama)
    // -------------------------------------------------------------------------

    /** Set callback saat timer expire. Menggantikan callback dari constructor jika keduanya diset. */
    public void setOnExpire(Runnable callback) {
        this.onExpire = callback;
    }

    /**
     * Set callback per detik dengan sisa waktu (pakai {@link Consumer}{@code <Integer>}).
     * Kompatibel dengan controller yang sudah pakai lambda {@code s -> label.setText(...)}.
     */
    public void setOnTick(Consumer<Integer> callback) {
        this.onTickConsumer = callback;
    }

    // -------------------------------------------------------------------------
    // Lifecycle
    // -------------------------------------------------------------------------

    /**
     * Memulai countdown di background daemon thread.
     * Panggilan diabaikan jika timer sudah berjalan.
     */
    public synchronized void start() {
        if (running.get()) {
            return;
        }
        cancelled.set(false);
        paused = false;
        remaining.set(TURN_DURATION_SECONDS);
        running.set(true);

        timerThread = new Thread(this, "TurnTimer-" + System.nanoTime());
        timerThread.setDaemon(true);
        timerThread.start();
    }

    /**
     * Menghentikan timer sepenuhnya dan menunggu thread terminasi (maks 1 detik).
     * Callback expire <strong>tidak</strong> akan dipanggil setelah {@code stop()}.
     */
    public void stop() {
        cancelled.set(true);
        running.set(false);
        paused = false;
        if (timerThread != null) {
            timerThread.interrupt();
            try {
                timerThread.join(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Alias untuk {@link #stop()} — kompatibel dengan nama yang dipakai TurnManager.
     */
    public void cancel() {
        stop();
    }

    /**
     * Mem-pause countdown (clock berhenti sementara, thread tetap hidup).
     * Resume dengan {@link #resume()}.
     */
    public void pause() {
        paused = true;
        if (timerThread != null) {
            timerThread.interrupt(); // wake up sleep agar flag paused langsung efektif
        }
    }

    /** Melanjutkan countdown setelah {@link #pause()}. */
    public void resume() {
        paused = false;
        if (timerThread != null) {
            timerThread.interrupt(); // wake up sleep agar resume langsung efektif
        }
    }

    /** Reset sisa waktu ke {@link #TURN_DURATION_SECONDS} tanpa stop/start. */
    public void reset() {
        remaining.set(TURN_DURATION_SECONDS);
    }

    // -------------------------------------------------------------------------
    // Query
    // -------------------------------------------------------------------------

    /** Sisa waktu dalam detik (snapshot). */
    public int getRemainingSeconds() {
        return remaining.get();
    }

    /** {@code true} jika timer sedang berjalan (belum stop/cancel). */
    public boolean isRunning() {
        return running.get();
    }

    /** {@code true} jika timer sedang di-pause. */
    public boolean isPaused() {
        return paused;
    }

    // -------------------------------------------------------------------------
    // Runnable — loop countdown
    // -------------------------------------------------------------------------

    @Override
    public void run() {
        try {
            while (running.get() && remaining.get() > 0) {
                if (!paused) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ie) {
                        if (!running.get()) break;
                        continue; // bisa karena pause/resume — re-evaluate flags
                    }

                    int updated = remaining.decrementAndGet();
                    fireOnTick(updated);

                    if (updated <= 0) {
                        running.set(false);
                        fireOnExpire();
                    }
                } else {
                    // Pause mode: tidur pendek, mudah dibangunkan
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException ie) {
                        if (!running.get()) break;
                    }
                }
            }
        } finally {
            running.set(false);
        }
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /** Panggil semua callback onTick yang terdaftar (dari constructor maupun setter). */
    private void fireOnTick(int secs) {
        if (onTick != null) {
            safeRunLater(() -> onTick.onTick(secs));
        }
        if (onTickConsumer != null) {
            safeRunLater(() -> onTickConsumer.accept(secs));
        }
    }

    /** Panggil semua callback expire yang terdaftar (dari constructor maupun setter). */
    private void fireOnExpire() {
        if (onEnd != null) {
            safeRunLater(onEnd::onTimerEnd);
        }
        if (onExpire != null) {
            safeRunLater(onExpire);
        }
    }

    /**
     * Jalankan {@code r} via {@code Platform.runLater()} jika JavaFX tersedia,
     * atau langsung jika toolkit belum di-init (misal: unit test headless).
     */
    private void safeRunLater(Runnable r) {
        try {
            javafx.application.Platform.runLater(r);
        } catch (IllegalStateException ex) {
            // Toolkit not initialized — jalankan langsung (test/headless environment)
            try {
                r.run();
            } catch (Exception e) {
                // Swallow agar tidak merusak timer loop
            }
        }
    }
}
