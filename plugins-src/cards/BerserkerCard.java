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
 * Kartu Berserk — mencuri 1 resource acak dari pemain lain mana pun tanpa
 * memerlukan adjacency Nimon Ungu.
 *
 * <p>
 * Plugin card example: dipaketkan sebagai BerserkerCard.jar dan dimuat secara
 * runtime via Settings &amp; Plugins dialog.
 */
public class BerserkerCard implements ExperimentCard {

    private final Random random = new Random();

    /** No-arg constructor (wajib untuk PluginLoader). */
    public BerserkerCard() {}

    @Override
    public String getCardName() {
        return "Kartu Pengamuk (Berserker Card)";
    }

    @Override
    public String getDescription() {
        return "Curi 1 kartu sumber daya acak dari pemain lain mana pun. "
            + "Tidak perlu adjacency Nimon Ungu.";
    }

    @Override
    public void applyEffect(GameState state, Player player) {
        assert player != null
            : "Player harus tidak null saat mainkan BerserkerCard";
        assert state != null
            : "GameState harus tidak null saat mainkan BerserkerCard";

        // Cari pemain lain yang memiliki minimal 1 resource
        List<Player> candidates = new ArrayList<>();
        for (Player other : state.getAllPlayers()) {
            if (!other.equals(player) && other.getTotalResourceCount() > 0) {
                candidates.add(other);
            }
        }

        if (candidates.isEmpty()) {
            return; // Tidak ada yang bisa dicuri
        }

        // Pilih korban acak
        Player victim = candidates.get(random.nextInt(candidates.size()));

        // Cari resource yang dimiliki korban
        List<ResourceType> owned = new ArrayList<>();
        for (ResourceType type : ResourceType.values()) {
            if (victim.hasResource(type, 1)) {
                owned.add(type);
            }
        }

        if (owned.isEmpty()) {
            return;
        }

        ResourceType stolen = owned.get(random.nextInt(owned.size()));
        victim.removeResource(stolen, 1);
        player.addResource(stolen, 1);
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
