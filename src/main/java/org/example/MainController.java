package org.example;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class MainController {
    @FXML
    private Label messageLabel; // Make sure this name matches the fx:id in FXML

    @FXML
    private Button button;

    @FXML
    private void handleButtonClick() {
        messageLabel.setText("Button clicked!");
    }
}
