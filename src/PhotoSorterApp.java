import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.nio.file.Path;
import java.nio.file.Paths;

public class PhotoSorterApp extends Application {

    @Override
    public void start(Stage stage) {

        stage.setTitle("PhotoSorter");
        Path folder = Paths.get("/Users/conorlogan/Desktop/PhotoSorterTest");
        PhotoManager photoManagerCurrent = new PhotoManager(folder);
        photoManagerCurrent.openFolder();
        photoManagerCurrent.createSortingFolders();
        Path currentPhoto = photoManagerCurrent.getCurrentPhoto();
        VBox root = new VBox();
        root.setAlignment(Pos.CENTER);
        root.setSpacing(15);
        Label welcomeLabel = new Label("Welcome to PhotoSorter");
        Button keepButton = new Button("Keep");
        Button deleteButton = new Button("Delete");
        keepButton.setOnAction(event -> photoManagerCurrent.movePhoto(true));
        deleteButton.setOnAction(event -> photoManagerCurrent.movePhoto(false));
        root.getChildren().add(welcomeLabel);
        root.getChildren().add(keepButton);
        root.getChildren().add(deleteButton);
        Scene scene = new Scene(root, 800, 600);
        stage.setScene(scene);


        stage.show();

    }

    public static void main(String[] args) {
        launch(args);
    }
}