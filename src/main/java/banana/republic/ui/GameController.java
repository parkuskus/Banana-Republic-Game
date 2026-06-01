package banana.republic.ui;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import banana.republic.App;
import banana.republic.board.Board;
import banana.republic.board.HexTile;
import banana.republic.core.Game;
import banana.republic.core.GamePhase;
import banana.republic.core.LogEntry;
import banana.republic.core.TurnOrder;
import banana.republic.dice.DiceResult;
import banana.republic.player.Player;
import banana.republic.player.PlayerColor;
import banana.republic.resource.ResourceType;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Shape;


public class GameController implements Initializable {

    @FXML private StackPane mainGameRoot;
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
    private GaussianBlur blurEffect = new GaussianBlur(10);
    private Image gambarAnchorGlobal = null;
    private Image gambarNimonGlobal = null;
    // Tambahkan di bagian atas bersama variabel state lainnya
    private banana.republic.board.Intersection lastSetupSettlement = null;

    private final java.util.Map<StackPane, HexTile> visualToModelTile = new java.util.HashMap<>();

    // LAYERING BEBAS CRISS-CROSS
    private Pane permanentBuildLayer;
    private Pane buildOverlayPane;
    private List<StackPane> mainHexesOnly = new ArrayList<>();

    // Menyimpan koordinat visual dari semua titik persimpangan
    private final Map<banana.republic.board.Intersection, double[]> globalIntersectionCoords = new java.util.HashMap<>();
    private final List<double[]> harborPoints = new ArrayList<>();

    private static Game currentGame;
    private boolean setupOrderPending = false;
    private List<Integer> setupOrderCandidates = new ArrayList<>();
    private Map<Integer, DiceResult> setupOrderRolls = new HashMap<>();
    private int setupOrderCursor = 0;

    private enum InteractionMode {
        NONE, SETTLEMENT, ROAD, CITY, ROBBER, BUILD_OVERLAY
    }
    private InteractionMode currentMode = InteractionMode.NONE;

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

        if (hexdesert != null) {
            harborPoints.clear();
            pasangEventKlik(hexdesert);
            pasangAnchorSudut(harborLU, 5, 0, "3:1");
            pasangAnchorSudut(harborRU1, 0, 1, "2:1 Banana");
            pasangAnchorSudut(harborRU2, 1, 2, "3:1");
            pasangAnchorSudut(harborRV, 2, 3, "2:1 Ore");
            pasangAnchorSudut(harborRD2, 2, 3, "3:1");
            pasangAnchorSudut(harborRD1, 3, 4, "2:1 Wheat");
            pasangAnchorSudut(harborLD, 3, 4, "2:1 Brick");
            pasangAnchorSudut(harborLV2, 4, 5, "3:1");
            pasangAnchorSudut(harborLV1, 5, 0, "2:1 Wood");
        }

        setupAllTileClickHandlers();
    }

    public void initialize(Game game) {
        this.game = game;
        currentGame = game;
        if (game == null) return;

        buildVisualToModelMapping();
        setupOrderPending = isSetupOrderPending();
        if (setupOrderPending) {
            resetSetupOrderCandidates();
        }

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
            rollCurrentSetupOrderPlayer();
            updateSetupOrderOverlay();
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
        if (game == null) return;
        try {
            DiceResult result = rollDiceAndSyncProduction();

            if (result.isSeven()) {
                game.processDiscardPhase();
                refreshAllUI();

                humanDiscardQueue.clear();
                for (Player p : game.getPlayers()) {
                    if (!p.isBot() && p.getTotalResourceCount() > Game.HAND_LIMIT) {
                        humanDiscardQueue.offer(p);
                    }
                }
                if (!humanDiscardQueue.isEmpty()) {
                    showNextDiscardDialog();
                }

                currentMode = InteractionMode.ROBBER;
                showInfo("Dadu 7! Nimon Ungu aktif. Pilih petak untuk memindahkan Nimon Ungu.");
            } else {
                game.startTradeBuildTimer(this::updateTimer);
            }
            updatePhaseUI();
        } catch (IllegalStateException | IllegalArgumentException e) {
            showError(e.getMessage());
        }
    }

    @FXML
    private void onSetDice() {
        if (game == null) return;
        
        if (game.getCurrentPhase() != GamePhase.RESOURCE_GATHERING) {
            showError("Dadu sudah dikocok (atau tidak bisa melempar dadu saat ini).");
            return;
        }
        
        javafx.scene.control.TextInputDialog dialog = new javafx.scene.control.TextInputDialog("7");
        dialog.setTitle("Set Dice Manual");
        dialog.setHeaderText("Masukkan total nilai dadu (2-12)");
        dialog.showAndWait().ifPresent(input -> {
            try {
                int total = Integer.parseInt(input.trim());
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
                if (result.isSeven()) {
                    game.processDiscardPhase();
                    refreshAllUI();

                    humanDiscardQueue.clear();
                    for (Player p : game.getPlayers()) {
                        if (!p.isBot() && p.getTotalResourceCount() > Game.HAND_LIMIT) {
                            humanDiscardQueue.offer(p);
                        }
                    }
                    if (!humanDiscardQueue.isEmpty()) {
                        showNextDiscardDialog();
                    }

                    currentMode = InteractionMode.ROBBER;
                    showInfo("Dadu 7! Nimon Ungu aktif.");
                } else {
                    game.startTradeBuildTimer(this::updateTimer);
                }
                updatePhaseUI();
            } catch (NumberFormatException e) {
                showError("Format angka tidak valid.");
            } catch (Exception e) {
                showError(e.getMessage());
            }
        });
    }

    private DiceResult rollDiceAndSyncProduction() {
        Map<Player, Map<ResourceType, Integer>> beforeRoll = snapshotPlayerResources();
        DiceResult result = game.rollDice();
        showDiceResult(result);

        if (!result.isSeven()) {
            String productionSummary = buildProductionSummary(beforeRoll);
            game.getGameLog().addEntry(
                    LogEntry.EventType.RESOURCE_PRODUCTION,
                    productionSummary
            );
        }

        refreshAllUI();
        return result;
    }

    private Map<Player, Map<ResourceType, Integer>> snapshotPlayerResources() {
        Map<Player, Map<ResourceType, Integer>> snapshot = new HashMap<>();
        if (game == null) return snapshot;

        for (Player player : game.getPlayers()) {
            Map<ResourceType, Integer> counts = new EnumMap<>(ResourceType.class);
            for (ResourceType type : ResourceType.values()) {
                counts.put(type, player.getResourceCount(type));
            }
            snapshot.put(player, counts);
        }
        return snapshot;
    }

    private String buildProductionSummary(Map<Player, Map<ResourceType, Integer>> beforeRoll) {
        List<String> producedByPlayer = new ArrayList<>();
        for (Player player : game.getPlayers()) {
            Map<ResourceType, Integer> before = beforeRoll.get(player);
            if (before == null) continue;

            List<String> gainedResources = new ArrayList<>();
            for (ResourceType type : ResourceType.values()) {
                int beforeCount = before.getOrDefault(type, 0);
                int gained = player.getResourceCount(type) - beforeCount;
                if (gained > 0) {
                    gainedResources.add(gained + " " + type.getDisplayName());
                }
            }

            if (!gainedResources.isEmpty()) {
                producedByPlayer.add(player.getName() + " mendapat " + String.join(", ", gainedResources));
            }
        }

        if (producedByPlayer.isEmpty()) {
            return "Tidak ada resource yang diproduksi dari hasil dadu ini.";
        }
        return "Produksi resource: " + String.join("; ", producedByPlayer) + ".";
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

    private double[][] getHexCorners(StackPane sp) {
        double w = sp.getPrefWidth() > 0 ? sp.getPrefWidth() : 94.0;
        double h = sp.getPrefHeight() > 0 ? sp.getPrefHeight() : 108.0;
        double x = sp.getLayoutX();
        double y = sp.getLayoutY();
        return new double[][] {
                {x + w / 2, y}, {x + w, y + h * 0.25}, {x + w, y + h * 0.75},
                {x + w / 2, y + h}, {x, y + h * 0.75}, {x, y + h * 0.25}
        };
    }

    private void toggleBuildOverlay() {
        if (buildOverlayPane != null) {
            removeBuildOverlay();
            return;
        }

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
        currentMode = InteractionMode.BUILD_OVERLAY;

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

                javafx.scene.shape.Line rect = createRoadLine(coordsA[0], coordsA[1], coordsB[0], coordsB[1], 10.0);
                rect.setStroke(Color.rgb(200, 200, 200, 0.6));
                rect.setStyle("-fx-cursor: hand;");

                DropShadow glow = new DropShadow(12, Color.YELLOW);
                rect.setOnMouseEntered(e -> {
                    rect.setEffect(glow);
                    rect.setStroke(Color.YELLOW);
                    rect.setStrokeWidth(14.0);
                });
                rect.setOnMouseExited(e -> {
                    rect.setEffect(null);
                    rect.setStroke(Color.rgb(200, 200, 200, 0.6));
                    rect.setStrokeWidth(10.0);
                });

                final banana.republic.board.Path targetPath = path;
                rect.setOnMouseClicked(e -> {
                    eksekusiBangunDariOverlay(null, targetPath, false);
                    e.consume();
                });

                buildOverlayPane.getChildren().add(rect);
            }

            buildOverlayPane.toFront();
            updateConditionLabel();
            return;
        }

        // --- 3. ROAD BUILDING OVERLAY (TRADE_BUILD) ---
        if (game != null && game.getCurrentPhase() == GamePhase.TRADE_BUILD) {
            for (banana.republic.board.Path path : game.getBoard().getAllPaths()) {
                if (!path.isEmpty()) continue;
                if (!game.getBoard().isPathConnectedToPlayer(path, game.getActivePlayer())) continue;

                double[] cA = globalIntersectionCoords.get(path.getIntersectionA());
                double[] cB = globalIntersectionCoords.get(path.getIntersectionB());
                if (cA == null || cB == null) continue;

                javafx.scene.shape.Line roadOverlay = createRoadLine(cA[0], cA[1], cB[0], cB[1], 10.0);
                roadOverlay.setStroke(Color.rgb(255, 255, 255, 0.3));
                roadOverlay.setStyle("-fx-cursor: hand;");

                DropShadow glow = new DropShadow(10, finalPlayerColor);
                roadOverlay.setOnMouseEntered(e -> {
                    roadOverlay.setEffect(glow);
                    roadOverlay.setStroke(finalPlayerColor.deriveColor(0, 1, 1, 0.5));
                    roadOverlay.setStrokeWidth(14.0);
                });
                roadOverlay.setOnMouseExited(e -> {
                    roadOverlay.setEffect(null);
                    roadOverlay.setStroke(Color.rgb(255, 255, 255, 0.3));
                    roadOverlay.setStrokeWidth(10.0);
                });

                final banana.republic.board.Path targetPath = path;
                roadOverlay.setOnMouseClicked(e -> {
                    eksekusiBangunDariOverlay(null, targetPath, false);
                    e.consume();
                });

                buildOverlayPane.getChildren().add(roadOverlay);
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
            for (double[] hp : harborPoints) {
                if (Math.hypot(hp[0] - cx, hp[1] - cy) < 20.0) {
                    isHarbor = true;
                    break;
                }
            }

            StackPane nodeWadah = new StackPane();
            nodeWadah.setLayoutX(cx - 18);
            nodeWadah.setLayoutY(cy - 18);
            nodeWadah.setPrefSize(36, 36);
            nodeWadah.setStyle("-fx-cursor: hand;");

            if (canUpgradeToCity) {
                javafx.scene.shape.Rectangle labRect = new javafx.scene.shape.Rectangle(24, 24);
                labRect.setFill(finalPlayerColor.deriveColor(0, 0.8, 1.2, 0.7));
                labRect.setStroke(Color.WHITE);
                labRect.setStrokeWidth(2);
                nodeWadah.getChildren().add(labRect);

                nodeWadah.setOnMouseEntered(e -> labRect.setEffect(new DropShadow(15, Color.CYAN)));
                nodeWadah.setOnMouseExited(e -> labRect.setEffect(null));
                nodeWadah.setOnMouseClicked(e -> {
                    eksekusiBangunDariOverlay(inter, null, true);
                    e.consume();
                });
            } else {
                Circle dot = new Circle(14);
                if (isHarbor) {
                    dot.setFill(Color.rgb(255, 255, 255, 0.4));
                    dot.setStroke(Color.rgb(40, 40, 40, 0.8));
                    dot.setStrokeWidth(2.5);
                } else {
                    dot.setFill(Color.rgb(255, 255, 255, 0.85));
                    dot.setStroke(Color.rgb(40, 40, 40, 0.8));
                    dot.setStrokeWidth(1.5);
                }
                nodeWadah.getChildren().add(dot);

                if (isHarbor && gambarAnchorGlobal != null) {
                    ImageView anchorView = new ImageView(gambarAnchorGlobal);
                    anchorView.setFitWidth(16);
                    anchorView.setFitHeight(16);
                    anchorView.setPreserveRatio(true);
                    nodeWadah.getChildren().add(anchorView);
                }

                nodeWadah.setOnMouseEntered(e -> dot.setEffect(new DropShadow(15, finalPlayerColor)));
                nodeWadah.setOnMouseExited(e -> dot.setEffect(null));
                nodeWadah.setOnMouseClicked(e -> {
                    eksekusiBangunDariOverlay(inter, null, false);
                    e.consume();
                });
            }

            buildOverlayPane.getChildren().add(nodeWadah);
        }

        buildOverlayPane.toFront();
        updateConditionLabel();
    }

    private void createBuildingOverlay(double cx, double cy, Pane intersectionLayer, Pane popupLayer,
                                       banana.republic.board.Intersection intersection, boolean isUpgrade) {
        Shape marker;
        if (isUpgrade) {
            // Bentuk KOTAK ORANYE untuk opsi Upgrade Laboratorium
            javafx.scene.shape.Rectangle rect = new javafx.scene.shape.Rectangle(cx - 12, cy - 12, 24, 24);
            rect.setFill(Color.rgb(255, 150, 0, 0.85));
            rect.setStroke(Color.rgb(40, 40, 40, 0.8));
            rect.setStrokeWidth(2.0);
            marker = rect;
        } else {
            // Bentuk BULAT PUTIH untuk opsi Pos Pantau
            Circle dot = new Circle(cx, cy, 14);
            dot.setFill(Color.rgb(255, 255, 255, 0.85));
            dot.setStroke(Color.rgb(40, 40, 40, 0.8));
            dot.setStrokeWidth(1.5);
            marker = dot;
        }
        marker.setStyle("-fx-cursor: hand;");

        // Popup dinamis
        VBox hoverPopup = new VBox();
        hoverPopup.setAlignment(javafx.geometry.Pos.CENTER);
        hoverPopup.setStyle("-fx-background-color: " + (isUpgrade ? "#FFC107;" : "#8FD8D8;") +
                "-fx-border-color: white; -fx-border-width: 3; -fx-background-radius: 8; -fx-border-radius: 8; -fx-padding: 5;");
        hoverPopup.setPrefSize(50, 50);
        Label lbl = new Label(isUpgrade ? "Build Lab" : "Build Pos");
        lbl.setStyle("-fx-font-weight: bold; -fx-text-fill: white; -fx-font-size: 10px;");
        hoverPopup.getChildren().add(lbl);
        hoverPopup.setPickOnBounds(false);

        hoverPopup.setLayoutX(cx - 25);
        hoverPopup.setLayoutY(cy - 60);
        hoverPopup.setVisible(false);
        popupLayer.getChildren().add(hoverPopup);

        DropShadow glow = new DropShadow(12, isUpgrade ? Color.CYAN : Color.YELLOW);

        marker.setOnMouseEntered(e -> {
            marker.setEffect(glow);
            marker.setFill(isUpgrade ? Color.CYAN : Color.YELLOW);
            hoverPopup.setVisible(true);
            hoverPopup.toFront();
        });

        marker.setOnMouseExited(e -> {
            marker.setEffect(null);
            marker.setFill(isUpgrade ? Color.rgb(255, 150, 0, 0.85) : Color.rgb(255, 255, 255, 0.85));
            hoverPopup.setVisible(false);
        });

        marker.setOnMouseClicked(e -> eksekusiBangunDariOverlay(intersection, null, isUpgrade));
        intersectionLayer.getChildren().add(marker);
    }

    private javafx.scene.shape.Line createRoadLine(double sx, double sy, double ex, double ey, double width) {
        javafx.scene.shape.Line line = new javafx.scene.shape.Line(sx, sy, ex, ey);
        line.setStrokeWidth(width);
        line.setStrokeLineCap(javafx.scene.shape.StrokeLineCap.ROUND);
        return line;
    }

    private void createPathLine(double sx, double sy, double ex, double ey, Pane pathLayer, banana.republic.board.Path path) {
        javafx.scene.shape.Line line = createRoadLine(sx, sy, ex, ey, 10.0);
        line.setStroke(Color.rgb(200, 200, 200, 0.6));
        line.setStyle("-fx-cursor: hand;");

        DropShadow glow = new DropShadow(12, Color.YELLOW);
        line.setOnMouseEntered(e -> {
            line.setEffect(glow);
            line.setStroke(Color.YELLOW);
            line.setStrokeWidth(14.0);
        });
        line.setOnMouseExited(e -> {
            line.setEffect(null);
            line.setStroke(Color.rgb(200, 200, 200, 0.6));
            line.setStrokeWidth(10.0);
        });

        line.setOnMouseClicked(e -> eksekusiBangunDariOverlay(null, path, false));
        pathLayer.getChildren().add(line);
    }

    private void createPathLine(double sx, double sy, double ex, double ey, Pane pathLayer) {
        javafx.scene.shape.Line line = createRoadLine(sx, sy, ex, ey, 10.0);
        line.setStroke(Color.rgb(200, 200, 200, 0.6));
        line.setStyle("-fx-cursor: hand;");

        Tooltip.install(line, new Tooltip("Bangun Pipa\n" + getRoadCostString()));

        DropShadow glow = new DropShadow(12, Color.YELLOW);
        line.setOnMouseEntered(e -> {
            line.setEffect(glow);
            line.setStroke(Color.YELLOW);
            line.setStrokeWidth(14.0);
        });
        line.setOnMouseExited(e -> {
            line.setEffect(null);
            line.setStroke(Color.rgb(200, 200, 200, 0.6));
            line.setStrokeWidth(10.0);
        });

        line.setOnMouseClicked(e -> buildFromVisualEdge(sx, sy, ex, ey));

        pathLayer.getChildren().add(line);
    }

    private banana.republic.board.Intersection findClosestBackendIntersection(double vx, double vy) {
        banana.republic.board.Intersection closest = null;
        double minDist = Double.MAX_VALUE;
        for (Map.Entry<banana.republic.board.Intersection, double[]> entry : globalIntersectionCoords.entrySet()) {
            double[] coords = entry.getValue();
            double dist = Math.hypot(coords[0] - vx, coords[1] - vy);
            if (dist < minDist) {
                minDist = dist;
                closest = entry.getKey();
            }
        }
        return minDist < 20.0 ? closest : null;
    }

    private void buildFromVisualCorner(double cx, double cy) {
        banana.republic.board.Intersection inter = findClosestBackendIntersection(cx, cy);
        if (inter != null) {
//            eksekusiBangunDariOverlay(inter, null);
        } else {
            showError("Lokasi ini tidak valid di backend sistem.");
        }
    }

    private void buildFromVisualEdge(double cx, double cy, double nx, double ny) {
        banana.republic.board.Intersection iA = findClosestBackendIntersection(cx, cy);
        banana.republic.board.Intersection iB = findClosestBackendIntersection(nx, ny);
        if (iA != null && iB != null) {
            banana.republic.board.Path path = cariPathAntara(iA, iB);
            if (path != null) {
                if (game != null && game.getCurrentPhase().isSetupPhase() && lastSetupSettlement != null) {
                    eksekusiBangunDariOverlay(null, path, false);
                }
                return;
            }
        }
        showError("Jalur pipa ini tidak valid atau tumpang tindih di backend.");
    }

    // --- Build overlay helpers (no popups/VBox allowed) ---
    private void createIntersectionCircle(double cx, double cy, Pane intersectionLayer, Pane popupLayer) {
        Circle dot = new Circle(cx, cy, 14);
        boolean isNearHarbor = harborPoints.stream().anyMatch(hp -> Math.hypot(hp[0] - cx, hp[1] - cy) < 20.0);

        if (isNearHarbor) {
            // Biarkan transparan jika dekat pelabuhan
            dot.setFill(Color.rgb(255, 255, 255, 0.01));
            dot.setStroke(Color.TRANSPARENT);
        } else {
            dot.setFill(Color.rgb(255, 255, 255, 0.85));
            dot.setStroke(Color.rgb(40, 40, 40, 0.8));
            dot.setStrokeWidth(1.5);
        }
        dot.setStyle("-fx-cursor: hand;");

        // Bikin UI Pop-up Kustom
        VBox hoverPopup = createHouseHoverPopup();
        // Posisikan pop-up di atas titik lingkaran
        hoverPopup.setLayoutX(cx - 25); // Sesuaikan offset X
        hoverPopup.setLayoutY(cy - 60); // Sesuaikan offset Y (melayang di atas)
        hoverPopup.setVisible(false); // Sembunyikan default
        popupLayer.getChildren().add(hoverPopup);

        DropShadow glow = new DropShadow(12, Color.YELLOW);

        dot.setOnMouseEntered(e -> {
            if (!isNearHarbor) {
                dot.setEffect(glow);
                dot.setFill(Color.YELLOW);
                // Munculkan popup kustom
                hoverPopup.setVisible(true);
                hoverPopup.toFront();
            }
        });

        dot.setOnMouseExited(e -> {
            if (!isNearHarbor) {
                dot.setEffect(null);
                dot.setFill(Color.rgb(255, 255, 255, 0.85));
                // Sembunyikan popup
                hoverPopup.setVisible(false);
            }
        });

        dot.setOnMouseClicked(e -> {
            if (!isNearHarbor) { // Hapus kondisi !isNearHarbor ini jika ternyata kamu mau Harbor bisa dibangun
                buildFromVisualCorner(cx, cy);
            }
        });

        intersectionLayer.getChildren().add(dot);
    }

    private VBox createHouseHoverPopup() {
        VBox container = new VBox();
        container.setAlignment(javafx.geometry.Pos.CENTER);

        // Styling kotak agar mirip screenshot (biru muda, border putih tebal, rounded)
        container.setStyle(
                "-fx-background-color: #8FD8D8;" + // Warna biru cyan terang
                        "-fx-border-color: white;" +
                        "-fx-border-width: 3;" +
                        "-fx-background-radius: 8;" +
                        "-fx-border-radius: 8;" +
                        "-fx-padding: 5;"
        );
        container.setPrefSize(50, 50);

        // Gambar rumah
        ImageView houseIcon = new ImageView();
        try {
            // GANTI PATH INI sesuai dengan nama file icon rumahmu
            URL url = getClass().getResource("/icons/house_red.png");
            if (url != null) {
                houseIcon.setImage(new Image(url.toExternalForm()));
            }
        } catch (Exception e) {
            System.out.println("Gagal load icon rumah: " + e.getMessage());
        }

        houseIcon.setFitWidth(30);
        houseIcon.setFitHeight(30);
        houseIcon.setPreserveRatio(true);

        container.getChildren().add(houseIcon);
        container.setPickOnBounds(false); // Agar tidak memblokir klik mouse ke lingkaran di bawahnya

        return container;
    }



    private String getSettlementCostString() {
        // Hard-coded human readable cost (no Cost model available in project)
        return "Biaya: 1 Wood, 1 Brick, 1 Wheat, 1 Banana";
    }

    private String getRoadCostString() {
        return "Biaya: 1 Wood, 1 Brick";
    }

    private void eksekusiBangunDariOverlay(banana.republic.board.Intersection intersection, banana.republic.board.Path path, boolean isUpgrade) {
        try {
            Player activePlayer = game.getActivePlayer();

            if (intersection != null) {
                if (game.getCurrentPhase().isSetupPhase()) {
                    boolean shouldGrantInitialResources = game.getCurrentPhase() == GamePhase.SETUP_SECOND_ROUND;
                    Map<ResourceType, Integer> beforeResources = snapshotPlayerResourceCounts(activePlayer);
                    Map<ResourceType, Integer> expectedInitialResources = shouldGrantInitialResources
                            ? getVisualAdjacentInitialResources(intersection)
                            : Map.of();
                    game.placeInitialSettlement(activePlayer, intersection);
                    if (shouldGrantInitialResources) {
                        syncInitialResourcesWithVisualBoard(activePlayer, beforeResources, expectedInitialResources);
                    }
                    lastSetupSettlement = intersection;
                    removeBuildOverlay();
                    refreshAllUI();
                    toggleBuildOverlay(); // Buka otomatis untuk pilih jalan setelah taruh Pos!
                    return; // Berhenti di sini, biarkan UI jalan terbuka
                } else {
                    if (isUpgrade) game.buildCity(activePlayer, intersection);
                    else game.buildSettlement(activePlayer, intersection);
                }
            }
            else if (path != null) {
                if (game.getCurrentPhase().isSetupPhase()) {
                    game.placeInitialRoad(activePlayer, path);
                    lastSetupSettlement = null; // Reset untuk giliran pemain selanjutnya
                } else {
                    game.buildRoad(activePlayer, path);
                }
            }

            removeBuildOverlay();
            refreshAllUI();
            updatePhaseUI();
        } catch (IllegalStateException | IllegalArgumentException ex) {
            showError("Gagal membangun: " + ex.getMessage());
        }
    }

    private boolean isVisualDistanceRuleValid(banana.republic.board.Intersection candidate) {
        double[] candidateCoords = globalIntersectionCoords.get(candidate);
        if (candidateCoords == null) return true;

        for (Map.Entry<banana.republic.board.Intersection, double[]> entry : globalIntersectionCoords.entrySet()) {
            banana.republic.board.Intersection other = entry.getKey();
            if (other == candidate || !other.hasBuilding()) continue;
            double[] otherCoords = entry.getValue();
            double distance = Math.hypot(candidateCoords[0] - otherCoords[0], candidateCoords[1] - otherCoords[1]);
            if (distance < 60.0) {
                return false;
            }
        }
        return true;
    }

    private Map<ResourceType, Integer> snapshotPlayerResourceCounts(Player player) {
        Map<ResourceType, Integer> counts = new EnumMap<>(ResourceType.class);
        if (player == null) return counts;
        for (ResourceType type : ResourceType.values()) {
            counts.put(type, player.getResourceCount(type));
        }
        return counts;
    }

    private Map<ResourceType, Integer> getVisualAdjacentInitialResources(banana.republic.board.Intersection intersection) {
        Map<ResourceType, Integer> resources = new EnumMap<>(ResourceType.class);
        double[] coords = globalIntersectionCoords.get(intersection);
        if (coords == null) return resources;

        for (Map.Entry<StackPane, HexTile> entry : visualToModelTile.entrySet()) {
            StackPane visualTile = entry.getKey();
            HexTile modelTile = entry.getValue();
            if (modelTile == null || !modelTile.canProduce()) continue;

            double[][] corners = getHexCorners(visualTile);
            for (double[] corner : corners) {
                if (Math.hypot(corner[0] - coords[0], corner[1] - coords[1]) < 28.0) {
                    ResourceType type = modelTile.getResourceType();
                    if (type != null) {
                        resources.merge(type, 1, Integer::sum);
                    }
                    break;
                }
            }
        }
        return resources;
    }

    private void syncInitialResourcesWithVisualBoard(Player player,
                                                     Map<ResourceType, Integer> beforeResources,
                                                     Map<ResourceType, Integer> expectedResources) {
        if (player == null || expectedResources == null) return;

        List<String> gainedText = new ArrayList<>();
        for (ResourceType type : ResourceType.values()) {
            int before = beforeResources.getOrDefault(type, 0);
            int actualGain = player.getResourceCount(type) - before;
            int expectedGain = expectedResources.getOrDefault(type, 0);

            if (actualGain > expectedGain) {
                int extra = actualGain - expectedGain;
                player.removeResource(type, extra);
                game.getBank().returnResource(type, extra);
            } else if (expectedGain > actualGain) {
                int missing = expectedGain - actualGain;
                int available = Math.min(missing, game.getBank().getCount(type));
                if (available > 0) {
                    game.getBank().takeResource(type, available);
                    player.addResource(type, available);
                }
            }

            int finalGain = player.getResourceCount(type) - before;
            if (finalGain > 0) {
                gainedText.add(finalGain + " " + type.getDisplayName());
            }
        }

        if (!gainedText.isEmpty()) {
            game.getGameLog().addEntry(
                    LogEntry.EventType.RESOURCE_PRODUCTION,
                    player.getName(),
                    "Resource awal dari Pos Pantau kedua: " + String.join(", ", gainedText) + "."
            );
        }
    }

    private void renderExistingBuildings() {
        if (permanentBuildLayer == null || game == null) return;
        permanentBuildLayer.getChildren().clear();

        Set<banana.republic.board.Path> allPaths = new java.util.HashSet<>();
        for (banana.republic.board.Intersection inter : globalIntersectionCoords.keySet()) {
            if (inter.getAdjacentPaths() != null) allPaths.addAll(inter.getAdjacentPaths());
        }

        for (banana.republic.board.Path path : allPaths) {
            if (path.getOwner() != null) {
                double[] cA = globalIntersectionCoords.get(path.getIntersectionA());
                double[] cB = globalIntersectionCoords.get(path.getIntersectionB());
                if (cA != null && cB != null) {
                    javafx.scene.shape.Line builtRoad = createRoadLine(cA[0], cA[1], cB[0], cB[1], 12.0);
                    builtRoad.setStroke(Color.web(playerColorToHex(path.getOwner().getColor())));
                    permanentBuildLayer.getChildren().add(builtRoad);
                }
            }
        }

        for (Map.Entry<banana.republic.board.Intersection, double[]> entry : globalIntersectionCoords.entrySet()) {
            banana.republic.board.Intersection inter = entry.getKey();
            if (inter.getOwner() != null) {
                double[] coords = entry.getValue();
                Color pColor = Color.web(playerColorToHex(inter.getOwner().getColor()));

                // Pembedaan Visual Lab vs Pos Pantau di Papan Permainan
                if (inter.getBuilding().getBuildingType() == banana.republic.building.BuildingType.LABORATORIUM) {
                    javafx.scene.shape.Rectangle builtLab = new javafx.scene.shape.Rectangle(coords[0] - 14, coords[1] - 14, 28, 28);
                    builtLab.setFill(pColor);
                    builtLab.setStroke(Color.BLACK);
                    builtLab.setStrokeWidth(2.5);
                    permanentBuildLayer.getChildren().add(builtLab);
                } else {
                    Circle builtSettlement = new Circle(coords[0], coords[1], 16, pColor);
                    builtSettlement.setStroke(Color.BLACK);
                    builtSettlement.setStrokeWidth(2);
                    permanentBuildLayer.getChildren().add(builtSettlement);
                }
            }
        }
        
        // Render Nimon Ungu (Robber)
        if (game.getRobber() != null && game.getRobber().getCurrentTile() != null) {
            HexTile robberTile = game.getRobber().getCurrentTile();
            StackPane sp = modelToVisualTileFallback(robberTile);
            if (sp != null) {
                if (gambarNimonGlobal == null) {
                    try {
                        URL nimonUrl = getClass().getResource("/icons/nimon_ungu.png");
                        if (nimonUrl != null) gambarNimonGlobal = new Image(nimonUrl.toExternalForm());
                    } catch (Exception e) {}
                }
                if (gambarNimonGlobal != null) {
                    ImageView robberView = new ImageView(gambarNimonGlobal);
                    robberView.setFitWidth(36);
                    robberView.setFitHeight(36);
                    robberView.setPreserveRatio(true);
                    
                    double w = sp.getPrefWidth() > 0 ? sp.getPrefWidth() : 94.0;
                    double h = sp.getPrefHeight() > 0 ? sp.getPrefHeight() : 108.0;
                    double rx = sp.getLayoutX() + (w / 2) - 18;
                    double ry = sp.getLayoutY() + (h / 2) - 18;
                    
                    robberView.setLayoutX(rx);
                    robberView.setLayoutY(ry);
                    
                    // Efek shadow agar Nimon terlihat melayang/menonjol
                    robberView.setEffect(new DropShadow(10, Color.PURPLE));
                    
                    permanentBuildLayer.getChildren().add(robberView);
                }
            }
        }
        
        permanentBuildLayer.toFront();
    }
    
    private StackPane modelToVisualTileFallback(HexTile tile) {
        for (Map.Entry<StackPane, HexTile> entry : visualToModelTile.entrySet()) {
            if (entry.getValue().equals(tile)) return entry.getKey();
        }
        return null;
    }

    @FXML
    private void onEndTurn() {
        if (game == null) return;
        try {
            removeBuildOverlay();
            game.endTurn();
            currentMode = InteractionMode.NONE;
            refreshAllUI();
            updatePhaseUI();

            if (game.getActivePlayer() != null && game.getActivePlayer().isBot()) {
                handleBotTurn();
            } else {
                showTransitionScreen();
            }
        } catch (IllegalStateException | IllegalArgumentException e) {
            showError(e.getMessage());
        }
    }

    private void showTransitionScreen() {
        try {
            FXMLLoader loader = App.getLoader("transition");
            Parent root = loader.load();
            TransitionScreenController controller = loader.getController();
            controller.setGame(game);
            controller.setStartTurnHandler(() -> {
                try {
                    FXMLLoader gameLoader = App.getLoader("game");
                    Parent gameRoot = gameLoader.load();
                    GameController gameCtrl = gameLoader.getController();
                    gameCtrl.setGame(currentGame);
                    App.setRootFromLoader(gameRoot);
                } catch (IOException e) {
                    showError("Gagal kembali ke game: " + e.getMessage());
                }
            });
            App.setRootFromLoader(root);
        } catch (IOException e) {
            showError("Gagal membuka layar transisi: " + e.getMessage());
        }
    }

    @FXML
    private void buyCard() {
        if (game == null) return;
        try {
            game.buyDevelopmentCard(game.getActivePlayer());
            refreshAllUI();
            showInfo("Berhasil membeli Kartu Temuan.");
        } catch (IllegalStateException | IllegalArgumentException e) {
            showError(e.getMessage());
        }
    }

    @FXML
    private void onCard() {
        if (game == null) return;
        try { openDialog("card", cardDialogOverlay); } catch (IOException e) {}
    }

    @FXML
    private void toCard() throws IOException { openDialog("card", cardDialogOverlay); }

    @FXML
    private void onDeclareVictory() {
        if (game == null) return;
        Player winner = game.checkVictory();
        if (winner != null) {
            try {
                FXMLLoader loader = App.getLoader("result");
                Parent root = loader.load();
                GameResultController controller = loader.getController();
                controller.setGame(game);
                App.setRootFromLoader(root);
            } catch (IOException e) {
                showError("Gagal membuka layar hasil: " + e.getMessage());
            }
        } else {
            try { openDialog("victory", victoryDialogOverlay); } catch (IOException e) {}
        }
        refreshAllUI();
    }

    @FXML
    private void onSettings() {
        if (game == null) return;
        try { openDialog("settings", settingsDialogOverlay); } catch (IOException e) {}
    }

    @FXML
    private void toSettings() throws IOException { openDialog("settings", settingsDialogOverlay); }

    @FXML
    private void toTrade() throws IOException { openDialog("trade", tradeDialogOverlay); }

    private void refreshAllUI() {
        updatePlayerPanel();
        updateResourceCards();
        updateLogbook();
        updateCurrentPlayer();
        updateBuildCostLabel();
        updateTimer(game.getTurnManager().getRemainingTimerSeconds());
        renderExistingBuildings();
    }

    private boolean isSetupOrderPending() {
        if (game == null || game.getCurrentPhase() != GamePhase.SETUP_FIRST_ROUND || game.getSetupSettlementCount() != 0) {
            return false;
        }
        return game.getGameLog().getEntries().stream()
                .map(LogEntry::getMessage)
                .noneMatch(message -> message != null && message.contains("Fase Setup dimulai"));
    }

    private void updateSetupOrderOverlay() {
        if (setupOrderOverlay == null) return;
        setupOrderOverlay.setVisible(setupOrderPending);
        setupOrderOverlay.setManaged(setupOrderPending);
        if (setupOrderPending) {
            setupOrderOverlay.toFront();
            updateSetupOrderPlayersLabel();
        }
    }

    private void updateSetupOrderPlayersLabel() {
        if (setupOrderPlayersBox == null || game == null) return;
        setupOrderPlayersBox.getChildren().clear();

        int currentIndex = getCurrentSetupOrderPlayerIndex();
        for (int i = 0; i < game.getPlayers().size(); i++) {
            Player player = game.getPlayers().get(i);
            DiceResult roll = setupOrderRolls.get(i);
            String resultText = roll == null ? "belum roll" : roll.getDie1() + " + " + roll.getDie2() + " = " + roll.getTotal();
            Label row = new Label((i + 1) + ". " + player.getName() + " - " +
                    formatPlayerColorName(player.getColor()) + " | " + resultText);
            row.setMaxWidth(Double.MAX_VALUE);
            row.setWrapText(true);
            row.setStyle("-fx-font-size: 14px; -fx-text-fill: #444; -fx-padding: 4 6;");
            if (i == currentIndex) {
                row.setStyle(row.getStyle() + "-fx-font-weight: bold; -fx-background-color: #fff3cd; -fx-background-radius: 6;");
            }
            setupOrderPlayersBox.getChildren().add(row);
        }

        if (setupOrderStatusLabel != null) {
            int idx = getCurrentSetupOrderPlayerIndex();
            if (idx >= 0) {
                setupOrderStatusLabel.setText("Giliran roll: " + game.getPlayers().get(idx).getName());
            } else {
                setupOrderStatusLabel.setText("Menentukan pemenang roll...");
            }
        }

        if (btnStartSetupOrder != null) {
            int idx = getCurrentSetupOrderPlayerIndex();
            btnStartSetupOrder.setText(idx >= 0 ? "Roll untuk " + game.getPlayers().get(idx).getName() : "Lanjut");
        }
    }

    private void resetSetupOrderCandidates() {
        setupOrderCandidates = new ArrayList<>();
        for (int i = 0; i < game.getPlayers().size(); i++) {
            setupOrderCandidates.add(i);
        }
        setupOrderRolls = new HashMap<>();
        setupOrderCursor = 0;
    }

    private int getCurrentSetupOrderPlayerIndex() {
        if (!setupOrderPending || setupOrderCandidates.isEmpty() || setupOrderCursor >= setupOrderCandidates.size()) {
            return -1;
        }
        return setupOrderCandidates.get(setupOrderCursor);
    }

    private void rollCurrentSetupOrderPlayer() {
        if (setupOrderCandidates.isEmpty()) {
            resetSetupOrderCandidates();
        }

        int playerIndex = getCurrentSetupOrderPlayerIndex();
        if (playerIndex < 0) return;

        Player player = game.getPlayers().get(playerIndex);
        DiceResult result = game.getDice().roll();
        setupOrderRolls.put(playerIndex, result);
        showDiceResult(result);
        game.getGameLog().addEntry(
                LogEntry.EventType.SYSTEM,
                player.getName(),
                player.getName() + " melempar urutan: " + result.getDie1() +
                        " + " + result.getDie2() + " = " + result.getTotal()
        );

        setupOrderCursor++;
        if (setupOrderCursor >= setupOrderCandidates.size()) {
            resolveSetupOrderRound();
        }
    }

    private void resolveSetupOrderRound() {
        int highest = setupOrderCandidates.stream()
                .map(setupOrderRolls::get)
                .filter(result -> result != null)
                .mapToInt(DiceResult::getTotal)
                .max()
                .orElse(0);

        List<Integer> winners = setupOrderCandidates.stream()
                .filter(index -> setupOrderRolls.get(index) != null && setupOrderRolls.get(index).getTotal() == highest)
                .toList();

        if (winners.size() == 1) {
            int winnerIndex = winners.get(0);
            Player winner = game.getPlayers().get(winnerIndex);
            game.getTurnManager().setActiveIndex(winnerIndex);
            game.getTurnManager().setOrder(TurnOrder.CLOCKWISE);
            game.getGameLog().addEntry(LogEntry.EventType.SYSTEM,
                    "Fase Setup dimulai. Pemain menentukan urutan dengan dadu.");
            game.getGameLog().addEntry(
                    LogEntry.EventType.TURN_CHANGE,
                    winner.getName(),
                    winner.getName() + " memulai pertama (dadu tertinggi: " + highest + ")"
            );
            setupOrderPending = false;
            if (setupOrderStatusLabel != null) {
                setupOrderStatusLabel.setText(winner.getName() + " memulai pertama.");
            }
            return;
        }

        String names = String.join(", ", winners.stream()
                .map(index -> game.getPlayers().get(index).getName())
                .toList());
        game.getGameLog().addEntry(LogEntry.EventType.SYSTEM,
                "Seri urutan pemain: " + names + ". Roll ulang untuk pemain yang seri.");
        setupOrderCandidates = new ArrayList<>(winners);
        setupOrderRolls = new HashMap<>();
        setupOrderCursor = 0;
    }

    private String formatPlayerColorName(PlayerColor color) {
        if (color == null) return "Unknown";
        return switch (color) {
            case RED -> "Red";
            case BLUE -> "Blue";
            case GREEN -> "Green";
            case ORANGE -> "Orange";
            default -> color.name();
        };
    }

    public void updatePlayerPanel() {
        if (game == null) return;
        List<Player> players = game.getPlayers();
        for (int i = 0; i < playerPanels.size(); i++) {
            HBox panel = playerPanels.get(i);
            if (i < players.size()) {
                panel.setVisible(true);
                panel.setManaged(true);
                renderPlayerPanel(panel, players.get(i), i + 1);
            } else {
                panel.setVisible(false);
                panel.setManaged(false);
            }
        }
    }

    private void renderPlayerPanel(HBox panel, Player player, int rank) {
        VBox content = (VBox) panel.getChildren().get(1);
        HBox nameRow = (HBox) content.getChildren().get(0);
        HBox supplyRow = (HBox) content.getChildren().get(1);
        HBox resourceRow = (HBox) content.getChildren().get(2);

        content.getStyleClass().removeAll("side-card-red", "side-card-blue", "side-card-green", "side-card-orange");
        content.getStyleClass().add(getSideCardStyleClass(player.getColor()));

        StackPane numberPane = (StackPane) nameRow.getChildren().get(0);
        Circle circle = (Circle) numberPane.getChildren().get(0);
        Label numberLabel = (Label) numberPane.getChildren().get(1);
        numberLabel.setText(String.valueOf(rank));

        Label nameLabel = (Label) nameRow.getChildren().get(1);
        nameLabel.setText(player.getName());

        Label vpLabel = (Label) nameRow.getChildren().get(3);
        int vp = game.getVPTotal(player);
        vpLabel.setText(vp + " VP");

        String colorHex = playerColorToHex(player.getColor());
        circle.setFill(Color.web(colorHex));
        vpLabel.setStyle("-fx-background-color: " + colorHex + "; -fx-padding: 4 8; -fx-background-radius: 4; -fx-font-weight: bold; -fx-text-fill: white; -fx-font-size: 1.1em;");

        Region leftBar = (Region) panel.getChildren().get(0);
        leftBar.setStyle("-fx-background-color: " + colorHex + "; -fx-background-radius: 8 0 0 8;");

        var supply = game.getSupply(player);
        int roads = supply != null ? supply.getRoadsRemaining() : 0;
        int posts = supply != null ? supply.getPosPantauRemaining() : 0;
        int labs = supply != null ? supply.getLaboratoriumRemaining() : 0;

        Label pipeLabel = (Label) supplyRow.getChildren().get(0);
        Label postLabel = (Label) supplyRow.getChildren().get(1);
        Label labLabel = (Label) supplyRow.getChildren().get(2);
        pipeLabel.setText("PIPE: " + roads);
        postLabel.setText("POST: " + posts);
        labLabel.setText("LAB: " + labs);

        Label resLabel = (Label) resourceRow.getChildren().get(0);
        Label cardLabel = (Label) resourceRow.getChildren().get(1);
        resLabel.setText("Resources: " + player.getTotalResourceCount());
        cardLabel.setText("Dev Cards: " + player.getHandCards().size());
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

    private String getSideCardStyleClass(PlayerColor color) {
        if (color == null) return "side-card-white";
        return switch (color) {
            case RED -> "side-card-red";
            case BLUE -> "side-card-blue";
            case GREEN -> "side-card-green";
            case ORANGE -> "side-card-orange";
            default -> "side-card-white";
        };
    }

    private String getSideCardBackgroundHex(PlayerColor color) {
        if (color == null) return "#e8f3ec";
        return switch (color) {
            case RED -> "#ff7e70";
            case BLUE -> "#84a7ff";
            case GREEN -> "#8cffb2";
            case ORANGE -> "#f9a85c";
            default -> "#e8f3ec";
        };
    }

    public void updateResourceCards() {
        if (game == null) return;
        Player active = game.getActivePlayer();
        if (active == null) return;
        lblWoodCount.setText(String.valueOf(active.getResourceCount(ResourceType.WOOD)));
        lblBrickCount.setText(String.valueOf(active.getResourceCount(ResourceType.BRICK)));
        lblWheatCount.setText(String.valueOf(active.getResourceCount(ResourceType.WHEAT)));
        lblOreCount.setText(String.valueOf(active.getResourceCount(ResourceType.ORE)));
        lblBananaCount.setText(String.valueOf(active.getResourceCount(ResourceType.BANANA)));
    }

    public void updateLogbook() {
        if (game == null) return;
        List<LogEntry> entries = game.getGameLog().getRecentEntries(logLabels.size());
        for (int i = 0; i < logLabels.size(); i++) {
            if (i < entries.size()) {
                logLabels.get(i).setText("• " + entries.get(entries.size() - 1 - i).getMessage());
            } else {
                logLabels.get(i).setText("");
            }
        }
    }

    private void updateBuildCostLabel() {
        if (buildCostLeftLabel == null || buildCostRightLabel == null) return;
        if (game == null || game.getActivePlayer() == null) {
            buildCostLeftLabel.setText("Tidak ada game aktif.");
            buildCostRightLabel.setText("");
            return;
        }

        Player active = game.getActivePlayer();
        buildCostLeftLabel.setText(
                costLine("Pos Pantau", active, Map.of(
                        ResourceType.WOOD, 1,
                        ResourceType.BRICK, 1,
                        ResourceType.WHEAT, 1,
                        ResourceType.BANANA, 1
                )) + "\n" +
                costLine("Pipa", active, Map.of(
                        ResourceType.WOOD, 1,
                        ResourceType.BRICK, 1
                ))
        );
        buildCostRightLabel.setText(
                costLine("Laboratorium", active, Map.of(
                        ResourceType.WHEAT, 2,
                        ResourceType.ORE, 3
                )) + "\n" +
                costLine("Kartu Temuan", active, Map.of(
                        ResourceType.ORE, 1,
                        ResourceType.WHEAT, 1,
                        ResourceType.BANANA, 1
                ))
        );
    }

    private String costLine(String label, Player player, Map<ResourceType, Integer> cost) {
        return label + ": " + formatResourceCost(cost) + " (" + (canPay(player, cost) ? "Bisa" : "Kurang") + ")";
    }

    private String formatResourceCost(Map<ResourceType, Integer> cost) {
        List<String> parts = new ArrayList<>();
        for (ResourceType type : ResourceType.values()) {
            int amount = cost.getOrDefault(type, 0);
            if (amount > 0) {
                parts.add(amount + " " + type.getDisplayName());
            }
        }
        return String.join(", ", parts);
    }

    private boolean canPay(Player player, Map<ResourceType, Integer> cost) {
        if (player == null) return false;
        for (Map.Entry<ResourceType, Integer> entry : cost.entrySet()) {
            if (!player.hasResource(entry.getKey(), entry.getValue())) {
                return false;
            }
        }
        return true;
    }

    public void updateTimer(int remainingSeconds) {
        if (timerLabel == null) return;
        int minutes = remainingSeconds / 60;
        int seconds = remainingSeconds % 60;
        timerLabel.setText(String.format("%02d:%02d", minutes, seconds));
    }

    public void updateCurrentPlayer() {
        if (game == null || currentPlayerLabel == null) return;
        if (setupOrderPending) {
            currentPlayerLabel.setText("Roll Order");
            currentPlayerLabel.setStyle("-fx-background-color: #fff3cd;");
            return;
        }
        Player active = game.getActivePlayer();
        if (active != null) {
            currentPlayerLabel.setText(active.getName() + "'s Turn");
            currentPlayerLabel.setStyle("-fx-background-color: " + getSideCardBackgroundHex(active.getColor()) + ";");
        }
    }

    public void showDiceResult(DiceResult result) {
        if (diceImage1 == null || diceImage2 == null) return;
        int d1 = result.getDie1();
        int d2 = result.getDie2();
        if (d1 >= 1 && d1 <= 6 && diceImages[d1 - 1] != null) {
            diceImage1.setImage(diceImages[d1 - 1]);
        }
        if (d2 >= 1 && d2 <= 6 && diceImages[d2 - 1] != null) {
            diceImage2.setImage(diceImages[d2 - 1]);
        }
    }

    private void updatePhaseUI() {
        if (game == null) return;
        GamePhase phase = game.getCurrentPhase();

        boolean isSetup = phase.isSetupPhase();
        boolean isGathering = (phase == GamePhase.RESOURCE_GATHERING);
        boolean isGameOver = (phase == GamePhase.GAME_OVER);

        btnRollDice.setDisable(setupOrderPending || isSetup || !isGathering || isGameOver);
        btnSetDice.setDisable(setupOrderPending || isSetup || isGameOver);
        btnBuild.setDisable(setupOrderPending || isGathering || isGameOver);
        btnTrade.setDisable(setupOrderPending || isSetup || isGathering || isGameOver);
        btnCard.setDisable(setupOrderPending || isSetup || isGathering || isGameOver);
        btnDeclareVictory.setDisable(setupOrderPending || isSetup || isGameOver);
        btnEndTurn.setDisable(setupOrderPending || isSetup || isGathering || isGameOver);
        btnSettings.setDisable(isGameOver);
        if (btnStartSetupOrder != null) btnStartSetupOrder.setDisable(!setupOrderPending);

        if (btnSteal != null) btnSteal.setDisable(currentMode != InteractionMode.ROBBER);
        if (btnDiscard != null) btnDiscard.setDisable(humanDiscardQueue.isEmpty());
        if (btnEndGame != null) btnEndGame.setDisable(isGameOver);

        updateConditionLabel();
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
            HexTile modelTile = visualToModelTile.get(hexTile);
            if (modelTile == null) modelTile = findHexTileByVisualFallback(hexTile);
            if (modelTile == null) return;
            if (currentMode == InteractionMode.ROBBER) {
                handleRobberClick(hexTile);
            }
        });
    }

    private banana.republic.board.Path cariPathAntara(banana.republic.board.Intersection a, banana.republic.board.Intersection b) {
        if (a == null || b == null) return null;
        if (a.getAdjacentPaths() == null) return null;
        for (banana.republic.board.Path path : a.getAdjacentPaths()) {
            if (path == null) continue;
            if (path.getIntersectionA().getId() == b.getId() || path.getIntersectionB().getId() == b.getId()) {
                return path;
            }
        }
        return null;
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

    private static final double SQRT_3 = Math.sqrt(3.0);
    private static final int[] BACKEND_TO_UI_CORNER = {1, 2, 3, 4, 5, 0};

    private String getCornerKey(HexTile tile, int cornerIndex) {
        double centerX = SQRT_3 * (tile.getColumn() + tile.getRow() / 2.0);
        double centerY = 1.5 * tile.getRow();
        double angleDeg = 60.0 * cornerIndex - 30.0;
        double angleRad = Math.toRadians(angleDeg);
        double x = centerX + Math.cos(angleRad);
        double y = centerY + Math.sin(angleRad);
        return String.format(java.util.Locale.US, "%.6f,%.6f", x, y);
    }

    private void buildVisualToModelMapping() {
        if (game == null || hexdesert == null) return;
        visualToModelTile.clear();
        globalIntersectionCoords.clear();
        mainHexesOnly.clear();

        Group parentMap = (Group) hexdesert.getParent();
        if (parentMap == null) return;

        Board board = game.getBoard();

        // 1. Identify all 19 hex StackPanes
        for (javafx.scene.Node node : parentMap.getChildren()) {
            if (node instanceof StackPane sp) {
                if (sp.getStyleClass().stream().anyMatch(s -> s.startsWith("hex-tile-"))) {
                    mainHexesOnly.add(sp);
                }
            }
        }

        // 2. Sort StackPanes by Y then X (with tolerance for minor alignment issues)
        mainHexesOnly.sort((a, b) -> {
            double ay = a.getLayoutY();
            double by = b.getLayoutY();
            if (Math.abs(ay - by) > 10) return Double.compare(ay, by);
            return Double.compare(a.getLayoutX(), b.getLayoutX());
        });

        // 3. Sort Model HexTiles by R then Q (consistent with StandardMapGenerator axial order)
        List<HexTile> modelTiles = new ArrayList<>(board.getAllHexTiles());
        modelTiles.sort((a, b) -> {
            if (a.getRow() != b.getRow()) return Integer.compare(a.getRow(), b.getRow());
            return Integer.compare(a.getColumn(), b.getColumn());
        });

        // 4. Match visual tiles to model tiles by terrain + token first.
        Map<HexTile, StackPane> modelToVisualTile = new HashMap<>();
        Set<HexTile> usedTiles = new HashSet<>();
        for (StackPane sp : mainHexesOnly) {
            banana.republic.board.TerrainType terrain = sp.getStyleClass().stream()
                    .filter(s -> s.startsWith("hex-tile-"))
                    .map(this::parseTerrainFromStyle)
                    .filter(t -> t != null)
                    .findFirst()
                    .orElse(null);
            HexTile tile = findMatchingTile(board, terrain, parseTokenFromVisual(sp), usedTiles);
            if (tile == null) continue;
            visualToModelTile.put(sp, tile);
            modelToVisualTile.put(tile, sp);
            usedTiles.add(tile);
        }

        // Fallback for any unmatched tile; keeps older/custom maps from rendering blank.
        int count = Math.min(mainHexesOnly.size(), modelTiles.size());
        for (int i = 0; i < count && visualToModelTile.size() < count; i++) {
            StackPane sp = mainHexesOnly.get(i);
            if (visualToModelTile.containsKey(sp)) continue;
            HexTile tile = modelTiles.get(i);
            if (usedTiles.contains(tile)) continue;
            visualToModelTile.put(sp, tile);
            modelToVisualTile.put(tile, sp);
            usedTiles.add(tile);
        }

        // 5. Calculate precise intersection coordinates using mathematical mapping
        Map<String, double[]> cornerSum = new java.util.LinkedHashMap<>();
        Map<String, Integer> cornerCount = new java.util.HashMap<>();

        for (HexTile tile : board.getAllHexTiles()) {
            StackPane sp = modelToVisualTile.get(tile);
            if (sp == null) continue;
            double[][] uiCorners = getHexCorners(sp);

            for (int cornerIndex = 0; cornerIndex < 6; cornerIndex++) {
                String key = getCornerKey(tile, cornerIndex);
                int uiIdx = BACKEND_TO_UI_CORNER[cornerIndex];

                double[] sum = cornerSum.getOrDefault(key, new double[]{0.0, 0.0});
                sum[0] += uiCorners[uiIdx][0];
                sum[1] += uiCorners[uiIdx][1];
                cornerSum.put(key, sum);

                cornerCount.put(key, cornerCount.getOrDefault(key, 0) + 1);
            }
        }

        // Map calculated UI coordinates to the Board's logical intersections exactly in order
        List<banana.republic.board.Intersection> allIntersections = board.getAllIntersections();
        int idx = 0;
        for (Map.Entry<String, double[]> entry : cornerSum.entrySet()) {
            String key = entry.getKey();
            double[] sum = entry.getValue();
            int cCount = cornerCount.get(key);

            double cx = sum[0] / cCount;
            double cy = sum[1] / cCount;

            if (idx < allIntersections.size()) {
                globalIntersectionCoords.put(allIntersections.get(idx), new double[]{cx, cy});
                idx++;
            }
        }
    }

    private banana.republic.board.TerrainType parseTerrainFromStyle(String style) {
        return switch (style) {
            case "hex-tile-desert" -> banana.republic.board.TerrainType.DESERT;
            case "hex-tile-wood" -> banana.republic.board.TerrainType.FOREST;
            case "hex-tile-brick" -> banana.republic.board.TerrainType.HILL;
            case "hex-tile-wheat" -> banana.republic.board.TerrainType.FIELD;
            case "hex-tile-ore" -> banana.republic.board.TerrainType.MOUNTAIN;
            case "hex-tile-banana" -> banana.republic.board.TerrainType.BANANA_PLANTATION;
            default -> null;
        };
    }

    private int parseTokenFromVisual(StackPane sp) {
        for (javafx.scene.Node child : sp.getChildren()) {
            if (child instanceof Label lbl) {
                try {
                    return Integer.parseInt(lbl.getText().trim());
                } catch (NumberFormatException e) {
                    continue;
                }
            }
        }
        return -1;
    }

    private HexTile findMatchingTile(Board board, banana.republic.board.TerrainType terrain, int tokenValue, Set<HexTile> usedTiles) {
        if (terrain == null) return null;
        for (HexTile tile : board.getAllHexTiles()) {
            if (usedTiles.contains(tile)) continue;
            if (tile.getTerrainType() != terrain) continue;
            if (terrain == banana.republic.board.TerrainType.DESERT) return tile;
            if (tile.getNumberToken() != null && tile.getNumberToken().getValue() == tokenValue) return tile;
        }
        return null;
    }

    private void handleRobberClick(StackPane hexTile) {
        if (game == null) return;
        HexTile target = visualToModelTile.get(hexTile);
        if (target == null) target = findHexTileByVisualFallback(hexTile);
        if (target == null) return;
        try {
            game.activateRobber(target, null);
            currentMode = InteractionMode.NONE;
            refreshAllUI();
            updatePhaseUI();

            List<Player> eligible = game.getRobber().getEligibleVictims(game.getActivePlayer(), game.getBoard());
            eligible.remove(game.getActivePlayer());
            if (!eligible.isEmpty()) {
                openStealDialog(eligible);
            } else {
                game.startTradeBuildTimer(this::updateTimer);
            }
        } catch (IllegalStateException | IllegalArgumentException e) {
            showError(e.getMessage());
        }
    }

    private void openStealDialog(List<Player> eligibleVictims) {
        if (stealDialogOverlay == null) return;
        try {
            stealDialogOverlay.getChildren().clear();
            URL fxmlLocation = getClass().getResource("/fxml/steal.fxml");
            if (fxmlLocation == null) return;

            FXMLLoader loader = new FXMLLoader(fxmlLocation);
            Parent dialogUI = loader.load();

            Object controller = loader.getController();
            if (controller instanceof DialogController dc) {
                dc.setCloseHandler(() -> {
                    stealDialogOverlay.setVisible(false);
                    if (mainGameRoot != null) mainGameRoot.setEffect(null);
                    refreshAllUI();
                    game.startTradeBuildTimer(this::updateTimer);
                });
            }
            if (controller instanceof GameAwareController ga) ga.setGame(game);
            if (controller instanceof StealDialogController sdc) sdc.setEligibleVictims(eligibleVictims);

            stealDialogOverlay.getChildren().add(dialogUI);
            stealDialogOverlay.setVisible(true);
            stealDialogOverlay.toFront();
            if (mainGameRoot != null) mainGameRoot.setEffect(blurEffect);
        } catch (IOException e) {
            showError("Gagal membuka dialog steal.");
        }
    }

    private HexTile findHexTileByVisualFallback(StackPane visual) {
        if (game == null) return null;
        Board board = game.getBoard();
        if (visual == hexdesert) {
            for (HexTile t : board.getAllHexTiles()) {
                if (t.getTerrainType() == banana.republic.board.TerrainType.DESERT) return t;
            }
        }
        return board.getAllHexTiles().isEmpty() ? null : board.getAllHexTiles().get(0);
    }

    public void pasangAnchorSudut(StackPane hexTile, int idx1, int idx2, String teksToko) {
        if (hexTile == null) return;
        Group parentMap = (Group) hexTile.getParent();
        double w = hexTile.getPrefWidth() > 0 ? hexTile.getPrefWidth() : 94.0;
        double h = hexTile.getPrefHeight() > 0 ? hexTile.getPrefHeight() : 108.0;
        double[][] titikHex = {
                {w / 2, 0}, {w, h * 0.25}, {w, h * 0.75},
                {w / 2, h}, {0, h * 0.75}, {0, h * 0.25}
        };
        for (int i = 0; i < 6; i++) {
            if (i != idx1 && i != idx2) continue;

            double absoluteX = hexTile.getLayoutX() + titikHex[i][0];
            double absoluteY = hexTile.getLayoutY() + titikHex[i][1];
            harborPoints.add(new double[]{absoluteX, absoluteY});

            StackPane wadahSudut = new StackPane();
            Circle anchorCircle = new Circle(12);
            anchorCircle.setFill(Color.WHITE);
            anchorCircle.setStroke(Color.BLACK);
            anchorCircle.setStrokeWidth(1);
            ImageView iconView = new ImageView();
            if (gambarAnchorGlobal != null) {
                iconView.setImage(gambarAnchorGlobal);
                iconView.setFitWidth(14);
                iconView.setFitHeight(14);
                iconView.setPreserveRatio(true);
            }
            wadahSudut.getChildren().addAll(anchorCircle, iconView);
            wadahSudut.setLayoutX(absoluteX - 12);
            wadahSudut.setLayoutY(absoluteY - 12);
            parentMap.getChildren().add(wadahSudut);
        }
        pasangTokoSisi(hexTile, idx1, teksToko);
    }

    public void pasangTokoSisi(StackPane hexTile, int indeksSisi, String teksToko) {
        if (hexTile == null) return;
        Group parentMap = (Group) hexTile.getParent();
        double w = hexTile.getPrefWidth() > 0 ? hexTile.getPrefWidth() : 94.0;
        double h = hexTile.getPrefHeight() > 0 ? hexTile.getPrefHeight() : 108.0;

        double[][] titikLokal = {
                {w / 2, 0}, {w, h * 0.25}, {w, h * 0.75},
                {w / 2, h}, {0, h * 0.75}, {0, h * 0.25}
        };

        double startX = titikLokal[indeksSisi][0];
        double startY = titikLokal[indeksSisi][1];
        int sudutBerikutnya = (indeksSisi + 1) % 6;
        double endX = titikLokal[sudutBerikutnya][0];
        double endY = titikLokal[sudutBerikutnya][1];

        double midX = (startX + endX) / 2.0;
        double midY = (startY + endY) / 2.0;
        double dx = midX - (w / 2.0);
        double dy = midY - (h / 2.0);
        double distance = Math.sqrt(dx * dx + dy * dy);
        double jarakDorong = 50.0;

        double pushX = (dx / distance) * jarakDorong;
        double pushY = (dy / distance) * jarakDorong;
        double absoluteX = hexTile.getLayoutX() + midX + pushX;
        double absoluteY = hexTile.getLayoutY() + midY + pushY;

        // GAMBAR GARIS DERMAGA (DOCK LINES) KE LABEL
        double absStartX = hexTile.getLayoutX() + startX;
        double absStartY = hexTile.getLayoutY() + startY;
        double absEndX = hexTile.getLayoutX() + endX;
        double absEndY = hexTile.getLayoutY() + endY;

        javafx.scene.shape.Line dock1 = new javafx.scene.shape.Line(absStartX, absStartY, absoluteX, absoluteY);
        dock1.setStroke(Color.rgb(160, 100, 50, 0.8)); // Warna kayu (cokelat)
        dock1.setStrokeWidth(4.0);

        javafx.scene.shape.Line dock2 = new javafx.scene.shape.Line(absEndX, absEndY, absoluteX, absoluteY);
        dock2.setStroke(Color.rgb(160, 100, 50, 0.8));
        dock2.setStrokeWidth(4.0);

        // KOTAK LABEL TOKO
        javafx.scene.layout.HBox tokoBox = new javafx.scene.layout.HBox(3);
        tokoBox.setAlignment(javafx.geometry.Pos.CENTER);
        tokoBox.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-border-color: black; -fx-border-radius: 12; -fx-padding: 2 5 2 5;");
        tokoBox.setMaxSize(javafx.scene.layout.Region.USE_PREF_SIZE, javafx.scene.layout.Region.USE_PREF_SIZE);

        javafx.scene.control.Label labelToko = new javafx.scene.control.Label(teksToko);
        labelToko.setStyle("-fx-font-family: 'Russo One'; -fx-font-size: 10px;");

        ImageView iconView = new ImageView();
        if (gambarAnchorGlobal != null) {
            iconView.setImage(gambarAnchorGlobal);
            iconView.setFitWidth(12);
            iconView.setFitHeight(12);
        }
        tokoBox.getChildren().addAll(labelToko, iconView);
        tokoBox.setLayoutX(absoluteX);
        tokoBox.setLayoutY(absoluteY);
        tokoBox.translateXProperty().bind(tokoBox.widthProperty().divide(-2));
        tokoBox.translateYProperty().bind(tokoBox.heightProperty().divide(-2));

        // Pastikan garis dermaga ditambahkan ke map bersamaan dengan kotaknya
        parentMap.getChildren().addAll(dock1, dock2, tokoBox);
    }

    private java.util.Queue<Player> humanDiscardQueue = new java.util.LinkedList<>();

    private void showNextDiscardDialog() {
        if (humanDiscardQueue.isEmpty()) return;
        Player p = humanDiscardQueue.poll();
        try {
            if (discardDialogOverlay == null) return;
            discardDialogOverlay.getChildren().clear();
            URL fxmlLocation = getClass().getResource("/fxml/discard.fxml");
            if (fxmlLocation == null) return;

            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(fxmlLocation);
            javafx.scene.Parent dialogUI = loader.load();

            Object controller = loader.getController();
            if (controller instanceof DiscardDialogController discardController) {
                discardController.setGame(game);
                discardController.setDiscardingPlayer(p);
                discardController.setCloseHandler(() -> {
                    discardDialogOverlay.setVisible(false);
                    if (mainGameRoot != null) mainGameRoot.setEffect(null);
                    refreshAllUI();
                    showNextDiscardDialog(); // Tampilkan pemain berikutnya
                });
            }

            discardDialogOverlay.getChildren().add(dialogUI);
            discardDialogOverlay.setVisible(true);
            if (mainGameRoot != null) {
                javafx.scene.effect.BoxBlur blur = new javafx.scene.effect.BoxBlur(5, 5, 3);
                mainGameRoot.setEffect(blur);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void openDialog(String fxmlName, StackPane dialogOverlay) throws IOException {
        if (dialogOverlay == null) return;
        dialogOverlay.getChildren().clear();
        URL fxmlLocation = getClass().getResource("/fxml/" + fxmlName + ".fxml");
        if (fxmlLocation == null) return;

        FXMLLoader loader = new FXMLLoader(fxmlLocation);
        Parent dialogUI = loader.load();

        Object controller = loader.getController();
        if (controller instanceof DialogController dialogController) {
            dialogController.setCloseHandler(() -> {
                dialogOverlay.setVisible(false);
                if (mainGameRoot != null) mainGameRoot.setEffect(null);
                refreshAllUI();
            });
        }
        if (controller instanceof GameAwareController gameAware) gameAware.setGame(game);

        dialogOverlay.getChildren().add(dialogUI);
        dialogOverlay.setVisible(true);
        dialogOverlay.toFront();
        if (mainGameRoot != null) mainGameRoot.setEffect(blurEffect);
    }

    private void handleBotTurn() {
        Platform.runLater(() -> {
            try { Thread.sleep(500); } catch (InterruptedException ignored) {}
            if (game == null) return;
            Player bot = game.getActivePlayer();
            if (bot == null || !bot.isBot()) return;

            // Jalankan strategi bot jika tersedia
            java.util.List<banana.republic.player.Action> botActions = null;
            if (bot instanceof banana.republic.player.BotPlayer botPlayer) {
                try {
                    botActions = botPlayer.executeTurn(game.getState());
                } catch (Exception e) {
                    // Strategi gagal, lanjutkan dengan perilaku bawaan
                }
            }

            try {
                DiceResult result = game.rollDice();
                showDiceResult(result);
                updateLogbook();
                updateResourceCards();
                if (result.isSeven()) {
                    game.processDiscardPhase();
                    refreshAllUI();
                    HexTile target = null;
                    for (HexTile t : game.getBoard().getAllHexTiles()) {
                        if (!t.equals(game.getRobber().getCurrentTile())) {
                            target = t;
                            break;
                        }
                    }
                    if (target != null) game.activateRobber(target, null);
                }
                game.startTradeBuildTimer(this::updateTimer);
            } catch (Exception e) {}

            // Eksekusi aksi dari strategi bot (trade, build, dsb.)
            if (botActions != null) {
                for (banana.republic.player.Action action : botActions) {
                    executeBotAction(bot, action);
                }
                refreshAllUI();
            }

            Platform.runLater(() -> {
                try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
                if (game != null) {
                    game.endTurn();
                    refreshAllUI();
                    updatePhaseUI();
                    if (game.getActivePlayer() != null && game.getActivePlayer().isBot()) {
                        handleBotTurn();
                    }
                }
            });
        });
    }

    /**
     * Mengeksekusi satu aksi yang dikembalikan oleh strategi bot.
     */
    private void executeBotAction(Player bot, banana.republic.player.Action action) {
        if (action == null || game == null || bot == null) return;
        try {
            switch (action.getActionType()) {
                case TRADE_MARITIME -> {
                    banana.republic.resource.ResourceType sell =
                        (banana.republic.resource.ResourceType) action.getParameter("sellType");
                    banana.republic.resource.ResourceType buy =
                        (banana.republic.resource.ResourceType) action.getParameter("buyType");
                    if (sell != null && buy != null) {
                        game.tradeWithBank(bot, sell, buy);
                    }
                }
                case BUY_DEV_CARD -> game.buyDevelopmentCard(bot);
                case BUILD_ROAD -> {
                    Integer pathId = (Integer) action.getParameter("pathId");
                    if (pathId != null) {
                        game.getBoard().getPathById(pathId).ifPresent(
                            path -> game.buildRoad(bot, path));
                    }
                }
                case BUILD_SETTLEMENT -> {
                    Integer interId = (Integer) action.getParameter("intersectionId");
                    if (interId != null) {
                        game.getBoard().getIntersectionById(interId).ifPresent(
                            inter -> game.buildSettlement(bot, inter));
                    }
                }
                case BUILD_CITY -> {
                    Integer interId = (Integer) action.getParameter("intersectionId");
                    if (interId != null) {
                        game.getBoard().getIntersectionById(interId).ifPresent(
                            inter -> game.buildCity(bot, inter));
                    }
                }
                case PLAY_KNIGHT -> {
                    Integer tileId = (Integer) action.getParameter("tileId");
                    Integer victimIdx = (Integer) action.getParameter("victimIndex");
                    if (tileId != null) {
                        banana.republic.board.HexTile target =
                            game.getBoard().getHexTileById(tileId).orElse(null);
                        if (target != null) {
                            banana.republic.player.Player victim = null;
                            if (victimIdx != null && victimIdx >= 0 &&
                                victimIdx < game.getPlayers().size()) {
                                victim = game.getPlayers().get(victimIdx);
                            }
                            game.activateRobber(target, victim);
                        }
                    }
                }
                case END_TURN -> { /* diteruskan ke blok end turn di bawah */ }
                default -> { /* aksi lain diabaikan untuk sekarang */ }
            }
        } catch (IllegalStateException | IllegalArgumentException e) {
            // Aksi bot tidak valid (misal resource tidak cukup), abaikan
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Info");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML private void toSteal() throws IOException { openDialog("steal", stealDialogOverlay); }
    @FXML private void toVictory() throws IOException { openDialog("victory", victoryDialogOverlay); }
    @FXML private void toDiscard() throws IOException { openDialog("discard", discardDialogOverlay); }

    @FXML
    private void endGame() throws IOException {
        if (game == null) {
            App.setRoot("result");
            return;
        }
        FXMLLoader loader = App.getLoader("result");
        Parent root = loader.load();
        GameResultController controller = loader.getController();
        controller.setGame(game);
        App.setRootFromLoader(root);
    }

    public void updateConditionLabel() {
        if (currentConditionLabel == null || game == null) return;
        if (setupOrderPending) {
            currentConditionLabel.setText("Fase: Tentukan Urutan");
            return;
        }
        switch (currentMode) {
            case SETTLEMENT -> currentConditionLabel.setText("Mode: Build Pos Pantau");
            case BUILD_OVERLAY -> currentConditionLabel.setText("Mode: Build (Pos Pantau / Pipa)");
            case ROAD -> currentConditionLabel.setText("Mode: Build Pipa");
            case CITY -> currentConditionLabel.setText("Mode: Build Lab");
            case ROBBER -> currentConditionLabel.setText("Mode: Pindah Nimon");
            case NONE -> {
                GamePhase phase = game.getCurrentPhase();
                if (phase == null) currentConditionLabel.setText("Menunggu...");
                else if (phase.isSetupPhase()) currentConditionLabel.setText("Fase: Setup Awal");
                else if (phase == GamePhase.RESOURCE_GATHERING) currentConditionLabel.setText("Fase: Roll Dadu");
                else if (phase == GamePhase.TRADE_BUILD) currentConditionLabel.setText("Fase: Trade & Build");
                else if (phase == GamePhase.GAME_OVER) currentConditionLabel.setText("Game Over!");
                else currentConditionLabel.setText("Menunggu Giliran");
            }
        }
    }

    // responsive scaling helper: scale the parent map group to fit its container while preserving aspect
    private void setupResponsiveScaling(Group parentMap) {
        if (parentMap == null) return;
        javafx.scene.Node parent = parentMap.getParent();
        if (!(parent instanceof javafx.scene.layout.Region)) return;
        javafx.scene.layout.Region container = (javafx.scene.layout.Region) parent;

        Runnable recompute = () -> {
            double mapW = parentMap.getLayoutBounds().getWidth();
            double mapH = parentMap.getLayoutBounds().getHeight();
            double availW = container.getWidth();
            double availH = container.getHeight();
            if (mapW <= 0 || mapH <= 0 || availW <= 0 || availH <= 0) return;
            double scale = Math.min(availW / mapW, availH / mapH);
            // do not upscale beyond 1.0 (prevents blurry scaling); allow downscale to fit
            scale = Math.min(scale, 1.0);
            parentMap.setScaleX(scale);
            parentMap.setScaleY(scale);
        };

        // initial run after layout
        javafx.application.Platform.runLater(recompute);

        // listen for changes
        container.widthProperty().addListener((obs, o, n) -> recompute.run());
        container.heightProperty().addListener((obs, o, n) -> recompute.run());
        parentMap.layoutBoundsProperty().addListener((obs, o, n) -> recompute.run());
    }

}
