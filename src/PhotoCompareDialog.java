import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;

/** A manual filmstrip for comparing neighbouring shots before sorting. */
public final class PhotoCompareDialog {
    private PhotoCompareDialog() {}

    public static void show(Window owner, List<Path> photos, Consumer<Path> chooseNext) {
        if (photos.isEmpty()) return;
        Stage dialog = new Stage();
        dialog.setTitle("PicFlick — Compare shots");
        dialog.initOwner(owner);
        dialog.initModality(Modality.WINDOW_MODAL);

        Label heading = new Label("Compare nearby photos before deciding what to keep");
        heading.setStyle("-fx-text-fill: white;-fx-font-size: 18px;-fx-font-weight: bold;");
        Label note = new Label("Click a photo to preview it. Choose 'Sort this next' to return to the sorter with that shot first. No files are changed here.");
        note.setWrapText(true);
        note.setStyle("-fx-text-fill: #B8C3D3;");

        ImageView enlarged = new ImageView();
        enlarged.setPreserveRatio(true);
        enlarged.setSmooth(true);
        enlarged.setFitWidth(650);
        enlarged.setFitHeight(340);
        Label selectedName = new Label(photos.get(0).getFileName().toString());
        selectedName.setStyle("-fx-text-fill: white;");
        Path[] selected = {photos.get(0)};
        Runnable refreshPreview = () -> {
            Path path = selected[0];
            if (Files.isRegularFile(path)) {
                enlarged.setImage(new Image(path.toUri().toString(), 1100, 900, true, true, true));
                enlarged.setRotate(PhotoOrientation.rotationDegrees(path));
            }
            selectedName.setText(path.getFileName().toString());
        };
        refreshPreview.run();

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);
        grid.setPadding(new Insets(10));
        int index = 0;
        for (Path path : photos) {
            if (!Files.isRegularFile(path)) continue;
            ImageView thumb = new ImageView(new Image(path.toUri().toString(), 260, 190, true, true, true));
            thumb.setFitWidth(175);
            thumb.setFitHeight(135);
            thumb.setPreserveRatio(true);
            thumb.setRotate(PhotoOrientation.rotationDegrees(path));
            Label filename = new Label(path.getFileName().toString());
            filename.setMaxWidth(175);
            filename.setStyle("-fx-text-fill: #DCE5F0;");
            Button preview = new Button("View larger");
            preview.setOnAction(event -> {
                selected[0] = path;
                refreshPreview.run();
            });
            VBox tile = new VBox(7, thumb, filename, preview);
            tile.setAlignment(Pos.CENTER);
            tile.setPadding(new Insets(10));
            tile.setPrefWidth(210);
            tile.setStyle("-fx-background-color: #232A34;-fx-background-radius: 10;");
            grid.add(tile, index % 4, index / 4);
            index++;
        }
        ScrollPane strip = new ScrollPane(grid);
        strip.setFitToWidth(true);
        strip.setPrefViewportHeight(245);
        strip.setStyle("-fx-background: #15191F;-fx-background-color: #15191F;");
        Button choose = new Button("Sort this next");
        choose.setStyle("-fx-background-color: #246A4D;-fx-text-fill: white;-fx-font-weight: bold;-fx-padding: 10 20;");
        choose.setOnAction(event -> {
            dialog.close();
            chooseNext.accept(selected[0]);
        });
        Button close = new Button("Back to sorting");
        close.setOnAction(event -> dialog.close());
        HBox buttons = new HBox(12, choose, close);
        buttons.setAlignment(Pos.CENTER);

        VBox layout = new VBox(12, heading, note, selectedName, enlarged, strip, buttons);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(20));
        layout.setStyle("-fx-background-color: #15191F;");
        VBox.setVgrow(strip, Priority.ALWAYS);
        dialog.setScene(new Scene(layout, 940, 740));
        dialog.setMinWidth(650);
        dialog.setMinHeight(580);
        dialog.showAndWait();
    }
}
