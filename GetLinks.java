import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GetLinks {
    public static void main(String[] args) {
        final String FILE_EXTENSION = ".url";
        final String URL_LINE_START = "URL=";
        final String DIRECTORY = "./";
        try (
                Stream<Path> stream = Files.list(Paths.get(DIRECTORY))
        ) {
            String links = stream
                    .filter(filePath -> !Files.isDirectory(filePath)
                            && filePath.getFileName().toString().toLowerCase().endsWith(FILE_EXTENSION))
                    .map(filePath -> {
                        try {
                            String fileContent = Files.readString(filePath);
                            int urlStartIndex = fileContent.lastIndexOf(URL_LINE_START);
                            if (fileContent.contains("[InternetShortcut]") && urlStartIndex >= 0) {
                                fileContent = fileContent.substring(urlStartIndex + URL_LINE_START.length());
                                int lineEndIndex = fileContent.indexOf("\n");
                                if (lineEndIndex > -1)
                                    fileContent = fileContent.substring(0, lineEndIndex).stripTrailing();
                                String fileName = filePath.getFileName().toString();
                                return fileName.substring(0, fileName.length() - FILE_EXTENSION.length())
                                        + "\n"
                                        + fileContent.stripTrailing();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return "";
                    })
                    .collect(Collectors.joining("\n\n"));
            Files.writeString(Files.createFile(Paths.get(DIRECTORY + "links.txt")), links);
            System.out.println("Links saved successfully :)");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
