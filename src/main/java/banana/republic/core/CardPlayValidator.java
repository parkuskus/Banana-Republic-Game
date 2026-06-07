package banana.republic.core;

import banana.republic.card.ExperimentCard;
import banana.republic.player.Player;
import banana.republic.trade.ValidationResult;

/**
 * Memvalidasi pre-condition sebelum pemain memainkan Kartu Temuan.
 *
 * <p>Semua method bersifat stateless dan mengembalikan {@link ValidationResult}.
 */
public class CardPlayValidator {

    /**
     * Memvalidasi apakah {@code player} boleh memainkan {@code card} saat ini.
     *
     * Cek:
     * <ul>
     *   <li>Phase harus {@code TRADE_BUILD}</li>
     *   <li>{@code player} dan {@code card} tidak boleh null</li>
     *   <li>Kartu ada di tangan pemain</li>
     *   <li>Kartu bukan yang baru dibeli giliran ini</li>
     *   <li>Belum ada kartu lain yang dimainkan giliran ini</li>
     *   <li>{@link ExperimentCard#isPlayable()} harus true</li>
     * </ul>
     *
     * @param player              pemain yang ingin memainkan kartu
     * @param card                kartu yang ingin dimainkan
     * @param cardBoughtThisTurn  kartu yang dibeli giliran ini (boleh null)
     * @param cardPlayedThisTurn  kartu yang sudah dimainkan giliran ini (boleh null)
     * @param phase               fase permainan saat ini
     */
    public ValidationResult canPlay(Player player, ExperimentCard card,
                                    ExperimentCard cardBoughtThisTurn,
                                    ExperimentCard cardPlayedThisTurn,
                                    GamePhase phase) {
        if (player == null || card == null) {
            throw new IllegalArgumentException("Player dan kartu tidak boleh null");
        }
        if (phase != GamePhase.RESOURCE_GATHERING && phase != GamePhase.TRADE_BUILD) {
            return ValidationResult.fail(
                "Kartu hanya bisa dimainkan saat giliran aktif (sebelum/sesudah lempar dadu). Fase saat ini: "
                    + phase);
        }
        if (!player.getHandCards().contains(card)) {
            return ValidationResult.fail(
                player.getName() + " tidak punya kartu " + card.getCardName() + " di tangan");
        }
        if (card == cardBoughtThisTurn) {
            return ValidationResult.fail(
                "Kartu yang baru dibeli tidak bisa langsung dimainkan giliran ini");
        }
        if (cardPlayedThisTurn != null) {
            return ValidationResult.fail("Hanya boleh memainkan 1 kartu per giliran");
        }
        if (!card.isPlayable()) {
            return ValidationResult.fail(
                "Kartu " + card.getCardName() + " tidak bisa dimainkan saat ini");
        }
        return ValidationResult.ok();
    }
}
