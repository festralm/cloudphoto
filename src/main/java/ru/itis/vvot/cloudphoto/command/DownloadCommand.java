package ru.itis.vvot.cloudphoto.command;

import com.amazonaws.services.s3.model.S3Object;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import ru.itis.vvot.cloudphoto.service.FileService;
import ru.itis.vvot.cloudphoto.service.YandexStorageService;

import java.util.List;
import java.util.concurrent.Callable;

@Command(
        name = "download",
        description = "Upload photos from cloud storage"
)
public class DownloadCommand implements Callable<Integer> {

    @Option(names = {"--help"}, usageHelp = true, description = "Display help message")
    boolean usageHelpRequested;
    @Option(names = {"--album"}, paramLabel = "ALBUM", required = true)
    private String album;
    @Option(names = {"--path"}, paramLabel = "PHOTOS_DIR")
    private String path;
    @Setter(onMethod_ = @Autowired)
    private YandexStorageService yandexService;

    @Setter(onMethod_ = @Autowired)
    private FileService fileService;

    @Override
    public Integer call() {
        if (album.contains("/")) {
            System.err.println("Incorrect album name " + album);
            return 1;
        }
        if (path == null) {
            path = System.getProperty("user.dir");
        }

        try {
            yandexService.initClient();
        } catch (Exception e) {
            System.err.println("Failed to access storage");
            return 1;
        }

        List<String> imageNames = yandexService.getImageNamesInAlbum(album);

        if (imageNames.size() == 0) {
            System.err.println("Album is empty");
            return 1;
        }

        for (String x : imageNames) {
            S3Object object;
            try {
                object = yandexService.getObject(album + "/" + x);
            } catch (Exception e) {
                System.err.println("Failed to download image " + x);
                return 1;
            }
            try {
                fileService.saveFile(object.getObjectContent(), path + "/" + x);
            } catch (Exception e) {
                System.err.println("Failed to save image " + x);
                return 1;
            }
        }
        return 0;
    }
}
