package banana.republic.trade;

import java.util.List;
import java.util.Map;

import banana.republic.board.Board;
import banana.republic.core.GamePhase;
import banana.republic.player.Player;
import banana.republic.resource.Bank;
import banana.republic.resource.ResourceType;

/**
 * Kumpulan aturan validasi untuk semua jenis trade.
 *
 * <p>Semua method bersifat stateless — menerima parameter dan mengembalikan
 * {@link ValidationResult}. Tidak melempar exception untuk kondisi tidak valid;
 * exception hanya dilempar jika argumen yang tidak boleh null ternyata null.
 */
public class TradeValidator {

    private final MaritimeTrade maritimeTrade = new MaritimeTrade();

    // -------------------------------------------------------------------------
    // Validasi fase
    // -------------------------------------------------------------------------

    /**
     * Memastikan trade hanya bisa dilakukan saat fase {@code TRADE_BUILD}.
     */
    public ValidationResult validatePhase(GamePhase currentPhase) {
        if (currentPhase != GamePhase.TRADE_BUILD) {
            return ValidationResult.fail(
                    "Trade hanya bisa dilakukan saat fase TRADE_BUILD (saat ini: " + currentPhase + ")");
        }
        return ValidationResult.ok();
    }

    // -------------------------------------------------------------------------
    // Validasi offer domestik (pemain ↔ pemain)
    // -------------------------------------------------------------------------

    /**
     * Memvalidasi {@link TradeOffer} dari sisi offerer:
     * <ul>
     *   <li>Offerer harus punya semua resource di {@code offer}</li>
     *   <li>Offer dan request tidak boleh kosong</li>
     *   <li>Target tidak boleh sama dengan offerer</li>
     * </ul>
     */
    public ValidationResult validateOffer(TradeOffer offer, GamePhase phase) {
        if (offer == null) return ValidationResult.fail("Offer tidak boleh null");

        ValidationResult phaseCheck = validatePhase(phase);
        if (!phaseCheck.isValid()) return phaseCheck;

        Player offerer = offer.getOfferer();
        if (offer.getTarget() != null && offerer.equals(offer.getTarget())) {
            return ValidationResult.fail("Tidak bisa trade dengan diri sendiri");
        }

        // Cek offerer punya resource yang ditawarkan
        for (Map.Entry<ResourceType, Integer> e : offer.getOffer().entrySet()) {
            if (e.getValue() <= 0) continue;
            if (!offerer.hasResource(e.getKey(), e.getValue())) {
                return ValidationResult.fail(
                        offerer.getName() + " tidak punya cukup " + e.getKey().getDisplayName()
                        + " (butuh " + e.getValue() + ", punya " + offerer.getResourceCount(e.getKey()) + ")");
            }
        }

        return ValidationResult.ok();
    }

    /**
     * Memvalidasi dari sisi target (apakah target punya resource yang diminta offerer).
     */
    public ValidationResult validateTargetCanFulfill(TradeOffer offer) {
        if (offer == null) return ValidationResult.fail("Offer tidak boleh null");

        Player target = offer.getTarget();
        if (target == null) return ValidationResult.fail("Offer tidak punya target spesifik");

        for (Map.Entry<ResourceType, Integer> e : offer.getRequest().entrySet()) {
            if (e.getValue() <= 0) continue;
            if (!target.hasResource(e.getKey(), e.getValue())) {
                return ValidationResult.fail(
                        target.getName() + " tidak punya cukup " + e.getKey().getDisplayName()
                        + " (butuh " + e.getValue() + ", punya " + target.getResourceCount(e.getKey()) + ")");
            }
        }

        return ValidationResult.ok();
    }

    // -------------------------------------------------------------------------
    // Validasi trade maritim (pemain ↔ bank)
    // -------------------------------------------------------------------------

    /**
     * Memvalidasi trade dengan bank:
     * <ul>
     *   <li>Pemain harus punya resource sebanyak rasio terbaik</li>
     *   <li>Bank harus punya resource yang diminta</li>
     *   <li>Resource yang dijual ≠ resource yang dibeli</li>
     * </ul>
     *
     * @param player   pemain yang trade
     * @param sellType resource yang dijual
     * @param sellAmt  jumlah yang dijual (harus = rasio harbor)
     * @param buyType  resource yang dibeli
     * @param buyAmt   jumlah yang dibeli (harus = 1 per trade)
     * @param bank     bank untuk cek ketersediaan
     * @param board    papan untuk cek harbor
     * @param phase    fase permainan saat ini
     */
    public ValidationResult validateBankTrade(Player player, ResourceType sellType, int sellAmt,
                                               ResourceType buyType, int buyAmt,
                                               Bank bank, Board board, GamePhase phase) {
        if (player == null || sellType == null || buyType == null) {
            return ValidationResult.fail("Parameter tidak boleh null");
        }

        ValidationResult phaseCheck = validatePhase(phase);
        if (!phaseCheck.isValid()) return phaseCheck;

        if (sellType == buyType) {
            return ValidationResult.fail("Resource yang dijual dan dibeli tidak boleh sama");
        }
        if (sellAmt <= 0 || buyAmt <= 0) {
            return ValidationResult.fail("Jumlah harus lebih dari 0");
        }

        // Cek rasio
        int requiredRatio = maritimeTrade.getBestRatio(player, sellType, board);
        if (sellAmt != requiredRatio) {
            return ValidationResult.fail(
                    "Rasio trade untuk " + sellType.getDisplayName() + " adalah " + requiredRatio + ":1"
                    + ", bukan " + sellAmt + ":1");
        }
        if (buyAmt != 1) {
            return ValidationResult.fail("Trade dengan bank hanya menghasilkan 1 resource per transaksi");
        }

        // Cek resource pemain
        if (!player.hasResource(sellType, sellAmt)) {
            return ValidationResult.fail(
                    player.getName() + " tidak punya cukup " + sellType.getDisplayName()
                    + " (butuh " + sellAmt + ", punya " + player.getResourceCount(sellType) + ")");
        }

        // Cek ketersediaan bank
        if (!bank.hasResource(buyType, buyAmt)) {
            return ValidationResult.fail(
                    "Bank tidak punya cukup " + buyType.getDisplayName());
        }

        return ValidationResult.ok();
    }

    // -------------------------------------------------------------------------
    // Validasi broadcast offer (1 offerer ke banyak pemain)
    // -------------------------------------------------------------------------

    /**
     * Memvalidasi bahwa minimal ada 1 pemain selain offerer yang bisa memenuhi broadcast offer.
     */
    public ValidationResult validateBroadcastOffer(TradeOffer offer, List<Player> allPlayers) {
        if (offer == null) return ValidationResult.fail("Offer tidak boleh null");
        if (!offer.isBroadcast()) return ValidationResult.fail("Offer ini bukan broadcast offer");

        Player offerer = offer.getOfferer();
        for (Player p : allPlayers) {
            if (p.equals(offerer)) continue;
            boolean canFulfill = true;
            for (Map.Entry<ResourceType, Integer> e : offer.getRequest().entrySet()) {
                if (!p.hasResource(e.getKey(), e.getValue())) {
                    canFulfill = false;
                    break;
                }
            }
            if (canFulfill) return ValidationResult.ok();
        }

        return ValidationResult.fail("Tidak ada pemain yang bisa memenuhi penawaran ini");
    }
}
