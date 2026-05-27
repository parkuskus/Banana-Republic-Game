package banana.republic.trade;

import banana.republic.player.Player;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Riwayat dan state satu sesi negosiasi dagang antar pemain.
 *
 * Satu sesi negosiasi bisa terdiri dari beberapa putaran penawaran (initial
 * offer → counter → counter balik → akhirnya diterima/ditolak).
 *
 * Kelas ini mutable dan dirancang sebagai state machine: OPEN → ACCEPTED
 * (finalize) OPEN → REJECTED (finalize) OPEN → CANCELLED (finalize)
 */
public class TradeNegotiation {

    public enum NegotiationStatus { OPEN, ACCEPTED, REJECTED, CANCELLED }

    private final List<TradeOffer> history = new ArrayList<>();
    private NegotiationStatus status = NegotiationStatus.OPEN;

    /**
     * Mulai negosiasi dengan offer pertama dari pemain aktif.
     *
     * initialOffer offer pertama; tidak boleh null dan harus PENDING
     */
    public TradeNegotiation(TradeOffer initialOffer) {
        if (initialOffer == null) {
            throw new IllegalArgumentException(
                "Initial offer tidak boleh null");
        }
        if (initialOffer.getStatus() != TradeOffer.Status.PENDING) {
            throw new IllegalArgumentException(
                "Initial offer harus berstatus PENDING");
        }
        history.add(initialOffer);
    }

    /**
     * Target menerima offer terkini. Menutup negosiasi dengan status ACCEPTED.
     *
     * return offer yang diterima (hasil {@link TradeOffer#accept()})
     */
    public TradeOffer accept() {
        ensureOpen();
        TradeOffer accepted = getLatestOffer().accept();
        history.add(accepted);
        status = NegotiationStatus.ACCEPTED;
        return accepted;
    }

    /**
     * Target menolak offer terkini. Menutup negosiasi dengan status REJECTED.
     *
     * return offer yang ditolak (hasil {@link TradeOffer#reject()})
     */
    public TradeOffer reject() {
        ensureOpen();
        TradeOffer rejected = getLatestOffer().reject();
        history.add(rejected);
        status = NegotiationStatus.REJECTED;
        return rejected;
    }

    /**
     * Pembuat offer membatalkan negosiasi. Menutup negosiasi dengan status
     * CANCELLED.
     */
    public void cancel() {
        ensureOpen();
        TradeOffer cancelled = getLatestOffer().cancel();
        history.add(cancelled);
        status = NegotiationStatus.CANCELLED;
    }

    /**
     * Target mengajukan counter-offer. Negosiasi tetap OPEN, counter-offer
     * ditambahkan ke riwayat.
     *
     * param counterOffer offer baru dari target; harus ditujukan ke offerer
     * asal
     */
    public void addCounterOffer(TradeOffer counterOffer) {
        ensureOpen();
        if (counterOffer == null) {
            throw new IllegalArgumentException(
                "Counter offer tidak boleh null");
        }
        // Tandai offer sebelumnya sebagai COUNTERED
        TradeOffer prev = getLatestOffer().counter(counterOffer);
        history.set(history.size() - 1, prev);
        // Tambahkan counter-offer baru sebagai offer aktif
        history.add(counterOffer);
    }

    /**
     * Offer terkini (bisa berupa initial offer atau counter-offer terakhir).
     */
    public TradeOffer getLatestOffer() {
        return history.get(history.size() - 1);
    }

    /** Offer pertama yang mengawali negosiasi ini. */
    public TradeOffer getInitialOffer() { return history.get(0); }

    /** Semua offer dalam urutan kronologis (unmodifiable). */
    public List<TradeOffer> getHistory() {
        return Collections.unmodifiableList(history);
    }

    /** Status negosiasi saat ini. */
    public NegotiationStatus getStatus() { return status; }

    /** {@code true} jika negosiasi masih berlangsung. */
    public boolean isOpen() { return status == NegotiationStatus.OPEN; }

    /** Pemain yang memulai negosiasi (dari initial offer). */
    public Player getInitiator() { return getInitialOffer().getOfferer(); }

    private void ensureOpen() {
        if (status != NegotiationStatus.OPEN) {
            throw new IllegalStateException(
                "Negosiasi sudah selesai (status: " + status + ")");
        }
    }

    @Override
    public String toString() {
        return String.format("TradeNegotiation[status=%s, steps=%d, latest=%s]",
                             status, history.size(), getLatestOffer());
    }
}
