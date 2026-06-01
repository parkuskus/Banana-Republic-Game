package banana.republic.plugin;

import banana.republic.card.ExperimentCard;
import banana.republic.player.PlayerStrategy;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Plugin loader menggunakan URLClassLoader + Reflection.
 *
 * <p>Tanggung jawab:
 * <ul>
 *   <li>Memuat file .jar eksternal secara runtime</li>
 *   <li>Validasi class yang dimuat mengimplementasikan interface yang benar</li>
 *   <li>Mengelola instance plugin yang sudah di-load</li>
 * </ul>
 *
 * <p>Waktu load:
 * <ul>
 *   <li>{@code ExperimentCard}: mid-game (Settings Dialog)</li>
 *   <li>{@code MapGeneratorPlugin}: pre-game (Lobby)</li>
 *   <li>{@code PlayerStrategy}/Bot: pre-game (Lobby)</li>
 * </ul>
 *
 * <p>Referensi: class-diagram/Module5_UI_Plugin_Save.puml
 */
public class PluginLoader {

    private final List<ExperimentCard> loadedCardPlugins = new ArrayList<>();
    private MapGeneratorPlugin loadedMapPlugin = null;
    private final List<PlayerStrategy> loadedBotStrategies = new ArrayList<>();

    /**
     * Load ExperimentCard plugin dari file .jar.
     *
     * @param jarPath path absolut ke file .jar
     * @return instance ExperimentCard pertama yang berhasil di-load
     * @throws IllegalStateException jika tidak ada class valid di dalam .jar
     */
    public ExperimentCard loadExperimentCard(String jarPath) {
        Object plugin = loadClassFromJar(jarPath, ExperimentCard.class);
        if (!(plugin instanceof ExperimentCard card)) {
            throw new IllegalStateException(
                "Class yang dimuat dari '" + jarPath + "' tidak mengimplementasikan ExperimentCard"
            );
        }
        loadedCardPlugins.add(card);
        return card;
    }

    /**
     * Load MapGeneratorPlugin dari file .jar.
     *
     * @param jarPath path absolut ke file .jar
     * @return instance MapGeneratorPlugin yang berhasil di-load
     * @throws IllegalStateException jika tidak ada class valid di dalam .jar
     */
    public MapGeneratorPlugin loadMapGenerator(String jarPath) {
        Object plugin = loadClassFromJar(jarPath, MapGeneratorPlugin.class);
        if (!(plugin instanceof MapGeneratorPlugin mapPlugin)) {
            throw new IllegalStateException(
                "Class yang dimuat dari '" + jarPath + "' tidak mengimplementasikan MapGeneratorPlugin"
            );
        }
        loadedMapPlugin = mapPlugin;
        return mapPlugin;
    }

    /**
     * Load PlayerStrategy (bot plugin) dari file .jar.
     *
     * @param jarPath path absolut ke file .jar
     * @return instance PlayerStrategy pertama yang berhasil di-load
     * @throws IllegalStateException jika tidak ada class valid di dalam .jar
     */
    public PlayerStrategy loadBotStrategy(String jarPath) {
        Object plugin = loadClassFromJar(jarPath, PlayerStrategy.class);
        if (!(plugin instanceof PlayerStrategy strategy)) {
            throw new IllegalStateException(
                "Class yang dimuat dari '" + jarPath + "' tidak mengimplementasikan PlayerStrategy"
            );
        }
        loadedBotStrategies.add(strategy);
        return strategy;
    }

    public List<ExperimentCard> getLoadedCardPlugins() {
        return Collections.unmodifiableList(loadedCardPlugins);
    }

    public MapGeneratorPlugin getLoadedMapPlugin() {
        return loadedMapPlugin;
    }

    public List<PlayerStrategy> getLoadedBotStrategies() {
        return Collections.unmodifiableList(loadedBotStrategies);
    }

    /**
     * Load class dari .jar menggunakan URLClassLoader + Reflection.
     *
     * <p>Proses:
     * <ol>
     *   <li>Buat URLClassLoader dari path .jar</li>
     *   <li>Iterasi semua entry .jar yang berakhiran .class</li>
     *   <li>Load class dengan Class.forName menggunakan class loader baru</li>
     *   <li>Cek apakah class mengimplementasikan targetInterface</li>
     *   <li>Buat instance via getDeclaredConstructor().newInstance()</li>
     * </ol>
     *
     * @param jarPath         path ke file .jar
     * @param targetInterface interface yang wajib diimplementasikan
     * @return instance object yang valid
     * @throws IllegalStateException jika tidak ada class yang cocok
     */
    private Object loadClassFromJar(String jarPath, Class<?> targetInterface) {
        if (jarPath == null || jarPath.isBlank()) {
            throw new IllegalArgumentException("JAR path cannot be null or blank");
        }

        File jarFile = new File(jarPath);
        if (!jarFile.exists()) {
            throw new IllegalArgumentException("JAR file not found: " + jarPath);
        }

        URL jarUrl;
        try {
            jarUrl = jarFile.toURI().toURL();
        } catch (MalformedURLException e) {
            throw new IllegalStateException("Invalid JAR path: " + jarPath, e);
        }

        try (URLClassLoader classLoader = new URLClassLoader(
                new URL[]{jarUrl},
                PluginLoader.class.getClassLoader())) {

            List<String> classNames = scanJarClasses(jarFile);

            for (String className : classNames) {
                try {
                    Class<?> clazz = Class.forName(className, true, classLoader);
                    if (validateInterface(clazz, targetInterface)) {
                        Object instance = clazz.getDeclaredConstructor().newInstance();
                        return instance;
                    }
                } catch (ClassNotFoundException e) {
                    // Class mungkin bergantung pada dependency lain, skip
                    continue;
                } catch (NoSuchMethodException e) {
                    // Class tidak punya no-arg constructor, skip
                    continue;
                } catch (Exception e) {
                    // Instantiation gagal, skip ke class berikutnya
                    continue;
                }
            }

            throw new IllegalStateException(
                "Tidak ada class yang mengimplementasikan " + targetInterface.getName()
                    + " di '" + jarPath + "'"
            );
        } catch (java.io.IOException e) {
            throw new IllegalStateException("Failed to load JAR: " + jarPath, e);
        }
    }

    /**
     * Scan semua class names (FQCN) yang ada di dalam .jar file.
     */
    private List<String> scanJarClasses(File jarFile) throws java.io.IOException {
        List<String> classNames = new ArrayList<>();
        try (JarFile jar = new JarFile(jarFile)) {
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String name = entry.getName();
                if (name.endsWith(".class")) {
                    // Convert jar path to fully-qualified class name
                    String className = name.replace('/', '.').replace("\\", ".");
                    className = className.substring(0, className.length() - ".class".length());
                    classNames.add(className);
                }
            }
        }
        return classNames;
    }

    /**
     * Validasi apakah class mengimplementasikan interface yang diharapkan.
     *
     * @param clazz           class yang akan divalidasi
     * @param expectedInterface interface yang diharapkan
     * @return true jika clazz adalah instance dari expectedInterface
     */
    private boolean validateInterface(Class<?> clazz, Class<?> expectedInterface) {
        if (!expectedInterface.isInterface()) {
            throw new IllegalArgumentException(
                expectedInterface.getName() + " bukan interface"
            );
        }
        return expectedInterface.isAssignableFrom(clazz)
            && !clazz.isInterface()
            && !java.lang.reflect.Modifier.isAbstract(clazz.getModifiers());
    }
}
