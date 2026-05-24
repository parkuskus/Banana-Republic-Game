package banana.republic.card;

import banana.republic.core.GameState;
import banana.republic.player.Player;
import banana.republic.resource.ResourceType;

/**
 * Monopoly progress card (Monopoli Nimon).
 * Jumlah: 3 kartu
 * Efek: Pemain menyebutkan 1 jenis sumber daya.
 *       Semua pemain lain yang punya sumber daya jenis itu HARUS menyerahkan semuanya.
 */
public class MonopolyCard extends ProgressCard {
    private ResourceType targetResource;

    /**
     * Constructor untuk Monopoly Card.
     * targetResource diset ketika pemain memilih jenis sumber daya yang dimonopoli.
     */
    public MonopolyCard() {
        super();
        this.targetResource = null;
    }

    @Override
    public String getCardName() {
        return "Kartu Monopoli Nimon (Monopoly)";
    }

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
     * Get target resource.
     */
    public ResourceType getTargetResource() {
        return targetResource;
    }

    /**
     * Set target resource yang akan dimonopoli.
     */
    public void setTargetResource(ResourceType type) {
        this.targetResource = type;
    }

    @Override
    public void applyEffect(GameState state, Player player) {
        // Efek Monopoly:
        // 1. Validasi targetResource sudah diset
        // 2. Iterasi semua pemain lain
        // 3. Ambil semua kartu targetResource dari mereka

        assert player != null : "Player harus tidak null saat mainkan MonopolyCard";
        assert state != null : "GameState harus tidak null saat mainkan MonopolyCard";
        assert targetResource != null : "Target resource harus diset sebelum applyEffect Monopoly";

        // Ref
        // Iterasi pemain lain dan ambil kartu mereka
        for (Player other : state.getAllPlayers()) {
            if (!other.equals(player)) {
                // Transfer semua kartu jenis targetResource dari other ke player
                int count = other.getResourceCount(targetResource);
                if (count > 0) {
                    other.removeResource(targetResource, count);
                    player.addResource(targetResource, count);
                }
            }
        }

        // Reveal kartu
        this.reveal();

        // Consume kartu karena sudah digunakan
        this.consume();
    }

    @Override
    public boolean isPlayable() {
        // Monopoly card bisa dimainkan, tapi perlu pilih resource dulu
        // Jika sudah dimainkan sebelumnya (consumed), tidak bisa dimainkan lagi
        return !isNewlyDrawn() && !isConsumed();
    }

    @Override
    public CardType getCardType() {
        return CardType.MONOPOLY;
    }
}
