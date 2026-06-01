package banana.republic.save;

/**
 * Card save data DTO.
 *
 * <p>Field {@code pluginClassName} bersifat opsional dan hanya diisi
 * untuk kartu yang berasal dari plugin JAR eksternal ({@code isPluginCard() == true}).
 * Jika diisi, {@code cardType} <b>tidak perlu</b> diisi karena kartu
 * direkonstuksi via Reflection, bukan via {@code CardType} enum.
 */
public class CardSaveData {
    public String  cardType;       // KNIGHT | ROAD_BUILDING | MONOPOLY | VICTORY_POINT
    public boolean revealed;
    public boolean newlyDrawn;
    public boolean consumed;
    public String  monopolyTarget;

    /**
     * Fully-qualified class name kartu plugin.
     * Contoh: {@code "com.ta.HealCard"}.
     * {@code null} untuk kartu built-in.
     */
    public String  pluginClassName;
}
