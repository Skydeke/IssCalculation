package main.java;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {

    @Override
    public void start(final Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader();
        final Controller controller = new Controller();
        loader.setController(controller);
        Parent root = loader.load(Main.class.getResourceAsStream("/mainUI.fxml"));
        stage.setTitle("IssCalculate - Kreisbahnsimulationen");
        Scene s = new Scene(root);
        stage.setScene(s);
        stage.show();
    }
}
