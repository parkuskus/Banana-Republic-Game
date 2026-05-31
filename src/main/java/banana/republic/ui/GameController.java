package banana.republic.ui;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

import banana.republic.board.Board;
import banana.republic.board.HexTile;
import banana.republic.core.Game;
import banana.republic.core.GamePhase;
import banana.republic.core.LogEntry;
import banana.republic.dice.DiceResult;
import banana.republic.player.Player;
import banana.republic.player.PlayerColor;
import banana.republic.resource.ResourceType;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.Parent;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.StrokeLineCap;

public class GameController implements Initializable {

    @FXML
    private StackPane mainGameRoot;
    @FXML
    private StackPane tradeDialogOverlay;
    @FXML
    private StackPane cardDialogOverlay;
    @FXML
    private StackPane settingsDialogOverlay;
    @FXML
    private StackPane hexdesert;
    @FXML
    private StackPane harborLV1; //left vertical (upper)
    @FXML
    private StackPane harborLV2; //left vertical (lower)
    @FXML
    private StackPane harborLU;  //left up
    @FXML
    private StackPane harborLD;  //left down
    @FXML
    private StackPane harborRV;  //right vertical
    @FXML
    private StackPane harborRU1; //right up
    @FXML
    private StackPane harborRU2; //right up (lower)
    @FXML
    private StackPane harborRD1; //right down (inner)
    @FXML
    private StackPane harborRD2; //right down (outer)

    @FXML private VBox sidePanel;
    @FXML private HBox playerPanel1, playerPanel2, playerPanel3, playerPanel4;

    @FXML private Label lblWoodCount, lblBrickCount, lblWheatCount, lblOreCount, lblBananaCount;

    @FXML private Label timerLabel;
    @FXML private ImageView diceImage1, diceImage2;
    @FXML private Label currentPlayerLabel;

    // Logbook
    @FXML private VBox logbookContainer;
    @FXML private Label logEntry1, logEntry2, logEntry3, logEntry4, logEntry5, logEntry6, logEntry7;

    // Buttons
    @FXML private Button btnRollDice, btnSetDice, btnBuild, btnTrade, btnCard, btnDeclareVictory, btnSettings, btnEndTurn;

    // ============================================================
    // Model & State
    // ============================================================
    private Game game;
    private List<Label> logLabels;
    private List<HBox> playerPanels;
    private Image[] diceImages = new Image[6];
    private GaussianBlur blurEffect = new GaussianBlur(10);
    private Image gambarAnchorGlobal = null;

    // Board visual → model mapping
    private final java.util.Map<StackPane, HexTile> visualToModelTile = new java.util.HashMap<>();

    // Build / interaction modes
    private enum InteractionMode {
        NONE, SETTLEMENT, ROAD, CITY, ROBBER
    }
    private InteractionMode currentMode = InteractionMode.NONE;

    // ============================================================
    // Initialization
    // ============================================================
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Load dice images
        for (int i = 1; i <= 6; i++) {
            URL url = getClass().getResource("/icons/dice_one.png"); // fallback if specific images missing
            try {
                URL specific = getClass().getResource("/icons/dice_" + i + ".png");
                if (specific == null) specific = getClass().getResource("/icons/dice_one.png");
                diceImages[i - 1] = new Image(specific.toExternalForm());
            } catch (Exception e) {
                diceImages[i - 1] = null;
            }
        }

        // Anchor image
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

        // Log labels list
        logLabels = Arrays.asList(logEntry1, logEntry2, logEntry3, logEntry4, logEntry5, logEntry6, logEntry7);
        playerPanels = Arrays.asList(playerPanel1, playerPanel2, playerPanel3, playerPanel4);

        // Setup board visuals (existing code preserved)
        if (hexdesert != null) {
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

        // Pasang event klik ke semua tile di board (dinamis)
        setupAllTileClickHandlers();
    }

    /**
     * Called after FXML load to inject the Game instance.
     */
    public void initialize(Game game) {
        this.game = game;
        if (game == null) return;

        buildVisualToModelMapping();
        game.startSetupPhase();
        refreshAllUI();
        updatePhaseUI();

        // If first player is bot, trigger bot turn
        if (game.getActivePlayer() != null && game.getActivePlayer().isBot()) {
            handleBotTurn();
        }
    }

    public void setGame(Game game) {
        initialize(game);
    }

    public Game getGame() {
        return game;
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
        dialog.setContentText("Format: die1,die2 (contoh: 3,4)");

        dialog.showAndWait().ifPresent(input -> {
            try {
                String[] parts = input.split(",");
                int d1 = Integer.parseInt(parts[0].trim());
                int d2 = Integer.parseInt(parts[1].trim());
                if (d1 < 1 || d1 > 6 || d2 < 1 || d2 > 6) {
                    showError("Nilai dadu harus antara 1 dan 6.");
                    return;
                }
                game.getDice().setManualMode(true);
                game.getDice().setManualValues(d1, d2);
                DiceResult result = game.rollDice();
                game.getDice().setManualMode(false); // reset setelah roll
                showDiceResult(result);
                updateLogbook();
                updateResourceCards();

                if (result.isSeven()) {
                    currentMode = InteractionMode.ROBBER;
                    showInfo("Dadu 7! Nimon Ungu aktif. Pilih petak untuk memindahkan Nimon Ungu.");
                } else {
                    game.startTradeBuildTimer(this::updateTimer);
                }
                updatePhaseUI();
            } catch (Exception e) {
                showError("Format tidak valid. Gunakan: die1,die2 (contoh: 3,4)");
            }
        });
    }

    @FXML
    private void onBuild() {
        if (game == null) return;
        GamePhase phase = game.getCurrentPhase();
        if (phase.isSetupPhase()) {
            if (game.getSetupSettlementCount() % 2 == 0) {
                currentMode = InteractionMode.SETTLEMENT;
                showInfo("Klik persimpangan (intersection) untuk menempatkan Pos Pantau.");
            } else {
                currentMode = InteractionMode.ROAD;
                showInfo("Klik jalan (path) untuk menempatkan Pipa.");
            }
        } else if (phase == GamePhase.TRADE_BUILD) {
            // Toggle build menu / prompt in future could show choices
            currentMode = InteractionMode.SETTLEMENT;
            showInfo("Klik persimpangan untuk membangun Pos Pantau, atau tekan Shift+klik untuk upgrade ke Laboratorium.");
        } else {
            showError("Tidak bisa membangun di fase ini.");
        }
    }

    @FXML
    private void onEndTurn() {
        if (game == null) return;
        try {
            game.endTurn();
            currentMode = InteractionMode.NONE;
            refreshAllUI();
            updatePhaseUI();

            if (game.getActivePlayer() != null && game.getActivePlayer().isBot()) {
                handleBotTurn();
            }
        } catch (IllegalStateException | IllegalArgumentException e) {
            showError(e.getMessage());
        }
    }

    @FXML
    private void onCard() {
        if (game == null) return;
        try {
            openDialog("card", cardDialogOverlay);
        } catch (IOException e) {
            showError("Gagal membuka dialog kartu.");
        }
    }

    @FXML
    private void toCard() throws IOException {
        openDialog("card", cardDialogOverlay);
    }

    @FXML
    private void onDeclareVictory() {
        if (game == null) return;
        Player winner = game.checkVictory();
        if (winner != null) {
            showInfo(winner.getName() + " MENANG!");
            // TODO: Transition to GameResult
        } else {
            showInfo("Belum ada pemain yang mencapai 10 Poin Prestasi.");
        }
        refreshAllUI();
    }

    @FXML
    private void onSettings() {
        if (game == null) return;
        try {
            openDialog("settings", settingsDialogOverlay);
        } catch (IOException e) {
            showError("Gagal membuka dialog pengaturan.");
        }
    }

    @FXML
    private void toSettings() throws IOException {
        openDialog("settings", settingsDialogOverlay);
    }

    @FXML
    private void toTrade() throws IOException {
        openDialog("trade", tradeDialogOverlay);
    }

    // ============================================================
    // UI Update Methods
    // ============================================================
    private void refreshAllUI() {
        updatePlayerPanel();
        updateResourceCards();
        updateLogbook();
        updateCurrentPlayer();
        updateTimer(game.getTurnManager().getRemainingTimerSeconds());
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
        // Sidebar panel structure:
        // HBox -> Region(color bar) + VBox -> HBox(name, vp) + HBox(supplies) + HBox(resources, cards)
        VBox content = (VBox) panel.getChildren().get(1);
        HBox nameRow = (HBox) content.getChildren().get(0);
        HBox supplyRow = (HBox) content.getChildren().get(1);
        HBox resourceRow = (HBox) content.getChildren().get(2);

        // Name & VP
        StackPane numberPane = (StackPane) nameRow.getChildren().get(0);
        Circle circle = (Circle) numberPane.getChildren().get(0);
        Label numberLabel = (Label) numberPane.getChildren().get(1);
        numberLabel.setText(String.valueOf(rank));

        Label nameLabel = (Label) nameRow.getChildren().get(1);
        nameLabel.setText(player.getName());

        Label vpLabel = (Label) nameRow.getChildren().get(3);
        int vp = game.getVPTotal(player);
        vpLabel.setText(vp + " VP");

        // Color mapping
        String colorHex = playerColorToHex(player.getColor());
        circle.setFill(Color.web(colorHex));
        vpLabel.setStyle("-fx-background-color: " + colorHex + "; -fx-padding: 4 8; -fx-background-radius: 4; -fx-font-weight: bold; -fx-text-fill: white; -fx-font-size: 1.1em;");
        ((Region) panel.getChildren().get(0)).setStyle("-fx-background-color: " + colorHex + "; -fx-background-radius: 8 0 0 8;");

        // Supply counts
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

        // Resources & cards
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
            case WHITE -> "#aaaaaa";
            default -> "#888888";
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

    // ============================================================
    // Phase-based UI State
    // ============================================================
    private void updatePhaseUI() {
        if (game == null) return;
        GamePhase phase = game.getCurrentPhase();

        boolean isSetup = phase.isSetupPhase();
        boolean isGathering = (phase == GamePhase.RESOURCE_GATHERING);
        boolean isTradeBuild = (phase == GamePhase.TRADE_BUILD);
        boolean isGameOver = (phase == GamePhase.GAME_OVER);

        btnRollDice.setDisable(isSetup || !isGathering || isGameOver);
        btnSetDice.setDisable(isSetup || isGameOver);
        btnBuild.setDisable(isGathering || isGameOver);
        btnTrade.setDisable(isSetup || isGathering || isGameOver);
        btnCard.setDisable(isSetup || isGathering || isGameOver);
        btnDeclareVictory.setDisable(isSetup || isGameOver);
        btnEndTurn.setDisable(isSetup || isGathering || isGameOver);
        btnSettings.setDisable(isGameOver);
    }

    // ============================================================
    // Board Interaction (Preserved + Enhanced)
    // ============================================================
    private boolean canAddRoad = true;

    public void setCanAddRoad(boolean canAddRoad) {
        this.canAddRoad = canAddRoad;
    }

    public boolean isCanAddRoad() {
        return this.canAddRoad;
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
            if (currentMode == InteractionMode.ROBBER) {
                handleRobberClick(hexTile);
                return;
            }
            if (!canAddRoad) {
                return;
            }
            double clickX = event.getX();
            double clickY = event.getY();
            int sisiTerdekat = hitungSisiTerdekatAngle(w, h, clickX, clickY);
            warnaiSisiHexagon(hexTile, sisiTerdekat, Color.BLUE);
        });
    }

    private void setupAllTileClickHandlers() {
        if (hexdesert == null) return;
        javafx.scene.Group parentMap = (javafx.scene.Group) hexdesert.getParent();
        if (parentMap == null) return;

        for (javafx.scene.Node node : parentMap.getChildren()) {
            if (node instanceof StackPane sp) {
                String style = sp.getStyleClass().stream()
                    .filter(s -> s.startsWith("hex-tile-"))
                    .findFirst().orElse("");
                if (!style.isEmpty() && sp != hexdesert) {
                    pasangEventKlik(sp);
                }
            }
        }
    }

    /**
     * Build visual→model mapping saat game sudah di-set.
     * Harus dipanggil setelah board tersedia.
     */
    private void buildVisualToModelMapping() {
        if (game == null || hexdesert == null) return;
        visualToModelTile.clear();
        javafx.scene.Group parentMap = (javafx.scene.Group) hexdesert.getParent();
        if (parentMap == null) return;

        Board board = game.getBoard();
        for (javafx.scene.Node node : parentMap.getChildren()) {
            if (!(node instanceof StackPane sp)) continue;
            String style = sp.getStyleClass().stream()
                .filter(s -> s.startsWith("hex-tile-"))
                .findFirst().orElse("");
            if (style.isEmpty()) continue;

            // Parse terrain dari styleClass
            banana.republic.board.TerrainType terrain = parseTerrainFromStyle(style);
            // Parse token number dari Label child
            int tokenValue = parseTokenFromVisual(sp);

            HexTile matched = findMatchingTile(board, terrain, tokenValue);
            if (matched != null) {
                visualToModelTile.put(sp, matched);
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
        return -1; // Desert atau tidak ada token
    }

    private HexTile findMatchingTile(Board board, banana.republic.board.TerrainType terrain, int tokenValue) {
        if (terrain == null) return null;
        for (HexTile tile : board.getAllHexTiles()) {
            if (tile.getTerrainType() != terrain) continue;
            if (terrain == banana.republic.board.TerrainType.DESERT) {
                return tile;
            }
            if (tile.getNumberToken() != null && tile.getNumberToken().getValue() == tokenValue) {
                return tile;
            }
        }
        return null;
    }

    private void handleRobberClick(StackPane hexTile) {
        if (game == null) return;
        HexTile target = visualToModelTile.get(hexTile);
        if (target == null) {
            target = findHexTileByVisualFallback(hexTile);
        }
        if (target == null) {
            showError("Tidak dapat menemukan petak.");
            return;
        }
        try {
            game.activateRobber(target, null);
            currentMode = InteractionMode.NONE;
            game.startTradeBuildTimer(this::updateTimer);
            refreshAllUI();
            updatePhaseUI();
        } catch (IllegalStateException | IllegalArgumentException e) {
            showError(e.getMessage());
        }
    }

    private HexTile findHexTileByVisualFallback(StackPane visual) {
        if (game == null) return null;
        Board board = game.getBoard();
        if (visual == hexdesert) {
            for (HexTile t : board.getAllHexTiles()) {
                if (t.getTerrainType() == banana.republic.board.TerrainType.DESERT) {
                    return t;
                }
            }
        }
        return board.getAllHexTiles().isEmpty() ? null : board.getAllHexTiles().get(0);
    }

    private int hitungSisiTerdekatAngle(double w, double h, double cx, double cy) {
        double centerX = w / 2;
        double centerY = h / 2;
        double angle = Math.toDegrees(Math.atan2(cy - centerY, cx - centerX));
        if (angle >= -90 && angle < -30) return 0;
        else if (angle >= -30 && angle < 30) return 1;
        else if (angle >= 30 && angle < 90) return 2;
        else if (angle >= 90 && angle < 150) return 3;
        else if (angle >= 150 || angle < -150) return 4;
        else return 5;
    }

    private void warnaiSisiHexagon(StackPane hexTile, int indeksSisi, Color warna) {
        double w = hexTile.getPrefWidth();
        double h = hexTile.getPrefHeight();
        double[][] titik = {
                {w / 2, 0}, {w, h * 0.25}, {w, h * 0.75},
                {w / 2, h}, {0, h * 0.75}, {0, h * 0.25}
        };
        double startX = titik[indeksSisi][0];
        double startY = titik[indeksSisi][1];
        int sudutBerikutnya = (indeksSisi + 1) % 6;
        double endX = titik[sudutBerikutnya][0];
        double endY = titik[sudutBerikutnya][1];
        Line garisPinggir = new Line(startX, startY, endX, endY);
        garisPinggir.setStroke(warna);
        garisPinggir.setStrokeWidth(8.0);
        garisPinggir.setStrokeLineCap(StrokeLineCap.ROUND);
        garisPinggir.setManaged(false);
        garisPinggir.setMouseTransparent(true);
        hexTile.getChildren().add(garisPinggir);
        garisPinggir.toFront();
        hexTile.toFront();
    }

    public void pasangAnchorSudut(StackPane hexTile, int idx1, int idx2, String teksToko) {
        if (hexTile == null) {
            System.out.println("Peringatan: Ada StackPane harbor yang belum di-link (null) di Scene Builder!");
            return;
        }
        javafx.scene.Group parentMap = (javafx.scene.Group) hexTile.getParent();
        double w = hexTile.getPrefWidth();
        double h = hexTile.getPrefHeight();
        double[][] titikHex = {
                {w / 2, 0}, {w, h * 0.25}, {w, h * 0.75},
                {w / 2, h}, {0, h * 0.75}, {0, h * 0.25}
        };
        for (int i = 0; i < 6; i++) {
            if (i != idx1 && i != idx2) continue;
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
            double absoluteX = hexTile.getLayoutX() + titikHex[i][0];
            double absoluteY = hexTile.getLayoutY() + titikHex[i][1];
            wadahSudut.setLayoutX(absoluteX - 12);
            wadahSudut.setLayoutY(absoluteY - 12);
            parentMap.getChildren().add(wadahSudut);
        }
        pasangTokoSisi(hexTile, idx1, teksToko);
    }

    public void pasangTokoSisi(StackPane hexTile, int indeksSisi, String teksToko) {
        if (hexTile == null) return;
        javafx.scene.Group parentMap = (javafx.scene.Group) hexTile.getParent();
        double w = hexTile.getPrefWidth();
        double h = hexTile.getPrefHeight();
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
        parentMap.getChildren().add(tokoBox);
    }

    // ============================================================
    // Dialog Handling (Preserved)
    // ============================================================
    @FXML
    private void openDialog(String fxmlName, StackPane dialogOverlay) throws IOException {
        if (dialogOverlay == null) {
            System.out.println("ERROR: dialogOverlay bernilai null!");
            return;
        }

        dialogOverlay.getChildren().clear();

        String pathFxml = "/fxml/" + fxmlName + ".fxml";
        URL fxmlLocation = getClass().getResource(pathFxml);
        if (fxmlLocation == null) {
            System.out.println("ERROR KRITIS: File tidak ditemukan: " + pathFxml);
            return;
        }

        FXMLLoader loader = new FXMLLoader(fxmlLocation);
        Parent dialogUI = loader.load();

        Object controller = loader.getController();
        if (controller instanceof DialogController dialogController) {
            dialogController.setCloseHandler(() -> {
                dialogOverlay.setVisible(false);
                if (mainGameRoot != null) {
                    mainGameRoot.setEffect(null);
                }
                refreshAllUI();
            });
        }
        if (controller instanceof GameAwareController gameAware) {
            gameAware.setGame(game);
        }

        dialogOverlay.getChildren().add(dialogUI);
        dialogOverlay.setVisible(true);
        dialogOverlay.toFront();

        if (mainGameRoot != null) {
            mainGameRoot.setEffect(blurEffect);
        }
    }

    private void closeOverlay(StackPane dialogOverlay) {
        dialogOverlay.setVisible(false);
        if (mainGameRoot != null) {
            mainGameRoot.setEffect(null);
        }
    }

    // ============================================================
    // Bot Turn Handling
    // ============================================================
    private void handleBotTurn() {
        Platform.runLater(() -> {
            try {
                Thread.sleep(500); // small delay for UX
            } catch (InterruptedException ignored) {}
            if (game == null) return;
            Player bot = game.getActivePlayer();
            if (bot == null || !bot.isBot()) return;

            // Bot roll dice automatically
            try {
                DiceResult result = game.rollDice();
                showDiceResult(result);
                updateLogbook();
                updateResourceCards();
                if (result.isSeven()) {
                    // Bot robber: choose first valid tile and first victim
                    HexTile target = null;
                    for (HexTile t : game.getBoard().getAllHexTiles()) {
                        if (!t.equals(game.getRobber().getCurrentTile())) {
                            target = t;
                            break;
                        }
                    }
                    if (target != null) {
                        game.activateRobber(target, null);
                    }
                }
                game.startTradeBuildTimer(this::updateTimer);
            } catch (Exception e) {
                System.out.println("Bot roll error: " + e.getMessage());
            }

            // Bot builds if possible (stub logic)
            // End turn after delay
            Platform.runLater(() -> {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ignored) {}
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

    // ============================================================
    // Helpers
    // ============================================================
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
}
