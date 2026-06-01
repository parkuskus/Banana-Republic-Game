package banana.republic.save;

import banana.republic.board.Board;
import banana.republic.board.Harbor;
import banana.republic.board.HarborType;
import banana.republic.board.HexTile;
import banana.republic.board.Intersection;
import banana.republic.board.NumberToken;
import banana.republic.board.Path;
import banana.republic.board.TerrainType;
import banana.republic.building.Building;
import banana.republic.building.BuildingType;
import banana.republic.building.Road;
import banana.republic.card.CardDeck;
import banana.republic.card.CardType;
import banana.republic.card.DevelopmentCard;
import banana.republic.card.ExperimentCard;
import banana.republic.card.KnightCard;
import banana.republic.card.MonopolyCard;
import banana.republic.card.ProgressCard;
import banana.republic.card.RoadBuildingCard;
import banana.republic.card.VictoryPointCard;
import banana.republic.core.Game;
import banana.republic.core.GamePhase;
import banana.republic.dice.DiceResult;
import banana.republic.player.BotPlayer;
import banana.republic.player.HumanPlayer;
import banana.republic.player.Player;
import banana.republic.player.PlayerColor;
import banana.republic.player.PlayerStrategy;
import banana.republic.player.SpecialCardType;
import banana.republic.resource.Bank;
import banana.republic.resource.BankImpl;
import banana.republic.resource.ResourceType;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Game save/load manager (JSON format).
 * Refer to class-diagram/Module5_UI_Plugin_Save.puml for full specification.
 *
 * <p>All fields in a loaded save file are validated via {@link #validateSaveData(GameSaveData)}
 * before any object construction begins, so manually edited or corrupted files produce
 * descriptive errors instead of silent data corruption or obscure exceptions.
 */
public class GameSaveManager {

    private static final String DEFAULT_VERSION = "1.0";
    private static final String DEFAULT_DIR = "./saves";
    private static final DateTimeFormatter FILE_TS_FORMAT =
        DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

    /** Maximum resource tokens in the bank per resource type (standard Catan). */
    private static final int MAX_BANK_RESOURCE = 19;
    /** Valid face values for a standard six-sided die. */
    private static final int MIN_DIE = 1;
    private static final int MAX_DIE = 6;
    /** Valid number token range printed on hex tiles. */
    private static final int MIN_TOKEN = 2;
    private static final int MAX_TOKEN = 12;
    /** Minimum and maximum number of players allowed by game rules. */
    private static final int MIN_PLAYERS = 3;
    private static final int MAX_PLAYERS = 4;

    public static void saveGame(Game game, String filePath) {
        if (game == null) {
            throw new IllegalArgumentException("Game cannot be null");
        }

        GameSaveData data = toSaveData(game);
        Gson gson = buildGson();

        java.nio.file.Path targetPath = resolveSavePath(filePath, data.timestamp);
        try {
            Files.createDirectories(targetPath.getParent());
            try (Writer writer = Files.newBufferedWriter(targetPath)) {
                gson.toJson(data, writer);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to save game: " + e.getMessage(), e);
        }
    }

    public static Game loadGame(String filePath) {
        if (filePath == null || filePath.isBlank()) {
            throw new IllegalArgumentException("File path cannot be null or blank");
        }

        java.nio.file.Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            throw new IllegalArgumentException("Save file does not exist: " + filePath);
        }

        Gson gson = buildGson();
        GameSaveData data;
        try (Reader reader = Files.newBufferedReader(path)) {
            data = gson.fromJson(reader, GameSaveData.class);
        } catch (IOException e) {
            throw new IllegalStateException(
                "Failed to read save file '" + filePath + "': " + e.getMessage(), e);
        } catch (com.google.gson.JsonSyntaxException e) {
            throw new IllegalStateException(
                "Save file '" + filePath + "' contains malformed JSON — it may have been "
                + "corrupted or manually edited incorrectly. Details: " + e.getMessage(), e);
        }

        if (data == null) {
            throw new IllegalStateException(
                "Save file '" + filePath + "' is empty or does not contain valid JSON");
        }

        validateSaveData(data);
        return fromSaveData(data);
    }

    // =========================================================================
    // Validation — runs entirely before any game-object construction
    // =========================================================================

    /**
     * Validates every field in a parsed {@link GameSaveData}.
     * Throws {@link IllegalStateException} with a descriptive, actionable message for any
     * field that is missing, out of range, or contains an unknown enum value.
     */
    private static void validateSaveData(GameSaveData data) {
        validateRootFields(data);
        validatePlayers(data.players);
        // Upper-bound check requires knowing the player count, so it happens after validatePlayers.
        if (data.activePlayerIndex >= data.players.size()) {
            throw new IllegalStateException(
                "Save file has 'active_player_index' " + data.activePlayerIndex
                + " which is out of bounds for " + data.players.size() + " player(s)"
                + " (valid range: 0\u2013" + (data.players.size() - 1) + ")");
        }
        if (data.bank != null) {
            validateBankSaveData(data.bank);
        }
        if (data.deck != null) {
            validateDeckSaveData(data.deck);
        }
        if (data.lastDice != null) {
            validateDiceSaveData(data.lastDice);
        }
        if (data.timer != null) {
            validateTimerSaveData(data.timer);
        }
        validateBoardSaveData(data.board, data.players.size());
    }

    // --- Players & cards ---

    private static void validatePlayers(List<PlayerSaveData> players) {
        if (players == null || players.isEmpty()) {
            throw new IllegalStateException("Save file has no 'players' list");
        }
        if (players.size() < MIN_PLAYERS || players.size() > MAX_PLAYERS) {
            throw new IllegalStateException(
                "Save file has " + players.size() + " player(s) but the game requires "
                + MIN_PLAYERS + "\u2013" + MAX_PLAYERS + " players");
        }
        Set<String> usedColors = new HashSet<>();
        for (int i = 0; i < players.size(); i++) {
            PlayerSaveData p = players.get(i);
            validatePlayerSaveData(p, i);
            if (!usedColors.add(p.color)) {
                throw new IllegalStateException(
                    "Player at index " + i + " shares color '" + p.color
                    + "' with another player \u2014 each player must have a unique color");
            }
        }
    }

    private static void validatePlayerSaveData(PlayerSaveData p, int index) {
        String ctx = "Player at index " + index;
        if (p.name == null || p.name.isBlank()) {
            throw new IllegalStateException(ctx + " has a blank or missing 'name'");
        }
        ctx = ctx + " ('" + p.name + "')";
        if (p.color == null || p.color.isBlank()) {
            throw new IllegalStateException(ctx + " has a blank or missing 'color'");
        }
        try {
            PlayerColor.valueOf(p.color);
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException(
                ctx + " has unknown color '" + p.color
                + "'. Valid values: " + validNames(PlayerColor.values()), e);
        }
        if (p.knightsPlayed < 0) {
            throw new IllegalStateException(
                ctx + " has invalid 'knights_played' " + p.knightsPlayed + " \u2014 must be >= 0");
        }
        if (p.longestRoadLength < 0) {
            throw new IllegalStateException(
                ctx + " has invalid 'longest_road_length' " + p.longestRoadLength
                + " \u2014 must be >= 0");
        }
        if (p.resources != null) {
            for (Map.Entry<String, Integer> entry : p.resources.entrySet()) {
                try {
                    ResourceType.valueOf(entry.getKey());
                } catch (IllegalArgumentException e) {
                    throw new IllegalStateException(
                        ctx + " has unknown resource key '" + entry.getKey()
                        + "'. Valid values: " + validNames(ResourceType.values()), e);
                }
                if (entry.getValue() == null || entry.getValue() < 0) {
                    throw new IllegalStateException(
                        ctx + " has invalid resource amount " + entry.getValue()
                        + " for '" + entry.getKey() + "' \u2014 must be >= 0");
                }
            }
        }
        if (p.specialCards != null) {
            for (String key : p.specialCards.keySet()) {
                try {
                    SpecialCardType.valueOf(key);
                } catch (IllegalArgumentException e) {
                    throw new IllegalStateException(
                        ctx + " has unknown special card key '" + key
                        + "'. Valid values: " + validNames(SpecialCardType.values()), e);
                }
            }
        }
        if (p.handCards != null) {
            for (int j = 0; j < p.handCards.size(); j++) {
                validateCardSaveData(p.handCards.get(j),
                    ctx + " hand card at index " + j);
            }
        }
    }

    private static void validateCardSaveData(CardSaveData card, String ctx) {
        if (card == null) {
            throw new IllegalStateException(
                ctx + " is null \u2014 remove the entry or provide a valid card object");
        }
        if (card.cardType == null || card.cardType.isBlank()) {
            throw new IllegalStateException(ctx + " is missing 'card_type'");
        }
        CardType type;
        try {
            type = CardType.valueOf(card.cardType);
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException(
                ctx + " has unknown 'card_type' '" + card.cardType
                + "'. Valid values: " + validNames(CardType.values()), e);
        }
        if (card.monopolyTarget != null && !card.monopolyTarget.isBlank()) {
            if (type != CardType.MONOPOLY) {
                throw new IllegalStateException(
                    ctx + " has 'monopoly_target' set but card type is '" + card.cardType
                    + "' \u2014 'monopoly_target' is only valid for MONOPOLY cards");
            }
            try {
                ResourceType.valueOf(card.monopolyTarget);
            } catch (IllegalArgumentException e) {
                throw new IllegalStateException(
                    ctx + " has unknown 'monopoly_target' '" + card.monopolyTarget
                    + "'. Valid values: " + validNames(ResourceType.values()), e);
            }
        }
    }

    private static void validateRootFields(GameSaveData data) {
        if (data.currentPhase == null || data.currentPhase.isBlank()) {
            throw new IllegalStateException(
                "Save file is missing required field 'current_phase'");
        }
        try {
            GamePhase.valueOf(data.currentPhase);
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException(
                "Save file has unknown game phase '" + data.currentPhase
                + "'. Valid values: " + validNames(GamePhase.values()), e);
        }

        if (data.turnNumber < 0) {
            throw new IllegalStateException(
                "Save file has invalid 'turn_number' " + data.turnNumber + " — must be >= 0");
        }
        if (data.setupSettlementCount < 0) {
            throw new IllegalStateException(
                "Save file has invalid 'setup_settlement_count' " + data.setupSettlementCount
                + " — must be >= 0");
        }
        if (data.activePlayerIndex < 0) {
            throw new IllegalStateException(
                "Save file has invalid 'active_player_index' " + data.activePlayerIndex
                + " — must be >= 0");
        }
    }

    // --- Bank, deck, dice, timer ---

    private static void validateBankSaveData(Map<String, Integer> bank) {
        for (Map.Entry<String, Integer> entry : bank.entrySet()) {
            try {
                ResourceType.valueOf(entry.getKey());
            } catch (IllegalArgumentException e) {
                throw new IllegalStateException(
                    "Bank has unknown resource key '" + entry.getKey()
                    + "'. Valid values: " + validNames(ResourceType.values()), e);
            }
            if (entry.getValue() == null) {
                throw new IllegalStateException(
                    "Bank resource '" + entry.getKey() + "' has a null amount");
            }
            if (entry.getValue() < 0 || entry.getValue() > MAX_BANK_RESOURCE) {
                throw new IllegalStateException(
                    "Bank resource '" + entry.getKey() + "' has amount " + entry.getValue()
                    + " which is out of range [0, " + MAX_BANK_RESOURCE + "]");
            }
        }
    }

    private static void validateDeckSaveData(CardDeckSaveData deck) {
        if (deck.drawPile != null) {
            for (int i = 0; i < deck.drawPile.size(); i++) {
                validateCardSaveData(deck.drawPile.get(i), "Deck draw pile card at index " + i);
            }
        }
        if (deck.discardPile != null) {
            for (int i = 0; i < deck.discardPile.size(); i++) {
                validateCardSaveData(deck.discardPile.get(i),
                    "Deck discard pile card at index " + i);
            }
        }
    }

    private static void validateDiceSaveData(DiceSaveData dice) {
        if (dice.die1 < MIN_DIE || dice.die1 > MAX_DIE) {
            throw new IllegalStateException(
                "Last dice result 'die1' value " + dice.die1
                + " is out of range [" + MIN_DIE + ", " + MAX_DIE + "]");
        }
        if (dice.die2 < MIN_DIE || dice.die2 > MAX_DIE) {
            throw new IllegalStateException(
                "Last dice result 'die2' value " + dice.die2
                + " is out of range [" + MIN_DIE + ", " + MAX_DIE + "]");
        }
    }

    private static void validateTimerSaveData(TimerSaveData timer) {
        if (timer.remainingSeconds < 0) {
            throw new IllegalStateException(
                "Timer 'remaining_seconds' " + timer.remainingSeconds
                + " is negative — must be >= 0");
        }
        if (timer.paused && !timer.running) {
            throw new IllegalStateException(
                "Timer state is invalid: 'paused' is true but 'running' is false"
                + " — a paused timer must also have 'running' set to true");
        }
    }

    // --- Board ---

    private static void validateBoardSaveData(BoardSaveData board, int playerCount) {
        if (board == null) {
            throw new IllegalStateException("Save file is missing required field 'board'");
        }
        if (board.hexTiles == null || board.hexTiles.isEmpty()) {
            throw new IllegalStateException("Board 'hex_tiles' list is missing or empty");
        }
        if (board.intersections == null || board.intersections.isEmpty()) {
            throw new IllegalStateException("Board 'intersections' list is missing or empty");
        }
        if (board.paths == null || board.paths.isEmpty()) {
            throw new IllegalStateException("Board 'paths' list is missing or empty");
        }
        Set<Integer> tileIds = validateHexTiles(board.hexTiles);
        Set<Integer> intersectionIds = validateIntersections(
            board.intersections, tileIds, playerCount);
        validatePaths(board.paths, intersectionIds, playerCount);
        if (board.harbors != null) {
            validateHarbors(board.harbors, intersectionIds);
        }
    }

    private static Set<Integer> validateHexTiles(List<HexTileSaveData> hexTiles) {
        Set<Integer> ids = new HashSet<>();
        int robberCount = 0;
        for (int i = 0; i < hexTiles.size(); i++) {
            HexTileSaveData tile = hexTiles.get(i);
            String ctx = "Hex tile at index " + i + " (id=" + tile.id + ")";
            if (!ids.add(tile.id)) {
                throw new IllegalStateException(
                    ctx + " has duplicate id " + tile.id
                    + " \u2014 all hex tile ids must be unique");
            }
            if (tile.terrain == null || tile.terrain.isBlank()) {
                throw new IllegalStateException(ctx + " is missing 'terrain'");
            }
            TerrainType terrain;
            try {
                terrain = TerrainType.valueOf(tile.terrain);
            } catch (IllegalArgumentException e) {
                throw new IllegalStateException(
                    ctx + " has unknown terrain '" + tile.terrain
                    + "'. Valid values: " + validNames(TerrainType.values()), e);
            }
            if (terrain == TerrainType.DESERT) {
                if (tile.token != null) {
                    throw new IllegalStateException(
                        ctx + " has terrain DESERT but also has number token " + tile.token
                        + " \u2014 desert tiles must not have a number token");
                }
            } else if (tile.token != null) {
                if (tile.token < MIN_TOKEN || tile.token > MAX_TOKEN) {
                    throw new IllegalStateException(
                        ctx + " has number token " + tile.token
                        + " which is out of range [" + MIN_TOKEN + ", " + MAX_TOKEN + "]");
                }
            }
            if (tile.hasRobber) {
                robberCount++;
            }
        }
        if (robberCount > 1) {
            throw new IllegalStateException(
                "Board has " + robberCount + " hex tiles with 'has_robber' = true"
                + " \u2014 only one tile may hold the robber at a time");
        }
        return ids;
    }

    private static Set<Integer> validateIntersections(List<IntersectionSaveData> intersections,
                                                      Set<Integer> tileIds, int playerCount) {
        Set<Integer> ids = new HashSet<>();
        for (int i = 0; i < intersections.size(); i++) {
            IntersectionSaveData inter = intersections.get(i);
            String ctx = "Intersection at index " + i + " (id=" + inter.id + ")";
            if (!ids.add(inter.id)) {
                throw new IllegalStateException(
                    ctx + " has duplicate id " + inter.id
                    + " \u2014 all intersection ids must be unique");
            }
            if (inter.adjacentHexTileIds != null) {
                for (Integer hexId : inter.adjacentHexTileIds) {
                    if (hexId == null) {
                        throw new IllegalStateException(
                            ctx + " has a null entry in 'adjacent_hex_tile_ids'");
                    }
                    if (!tileIds.contains(hexId)) {
                        throw new IllegalStateException(
                            ctx + " references hex tile id " + hexId
                            + " which does not exist in 'hex_tiles'");
                    }
                }
            }
            if (inter.building != null) {
                validateBuildingSaveData(inter.building, ctx + " building", playerCount);
            }
        }
        return ids;
    }

    private static void validatePaths(List<PathSaveData> paths, Set<Integer> intersectionIds,
                                      int playerCount) {
        Set<Integer> ids = new HashSet<>();
        for (int i = 0; i < paths.size(); i++) {
            PathSaveData path = paths.get(i);
            String ctx = "Path at index " + i + " (id=" + path.id + ")";
            if (!ids.add(path.id)) {
                throw new IllegalStateException(
                    ctx + " has duplicate id " + path.id
                    + " \u2014 all path ids must be unique");
            }
            if (!intersectionIds.contains(path.intersectionAId)) {
                throw new IllegalStateException(
                    ctx + " references 'intersection_a_id' " + path.intersectionAId
                    + " which does not exist in 'intersections'");
            }
            if (!intersectionIds.contains(path.intersectionBId)) {
                throw new IllegalStateException(
                    ctx + " references 'intersection_b_id' " + path.intersectionBId
                    + " which does not exist in 'intersections'");
            }
            if (path.intersectionAId == path.intersectionBId) {
                throw new IllegalStateException(
                    ctx + " has 'intersection_a_id' == 'intersection_b_id' ("
                    + path.intersectionAId
                    + ") \u2014 a path must connect two different intersections");
            }
            if (path.road != null) {
                if (path.road.ownerIndex < 0 || path.road.ownerIndex >= playerCount) {
                    throw new IllegalStateException(
                        ctx + " road has 'owner_index' " + path.road.ownerIndex
                        + " which is out of bounds for " + playerCount + " player(s)"
                        + " (valid range: 0\u2013" + (playerCount - 1) + ")");
                }
            }
        }
    }

    private static void validateHarbors(List<HarborSaveData> harbors,
                                        Set<Integer> intersectionIds) {
        Set<Integer> ids = new HashSet<>();
        for (int i = 0; i < harbors.size(); i++) {
            HarborSaveData harbor = harbors.get(i);
            String ctx = "Harbor at index " + i + " (id=" + harbor.id + ")";
            if (!ids.add(harbor.id)) {
                throw new IllegalStateException(
                    ctx + " has duplicate id " + harbor.id
                    + " \u2014 all harbor ids must be unique");
            }
            if (harbor.type == null || harbor.type.isBlank()) {
                throw new IllegalStateException(ctx + " is missing 'type'");
            }
            try {
                HarborType.valueOf(harbor.type);
            } catch (IllegalArgumentException e) {
                throw new IllegalStateException(
                    ctx + " has unknown harbor type '" + harbor.type
                    + "'. Valid values: " + validNames(HarborType.values()), e);
            }
            if (harbor.adjacentIntersectionIds != null) {
                for (Integer interId : harbor.adjacentIntersectionIds) {
                    if (interId == null) {
                        throw new IllegalStateException(
                            ctx + " has a null entry in 'adjacent_intersection_ids'");
                    }
                    if (!intersectionIds.contains(interId)) {
                        throw new IllegalStateException(
                            ctx + " references intersection id " + interId
                            + " which does not exist in 'intersections'");
                    }
                }
            }
        }
    }

    private static void validateBuildingSaveData(BuildingSaveData building, String ctx,
                                                 int playerCount) {
        if (building.type == null || building.type.isBlank()) {
            throw new IllegalStateException(ctx + " is missing 'type'");
        }
        try {
            BuildingType.valueOf(building.type);
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException(
                ctx + " has unknown building type '" + building.type
                + "'. Valid values: " + validNames(BuildingType.values()), e);
        }
        if (building.ownerIndex < 0 || building.ownerIndex >= playerCount) {
            throw new IllegalStateException(
                ctx + " has 'owner_index' " + building.ownerIndex
                + " which is out of bounds for " + playerCount + " player(s)"
                + " (valid range: 0\u2013" + (playerCount - 1) + ")");
        }
    }

    /** Returns a comma-separated bracket-enclosed list of enum names for error messages. */
    private static String validNames(Enum<?>[] values) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < values.length; i++) {
            sb.append(values[i].name());
            if (i < values.length - 1) sb.append(", ");
        }
        sb.append("]");
        return sb.toString();
    }

    private static Gson buildGson() {
        return new GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .setPrettyPrinting()
            .create();
    }

    private static java.nio.file.Path resolveSavePath(String filePath, String timestamp) {
        String ts = (timestamp == null || timestamp.isBlank())
            ? FILE_TS_FORMAT.format(LocalDateTime.now())
            : timestamp.replace(":", "-");

        if (filePath == null || filePath.isBlank()) {
            return Paths.get(DEFAULT_DIR, "save-" + ts + ".json");
        }

        java.nio.file.Path path = Paths.get(filePath);
        if (Files.exists(path) && Files.isDirectory(path)) {
            return path.resolve("save-" + ts + ".json");
        }

        if (filePath.endsWith("/")) {
            return Paths.get(filePath, "save-" + ts + ".json");
        }

        if (!filePath.endsWith(".json")) {
            return Paths.get(filePath + ".json");
        }

        return path;
    }

    private static GameSaveData toSaveData(Game game) {
        GameSaveData data = new GameSaveData();
        data.version = DEFAULT_VERSION;
        data.timestamp = Instant.now().toString();
        data.currentPhase = game.getCurrentPhase().name();
        data.turnNumber = game.getTurnNumber();
        data.activePlayerIndex = game.getTurnManager().getActiveIndex();
        data.setupSettlementCount = game.getSetupSettlementCount();

        TimerSaveData timer = new TimerSaveData();
        timer.remainingSeconds = game.getTurnManager().getRemainingTimerSeconds();
        timer.running = game.getTurnManager().isTimerRunning();
        timer.paused = game.getTurnManager().isTimerPaused();
        data.timer = timer;

        data.bank = buildBankSaveData(game.getBank());
        data.board = buildBoardSaveData(game.getBoard(), game.getPlayers());
        data.players = buildPlayersSaveData(game.getPlayers());
        data.deck = buildDeckSaveData(game.getCardDeck());
        data.lastDice = buildDiceSaveData(game.getLastDiceResult());

        return data;
    }

    private static Map<String, Integer> buildBankSaveData(Bank bank) {
        Map<String, Integer> result = new LinkedHashMap<>();
        for (ResourceType type : ResourceType.values()) {
            result.put(type.name(), bank.getCount(type));
        }
        return result;
    }

    private static BoardSaveData buildBoardSaveData(Board board, List<Player> players) {
        BoardSaveData data = new BoardSaveData();
        data.hexTiles = new ArrayList<>();
        data.intersections = new ArrayList<>();
        data.paths = new ArrayList<>();
        data.harbors = new ArrayList<>();

        Map<Player, Integer> playerIndex = indexPlayers(players);

        for (HexTile tile : board.getAllHexTiles()) {
            HexTileSaveData tileData = new HexTileSaveData();
            tileData.id = tile.getId();
            tileData.terrain = tile.getTerrainType().name();
            tileData.token = tile.getNumberToken() != null
                ? tile.getNumberToken().getValue()
                : null;
            tileData.hasRobber = tile.hasRobber();
            tileData.column = tile.getColumn();
            tileData.row = tile.getRow();
            data.hexTiles.add(tileData);
        }

        for (Intersection intersection : board.getAllIntersections()) {
            IntersectionSaveData interData = new IntersectionSaveData();
            interData.id = intersection.getId();
            interData.adjacentHexTileIds = new ArrayList<>();
            for (HexTile tile : intersection.getAdjacentHexTiles()) {
                interData.adjacentHexTileIds.add(tile.getId());
            }

            if (intersection.hasBuilding()) {
                Building building = intersection.getBuilding();
                BuildingSaveData buildingData = new BuildingSaveData();
                buildingData.type = building.getBuildingType().name();
                buildingData.ownerIndex = playerIndex.getOrDefault(building.getOwner(), -1);
                interData.building = buildingData;
            }

            data.intersections.add(interData);
        }

        for (Path path : board.getAllPaths()) {
            PathSaveData pathData = new PathSaveData();
            pathData.id = path.getId();
            pathData.intersectionAId = path.getIntersectionA().getId();
            pathData.intersectionBId = path.getIntersectionB().getId();
            if (path.hasRoad()) {
                RoadSaveData roadData = new RoadSaveData();
                roadData.ownerIndex = playerIndex.getOrDefault(path.getOwner(), -1);
                pathData.road = roadData;
            }
            data.paths.add(pathData);
        }

        for (Harbor harbor : board.getHarbors()) {
            HarborSaveData harborData = new HarborSaveData();
            harborData.id = harbor.getId();
            harborData.type = harbor.getHarborType().name();
            harborData.adjacentIntersectionIds = new ArrayList<>();
            for (Intersection intersection : harbor.getAdjacentIntersections()) {
                harborData.adjacentIntersectionIds.add(intersection.getId());
            }
            data.harbors.add(harborData);
        }

        return data;
    }

    private static List<PlayerSaveData> buildPlayersSaveData(List<Player> players) {
        List<PlayerSaveData> result = new ArrayList<>();
        for (Player player : players) {
            PlayerSaveData data = new PlayerSaveData();
            data.name = player.getName();
            data.color = player.getColor().name();
            data.isBot = player.isBot();

            data.resources = new LinkedHashMap<>();
            for (ResourceType type : ResourceType.values()) {
                data.resources.put(type.name(), player.getResourceCount(type));
            }

            data.specialCards = new LinkedHashMap<>();
            for (SpecialCardType type : SpecialCardType.values()) {
                data.specialCards.put(type.name(), player.hasSpecialCard(type));
            }

            data.knightsPlayed = player.getKnightsPlayed();
            data.longestRoadLength = player.getLongestRoadLength();

            data.handCards = new ArrayList<>();
            for (ExperimentCard card : player.getHandCards()) {
                if (card.isPluginCard()) continue; // Skip cards loaded from external plugins
                data.handCards.add(toCardSaveData(card));
            }

            result.add(data);
        }
        return result;
    }

    private static CardDeckSaveData buildDeckSaveData(CardDeck deck) {
        CardDeckSaveData data = new CardDeckSaveData();
        data.drawPile = new ArrayList<>();
        data.discardPile = new ArrayList<>();

        for (ExperimentCard card : deck.getDrawPile()) {
            if (card.isPluginCard()) continue; // Skip cards loaded from external plugins
            data.drawPile.add(toCardSaveData(card));
        }
        for (ExperimentCard card : deck.getDiscardPile()) {
            if (card.isPluginCard()) continue; // Skip cards loaded from external plugins
            data.discardPile.add(toCardSaveData(card));
        }
        return data;
    }

    private static DiceSaveData buildDiceSaveData(DiceResult result) {
        if (result == null) {
            return null;
        }
        DiceSaveData data = new DiceSaveData();
        data.die1 = result.getDie1();
        data.die2 = result.getDie2();
        return data;
    }

    private static CardSaveData toCardSaveData(ExperimentCard card) {
        CardSaveData data = new CardSaveData();
        data.cardType = card.getCardType().name();

        if (card instanceof DevelopmentCard dev) {
            data.revealed = dev.isRevealed();
            data.newlyDrawn = dev.isNewlyDrawn();
        }

        if (card instanceof ProgressCard prog) {
            data.consumed = prog.isConsumed();
        }

        if (card instanceof MonopolyCard monopoly) {
            ResourceType target = monopoly.getTargetResource();
            data.monopolyTarget = (target != null) ? target.name() : null;
        }

        return data;
    }

    private static Game fromSaveData(GameSaveData data) {
        if (data.players == null || data.players.isEmpty()) {
            throw new IllegalStateException("Save data has no players");
        }
        if (data.board == null) {
            throw new IllegalStateException("Save data has no board");
        }

        List<Player> players = buildPlayersFromSave(data.players);
        Board board = buildBoardFromSave(data.board);
        Bank bank = buildBankFromSave(data.bank);
        CardDeck deck = buildDeckFromSave(data.deck);

        DiceResult lastDice = null;
        if (data.lastDice != null) {
            lastDice = new DiceResult(data.lastDice.die1, data.lastDice.die2);
        }

        GamePhase phase = GamePhase.RESOURCE_GATHERING;
        if (data.currentPhase != null && !data.currentPhase.isBlank()) {
            phase = GamePhase.valueOf(data.currentPhase);
        }

        int activeIndex = Math.max(0, Math.min(players.size() - 1, data.activePlayerIndex));

        Game game = new Game(players, board, bank, deck, phase, data.turnNumber,
                             activeIndex, data.setupSettlementCount, lastDice);

        restorePlayersState(players, data.players);
        restoreBoardState(board, players, data.board.intersections, data.board.paths);

        if (data.timer != null && phase == GamePhase.TRADE_BUILD) {
            game.getTurnManager().restoreTimer(
                data.timer.remainingSeconds,
                data.timer.running,
                data.timer.paused,
                null
            );
        }

        return game;
    }

    private static List<Player> buildPlayersFromSave(List<PlayerSaveData> saved) {
        List<Player> players = new ArrayList<>();
        for (PlayerSaveData data : saved) {
            PlayerColor color = PlayerColor.valueOf(data.color);
            if (data.isBot) {
                players.add(new BotPlayer(data.name, color, defaultBotStrategy()));
            } else {
                players.add(new HumanPlayer(data.name, color));
            }
        }
        return players;
    }

    private static PlayerStrategy defaultBotStrategy() {
        return new PlayerStrategy() {
            @Override
            public List<banana.republic.player.Action> takeTurn(banana.republic.core.GameState state) {
                return Collections.emptyList();
            }

            @Override
            public Player chooseRobberTarget(banana.republic.core.GameState state,
                                             List<Player> candidates) {
                return (candidates == null || candidates.isEmpty()) ? null : candidates.get(0);
            }

            @Override
            public HexTile chooseRobberPlacement(banana.republic.core.GameState state) {
                return null;
            }
        };
    }

    private static Board buildBoardFromSave(BoardSaveData boardData) {
        List<HexTile> tiles = new ArrayList<>();
        Map<Integer, HexTile> tileById = new HashMap<>();
        for (HexTileSaveData tileData : boardData.hexTiles) {
            TerrainType terrain = TerrainType.valueOf(tileData.terrain);
            NumberToken token = (tileData.token != null) ? new NumberToken(tileData.token) : null;
            HexTile tile = new HexTile(tileData.id, terrain, token, tileData.hasRobber,
                                       tileData.column, tileData.row);
            tiles.add(tile);
            tileById.put(tile.getId(), tile);
        }

        List<Intersection> intersections = new ArrayList<>();
        Map<Integer, Intersection> intersectionById = new HashMap<>();
        for (IntersectionSaveData interData : boardData.intersections) {
            List<HexTile> adjHex = new ArrayList<>();
            if (interData.adjacentHexTileIds != null) {
                for (Integer id : interData.adjacentHexTileIds) {
                    HexTile tile = tileById.get(id);
                    if (tile != null) {
                        adjHex.add(tile);
                    }
                }
            }
            Intersection intersection = new Intersection(interData.id, adjHex, new ArrayList<>());
            intersections.add(intersection);
            intersectionById.put(intersection.getId(), intersection);
        }

        List<Path> paths = new ArrayList<>();
        Map<Integer, Path> pathById = new HashMap<>();
        for (PathSaveData pathData : boardData.paths) {
            Intersection a = intersectionById.get(pathData.intersectionAId);
            Intersection b = intersectionById.get(pathData.intersectionBId);
            if (a == null || b == null) {
                continue;
            }
            Path path = new Path(pathData.id, a, b);
            paths.add(path);
            pathById.put(path.getId(), path);
            a.addAdjacentPath(path);
            b.addAdjacentPath(path);
        }

        List<Harbor> harbors = new ArrayList<>();
        if (boardData.harbors != null) {
            for (HarborSaveData harborData : boardData.harbors) {
                HarborType type = HarborType.valueOf(harborData.type);
                List<Intersection> adj = new ArrayList<>();
                if (harborData.adjacentIntersectionIds != null) {
                    for (Integer id : harborData.adjacentIntersectionIds) {
                        Intersection inter = intersectionById.get(id);
                        if (inter != null) {
                            adj.add(inter);
                        }
                    }
                }
                harbors.add(new Harbor(harborData.id, type, adj));
            }
        }

        return new Board(tiles, intersections, paths, harbors);
    }

    private static Bank buildBankFromSave(Map<String, Integer> savedBank) {
        BankImpl bank = new BankImpl();
        if (savedBank == null) {
            return bank;
        }

        for (ResourceType type : ResourceType.values()) {
            int current = bank.getCount(type);
            Integer rawTarget = savedBank.get(type.name());
            if (rawTarget == null) {
                continue;
            }
            int target = Math.max(0, Math.min(19, rawTarget));
            if (target < current) {
                bank.takeResource(type, current - target);
            } else if (target > current) {
                bank.returnResource(type, target - current);
            }
        }

        return bank;
    }

    private static CardDeck buildDeckFromSave(CardDeckSaveData deckData) {
        CardDeck deck = new CardDeck();
        if (deckData == null) {
            return deck;
        }

        if (deckData.drawPile != null) {
            for (int i = deckData.drawPile.size() - 1; i >= 0; i--) {
                ExperimentCard card = toCardFromSaveData(deckData.drawPile.get(i));
                deck.addCard(card);
            }
        }

        if (deckData.discardPile != null) {
            for (CardSaveData cardData : deckData.discardPile) {
                deck.addToDiscardPile(toCardFromSaveData(cardData));
            }
        }

        return deck;
    }

    private static void restorePlayersState(List<Player> players,
                                            List<PlayerSaveData> savedPlayers) {
        for (int i = 0; i < players.size(); i++) {
            Player player = players.get(i);
            PlayerSaveData data = savedPlayers.get(i);

            if (data.resources != null) {
                for (Map.Entry<String, Integer> entry : data.resources.entrySet()) {
                    ResourceType type = ResourceType.valueOf(entry.getKey());
                    int amount = Math.max(0, entry.getValue());
                    if (amount > 0) {
                        player.addResource(type, amount);
                    }
                }
            }

            if (data.specialCards != null) {
                for (Map.Entry<String, Boolean> entry : data.specialCards.entrySet()) {
                    SpecialCardType type = SpecialCardType.valueOf(entry.getKey());
                    player.setSpecialCard(type, Boolean.TRUE.equals(entry.getValue()));
                }
            }

            for (int k = 0; k < Math.max(0, data.knightsPlayed); k++) {
                player.incrementKnightsPlayed();
            }

            player.setLongestRoadLength(Math.max(0, data.longestRoadLength));

            if (data.handCards != null) {
                for (CardSaveData cardData : data.handCards) {
                    player.addCard(toCardFromSaveData(cardData));
                }
            }
        }
    }

    private static void restoreBoardState(Board board, List<Player> players,
                                          List<IntersectionSaveData> intersections,
                                          List<PathSaveData> paths) {
        Map<Integer, Intersection> intersectionById = new HashMap<>();
        for (Intersection intersection : board.getAllIntersections()) {
            intersectionById.put(intersection.getId(), intersection);
        }

        Map<Integer, Path> pathById = new HashMap<>();
        for (Path path : board.getAllPaths()) {
            pathById.put(path.getId(), path);
        }

        if (intersections != null) {
            for (IntersectionSaveData interData : intersections) {
                if (interData.building == null) {
                    continue;
                }
                Intersection intersection = intersectionById.get(interData.id);
                if (intersection == null || intersection.hasBuilding()) {
                    continue;
                }
                if (interData.building.ownerIndex < 0 ||
                    interData.building.ownerIndex >= players.size()) {
                    continue;
                }

                Player owner = players.get(interData.building.ownerIndex);
                BuildingType type = BuildingType.valueOf(interData.building.type);
                Building building = (type == BuildingType.LABORATORIUM)
                    ? owner.getSupply().takeLaboratorium()
                    : owner.getSupply().takePosPantau();
                intersection.placeBuilding(building);
            }
        }

        if (paths != null) {
            for (PathSaveData pathData : paths) {
                if (pathData.road == null) {
                    continue;
                }
                Path path = pathById.get(pathData.id);
                if (path == null || path.hasRoad()) {
                    continue;
                }
                int ownerIndex = pathData.road.ownerIndex;
                if (ownerIndex < 0 || ownerIndex >= players.size()) {
                    continue;
                }
                Player owner = players.get(ownerIndex);
                Road road = owner.getSupply().takeRoad();
                path.placeRoad(road);
            }
        }
    }

    private static ExperimentCard toCardFromSaveData(CardSaveData data) {
        if (data == null || data.cardType == null) {
            return new VictoryPointCard();
        }

        CardType type = CardType.valueOf(data.cardType);
        ExperimentCard card;
        switch (type) {
            case KNIGHT -> card = new KnightCard();
            case ROAD_BUILDING -> card = new RoadBuildingCard();
            case MONOPOLY -> card = new MonopolyCard();
            case VICTORY_POINT -> card = new VictoryPointCard();
            default -> card = new VictoryPointCard();
        }

        if (card instanceof DevelopmentCard dev) {
            dev.setNewlyDrawn(data.newlyDrawn);
            if (data.revealed) {
                dev.reveal();
            }
        }

        if (card instanceof ProgressCard prog && data.consumed) {
            prog.consume();
        }

        if (card instanceof MonopolyCard monopoly && data.monopolyTarget != null) {
            monopoly.setTargetResource(ResourceType.valueOf(data.monopolyTarget));
        }

        return card;
    }

    private static Map<Player, Integer> indexPlayers(List<Player> players) {
        Map<Player, Integer> map = new HashMap<>();
        for (int i = 0; i < players.size(); i++) {
            map.put(players.get(i), i);
        }
        return map;
    }
}
