package donut;

import banana.republic.board.Board;
import banana.republic.board.TerrainType;
import banana.republic.plugin.StandardMapGenerator;

/**
 * Example external map plugin packaged as DonutIsland.jar.
 *
 * This plugin keeps the Banana Republic-compliant 19-hex topology and terrain
 * counts, but changes the terrain and token placement so it is visibly distinct
 * from the standard map when loaded from the lobby.
 */
public class DonutIslandMapGenerator extends StandardMapGenerator {

    @Override
    protected TerrainType[] getHexLayout() {
        return new TerrainType[] {
            // Row -2
            TerrainType.BANANA_PLANTATION,
            TerrainType.FIELD,
            TerrainType.BANANA_PLANTATION,
            // Row -1
            TerrainType.FOREST,
            TerrainType.MOUNTAIN,
            TerrainType.HILL,
            TerrainType.FIELD,
            // Row 0
            TerrainType.HILL,
            TerrainType.FOREST,
            TerrainType.DESERT,
            TerrainType.FOREST,
            TerrainType.MOUNTAIN,
            // Row 1
            TerrainType.FIELD,
            TerrainType.BANANA_PLANTATION,
            TerrainType.HILL,
            TerrainType.FOREST,
            // Row 2
            TerrainType.BANANA_PLANTATION,
            TerrainType.MOUNTAIN,
            TerrainType.FIELD
        };
    }

    @Override
    protected int[] getTokenLayout() {
        return new int[] {
            6, 3, 11,
            4, 8, 10, 5,
            9, 12, 2, 8,
            10, 4, 9, 5,
            6, 3, 11
        };
    }
}
