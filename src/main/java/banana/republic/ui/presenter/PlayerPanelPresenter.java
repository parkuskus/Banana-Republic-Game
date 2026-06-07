package banana.republic.ui.presenter;

import java.util.List;

import banana.republic.core.Game;
import banana.republic.player.Player;
import banana.republic.player.PlayerColor;
import banana.republic.player.SpecialCardType;
import banana.republic.resource.ResourceType;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class PlayerPanelPresenter {

    private final List<HBox> playerPanels;
    private final Label currentPlayerLabel;
    private final Label woodCount;
    private final Label brickCount;
    private final Label wheatCount;
    private final Label oreCount;
    private final Label bananaCount;
    private final Label longestRoadStatusLabel;
    private final Label largestArmyStatusLabel;

    public PlayerPanelPresenter(List<HBox> playerPanels,
                                Label currentPlayerLabel,
                                Label woodCount,
                                Label brickCount,
                                Label wheatCount,
                                Label oreCount,
                                Label bananaCount,
                                Label longestRoadStatusLabel,
                                Label largestArmyStatusLabel) {
        this.playerPanels = playerPanels;
        this.currentPlayerLabel = currentPlayerLabel;
        this.woodCount = woodCount;
        this.brickCount = brickCount;
        this.wheatCount = wheatCount;
        this.oreCount = oreCount;
        this.bananaCount = bananaCount;
        this.longestRoadStatusLabel = longestRoadStatusLabel;
        this.largestArmyStatusLabel = largestArmyStatusLabel;
    }

    public void renderPlayers(Game game) {
        if (game == null || playerPanels == null) return;
        List<Player> players = game.getPlayers();
        for (int i = 0; i < playerPanels.size(); i++) {
            HBox panel = playerPanels.get(i);
            if (panel == null) continue;
            if (i < players.size()) {
                panel.setVisible(true);
                panel.setManaged(true);
                renderPlayerPanel(game, panel, players.get(i), i + 1);
            } else {
                panel.setVisible(false);
                panel.setManaged(false);
            }
        }
        renderLongestRoadStatus(players);
        renderLargestArmyStatus(players);
    }

    private void renderLargestArmyStatus(List<Player> players) {
        if (largestArmyStatusLabel == null || players == null || players.isEmpty()) return;

        Player holder = null;
        Player candidate = null;
        int bestKnights = 0;

        for (Player player : players) {
            int knights = player.getKnightsPlayed();
            if (player.hasSpecialCard(SpecialCardType.LARGEST_ARMY)) {
                holder = player;
            }
            if (knights > bestKnights) {
                bestKnights = knights;
                candidate = player;
            }
        }

        if (holder != null) {
            largestArmyStatusLabel.setText("LARGEST ARMY: " + holder.getName()
                    + " (" + holder.getKnightsPlayed() + " knight)");
        } else if (candidate != null && bestKnights > 0) {
            largestArmyStatusLabel.setText("LARGEST ARMY: belum ada | terbanyak "
                    + candidate.getName() + " (" + bestKnights + "/3)");
        } else {
            largestArmyStatusLabel.setText("LARGEST ARMY: belum ada");
        }
    }

    public void renderResourceCards(Game game) {
        if (game == null) return;
        Player active = game.getActivePlayer();
        if (active == null) return;
        if (woodCount != null) woodCount.setText(String.valueOf(active.getResourceCount(ResourceType.WOOD)));
        if (brickCount != null) brickCount.setText(String.valueOf(active.getResourceCount(ResourceType.BRICK)));
        if (wheatCount != null) wheatCount.setText(String.valueOf(active.getResourceCount(ResourceType.WHEAT)));
        if (oreCount != null) oreCount.setText(String.valueOf(active.getResourceCount(ResourceType.ORE)));
        if (bananaCount != null) bananaCount.setText(String.valueOf(active.getResourceCount(ResourceType.BANANA)));
    }

    public void renderCurrentPlayer(Game game, boolean setupOrderPending) {
        if (game == null || currentPlayerLabel == null) return;
        if (setupOrderPending) {
            currentPlayerLabel.setText("Roll Order");
            currentPlayerLabel.setStyle("-fx-background-color: #fff3cd;");
            return;
        }
        Player active = game.getActivePlayer();
        if (active != null) {
            currentPlayerLabel.setText(active.getName() + "'s Turn");
            currentPlayerLabel.setStyle("-fx-background-color: " + sideCardBackgroundHex(active.getColor()) + ";");
        }
    }

    private void renderPlayerPanel(Game game, HBox panel, Player player, int rank) {
        VBox content = (VBox) panel.getChildren().get(1);
        HBox nameRow = (HBox) content.getChildren().get(0);
        HBox supplyRow = (HBox) content.getChildren().get(1);
        HBox resourceRow = (HBox) content.getChildren().get(2);

        content.getStyleClass().removeAll("side-card-red", "side-card-blue", "side-card-green", "side-card-orange");
        content.getStyleClass().add(sideCardStyleClass(player.getColor()));

        StackPane numberPane = (StackPane) nameRow.getChildren().get(0);
        Circle circle = (Circle) numberPane.getChildren().get(0);
        Label numberLabel = (Label) numberPane.getChildren().get(1);
        numberLabel.setText(String.valueOf(rank));

        Label nameLabel = (Label) nameRow.getChildren().get(1);
        nameLabel.setText(player.getName());

        Label vpLabel = (Label) nameRow.getChildren().get(3);
        int vp = game.getVPBreakdown(player).getPublicTotal();
        vpLabel.setText(vp + " VP");

        String colorHex = playerColorHex(player.getColor());
        circle.setFill(Color.web(colorHex));
        vpLabel.setStyle("-fx-background-color: " + colorHex + "; -fx-padding: 4 8; -fx-background-radius: 4; -fx-font-weight: bold; -fx-text-fill: white; -fx-font-size: 1.1em;");

        Region leftBar = (Region) panel.getChildren().get(0);
        leftBar.setStyle("-fx-background-color: " + colorHex + "; -fx-background-radius: 8 0 0 8;");

        var supply = game.getSupply(player);
        int roadsUsed = supply != null ? supply.getRoadsUsed() : 0;
        int roadsMax = supply != null ? supply.getMaxRoads() : 0;
        int postsUsed = supply != null ? supply.getPosPantauUsed() : 0;
        int postsMax = supply != null ? supply.getMaxPosPantau() : 0;
        int labsUsed = supply != null ? supply.getLaboratoriumUsed() : 0;
        int labsMax = supply != null ? supply.getMaxLaboratorium() : 0;

        Label pipeLabel = (Label) supplyRow.getChildren().get(0);
        Label postLabel = (Label) supplyRow.getChildren().get(1);
        Label labLabel = (Label) supplyRow.getChildren().get(2);
        pipeLabel.setText("PIPE: " + roadsUsed + "/" + roadsMax);
        postLabel.setText("POST: " + postsUsed + "/" + postsMax);
        labLabel.setText("LAB: " + labsUsed + "/" + labsMax);

        Label resLabel = (Label) resourceRow.getChildren().get(0);
        Label cardLabel = (Label) resourceRow.getChildren().get(1);
        resLabel.setText("Resources: " + player.getTotalResourceCount());
        cardLabel.setText("Dev Cards: " + player.getHandCards().size()
                + "   Road: " + player.getLongestRoadLength()
                + "   Army: " + player.getKnightsPlayed());
    }

    private void renderLongestRoadStatus(List<Player> players) {
        if (longestRoadStatusLabel == null || players == null || players.isEmpty()) return;

        Player holder = null;
        Player candidate = null;
        int bestLength = 0;

        for (Player player : players) {
            int length = player.getLongestRoadLength();
            if (player.hasSpecialCard(SpecialCardType.LONGEST_ROAD)) {
                holder = player;
            }
            if (length > bestLength) {
                bestLength = length;
                candidate = player;
            }
        }

        if (holder != null) {
            longestRoadStatusLabel.setText("LONGEST ROAD: " + holder.getName()
                    + " (" + holder.getLongestRoadLength() + " pipa)");
        } else if (candidate != null && bestLength > 0) {
            longestRoadStatusLabel.setText("LONGEST ROAD: belum ada | terpanjang "
                    + candidate.getName() + " (" + bestLength + "/5)");
        } else {
            longestRoadStatusLabel.setText("LONGEST ROAD: belum ada");
        }
    }

    private String playerColorHex(PlayerColor color) {
        if (color == null) return "#888888";
        return switch (color) {
            case RED -> "#c21a09";
            case BLUE -> "#305cde";
            case ORANGE -> "#ff7f00";
            case GREEN -> "#4fc978";
            default -> "#888888";
        };
    }

    private String sideCardStyleClass(PlayerColor color) {
        if (color == null) return "side-card-white";
        return switch (color) {
            case RED -> "side-card-red";
            case BLUE -> "side-card-blue";
            case GREEN -> "side-card-green";
            case ORANGE -> "side-card-orange";
            default -> "side-card-white";
        };
    }

    private String sideCardBackgroundHex(PlayerColor color) {
        if (color == null) return "#e8f3ec";
        return switch (color) {
            case RED -> "#ff7e70";
            case BLUE -> "#84a7ff";
            case GREEN -> "#8cffb2";
            case ORANGE -> "#f9a85c";
            default -> "#e8f3ec";
        };
    }
}
