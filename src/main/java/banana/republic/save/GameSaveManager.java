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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Game save/load manager (JSON format).
 * Refer to class-diagram/Module5_UI_Plugin_Save.puml for full specification.
 */
public class GameSaveManager {

    private static final String DEFAULT_VERSION = "1.0";
    private static final String DEFAULT_DIR = "./saves";
    private static final DateTimeFormatter FILE_TS_FORMAT =
        DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

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

        return fromSaveData(data);
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
            data.drawPile.add(toCardSaveData(card));
        }
        for (ExperimentCard card : deck.getDiscardPile()) {
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
