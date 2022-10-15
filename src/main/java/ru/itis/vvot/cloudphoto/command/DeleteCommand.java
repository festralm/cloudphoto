package ru.itis.vvot.cloudphoto.command;

import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import ru.itis.vvot.cloudphoto.service.YandexStorageService;

import java.util.concurrent.Callable;

@Command(
        name = "delete",
        description = "Delete albums and photos in the cloud storage"
)
public class DeleteCommand implements Callable<Integer> {
    @Option(names = {"--help"}, usageHelp = true, description = "Display help message")
    boolean usageHelpRequested;
    @Option(names = {"--album"}, paramLabel = "ALBUM", required = true)
    private String album;
    @Option(names = {"--photo"}, paramLabel = "PHOTO")
    private String photo;
    @Setter(onMethod_ = @Autowired)
    private YandexStorageService yandexService;

    @Override
    public Integer call() {
        try {
            yandexService.initClient();
        } catch (Exception e) {
            System.err.println("Failed to access storage");
            return 1;
        }

        if (!yandexService.albumExists(album)) {
            System.err.println("Album does not exist");
            return 1;
        }

        if (photo == null) {
            yandexService.deleteObjectsByPrefix(album + "/");
        } else {
            if (!yandexService.photoExists(album, photo)) {
                System.err.println("Photo does not exist");
                return 1;
            }
            yandexService.deleteObject(album + "/" + photo);
        }
        return 0;
    }
}
