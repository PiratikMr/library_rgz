package example.org.library;

import example.org.library.controller.MainController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Главный класс приложения «Личная Библиотека».
 */
public class LibraryApp extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(
                getClass().getResource("/example/org/library/view/main-view.fxml"));
        Parent root = fxmlLoader.load();

        Scene scene = new Scene(root, 1020, 680);
        scene.getStylesheets().add(
                getClass().getResource("/example/org/library/style.css").toExternalForm());

        // Настройка горячих клавиш
        MainController controller = fxmlLoader.getController();
        controller.setupHotkeys(scene);

        stage.setTitle("Личная Библиотека");
        stage.setMinWidth(800);
        stage.setMinHeight(500);
        stage.setScene(scene);
        stage.show();
    }
}
