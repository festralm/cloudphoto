package ru.itis.vvot.cloudphoto.command;

import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.concurrent.Callable;

@Command(
        name = "cloudphoto",
        description = "Manage albums and photos in Yandex Object Storage",
        subcommands = {
                UploadCommand.class,
                DownloadCommand.class,
                ListCommand.class,
                DeleteCommand.class,
                MkSiteCommand.class,
                InitCommand.class
        }
)
@Component
public class CloudphotoCommand implements Callable<Integer> {

    @Option(names = {"--help"}, usageHelp = true,
            description = "Display help message")
    boolean usageHelpRequested;

    @Override
    public Integer call() {
        return 1;
    }
}
