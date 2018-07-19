package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));
        primaryStage.setTitle("Конвертер валют v.1.0.2");
        primaryStage.getIcons().add(new Image("sample/badLogo.png"));
        primaryStage.setScene(new Scene(root, 769, 455));
        root.getStylesheets().add("sample/style.css");
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
        System.out.println("Hello world!");
    }
}
