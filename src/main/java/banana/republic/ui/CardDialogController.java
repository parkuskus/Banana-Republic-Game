package banana.republic.ui;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class CardDialogController implements DialogController {

    private Runnable closeHandler;
    @FXML
    private VBox cardPurple;
    @FXML
    private VBox cardGreen;
    @FXML
    private VBox cardOrange;
    @FXML
    private VBox cardGray;

    @Override
    public void setCloseHandler(Runnable closeHandler) {
        this.closeHandler = closeHandler;
    }

    @FXML
    private void closeDialog() {
        if (closeHandler != null) {
            closeHandler.run();
        }
    }


    @FXML
    private void togglePurple() {
        toggleCard(cardPurple);
    }
    @FXML
    private void toggleOrange() {
        toggleCard(cardOrange);
    }
    @FXML
    private void toggleGreen() {
        toggleCard(cardGreen);
    }
    @FXML
    private void toggleGray() {
        toggleCard(cardGray);
    }


    // helper
    private void toggleCard(VBox card) {
        if (!card.getStyleClass().contains("card-selected")) {
            card.getStyleClass().add("card-selected");
        } else{
            card.getStyleClass().remove("card-selected");
        }
    }
}