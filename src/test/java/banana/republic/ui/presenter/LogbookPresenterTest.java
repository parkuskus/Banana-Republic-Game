package banana.republic.ui.presenter;

import static org.junit.jupiter.api.Assertions.*;

import banana.republic.core.Game;
import banana.republic.core.LogEntry;
import banana.republic.player.HumanPlayer;
import banana.republic.player.Player;
import banana.republic.player.PlayerColor;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.control.Label;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

/**
 * Unit tests untuk LogbookPresenter.
 *
 * <p>Memverifikasi bahwa render() menampilkan entri log dengan format
 * toDisplayString() yang mengandung timestamp [HH:mm:ss] dan nama pemain.
 *
 * <p>Catatan: Konstruktor Game() otomatis menambahkan 1 entry SYSTEM
 * ("Permainan Banana Republic dimulai dengan X pemain.").
 */
@DisplayName("LogbookPresenter Tests")
public class LogbookPresenterTest extends ApplicationTest {

    private Game game;
    private List<Label> logLabels;
    private LogbookPresenter presenter;

    @BeforeEach
    void setUp() {
        List<Player> players = new ArrayList<>();
        players.add(new HumanPlayer("Alice", PlayerColor.RED));
        players.add(new HumanPlayer("Bob", PlayerColor.BLUE));
        players.add(new HumanPlayer("Charlie", PlayerColor.ORANGE));
        game = new Game(players, null);

        logLabels = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            logLabels.add(new Label());
        }

        presenter = new LogbookPresenter(logLabels);
    }

    /**
     * Helper: extract the HH:mm:ss portion from a rendered label.
     * Format: "• [HH:mm:ss] [PlayerName] message"
     */
    private String extractTimestamp(String labelText) {
        int open = labelText.indexOf('[');
        int close = labelText.indexOf(']');
        if (open < 0 || close <= open) return "";
        return labelText.substring(open + 1, close);
    }

    @Test
    @DisplayName("render should show timestamp, player name, and message in toDisplayString() format")
    void testRenderShowsTimestampAndPlayerName() {
        // Game has 1 system entry + 2 added = 3 total entries
        game.getGameLog().addEntry(LogEntry.EventType.BUILD, "Alice", "membangun jalan");
        game.getGameLog().addEntry(LogEntry.EventType.TRADE, "Bob", "berdagang dengan bank");

        presenter.render(game);

        // All three labels should have content (newest first)
        assertFalse(logLabels.get(0).getText().isEmpty(), "Label 0 should have text");
        assertFalse(logLabels.get(1).getText().isEmpty(), "Label 1 should have text");
        assertFalse(logLabels.get(2).getText().isEmpty(), "Label 2 should have text");

        // Verify labels start with bullet and contain timestamp brackets
        assertTrue(logLabels.get(0).getText().startsWith("• ["),
            "Label 0 should start with '• [' but was: " + logLabels.get(0).getText());
        assertTrue(logLabels.get(1).getText().startsWith("• ["),
            "Label 1 should start with '• [' but was: " + logLabels.get(1).getText());

        // Verify timestamp format [HH:mm:ss]
        String ts0 = extractTimestamp(logLabels.get(0).getText());
        assertTrue(ts0.matches("\\d{2}:\\d{2}:\\d{2}"),
            "Label 0 timestamp should be HH:mm:ss but got: " + ts0);

        // Verify player names (newest first: Bob at 0, Alice at 1)
        assertTrue(logLabels.get(0).getText().contains("[Bob]"),
            "Label 0 (newest) should contain [Bob] but was: " + logLabels.get(0).getText());
        assertTrue(logLabels.get(1).getText().contains("[Alice]"),
            "Label 1 should contain [Alice] but was: " + logLabels.get(1).getText());

        // Verify messages are preserved
        assertTrue(logLabels.get(0).getText().contains("berdagang dengan bank"),
            "Label 0 should contain Bob's message");
        assertTrue(logLabels.get(1).getText().contains("membangun jalan"),
            "Label 1 should contain Alice's message");

        // System entry (oldest) at index 2 — no player name
        assertTrue(logLabels.get(2).getText().contains("dimulai dengan 3 pemain"),
            "Label 2 should contain system message");
    }

    @Test
    @DisplayName("render should leave extra labels empty when there are fewer entries than labels")
    void testRenderWithMoreLabelsThanEntries() {
        // Game has 1 system entry; add 1 more = 2 total
        game.getGameLog().addEntry(LogEntry.EventType.SYSTEM, "Kartu dimainkan");

        presenter.render(game);

        // Labels 0-1 should have content, labels 2-4 should be empty
        assertFalse(logLabels.get(0).getText().isEmpty(), "Label 0 should have content");
        assertFalse(logLabels.get(1).getText().isEmpty(), "Label 1 should have content");
        assertTrue(logLabels.get(2).getText().isEmpty(), "Label 2 should be empty");
        assertTrue(logLabels.get(3).getText().isEmpty(), "Label 3 should be empty");
        assertTrue(logLabels.get(4).getText().isEmpty(), "Label 4 should be empty");
    }

    @Test
    @DisplayName("render should handle null game without throwing")
    void testRenderWithNullGame() {
        presenter.render(null);
        for (int i = 0; i < logLabels.size(); i++) {
            assertTrue(logLabels.get(i).getText().isEmpty(),
                "Label " + i + " should be empty when game is null");
        }
    }

    @Test
    @DisplayName("render should display system entries with toDisplayString() format")
    void testRenderWithOnlySystemEntries() {
        // Game constructor adds 1 system entry
        presenter.render(game);

        // Only label 0 has content (1 entry), rest are empty
        String text0 = logLabels.get(0).getText();
        assertFalse(text0.isEmpty(), "Label 0 should show the system entry");
        assertTrue(text0.startsWith("• ["), "Label 0 should start with timestamp bracket");

        // Extract and validate timestamp
        String ts0 = extractTimestamp(text0);
        assertTrue(ts0.matches("\\d{2}:\\d{2}:\\d{2}"),
            "System entry timestamp should be HH:mm:ss but got: " + ts0);

        // Verify message content
        assertTrue(text0.contains("Banana Republic"),
            "Label 0 should contain the game start message");

        // Labels 1-4 should be empty
        for (int i = 1; i < logLabels.size(); i++) {
            assertTrue(logLabels.get(i).getText().isEmpty(),
                "Label " + i + " should be empty when log has only 1 entry");
        }
    }

    @Test
    @DisplayName("render should display newest entries first")
    void testRenderNewestFirst() {
        // Game has 1 system entry; add 3 more = 4 total
        game.getGameLog().addEntry(LogEntry.EventType.BUILD, "Alice", "membangun jalan");
        game.getGameLog().addEntry(LogEntry.EventType.TRADE, "Bob", "berdagang");
        game.getGameLog().addEntry(LogEntry.EventType.ROBBER, "Charlie", "mencuri");

        presenter.render(game);

        // Newest entry first: Charlie (0), Bob (1), Alice (2), System (3)
        assertTrue(logLabels.get(0).getText().contains("[Charlie]"),
            "Newest first: expected [Charlie] but was: " + logLabels.get(0).getText());
        assertTrue(logLabels.get(1).getText().contains("[Bob]"),
            "Second should be [Bob] but was: " + logLabels.get(1).getText());
        assertTrue(logLabels.get(2).getText().contains("[Alice]"),
            "Third should be [Alice] but was: " + logLabels.get(2).getText());

        // System message (oldest) has no player name
        assertTrue(logLabels.get(3).getText().contains("dimulai dengan"),
            "Oldest should be system message");
    }
}
