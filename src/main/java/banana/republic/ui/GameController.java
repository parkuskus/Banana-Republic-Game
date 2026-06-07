package banana.republic.ui;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import banana.republic.board.Harbor;
import banana.republic.board.HarborType;
import banana.republic.board.HexTile;
import banana.republic.core.Game;
import banana.republic.core.GamePhase;
import banana.republic.core.LogEntry;
import banana.republic.core.TurnOrder;
import banana.republic.dice.DiceResult;
import banana.republic.player.Player;
import banana.republic.player.PlayerColor;
import banana.republic.resource.ResourceType;
import banana.republic.ui.command.BuildUiService;
import banana.republic.ui.command.GameActionUiService;
import banana.republic.ui.command.PluginUiService;
import banana.republic.ui.command.UiActionResult;
import banana.republic.ui.board.BoardCoordinateMapper;
import banana.republic.ui.board.BoardView;
import banana.republic.ui.board.BuildInteractionController;
import banana.republic.ui.board.BuildOverlayView;
import banana.republic.ui.board.HarborView;
import banana.republic.ui.board.IntersectionCoordinate;
import banana.republic.ui.board.RobberOverlayView;
import banana.republic.ui.dialog.DialogHostService;
import banana.republic.ui.dialog.NumberInputDialogService;
import banana.republic.ui.flow.BotTurnController;
import banana.republic.ui.flow.GameDialogFlowController;
import banana.republic.ui.flow.ResourceProductionTracker;
import banana.republic.ui.flow.RobberFlowController;
import banana.republic.ui.flow.SetupOrderController;
import banana.republic.ui.flow.TurnFlowController;
import banana.republic.ui.presenter.BuildCostPresenter;
import banana.republic.ui.presenter.DicePresenter;
import banana.republic.ui.presenter.LogbookPresenter;
import banana.republic.ui.presenter.PhaseUiPresenter;
import banana.republic.ui.presenter.PlayerPanelPresenter;
import banana.republic.ui.util.ResponsiveScaleBinder;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;


public class GameController implements Initializable {

    @FXML private StackPane tradeDialogOverlay;
    @FXML private StackPane cardDialogOverlay;
    @FXML private StackPane stealDialogOverlay;
    @FXML private StackPane settingsDialogOverlay;
    @FXML private StackPane victoryDialogOverlay;
    @FXML private StackPane discardDialogOverlay;
    @FXML private StackPane setupOrderOverlay;
    @FXML private StackPane hexdesert;
    @FXML private StackPane harborLV1;
    @FXML private StackPane harborLV2;
    @FXML private StackPane harborLU;
    @FXML private StackPane harborLD;
    @FXML private StackPane harborRV;
    @FXML private StackPane harborRU1;
    @FXML private StackPane harborRU2;
    @FXML private StackPane harborRD1;
    @FXML private StackPane harborRD2;

    @FXML private VBox sidePanel;
    @FXML private HBox playerPanel1, playerPanel2, playerPanel3, playerPanel4;

    @FXML private Label lblWoodCount, lblBrickCount, lblWheatCount, lblOreCount, lblBananaCount;
    @FXML private Label longestRoadStatusLabel;
    @FXML private Label largestArmyStatusLabel;
    @FXML private Label timerLabel;
    @FXML private ImageView diceImage1, diceImage2;
    @FXML private Label currentPlayerLabel;
    @FXML private Label setupOrderStatusLabel;
    @FXML private VBox setupOrderPlayersBox;

    @FXML private VBox logbookContainer;
    @FXML private Label logEntry1, logEntry2, logEntry3, logEntry4, logEntry5, logEntry6, logEntry7, logEntry8;
    @FXML private Label logEntry9, logEntry10, logEntry11, logEntry12, logEntry13, logEntry14, logEntry15, logEntry16;
    @FXML private Label buildCostLeftLabel, buildCostRightLabel;

    @FXML private Button btnRollDice, btnSetDice, btnBuild, btnTrade, btnBuyCard, btnCard, btnDeclareVictory, btnSettings, btnEndTurn;
    @FXML private Button btnSteal, btnDiscard, btnEndGame;
    @FXML private Button btnStartSetupOrder;
    @FXML private Label currentConditionLabel;

    // ============================================================
    // Model & State
    // ============================================================
    private Game game;
    private List<Label> logLabels;
    private List<HBox> playerPanels;
    private Image[] diceImages = new Image[6];
    private Image gambarAnchorGlobal = null;
    private Image gambarNimonGlobal = null;
    // Tambahkan di bagian atas bersama variabel state lainnya
    private banana.republic.board.Intersection lastSetupSettlement = null;

    private final java.util.Map<StackPane, HexTile> visualToModelTile = new java.util.HashMap<>();

    // LAYERING BEBAS CRISS-CROSS
    private Pane permanentBuildLayer;
    private Pane buildOverlayPane;
    private Pane robberOverlayPane;
    private List<StackPane> mainHexesOnly = new ArrayList<>();

    // Menyimpan koordinat visual dari semua titik persimpangan
    private final Map<banana.republic.board.Intersection, double[]> globalIntersectionCoords = new java.util.HashMap<>();
    private final List<double[]> harborPoints = new ArrayList<>();
    private final List<VisualHarborSpec> visualHarborSpecs = new ArrayList<>();

    private static Game currentGame;
    private final UiDialogs dialogs = new UiDialogs();
    private final UiNavigator navigator = new AppUiNavigator();
    private final DialogHostService dialogHostService = new DialogHostService(GameController.class);
    private final NumberInputDialogService numberInputDialogService = new NumberInputDialogService();
    private final BoardCoordinateMapper boardCoordinateMapper = new BoardCoordinateMapper();
    private final BoardView boardView = new BoardView();
    private final BuildInteractionController buildInteractionController = new BuildInteractionController();
    private final BuildOverlayView buildOverlayView = new BuildOverlayView(boardView);
    private final HarborView harborView = new HarborView();
    private final RobberOverlayView robberOverlayView = new RobberOverlayView();
    private final BuildUiService buildUiService = new BuildUiService();
    private final GameActionUiService gameActionUiService = new GameActionUiService();
    private final PluginUiService pluginUiService = new PluginUiService();
    private final ResponsiveScaleBinder responsiveScaleBinder = new ResponsiveScaleBinder();
    private final ResourceProductionTracker resourceProductionTracker = new ResourceProductionTracker();
    private BuildCostPresenter buildCostPresenter;
    private DicePresenter dicePresenter;
    private LogbookPresenter logbookPresenter;
    private PhaseUiPresenter phaseUiPresenter;
    private PlayerPanelPresenter playerPanelPresenter;
    private SetupOrderController setupOrderController;
    private TurnFlowController turnFlowController;
    private BotTurnController botTurnController;
    private GameDialogFlowController gameDialogFlowController;
    private RobberFlowController robberFlowController;
    private boolean timerExpiryUiQueued = false;
    private boolean setupOrderPending = false;

    private enum InteractionMode {
        NONE, SETTLEMENT, ROAD, CITY, ROBBER, BUILD_OVERLAY, ROAD_BUILDING_CARD
    }
    private InteractionMode currentMode = InteractionMode.NONE;

    private static Thread bgMusicThread;

    private record VisualHarborSpec(StackPane hexTile, int firstCorner, int secondCorner, HarborType harborType) {
    }

    // ============================================================
    // Initialization
    // ============================================================
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        String[] diceNames = {"one", "two", "three", "four", "five", "six"};
        for (int i = 0; i < 6; i++) {
            try {
                URL specific = getClass().getResource("/icons/dice_" + diceNames[i] + ".png");
                if (specific == null) specific = getClass().getResource("/icons/dice_one.png");
                diceImages[i] = new Image(specific.toExternalForm());
            } catch (Exception e) {
                diceImages[i] = null;
            }
        }

        try {
            URL imageUrl = getClass().getResource("/icons/anchor.png");
            if (imageUrl != null) {
                gambarAnchorGlobal = new Image(imageUrl.toExternalForm());
            } else {
                gambarAnchorGlobal = new Image("file:icons/anchor.png");
            }
        } catch (Exception e) {
            System.out.println("Gagal memuat gambar jangkar: " + e.getMessage());
        }

        logLabels = Arrays.asList(
                logEntry1, logEntry2, logEntry3, logEntry4, logEntry5, logEntry6, logEntry7, logEntry8,
                logEntry9, logEntry10, logEntry11, logEntry12, logEntry13, logEntry14, logEntry15, logEntry16
        );
        playerPanels = Arrays.asList(playerPanel1, playerPanel2, playerPanel3, playerPanel4);
        dicePresenter = new DicePresenter(diceImage1, diceImage2, diceImages);
        logbookPresenter = new LogbookPresenter(logLabels);
        buildCostPresenter = new BuildCostPresenter(buildCostLeftLabel, buildCostRightLabel);
        playerPanelPresenter = new PlayerPanelPresenter(
                playerPanels,
                currentPlayerLabel,
                lblWoodCount,
                lblBrickCount,
                lblWheatCount,
                lblOreCount,
                lblBananaCount,
                longestRoadStatusLabel,
                largestArmyStatusLabel
        );
        phaseUiPresenter = new PhaseUiPresenter(
                btnRollDice,
                btnSetDice,
                btnBuild,
                btnTrade,
                btnBuyCard,
                btnCard,
                btnDeclareVictory,
                btnSettings,
                btnEndTurn,
                btnSteal,
                btnDiscard,
                btnEndGame,
                btnStartSetupOrder,
                currentConditionLabel
        );
        setupOrderController = new SetupOrderController(
                setupOrderOverlay,
                setupOrderPlayersBox,
                setupOrderStatusLabel,
                btnStartSetupOrder,
                this::showDiceResult
        );

        if (hexdesert != null) {
            harborPoints.clear();
            visualHarborSpecs.clear();
            pasangEventKlik(hexdesert);
            pasangAnchorSudut(harborLU, 5, 0, "3:1", HarborType.GENERIC_3TO1);
            pasangAnchorSudut(harborRU1, 0, 1, "2:1 Banana", HarborType.BANANA_2TO1);
            pasangAnchorSudut(harborRU2, 1, 2, "3:1", HarborType.GENERIC_3TO1);
            pasangAnchorSudut(harborRV, 2, 3, "2:1 Ore", HarborType.ORE_2TO1);
            pasangAnchorSudut(harborRD2, 2, 3, "3:1", HarborType.GENERIC_3TO1);
            pasangAnchorSudut(harborRD1, 3, 4, "2:1 Wheat", HarborType.WHEAT_2TO1);
            pasangAnchorSudut(harborLD, 3, 4, "2:1 Brick", HarborType.BRICK_2TO1);
            pasangAnchorSudut(harborLV2, 4, 5, "3:1", HarborType.GENERIC_3TO1);
            pasangAnchorSudut(harborLV1, 5, 0, "2:1 Wood", HarborType.WOOD_2TO1);
        }

        setupAllTileClickHandlers();
    }

    public void initialize(Game game) {
        this.game = game;
        currentGame = game;
        if (game == null) return;
        turnFlowController = new TurnFlowController(game, this::handleTradeBuildTimerTick, this::refreshAllUI, this::updatePhaseUI);
        botTurnController = new BotTurnController(
                game,
                this::showDiceResult,
                this::handleTradeBuildTimerTick,
                this::refreshAllUI,
                this::updatePhaseUI);
        gameDialogFlowController = new GameDialogFlowController(
                dialogHostService,
                game,
                tradeDialogOverlay,
                cardDialogOverlay,
                stealDialogOverlay,
                settingsDialogOverlay,
                victoryDialogOverlay,
                discardDialogOverlay,
                this::refreshAllUI,
                this::enterRobberMode,
                this::enterRoadBuildingCardMode,
                this::handleTradeBuildTimerTick,
                this::showError);
        robberFlowController = new RobberFlowController(
                game,
                visualToModelTile,
                boardCoordinateMapper,
                this::removeRobberOverlay,
                this::refreshAllUI,
                this::updatePhaseUI,
                victims -> {
                    if (gameDialogFlowController != null) gameDialogFlowController.openSteal(victims);
                },
                this::handleTradeBuildTimerTick,
                this::showError);

        buildVisualToModelMapping();
        if (setupOrderController != null) setupOrderController.initialize(game);
        setupOrderPending = setupOrderController != null && setupOrderController.isPending();

        if (hexdesert != null) {
            Group parentMap = (Group) hexdesert.getParent();
            if (permanentBuildLayer == null) {
                permanentBuildLayer = new Pane();
                permanentBuildLayer.setPickOnBounds(false);
                parentMap.getChildren().add(permanentBuildLayer);
            }
            // make map scale responsively to its container so the view fits different laptop screens
            setupResponsiveScaling(parentMap);
        }

        refreshAllUI();
        updateSetupOrderOverlay();
        updatePhaseUI();

        if (!setupOrderPending && game.getActivePlayer() != null && game.getActivePlayer().isBot()) {
            handleBotTurn();
        }

        try {
            if (bgMusicThread == null || !bgMusicThread.isAlive()) {
                bgMusicThread = new Thread(() -> {
                    while (true) {
                        try {
                            java.io.InputStream is = getClass().getResourceAsStream("/bg-music.mp3");
                            if (is != null) {
                                javazoom.jl.player.Player player = new javazoom.jl.player.Player(is);
                                player.play();
                            } else {
                                break;
                            }
                        } catch (Exception e) {
                            break;
                        }
                    }
                });
                bgMusicThread.setDaemon(true);
                bgMusicThread.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setGame(Game game) {
        initialize(game);
    }

    public static Game getCurrentGame() {
        return currentGame;
    }

    // ============================================================
    // Action Handlers
    // ============================================================
    @FXML
    private void onStartSetupOrder() {
        if (game == null || !setupOrderPending) return;
        try {
            if (setupOrderController != null) setupOrderController.rollCurrentPlayer();
            setupOrderPending = setupOrderController != null && setupOrderController.isPending();
            refreshAllUI();
            updatePhaseUI();

            if (!setupOrderPending && game.getActivePlayer() != null && game.getActivePlayer().isBot()) {
                handleBotTurn();
            }
        } catch (IllegalStateException | IllegalArgumentException e) {
            showError(e.getMessage());
        }
    }

    @FXML
    private void onRollDice() {
        if (game == null || turnFlowController == null) return;
        try {
            DiceResult result = rollDiceAndSyncProduction();
            turnFlowController.afterDiceRoll(result, this::queueHumanDiscardsAndShow);
        } catch (IllegalStateException | IllegalArgumentException e) {
            showError(e.getMessage());
        }
    }

    private void queueHumanDiscardsAndShow() {
        List<Player> playersToDiscard = new ArrayList<>();
        for (Player p : game.getPlayers()) {
            if (!p.isBot() && p.getTotalResourceCount() > Game.HAND_LIMIT) {
                playersToDiscard.add(p);
            }
        }
        if (gameDialogFlowController != null) gameDialogFlowController.queueDiscardsAndShow(playersToDiscard);
    }

    @FXML
    private void onSetDice() {
        if (game == null) return;
        
        if (game.getCurrentPhase() != GamePhase.RESOURCE_GATHERING) {
            showError("Dadu sudah dikocok (atau tidak bisa melempar dadu saat ini).");
            return;
        }
        
        var requestedTotal = numberInputDialogService.requestInt(
                "Set Dice Manual",
                "Masukkan total nilai dadu (2-12)",
                "7");
        requestedTotal.ifPresent(total -> {
            try {
                if (total < 2 || total > 12) {
                    showError("Total dadu harus antara 2 hingga 12.");
                    return;
                }
                
                // Pisahkan total menjadi 2 nilai dadu (1-6)
                int d1 = Math.min(6, total - 1);
                int d2 = total - d1;
                
                game.getDice().setManualMode(true);
                game.getDice().setManualValues(d1, d2);
                DiceResult result;
                try {
                    result = rollDiceAndSyncProduction();
                } finally {
                    game.getDice().setManualMode(false);
                }
                if (turnFlowController != null) {
                    turnFlowController.afterDiceRoll(result, this::queueHumanDiscardsAndShow);
                }
            } catch (Exception e) {
                showError(e.getMessage());
            }
        });
    }

    private DiceResult rollDiceAndSyncProduction() {
        Map<Player, Map<ResourceType, Integer>> beforeRoll = resourceProductionTracker.snapshot(game);
        DiceResult result = game.rollDice();
        if (!result.isSeven()) {
            reconcileVisualResourceProduction(result.getTotal(), beforeRoll);
        }
        showDiceResult(result);

        if (!result.isSeven()) {
            String productionSummary = resourceProductionTracker.summarize(game, beforeRoll);
            game.getGameLog().addEntry(
                    LogEntry.EventType.RESOURCE_PRODUCTION,
                    productionSummary
            );
        }

        refreshAllUI();
        return result;
    }

    private void reconcileVisualResourceProduction(int roll,
                                                   Map<Player, Map<ResourceType, Integer>> beforeRoll) {
        Map<Player, Map<ResourceType, Integer>> expected = calculateVisualProductionForRoll(roll);
        for (Map.Entry<Player, Map<ResourceType, Integer>> playerEntry : expected.entrySet()) {
            Player player = playerEntry.getKey();
            Map<ResourceType, Integer> before = beforeRoll.get(player);
            if (before == null) continue;

            for (Map.Entry<ResourceType, Integer> resourceEntry : playerEntry.getValue().entrySet()) {
                ResourceType type = resourceEntry.getKey();
                int expectedGain = resourceEntry.getValue();
                int actualGain = player.getResourceCount(type) - before.getOrDefault(type, 0);
                int missing = expectedGain - actualGain;
                if (missing <= 0) continue;

                int available = game.getBank().getCount(type);
                int granted = Math.min(missing, available);
                if (granted > 0) {
                    game.getBank().takeResource(type, granted);
                    player.addResource(type, granted);
                }
            }
        }
    }

    private Map<Player, Map<ResourceType, Integer>> calculateVisualProductionForRoll(int roll) {
        Map<Player, Map<ResourceType, Integer>> expected = new java.util.HashMap<>();
        for (Map.Entry<banana.republic.board.Intersection, double[]> entry : globalIntersectionCoords.entrySet()) {
            banana.republic.board.Intersection intersection = entry.getKey();
            if (!intersection.hasBuilding() || intersection.getOwner() == null) continue;

            double[] point = entry.getValue();
            int productionAmount = intersection.getBuilding().getProductionAmount();
            if (productionAmount <= 0) continue;

            for (Map.Entry<StackPane, HexTile> tileEntry : visualToModelTile.entrySet()) {
                HexTile tile = tileEntry.getValue();
                if (tile == null || !tile.canProduce() || tile.getNumberToken() == null) continue;
                if (tile.getNumberToken().getValue() != roll) continue;
                if (!isVisualCornerAdjacent(tileEntry.getKey(), point)) continue;

                ResourceType type = tile.getResourceType();
                if (type == null) continue;
                expected
                    .computeIfAbsent(intersection.getOwner(), ignored -> new java.util.EnumMap<>(ResourceType.class))
                    .merge(type, productionAmount, Integer::sum);
            }
        }
        return expected;
    }

    private boolean isVisualCornerAdjacent(StackPane visualTile, double[] point) {
        if (visualTile == null || point == null || point.length < 2) return false;
        double[][] corners = boardCoordinateMapper.getHexCorners(visualTile);
        for (double[] corner : corners) {
            if (Math.hypot(corner[0] - point[0], corner[1] - point[1]) < 28.0) {
                return true;
            }
        }
        return false;
    }

    // --- FITUR BUILD OVERLAY INTERACTIVE TER-ROBUST ---
    @FXML
    private void onBuild() {
        if (game == null) return;
        if (setupOrderPending) {
            showError("Tentukan urutan pemain terlebih dahulu.");
            return;
        }
        GamePhase phase = game.getCurrentPhase();
        if (phase.isSetupPhase() || phase == GamePhase.TRADE_BUILD) {
            toggleBuildOverlay();
        } else {
            showError("Tidak bisa membangun di fase ini.");
        }
        updateConditionLabel();
    }

    private void removeBuildOverlay() {
        if (buildOverlayPane != null && hexdesert != null) {
            Group parentMap = (Group) hexdesert.getParent();
            if (parentMap != null) {
                parentMap.getChildren().remove(buildOverlayPane);
            }
            buildOverlayPane = null;
        }
        currentMode = InteractionMode.NONE;
    }

    private void enterRoadBuildingCardMode() {
        removeBuildOverlay();
        currentMode = InteractionMode.ROAD_BUILDING_CARD;
        toggleBuildOverlay();
    }

    private void showRobberOverlay() {
        if (hexdesert == null) return;
        Group parentMap = (Group) hexdesert.getParent();
        if (parentMap == null) return;

        // Mirror toggleBuildOverlay: allow clicks to pass through sibling overlays
        StackPane hexmapBg = (StackPane) parentMap.getParent();
        if (hexmapBg != null) {
            for (javafx.scene.Node child : hexmapBg.getChildren()) {
                if (child instanceof VBox) {
                    child.setPickOnBounds(false);
                }
            }
        }
        if (tradeDialogOverlay != null) tradeDialogOverlay.setPickOnBounds(false);
        if (cardDialogOverlay != null) cardDialogOverlay.setPickOnBounds(false);
        if (stealDialogOverlay != null) stealDialogOverlay.setPickOnBounds(false);
        if (settingsDialogOverlay != null) settingsDialogOverlay.setPickOnBounds(false);
        if (victoryDialogOverlay != null) victoryDialogOverlay.setPickOnBounds(false);
        if (discardDialogOverlay != null) discardDialogOverlay.setPickOnBounds(false);

        removeRobberOverlay();

        HexTile currentRobberTile = (game != null && game.getRobber() != null)
                ? game.getRobber().getCurrentTile() : null;
        robberOverlayPane = robberOverlayView.createOverlay(visualToModelTile, currentRobberTile,
                tile -> {
                    currentMode = InteractionMode.NONE;
                    if (robberFlowController != null) robberFlowController.handleRobberClick(tile);
                });

        parentMap.getChildren().add(robberOverlayPane);
        robberOverlayPane.toFront();
    }

    private void removeRobberOverlay() {
        if (robberOverlayPane != null && hexdesert != null) {
            Group parentMap = (Group) hexdesert.getParent();
            if (parentMap != null) {
                parentMap.getChildren().remove(robberOverlayPane);
            }
            robberOverlayPane = null;
        }
    }

    private double[][] getHexCorners(StackPane sp) {
        return boardCoordinateMapper.getHexCorners(sp);
    }

    private void toggleBuildOverlay() {
        if (buildOverlayPane != null) {
            removeBuildOverlay();
            return;
        }
        boolean roadBuildingCardMode = currentMode == InteractionMode.ROAD_BUILDING_CARD
                && game != null
                && game.isRoadBuildingCardPlacementActive(game.getActivePlayer());

        Group parentMap = (Group) hexdesert.getParent();
        if (parentMap == null) return;

        StackPane hexmapBg = (StackPane) parentMap.getParent();
        if (hexmapBg != null) {
            for (javafx.scene.Node child : hexmapBg.getChildren()) {
                if (child instanceof VBox) {
                    child.setPickOnBounds(false);
                }
            }
        }
        if (tradeDialogOverlay != null) tradeDialogOverlay.setPickOnBounds(false);
        if (cardDialogOverlay != null) cardDialogOverlay.setPickOnBounds(false);
        if (stealDialogOverlay != null) stealDialogOverlay.setPickOnBounds(false);

        buildOverlayPane = new Pane();
        buildOverlayPane.setPickOnBounds(false);
        parentMap.getChildren().add(buildOverlayPane);
        currentMode = roadBuildingCardMode ? InteractionMode.ROAD_BUILDING_CARD : InteractionMode.BUILD_OVERLAY;

        // --- 1. AMBIL WARNA PLAYER AKTIF ---
        Color activePlayerColor = Color.BLACK;
        if (game != null && game.getActivePlayer() != null) {
            String hexStr = playerColorToHex(game.getActivePlayer().getColor());
            activePlayerColor = Color.web(hexStr);
        }
        final Color finalPlayerColor = activePlayerColor;

        // --- 2. SETUP ROAD MODE (Special Case) ---
        if (game != null && game.getCurrentPhase().isSetupPhase() && lastSetupSettlement != null) {
            for (banana.republic.board.Path path : lastSetupSettlement.getAdjacentPaths()) {
                if (path == null || path.hasRoad()) continue;

                banana.republic.board.Intersection other =
                    (path.getIntersectionA().getId() == lastSetupSettlement.getId()) ? path.getIntersectionB() : path.getIntersectionA();
                double[] coordsA = globalIntersectionCoords.get(lastSetupSettlement);
                double[] coordsB = globalIntersectionCoords.get(other);

                if (coordsA == null || coordsB == null) continue;

                final banana.republic.board.Path targetPath = path;
                javafx.scene.shape.Line rect = buildOverlayView.roadOption(
                        new IntersectionCoordinate(coordsA[0], coordsA[1]),
                        new IntersectionCoordinate(coordsB[0], coordsB[1]),
                        Color.rgb(200, 200, 200, 0.6),
                        Color.YELLOW,
                        () -> eksekusiBangunDariOverlay(null, targetPath, false));

                buildOverlayPane.getChildren().add(rect);
            }

            buildOverlayPane.toFront();
            updateConditionLabel();
            return;
        }

        // --- 3. ROAD BUILDING OVERLAY (TRADE_BUILD / Konstruksi Cepat) ---
        if (game != null && (game.getCurrentPhase() == GamePhase.TRADE_BUILD || roadBuildingCardMode)) {
            for (banana.republic.board.Path path : game.getBoard().getAllPaths()) {
                if (!path.isEmpty()) continue;
                if (!game.getBoard().isPathConnectedToPlayer(path, game.getActivePlayer())) continue;

                double[] cA = globalIntersectionCoords.get(path.getIntersectionA());
                double[] cB = globalIntersectionCoords.get(path.getIntersectionB());
                if (cA == null || cB == null) continue;

                final banana.republic.board.Path targetPath = path;
                javafx.scene.shape.Line roadOverlay = buildOverlayView.roadOption(
                        new IntersectionCoordinate(cA[0], cA[1]),
                        new IntersectionCoordinate(cB[0], cB[1]),
                        Color.rgb(255, 255, 255, 0.3),
                        finalPlayerColor.deriveColor(0, 1, 1, 0.5),
                        () -> eksekusiBangunDariOverlay(null, targetPath, false));

                buildOverlayPane.getChildren().add(roadOverlay);
            }
            if (roadBuildingCardMode) {
                buildOverlayPane.toFront();
                updateConditionLabel();
                return;
            }
        }

        // --- 4. INTERSECTION BUILDING OVERLAY (SETTLEMENT / CITY) ---
        for (Map.Entry<banana.republic.board.Intersection, double[]> entry : globalIntersectionCoords.entrySet()) {
            banana.republic.board.Intersection inter = entry.getKey();
            double[] coords = entry.getValue();
            double cx = coords[0];
            double cy = coords[1];

            boolean canBuildSettlement = false;
            boolean canUpgradeToCity = false;

            if (inter.getOwner() == null) {
                // Check distance rule and connection for settlement
                if (game.getBoard().isDistanceRuleValid(inter) && isVisualDistanceRuleValid(inter)) {
                    if (game.getCurrentPhase().isSetupPhase()) {
                        canBuildSettlement = true;
                    } else if (inter.getAdjacentPaths().stream().anyMatch(p -> p.hasRoad() && game.getActivePlayer().equals(p.getOwner()))) {
                        canBuildSettlement = true;
                    }
                }
            } else if (inter.getOwner().equals(game.getActivePlayer())) {
                if (inter.getBuilding().getBuildingType() == banana.republic.building.BuildingType.POS_PANTAU && game.getCurrentPhase() == GamePhase.TRADE_BUILD) {
                    canUpgradeToCity = true;
                }
            }

            if (!canBuildSettlement && !canUpgradeToCity) continue;

            // Harbor check for anchor icon
            boolean isHarbor = false;
            isHarbor = buildInteractionController.isNearHarbor(cx, cy, harborPoints);

            if (canUpgradeToCity) {
                buildOverlayPane.getChildren().add(buildOverlayView.laboratoryOption(
                        cx,
                        cy,
                        finalPlayerColor,
                        () -> eksekusiBangunDariOverlay(inter, null, true)));
            } else {
                buildOverlayPane.getChildren().add(buildOverlayView.settlementOption(
                        cx,
                        cy,
                        isHarbor,
                        gambarAnchorGlobal,
                        finalPlayerColor,
                        () -> eksekusiBangunDariOverlay(inter, null, false)));
            }
        }

        buildOverlayPane.toFront();
        updateConditionLabel();
    }

    private void eksekusiBangunDariOverlay(banana.republic.board.Intersection intersection, banana.republic.board.Path path, boolean isUpgrade) {
        try {
            Player activePlayer = game.getActivePlayer();
            if (currentMode == InteractionMode.ROAD_BUILDING_CARD && path != null) {
                game.buildRoadFromRoadBuildingCard(activePlayer, path);
                removeBuildOverlay();
                refreshAllUI();
                if (game.isRoadBuildingCardPlacementActive(activePlayer)) {
                    currentMode = InteractionMode.ROAD_BUILDING_CARD;
                    toggleBuildOverlay();
                }
                return;
            }
            Map<ResourceType, Integer> expectedInitialResources = intersection != null
                    && game.getCurrentPhase() == GamePhase.SETUP_SECOND_ROUND
                    ? buildInteractionController.visualAdjacentInitialResources(
                            intersection,
                            globalIntersectionCoords,
                            visualToModelTile,
                            boardCoordinateMapper)
                    : Map.of();

            BuildUiService.BuildResult result = buildUiService.execute(
                    game,
                    activePlayer,
                    intersection,
                    path,
                    isUpgrade,
                    expectedInitialResources);

            if (result.setupSettlementPlaced()) {
                lastSetupSettlement = intersection;
                removeBuildOverlay();
                refreshAllUI();
                toggleBuildOverlay();
                return;
            }
            if (result.setupRoadPlaced()) {
                lastSetupSettlement = null;
            }

            removeBuildOverlay();
            refreshAllUI();
            updatePhaseUI();
        } catch (IllegalStateException | IllegalArgumentException ex) {
            showError("Gagal membangun: " + ex.getMessage());
        }
    }

    private boolean isVisualDistanceRuleValid(banana.republic.board.Intersection candidate) {
        return buildInteractionController.isVisualDistanceRuleValidFromRaw(candidate, globalIntersectionCoords);
    }

    private void renderExistingBuildings() {
        ensureRobberImageLoaded();
        boardView.renderPermanentBuildLayer(
                game,
                permanentBuildLayer,
                globalIntersectionCoords,
                visualToModelTile,
                gambarNimonGlobal);
    }

    private void ensureRobberImageLoaded() {
        if (gambarNimonGlobal != null) return;
        try {
            URL nimonUrl = getClass().getResource("/icons/nimon_ungu.png");
            if (nimonUrl != null) gambarNimonGlobal = new Image(nimonUrl.toExternalForm());
        } catch (Exception ignored) {
            // Missing robber art should not block gameplay rendering.
        }
    }

    @FXML
    private void onEndTurn() {
        if (game == null || turnFlowController == null) return;
        try {
            turnFlowController.endTurn(
                    () -> {
                        removeBuildOverlay();
                        currentMode = InteractionMode.NONE;
                    },
                    () -> {
                        if (game.getActivePlayer() != null && game.getActivePlayer().isBot()) {
                            handleBotTurn();
                        } else {
                            showTransitionScreen();
                        }
                    });
        } catch (IllegalStateException | IllegalArgumentException e) {
            showError(e.getMessage());
        }
    }

    private void showTransitionScreen() {
        try {
            navigator.showTransition(game, () -> {
                try {
                    navigator.showGame(currentGame);
                } catch (IOException e) {
                    showError("Gagal kembali ke game: " + e.getMessage());
                }
            });
        } catch (IOException e) {
            showError("Gagal membuka layar transisi: " + e.getMessage());
        }
    }

    @FXML
    private void buyCard() {
        if (game == null) return;
        try {
            UiActionResult result = gameActionUiService.buyDevelopmentCard(game);
            refreshAllUI();
            if (result.isSuccess()) showInfo(result.getMessage());
            else showError(result.getMessage());
        } catch (IllegalStateException | IllegalArgumentException e) {
            showError(e.getMessage());
        }
    }

    @FXML
    private void onCard() {
        if (game == null) return;
        if (gameDialogFlowController != null) gameDialogFlowController.openCard();
    }

    @FXML
    private void toCard() { if (gameDialogFlowController != null) gameDialogFlowController.openCard(); }

    @FXML
    private void onDeclareVictory() {
        if (game == null) return;
        Player winner = gameActionUiService.checkVictory(game);
        if (winner != null) {
            try {
                navigator.showResult(game);
            } catch (IOException e) {
                showError("Gagal membuka layar hasil: " + e.getMessage());
            }
        } else {
            if (gameDialogFlowController != null) gameDialogFlowController.openVictory();
        }
        refreshAllUI();
    }

    @FXML
    private void onSettings() {
        if (game == null) return;
        if (gameDialogFlowController != null) gameDialogFlowController.openSettings();
    }

    @FXML
    private void toSettings() { if (gameDialogFlowController != null) gameDialogFlowController.openSettings(); }

    @FXML
    private void toTrade() { if (gameDialogFlowController != null) gameDialogFlowController.openTrade(); }

    private void refreshAllUI() {
        if (game != null) game.refreshLongestRoadStatus();
        updatePlayerPanel();
        updateResourceCards();
        updateLogbook();
        updateCurrentPlayer();
        updateBuildCostLabel();
        updateTimer(game.getTurnManager().getRemainingTimerSeconds());
        renderExistingBuildings();
        if (robberOverlayPane != null) robberOverlayPane.toFront();
        updatePhaseUI();
    }

    private void updateSetupOrderOverlay() {
        if (setupOrderController != null) {
            setupOrderController.render();
            setupOrderPending = setupOrderController.isPending();
        }
    }

    public void updatePlayerPanel() {
        if (playerPanelPresenter != null) playerPanelPresenter.renderPlayers(game);
    }

    private String playerColorToHex(PlayerColor color) {
        if (color == null) return "#888888";
        return switch (color) {
            case RED -> "#c21a09";
            case BLUE -> "#305cde";
            case ORANGE -> "#ff7f00";
            case GREEN -> "#4fc978";
            default -> "#888888";
        };
    }

    public void updateResourceCards() {
        if (playerPanelPresenter != null) playerPanelPresenter.renderResourceCards(game);
    }

    public void updateLogbook() {
        if (logbookPresenter != null) logbookPresenter.render(game);
    }

    private void updateBuildCostLabel() {
        if (buildCostPresenter != null) buildCostPresenter.render(game);
    }

    public void updateTimer(int remainingSeconds) {
        if (timerLabel == null) return;
        timerLabel.setText(TurnFlowController.formatTimer(remainingSeconds));
    }

    private void handleTradeBuildTimerTick(int remainingSeconds) {
        updateTimer(remainingSeconds);
        if (remainingSeconds > 0) {
            timerExpiryUiQueued = false;
            return;
        }
        if (timerExpiryUiQueued) return;
        timerExpiryUiQueued = true;
        javafx.application.Platform.runLater(() -> {
            try {
                refreshAllUI();
                updatePhaseUI();
                if (game != null && game.getActivePlayer() != null && game.getActivePlayer().isBot()) {
                    handleBotTurn();
                } else {
                    showTransitionScreen();
                }
            } finally {
                timerExpiryUiQueued = false;
            }
        });
    }

    public void updateCurrentPlayer() {
        if (playerPanelPresenter != null) playerPanelPresenter.renderCurrentPlayer(game, setupOrderPending);
    }

    public void showDiceResult(DiceResult result) {
        if (dicePresenter != null) dicePresenter.show(result);
    }

    private void updatePhaseUI() {
        if (phaseUiPresenter != null) {
            boolean discardPending = gameDialogFlowController != null && gameDialogFlowController.hasPendingDiscards();
            phaseUiPresenter.render(game, setupOrderPending, currentMode.name(), discardPending);
        }
    }

    private void enterRobberMode() {
        currentMode = InteractionMode.ROBBER;
        updatePhaseUI();
        showRobberOverlay();
    }

    public void pasangEventKlik(StackPane hexTile) {
        hexTile.setPickOnBounds(false);
        double w = hexTile.getPrefWidth();
        double h = hexTile.getPrefHeight();

        Polygon hitbox = new Polygon(
                w / 2, 0,
                w, h * 0.25,
                w, h * 0.75,
                w / 2, h,
                0, h * 0.75,
                0, h * 0.25
        );
        hitbox.setFill(Color.TRANSPARENT);
        hexTile.getChildren().add(hitbox);

        hitbox.setOnMouseClicked(event -> {
            if (game == null) return;
            if (currentMode == InteractionMode.ROBBER) {
                if (robberFlowController != null && robberFlowController.isCurrentRobberTile(hexTile)) return;
                if (robberFlowController != null) {
                    currentMode = InteractionMode.NONE;
                    robberFlowController.handleRobberClick(hexTile);
                }
            }
        });
    }

    private void setupAllTileClickHandlers() {
        if (hexdesert == null) return;
        Group parentMap = (Group) hexdesert.getParent();
        if (parentMap == null) return;

        for (javafx.scene.Node node : parentMap.getChildren()) {
            if (node instanceof StackPane sp) {
                String style = sp.getStyleClass().stream().filter(s -> s.startsWith("hex-tile-")).findFirst().orElse("");
                if (!style.isEmpty() && sp != hexdesert) pasangEventKlik(sp);
            }
        }
    }

    private void buildVisualToModelMapping() {
        if (game == null || hexdesert == null) return;
        visualToModelTile.clear();
        globalIntersectionCoords.clear();
        mainHexesOnly.clear();

        Group parentMap = (Group) hexdesert.getParent();
        if (parentMap == null) return;

        BoardCoordinateMapper.Mapping mapping = boardCoordinateMapper.map(game.getBoard(), parentMap);
        visualToModelTile.putAll(mapping.visualToModelTile());
        globalIntersectionCoords.putAll(mapping.intersectionCoordinates());
        mainHexesOnly.addAll(mapping.mainHexes());
        syncBackendHarborsWithVisualLabels();
    }

    public void pasangAnchorSudut(StackPane hexTile, int idx1, int idx2, String teksToko) {
        harborView.renderHarbor(hexTile, idx1, idx2, teksToko, gambarAnchorGlobal, harborPoints);
    }

    public void pasangAnchorSudut(StackPane hexTile, int idx1, int idx2, String teksToko, HarborType harborType) {
        harborView.renderHarbor(hexTile, idx1, idx2, teksToko, gambarAnchorGlobal, harborPoints);
        if (hexTile != null && harborType != null) {
            visualHarborSpecs.add(new VisualHarborSpec(hexTile, idx1, idx2, harborType));
        }
    }

    public void pasangTokoSisi(StackPane hexTile, int indeksSisi, String teksToko) {
        harborView.renderHarbor(hexTile, indeksSisi, (indeksSisi + 1) % 6, teksToko, gambarAnchorGlobal, harborPoints);
    }

    private void syncBackendHarborsWithVisualLabels() {
        if (game == null || visualHarborSpecs.isEmpty() || globalIntersectionCoords.isEmpty()) return;
        List<Harbor> harbors = new ArrayList<>();
        int harborId = 1;
        for (VisualHarborSpec spec : visualHarborSpecs) {
            double[][] corners = boardCoordinateMapper.getHexCorners(spec.hexTile());
            banana.republic.board.Intersection a = findClosestMappedIntersection(corners[spec.firstCorner()]);
            banana.republic.board.Intersection b = findClosestMappedIntersection(corners[spec.secondCorner()]);
            if (a == null || b == null || a.equals(b)) continue;
            harbors.add(new Harbor(harborId++, spec.harborType(), List.of(a, b)));
        }
        if (!harbors.isEmpty()) {
            game.getBoard().replaceHarbors(harbors);
        }
    }

    private banana.republic.board.Intersection findClosestMappedIntersection(double[] point) {
        banana.republic.board.Intersection closest = null;
        double minDistance = Double.MAX_VALUE;
        for (Map.Entry<banana.republic.board.Intersection, double[]> entry : globalIntersectionCoords.entrySet()) {
            double[] candidate = entry.getValue();
            double distance = Math.hypot(candidate[0] - point[0], candidate[1] - point[1]);
            if (distance < minDistance) {
                minDistance = distance;
                closest = entry.getKey();
            }
        }
        return minDistance < 36.0 ? closest : null;
    }

    private void handleBotTurn() {
        if (botTurnController != null) botTurnController.runIfBotActive(this::handleBotTurn);
    }

    private void showError(String message) {
        dialogs.showError(message);
    }

    private void showInfo(String message) {
        dialogs.showInfo(message);
    }

    @FXML private void toSteal() { if (gameDialogFlowController != null) gameDialogFlowController.openSteal(); }
    @FXML private void toVictory() { if (gameDialogFlowController != null) gameDialogFlowController.openVictory(); }
    @FXML private void toDiscard() { if (gameDialogFlowController != null) gameDialogFlowController.openDiscard(); }

    @FXML
    private void endGame() throws IOException {
        boolean confirmed = dialogs.confirm(
                "End Game",
                "Akhiri permainan?",
                "Game akan langsung ditutup dan masuk ke layar hasil.");
        if (!confirmed) return;

        gameActionUiService.endGame(game);
        navigator.showResult(game);
    }

    public void updateConditionLabel() {
        if (phaseUiPresenter != null) {
            phaseUiPresenter.renderCondition(game, setupOrderPending, currentMode.name());
        }
    }

    // responsive scaling helper: scale the parent map group to fit its container while preserving aspect
    private void setupResponsiveScaling(Group parentMap) {
        responsiveScaleBinder.bindToParent(parentMap);
    }

    public java.util.List<banana.republic.plugin.PluginLoadResult<banana.republic.card.ExperimentCard>>
    loadCardPlugin(String jarPath) {
        return pluginUiService.loadCardPlugin(game, jarPath);
    }

    public banana.republic.plugin.PluginLoadResult<banana.republic.card.ExperimentCard>
    loadCardPluginManual(String jarPath, String className) {
        return pluginUiService.loadCardPluginManual(game, jarPath, className);
    }

    public java.util.List<String> discoverCardClasses(String jarPath) {
        return pluginUiService.discoverCardClasses(jarPath);
    }
}
