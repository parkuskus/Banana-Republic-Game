package volcano;

import banana.republic.board.TerrainType;
import banana.republic.plugin.StandardMapGenerator;

/**
 * Volcano Island — peta alternatif bertema gunung berapi.
 *
 * <p>
 * Pola: gunung dan bukit mengelilingi gurun di tengah (seperti cincin
 * vulkanik), hutan/kebun/ladang di bagian luar.
 *
 * <p>
 * Dipaketkan sebagai VolcanoIsland.jar — dimuat dari Lobby sebelum game
 * dimulai.
 */
public class VolcanoIslandMapGenerator extends StandardMapGenerator {

    // 4 FOREST, 3 HILL, 4 FIELD, 3 MOUNTAIN, 4 BANANA_PLANTATION, 1 DESERT
    private static final TerrainType[] VOLCANO_HEX_LAYOUT = new TerrainType[] {
        // Row -2 (3 tiles)
        TerrainType.FOREST,
        TerrainType.BANANA_PLANTATION,
        TerrainType.FIELD,
        // Row -1 (4 tiles)
        TerrainType.BANANA_PLANTATION,
        TerrainType.MOUNTAIN,
        TerrainType.HILL,
        TerrainType.FOREST,
        // Row 0 (5 tiles) — ring vulkanik di sekitar desert
        TerrainType.HILL,
        TerrainType.MOUNTAIN,
        TerrainType.DESERT, // pusat — Nimon Ungu mulai
                            // di sini
        TerrainType.MOUNTAIN,
        TerrainType.BANANA_PLANTATION,
        // Row 1 (4 tiles)
        TerrainType.FIELD,
        TerrainType.HILL,
        TerrainType.FOREST,
        TerrainType.BANANA_PLANTATION,
        // Row 2 (3 tiles)
        TerrainType.FIELD,
        TerrainType.FOREST,
        TerrainType.FIELD,
    };

    private static final int[] VOLCANO_TOKEN_LAYOUT = new int[] {
        8, 3, 11, 4, 9, 6, 5, 10, 12, 2, 8, 9, 4, 10, 5, 6, 3, 11,
    };

    @Override
    protected TerrainType[] getHexLayout() {
        return VOLCANO_HEX_LAYOUT.clone();
    }

    @Override
    protected int[] getTokenLayout() {
        return VOLCANO_TOKEN_LAYOUT.clone();
    }
}
