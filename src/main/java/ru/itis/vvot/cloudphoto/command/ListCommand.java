package ru.itis.vvot.cloudphoto.command;

import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import ru.itis.vvot.cloudphoto.service.YandexStorageService;

import java.util.List;
import java.util.concurrent.Callable;

@Command(
        name = "list",
        description = "Display a list of albums and photos in the cloud storage"
)
public class ListCommand implements Callable<Integer> {
    @Option(names = {"--help"}, usageHelp = true, description = "Display help message")
    boolean usageHelpRequested;
    @Option(names = {"--album"}, paramLabel = "ALBUM")
    private String album;
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
        if (album == null) {
            List<String> albumNames = yandexService.getAlbumNames();
            if (albumNames.size() == 0) {
                System.err.println("There are no albums in storage");
                return 1;
            }
            for (String name : albumNames) {
                System.out.println(name);
            }
        } else {
            if (!yandexService.albumExists(album)) {
                System.err.println("Album does not exist");
                return 1;
            }
            List<String> imageNames = yandexService.getImageNamesInAlbum(album);
            if (imageNames.size() == 0) {
                System.err.println("Album is empty");
                return 1;
            }
            for (String name : imageNames) {
                System.out.println(name);
            }
        }
        return 0;
    }
}
