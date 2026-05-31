package banana.republic.ui;


import banana.republic.App;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ResourceBundle;
/**
 * Trade dialog controller.
 * */
public class TradeDialogController implements Initializable, DialogController {
    @FXML
    private Label tabDomestic;
    @FXML
    private Label tabMaritime;
    @FXML
    private VBox offerBox;


    @FXML
    private Button btnGiveWoodMinus, btnGiveBrickMinus, btnGiveWheatMinus,btnGiveOreMinus, btnGiveBananaMinus;
    @FXML
    private Label lblGiveWoodVal, lblGiveBrickVal, lblGiveWheatVal, lblGiveOreVal, lblGiveBananaVal;
    @FXML
    private Button btnGiveWoodPlus, btnGiveBrickPlus, btnGiveWheatPlus,btnGiveOrePlus, btnGiveBananaPlus;

    @FXML
    private Button btnReceiveWoodMinus, btnReceiveBrickMinus, btnReceiveWheatMinus,btnReceiveOreMinus, btnReceiveBananaMinus;
    @FXML
    private Label lblReceiveWoodVal, lblReceiveBrickVal, lblReceiveWheatVal, lblReceiveOreVal, lblReceiveBananaVal;
    @FXML
    private Button btnReceiveWoodPlus, btnReceiveBrickPlus, btnReceiveWheatPlus,btnReceiveOrePlus, btnReceiveBananaPlus;

    private Runnable closeHandler;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        int maxItems = 20;
        setupStepper(btnGiveWoodMinus, btnGiveWoodPlus, lblGiveWoodVal, 0, maxItems);
        setupStepper(btnGiveWheatMinus, btnGiveWheatPlus, lblGiveWheatVal, 0, maxItems);
        setupStepper(btnGiveBrickMinus, btnGiveBrickPlus, lblGiveBrickVal, 0, maxItems);
        setupStepper(btnGiveOreMinus, btnGiveOrePlus, lblGiveOreVal, 0, maxItems);
        setupStepper(btnGiveBananaMinus, btnGiveBananaPlus, lblGiveBananaVal, 0, maxItems);

        setupStepper(btnReceiveWoodMinus, btnReceiveWoodPlus, lblReceiveWoodVal, 0, maxItems);
        setupStepper(btnReceiveWheatMinus, btnReceiveWheatPlus, lblReceiveWheatVal, 0, maxItems);
        setupStepper(btnReceiveBrickMinus, btnReceiveBrickPlus, lblReceiveBrickVal, 0, maxItems);
        setupStepper(btnReceiveOreMinus, btnReceiveOrePlus, lblReceiveOreVal, 0, maxItems);
        setupStepper(btnReceiveBananaMinus, btnReceiveBananaPlus, lblReceiveBananaVal, 0, maxItems);
    }

    // untuk handle plus minus resource
    private void setupStepper(Button btnMinus, Button btnPlus, Label lblValue, int min, int max) {
        // Untuk tombol plus
        btnPlus.setOnAction(event -> {
            int currentValue = Integer.parseInt(lblValue.getText());
            if (currentValue < max) {
                lblValue.setText(String.valueOf(currentValue + 1));
            }
        });

        // Untuk tombol minus
        btnMinus.setOnAction(event -> {
            int currentValue = Integer.parseInt(lblValue.getText());
            if (currentValue > min) {
                lblValue.setText(String.valueOf(currentValue - 1));
            }
        });
    }

    // untuk titip perintah close dialog
    @Override
    public void setCloseHandler(Runnable closeHandler) {
        this.closeHandler = closeHandler;
    }

    // tutup dialog
    @FXML
    private void closeDialog() {
        if (closeHandler != null) {
            closeHandler.run(); // Jalankan perintah tutup!
        }
    }

    // pindah ke tab domestic trade
    @FXML
    private void switchToDomestic() {
        ubahGayaTab(tabDomestic, tabMaritime);
        offerBox.setVisible(true);
    }

    // pindah ke tab maritime trade
    @FXML
    private void switchToMaritime() {
        ubahGayaTab(tabMaritime, tabDomestic);
        offerBox.setVisible(false);
    }

    // helper
    private void ubahGayaTab(Label tabAktif, Label tabInaktif) {
        // Berikan aktif ke tab yang diklik
        tabAktif.getStyleClass().remove("tab-inactive");
        if (!tabAktif.getStyleClass().contains("tab-active")) {
            tabAktif.getStyleClass().add("tab-active");
        }

        // Kembalikan tab sebelahnya ke gaya inaktif
        tabInaktif.getStyleClass().remove("tab-active");
        if (!tabInaktif.getStyleClass().contains("tab-inactive")) {
            tabInaktif.getStyleClass().add("tab-inactive");
        }
    }
}
