package banana.republic.save;

import java.util.List;
import java.util.Map;

/**
 * Data structure for saved game state.
 * Refer to class-diagram/Module5_UI_Plugin_Save.puml for full specification.
 */
public class GameSaveData {
    public String version;
    public String timestamp;
    public String currentPhase;
    public int turnNumber;
    public int activePlayerIndex;
    public int setupSettlementCount;
    public TimerSaveData timer;
    public Map<String, Integer> bank;
    public BoardSaveData board;
    public List<PlayerSaveData> players;
    public CardDeckSaveData deck;
    public DiceSaveData lastDice;
}
