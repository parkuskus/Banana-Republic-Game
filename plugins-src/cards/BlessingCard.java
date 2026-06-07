package cards;

import banana.republic.card.CardType;
import banana.republic.card.ExperimentCard;
import banana.republic.core.GameState;
import banana.republic.player.Player;
import banana.republic.resource.ResourceType;

/**
 * Kartu Blessing: mengambil 1 kartu dari setiap jenis sumber daya yang tersedia
 * di bank.
 *
 * <p>
 * Plugin card example: dipaketkan sebagai BlessingCard.jar dan dimuat secara
 * runtime via Settings &amp; Plugins dialog.
 *
 * <p>
 * Kartu ini langka dan kuat — diperlakukan sebagai VICTORY_POINT dalam hal
 * visibilitas (secret / tersembunyi).
 */
public class BlessingCard implements ExperimentCard {

    /** No-arg constructor (wajib untuk PluginLoader). */
    public BlessingCard() {}

    @Override
    public String getCardName() {
        return "Kartu Berkah (Blessing Card)";
    }

    @Override
    public String getDescription() {
        return "Ambil 1 kartu dari setiap jenis sumber daya "
            + "yang tersedia di bank. Kartu ini bersifat rahasia.";
    }

    @Override
    public void applyEffect(GameState state, Player player) {
        assert player != null
            : "Player harus tidak null saat mainkan BlessingCard";
        assert state != null
            : "GameState harus tidak null saat mainkan BlessingCard";

        var bank = state.getBank();

        for (ResourceType type : ResourceType.values()) {
            if (bank.hasResource(type, 1)) {
                bank.takeResource(type, 1);
                player.addResource(type, 1);
            }
        }
    }

    @Override
    public boolean isPlayable() {
        return true;
    }

    @Override
    public boolean isSecret() {
        return true; // Langka — tetap tersembunyi
    }

    @Override
    public CardType getCardType() {
        return CardType.VICTORY_POINT;
    }

    @Override
    public boolean isPluginCard() {
        return true;
    }
}
