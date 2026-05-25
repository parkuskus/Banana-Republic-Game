package banana.republic.core;

import java.util.List;

import banana.republic.player.Player;
import banana.republic.timer.TurnTimer;

/**
 * Mengelola urutan giliran pemain dan mengintegrasikan {@link TurnTimer}.
 *
 * <p>Tanggung jawab TurnManager:
 * <ul>
 *   <li>Melacak indeks pemain aktif dalam list pemain</li>
 *   <li>Menentukan arah putaran (clockwise / counter-clockwise) sesuai fase setup</li>
 *   <li>Memulai dan menghentikan {@link TurnTimer} saat fase Trade/Build dimulai</li>
 *   <li>Mengirimkan notifikasi ke Game saat timer habis (via callback {@code Runnable})</li>
 * </ul>
 *
 * <p>TurnManager <em>tidak</em> memanggil {@code game.endTurn()} secara langsung —
 * ia menginformasikan Game melalui callback ({@code onTurnEnd}) agar Game tetap
 * menjadi satu-satunya pengontrol lifecycle permainan (DIP).
 */
public class TurnManager {

    private final List<Player>   players;
    private int                  activeIndex;
    private TurnOrder            currentOrder;

    /** Timer yang sedang berjalan, atau {@code null} jika tidak ada. */
    private TurnTimer            activeTimer;

    /** Callback yang dipanggil TurnTimer saat waktu habis. */
    private final Runnable       onTimerExpired;

    // -------------------------------------------------------------------------
    // Konstruktor
    // -------------------------------------------------------------------------

    /**
     * @param players        daftar pemain sesuai urutan awal; tidak boleh null/kosong
     * @param startIndex     indeks pemain yang giliran pertama
     * @param onTimerExpired callback dipanggil saat timer 90 detik habis;
     *                       biasanya {@code game::endTurn}
     */
    public TurnManager(List<Player> players, int startIndex, Runnable onTimerExpired) {
        if (players == null || players.isEmpty()) {
            throw new IllegalArgumentException("Players list cannot be null or empty");
        }
        if (startIndex < 0 || startIndex >= players.size()) {
            throw new IllegalArgumentException("Invalid startIndex: " + startIndex);
        }
        if (onTimerExpired == null) {
            throw new IllegalArgumentException("onTimerExpired callback cannot be null");
        }
        this.players        = players;
        this.activeIndex    = startIndex;
        this.currentOrder   = TurnOrder.CLOCKWISE;
        this.onTimerExpired = onTimerExpired;
    }

    // -------------------------------------------------------------------------
    // Query pemain
    // -------------------------------------------------------------------------

    /** Mengembalikan pemain yang sedang aktif. */
    public Player getActivePlayer() {
        return players.get(activeIndex);
    }

    /** Mengembalikan indeks pemain aktif dalam list. */
    public int getActiveIndex() {
        return activeIndex;
    }

    /** Mengembalikan arah putaran giliran saat ini. */
    public TurnOrder getCurrentOrder() {
        return currentOrder;
    }

    // -------------------------------------------------------------------------
    // Navigasi giliran
    // -------------------------------------------------------------------------

    /**
     * Memajukan giliran ke pemain berikutnya sesuai {@link #currentOrder}.
     * Menghentikan timer yang sedang berjalan sebelum berpindah.
     *
     * @return pemain yang sekarang menjadi aktif setelah advance
     */
    public Player advanceTurn() {
        stopTimer();
        activeIndex = nextIndex(activeIndex, currentOrder);
        return getActivePlayer();
    }

    /**
     * Memajukan giliran ke pemain berikutnya sesuai arah yang diberikan,
     * tanpa mengubah {@link #currentOrder} secara permanen.
     * Digunakan saat transisi antara fase setup.
     */
    public Player advanceTurnInDirection(TurnOrder order) {
        stopTimer();
        activeIndex = nextIndex(activeIndex, order);
        return getActivePlayer();
    }

    /**
     * Mengubah arah putaran. Dipakai saat transisi dari putaran pertama ke
     * putaran kedua fase setup (clockwise → counter-clockwise).
     */
    public void setOrder(TurnOrder order) {
        if (order == null) {
            throw new IllegalArgumentException("TurnOrder cannot be null");
        }
        this.currentOrder = order;
    }

    /**
     * Langsung menetapkan indeks pemain aktif.
     * Berguna saat memuat state permainan dari save file.
     */
    public void setActiveIndex(int index) {
        if (index < 0 || index >= players.size()) {
            throw new IllegalArgumentException("Invalid player index: " + index);
        }
        this.activeIndex = index;
    }

    // -------------------------------------------------------------------------
    // Timer management
    // -------------------------------------------------------------------------

    /**
     * Memulai timer 90 detik untuk fase Trade/Build.
     * Jika timer sudah berjalan, panggilan ini diabaikan.
     *
     * @param onTick callback per detik untuk memperbarui tampilan timer di UI;
     *               boleh {@code null} jika tidak diperlukan
     */
    public void startTimer(TurnTimer.OnTickCallback onTick) {
        stopTimer(); // pastikan tidak ada timer yang overlap
        activeTimer = new TurnTimer(onTimerExpired::run, onTick);
        activeTimer.start();
    }

    /**
     * Menghentikan timer yang sedang berjalan (jika ada).
     * Aman dipanggil meskipun tidak ada timer yang aktif.
     */
    public void stopTimer() {
        if (activeTimer != null && activeTimer.isRunning()) {
            activeTimer.cancel();
        }
        activeTimer = null;
    }

    /** Mengembalikan sisa waktu timer dalam detik, atau 0 jika tidak ada timer aktif. */
    public int getRemainingTimerSeconds() {
        if (activeTimer == null || !activeTimer.isRunning()) {
            return 0;
        }
        return activeTimer.getRemainingSeconds();
    }

    /** Mengembalikan {@code true} jika timer sedang aktif berjalan. */
    public boolean isTimerRunning() {
        return activeTimer != null && activeTimer.isRunning();
    }

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    private int nextIndex(int current, TurnOrder order) {
        int size = players.size();
        if (order == TurnOrder.CLOCKWISE) {
            return (current + 1) % size;
        } else {
            return (current - 1 + size) % size;
        }
    }
}

