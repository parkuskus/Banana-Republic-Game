package volcano;

import banana.republic.board.TerrainType;
import banana.republic.plugin.StandardMapGenerator;

/**
 * Alternative external map plugin packaged as VolcanoIsland.jar.
 *
 * Keeps the required Banana Republic terrain counts and 19-hex topology while
 * using a clearly different terrain/token arrangement from Standard and Donut.
 */
public class VolcanoIslandMapGenerator extends StandardMapGenerator {

    @Override
    protected TerrainType[] getHexLayout() {
        return new TerrainType[] {
            // Row -2
            TerrainType.MOUNTAIN,
            TerrainType.FOREST,
            TerrainType.HILL,
            // Row -1
            TerrainType.BANANA_PLANTATION,
            TerrainType.FIELD,
            TerrainType.FOREST,
            TerrainType.MOUNTAIN,
            // Row 0
            TerrainType.FIELD,
            TerrainType.HILL,
            TerrainType.DESERT,
            TerrainType.BANANA_PLANTATION,
            TerrainType.FIELD,
            // Row 1
            TerrainType.FOREST,
            TerrainType.BANANA_PLANTATION,
            TerrainType.HILL,
            TerrainType.MOUNTAIN,
            // Row 2
            TerrainType.BANANA_PLANTATION,
            TerrainType.FIELD,
            TerrainType.FOREST
        };
    }

    @Override
    protected int[] getTokenLayout() {
        return new int[] {
            11, 4, 10,
            5, 9, 3, 8,
            6, 12, 2, 5,
            10, 4, 9, 11,
            3, 8, 6
        };
    }
}
