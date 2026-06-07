package banana.republic.ui;

import banana.republic.core.Game;
import banana.republic.core.LogEntry;
import banana.republic.core.VictoryPointBreakdown;
import banana.republic.player.Player;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

public class GameResultController {

    @FXML
    private Label titleLabel;
    @FXML
    private Label totalPlaytimeLabel;
    @FXML
    private VBox resultTableContainer;

    private Game game;
    private final UiNavigator navigator = new AppUiNavigator();

    public void setGame(Game game) {
        this.game = game;
        populateResults();
    }

    private void populateResults() {
        if (game == null) return;
        Player winner = game.getWinner();
        List<VictoryPointBreakdown> breakdowns = game.getAllVPBreakdowns();

        if (winner != null) {
            titleLabel.setText(winner.getName() + " has founded the Banana Republic!");
        }
        if (totalPlaytimeLabel != null) {
            totalPlaytimeLabel.setText(formatPlaytime(calculatePlaytimeSeconds()));
        }

        resultTableContainer.getChildren().clear();

        HBox headerRow = new HBox();
        headerRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        headerRow.getStyleClass().add("go-table-header");

        headerRow.getChildren().addAll(
            createHeaderLabel("PLAYER", 180.0),
            createHeaderLabel("POS\nPANTAU", 110.0),
            createHeaderLabel("LABORATORIUM", 120.0),
            createHeaderLabel("KARTU\nSPESIAL", 110.0),
            createHeaderLabel("POIN\nRAHASIA", 110.0),
            createHeaderLabel("TOTAL", 90.0)
        );
        resultTableContainer.getChildren().add(headerRow);

        for (VictoryPointBreakdown vp : breakdowns) {
            boolean isWinner = winner != null && vp.getPlayer().equals(winner);
            HBox row = new HBox();
            row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            row.getStyleClass().add(isWinner ? "go-table-row-winner" : "go-table-row");

            String textColor = isWinner ? "-fx-text-fill: #990a14;" : "";
            String winnerSuffix = isWinner ? "\n(Winner)" : "";

            HBox playerBox = new HBox();
            playerBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            playerBox.setPrefWidth(180.0);
            playerBox.setSpacing(8.0);

            if (isWinner) {
                try {
                    ImageView trophy = new ImageView(new Image(getClass().getResourceAsStream("/icons/achievement.png")));
                    trophy.setFitHeight(27.0);
                    trophy.setFitWidth(30.0);
                    trophy.setPreserveRatio(true);
                    playerBox.getChildren().add(trophy);
                } catch (Exception e) {
                    // ignore if icon missing
                }
            }

            Label nameLabel = new Label(vp.getPlayer().getName() + winnerSuffix);
            nameLabel.getStyleClass().add(isWinner ? "go-table-text-winner" : "go-table-text");
            if (isWinner) nameLabel.setStyle(textColor);
            playerBox.getChildren().add(nameLabel);

            int specialPoints = vp.getLargestArmyPoints() + vp.getLongestRoadPoints();

            row.getChildren().addAll(
                playerBox,
                createCellLabel(String.valueOf(vp.getSettlementPoints()), 110.0, isWinner, textColor, "go-table-text"),
                createCellLabel(String.valueOf(vp.getCityPoints()), 120.0, isWinner, textColor, "go-table-text"),
                createCellLabel(String.valueOf(specialPoints), 110.0, isWinner, textColor, "go-table-text"),
                createCellLabel(String.valueOf(vp.getVictoryCardPoints()), 110.0, isWinner, textColor, "go-table-text"),
                createCellLabel(String.valueOf(vp.getTotal()), 90.0, isWinner, textColor, isWinner ? "go-table-total-winner" : "go-table-total")
            );
            resultTableContainer.getChildren().add(row);
        }
    }

    private Label createHeaderLabel(String text, double width) {
        Label label = new Label(text);
        label.setPrefWidth(width);
        label.getStyleClass().add("go-table-header-text");
        label.setAlignment(javafx.geometry.Pos.CENTER);
        return label;
    }

    private Label createCellLabel(String text, double width, boolean isWinner, String textColor, String styleClass) {
        Label label = new Label(text);
        label.setPrefWidth(width);
        label.getStyleClass().add(styleClass);
        if (isWinner) label.setStyle(textColor);
        label.setAlignment(javafx.geometry.Pos.CENTER);
        return label;
    }

    private long calculatePlaytimeSeconds() {
        List<LogEntry> entries = game.getGameLog().getEntries();
        if (entries.isEmpty()) return 0;

        LocalDateTime start = entries.get(0).getTimestamp();
        LocalDateTime end = entries.stream()
                .filter(entry -> entry.getEventType() == LogEntry.EventType.VICTORY)
                .map(LogEntry::getTimestamp)
                .reduce((first, second) -> second)
                .orElse(LocalDateTime.now());
        return Math.max(0, Duration.between(start, end).getSeconds());
    }

    private String formatPlaytime(long totalSeconds) {
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;
        return String.format("%02d : %02d : %02d", hours, minutes, seconds);
    }

    @FXML
    private void handleViewBoard() throws IOException {
        if (game != null) {
            navigator.showGame(game);
        }
    }

    @FXML
    private void handleMainMenu() throws IOException {
        navigator.showMainMenu();
    }
}
