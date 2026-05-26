package banana.republic.core;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Bersifat immutable. Produced by GameLog, nanti ditampilkan di UI juga Setiap
 * entri memiliki:
 *
 * EventType — kategori kejadian (untuk filtering / pewarnaan di UI)
 *
 * Nama pemain yang terlibat (opsional, null untuk kejadian sistem)
 *
 * Pesan deskriptif dalam format teks bebas
 *
 * Timestamp saat entri dibuat
 */
public class LogEntry {

    // Kategori kejadian yang tercatat dalam log.
    public enum EventType {
        RESOURCE_PRODUCTION,
        BUILD,
        TRADE,
        CARD_PLAYED,
        CARD_BOUGHT,
        ROBBER,
        STEAL,
        DISCARD,
        TURN_CHANGE,
        // longest army/road
        SPECIAL_CARD,
        VICTORY,
        // init save/load
        SYSTEM
    }

    private static final DateTimeFormatter DISPLAY_FORMATTER =
        DateTimeFormatter.ofPattern("HH:mm:ss");

    private final EventType eventType;
    private final String playerName; // null jika bukan aksi spesifik pemain
    private final String message;
    private final LocalDateTime timestamp;

    public LogEntry(EventType eventType, String playerName, String message) {
        if (eventType == null) {
            throw new IllegalArgumentException("EventType tidak boleh null");
        }
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException(
                "Message tidak boleh null atau kosong");
        }
        this.eventType = eventType;
        this.playerName = playerName;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }

    // log tanpa player
    public LogEntry(EventType eventType, String message) {
        this(eventType, null, message);
    }

    public EventType getEventType() { return eventType; }

    public String getPlayerName() { return playerName; }

    public String getMessage() { return message; }

    public LocalDateTime getTimestamp() { return timestamp; }

    public String toDisplayString() {
        String time = timestamp.format(DISPLAY_FORMATTER);
        if (playerName != null && !playerName.isBlank()) {
            return String.format("[%s] [%s] %s", time, playerName, message);
        }
        return String.format("[%s] %s", time, message);
    }

    @Override
    public String toString() {
        return toDisplayString();
    }
}
