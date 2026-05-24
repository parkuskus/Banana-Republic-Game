package banana.republic.card;

import banana.republic.core.GameState;
import banana.republic.player.Player;
import banana.republic.robber.Robber;

/**
 * Knight Card (Kartu Penjaga).
 * Memungkinkan pemain memindahkan Nimon Ungu tanpa penalti discard fase.
 *
 * Jumlah: 14 kartu
 * Efek: Pindahkan Nimon Ungu ke petak lain, boleh curi 1 kartu acak dari pemain terdekat.
 */
public class KnightCard extends DevelopmentCard {

    @Override
    public String getCardName() {
        return "Kartu Penjaga (Knight)";
    }

    @Override
    public String getDescription() {
        return "Pindahkan Nimon Ungu ke petak terrain lain. " +
                "Anda boleh mencuri 1 kartu sumber daya acak dari pemain yang punya bangunan " +
                "di sekitar lokasi baru Nimon Ungu.";
    }

    @Override
    public void applyEffect(GameState state, Player player) {
        // Efek Knight:
        // 1. Pindahkan Nimon Ungu (via Robber.move())
        // 2. Boleh curi kartu acak dari pemain di sekitar lokasi baru
        //
        // Catatan: Logika pindah tile, curi, dan validasi pemain
        // dilakukan di controller/TurnManager (tidak di kartu).
        // Kartu ini hanya marker untuk trigger mekanisme.

        assert player != null : "Player harus tidak null saat mainkan KnightCard";
        assert state != null : "GameState harus tidak null saat mainkan KnightCard";

        // Increment knights played counter untuk Largest Army tracking
        player.incrementKnightsPlayed();

        // Reveal kartu karena sudah dimainkan
        this.reveal();
    }

    @Override
    public boolean isPlayable() {
        // Knight bisa dimainkan kapan saja, bahkan jika baru diambil
        // (berbeda dengan Progress Card yang tidak bisa langsung dimainkan).
        // Tapi ada aturan: max 1 kartu per giliran.
        return true;
    }

    @Override
    public boolean isSecret() {
        // Knight terungkap ketika dimainkan
        // Tapi sebelum dimainkan, tersembunyi
        return !revealed;
    }

    @Override
    public CardType getCardType() {
        return CardType.KNIGHT;
    }
}
