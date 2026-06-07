package crescent;

import banana.republic.board.TerrainType;
import banana.republic.plugin.StandardMapGenerator;

/**
 * Crescent Island — peta alternatif bertema bulan sabit.
 *
 * <p>Pola: terrain serupa dikelompokkan dalam busur membentuk
 * pola bulan sabit. Gurun tetap di pusat papan.
 *
 * <p>Dipaketkan sebagai CrescentIsland.jar — dimuat dari Lobby
 * sebelum game dimulai.
 */
public class CrescentIslandMapGenerator extends StandardMapGenerator {

    // 4 FOREST, 3 HILL, 4 FIELD, 3 MOUNTAIN, 4 BANANA_PLANTATION, 1 DESERT
    private static final TerrainType[] CRESCENT_HEX_LAYOUT = new TerrainType[]{
        // Row -2 (3 tiles)
        TerrainType.FOREST,
        TerrainType.FOREST,
        TerrainType.BANANA_PLANTATION,
        // Row -1 (4 tiles)
        TerrainType.HILL,
        TerrainType.FIELD,
        TerrainType.BANANA_PLANTATION,
        TerrainType.FIELD,
        // Row 0 (5 tiles)
        TerrainType.MOUNTAIN,
        TerrainType.HILL,
        TerrainType.DESERT,        // pusat
        TerrainType.BANANA_PLANTATION,
        TerrainType.MOUNTAIN,
        // Row 1 (4 tiles)
        TerrainType.FIELD,
        TerrainType.MOUNTAIN,
        TerrainType.HILL,
        TerrainType.BANANA_PLANTATION,
        // Row 2 (3 tiles)
        TerrainType.FOREST,
        TerrainType.FIELD,
        TerrainType.FOREST,
    };

    // 1×2, 2×{3,4,5,6,8,9,10,11}, 1×12 — no 7
    private static final int[] CRESCENT_TOKEN_LAYOUT = new int[]{
        5, 2, 6,
        3, 10, 8, 9,
        12, 4,      11, 5,
        10, 9, 3, 6,
        8, 4, 11,
    };

    @Override
    protected TerrainType[] getHexLayout() {
        return CRESCENT_HEX_LAYOUT.clone();
    }

    @Override
    protected int[] getTokenLayout() {
        return CRESCENT_TOKEN_LAYOUT.clone();
    }
}
