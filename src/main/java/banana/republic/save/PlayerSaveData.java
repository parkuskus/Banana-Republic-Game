package banana.republic.save;

import java.util.List;
import java.util.Map;

/**
 * Player save data DTO.
 * Refer to class-diagram/Module5_UI_Plugin_Save.puml for full specification.
 */
public class PlayerSaveData {
    public String name;
    public String color;
    public boolean isBot;
    public Map<String, Integer> resources;
    public Map<String, Boolean> specialCards;
    public int knightsPlayed;
    public int longestRoadLength;
    public List<CardSaveData> handCards;
}
