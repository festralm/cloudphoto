package ru.itis.vvot.cloudphoto.command;

import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import ru.itis.vvot.cloudphoto.service.YandexStorageService;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.Callable;

@Command(
        name = "mksite",
        description = "Generate and public web pages of the photo archive"
)
public class MkSiteCommand implements Callable<Integer> {
    private static final String OUTPUT_URL_SAMPLE = "https://{bucket_name}.website.yandexcloud.net/";
    private static final String INDEX_PAGE_NAME = "index.html";
    private static final String INDEX_PAGE_SAMPLE = "<html>\n" +
            "    <head>\n" +
            "        <meta charset=\"UTF-8\">" +
            "        <title>Фотоархив</title>\n" +
            "    </head>\n" +
            "<body>\n" +
            "    <h1>Фотоархив</h1>\n" +
            "    <ul>\n" +
            "        {albums}" +
            "    </ul>\n" +
            "</body";
    private static final String ALBUM_TAG_SAMPLE = "        <li><a href=\"{src}\">{title}</a></li>\n";

    private static final String PAGE_NAME_SAMPLE = "album{N}.html";
    private static final String ALBUM_PAGE_SAMPLE = "<!doctype html>\n" +
            "<html>\n" +
            "    <head>\n" +
            "        <meta charset=\"UTF-8\">" +
            "        <link rel=\"stylesheet\" type=\"text/css\" href=\"https://cdnjs.cloudflare.com/ajax/libs/galleria/1.6.1/themes/classic/galleria.classic.min.css\" />\n" +
            "        <style>\n" +
            "            .galleria{ width: 960px; height: 540px; background: #000 }\n" +
            "        </style>\n" +
            "        <script src=\"https://ajax.googleapis.com/ajax/libs/jquery/3.6.0/jquery.min.js\"></script>\n" +
            "        <script src=\"https://cdnjs.cloudflare.com/ajax/libs/galleria/1.6.1/galleria.min.js\"></script>\n" +
            "        <script src=\"https://cdnjs.cloudflare.com/ajax/libs/galleria/1.6.1/themes/classic/galleria.classic.min.js\"></script>\n" +
            "    </head>\n" +
            "    <body>\n" +
            "        <div class=\"galleria\">\n" +
            "           {photos}" +
            "        </div>\n" +
            "        <p>Вернуться на <a href=\"index.html\">главную страницу</a> фотоархива</p>\n" +
            "        <script>\n" +
            "            (function() {\n" +
            "                Galleria.run('.galleria');\n" +
            "            }());\n" +
            "        </script>\n" +
            "    </body>\n" +
            "</html>";

    private static final String PHOTO_TAG_SAMPLE = "<img src=\"{src}\" data-title=\"{title}\">\n";

    private static final String ERROR_PAGE_NAME = "error.html";
    private static final String ERROR_PAGE = "<!doctype html>\n" +
            "<html>\n" +
            "    <head>\n" +
            "        <meta charset=\"UTF-8\">" +
            "        <title>Фотоархив</title>\n" +
            "    </head>\n" +
            "<body>\n" +
            "    <h1>Ошибка</h1>\n" +
            "    <p>Ошибка при доступе к фотоархиву. Вернитесь на <a href=\"index.html\">главную страницу</a> фотоархива.</p>\n" +
            "</body>\n" +
            "</html>";

    @Option(names = {"--help"}, usageHelp = true, description = "Display help message")
    boolean usageHelpRequested;

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

        yandexService.makeSite();

        List<String> albums = yandexService.getAlbumNames();

        StringBuilder albumPartScript = new StringBuilder();

        for (int i = 0; i < albums.size(); i++) {
            String pageName = PAGE_NAME_SAMPLE.replace("{N}", Integer.toString(i + 1));
            List<String> photos = yandexService.getImageNamesInAlbum(albums.get(i));
            StringBuilder photoPartScript = new StringBuilder();
            for (String image : photos) {
                photoPartScript.append(PHOTO_TAG_SAMPLE
                        .replace("{src}", yandexService
                                .getObjectUrl(albums.get(i), image))
                        .replace("{title}", image));
            }
            String albumScript = ALBUM_PAGE_SAMPLE.replace("{photos}", photoPartScript);

            InputStream photosStream = new ByteArrayInputStream(albumScript.getBytes());
            yandexService.uploadHtml(pageName, photosStream);

            albumPartScript.append(ALBUM_TAG_SAMPLE
                    .replace("{src}", pageName)
                    .replace("{title}", albums.get(i)));
        }

        String indexScript = INDEX_PAGE_SAMPLE.replace("{albums}", albumPartScript);

        InputStream indexStream = new ByteArrayInputStream(indexScript.getBytes());
        yandexService.uploadHtml(INDEX_PAGE_NAME, indexStream);

        InputStream errorStream = new ByteArrayInputStream(ERROR_PAGE.getBytes());
        yandexService.uploadHtml(ERROR_PAGE_NAME, errorStream);
        System.out.println(OUTPUT_URL_SAMPLE.replace("{bucket_name}", yandexService.getBucketName()));
        return 0;
    }
}
