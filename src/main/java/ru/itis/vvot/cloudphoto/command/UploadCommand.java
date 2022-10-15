package ru.itis.vvot.cloudphoto.command;

import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import ru.itis.vvot.cloudphoto.exception.IncorrectDirNameException;
import ru.itis.vvot.cloudphoto.service.FileService;
import ru.itis.vvot.cloudphoto.service.YandexStorageService;

import java.io.File;
import java.util.List;
import java.util.concurrent.Callable;

@Command(
        name = "upload",
        description = "Send photos to cloud storage"
)
public class UploadCommand implements Callable<Integer> {
    @Option(names = {"--help"}, usageHelp = true, description = "Display help message")
    boolean usageHelpRequested;
    @Value("${image.extensions}")
    private List<String> imageExtensions;
    @Option(names = {"--album"}, required = true, paramLabel = "ALBUM")
    private String album;
    @Option(names = {"--path"}, paramLabel = "PHOTOS_DIR")
    private String path;
    @Setter(onMethod_ = @Autowired)
    private YandexStorageService yandexService;

    @Setter(onMethod_ = @Autowired)
    private FileService fileService;

    @Override
    public Integer call() {
        if (album.contains("/") || album.trim().isEmpty()) {
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

        List<File> images;
        try {
            images = fileService.getFilesInDir(path, imageExtensions);
        } catch (IncorrectDirNameException e) {
            System.err.println("Failed to access " + path);
            return 1;
        }
        if (images.size() == 0) {
            System.err.println("There are no images in " + path);
            return 1;
        }
        images.forEach(x -> {
            String fileName = album + "/" + x.getName();
            try {
                yandexService.uploadObject(fileName, x);
            } catch (Exception e) {
                System.err.println("Failed uploading file " + x.getName());
            }
        });
        return 0;
    }
}
