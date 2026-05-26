package banana.republic.timer;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Countdown timer 90 detik untuk fase Trade/Build setiap giliran.
 *
 * <p>
 * Berjalan di background thread sehingga tidak memblokir UI thread untuk
 * Multithreadding. Setelah timer habis, callback
 * OnTimerEndCallback#onTimerEnd() dipanggil, Game/TurnManager bisa bereaksi
 * tanpa TurnTimer tahu siapa yang mendengarkan (Observer mini).
 *
 * TurnTimer timer = new TurnTimer(() -> game.endTurn()); timer.start(); //
 * mulai di background timer.cancel(); // batalkan jika pemain menekan End Turn
 * lebih awal
 *
 * <p>
 * Implements Runnable, thread lifecycle dikelola secara internal oleh start()
 * agar pemanggil tidak perlu mengelola Thread secara manual.
 */
public class TurnTimer implements Runnable {

    public static final int TURN_DURATION_SECONDS = 90;

    @FunctionalInterface
    public interface OnTimerEndCallback {
        void onTimerEnd();
    }

    @FunctionalInterface
    public interface OnTickCallback {
        void onTick(int secondsRemaining);
    }

    private final OnTimerEndCallback onEnd;
    private final OnTickCallback onTick;

    private final AtomicBoolean cancelled = new AtomicBoolean(false);
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final AtomicInteger remaining =
        new AtomicInteger(TURN_DURATION_SECONDS);

    private Thread timerThread;

    public TurnTimer(OnTimerEndCallback onEnd, OnTickCallback onTick) {
        if (onEnd == null) {
            throw new IllegalArgumentException(
                "OnTimerEndCallback cannot be null");
        }
        this.onEnd = onEnd;
        this.onTick = onTick;
    }

    public TurnTimer(OnTimerEndCallback onEnd) { this(onEnd, null); }

    /**
     * Memulai countdown di background thread. Mengabaikan panggilan jika timer
     * sudah berjalan.
     */
    public synchronized void start() {
        if (running.get()) {
            return;
        }
        cancelled.set(false);
        remaining.set(TURN_DURATION_SECONDS);
        running.set(true);
        timerThread = new Thread(this, "TurnTimer-Thread");
        timerThread.setDaemon(true); // tidak menghalangi JVM shutdown
        timerThread.start();
    }

    /**
     * Membatalkan timer. Aman dipanggil berkali-kali dan dari thread manapun.
     * Callback
     */
    public void cancel() {
        cancelled.set(true);
        running.set(false);
        if (timerThread != null) {
            timerThread.interrupt();
        }
    }

    // Mengembalikan sisa waktu dalam detik (snapshot, bisa berubah).
    public int getRemainingSeconds() { return remaining.get(); }

    public boolean isRunning() { return running.get(); }

    // Runnable — loop countdown (dijalankan di background thread)
    @Override
    public void run() {
        try {
            while (!cancelled.get() && remaining.get() > 0) {
                int secs = remaining.get();

                if (onTick != null) {
                    onTick.onTick(secs);
                }

                Thread.sleep(1000);
                remaining.decrementAndGet();
            }

            // Jika loop selesai bukan karena cancel → waktu habis
            if (!cancelled.get()) {
                onEnd.onTimerEnd();
            }
        } catch (InterruptedException e) {
            // Thread di-interrupt oleh cancel() — tidak perlu aksi lebih lanjut
            Thread.currentThread().interrupt();
        } finally {
            running.set(false);
        }
    }
}
