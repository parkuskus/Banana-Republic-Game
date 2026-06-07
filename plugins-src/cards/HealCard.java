package cards;

import banana.republic.card.CardType;
import banana.republic.card.ExperimentCard;
import banana.republic.core.GameState;
import banana.republic.player.Player;
import banana.republic.resource.ResourceType;
import java.util.Random;

/**
 * Kartu Heal — mengambil 2 sumber daya acak dari bank.
 *
 * <p>
 * Plugin card example: dipaketkan sebagai HealCard.jar dan dimuat secara
 * runtime via Settings &amp; Plugins dialog.
 */
public class HealCard implements ExperimentCard {

    private final Random random = new Random();

    /** No-arg constructor (wajib untuk PluginLoader). */
    public HealCard() {}

    @Override
    public String getCardName() {
        return "Kartu Penyembuhan (Heal Card)";
    }

    @Override
    public String getDescription() {
        return "Ambil 2 sumber daya acak dari bank. "
            + "Sumber daya diambil secara acak dari jenis yang tersedia.";
    }

    @Override
    public void applyEffect(GameState state, Player player) {
        assert player != null : ("Player harus tidak null saat mainkan "
                                 + "HealCard");
        assert state != null
            : "GameState harus tidak null saat mainkan HealCard";

        var bank = state.getBank();
        var types = ResourceType.values();

        for (int i = 0; i < 2; i++) {
            ResourceType type = types[random.nextInt(types.length)];
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
        return false;
    }

    @Override
    public CardType getCardType() {
        return CardType.KNIGHT;
    }

    @Override
    public boolean isPluginCard() {
        return true;
    }
}
