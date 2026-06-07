package banana.republic.ui.dialog;

import java.util.OptionalInt;

import javafx.scene.control.TextInputDialog;

public class NumberInputDialogService {

    public OptionalInt requestInt(String title, String headerText, String defaultValue) {
        TextInputDialog dialog = new TextInputDialog(defaultValue);
        dialog.setTitle(title);
        dialog.setHeaderText(headerText);
        return dialog.showAndWait()
                .map(String::trim)
                .flatMap(this::parseInt)
                .map(OptionalInt::of)
                .orElseGet(OptionalInt::empty);
    }

    private java.util.Optional<Integer> parseInt(String value) {
        try {
            return java.util.Optional.of(Integer.parseInt(value));
        } catch (NumberFormatException e) {
            return java.util.Optional.empty();
        }
    }
}
