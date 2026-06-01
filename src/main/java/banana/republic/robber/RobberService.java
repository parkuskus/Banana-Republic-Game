package banana.republic.robber;

import banana.republic.player.Player;
import banana.republic.resource.Bank;
import banana.republic.resource.ResourceType;

import java.util.HashMap;
import java.util.Map;

/**
 * Mengelola logika otomatis Nimon Ungu yang tidak memerlukan interaksi UI.
 *
 * <p>Saat ini tanggung jawabnya adalah satu:
 * memproses pembuangan resource otomatis untuk <strong>bot player</strong>
 * ketika dadu menghasilkan 7.
 *
 * <p>Untuk human player, UI tetap yang memicu {@code Game.discardResource()}
 * satu per satu — kelas ini tidak menangani itu.
 */
public class RobberService {

    /**
     * Memproses pembuangan resource otomatis untuk semua bot dalam {@code discardMap}.
     *
     * <p>Setiap bot membuang resource secara greedy dari urutan
     * pertama {@link ResourceType#values()} sampai jumlah {@code toDiscard} terpenuhi.
     *
     * <p>Human player dalam {@code discardMap} dilewati — mereka ditangani UI.
     *
     * @param discardMap map dari {@link Robber#activateDiscardPhase} —
     *                   memetakan pemain ke jumlah kartu yang harus dibuang
     * @param bank       bank tujuan resource yang dibuang
     * @return map bot → jumlah resource yang benar-benar dibuang,
     *         hanya berisi entry untuk bot (bukan human)
     */
    public Map<Player, Integer> discardForBots(Map<Player, Integer> discardMap, Bank bank) {
        if (discardMap == null || bank == null) {
            throw new IllegalArgumentException("discardMap dan bank tidak boleh null");
        }

        Map<Player, Integer> result = new HashMap<>();

        for (Map.Entry<Player, Integer> entry : discardMap.entrySet()) {
            Player player = entry.getKey();
            if (!player.isBot()) {
                continue; // Human dihandle UI
            }

            int toDiscard = entry.getValue();
            int discarded = 0;

            for (ResourceType type : ResourceType.values()) {
                if (discarded >= toDiscard) break;
                int count   = player.getResourceCount(type);
                int remove  = Math.min(count, toDiscard - discarded);
                if (remove > 0) {
                    player.removeResource(type, remove);
                    bank.returnResource(type, remove);
                    discarded += remove;
                }
            }

            result.put(player, discarded);
        }

        return result;
    }
}
