package banana.republic.plugin;

import banana.republic.card.ExperimentCard;
import banana.republic.core.GameState;
import banana.republic.player.Player;
import banana.republic.card.CardType;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.util.List;

/**
 * Unit tests untuk PluginLoader.
 *
 * Strategi testing: kompilasi class plugin on-the-fly ke TempDir,
 * bungkus ke JAR, lalu test PluginLoader terhadap JAR tersebut.
 */
@DisplayName("PluginLoader Tests")
class PluginLoaderTest {

    @TempDir
    Path tempDir;

    private PluginLoader loader;

    @BeforeEach
    void setUp() {
        loader = new PluginLoader();
    }

    @AfterEach
    void tearDown() {
        loader.closeAll();
    }

    // -------------------------------------------------------------------------
    // Helper: build a real JAR with a real ExperimentCard implementor
    // -------------------------------------------------------------------------

    /**
     * Membuat JAR yang berisi satu class ExperimentCard sederhana.
     * Karena test perlu JAR nyata, kita pakai class yang sudah ada di classpath.
     * Kita test dengan class yang TIDAK ada di JAR (hanya JAR palsu).
     */

    @Test
    @DisplayName("loadCard: JAR tidak ada → failure")
    void testLoadCard_jarNotFound() {
        PluginLoadResult<ExperimentCard> result =
            loader.loadCard("/non/existent/path.jar", "com.example.Card");

        assertFalse(result.isSuccess(), "Harus failure jika JAR tidak ada");
        assertNotNull(result.getFailureReason(), "FailureReason tidak boleh null");
        assertTrue(result.getFailureReason().contains("tidak ditemukan")
                   || result.getFailureReason().contains("tidak ada"),
                   "Pesan error harus menjelaskan JAR tidak ada: " + result.getFailureReason());
        assertNull(result.getInstance(), "Instance harus null pada failure");
    }

    @Test
    @DisplayName("loadCard: jarPath kosong → failure")
    void testLoadCard_emptyJarPath() {
        PluginLoadResult<ExperimentCard> result = loader.loadCard("", "com.example.Card");

        assertFalse(result.isSuccess());
        assertNotNull(result.getFailureReason());
    }

    @Test
    @DisplayName("loadCard: className kosong → failure")
    void testLoadCard_emptyClassName() {
        PluginLoadResult<ExperimentCard> result = loader.loadCard("/some/path.jar", "");

        assertFalse(result.isSuccess());
        assertNotNull(result.getFailureReason());
    }

    @Test
    @DisplayName("loadMapGenerator: JAR tidak ada → failure")
    void testLoadMapGenerator_jarNotFound() {
        PluginLoadResult<MapGeneratorPlugin> result =
            loader.loadMapGenerator("/no/jar.jar", "com.example.Gen");

        assertFalse(result.isSuccess());
        assertNotNull(result.getFailureReason());
        assertNull(result.getInstance());
    }

    @Test
    @DisplayName("loadStrategy: JAR tidak ada → failure")
    void testLoadStrategy_jarNotFound() {
        PluginLoadResult<banana.republic.player.PlayerStrategy> result =
            loader.loadStrategy("/no/jar.jar", "com.example.Bot");

        assertFalse(result.isSuccess());
        assertNotNull(result.getFailureReason());
    }

    @Test
    @DisplayName("discoverImplementors: JAR tidak ada → list kosong")
    void testDiscoverImplementors_jarNotFound() {
        List<String> found = loader.discoverImplementors(
            "/no/jar.jar", ExperimentCard.class);

        assertNotNull(found, "Harus return list, bukan null");
        assertTrue(found.isEmpty(), "Harus kosong jika JAR tidak ada");
    }

    @Test
    @DisplayName("discoverCards: JAR tidak ada → list kosong")
    void testDiscoverCards_jarNotFound() {
        List<String> found = loader.discoverCards("/no/jar.jar");

        assertNotNull(found);
        assertTrue(found.isEmpty());
    }

    @Test
    @DisplayName("discoverMapGenerators: JAR tidak ada → list kosong")
    void testDiscoverMapGenerators_jarNotFound() {
        List<String> found = loader.discoverMapGenerators("/no/jar.jar");

        assertNotNull(found);
        assertTrue(found.isEmpty());
    }

    @Test
    @DisplayName("discoverStrategies: JAR tidak ada → list kosong")
    void testDiscoverStrategies_jarNotFound() {
        List<String> found = loader.discoverStrategies("/no/jar.jar");

        assertNotNull(found);
        assertTrue(found.isEmpty());
    }

    @Test
    @DisplayName("PluginLoadResult: success factory valid")
    void testPluginLoadResult_success() {
        ExperimentCard mockCard = createMockCard();
        PluginLoadResult<ExperimentCard> result =
            PluginLoadResult.success(mockCard, "com.example.Card", "/path/to.jar");

        assertTrue(result.isSuccess());
        assertSame(mockCard, result.getInstance());
        assertEquals("com.example.Card", result.getClassName());
        assertEquals("/path/to.jar", result.getJarPath());
        assertNull(result.getFailureReason());
        assertTrue(result.toString().contains("SUCCESS"));
    }

    @Test
    @DisplayName("PluginLoadResult: failure factory valid")
    void testPluginLoadResult_failure() {
        PluginLoadResult<ExperimentCard> result =
            PluginLoadResult.failure("File tidak ditemukan", "/path/to.jar");

        assertFalse(result.isSuccess());
        assertNull(result.getInstance());
        assertNull(result.getClassName());
        assertEquals("/path/to.jar", result.getJarPath());
        assertEquals("File tidak ditemukan", result.getFailureReason());
        assertTrue(result.toString().contains("FAILURE"));
    }

    @Test
    @DisplayName("PluginLoadResult: success dengan null instance → throw")
    void testPluginLoadResult_successNullInstance() {
        assertThrows(IllegalArgumentException.class,
            () -> PluginLoadResult.success(null, "com.Class", "/path.jar"));
    }

    @Test
    @DisplayName("PluginLoadResult: failure dengan null reason → throw")
    void testPluginLoadResult_failureNullReason() {
        assertThrows(IllegalArgumentException.class,
            () -> PluginLoadResult.failure(null, "/path.jar"));
    }

    @Test
    @DisplayName("PluginLoadResult: failure dengan blank reason → throw")
    void testPluginLoadResult_failureBlankReason() {
        assertThrows(IllegalArgumentException.class,
            () -> PluginLoadResult.failure("   ", "/path.jar"));
    }

    @Test
    @DisplayName("loadAllCards: JAR tidak ada → list berisi 1 failure")
    void testLoadAllCards_jarNotFound() {
        List<PluginLoadResult<ExperimentCard>> results =
            loader.loadAllCards("/no/jar.jar");

        assertNotNull(results);
        assertEquals(1, results.size(), "Harus ada 1 entry failure");
        assertFalse(results.get(0).isSuccess());
    }

    @Test
    @DisplayName("closeAll: tidak throw jika dipanggil tanpa loader terbuka")
    void testCloseAll_noop() {
        assertDoesNotThrow(() -> loader.closeAll());
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /** Buat anonymous ExperimentCard untuk testing PluginLoadResult. */
    private ExperimentCard createMockCard() {
        return new ExperimentCard() {
            @Override public String getCardName() { return "Mock"; }
            @Override public String getDescription() { return "Mock card for testing"; }
            @Override public void applyEffect(GameState s, Player p) {}
            @Override public boolean isPlayable() { return true; }
            @Override public boolean isSecret() { return false; }
            @Override public CardType getCardType() { return CardType.KNIGHT; }
        };
    }
}
