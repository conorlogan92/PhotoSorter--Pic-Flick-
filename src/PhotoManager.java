import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class PhotoManager {
    private final Path folderLocation;
    private final ArrayList<Path> imageFiles = new ArrayList<>();
    private final Stack<PhotoAction> undoHistory = new Stack<>();
    private Path keepFolder;
    private Path deleteFolder;
    private Path duplicatesFolder;

    public PhotoManager(Path folderLocation) {
        this.folderLocation = folderLocation;
    }

    public boolean isCorrectFormat(Path file) {
        String name = file.getFileName().toString().toLowerCase(java.util.Locale.ROOT);
        return name.endsWith(".jpg") || name.endsWith(".jpeg") ||
                name.endsWith(".png") || name.endsWith(".webp");
    }

    public void openFolder() {
        imageFiles.clear();
        undoHistory.clear();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(folderLocation)) {
            for (Path file : stream) {
                if (Files.isRegularFile(file) && isCorrectFormat(file)) {
                    imageFiles.add(file);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Could not open folder: " + e.getMessage(), e);
        }
        imageFiles.sort(java.util.Comparator.comparing(p -> p.getFileName().toString().toLowerCase(java.util.Locale.ROOT)));
    }

    public ArrayList<Path> getImageFiles() { return imageFiles; }

    public List<Path> getImageFilesSnapshot() { return new ArrayList<>(imageFiles); }

    public void createSortingFolders() {
        keepFolder = folderLocation.resolve("ToKeep");
        deleteFolder = folderLocation.resolve("ToDelete");
        duplicatesFolder = folderLocation.resolve("Duplicates");
        try {
            Files.createDirectories(keepFolder);
            Files.createDirectories(deleteFolder);
        } catch (IOException e) {
            throw new RuntimeException("Could not create sorting folders: " + e.getMessage(), e);
        }
    }

    // Find a free filename, so an existing photo is never overwritten.
    private Path availableDestination(Path folder, Path source) {
        String filename = source.getFileName().toString();
        int dot = filename.lastIndexOf('.');
        String base = dot > 0 ? filename.substring(0, dot) : filename;
        String extension = dot > 0 ? filename.substring(dot) : "";
        Path destination = folder.resolve(filename);
        int number = 2;
        while (Files.exists(destination)) {
            destination = folder.resolve(base + " (" + number++ + ")" + extension);
        }
        return destination;
    }

    public void movePhoto(boolean keep) {
        if (imageFiles.isEmpty()) return;
        Path current = imageFiles.get(0);
        Path destination = availableDestination(keep ? keepFolder : deleteFolder, current);
        try {
            Files.move(current, destination);
            imageFiles.remove(0);
            undoHistory.push(new PhotoAction(current, destination, 0));
        } catch (IOException e) {
            throw new RuntimeException("Could not move photo: " + e.getMessage(), e);
        }
    }

    // Called on the JavaFX thread only, after the user confirms a duplicate.
    public void moveDuplicate(Path duplicate) throws IOException {
        if (!imageFiles.contains(duplicate) || !Files.isRegularFile(duplicate)) {
            throw new IOException("Photo is no longer in the unsorted folder: " + duplicate.getFileName());
        }
        Files.createDirectories(duplicatesFolder);
        Path destination = availableDestination(duplicatesFolder, duplicate);
        int index = imageFiles.indexOf(duplicate);
        Files.move(duplicate, destination);
        imageFiles.remove(index);
        undoHistory.push(new PhotoAction(duplicate, destination, index));
    }

    public void skipPhoto() {
        if (imageFiles.isEmpty()) return;
        imageFiles.add(imageFiles.remove(0));
    }

    public Path getCurrentPhoto() {
        return imageFiles.isEmpty() ? null : imageFiles.get(0);
    }

    public void undoPhoto() {
        if (undoHistory.isEmpty()) return;
        PhotoAction action = undoHistory.peek();
        try {
            Files.move(action.getDestinationPath(), action.getOriginalPath());
            imageFiles.add(Math.min(action.getOriginalIndex(), imageFiles.size()), action.getOriginalPath());
            undoHistory.pop();
        } catch (IOException e) {
            throw new RuntimeException("Could not undo move: " + e.getMessage(), e);
        }
    }

    public boolean canUndo() { return !undoHistory.isEmpty(); }
}
