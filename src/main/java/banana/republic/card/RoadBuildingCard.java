package banana.republic.card;

import banana.republic.core.GameState;
import banana.republic.player.Player;

/**
 * Road building progress card.
 * Refer to class-diagram/Module3_Cards_Robber_Timer.puml for full specification.
 */
public class RoadBuildingCard extends ProgressCard {
    @Override
    public String getCardName() {
        return "Kartu Konstruksi Cepat (Road Building)";
    }

    @Override
    public String getDescription() {
        return "Tempatkan 2 Pipa Transportasi baru secara GRATIS di mana pun sesuai aturan pembangunan.";
    }

    @Override
    public void applyEffect(GameState state, Player player) {
        // Efek Road Building:
        // Pemain mendapat akses untuk menempatkan 2 pipa gratis.
        // Logika placement dilakukan di controller (player memilih posisi).
        // Card ini hanya marker untuk meng-unlock akses tersebut.

        assert player != null : "Player harus tidak null saat mainkan RoadBuildingCard";
        assert state != null : "GameState harus tidak null saat mainkan RoadBuildingCard";

        // Reveal kartu
        this.reveal();

        // Consume kartu karena sudah digunakan
        this.consume();
    }

    @Override
    public CardType getCardType() {
        return CardType.ROAD_BUILDING;
    }
}
