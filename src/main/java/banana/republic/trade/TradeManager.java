package banana.republic.trade;

import banana.republic.board.Board;
import banana.republic.core.GamePhase;
import banana.republic.player.Player;
import banana.republic.resource.Bank;
import banana.republic.resource.ResourceType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Orkestrator semua transaksi dagang dalam satu giliran.
 *
 * Bertanggung jawab atas: Domestic trade: pemain dan pemain, melalui
 * offer/counter/accept Maritime trade: pemain dan bank, dengan rasio berbasis
 * harbor
 *
 * TradeManager menyimpan negosiasi yang sedang aktif dalam satu giliran. Game
 * memanggilnya melalui method tradeWithPlayer} dan tradeWithBank.
 *
 * Setiap giliran baru, #reset() harus dipanggil untuk membersihkan state.
 */
public class TradeManager {

    private final TradeValidator validator = new TradeValidator();
    private final MaritimeTrade maritimeTrade = new MaritimeTrade();
    private final List<TradeNegotiation> negotiations = new ArrayList<>();
    private TradeNegotiation activeNegotiation;

    /**
     * Offerer membuat penawaran baru ke target (atau broadcast ke semua).
     * Menutup negosiasi aktif sebelumnya jika ada.
     *
     * offer penawaran yang dibuat phase fase permainan saat ini return {@link
     * ValidationResult} — jika gagal, offer tidak dibuat
     */
    public ValidationResult makeOffer(TradeOffer offer, GamePhase phase) {
        ValidationResult check = validator.validateOffer(offer, phase);
        if (!check.isValid())
            return check;

        // Tutup negosiasi aktif lama jika ada
        if (activeNegotiation != null && activeNegotiation.isOpen()) {
            activeNegotiation.cancel();
        }

        activeNegotiation = new TradeNegotiation(offer);
        negotiations.add(activeNegotiation);
        return ValidationResult.ok();
    }

    /**
     * Target menerima offer aktif saat ini. Eksekusi transfer resource otomatis
     * dilakukan di sini.
     *
     * target pemain yang menerima return {@link ValidationResult}
     */
    public ValidationResult acceptOffer(Player target) {
        if (activeNegotiation == null || !activeNegotiation.isOpen()) {
            return ValidationResult.fail(
                "Tidak ada negosiasi aktif yang bisa diterima");
        }

        TradeOffer offer = activeNegotiation.getLatestOffer();

        // Pastikan yang accept adalah target yang tepat
        if (!offer.isBroadcast() && !target.equals(offer.getTarget())) {
            return ValidationResult.fail(target.getName() +
                                         " bukan target dari offer ini");
        }

        // Validasi target bisa memenuhi
        TradeOffer offerWithTarget =
            offer.isBroadcast()
                ? new TradeOffer(offer.getOfferer(), target, offer.getOffer(),
                                 offer.getRequest())
                : offer;

        ValidationResult check =
            validator.validateTargetCanFulfill(offerWithTarget);
        if (!check.isValid())
            return check;

        // Eksekusi transfer
        executeTransfer(offer.getOfferer(), target, offer.getOffer(),
                        offer.getRequest());

        activeNegotiation.accept();
        activeNegotiation = null;
        return ValidationResult.ok();
    }

    /**
     * Target menolak offer aktif.
     */
    public ValidationResult rejectOffer(Player target) {
        if (activeNegotiation == null || !activeNegotiation.isOpen()) {
            return ValidationResult.fail("Tidak ada negosiasi aktif");
        }

        TradeOffer offer = activeNegotiation.getLatestOffer();
        if (!offer.isBroadcast() && !target.equals(offer.getTarget())) {
            return ValidationResult.fail(target.getName() +
                                         " bukan target dari offer ini");
        }

        activeNegotiation.reject();
        activeNegotiation = null;
        return ValidationResult.ok();
    }

    /**
     * Target mengajukan counter-offer ke offerer. Negosiasi tetap OPEN dengan
     * offer baru.
     *
     * counter offer baru dari target phase fase permainan saat ini return
     * ValidationResult
     */
    public ValidationResult counterOffer(TradeOffer counter, GamePhase phase) {
        if (activeNegotiation == null || !activeNegotiation.isOpen()) {
            return ValidationResult.fail(
                "Tidak ada negosiasi aktif untuk di-counter");
        }

        ValidationResult check = validator.validateOffer(counter, phase);
        if (!check.isValid())
            return check;

        activeNegotiation.addCounterOffer(counter);
        return ValidationResult.ok();
    }

    /**
     * Offerer membatalkan negosiasi aktif.
     */
    public ValidationResult cancelOffer(Player offerer) {
        if (activeNegotiation == null || !activeNegotiation.isOpen()) {
            return ValidationResult.fail(
                "Tidak ada negosiasi aktif untuk dibatalkan");
        }
        if (!offerer.equals(activeNegotiation.getInitiator())) {
            return ValidationResult.fail(
                "Hanya offerer yang bisa membatalkan offer");
        }
        activeNegotiation.cancel();
        activeNegotiation = null;
        return ValidationResult.ok();
    }

    /**
     * Eksekusi trade dengan bank: pemain menjual sellAmt unit sellType dan
     * mendapat 1 unit buyType.
     *
     * Rasio otomatis dihitung berdasarkan harbor terbaik pemain.
     *
     * player pemain yang trade sellType resource yang dijual buyType resource
     * yang dibeli bank bank board papan untuk cek harbor phase fase permainan
     * return ValidationResult
     */
    public ValidationResult tradeWithBank(Player player, ResourceType sellType,
                                          ResourceType buyType, Bank bank,
                                          Board board, GamePhase phase) {
        int ratio = maritimeTrade.getBestRatio(player, sellType, board);

        ValidationResult check = validator.validateBankTrade(
            player, sellType, ratio, buyType, 1, bank, board, phase);
        if (!check.isValid())
            return check;

        // Eksekusi
        player.removeResource(sellType, ratio);
        bank.returnResource(sellType, ratio);

        bank.takeResource(buyType, 1);
        player.addResource(buyType, 1);

        return ValidationResult.ok();
    }

    /**
     * Versi dengan jumlah eksplisit untuk validasi sebelum konfirmasi di UI.
     * Berguna UI yang ingin pre-check sebelum menampilkan konfirmasi.
     */
    public ValidationResult canTradeWithBank(Player player,
                                             ResourceType sellType,
                                             ResourceType buyType, Bank bank,
                                             Board board, GamePhase phase) {
        int ratio = maritimeTrade.getBestRatio(player, sellType, board);
        return validator.validateBankTrade(player, sellType, ratio, buyType, 1,
                                           bank, board, phase);
    }

    /**
     * Mengembalikan rasio trade terbaik pemain untuk resource tertentu.
     * Dipanggil UI untuk menampilkan rasio di harbor panel.
     */
    public int getBestTradeRatio(Player player, ResourceType sellType,
                                 Board board) {
        return maritimeTrade.getBestRatio(player, sellType, board);
    }

    /**
     * Mengembalikan rasio trade semua resource untuk pemain.
     */
    public int[] getAllTradeRatios(Player player, Board board) {
        return maritimeTrade.getAllRatios(player, board);
    }

    /** Reset state antar giliran. Dipanggil oleh {@code Game.endTurn()}. */
    public void reset() {
        if (activeNegotiation != null && activeNegotiation.isOpen()) {
            activeNegotiation.cancel();
        }
        activeNegotiation = null;
        negotiations.clear();
    }

    /** Negosiasi yang sedang aktif, atau {@code null} jika tidak ada. */
    public TradeNegotiation getActiveNegotiation() { return activeNegotiation; }

    /** Semua negosiasi dalam giliran ini (unmodifiable). */
    public List<TradeNegotiation> getNegotiations() {
        return Collections.unmodifiableList(negotiations);
    }

    /** {@code true} jika ada negosiasi aktif yang sedang berlangsung. */
    public boolean hasActiveNegotiation() {
        return activeNegotiation != null && activeNegotiation.isOpen();
    }

    /**
     * Transfer resource antara dua pemain. Offerer memberikan give, target
     * memberikan receive.
     */
    private void executeTransfer(Player offerer, Player target,
                                 Map<ResourceType, Integer> give,
                                 Map<ResourceType, Integer> receive) {
        // Offerer → Target (give)
        for (Map.Entry<ResourceType, Integer> e : give.entrySet()) {
            if (e.getValue() <= 0)
                continue;
            offerer.removeResource(e.getKey(), e.getValue());
            target.addResource(e.getKey(), e.getValue());
        }
        // Target → Offerer (receive)
        for (Map.Entry<ResourceType, Integer> e : receive.entrySet()) {
            if (e.getValue() <= 0)
                continue;
            target.removeResource(e.getKey(), e.getValue());
            offerer.addResource(e.getKey(), e.getValue());
        }
    }
}
