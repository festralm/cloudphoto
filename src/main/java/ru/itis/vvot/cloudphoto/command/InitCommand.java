package ru.itis.vvot.cloudphoto.command;

import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import ru.itis.vvot.cloudphoto.service.FileService;
import ru.itis.vvot.cloudphoto.service.YandexStorageService;

import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.Callable;

@Slf4j
@Command(
        name = "init",
        description = "Program init"
)
public class InitCommand implements Callable<Integer> {
    private static final String CONFIG_SAMPLE = "[DEFAULT]\n" +
            "bucket = INPUT_BUCKET_NAME\n" +
            "aws_access_key_id = INPUT_AWS_ACCESS_KEY_ID\n" +
            "aws_secret_access_key = INPUT_AWS_SECRET_ACCESS_KEY\n" +
            "region = ru-central1\n" +
            "endpoint_url = https://storage.yandexcloud.net";
    private static final Scanner sc = new Scanner(System.in);
    @Option(names = {"--help"}, usageHelp = true, description = "Display help message")
    boolean usageHelpRequested;
    @Setter(onMethod_ = @Autowired)
    private YandexStorageService yandexService;
    @Setter(onMethod_ = @Autowired)
    private FileService fileService;

    @SneakyThrows
    @Override
    public Integer call() {
        try {
            System.out.println("aws_access_key_id:");
            String awsAccessKeyId = sc.nextLine();
            System.out.println("aws_secret_access_key:");
            String awsSecretAccessKey = sc.nextLine();
            System.out.println("bucket:");
            String bucket = sc.nextLine();

            saveConfig(awsAccessKeyId, awsSecretAccessKey, bucket);

            try {
                yandexService.initClient();
            } catch (Exception e) {
                System.err.println("Failed to access storage");
                return 1;
            }
            yandexService.checkAndCreateBucket(bucket);
        } catch (Exception e) {
            System.err.println("Error during initialization");
            return 1;
        }
        return 0;
    }

    private void saveConfig(String awsAccessKeyId, String awsSecretAccessKey, String bucket) throws IOException {
        String configString = CONFIG_SAMPLE.replace("INPUT_BUCKET_NAME", bucket.trim())
                .replace("INPUT_AWS_ACCESS_KEY_ID", awsAccessKeyId.trim())
                .replace("INPUT_AWS_SECRET_ACCESS_KEY", awsSecretAccessKey.trim());
        fileService.writeToFile(YandexStorageService.CREDENTIALS_PATH, configString);
    }
}
