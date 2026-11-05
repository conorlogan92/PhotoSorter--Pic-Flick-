import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

public class PhotoManager {

    private Path folderLocation;
    private ArrayList<Path> imageFiles;
    private Path keepFolder;
    private Path deleteFolder;


    public PhotoManager(Path folderLocation){
        this.folderLocation = folderLocation;
        this.imageFiles = new ArrayList<>();
    }

    public boolean isCorrectFormat(Path file) {
        String currentFile = file.getFileName().toString().toLowerCase();

        return (currentFile.endsWith(".jpg") || currentFile.endsWith(".jpeg") ||
                currentFile.endsWith(".png") || currentFile.endsWith(".webp"));
    }

    public void openFolder() {

        try{

            DirectoryStream<Path> directoryStream = Files.newDirectoryStream(folderLocation);

            for (Path file: directoryStream){

                if (isCorrectFormat(file)){

                    imageFiles.add(file);

                }

            }

        } catch (IOException e){
            System.out.println("something went wrong");
        }

    }

    public ArrayList<Path> getImageFiles() {
        return imageFiles;
    }


    public void createSortingFolders() {

        keepFolder = folderLocation.resolve("ToKeep");
        deleteFolder = folderLocation.resolve("ToDelete");
        try {

            Files.createDirectories(keepFolder);
            Files.createDirectories(deleteFolder);


        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public void movePhoto(boolean keep) {

        if (imageFiles.isEmpty()) {
            return;
        }

        Path currentPhoto = imageFiles.get(0);
        Path destination;


        if (keep) {
            destination = keepFolder.resolve(currentPhoto.getFileName());
        } else {
            destination = deleteFolder.resolve(currentPhoto.getFileName());
        }

        try {
            Files.move(currentPhoto, destination);
            imageFiles.remove(0);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public Path getCurrentPhoto() {

        if (imageFiles.isEmpty()) {
            return null;
        } else {
            return imageFiles.get(0);
        }

    }

}
