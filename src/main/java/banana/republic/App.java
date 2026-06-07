package banana.republic;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
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
    private static StackPane viewport;
    private static Group scaledContent;
    private static Parent currentRoot;

    @Override
    public void start(Stage primaryStage) throws Exception {
        stage = primaryStage;

        Font.loadFont(getClass().getResourceAsStream("/fonts/Marcellus-Regular.ttf"), 14);
        Font.loadFont(getClass().getResourceAsStream("/fonts/RussoOne-Regular.ttf"), 14);

        viewport = new StackPane();
        viewport.setAlignment(Pos.CENTER);
        viewport.getStyleClass().add("app-viewport");

        // Scene boleh mengikuti ukuran window/fullscreen aktual; konten FXML
        // referensi tetap dipasang di viewport yang melakukan uniform scaling.
        scene = new Scene(viewport, DESIGN_WIDTH, DESIGN_HEIGHT);
        scene.getStylesheets().add(
            Objects.requireNonNull(getClass().getResource("/css/style.css")).toExternalForm()
        );
        scene.widthProperty().addListener((obs, oldValue, newValue) -> applyScaling(currentRoot));
        scene.heightProperty().addListener((obs, oldValue, newValue) -> applyScaling(currentRoot));

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

        installRoot(loadFXML("main"));
    }

    /**
     * Menghitung dan menerapkan scale pada root node agar konten desain referensi
     * (DESIGN_WIDTH x DESIGN_HEIGHT) pas mengisi layar penuh secara proporsional.
     * Scale dihitung agar tidak ada konten yang terpotong (letterbox approach).
     */
    public static void applyScaling(Parent root) {
        try {
            if (root == null || scaledContent == null) return;

            double screenW = scene != null && scene.getWidth() > 0 ? scene.getWidth() : fallbackScreenWidth();
            double screenH = scene != null && scene.getHeight() > 0 ? scene.getHeight() : fallbackScreenHeight();

            double scaleX = screenW / DESIGN_WIDTH;
            double scaleY = screenH / DESIGN_HEIGHT;

            // Pilih scale terkecil agar tidak ada yang terpotong (uniform scaling)
            double scale = Math.min(scaleX, scaleY);

            scaledContent.setScaleX(scale);
            scaledContent.setScaleY(scale);

            // Paksa root menggunakan ukuran desain referensi sebagai layout size
            if (root instanceof Region) {
                Region region = (Region) root;
                region.setMinWidth(DESIGN_WIDTH);
                region.setMinHeight(DESIGN_HEIGHT);
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
        installRoot(newRoot);
    }

    public static FXMLLoader getLoader(String fxml) throws IOException {
        return new FXMLLoader(App.class.getResource("/fxml/" + fxml + ".fxml"));
    }

    /**
     * Ganti root scene dari FXMLLoader eksternal, lalu terapkan scaling secara otomatis.
     */
    public static void setRootFromLoader(Parent root) {
        installRoot(root);
    }

    private static void installRoot(Parent root) {
        currentRoot = root;
        configureDesignRegion(root);
        scaledContent = new Group(root);
        if (viewport != null) {
            viewport.getChildren().setAll(scaledContent);
        }
        Platform.runLater(() -> applyScaling(root));
    }

    private static void configureDesignRegion(Parent root) {
        if (root instanceof Region region) {
            region.setMinSize(DESIGN_WIDTH, DESIGN_HEIGHT);
            region.setPrefSize(DESIGN_WIDTH, DESIGN_HEIGHT);
            region.setMaxSize(DESIGN_WIDTH, DESIGN_HEIGHT);
        }
    }

    private static double fallbackScreenWidth() {
        Rectangle2D bounds = stage != null && stage.isFullScreen()
            ? Screen.getPrimary().getBounds()
            : Screen.getPrimary().getVisualBounds();
        return bounds.getWidth();
    }

    private static double fallbackScreenHeight() {
        Rectangle2D bounds = stage != null && stage.isFullScreen()
            ? Screen.getPrimary().getBounds()
            : Screen.getPrimary().getVisualBounds();
        return bounds.getHeight();
    }

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("/fxml/" + fxml + ".fxml"));
        return fxmlLoader.load();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
