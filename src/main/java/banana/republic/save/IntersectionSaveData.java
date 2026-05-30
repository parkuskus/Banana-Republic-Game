package banana.republic.save;

import java.util.List;

/**
 * Intersection save data DTO.
 */
public class IntersectionSaveData {
    public int id;
    public List<Integer> adjacentHexTileIds;
    public BuildingSaveData building;
}
