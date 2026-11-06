import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.nio.file.Path;

public class PhotoSorterApp extends Application {

    private PhotoManager photoManagerCurrent;

    @Override
    public void start(Stage stage) {

        stage.setTitle("PhotoSorter");





        ImageView imageView = new ImageView();

        imageView.setFitWidth(500);
        imageView.setFitHeight(400);
        imageView.setPreserveRatio(true);
        VBox root = new VBox();
        root.setAlignment(Pos.CENTER);
        root.setSpacing(15);
        Label welcomeLabel = new Label("Welcome to PhotoSorter");
        Label remainingLabel = new Label("Photos remaining: 0");
        Button keepButton = new Button("Keep");
        Button deleteButton = new Button("Delete");
        keepButton.setDisable(true);
        deleteButton.setDisable(true);

        Button chooseFolderButton = new Button("Choose Folder");
        chooseFolderButton.setOnAction(event -> {

            DirectoryChooser directoryChooser = new DirectoryChooser();
            File selectedFolder = directoryChooser.showDialog(stage);
            if (selectedFolder == null) {
                return;
            }

            Path folder = selectedFolder.toPath();
            photoManagerCurrent = new PhotoManager(folder);
            photoManagerCurrent.openFolder();
            photoManagerCurrent.createSortingFolders();
            Path currentPhoto = photoManagerCurrent.getCurrentPhoto();
            remainingLabel.setText("Photos remaining: " + photoManagerCurrent.getImageFiles().size());
            if (currentPhoto != null) {
                Image image = new Image(currentPhoto.toUri().toString());
                imageView.setImage(image);
                keepButton.setDisable(false);
                deleteButton.setDisable(false);
                welcomeLabel.setText("Welcome to PhotoSorter");
            } else {

                imageView.setImage(null);
                welcomeLabel.setText("No photos available");
                keepButton.setDisable(true);
                deleteButton.setDisable(true);

            }

            root.getChildren().setAll(
                    welcomeLabel,
                    chooseFolderButton,
                    remainingLabel,
                    imageView,
                    keepButton,
                    deleteButton
            );



        });



        keepButton.setOnAction(event -> {
            photoManagerCurrent.movePhoto(true);
            remainingLabel.setText("Photos remaining: " + photoManagerCurrent.getImageFiles().size());
            Path nextPhoto = photoManagerCurrent.getCurrentPhoto();

            if (nextPhoto != null) {
                Image nextImage = new Image(nextPhoto.toUri().toString());
                imageView.setImage(nextImage);
            } else {
                imageView.setImage(null);
                welcomeLabel.setText("All photos sorted!");
                keepButton.setDisable(true);
                deleteButton.setDisable(true);

            }
        });
        deleteButton.setOnAction(event -> {
            photoManagerCurrent.movePhoto(false);
            remainingLabel.setText("Photos remaining: " + photoManagerCurrent.getImageFiles().size());
            Path nextPhoto = photoManagerCurrent.getCurrentPhoto();

            if (nextPhoto != null) {
                Image nextImage = new Image(nextPhoto.toUri().toString());
                imageView.setImage(nextImage);
            } else {
                imageView.setImage(null);
                welcomeLabel.setText("All photos sorted!");
                keepButton.setDisable(true);
                deleteButton.setDisable(true);


            }
        });

        root.getChildren().add(welcomeLabel);
        root.getChildren().add(chooseFolderButton);


        Scene scene = new Scene(root, 800, 600);
        stage.setScene(scene);


        stage.show();

    }

    public static void main(String[] args) {
        launch(args);
    }
}