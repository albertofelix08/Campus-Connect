package campusconnect;

import javafx.application.Application;
import campusconnect.ui.Theme;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // TODO: Load login screen first
        Parent root = FXMLLoader.load(getClass().getResource("view/login.fxml"));
        Scene scene = new Scene(root, 900, 600);
        Theme.apply(scene);
        primaryStage.setTitle("CampusConnect — Polished Edition");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
