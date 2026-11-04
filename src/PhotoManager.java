import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

public class PhotoManager {

    private Path folderLocation;
    private ArrayList<Path> imageFiles;


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

        Path keepFolder = folderLocation.resolve("ToKeep");
        Path deleteFolder = folderLocation.resolve("ToDelete");
        try {

            Files.createDirectories(keepFolder);
            Files.createDirectories(deleteFolder);


        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }

}
