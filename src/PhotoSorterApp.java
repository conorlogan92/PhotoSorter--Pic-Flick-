import javafx.application.Application;
import javafx.concurrent.Worker;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.SelectionMode;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class PhotoSorterApp extends Application {
    private PhotoManager photoManagerCurrent;
    private DuplicateScanner currentScan;
    // Rotation is only applied to the preview; original photographs are never changed.
    private final Map<Path, Integer> previewRotation = new HashMap<>();

    @Override
    public void start(Stage stage) {
        stage.setTitle("PicFlick");
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/images/picflick-logo.png")));

        ImageView imageView = new ImageView();
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);

        // Preserve the original responsive photo panel.
        StackPane photoPanel = new StackPane() {
            @Override
            protected void layoutChildren() {
                super.layoutChildren();
                double availableWidth = Math.max(1, getWidth() - 40);
                double availableHeight = Math.max(1, getHeight() - 40);
                // A quarter-turn reverses the available width/height of the image.
                boolean sideways = ((int) Math.round(imageView.getRotate()) % 180) != 0;
                imageView.setFitWidth(sideways ? availableHeight : availableWidth);
                imageView.setFitHeight(sideways ? availableWidth : availableHeight);
                double imageWidth = imageView.getBoundsInLocal().getWidth();
                double imageHeight = imageView.getBoundsInLocal().getHeight();
                imageView.relocate((getWidth() - imageWidth) / 2,
                        (getHeight() - imageHeight) / 2);
            }
        };
        photoPanel.getChildren().add(imageView);
        imageView.setManaged(false);
        photoPanel.setPrefSize(600, 440);
        photoPanel.setMinSize(0, 0);
        photoPanel.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        photoPanel.setStyle("-fx-background-color: #232A34;-fx-background-radius: 12;-fx-padding: 20;");

        VBox root = new VBox(15);
        root.setStyle("-fx-background-color: #15191F;");
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(20));
        VBox.setVgrow(photoPanel, Priority.ALWAYS);

        Image logo = new Image(getClass().getResource("/images/picflick-logo.png").toExternalForm());
        ImageView logoView = new ImageView(logo);
        logoView.setFitWidth(450);
        logoView.setPreserveRatio(true);
        ImageView sortingLogoView = new ImageView(logo);
        sortingLogoView.setFitWidth(180);
        sortingLogoView.setPreserveRatio(true);

        Label welcomeLabel = new Label("No photos available. Please choose another folder.");
        welcomeLabel.setStyle("-fx-text-fill: white;-fx-font-size: 18px;-fx-font-weight: bold;");
        Label remainingLabel = new Label("Photos remaining: 0");
        remainingLabel.setStyle("-fx-text-fill: #DCE5F0;-fx-font-size: 18px;-fx-font-weight: bold;");
        Label statusLabel = new Label("");
        statusLabel.setStyle("-fx-text-fill: white;-fx-font-size: 18px;");
        Label scanLabel = new Label("");
        scanLabel.setStyle("-fx-text-fill: #DCE5F0;-fx-font-size: 12px;");
        ProgressBar scanProgress = new ProgressBar(0);
        scanProgress.setPrefWidth(200);
        scanProgress.setVisible(false);
        scanProgress.setManaged(false);

        Button keepButton = new Button("Keep");
        Button deleteButton = new Button("Delete");
        Button skipButton = new Button("Skip");
        Button undoButton = new Button("Undo");
        Button scanButton = new Button("Find Duplicates");
        Button rotateLeftButton = new Button("↶ Rotate Left");
        Button rotateRightButton = new Button("↷ Rotate Right");
        Button compareButton = new Button("Compare Shots");
        styleButton(keepButton, "#246A4D");
        styleButton(deleteButton, "#813A43");
        styleButton(skipButton, "#343D4B");
        styleButton(undoButton, "#343D4B");
        styleButton(scanButton, "#3B82F6");
        styleButton(rotateLeftButton, "#343D4B");
        styleButton(rotateRightButton, "#343D4B");
        styleButton(compareButton, "#3B82F6");
        keepButton.setDisable(true);
        deleteButton.setDisable(true);
        skipButton.setDisable(true);
        undoButton.setDisable(true);

        HBox buttonRow = new HBox(15, keepButton, deleteButton, skipButton, undoButton);
        buttonRow.setAlignment(Pos.CENTER);
        HBox toolsRow = new HBox(10, rotateLeftButton, rotateRightButton, compareButton);
        toolsRow.setAlignment(Pos.CENTER);
        HBox scanRow = new HBox(12, scanButton, scanProgress, scanLabel);
        scanRow.setAlignment(Pos.CENTER);

        Button chooseFolderButton = new Button("Choose Folder");
        chooseFolderButton.setStyle("-fx-background-color: #3B82F6;-fx-text-fill: white;" +
                "-fx-font-size: 16px;-fx-font-weight: bold;-fx-background-radius: 8;" +
                "-fx-padding: 14 28;-fx-cursor: hand;");

        // A single refresh keeps the image, counter and sorting buttons consistent.
        Runnable refresh = () -> {
            if (photoManagerCurrent == null) return;
            Path current = photoManagerCurrent.getCurrentPhoto();
            remainingLabel.setText("Photos remaining: " + photoManagerCurrent.getImageFiles().size());
            imageView.setImage(current == null ? null : new Image(current.toUri().toString()));
            imageView.setRotate(current == null ? 0 : previewRotation.computeIfAbsent(
                    current, PhotoOrientation::rotationDegrees));
            photoPanel.requestLayout();
            rotateLeftButton.setDisable(current == null);
            rotateRightButton.setDisable(current == null);
            compareButton.setDisable(current == null);
            keepButton.setDisable(current == null);
            deleteButton.setDisable(current == null);
            skipButton.setDisable(current == null);
            undoButton.setDisable(!photoManagerCurrent.canUndo());
            statusLabel.setText(current == null ? "All photos sorted!" : "");
        };

        chooseFolderButton.setOnAction(event -> {
            DirectoryChooser chooser = new DirectoryChooser();
            File selectedFolder = chooser.showDialog(stage);
            if (selectedFolder == null) return;
            try {
                photoManagerCurrent = new PhotoManager(selectedFolder.toPath());
                photoManagerCurrent.openFolder();
                photoManagerCurrent.createSortingFolders();
                if (photoManagerCurrent.getCurrentPhoto() == null) {
                    root.getChildren().setAll(logoView, welcomeLabel, chooseFolderButton);
                } else {
                    root.getChildren().setAll(sortingLogoView, statusLabel, remainingLabel,
                            photoPanel, toolsRow, buttonRow, scanRow);
                    refresh.run();
                }
            } catch (RuntimeException ex) {
                showError("Could not open folder", ex.getMessage());
            }
        });

        keepButton.setOnAction(event -> {
            try {
                photoManagerCurrent.movePhoto(true);
                refresh.run();
            } catch (RuntimeException ex) { showError("Keep failed", ex.getMessage()); }
        });
        deleteButton.setOnAction(event -> {
            try {
                photoManagerCurrent.movePhoto(false);
                refresh.run();
            } catch (RuntimeException ex) { showError("Delete failed", ex.getMessage()); }
        });
        skipButton.setOnAction(event -> {
            photoManagerCurrent.skipPhoto();
            refresh.run();
        });
        undoButton.setOnAction(event -> {
            try {
                photoManagerCurrent.undoPhoto();
                refresh.run();
            } catch (RuntimeException ex) { showError("Undo failed", ex.getMessage()); }
        });

        rotateLeftButton.setOnAction(event -> rotatePreview(-90, imageView, photoPanel));
        rotateRightButton.setOnAction(event -> rotatePreview(90, imageView, photoPanel));
        compareButton.setOnAction(event -> {
            if (photoManagerCurrent == null) return;
            PhotoManager manager = photoManagerCurrent;
            // Neighbouring filenames typically contain consecutive burst shots.
            List<Path> candidates = manager.getImageFilesSnapshot();
            PhotoCompareDialog.show(stage, new ArrayList<>(
                    candidates.subList(0, Math.min(12, candidates.size()))), chosen -> {
                if (manager != photoManagerCurrent) return;
                manager.prioritizePhoto(chosen);
                refresh.run();
            });
        });

        scanButton.setOnAction(event -> {
            if (photoManagerCurrent == null || currentScan != null && currentScan.isRunning()) return;
            // Capture the manager: never apply old scan results to a different folder.
            PhotoManager managerForScan = photoManagerCurrent;
            DuplicateScanner scan = new DuplicateScanner(managerForScan.getImageFilesSnapshot());
            currentScan = scan;
            scanButton.setDisable(true);
            scanProgress.setVisible(true);
            scanProgress.setManaged(true);
            scanProgress.progressProperty().bind(scan.progressProperty());
            scanLabel.textProperty().bind(scan.messageProperty());

            scan.setOnSucceeded(done -> {
                endScan(scanButton, scanProgress, scanLabel);
                if (managerForScan != photoManagerCurrent) return;
                List<DuplicateScanner.Match> matches = scan.getValue();
                if (matches.isEmpty()) {
                    new Alert(Alert.AlertType.INFORMATION, "No exact duplicates found.", ButtonType.OK).showAndWait();
                } else {
                    reviewDuplicates(matches, managerForScan, refresh);
                }
            });
            scan.setOnFailed(failed -> {
                endScan(scanButton, scanProgress, scanLabel);
                showError("Duplicate scan failed", scan.getException().getMessage());
            });
            scan.setOnCancelled(cancelled -> endScan(scanButton, scanProgress, scanLabel));
            Thread worker = new Thread(scan, "picflick-duplicate-scanner");
            worker.setDaemon(true);
            worker.start();
        });

        root.getChildren().addAll(logoView, chooseFolderButton);
        Scene scene = new Scene(root, 800, 600);
        stage.setScene(scene);
        stage.setMinWidth(450);
        stage.setMinHeight(350);
        stage.setOnCloseRequest(event -> {
            if (currentScan != null && currentScan.getState() == Worker.State.RUNNING) currentScan.cancel();
        });
        stage.show();
    }

    private void rotatePreview(int delta, ImageView imageView, StackPane panel) {
        if (photoManagerCurrent == null) return;
        Path current = photoManagerCurrent.getCurrentPhoto();
        if (current == null) return;
        int angle = Math.floorMod(previewRotation.computeIfAbsent(
                current, PhotoOrientation::rotationDegrees) + delta, 360);
        previewRotation.put(current, angle);
        imageView.setRotate(angle);
        panel.requestLayout();
    }

    private void reviewDuplicates(List<DuplicateScanner.Match> matches,
                                  PhotoManager manager, Runnable refresh) {
        ListView<DuplicateScanner.Match> list = new ListView<>();
        list.getItems().addAll(matches);
        list.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        list.setPrefHeight(280);
        Label instructions = new Label("Select copies to move to Duplicates. The matching original stays in place.");
        instructions.setWrapText(true);
        VBox content = new VBox(10, instructions, list);
        ButtonType move = new ButtonType("Move selected copies");
        Alert dialog = new Alert(Alert.AlertType.CONFIRMATION);
        dialog.setTitle("PicFlick — Duplicate review");
        dialog.setHeaderText(matches.size() + " exact duplicate copies found");
        dialog.getDialogPane().setContent(content);
        dialog.getButtonTypes().setAll(move, ButtonType.CANCEL);
        Optional<ButtonType> response = dialog.showAndWait();
        if (!response.isPresent() || response.get() != move) return;
        List<DuplicateScanner.Match> selected = new ArrayList<>(list.getSelectionModel().getSelectedItems());
        if (selected.isEmpty()) return;

        int moved = 0;
        List<String> skipped = new ArrayList<>();
        for (DuplicateScanner.Match match : selected) {
            try {
                // A user may have sorted either photo since the scan began.
                // Never move a copy unless its reference and content are still present.
                if (!Files.isRegularFile(match.getOriginal()) ||
                        !Files.isRegularFile(match.getDuplicate()) ||
                        !manager.getImageFiles().contains(match.getOriginal()) ||
                        !manager.getImageFiles().contains(match.getDuplicate()) ||
                        !DuplicateScanner.sha256(match.getOriginal()).equals(match.getFingerprint()) ||
                        !DuplicateScanner.sha256(match.getDuplicate()).equals(match.getFingerprint())) {
                    skipped.add(match.getDuplicate().getFileName().toString());
                    continue;
                }
                manager.moveDuplicate(match.getDuplicate());
                moved++;
            } catch (IOException ex) {
                skipped.add(match.getDuplicate().getFileName().toString());
            }
        }
        refresh.run();
        String summary = moved + " duplicate copies moved to the Duplicates folder.";
        if (!skipped.isEmpty()) {
            summary += "\n" + skipped.size() + " skipped because files changed or could not be moved.";
        }
        new Alert(Alert.AlertType.INFORMATION, summary, ButtonType.OK).showAndWait();
    }

    private void endScan(Button button, ProgressBar progress, Label label) {
        progress.progressProperty().unbind();
        label.textProperty().unbind();
        progress.setVisible(false);
        progress.setManaged(false);
        label.setText("");
        button.setDisable(false);
    }

    private void styleButton(Button button, String color) {
        button.setStyle("-fx-background-color: " + color + ";-fx-text-fill: white;" +
                "-fx-font-size: 14px;-fx-font-weight: bold;-fx-background-radius: 8;-fx-padding: 12 24;");
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message == null ? "Unknown error" : message, ButtonType.OK);
        alert.setHeaderText(title);
        alert.showAndWait();
    }

    public static void main(String[] args) { launch(args); }
}
