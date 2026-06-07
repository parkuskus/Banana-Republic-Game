package banana.republic.card;

import banana.republic.core.GameState;
import banana.republic.core.LogEntry;
import banana.republic.player.Player;
import banana.republic.resource.ResourceType;

/**
 * Kartu Monopoli Nimon (Monopoly Progress Card).
 *
 * <p>Efek: Pemain menyebutkan 1 jenis sumber daya. Semua pemain lain
 * yang punya sumber daya jenis itu HARUS menyerahkan semuanya.
 *
 * <p>Komposisi deck: 3 kartu.
 */
public class MonopolyCard extends ProgressCard {
    private ResourceType targetResource;

    /**
     * Constructor default.
     *
     * <p>{@code targetResource} diset ketika pemain memilih jenis
     * sumber daya yang akan dimonopoli.
     */
    public MonopolyCard() {
        super();
        this.targetResource = null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getCardName() {
        return "Kartu Monopoli Nimon (Monopoly)";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDescription() {
        if (targetResource == null) {
            return "Sebutkan 1 jenis sumber daya. Semua pemain lain yang punya sumber daya itu " +
                    "HARUS menyerahkan SEMUA kartu jenis tersebut kepada Anda.";
        } else {
            return "Ambil semua kartu " + targetResource.getDisplayName() + " dari pemain lain!";
        }
    }

    /**
     * Mengembalikan jenis sumber daya target yang sedang dipilih.
     *
     * @return {@link ResourceType} yang dipilih, atau {@code null} jika belum dipilih
     */
    public ResourceType getTargetResource() {
        return targetResource;
    }

    /**
     * Mengatur jenis sumber daya yang akan dimonopoli.
     *
     * @param type jenis sumber daya target
     */
    public void setTargetResource(ResourceType type) {
        this.targetResource = type;
    }

    /**
     * Mengambil semua kartu {@code targetResource} dari pemain lain
     * dan menyerahkannya ke pemain yang memainkan kartu ini.
     *
     * @param state  state permainan saat ini
     * @param player pemain yang memainkan kartu
     */
    @Override
    public void applyEffect(GameState state, Player player) {
        assert player != null : "Player harus tidak null saat mainkan MonopolyCard";
        assert state != null : "GameState harus tidak null saat mainkan MonopolyCard";
        assert targetResource != null : "Target resource harus diset sebelum applyEffect Monopoly";

        boolean anyStolen = false;
        for (Player other : state.getAllPlayers()) {
            if (!other.equals(player)) {
                int count = other.getResourceCount(targetResource);
                if (count > 0) {
                    other.removeResource(targetResource, count);
                    player.addResource(targetResource, count);
                    state.getGameLog().addEntry(
                        LogEntry.EventType.CARD_PLAYED,
                        player.getName(),
                        player.getName() + " menggunakan Monopoli Nimon: mengambil "
                            + count + " " + targetResource.getDisplayName()
                            + " dari " + other.getName()
                    );
                    anyStolen = true;
                }
            }
        }

        if (!anyStolen) {
            state.getGameLog().addEntry(
                LogEntry.EventType.CARD_PLAYED,
                player.getName(),
                player.getName() + " menggunakan Monopoli Nimon: tidak ada pemain memiliki "
                    + targetResource.getDisplayName()
            );
        }

        this.reveal();
        this.consume();
    }

    /**
     * Monopoly Card bisa dimainkan jika bukan newly-drawn dan belum consumed.
     *
     * @return {@code true} jika playable
     */
    @Override
    public boolean isPlayable() {
        return !isNewlyDrawn() && !isConsumed();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CardType getCardType() {
        return CardType.MONOPOLY;
    }
}
