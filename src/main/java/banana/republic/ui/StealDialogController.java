package banana.republic.ui;

import javafx.fxml.FXML;
import javafx.scene.layout.VBox;

import java.util.function.IntConsumer;

public class StealDialogController implements DialogController{

    private Runnable closeHandler;
    private IntConsumer stealHandler;

    @FXML
    private VBox player1;
    @FXML
    private VBox player2;
    @FXML
    private VBox player3;
    @FXML
    private VBox player4;

    private int idxDipilih = 0;
    private int idxPencuri = 1;

    public void initialize(){
        boolean isPencuriPlayer1 = (idxPencuri == 1);
        boolean isPencuriPlayer2 = (idxPencuri == 2);
        boolean isPencuriPlayer3 = (idxPencuri == 3);
        boolean isPencuriPlayer4 = (idxPencuri == 4);
        player1.setVisible(!isPencuriPlayer1);
        player1.setManaged(!isPencuriPlayer1);
        player2.setVisible(!isPencuriPlayer2);
        player2.setManaged(!isPencuriPlayer2);
        player3.setVisible(!isPencuriPlayer3);
        player3.setManaged(!isPencuriPlayer3);
        player4.setVisible(!isPencuriPlayer4);
        player4.setManaged(!isPencuriPlayer4);
    }
    @Override
    public void setCloseHandler(Runnable closeHandler) {
        this.closeHandler = closeHandler;
    }

    public void setStealHandler(IntConsumer stealHandler) {
        this.stealHandler = stealHandler;
    }

    @FXML
    private void closeDialog() {
        if (closeHandler != null) {
            closeHandler.run();
        }
    }

    @FXML
    private void selectPlayer1() {
        pilihPlayer(player1, player2, player3, player4);
        idxDipilih = 1;
    }
    @FXML
    private void selectPlayer2() {
        pilihPlayer(player2, player1, player3, player4);
        idxDipilih = 2;
    }
    @FXML
    private void selectPlayer3() {
        pilihPlayer(player3, player1, player2, player4);
        idxDipilih = 3;
    }

    @FXML
    private void selectPlayer4() {
        pilihPlayer(player4, player1, player2, player3);
        idxDipilih = 4;
    }

    @FXML
    private void confirmSteal() {
        if (stealHandler != null) {
            stealHandler.accept(idxDipilih);
        }
        closeDialog();
    }

    private void pilihPlayer(VBox playerDipilih, VBox playerLain, VBox playerLain2, VBox playerLain3) {
        playerLain.getStyleClass().remove("card-selected");
        playerLain2.getStyleClass().remove("card-selected");
        playerLain3.getStyleClass().remove("card-selected");
        if (!playerDipilih.getStyleClass().contains("card-selected")) {
            playerDipilih.getStyleClass().add("card-selected");
        }
    }
}