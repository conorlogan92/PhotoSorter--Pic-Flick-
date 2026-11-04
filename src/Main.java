import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {



    public static void main(String[] args) {

        Path folder = Paths.get("/Users/conorlogan/Desktop/camera pics");
        PhotoManager photoManagerCurrent = new PhotoManager(folder);
        photoManagerCurrent.openFolder();
        System.out.println(photoManagerCurrent.getImageFiles().size());
        System.out.println(photoManagerCurrent.getImageFiles().get(0));
        photoManagerCurrent.createSortingFolders();

    }
}
