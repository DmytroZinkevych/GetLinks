import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

public class GetLinks_OLD {
    public static void main(String[] args) {
        final String FILE_EXTENSION = ".url";
        final String URL_LINE_START = "URL=";
        try (
                Stream<Path> stream = Files.list(Paths.get("./"));
                BufferedWriter writer = new BufferedWriter(new FileWriter("links.txt"))
        ) {
            stream
                    .filter(filePath -> !Files.isDirectory(filePath)
                            && filePath.getFileName().toString().toLowerCase().endsWith(FILE_EXTENSION))
                    .map(Path::toFile)
                    .forEach(file -> {
                        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)))) {
                            String line = br.readLine();
                            if (line.contains("[InternetShortcut]")) {
                                line = br.readLine();
                                if (line.toUpperCase().startsWith(URL_LINE_START)) {
                                    writer
                                            .append(file.getName(), 0, file.getName().length() - FILE_EXTENSION.length())
                                            .append("\n")
                                            .append(line.substring(URL_LINE_START.length()))
                                            .append("\n\n");
                                }
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
            System.out.println("Links saved successfully :)");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
