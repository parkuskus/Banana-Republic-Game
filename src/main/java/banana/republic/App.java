package banana.republic;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Region;
import javafx.scene.text.Font;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class App extends Application {

    // Resolusi referensi desain (ukuran yang semua FXML dirancang untuknya)
    private static final double DESIGN_WIDTH  = 1920.0;
    private static final double DESIGN_HEIGHT = 1080.0;

    private static Scene scene;
    private static Stage stage;

    @Override
    public void start(Stage primaryStage) throws Exception {
        stage = primaryStage;

        Font.loadFont(getClass().getResourceAsStream("/fonts/Marcellus-Regular.ttf"), 14);
        Font.loadFont(getClass().getResourceAsStream("/fonts/RussoOne-Regular.ttf"), 14);

        Parent root = loadFXML("main");

        // Scene dibuat sebesar resolusi referensi desain
        scene = new Scene(root, DESIGN_WIDTH, DESIGN_HEIGHT);
        scene.getStylesheets().add(
            Objects.requireNonNull(getClass().getResource("/css/style.css")).toExternalForm()
        );

        primaryStage.setScene(scene);
        primaryStage.setTitle("Banana Republic");
        primaryStage.setFullScreen(true);
        primaryStage.setFullScreenExitHint("");

        // Paksa kembali ke fullscreen jika user meng-maximize window
        primaryStage.maximizedProperty().addListener((obs, wasMax, isNowMax) -> {
            if (isNowMax) {
                Platform.runLater(() -> primaryStage.setFullScreen(true));
            }
        });

        primaryStage.show();

        // Terapkan scaling ke root awal setelah stage ditampilkan
        Platform.runLater(() -> applyScaling(root));
    }

    /**
     * Menghitung dan menerapkan scale pada root node agar konten desain referensi
     * (DESIGN_WIDTH x DESIGN_HEIGHT) pas mengisi layar penuh secara proporsional.
     * Scale dihitung agar tidak ada konten yang terpotong (letterbox approach).
     */
    public static void applyScaling(Parent root) {
        try {
            double screenW;
            double screenH;

            if (stage != null && stage.isFullScreen()) {
                // Gunakan ukuran layar fisik penuh saat fullscreen
                Rectangle2D fullBounds = Screen.getPrimary().getBounds();
                screenW = fullBounds.getWidth();
                screenH = fullBounds.getHeight();
            } else {
                // Gunakan area visual (tidak termasuk taskbar) saat windowed
                Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
                screenW = bounds.getWidth();
                screenH = bounds.getHeight();
            }

            double scaleX = screenW / DESIGN_WIDTH;
            double scaleY = screenH / DESIGN_HEIGHT;

            // Pilih scale terkecil agar tidak ada yang terpotong (uniform scaling)
            double scale = Math.min(scaleX, scaleY);

            root.setScaleX(scale);
            root.setScaleY(scale);

            // Paksa root menggunakan ukuran desain referensi sebagai layout size
            if (root instanceof Region) {
                Region region = (Region) root;
                region.setPrefWidth(DESIGN_WIDTH);
                region.setPrefHeight(DESIGN_HEIGHT);
                region.setMaxWidth(DESIGN_WIDTH);
                region.setMaxHeight(DESIGN_HEIGHT);
            }
        } catch (Exception ignored) {
            // Jika scaling gagal, biarkan layout berjalan dengan ukuran defaultnya
        }
    }

    /**
     * Ganti root scene ke FXML yang ditentukan, lalu terapkan scaling secara otomatis.
     */
    public static void setRoot(String fxml) throws IOException {
        Parent newRoot = loadFXML(fxml);
        scene.setRoot(newRoot);
        Platform.runLater(() -> applyScaling(newRoot));
    }

    public static FXMLLoader getLoader(String fxml) throws IOException {
        return new FXMLLoader(App.class.getResource("/fxml/" + fxml + ".fxml"));
    }

    /**
     * Ganti root scene dari FXMLLoader eksternal, lalu terapkan scaling secara otomatis.
     */
    public static void setRootFromLoader(Parent root) {
        scene.setRoot(root);
        Platform.runLater(() -> applyScaling(root));
    }

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("/fxml/" + fxml + ".fxml"));
        return fxmlLoader.load();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
