package banana.republic.save;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import banana.republic.board.Intersection;
import banana.republic.board.Path;
import banana.republic.core.Game;
import banana.republic.player.HumanPlayer;
import banana.republic.player.Player;
import banana.republic.player.PlayerColor;
import banana.republic.resource.ResourceType;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class GameSaveManagerTest {

    @Test
    void saveLoadRoundTrip(@TempDir java.nio.file.Path tempDir) {
        List<Player> players = List.of(
            new HumanPlayer("Alice", PlayerColor.RED),
            new HumanPlayer("Bob", PlayerColor.BLUE),
            new HumanPlayer("Charlie", PlayerColor.ORANGE)
        );

        Game game = new Game(players, null);

        Player p0 = players.get(0);
        game.getBank().takeResource(ResourceType.WOOD, 1);
        p0.addResource(ResourceType.WOOD, 1);

        Intersection intersection = game.getBoard().getAllIntersections().get(0);
        game.placeInitialSettlement(p0, intersection);

        Path roadPath = intersection.getAdjacentPaths().get(0);
        game.placeInitialRoad(p0, roadPath);

        java.nio.file.Path savePath = tempDir.resolve("save.json");
        game.saveGame(savePath.toString());

        Game loaded = Game.loadGame(savePath.toString());

        assertEquals(game.getPlayers().size(), loaded.getPlayers().size());
        assertEquals(game.getCurrentPhase(), loaded.getCurrentPhase());
        assertEquals(game.getTurnNumber(), loaded.getTurnNumber());
        assertEquals(game.getTurnManager().getActiveIndex(),
                     loaded.getTurnManager().getActiveIndex());

        Player loadedP0 = loaded.getPlayers().get(0);
        assertEquals(p0.getResourceCount(ResourceType.WOOD),
                     loadedP0.getResourceCount(ResourceType.WOOD));

        long settlementCount = loaded.getBoard().getAllIntersections().stream()
            .filter(i -> i.hasBuilding() && loadedP0.equals(i.getOwner()))
            .count();
        assertTrue(settlementCount >= 1);

        long roadCount = loaded.getBoard().getAllPaths().stream()
            .filter(p -> p.hasRoad() && loadedP0.equals(p.getOwner()))
            .count();
        assertTrue(roadCount >= 1);
    }

    @Test
    void saveToDefaultSavesFolder() throws Exception {
        List<Player> players = List.of(
            new HumanPlayer("Alice", PlayerColor.RED),
            new HumanPlayer("Bob", PlayerColor.BLUE),
            new HumanPlayer("Charlie", PlayerColor.ORANGE)
        );

        Game game = new Game(players, null);

        game.startMainGame();
        game.rollDice();
        game.getTurnManager().restoreTimer(45, true, true, null);

        java.nio.file.Path savePath = Paths.get("saves", "test-save.json");
        Files.deleteIfExists(savePath);

        game.saveGame(savePath.toString());

        assertTrue(Files.exists(savePath));

        // Files.deleteIfExists(savePath);
    }

    @Test
    void loadRestoresPausedTimer(@TempDir java.nio.file.Path tempDir) {
        List<Player> players = List.of(
            new HumanPlayer("Alice", PlayerColor.RED),
            new HumanPlayer("Bob", PlayerColor.BLUE),
            new HumanPlayer("Charlie", PlayerColor.ORANGE)
        );

        Game game = new Game(players, null);
        game.startMainGame();
        game.rollDice();
        game.getTurnManager().restoreTimer(33, true, true, null);

        java.nio.file.Path savePath = tempDir.resolve("save.json");
        game.saveGame(savePath.toString());

        Game loaded = Game.loadGame(savePath.toString());

        assertEquals(game.getCurrentPhase(), loaded.getCurrentPhase());
        assertTrue(loaded.getTurnManager().isTimerRunning());
        assertTrue(loaded.getTurnManager().isTimerPaused());
        assertEquals(33, loaded.getTurnManager().getRemainingTimerSeconds());

        loaded.getTurnManager().stopTimer();
    }
}
