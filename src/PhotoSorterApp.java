
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.nio.file.Path;

public class PhotoSorterApp extends Application {

    private PhotoManager photoManagerCurrent;

    @Override
    public void start(Stage stage) {

        stage.setTitle("PicFlick");
        stage.getIcons().add(
                new Image(getClass().getResourceAsStream("/images/picflick-logo.png"))
        );

        // Photo display
        ImageView imageView = new ImageView();
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);

        // Custom panel that resizes the image without
        // allowing the image to control the panel size.
        StackPane photoPanel = new StackPane() {

            @Override
            protected void layoutChildren() {
                super.layoutChildren();

                double availableWidth =
                        Math.max(1, getWidth() - 40);

                double availableHeight =
                        Math.max(1, getHeight() - 40);

                imageView.setFitWidth(availableWidth);
                imageView.setFitHeight(availableHeight);

                // Centre the image using its actual
                // displayed dimensions.
                double imageWidth =
                        imageView.getBoundsInLocal().getWidth();

                double imageHeight =
                        imageView.getBoundsInLocal().getHeight();

                imageView.relocate(
                        (getWidth() - imageWidth) / 2,
                        (getHeight() - imageHeight) / 2
                );
            }
        };

        photoPanel.getChildren().add(imageView);

        // Prevent the image from making the panel
        // grow to the source photo's resolution.
        imageView.setManaged(false);

        photoPanel.setPrefSize(600, 440);
        photoPanel.setMinSize(0, 0);
        photoPanel.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        photoPanel.setStyle(
                "-fx-background-color: #232A34;" +
                        "-fx-background-radius: 12;" +
                        "-fx-padding: 20;"
        );

        // Main layout
        VBox root = new VBox();
        root.setStyle("-fx-background-color: #15191F;");
        root.setAlignment(Pos.CENTER);
        root.setSpacing(15);
        root.setPadding(new Insets(20));

        // Allow the photo panel to expand vertically.
        VBox.setVgrow(photoPanel, Priority.ALWAYS);

        Image logo = new Image(
                getClass().getResource("/images/picflick-logo.png").toExternalForm()
        );

        ImageView logoView = new ImageView(logo);
        logoView.setFitWidth(450);
        logoView.setPreserveRatio(true);
        ImageView sortingLogoView = new ImageView(logo);
        sortingLogoView.setFitWidth(180);
        sortingLogoView.setPreserveRatio(true);

        Label welcomeLabel = new Label("Welcome to PhotoSorter");
        Label remainingLabel = new Label("Photos remaining: 0");
        Label statusLabel = new Label("");

        welcomeLabel.setStyle(
                "-fx-text-fill: white;" +
                        "-fx-font-size: 24px;" +
                        "-fx-font-weight: bold;"
        );

        remainingLabel.setStyle(
                "-fx-text-fill: #DCE5F0;" +
                        "-fx-font-size: 18px;" +
                        "-fx-font-weight: bold;"
        );

        statusLabel.setStyle(
                "-fx-text-fill: white;" +
                        "-fx-font-size: 18px;"
        );

        // Sorting buttons
        Button keepButton = new Button("Keep");
        Button deleteButton = new Button("Delete");
        Button skipButton = new Button("Skip");
        Button undoButton = new Button("Undo");

        keepButton.setStyle(
                "-fx-background-color: #246A4D;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 14px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 8;" +
                        "-fx-padding: 12 24;"
        );

        deleteButton.setStyle(
                "-fx-background-color: #813A43;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 14px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 8;" +
                        "-fx-padding: 12 24;"
        );

        skipButton.setStyle(
                "-fx-background-color: #343D4B;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 14px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 8;" +
                        "-fx-padding: 12 24;"
        );

        undoButton.setStyle(
                "-fx-background-color: #343D4B;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 14px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 8;" +
                        "-fx-padding: 12 24;"
        );

        HBox buttonRow = new HBox();
        buttonRow.setAlignment(Pos.CENTER);
        buttonRow.setSpacing(15);

        buttonRow.getChildren().addAll(
                keepButton,
                deleteButton,
                skipButton,
                undoButton
        );

        keepButton.setDisable(true);
        deleteButton.setDisable(true);
        skipButton.setDisable(true);
        undoButton.setDisable(true);

        // Choose Folder button
        Button chooseFolderButton = new Button("Choose Folder");

        chooseFolderButton.setStyle(
                "-fx-background-color: #3B82F6;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 16px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 8;" +
                        "-fx-padding: 14 28;" +
                        "-fx-cursor: hand;"
        );

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

            remainingLabel.setText(
                    "Photos remaining: " +
                            photoManagerCurrent.getImageFiles().size()
            );

            if (currentPhoto != null) {

                Image image = new Image(currentPhoto.toUri().toString());
                imageView.setImage(image);

                keepButton.setDisable(false);
                deleteButton.setDisable(false);
                skipButton.setDisable(false);
                undoButton.setDisable(true);

                statusLabel.setText("");

                root.getChildren().setAll(
                        sortingLogoView,
                        statusLabel,
                        remainingLabel,
                        photoPanel,
                        buttonRow
                );

            } else {

                imageView.setImage(null);

                welcomeLabel.setText(
                        "No photos available. Please choose another folder."
                );

                root.getChildren().setAll(
                        logoView,
                        welcomeLabel,
                        chooseFolderButton
                );

                keepButton.setDisable(true);
                deleteButton.setDisable(true);
                skipButton.setDisable(true);
                undoButton.setDisable(true);
            }
        });

        // Keep button
        keepButton.setOnAction(event -> {

            photoManagerCurrent.movePhoto(true);

            remainingLabel.setText(
                    "Photos remaining: " +
                            photoManagerCurrent.getImageFiles().size()
            );

            undoButton.setDisable(!photoManagerCurrent.canUndo());

            Path nextPhoto = photoManagerCurrent.getCurrentPhoto();

            if (nextPhoto != null) {

                Image nextImage = new Image(nextPhoto.toUri().toString());
                imageView.setImage(nextImage);

            } else {

                imageView.setImage(null);
                statusLabel.setText("All photos sorted!");

                keepButton.setDisable(true);
                deleteButton.setDisable(true);
                skipButton.setDisable(true);
            }
        });

        // Delete button
        deleteButton.setOnAction(event -> {

            photoManagerCurrent.movePhoto(false);

            remainingLabel.setText(
                    "Photos remaining: " +
                            photoManagerCurrent.getImageFiles().size()
            );

            undoButton.setDisable(!photoManagerCurrent.canUndo());

            Path nextPhoto = photoManagerCurrent.getCurrentPhoto();

            if (nextPhoto != null) {

                Image nextImage = new Image(nextPhoto.toUri().toString());
                imageView.setImage(nextImage);

            } else {

                imageView.setImage(null);
                statusLabel.setText("All photos sorted!");

                keepButton.setDisable(true);
                deleteButton.setDisable(true);
                skipButton.setDisable(true);
            }
        });

        // Skip button
        skipButton.setOnAction(event -> {

            photoManagerCurrent.skipPhoto();

            Path nextPhoto = photoManagerCurrent.getCurrentPhoto();

            if (nextPhoto != null) {

                Image nextImage = new Image(nextPhoto.toUri().toString());
                imageView.setImage(nextImage);

            } else {

                skipButton.setDisable(true);
            }
        });

        // Undo button
        undoButton.setOnAction(event -> {

            photoManagerCurrent.undoPhoto();

            remainingLabel.setText(
                    "Photos remaining: " +
                            photoManagerCurrent.getImageFiles().size()
            );

            Path restoredPhoto = photoManagerCurrent.getCurrentPhoto();

            if (restoredPhoto != null) {

                Image restoredImage =
                        new Image(restoredPhoto.toUri().toString());

                imageView.setImage(restoredImage);
            }

            keepButton.setDisable(false);
            deleteButton.setDisable(false);
            skipButton.setDisable(false);

            statusLabel.setText("");

            undoButton.setDisable(!photoManagerCurrent.canUndo());
        });


        // Welcome screen
        root.getChildren().addAll(
                logoView,
                chooseFolderButton
        );

        Scene scene = new Scene(root, 800, 600);

        stage.setScene(scene);
        stage.setMinWidth(450);
        stage.setMinHeight(350);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
