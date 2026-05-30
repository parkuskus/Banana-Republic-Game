package banana.republic.ui;

import banana.republic.App;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.paint.Color;
import javafx.scene.layout.StackPane;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class GameController implements Initializable {

    @FXML
    private StackPane mainGameRoot;
    @FXML
    private StackPane tradeDialogOverlay;
    @FXML
    private StackPane hexdesert;
    @FXML
    private StackPane harborLV1; //left vertical (upper)
    @FXML
    private StackPane harborLV2; //left vertical (lower)
    @FXML
    private StackPane harborLU;  //left up
    @FXML
    private StackPane harborLD;  //left down
    @FXML
    private StackPane harborRV;  //right vertical
    @FXML
    private StackPane harborRU1; //right up
    @FXML
    private StackPane harborRU2; //right up (lower)
    @FXML
    private StackPane harborRD1; //right down (inner)
    @FXML
    private StackPane harborRD2; //right down (outer)

    private boolean canAddRoad = true;

    private Image gambarAnchorGlobal = null;

    private GaussianBlur blurEffect = new GaussianBlur(10);

    // helpers
    public void setCanAddRoad(boolean canAddRoad) {
        this.canAddRoad = canAddRoad;
    }

    public boolean isCanAddRoad() {
        return this.canAddRoad;
    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            URL imageUrl = getClass().getResource("/icons/anchor.png");
            if (imageUrl != null) {
                gambarAnchorGlobal = new Image(imageUrl.toExternalForm());
            } else {
                gambarAnchorGlobal = new Image("file:icons/anchor.png");
            }
        } catch (Exception e) {
            System.out.println("Gagal memuat gambar jangkar: " + e.getMessage());
        }

        if (hexdesert != null) {
            pasangEventKlik(hexdesert);
            pasangAnchorSudut(harborLU, 5, 0, "3:1");
            pasangAnchorSudut(harborRU1, 0, 1, "2:1 Banana");
            pasangAnchorSudut(harborRU2, 1, 2, "3:1");
            pasangAnchorSudut(harborRV, 2, 3, "2:1 Ore");
            pasangAnchorSudut(harborRD2, 2, 3, "3:1");
            pasangAnchorSudut(harborRD1, 3, 4, "2:1 Wheat");
            pasangAnchorSudut(harborLD, 3, 4, "2:1 Brick");
            pasangAnchorSudut(harborLV2, 4, 5, "3:1");
            pasangAnchorSudut(harborLV1, 5, 0, "2:1 Wood");
        }
    }

    public void pasangEventKlik(StackPane hexTile) {
        hexTile.setPickOnBounds(false);

        double w = hexTile.getPrefWidth();
        double h = hexTile.getPrefHeight();

        Polygon hitbox = new Polygon(
                w / 2, 0,
                w, h * 0.25,
                w, h * 0.75,
                w / 2, h,
                0, h * 0.75,
                0, h * 0.25
        );
        hitbox.setFill(Color.TRANSPARENT);
        hexTile.getChildren().add(hitbox);

        hitbox.setOnMouseClicked(event -> {
            if (!canAddRoad) {
                return;
            }

            double clickX = event.getX();
            double clickY = event.getY();
            int sisiTerdekat = hitungSisiTerdekatAngle(w, h, clickX, clickY);
            warnaiSisiHexagon(hexTile, sisiTerdekat, Color.BLUE);
        });
    }

    private int hitungSisiTerdekatAngle(double w, double h, double cx, double cy) {
        double centerX = w / 2;
        double centerY = h / 2;

        double angle = Math.toDegrees(Math.atan2(cy - centerY, cx - centerX));

        if (angle >= -90 && angle < -30) {
            return 0; // kanan atas
        }
        else if (angle >= -30 && angle < 30) {
            return 1; // kanan tegak
        }
        else if (angle >= 30 && angle < 90) {
            return 2; //kanan bawah
        }
        else if (angle >= 90 && angle < 150) {
            return 3; // kiri bawah
        }
        else if (angle >= 150 || angle < -150) {
            return 4; // kiri tegak
        }
        else {
            // angle >= -150 && angle < -90
            return 5; // kiri atas
        }
    }

    private void warnaiSisiHexagon(StackPane hexTile, int indeksSisi, Color warna) {
        double w = hexTile.getPrefWidth();
        double h = hexTile.getPrefHeight();

        double[][] titik = {
                {w / 2, 0},
                {w, h * 0.25},
                {w, h * 0.75},
                {w / 2, h},
                {0, h * 0.75},
                {0, h * 0.25}
        };

        double startX = titik[indeksSisi][0];
        double startY = titik[indeksSisi][1];
        int sudutBerikutnya = (indeksSisi + 1) % 6;
        double endX = titik[sudutBerikutnya][0];
        double endY = titik[sudutBerikutnya][1];

        double ketebalanGaris = 8.0;

        Line garisPinggir = new Line(startX, startY, endX, endY);
        garisPinggir.setStroke(warna);
        garisPinggir.setStrokeWidth(ketebalanGaris);
        garisPinggir.setStrokeLineCap(StrokeLineCap.ROUND);

        garisPinggir.setManaged(false);
        garisPinggir.setMouseTransparent(true);

        hexTile.getChildren().add(garisPinggir);

        garisPinggir.toFront();
        hexTile.toFront();
    }


    public void pasangAnchorSudut(StackPane hexTile, int idx1, int idx2, String teksToko) {
        if (hexTile == null) {
            System.out.println("Peringatan: Ada StackPane harbor yang belum di-link (null) di Scene Builder!");
            return;
        }

        // Ambil group yang menampung seluruh heksagon
        javafx.scene.Group parentMap = (javafx.scene.Group) hexTile.getParent();

        double w = hexTile.getPrefWidth();
        double h = hexTile.getPrefHeight();

        // Koordinat titik sudut heksagon
        double[][] titikHex = {
                {w / 2, 0},           // 0: Atas
                {w, h * 0.25},        // 1: Kanan Atas
                {w, h * 0.75},        // 2: Kanan Bawah
                {w / 2, h},           // 3: Bawah
                {0, h * 0.75},        // 4: Kiri Bawah
                {0, h * 0.25}         // 5: Kiri Atas
        };

        // Buat 6 titik jangkar
        for (int i = 0; i < 6; i++) {
            if (i != idx1 && i != idx2){
                continue;
            }
            StackPane wadahSudut = new StackPane();
            Circle anchorCircle = new Circle(12);
            anchorCircle.setFill(Color.WHITE);
            anchorCircle.setStroke(Color.BLACK);
            anchorCircle.setStrokeWidth(1);

            ImageView iconView = new ImageView();
            if (gambarAnchorGlobal != null) {
                iconView.setImage(gambarAnchorGlobal);
                iconView.setFitWidth(14);
                iconView.setFitHeight(14);
                iconView.setPreserveRatio(true);
            }

            wadahSudut.getChildren().addAll(anchorCircle, iconView);

            // Hitung posisi absolut di layar (Layout Heksagon + Koordinat Lokal Sudut)
            double absoluteX = hexTile.getLayoutX() + titikHex[i][0];
            double absoluteY = hexTile.getLayoutY() + titikHex[i][1];

            // geser posisi wadah sudut biar pas di tengah sudut
            wadahSudut.setLayoutX(absoluteX - 12);
            wadahSudut.setLayoutY(absoluteY - 12);

            parentMap.getChildren().add(wadahSudut);
        }
        pasangTokoSisi(hexTile, idx1, teksToko);
    }

    // pasang label toko
    public void pasangTokoSisi(StackPane hexTile, int indeksSisi, String teksToko) {
        if (hexTile == null) return;

        javafx.scene.Group parentMap = (javafx.scene.Group) hexTile.getParent();
        double w = hexTile.getPrefWidth();
        double h = hexTile.getPrefHeight();

        double[][] titikLokal = {
                {w / 2, 0},           // 0: Atas
                {w, h * 0.25},        // 1: Kanan Atas
                {w, h * 0.75},        // 2: Kanan Bawah
                {w / 2, h},           // 3: Bawah
                {0, h * 0.75},        // 4: Kiri Bawah
                {0, h * 0.25}         // 5: Kiri Atas
        };

        double startX = titikLokal[indeksSisi][0];
        double startY = titikLokal[indeksSisi][1];
        int sudutBerikutnya = (indeksSisi + 1) % 6;
        double endX = titikLokal[sudutBerikutnya][0];
        double endY = titikLokal[sudutBerikutnya][1];

        double midX = (startX + endX) / 2.0;
        double midY = (startY + endY) / 2.0;

        double dx = midX - (w / 2.0);
        double dy = midY - (h / 2.0);
        double distance = Math.sqrt(dx * dx + dy * dy);

        double jarakDorong = 50.0;
        double pushX = (dx / distance) * jarakDorong;
        double pushY = (dy / distance) * jarakDorong;

        double absoluteX = hexTile.getLayoutX() + midX + pushX;
        double absoluteY = hexTile.getLayoutY() + midY + pushY;

        // Bikin Hbox untuk toko
        javafx.scene.layout.HBox tokoBox = new javafx.scene.layout.HBox(3);
        tokoBox.setAlignment(javafx.geometry.Pos.CENTER);
        tokoBox.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-border-color: black; -fx-border-radius: 12; -fx-padding: 2 5 2 5;");
        tokoBox.setMaxSize(javafx.scene.layout.Region.USE_PREF_SIZE, javafx.scene.layout.Region.USE_PREF_SIZE);

        javafx.scene.control.Label labelToko = new javafx.scene.control.Label(teksToko);
        labelToko.setStyle("-fx-font-family: 'Russo One'; -fx-font-size: 10px;");

        ImageView iconView = new ImageView();
        if (gambarAnchorGlobal != null) {
            iconView.setImage(gambarAnchorGlobal);
            iconView.setFitWidth(12);
            iconView.setFitHeight(12);
        }
        tokoBox.getChildren().addAll(labelToko, iconView);

        // Posisikan label di map
        tokoBox.setLayoutX(absoluteX);
        tokoBox.setLayoutY(absoluteY);

        tokoBox.translateXProperty().bind(tokoBox.widthProperty().divide(-2));
        tokoBox.translateYProperty().bind(tokoBox.heightProperty().divide(-2));

        // Letakkan HBox di Group Utama
        parentMap.getChildren().add(tokoBox);
    }

    @FXML
    private void openDialog(String fxmlName, StackPane dialogOverlay) throws IOException {
        if (dialogOverlay != null) {
            // bersihkan overlay sebelumnya jika ada
            dialogOverlay.getChildren().clear();

            String pathFxml = "/fxml/" + fxmlName + ".fxml";
            URL fxmlLocation = getClass().getResource(pathFxml);

            FXMLLoader loader = new FXMLLoader(fxmlLocation);
            javafx.scene.Parent dialogUI = loader.load();

            TradeDialogController tradeController = loader.getController();

            // Titipkan perintah apa yang harus dilakukan saat tombol Cancel/X ditekan
            tradeController.setCloseHandler(() -> {
                dialogOverlay.setVisible(false); // Sembunyikan dialog
                if (mainGameRoot != null) {
                    mainGameRoot.setEffect(null); // Hilangkan blur dari papan
                }
            });

            // Masukkan dialog ke dalam overlay
            dialogOverlay.getChildren().add(dialogUI);

            // Tampilkan overlay
            dialogOverlay.setVisible(true);
            dialogOverlay.toFront();

            // Beri efek blur
            if (mainGameRoot != null) {
                mainGameRoot.setEffect(blurEffect);
            }
        } else {
            System.out.println("ERROR: dialogOverlay bernilai null!");
        }
    }

    @FXML
    private void toTrade() throws IOException {
        openDialog("trade", tradeDialogOverlay);
    }
}