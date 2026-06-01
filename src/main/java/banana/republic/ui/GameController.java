package banana.republic.ui;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.HashSet;

import banana.republic.App;
import banana.republic.board.Board;
import banana.republic.board.HexTile;
import banana.republic.core.Game;
import banana.republic.core.GamePhase;
import banana.republic.core.LogEntry;
import banana.republic.dice.DiceResult;
import banana.republic.player.Player;
import banana.republic.player.PlayerColor;
import banana.republic.resource.ResourceType;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.control.Alert;
import javafx.scene.control.Tooltip;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.Parent;
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
import javafx.scene.shape.StrokeLineCap;
import javafx.util.Duration;

public class GameController implements Initializable {

    @FXML private StackPane mainGameRoot;
    @FXML private StackPane tradeDialogOverlay;
    @FXML private StackPane cardDialogOverlay;
    @FXML private StackPane stealDialogOverlay;
    @FXML private StackPane settingsDialogOverlay;
    @FXML private StackPane victoryDialogOverlay;
    @FXML private StackPane discardDialogOverlay;
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

    @FXML private VBox logbookContainer;
    @FXML private Label logEntry1, logEntry2, logEntry3, logEntry4, logEntry5, logEntry6, logEntry7;

    @FXML private Button btnRollDice, btnSetDice, btnBuild, btnTrade, btnCard, btnDeclareVictory, btnSettings, btnEndTurn;
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

    private enum InteractionMode {
        NONE, SETTLEMENT, ROAD, CITY, ROBBER, BUILD_OVERLAY
    }
    private InteractionMode currentMode = InteractionMode.NONE;

    // ============================================================
    // Initialization
    // ============================================================
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        for (int i = 1; i <= 6; i++) {
            URL url = getClass().getResource("/icons/dice_one.png");
            try {
                URL specific = getClass().getResource("/icons/dice_" + i + ".png");
                if (specific == null) specific = getClass().getResource("/icons/dice_one.png");
                diceImages[i - 1] = new Image(specific.toExternalForm());
            } catch (Exception e) {
                diceImages[i - 1] = null;
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

        logLabels = Arrays.asList(logEntry1, logEntry2, logEntry3, logEntry4, logEntry5, logEntry6, logEntry7);
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

        game.startSetupPhase();
        refreshAllUI();
        updatePhaseUI();

        if (game.getActivePlayer() != null && game.getActivePlayer().isBot()) {
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
    private void onRollDice() {
        if (game == null) return;
        try {
            DiceResult result = game.rollDice();
            showDiceResult(result);
            updateLogbook();
            updateResourceCards();

            if (result.isSeven()) {
                game.processDiscardPhase();
                refreshAllUI();
                boolean needHumanDiscard = game.getPlayers().stream()
                        .anyMatch(p -> !p.isBot() && p.getTotalResourceCount() > Game.HAND_LIMIT);
                if (needHumanDiscard) {
                    try { openDialog("discard", discardDialogOverlay); } catch (IOException e) {}
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
        javafx.scene.control.TextInputDialog dialog = new javafx.scene.control.TextInputDialog("1,1");
        dialog.setTitle("Set Dice Manual");
        dialog.setHeaderText("Masukkan nilai dadu (1-6)");
        dialog.showAndWait().ifPresent(input -> {
            try {
                String[] parts = input.split(",");
                int d1 = Integer.parseInt(parts[0].trim());
                int d2 = Integer.parseInt(parts[1].trim());
                game.getDice().setManualMode(true);
                game.getDice().setManualValues(d1, d2);
                DiceResult result = game.rollDice();
                game.getDice().setManualMode(false);
                showDiceResult(result);
                updateLogbook();
                updateResourceCards();
                if (result.isSeven()) {
                    game.processDiscardPhase();
                    refreshAllUI();
                    currentMode = InteractionMode.ROBBER;
                    showInfo("Dadu 7! Nimon Ungu aktif.");
                } else {
                    game.startTradeBuildTimer(this::updateTimer);
                }
                updatePhaseUI();
            } catch (Exception e) {
                showError("Format tidak valid.");
            }
        });
    }

    // --- FITUR BUILD OVERLAY INTERACTIVE TER-ROBUST ---
    @FXML
    private void onBuild() {
        if (game == null) return;
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

        // --- 2. LOOP MURNI DARI MEMORI BACKEND (TIDAK ADA LAGI PERGESERAN VISUAL) ---
        // Menggunakan globalIntersectionCoords menjamin bahwa titik yang diklik
        // sama persis posisinya dengan titik yang dirender ulang saat End Turn.
        for (Map.Entry<banana.republic.board.Intersection, double[]> entry : globalIntersectionCoords.entrySet()) {
            banana.republic.board.Intersection inter = entry.getKey();
            double[] coords = entry.getValue();

            // Titik Absolut yang dikunci oleh JavaFX dan Backend
            double cx = coords[0];
            double cy = coords[1];

            // ==============================================================
            // PRE-VALIDATION LOGIC
            // ==============================================================
            // Aturan 1: Lewati jika sudah ada bangunan di sini
            if (inter.getOwner() != null) continue;

            // Aturan 2: Aturan Jarak (Distance Rule)
            boolean distanceValid = true;
            if (inter.getAdjacentPaths() != null) {
                for (banana.republic.board.Path p : inter.getAdjacentPaths()) {
                    if (p == null) continue;
                    banana.republic.board.Intersection neighbor = (p.getIntersectionA() == inter) ? p.getIntersectionB() : p.getIntersectionA();
                    if (neighbor != null && neighbor.getOwner() != null) {
                        distanceValid = false;
                        break;
                    }
                }
            }
            if (!distanceValid) continue; // Skip, jangan munculkan lingkaran

            // Aturan 3: Aturan Koneksi Jalan (Hanya di luar Fase Setup)
            if (game != null && !game.getCurrentPhase().isSetupPhase() && game.getActivePlayer() != null) {
                boolean isConnectedToOwnRoad = false;
                if (inter.getAdjacentPaths() != null) {
                    for (banana.republic.board.Path p : inter.getAdjacentPaths()) {
                        if (p != null && game.getActivePlayer().equals(p.getOwner())) {
                            isConnectedToOwnRoad = true;
                            break;
                        }
                    }
                }
                if (!isConnectedToOwnRoad) continue; // Skip, tidak terhubung dengan pipa
            }
            // ==============================================================

            // Deteksi apakah titik ini adalah harbor untuk menambahkan logo jangkar
            boolean isHarbor = false;
            for (double[] hp : harborPoints) {
                if (Math.hypot(hp[0] - cx, hp[1] - cy) < 20.0) {
                    isHarbor = true;
                    // PERBAIKAN PENTING: Kita tidak lagi menimpa nilai cx dan cy dengan hp[0] hp[1].
                    // Hal inilah yang membuat posisi bangunan melompat-lompat di kode sebelumnya!
                    break;
                }
            }

            StackPane nodeWadah = new StackPane();
            nodeWadah.setLayoutX(cx - 15);
            nodeWadah.setLayoutY(cy - 15);
            nodeWadah.setPrefSize(30, 30);
            nodeWadah.setStyle("-fx-cursor: hand;");

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

            // Tambahkan Ikon Harbor
            if (isHarbor && gambarAnchorGlobal != null) {
                ImageView anchorView = new ImageView(gambarAnchorGlobal);
                anchorView.setFitWidth(16);
                anchorView.setFitHeight(16);
                anchorView.setPreserveRatio(true);
                nodeWadah.getChildren().add(anchorView);
            }

            final double finalCx = cx;
            final double finalCy = cy;

            // --- EFEK HOVER WARNA PLAYER ---
            nodeWadah.setOnMouseEntered(e -> {
                dot.setEffect(new DropShadow(15, finalPlayerColor));
            });
            nodeWadah.setOnMouseExited(e -> {
                dot.setEffect(null);
            });

            // --- EVENT KLIK KEKAL ---
            nodeWadah.setOnMouseClicked(e -> {
                if (game != null && game.getActivePlayer() != null) {
                    try {
                        Player activeP = game.getActivePlayer();
                        if (game.getCurrentPhase().isSetupPhase()) {
                            game.placeInitialSettlement(activeP, inter);
                        } else {
                            game.buildSettlement(activeP, inter);
                        }
                    } catch (Exception ex) {
                        showError("Sistem gagal membangun di backend: " + ex.getMessage());
                        return;
                    }
                }

                // Tampilkan bayangan permanen segera setelah diklik
                Circle permanentBuilding = new Circle(finalCx, finalCy, 16);
                permanentBuilding.setFill(finalPlayerColor);
                permanentBuilding.setStroke(Color.BLACK);
                permanentBuilding.setStrokeWidth(2);

                if (permanentBuildLayer != null) {
                    permanentBuildLayer.getChildren().add(permanentBuilding);
                    permanentBuildLayer.toFront();
                }

                removeBuildOverlay();
                if (btnEndTurn != null) {
                    btnEndTurn.setDisable(false);
                }

                e.consume();
            });

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

    private void createPathLine(double sx, double sy, double ex, double ey, Pane pathLayer, banana.republic.board.Path path) {
        Line line = new Line(sx, sy, ex, ey);
        line.setStroke(Color.rgb(200, 200, 200, 0.6));
        line.setStrokeWidth(12.0);
        line.setStrokeLineCap(StrokeLineCap.ROUND);
        line.setStyle("-fx-cursor: hand;");

        DropShadow glow = new DropShadow(12, Color.YELLOW);
        line.setOnMouseEntered(e -> {
            line.setEffect(glow);
            line.setStroke(Color.YELLOW);
            line.setStrokeWidth(18.0);
        });
        line.setOnMouseExited(e -> {
            line.setEffect(null);
            line.setStroke(Color.rgb(200, 200, 200, 0.6));
            line.setStrokeWidth(12.0);
        });

        line.setOnMouseClicked(e -> eksekusiBangunDariOverlay(null, path, false));
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
//                eksekusiBangunDariOverlay(null, path);
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

    private void createPathLine(double sx, double sy, double ex, double ey, Pane pathLayer) {
        Line line = new Line(sx, sy, ex, ey);
        line.setStroke(Color.rgb(200, 200, 200, 0.6));
        line.setStrokeWidth(12.0);
        line.setStrokeLineCap(StrokeLineCap.ROUND);
        line.setStyle("-fx-cursor: hand;");

        Tooltip.install(line, new Tooltip("Bangun Pipa\n" + getRoadCostString()));

        DropShadow glow = new DropShadow(12, Color.YELLOW);
        line.setOnMouseEntered(e -> {
            line.setEffect(glow);
            line.setStroke(Color.YELLOW);
            line.setStrokeWidth(18.0);
        });
        line.setOnMouseExited(e -> {
            line.setEffect(null);
            line.setStroke(Color.rgb(200, 200, 200, 0.6));
            line.setStrokeWidth(12.0);
        });

        line.setOnMouseClicked(e -> buildFromVisualEdge(sx, sy, ex, ey));

        pathLayer.getChildren().add(line);
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
                    game.placeInitialSettlement(activePlayer, intersection);
                    lastSetupSettlement = intersection;
                    removeBuildOverlay();
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
                    Line builtRoad = new Line(cA[0], cA[1], cB[0], cB[1]);
                    builtRoad.setStroke(Color.web(playerColorToHex(path.getOwner().getColor())));
                    builtRoad.setStrokeWidth(12);
                    builtRoad.setStrokeLineCap(StrokeLineCap.ROUND);
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
        permanentBuildLayer.toFront();
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
        updateTimer(game.getTurnManager().getRemainingTimerSeconds());
        renderExistingBuildings();
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

    public void updateTimer(int remainingSeconds) {
        if (timerLabel == null) return;
        int minutes = remainingSeconds / 60;
        int seconds = remainingSeconds % 60;
        timerLabel.setText(String.format("%02d:%02d", minutes, seconds));
    }

    public void updateCurrentPlayer() {
        if (game == null || currentPlayerLabel == null) return;
        Player active = game.getActivePlayer();
        if (active != null) {
            currentPlayerLabel.setText(active.getName() + "'s Turn");
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

        btnRollDice.setDisable(isSetup || !isGathering || isGameOver);
        btnSetDice.setDisable(isSetup || isGameOver);
        btnBuild.setDisable(isGathering || isGameOver);
        btnTrade.setDisable(isSetup || isGathering || isGameOver);
        btnCard.setDisable(isSetup || isGathering || isGameOver);
        btnDeclareVictory.setDisable(isSetup || isGameOver);
        btnEndTurn.setDisable(isSetup || isGathering || isGameOver);
        btnSettings.setDisable(isGameOver);

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
            if (path.getIntersectionA() == b || path.getIntersectionB() == b) {
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

    private void buildVisualToModelMapping() {
        if (game == null || hexdesert == null) return;
        visualToModelTile.clear();
        globalIntersectionCoords.clear();
        mainHexesOnly.clear();

        Group parentMap = (Group) hexdesert.getParent();
        if (parentMap == null) return;

        Board board = game.getBoard();
        Set<HexTile> usedTiles = new HashSet<>();

        for (javafx.scene.Node node : parentMap.getChildren()) {
            if (node instanceof StackPane sp) {
                String id = sp.getId();
//                if (id != null && id.startsWith("harbor")) continue; // ABAIKAN UBIN OCEAN HARBOR FXML
                if (sp.getStyleClass().toString().contains("hex-tile-")) {
                    mainHexesOnly.add(sp);
                }
            }
        }

        // TAHAP 1
        for (StackPane sp : mainHexesOnly) {
            String style = sp.getStyleClass().stream().filter(s -> s.startsWith("hex-tile-")).findFirst().orElse("");
            banana.republic.board.TerrainType terrain = parseTerrainFromStyle(style);
            int tokenValue = parseTokenFromVisual(sp);

            HexTile matched = findMatchingTile(board, terrain, tokenValue, usedTiles);
            if (matched != null) {
                visualToModelTile.put(sp, matched);
                usedTiles.add(matched);
            }
        }

        // TAHAP 2
        for (StackPane sp : mainHexesOnly) {
            if (!visualToModelTile.containsKey(sp)) {
                for (HexTile tile : board.getAllHexTiles()) {
                    if (!usedTiles.contains(tile)) {
                        visualToModelTile.put(sp, tile);
                        usedTiles.add(tile);
                        break;
                    }
                }
            }
        }

        // TAHAP 3
        for (Map.Entry<StackPane, HexTile> entry : visualToModelTile.entrySet()) {
            StackPane sp = entry.getKey();
            HexTile tile = entry.getValue();
            List<banana.republic.board.Intersection> adjs = game.getBoard().getAdjacentIntersections(tile);
            if (adjs == null) continue;

            double[][] corners = getHexCorners(sp);
            for (int i = 0; i < 6; i++) {
                if (i < adjs.size()) {
                    banana.republic.board.Intersection inter = adjs.get(i);
                    if (!globalIntersectionCoords.containsKey(inter)) {
                        globalIntersectionCoords.put(inter, corners[i]);
                    }
                }
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
