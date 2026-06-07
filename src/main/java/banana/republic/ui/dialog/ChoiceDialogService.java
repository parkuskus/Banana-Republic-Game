package banana.republic.ui.dialog;

import java.util.Collection;
import java.util.Optional;

import javafx.scene.control.ChoiceDialog;

public class ChoiceDialogService {

    public <T> Optional<T> choose(String title, String headerText, String contentText, Collection<T> choices) {
        if (choices == null || choices.isEmpty()) return Optional.empty();
        T first = choices.iterator().next();
        ChoiceDialog<T> dialog = new ChoiceDialog<>(first, choices);
        dialog.setTitle(title);
        dialog.setHeaderText(headerText);
        dialog.setContentText(contentText);
        return dialog.showAndWait();
    }
}
