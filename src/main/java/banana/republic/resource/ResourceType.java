package banana.republic.resource;


public enum ResourceType {
    WOOD("Kayu", "🪵"),
    BRICK("Batu Bata", "🧱"),
    WHEAT("Gandum", "🌾"),
    ORE("Bijih", "⛏"),
    BANANA("Pisang", "🍌");

    private final String displayName;
    private final String emoji;

    ResourceType(String displayName, String emoji) {
        this.displayName = displayName;
        this.emoji = emoji;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getEmoji() {
        return emoji;
    }
}
