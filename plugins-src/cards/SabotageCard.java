package cards;

import banana.republic.card.CardType;
import banana.republic.card.ExperimentCard;
import banana.republic.core.GameState;
import banana.republic.player.Player;
import banana.republic.resource.ResourceType;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Kartu Sabotage — pemain target membuang 2 sumber daya acak ke bank.
 *
 * <p>
 * Plugin card example: dipaketkan sebagai SabotageCard.jar dan dimuat secara
 * runtime via Settings &amp; Plugins dialog.
 */
public class SabotageCard implements ExperimentCard {

    private final Random random = new Random();

    /** No-arg constructor (wajib untuk PluginLoader). */
    public SabotageCard() {}

    @Override
    public String getCardName() {
        return "Kartu Sabotase (Sabotage Card)";
    }

    @Override
    public String getDescription() {
        return "Pilih satu pemain lain. Pemain itu harus membuang "
            + "2 sumber daya acak ke bank.";
    }

    @Override
    public void applyEffect(GameState state, Player player) {
        assert player != null
            : "Player harus tidak null saat mainkan SabotageCard";
        assert state != null
            : "GameState harus tidak null saat mainkan SabotageCard";

        var bank = state.getBank();
        var allPlayers = state.getAllPlayers();

        // Cari pemain lain yang memiliki minimal 1 resource
        List<Player> targets = new ArrayList<>();
        for (Player other : allPlayers) {
            if (!other.equals(player) && other.getTotalResourceCount() > 0) {
                targets.add(other);
            }
        }

        if (targets.isEmpty()) {
            return; // Tidak ada pemain valid untuk disabotase
        }

        // Pilih target pertama (UI seharusnya menyediakan pilihan,
        // plugin ini menggunakan pemain pertama yang valid)
        Player target = targets.get(0);

        // Buang 2 resource acak dari target
        for (int i = 0; i < 2; i++) {
            var types = ResourceType.values();
            // Kumpulkan tipe resource yang dimiliki target
            List<ResourceType> owned = new ArrayList<>();
            for (ResourceType type : types) {
                if (target.hasResource(type, 1)) {
                    owned.add(type);
                }
            }
            if (owned.isEmpty()) {
                break;
            }
            ResourceType chosen = owned.get(random.nextInt(owned.size()));
            target.removeResource(chosen, 1);
            bank.returnResource(chosen, 1);
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
