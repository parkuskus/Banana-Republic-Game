package banana.republic.plugin;

/**
 * Hasil dari satu operasi load plugin via {@link PluginLoader}.
 *
 * <p>Tidak pernah null — selalu berisi informasi apakah loading berhasil
 * atau gagal beserta alasannya. Tidak ada exception untuk kondisi bisnis.
 *
 * @param <T> tipe interface plugin ({@code ExperimentCard}, {@code MapGeneratorPlugin},
 *            atau {@code PlayerStrategy})
 */
public final class PluginLoadResult<T> {

    private final boolean success;
    private final T       instance;
    private final String  className;
    private final String  jarPath;
    private final String  failureReason;

    private PluginLoadResult(boolean success, T instance, String className,
                              String jarPath, String failureReason) {
        this.success       = success;
        this.instance      = instance;
        this.className     = className;
        this.jarPath       = jarPath;
        this.failureReason = failureReason;
    }

    // -------------------------------------------------------------------------
    // Factory methods
    // -------------------------------------------------------------------------

    /**
     * Membuat hasil loading yang berhasil.
     *
     * @param instance  instance plugin yang berhasil diinstansiasi
     * @param className fully-qualified class name yang berhasil diload
     * @param jarPath   path absolut ke file .jar yang diload
     */
    public static <T> PluginLoadResult<T> success(T instance, String className, String jarPath) {
        if (instance == null) {
            throw new IllegalArgumentException("instance tidak boleh null pada hasil success");
        }
        return new PluginLoadResult<>(true, instance, className, jarPath, null);
    }

    /**
     * Membuat hasil loading yang gagal.
     *
     * @param reason  penjelasan mengapa loading gagal
     * @param jarPath path absolut ke file .jar yang dicoba diload
     */
    public static <T> PluginLoadResult<T> failure(String reason, String jarPath) {
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("reason tidak boleh null/kosong pada hasil failure");
        }
        return new PluginLoadResult<>(false, null, null, jarPath, reason);
    }

    // -------------------------------------------------------------------------
    // Accessors
    // -------------------------------------------------------------------------

    /** {@code true} jika plugin berhasil diload dan diinstansiasi. */
    public boolean isSuccess() { return success; }

    /**
     * Instance plugin yang berhasil diload.
     *
     * @return instance, atau {@code null} jika {@link #isSuccess()} false
     */
    public T getInstance() { return instance; }

    /**
     * Fully-qualified class name yang diload.
     *
     * @return class name, atau {@code null} jika loading gagal
     */
    public String getClassName() { return className; }

    /** Path ke file .jar yang digunakan. */
    public String getJarPath() { return jarPath; }

    /**
     * Alasan kegagalan loading.
     *
     * @return pesan error, atau {@code null} jika {@link #isSuccess()} true
     */
    public String getFailureReason() { return failureReason; }

    @Override
    public String toString() {
        if (success) {
            return "PluginLoadResult[SUCCESS: " + className + " from " + jarPath + "]";
        }
        return "PluginLoadResult[FAILURE: " + failureReason + " (jar=" + jarPath + ")]";
    }
}
