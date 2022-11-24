import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GetLinks {
    private static final String DIRECTORY = "./";
    private static final String LINKS_FILE_NAME = "links.txt";
    private static final String LINE_ENDING = "\n";
    private static final String EMPTY_STRING = "";
    private static final String URL_LINE_START = "URL=";

    public static void main(String[] args) throws IOException {
        try (Stream<Path> stream = Files.list(Path.of(DIRECTORY))) {
            String links = stream
                    .filter(filePath -> !Files.isDirectory(filePath)
                            && (isUrlFile(filePath) || isWeblocFile(filePath))
                    )
                    .map(filePath -> {
                        String text = getFileName(filePath);
                        int dotLastIndex = text.lastIndexOf('.');
                        if (dotLastIndex > 0)
                            text = text.substring(0, dotLastIndex).stripTrailing();
                        text = text.replace('\u00a0', ' ') + LINE_ENDING;
                        if (isUrlFile(filePath)) {
                            text += extractLinkFromUrlFile(filePath);
                        } else if (isWeblocFile(filePath)) {
                            text += extractLinkFromWeblocFile(filePath);
                        }
                        return text;
                    })
                    .collect(Collectors.joining(LINE_ENDING.repeat(2)));
            Files.writeString(
                    Files.createFile(Path.of(DIRECTORY + LINKS_FILE_NAME)),
                    links
            );
            System.out.println("Links saved successfully :)");
        }
    }

    private static boolean isUrlFile(Path filePath) {
        return getFileName(filePath).toLowerCase().endsWith(".url");
    }

    private static boolean isWeblocFile(Path filePath) {
        return getFileName(filePath).toLowerCase().endsWith(".webloc");
    }

    private static String getFileName(Path filePath) {
        return filePath.getFileName().toString();
    }

    private static String extractLinkFromUrlFile(Path filePath) {
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
            printFileError(filePath, e);
        }
        return EMPTY_STRING;
    }

    private static String extractLinkFromWeblocFile(Path filePath) {
        try {
            Document document = DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder()
                    .parse(filePath.toFile());
            XPathExpression expr = XPathFactory.newInstance()
                    .newXPath()
                    .compile("/plist/dict[key='URL']/string");
            Node node = (Node) expr.evaluate(document, XPathConstants.NODE);
            if (node != null)
                return node.getTextContent();
        } catch (Exception e) {
            printFileError(filePath, e);
        }
        return EMPTY_STRING;
    }

    private static void printFileError(Path filePath, Exception e) {
        System.err.println("Error while reading the file \"" + getFileName(filePath) + "\"");
        e.printStackTrace();
    }
}
