package banana.republic.save;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import banana.republic.board.Intersection;
import banana.republic.board.Path;
import banana.republic.core.Game;
import banana.republic.player.HumanPlayer;
import banana.republic.player.Player;
import banana.republic.player.PlayerColor;
import banana.republic.resource.ResourceType;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.FieldNamingPolicy;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class GameSaveManagerTest {

    // -------------------------------------------------------------------------
    // Round-trip tests (happy path)
    // -------------------------------------------------------------------------

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

    // -------------------------------------------------------------------------
    // Validation error tests — manually corrupted JSON
    // -------------------------------------------------------------------------

    private static Gson buildGson() {
        return new GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .setPrettyPrinting()
            .create();
    }

    /** Saves a valid game, reads the raw JSON, applies a mutation, writes it back, then loads. */
    private Game saveModifyLoad(java.nio.file.Path dir, java.util.function.Consumer<GameSaveData> mutation)
            throws IOException {
        List<Player> players = List.of(
            new HumanPlayer("Alice", PlayerColor.RED),
            new HumanPlayer("Bob", PlayerColor.BLUE),
            new HumanPlayer("Charlie", PlayerColor.ORANGE)
        );
        Game game = new Game(players, null);
        java.nio.file.Path savePath = dir.resolve("save.json");
        game.saveGame(savePath.toString());

        Gson gson = buildGson();
        GameSaveData data;
        try (var reader = Files.newBufferedReader(savePath)) {
            data = gson.fromJson(reader, GameSaveData.class);
        }
        mutation.accept(data);
        try (var writer = Files.newBufferedWriter(savePath)) {
            gson.toJson(data, writer);
        }

        return Game.loadGame(savePath.toString());
    }

    @Test
    void rejectsUnknownGamePhase(@TempDir java.nio.file.Path tempDir) {
        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
            saveModifyLoad(tempDir, d -> d.currentPhase = "INVALID_PHASE")
        );
        assertTrue(ex.getMessage().contains("INVALID_PHASE"), ex.getMessage());
        assertTrue(ex.getMessage().contains("Valid values"), ex.getMessage());
    }

    @Test
    void rejectsMissingGamePhase(@TempDir java.nio.file.Path tempDir) {
        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
            saveModifyLoad(tempDir, d -> d.currentPhase = null)
        );
        assertTrue(ex.getMessage().contains("current_phase"), ex.getMessage());
    }

    @Test
    void rejectsNegativeTurnNumber(@TempDir java.nio.file.Path tempDir) {
        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
            saveModifyLoad(tempDir, d -> d.turnNumber = -5)
        );
        assertTrue(ex.getMessage().contains("turn_number"), ex.getMessage());
    }

    @Test
    void rejectsOutOfRangeActivePlayerIndex(@TempDir java.nio.file.Path tempDir) {
        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
            saveModifyLoad(tempDir, d -> d.activePlayerIndex = 99)
        );
        assertTrue(ex.getMessage().contains("active_player_index"), ex.getMessage());
    }

    @Test
    void rejectsUnknownPlayerColor(@TempDir java.nio.file.Path tempDir) {
        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
            saveModifyLoad(tempDir, d -> d.players.get(0).color = "PURPLE")
        );
        assertTrue(ex.getMessage().contains("PURPLE"), ex.getMessage());
        assertTrue(ex.getMessage().contains("Valid values"), ex.getMessage());
    }

    @Test
    void rejectsDuplicatePlayerColor(@TempDir java.nio.file.Path tempDir) {
        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
            saveModifyLoad(tempDir, d -> d.players.get(1).color = d.players.get(0).color)
        );
        assertTrue(ex.getMessage().contains("unique color"), ex.getMessage());
    }

    @Test
    void rejectsBlankPlayerName(@TempDir java.nio.file.Path tempDir) {
        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
            saveModifyLoad(tempDir, d -> d.players.get(0).name = "  ")
        );
        assertTrue(ex.getMessage().contains("name"), ex.getMessage());
    }

    @Test
    void rejectsNegativePlayerResource(@TempDir java.nio.file.Path tempDir) {
        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
            saveModifyLoad(tempDir, d -> d.players.get(0).resources.put("WOOD", -3))
        );
        assertTrue(ex.getMessage().contains("WOOD"), ex.getMessage());
    }

    @Test
    void rejectsUnknownResourceTypeInPlayer(@TempDir java.nio.file.Path tempDir) {
        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
            saveModifyLoad(tempDir, d -> d.players.get(0).resources.put("GOLD", 5))
        );
        assertTrue(ex.getMessage().contains("GOLD"), ex.getMessage());
    }

    @Test
    void rejectsUnknownCardType(@TempDir java.nio.file.Path tempDir) {
        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
            saveModifyLoad(tempDir, d -> {
                CardSaveData bad = new CardSaveData();
                bad.cardType = "SUPER_CARD";
                d.players.get(0).handCards.add(bad);
            })
        );
        assertTrue(ex.getMessage().contains("SUPER_CARD"), ex.getMessage());
    }

    @Test
    void rejectsMonopolyTargetOnNonMonopolyCard(@TempDir java.nio.file.Path tempDir) {
        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
            saveModifyLoad(tempDir, d -> {
                CardSaveData bad = new CardSaveData();
                bad.cardType = "KNIGHT";
                bad.monopolyTarget = "WOOD";
                d.players.get(0).handCards.add(bad);
            })
        );
        assertTrue(ex.getMessage().contains("monopoly_target"), ex.getMessage());
    }

    @Test
    void rejectsOutOfRangeBankResource(@TempDir java.nio.file.Path tempDir) {
        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
            saveModifyLoad(tempDir, d -> d.bank.put("WOOD", 25))
        );
        assertTrue(ex.getMessage().contains("WOOD"), ex.getMessage());
        assertTrue(ex.getMessage().contains("[0, 19]"), ex.getMessage());
    }

    @Test
    void rejectsOutOfRangeDie(@TempDir java.nio.file.Path tempDir) {
        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
            saveModifyLoad(tempDir, d -> {
                if (d.lastDice == null) {
                    d.lastDice = new DiceSaveData();
                    d.lastDice.die1 = 7;
                    d.lastDice.die2 = 3;
                } else {
                    d.lastDice.die1 = 7;
                }
            })
        );
        assertTrue(ex.getMessage().contains("die1"), ex.getMessage());
    }

    @Test
    void rejectsPausedTimerWithoutRunning(@TempDir java.nio.file.Path tempDir) throws IOException {
        // First we need a game in TRADE_BUILD phase so the timer is saved
        List<Player> players = List.of(
            new HumanPlayer("Alice", PlayerColor.RED),
            new HumanPlayer("Bob", PlayerColor.BLUE),
            new HumanPlayer("Charlie", PlayerColor.ORANGE)
        );
        Game game = new Game(players, null);
        game.startMainGame();
        game.rollDice();
        game.getTurnManager().restoreTimer(10, true, true, null);

        java.nio.file.Path savePath = tempDir.resolve("save.json");
        game.saveGame(savePath.toString());
        game.getTurnManager().stopTimer();

        Gson gson = buildGson();
        GameSaveData data;
        try (var reader = Files.newBufferedReader(savePath)) {
            data = gson.fromJson(reader, GameSaveData.class);
        }
        data.timer.paused = true;
        data.timer.running = false;
        try (var writer = Files.newBufferedWriter(savePath)) {
            gson.toJson(data, writer);
        }

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
            Game.loadGame(savePath.toString())
        );
        assertTrue(ex.getMessage().contains("paused"), ex.getMessage());
        assertTrue(ex.getMessage().contains("running"), ex.getMessage());
    }

    @Test
    void rejectsNegativeTimerSeconds(@TempDir java.nio.file.Path tempDir) throws IOException {
        List<Player> players = List.of(
            new HumanPlayer("Alice", PlayerColor.RED),
            new HumanPlayer("Bob", PlayerColor.BLUE),
            new HumanPlayer("Charlie", PlayerColor.ORANGE)
        );
        Game game = new Game(players, null);
        game.startMainGame();
        game.rollDice();
        game.getTurnManager().restoreTimer(10, true, false, null);

        java.nio.file.Path savePath = tempDir.resolve("save.json");
        game.saveGame(savePath.toString());
        game.getTurnManager().stopTimer();

        Gson gson = buildGson();
        GameSaveData data;
        try (var reader = Files.newBufferedReader(savePath)) {
            data = gson.fromJson(reader, GameSaveData.class);
        }
        data.timer.remainingSeconds = -1;
        try (var writer = Files.newBufferedWriter(savePath)) {
            gson.toJson(data, writer);
        }

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
            Game.loadGame(savePath.toString())
        );
        assertTrue(ex.getMessage().contains("remaining_seconds"), ex.getMessage());
    }

    @Test
    void rejectsUnknownTerrainType(@TempDir java.nio.file.Path tempDir) {
        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
            saveModifyLoad(tempDir, d -> d.board.hexTiles.get(0).terrain = "SWAMP")
        );
        assertTrue(ex.getMessage().contains("SWAMP"), ex.getMessage());
    }

    @Test
    void rejectsTokenOnDesertTile(@TempDir java.nio.file.Path tempDir) {
        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
            saveModifyLoad(tempDir, d -> {
                d.board.hexTiles.stream()
                    .filter(t -> "DESERT".equals(t.terrain))
                    .findFirst()
                    .ifPresent(t -> t.token = 6);
            })
        );
        assertTrue(ex.getMessage().contains("DESERT"), ex.getMessage());
        assertTrue(ex.getMessage().contains("token"), ex.getMessage());
    }

    @Test
    void rejectsOutOfRangeNumberToken(@TempDir java.nio.file.Path tempDir) {
        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
            saveModifyLoad(tempDir, d ->
                d.board.hexTiles.stream()
                    .filter(t -> !"DESERT".equals(t.terrain) && t.token != null)
                    .findFirst()
                    .ifPresent(t -> t.token = 1)  // 1 is invalid (min is 2)
            )
        );
        assertTrue(ex.getMessage().contains("token"), ex.getMessage());
    }

    @Test
    void rejectsDuplicateHexTileId(@TempDir java.nio.file.Path tempDir) {
        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
            saveModifyLoad(tempDir, d -> {
                if (d.board.hexTiles.size() >= 2) {
                    d.board.hexTiles.get(1).id = d.board.hexTiles.get(0).id;
                }
            })
        );
        assertTrue(ex.getMessage().contains("duplicate id"), ex.getMessage());
    }

    @Test
    void rejectsDanglingHexTileReferenceInIntersection(@TempDir java.nio.file.Path tempDir) {
        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
            saveModifyLoad(tempDir, d -> {
                if (d.board.intersections.get(0).adjacentHexTileIds != null
                        && !d.board.intersections.get(0).adjacentHexTileIds.isEmpty()) {
                    d.board.intersections.get(0).adjacentHexTileIds.set(0, 99999);
                }
            })
        );
        assertTrue(ex.getMessage().contains("hex tile id 99999"), ex.getMessage());
    }

    @Test
    void rejectsDanglingIntersectionReferenceInPath(@TempDir java.nio.file.Path tempDir) {
        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
            saveModifyLoad(tempDir, d -> d.board.paths.get(0).intersectionAId = 99999)
        );
        assertTrue(ex.getMessage().contains("intersection_a_id"), ex.getMessage());
        assertTrue(ex.getMessage().contains("99999"), ex.getMessage());
    }

    @Test
    void rejectsSelfLoopPath(@TempDir java.nio.file.Path tempDir) {
        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
            saveModifyLoad(tempDir, d -> {
                PathSaveData path = d.board.paths.get(0);
                path.intersectionBId = path.intersectionAId;
            })
        );
        assertTrue(ex.getMessage().contains("different intersections"), ex.getMessage());
    }

    @Test
    void rejectsMalformedJson(@TempDir java.nio.file.Path tempDir) throws IOException {
        java.nio.file.Path savePath = tempDir.resolve("bad.json");
        Files.writeString(savePath, "{ this is not valid json !!!");

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
            Game.loadGame(savePath.toString())
        );
        assertTrue(ex.getMessage().contains("malformed JSON"), ex.getMessage());
    }

    @Test
    void rejectsEmptyFile(@TempDir java.nio.file.Path tempDir) throws IOException {
        java.nio.file.Path savePath = tempDir.resolve("empty.json");
        Files.writeString(savePath, "");

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
            Game.loadGame(savePath.toString())
        );
        assertTrue(ex.getMessage().contains("empty"), ex.getMessage());
    }

    @Test
    void rejectsNonExistentFile() {
        assertThrows(IllegalArgumentException.class, () ->
            Game.loadGame("/no/such/path/game.json")
        );
    }
}
