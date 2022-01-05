import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GetLinks {

    private static final String DIRECTORY = "./";
    private static final String LINKS_FILE_NAME = "links.txt";
    private static final String LINE_ENDING = "\n";
    private static final String EMPTY_STRING = "";

    public static void main(String[] args) {
        try (
                Stream<Path> stream = Files.list(Paths.get(DIRECTORY))
        ) {
            String links = stream
                    .filter(filePath -> !Files.isDirectory(filePath) && isUrlFile(filePath))
                    .map(filePath -> {
                        String fileName = getFileName(filePath);
                        int dotLastIndex = fileName.lastIndexOf('.');
                        if (dotLastIndex > 0)
                            fileName = fileName.substring(0, dotLastIndex).stripTrailing();
                        return fileName.replace('\u00a0',' ')
                                + LINE_ENDING
                                + extractLinkFromUrlFile(filePath);
                    })
                    .collect(Collectors.joining(LINE_ENDING.repeat(2)));
            Files.writeString(
                    Files.createFile(Paths.get(DIRECTORY + LINKS_FILE_NAME)),
                    links
            );
            System.out.println("Links saved successfully :)");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static boolean isUrlFile(Path filePath) {
        return getFileName(filePath).toLowerCase().endsWith(".url");
    }

    private static String getFileName(Path filePath) {
        return filePath.getFileName().toString();
    }

    private static String extractLinkFromUrlFile(Path filePath) {
        final String URL_LINE_START = "URL=";
        try {
            String fileContent = Files.readString(filePath);
            int urlStartIndex = fileContent.lastIndexOf(URL_LINE_START);
            if (fileContent.contains("[InternetShortcut]") && urlStartIndex >= 0) {
                fileContent = fileContent.substring(urlStartIndex + URL_LINE_START.length());
                int lineEndIndex = fileContent.indexOf(LINE_ENDING);
                if (lineEndIndex > -1)
                    fileContent = fileContent.substring(0, lineEndIndex);
                return fileContent.stripTrailing();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return EMPTY_STRING;
    }
}
