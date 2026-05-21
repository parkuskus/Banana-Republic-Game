package banana.republic;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class MainController {
    @FXML
    private Label messageLabel; 

    @FXML
    private Button button;

    @FXML
    private void handleButtonClick() {
        messageLabel.setText("Button clicked!");
    }
}
