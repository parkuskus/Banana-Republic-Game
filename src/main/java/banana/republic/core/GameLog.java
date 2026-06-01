package banana.republic.core;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Log kejadian permainan Banana Republic, ditampilkan di panel Logbook pada UI.
 *
 * <p>GameLog menyimpan riwayat semua {@link LogEntry} secara kronologis.
 * Entri baru selalu ditambahkan di akhir (append-only).
 *
 * <p>Di-inject ke {@link GameState} agar plugin dan UI bisa membaca log tanpa
 * perlu akses langsung ke {@link Game}.
 */
public class GameLog {

    // cap to prevent memory leak
    private static final int MAX_ENTRIES = 500;

    private final Deque<LogEntry> entries;

    public GameLog() { this.entries = new ArrayDeque<>(); }

    public void addEntry(LogEntry.EventType eventType, String playerName,
                         String message) {
        append(new LogEntry(eventType, playerName, message));
    }

    public void addEntry(LogEntry.EventType eventType, String message) {
        append(new LogEntry(eventType, message));
    }

    public void addEntry(LogEntry entry) {
        if (entry == null) {
            throw new IllegalArgumentException("LogEntry tidak boleh null");
        }
        append(entry);
    }

    public List<LogEntry> getEntries() {
        return Collections.unmodifiableList(new ArrayList<>(entries));
    }

    public List<LogEntry> getEntriesByType(LogEntry.EventType type) {
        return entries.stream()
            .filter(e -> e.getEventType() == type)
            .collect(Collectors.toList());
    }

    public List<LogEntry> getEntriesByPlayer(String playerName) {
        return entries.stream()
            .filter(e -> playerName.equals(e.getPlayerName()))
            .collect(Collectors.toList());
    }

    public List<LogEntry> getRecentEntries(int n) {
        List<LogEntry> all = new ArrayList<>(entries);
        int fromIndex = Math.max(0, all.size() - n);
        return Collections.unmodifiableList(all.subList(fromIndex, all.size()));
    }

    public int size() { return entries.size(); }

    public boolean isEmpty() { return entries.isEmpty(); }

    private void append(LogEntry entry) {
        if (entries.size() >= MAX_ENTRIES) {
            entries.pollFirst();
        }
        entries.addLast(entry);
    }
}
