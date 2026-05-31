package banana.republic.ui;

import javafx.fxml.FXML;

public class VictoryDialogController implements DialogController{

    private Runnable closeHandler;
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
    private void handleRevealAndWin() {
        System.out.println("Deklarasi kemenangan");
        // TODO: sambung ke backend
        closeDialog();
    }
}