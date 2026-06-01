package banana.republic.plugin;

import banana.republic.card.ExperimentCard;
import banana.republic.player.PlayerStrategy;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Memuat plugin dari file {@code .jar} secara runtime menggunakan
 * {@code URLClassLoader} dan Java Reflection.
 *
 * <p>Mendukung tiga jenis plugin sesuai Spesifikasi Bab 8:
 * <ol>
 *   <li>{@link ExperimentCard}     — Kartu Temuan kustom (bisa multi per JAR)</li>
 *   <li>{@link MapGeneratorPlugin} — Generator peta kustom</li>
 *   <li>{@link PlayerStrategy}     — Strategi bot kustom</li>
 * </ol>
 *
 * <p>Semua method mengembalikan {@link PluginLoadResult} — tidak pernah melempar
 * exception untuk kondisi bisnis (JAR tidak ada, class tidak implement interface, dll).
 *
 * <p>Gunakan {@link #closeAll()} setelah selesai menggunakan loader untuk melepas
 * file handle dan mencegah memory leak.
 */
public class PluginLoader {

    /** Semua URLClassLoader yang dibuat oleh instance ini, untuk close saat selesai. */
    private final List<URLClassLoader> openLoaders = new ArrayList<>();

    // =========================================================================
    // ExperimentCard
    // =========================================================================

    /**
     * Memuat satu {@link ExperimentCard} dari JAR berdasarkan nama class-nya.
     *
     * <p>Gunakan {@link #discoverCards(String)} terlebih dahulu untuk auto-discovery
     * jika nama class belum diketahui.
     *
     * @param jarPath   path absolut ke file {@code .jar}
     * @param className fully-qualified class name (misal {@code "com.example.HealCard"})
     * @return {@link PluginLoadResult} berisi instance atau alasan kegagalan
     */
    public PluginLoadResult<ExperimentCard> loadCard(String jarPath, String className) {
        return loadPlugin(jarPath, className, ExperimentCard.class);
    }

    /**
     * Memuat semua implementasi {@link ExperimentCard} yang ditemukan dalam JAR.
     *
     * <p>Mendukung JAR yang berisi lebih dari satu kartu kustom sekaligus.
     * Inner class (nama mengandung {@code $}) dilewati otomatis.
     *
     * @param jarPath path absolut ke file {@code .jar}
     * @return list hasil loading; tiap entry bisa success atau failure
     */
    public List<PluginLoadResult<ExperimentCard>> loadAllCards(String jarPath) {
        return loadAllPlugins(jarPath, ExperimentCard.class);
    }

    /**
     * Menemukan semua nama class dalam JAR yang mengimplementasikan {@link ExperimentCard}.
     *
     * <p>Digunakan untuk auto-discovery: UI bisa menampilkan daftar pilihan
     * tanpa meminta pengguna mengetik nama class secara manual.
     *
     * @param jarPath path absolut ke file {@code .jar}
     * @return list fully-qualified class name; kosong jika tidak ada atau JAR tidak valid
     */
    public List<String> discoverCards(String jarPath) {
        return discoverImplementors(jarPath, ExperimentCard.class);
    }

    // =========================================================================
    // MapGeneratorPlugin
    // =========================================================================

    /**
     * Memuat satu {@link MapGeneratorPlugin} dari JAR berdasarkan nama class-nya.
     *
     * @param jarPath   path absolut ke file {@code .jar}
     * @param className fully-qualified class name
     * @return {@link PluginLoadResult} berisi instance atau alasan kegagalan
     */
    public PluginLoadResult<MapGeneratorPlugin> loadMapGenerator(String jarPath, String className) {
        return loadPlugin(jarPath, className, MapGeneratorPlugin.class);
    }

    /**
     * Menemukan semua implementasi {@link MapGeneratorPlugin} dalam JAR (auto-discovery).
     *
     * @param jarPath path absolut ke file {@code .jar}
     * @return list fully-qualified class name
     */
    public List<String> discoverMapGenerators(String jarPath) {
        return discoverImplementors(jarPath, MapGeneratorPlugin.class);
    }

    // =========================================================================
    // PlayerStrategy
    // =========================================================================

    /**
     * Memuat satu {@link PlayerStrategy} dari JAR berdasarkan nama class-nya.
     *
     * @param jarPath   path absolut ke file {@code .jar}
     * @param className fully-qualified class name
     * @return {@link PluginLoadResult} berisi instance atau alasan kegagalan
     */
    public PluginLoadResult<PlayerStrategy> loadStrategy(String jarPath, String className) {
        return loadPlugin(jarPath, className, PlayerStrategy.class);
    }

    /**
     * Menemukan semua implementasi {@link PlayerStrategy} dalam JAR (auto-discovery).
     *
     * @param jarPath path absolut ke file {@code .jar}
     * @return list fully-qualified class name
     */
    public List<String> discoverStrategies(String jarPath) {
        return discoverImplementors(jarPath, PlayerStrategy.class);
    }

    // =========================================================================
    // Auto-discovery generik
    // =========================================================================

    /**
     * Men-scan semua entry dalam JAR dan mengembalikan daftar nama class yang:
     * <ul>
     *   <li>Mengimplementasikan (atau meng-extend) {@code targetInterface}</li>
     *   <li>Bukan interface itu sendiri</li>
     *   <li>Bukan abstract class</li>
     *   <li>Bukan inner class (nama tidak mengandung {@code $})</li>
     * </ul>
     *
     * <p>Digunakan oleh UI untuk menampilkan dropdown auto-discovery.
     * Jika JAR tidak bisa dibaca, mengembalikan list kosong (tidak throw).
     *
     * @param jarPath         path absolut ke file {@code .jar}
     * @param targetInterface interface yang dicari implementornya
     * @return list fully-qualified class name; kosong jika tidak ada atau error
     */
    public List<String> discoverImplementors(String jarPath, Class<?> targetInterface) {
        List<String> found = new ArrayList<>();

        File jarFile = new File(jarPath);
        if (!jarFile.exists() || !jarFile.isFile()) {
            return found; // JAR tidak ada — kembalikan kosong
        }

        URLClassLoader loader = null;
        try {
            URL jarUrl = jarFile.toURI().toURL();
            loader = new URLClassLoader(new URL[]{jarUrl}, getClass().getClassLoader());
            openLoaders.add(loader);

            try (JarFile jar = new JarFile(jarFile)) {
                Enumeration<JarEntry> entries = jar.entries();
                while (entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement();
                    String name = entry.getName();

                    // Hanya proses .class yang bukan inner class
                    if (!name.endsWith(".class") || name.contains("$")) continue;

                    String className = name.replace('/', '.').replace(".class", "");
                    try {
                        Class<?> clazz = loader.loadClass(className);
                        if (targetInterface.isAssignableFrom(clazz)
                                && !clazz.isInterface()
                                && !Modifier.isAbstract(clazz.getModifiers())) {
                            found.add(className);
                        }
                    } catch (ClassNotFoundException | NoClassDefFoundError ignored) {
                        // Lewati class yang tidak bisa diload (dependency tidak tersedia)
                    }
                }
            }
        } catch (MalformedURLException e) {
            // Path JAR tidak valid — kembalikan list kosong
        } catch (IOException e) {
            // JAR tidak bisa dibuka — kembalikan list kosong
        }

        return found;
    }

    // =========================================================================
    // Resource management
    // =========================================================================

    /**
     * Menutup semua {@code URLClassLoader} yang dibuat oleh instance ini.
     *
     * <p>Penting untuk dipanggil saat aplikasi keluar atau setelah game selesai
     * agar file handle ke JAR dilepas dan tidak terjadi memory leak.
     */
    public void closeAll() {
        for (URLClassLoader loader : openLoaders) {
            try {
                loader.close();
            } catch (IOException ignored) {
                // Best-effort close
            }
        }
        openLoaders.clear();
    }

    // =========================================================================
    // Internal helpers
    // =========================================================================

    /**
     * Core loading logic: load class dari JAR, validasi implements targetInterface,
     * instantiasi via no-arg constructor.
     */
    @SuppressWarnings("unchecked")
    private <T> PluginLoadResult<T> loadPlugin(String jarPath, String className,
                                                Class<T> targetInterface) {
        if (jarPath == null || jarPath.isBlank()) {
            return PluginLoadResult.failure("jarPath tidak boleh kosong", jarPath == null ? "" : jarPath);
        }
        if (className == null || className.isBlank()) {
            return PluginLoadResult.failure("className tidak boleh kosong", jarPath);
        }

        File jarFile = new File(jarPath);
        if (!jarFile.exists() || !jarFile.isFile()) {
            return PluginLoadResult.failure("File JAR tidak ditemukan: " + jarPath, jarPath);
        }

        URLClassLoader loader = null;
        try {
            URL jarUrl = jarFile.toURI().toURL();
            loader = new URLClassLoader(new URL[]{jarUrl}, getClass().getClassLoader());
            openLoaders.add(loader);

            Class<?> clazz;
            try {
                clazz = loader.loadClass(className);
            } catch (ClassNotFoundException e) {
                return PluginLoadResult.failure(
                    "Class tidak ditemukan dalam JAR: " + className, jarPath);
            }

            // Validasi: class harus implement interface yang diharapkan
            if (!targetInterface.isAssignableFrom(clazz)) {
                return PluginLoadResult.failure(
                    "Class " + className + " tidak mengimplementasikan "
                    + targetInterface.getSimpleName(), jarPath);
            }

            // Validasi: tidak boleh abstract atau interface
            if (clazz.isInterface() || Modifier.isAbstract(clazz.getModifiers())) {
                return PluginLoadResult.failure(
                    "Class " + className + " adalah abstract/interface, tidak bisa diinstansiasi",
                    jarPath);
            }

            // Cek no-arg constructor
            try {
                clazz.getDeclaredConstructor(); // throws NoSuchMethodException jika tidak ada
            } catch (NoSuchMethodException e) {
                return PluginLoadResult.failure(
                    "Class " + className + " tidak memiliki no-arg constructor", jarPath);
            }

            // Instantiasi
            T instance;
            try {
                instance = (T) clazz.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                return PluginLoadResult.failure(
                    "Gagal menginstansiasi " + className + ": " + e.getMessage(), jarPath);
            }

            return PluginLoadResult.success(instance, className, jarPath);

        } catch (MalformedURLException e) {
            return PluginLoadResult.failure("Path JAR tidak valid: " + e.getMessage(), jarPath);
        }
    }

    /**
     * Load semua implementor {@code targetInterface} dalam satu JAR.
     * Menggunakan {@link #discoverImplementors} untuk scanning, lalu load satu per satu.
     */
    private <T> List<PluginLoadResult<T>> loadAllPlugins(String jarPath, Class<T> targetInterface) {
        List<PluginLoadResult<T>> results = new ArrayList<>();
        List<String> classNames = discoverImplementors(jarPath, targetInterface);

        if (classNames.isEmpty()) {
            results.add(PluginLoadResult.failure(
                "Tidak ada implementasi " + targetInterface.getSimpleName()
                + " yang ditemukan dalam JAR", jarPath));
            return results;
        }

        for (String className : classNames) {
            results.add(loadPlugin(jarPath, className, targetInterface));
        }
        return results;
    }
}
