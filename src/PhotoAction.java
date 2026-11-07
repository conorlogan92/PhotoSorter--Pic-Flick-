import java.nio.file.Path;

/** One reversible operation in the sorting queue. */
public class PhotoAction {
    public enum Type { MOVE, SKIP, REORDER }

    private final Type type;
    private final Path originalPath;
    private final Path destinationPath;
    private final int originalIndex;

    public PhotoAction(Path originalPath, Path destinationPath, int originalIndex) {
        this(Type.MOVE, originalPath, destinationPath, originalIndex);
    }

    private PhotoAction(Type type, Path originalPath, Path destinationPath, int originalIndex) {
        this.type = type;
        this.originalPath = originalPath;
        this.destinationPath = destinationPath;
        this.originalIndex = originalIndex;
    }

    public static PhotoAction skip(Path photo) {
        return new PhotoAction(Type.SKIP, photo, null, 0);
    }

    public static PhotoAction reorder(Path photo, int originalIndex) {
        return new PhotoAction(Type.REORDER, photo, null, originalIndex);
    }

    public Type getType() { return type; }
    public Path getOriginalPath() { return originalPath; }
    public Path getDestinationPath() { return destinationPath; }
    public int getOriginalIndex() { return originalIndex; }
}
