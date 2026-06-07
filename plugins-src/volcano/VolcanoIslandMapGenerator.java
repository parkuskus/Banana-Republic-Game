package volcano;

import banana.republic.board.TerrainType;
import banana.republic.plugin.StandardMapGenerator;

/**
<<<<<<< HEAD
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
=======
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
>>>>>>> origin/integrate-gui
    }

    @Override
    protected int[] getTokenLayout() {
<<<<<<< HEAD
        return new int[] {
            11, 4, 10,
            5, 9, 3, 8,
            6, 12, 2, 5,
            10, 4, 9, 11,
            3, 8, 6
        };
=======
        return VOLCANO_TOKEN_LAYOUT.clone();
>>>>>>> origin/integrate-gui
    }
}
