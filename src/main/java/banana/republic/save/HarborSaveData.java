package banana.republic.save;

import java.util.List;

/**
 * Harbor save data DTO.
 * Refer to class-diagram/Module5_UI_Plugin_Save.puml for full specification.
 */
public class HarborSaveData {
    public int id;
    public String type;
    public List<Integer> adjacentIntersectionIds;
}
