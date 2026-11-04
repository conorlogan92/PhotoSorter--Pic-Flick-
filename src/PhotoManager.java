import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.IOException;

public class PhotoManager {

    private Path folderLocation;
    private DirectoryStream<Path> directoryStream;


    public PhotoManager(Path folderLocation){
        this.folderLocation = folderLocation;
    }

    public boolean isCorrectFormat(Path file) {
        String currentFile = file.getFileName().toString().toLowerCase();

        return (currentFile.endsWith(".jpg") || currentFile.endsWith(".jpeg") ||
                currentFile.endsWith(".png") || currentFile.endsWith(".webp"));
    }

    public void openFolder() {

        try{

            directoryStream = Files.newDirectoryStream(folderLocation);

        } catch (IOException e){
            System.out.println("something went wrong");
        }



    }






}
