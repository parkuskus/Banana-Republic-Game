package banana.republic.ui.dialog;

import java.io.IOException;
import java.net.URL;

import banana.republic.core.Game;
import banana.republic.ui.DialogController;
import banana.republic.ui.GameAwareController;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.ScrollPane;
import javafx.scene.effect.Effect;
import javafx.scene.layout.StackPane;

public class DialogHostService {

    private final Class<?> resourceAnchor;

    public DialogHostService(Class<?> resourceAnchor) {
        this.resourceAnchor = resourceAnchor;
    }

    public Object open(String fxmlName, StackPane overlay, Game game, Effect backgroundEffect,
                       Runnable closeCallback) throws IOException {
        if (overlay == null) return null;
        overlay.getChildren().clear();
        URL fxmlLocation = resourceAnchor.getResource("/fxml/" + fxmlName + ".fxml");
        if (fxmlLocation == null) return null;

        FXMLLoader loader = new FXMLLoader(fxmlLocation);
        Parent dialogUI = loader.load();
        Object controller = loader.getController();

        if (controller instanceof DialogController dialogController) {
            dialogController.setCloseHandler(() -> {
                overlay.setVisible(false);
                if (closeCallback != null) closeCallback.run();
            });
        }
        if (controller instanceof GameAwareController gameAwareController) {
            gameAwareController.setGame(game);
        }

        overlay.setAlignment(Pos.CENTER);
        if (dialogUI instanceof ScrollPane scrollPane) {
            scrollPane.setFitToWidth(true);
            scrollPane.setFitToHeight(true);
            scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        }
        overlay.getChildren().add(dialogUI);
        overlay.setVisible(true);
        overlay.toFront();
        return controller;
    }
}
