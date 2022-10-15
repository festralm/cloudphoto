package ru.itis.vvot.cloudphoto.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.itis.vvot.cloudphoto.exception.IncorrectDirNameException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
public class FileService {
    public void writeToFile(String path, String text) throws FileNotFoundException {
        int startIndex = 0;
        int indexOfSlash = path.indexOf('/', startIndex);
        while (indexOfSlash != -1) {
            makeDirs(path.substring(0, indexOfSlash));
            indexOfSlash = path.indexOf('/', indexOfSlash + 1);
        }
        PrintWriter pw = getPrintWriter(path);
        pw.print(text);
        pw.close();
    }

    private PrintWriter getPrintWriter(String path) throws FileNotFoundException {
        return new PrintWriter(new OutputStreamWriter(new FileOutputStream(path), StandardCharsets.UTF_8));
    }

    public void saveFile(InputStream io, String path) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(path)) {
            byte[] read_buf = new byte[1024];
            int read_len;
            while ((read_len = io.read(read_buf)) > 0) {
                fos.write(read_buf, 0, read_len);
            }
        }
    }

    public String readFromFile(String path) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null) {
                sb.append(line);
                sb.append(System.lineSeparator());
                line = br.readLine();
            }
            return sb.toString();
        }
    }

    public void makeDirs(String... dirs) {
        for (String dir : dirs) {
            new File(dir).mkdir();
        }
    }

    public List<File> getFilesInDir(String dir, List<String> extensions) {
        File[] files = new File(dir).listFiles();
        if (files == null) {
            throw new IncorrectDirNameException();
        }
        if (extensions == null) {
            return Arrays.asList(files);
        }
        return Stream.of(files)
                .filter(file -> {
                    String name = file.getName();
                    return name.contains(".") && extensions.contains(name.substring(name.lastIndexOf(".") + 1).toLowerCase());
                })
                .collect(Collectors.toList());
    }
}
