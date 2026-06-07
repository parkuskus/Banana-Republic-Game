package banana.republic.ui.dialog;

import java.util.Optional;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Window;

/**
 * Centralizes JavaFX dialog creation so controllers do not duplicate alert
 * plumbing.
 */
public class UiDialogs {

    public void showError(String message) {
        show(Alert.AlertType.ERROR, "Error", message);
    }

    public void showInfo(String message) {
        show(Alert.AlertType.INFORMATION, "Info", message);
    }

    public boolean confirm(String title, String header, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(message);
        centerOnShown(alert);
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    private void show(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        centerOnShown(alert);
        alert.showAndWait();
    }

    private void centerOnShown(Alert alert) {
        alert.setOnShown(event -> {
            Window window = alert.getDialogPane().getScene().getWindow();
            if (window != null) {
                window.centerOnScreen();
            }
        });
    }
}
