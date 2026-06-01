package banana.republic.save;

import java.util.List;
import java.util.Map;

/**
 * Data structure for saved game state.
 * Refer to class-diagram/Module5_UI_Plugin_Save.puml for full specification.
 *
 * <p>Field {@code loadedPlugins} mencatat FQCN semua kartu plugin yang aktif
 * saat game disimpan. UI dapat membaca field ini untuk menampilkan notifikasi:
 * <em>"Save ini memerlukan plugin: HealCard. Silakan load plugin sebelum melanjutkan."</em>
 */
public class GameSaveData {
    public String  version;
    public String  timestamp;
    public String  currentPhase;
    public int     turnNumber;
    public int     activePlayerIndex;
    public int     setupSettlementCount;
    public TimerSaveData          timer;
    public Map<String, Integer>   bank;
    public BoardSaveData          board;
    public List<PlayerSaveData>   players;
    public CardDeckSaveData       deck;
    public DiceSaveData           lastDice;

    /**
     * FQCN semua plugin kartu yang sedang aktif di sesi ini.
     * Kosong / null jika tidak ada plugin kartu yang digunakan.
     * Digunakan oleh UI untuk memperingatkan user saat load.
     */
    public List<String> loadedPlugins;
}
