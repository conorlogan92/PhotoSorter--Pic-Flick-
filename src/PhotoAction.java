import java.nio.file.Path;

public class PhotoAction {
    private final Path originalPath;
    private final Path destinationPath;
    private final int originalIndex;

    public PhotoAction(Path originalPath, Path destinationPath, int originalIndex) {
        this.originalPath = originalPath;
        this.destinationPath = destinationPath;
        this.originalIndex = originalIndex;
    }

    public Path getOriginalPath() { return originalPath; }
    public Path getDestinationPath() { return destinationPath; }
    public int getOriginalIndex() { return originalIndex; }
}
