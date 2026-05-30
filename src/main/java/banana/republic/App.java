package banana.republic;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class App extends Application{

    private static Scene scene;

    @Override
    public void start(Stage primaryStage) throws Exception {
        Font.loadFont(getClass().getResourceAsStream("/fonts/Marcellus-Regular.ttf"), 14);
        Font.loadFont(getClass().getResourceAsStream("/fonts/RussoOne-Regular.ttf"), 14);

        Parent root = loadFXML("main");
        scene = new Scene(root);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/css/style.css")).toExternalForm());

        primaryStage.setScene(scene);

        primaryStage.setTitle("Banana Republic");
        primaryStage.setFullScreen(true);
        primaryStage.setFullScreenExitHint("");
        primaryStage.maximizedProperty().addListener((observable, wasMaximized, isNowMaximized) -> {
            if (isNowMaximized) {
                Platform.runLater(() -> {
                    // Paksa masuk ke Full Screen sejati
                    primaryStage.setFullScreen(true);
                });
            }
        });

        primaryStage.show();
    }

    public static void setRoot(String fxml) throws IOException {
        Parent newRoot = loadFXML(fxml);
        scene.setRoot(newRoot);
    }

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("/fxml/" + fxml + ".fxml"));
        return fxmlLoader.load();
    }

    public static void main(String[] args) {
        launch(args);
    }
}