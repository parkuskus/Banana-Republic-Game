package banana.republic.save;

import java.util.List;

/**
 * Board save data DTO.
 * Refer to class-diagram/Module5_UI_Plugin_Save.puml for full specification.
 */
public class BoardSaveData {
    public List<HexTileSaveData> hexTiles;
    public List<IntersectionSaveData> intersections;
    public List<PathSaveData> paths;
    public List<HarborSaveData> harbors;
}
