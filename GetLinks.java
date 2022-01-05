import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;

public class GetLinks {
    private static final String DIRECTORY = "./";
    private static final String LINKS_FILE_NAME = "links.txt";
    private static final String LINE_ENDING = "\n";
    private static final String EMPTY_STRING = "";

    public static void main(String[] args) throws IOException {
        String links = Files.list(Paths.get(DIRECTORY))
                .filter(filePath -> !Files.isDirectory(filePath)
                        && (isUrlFile(filePath) || isWeblocFile(filePath))
                )
                .map(filePath -> {
                    String content = getFileName(filePath);
                    int dotLastIndex = content.lastIndexOf('.');
                    if (dotLastIndex > 0)
                        content = content.substring(0, dotLastIndex).stripTrailing();
                    content = content.replace('\u00a0',' ') + LINE_ENDING;
                    if (isUrlFile(filePath)) {
                        content += extractLinkFromUrlFile(filePath);
                    } else if (isWeblocFile(filePath)) {
                        content += extractLinkFromWeblocFile(filePath);
                    }
                    return content;
                })
                .collect(Collectors.joining(LINE_ENDING.repeat(2)));
        Files.writeString(
                Files.createFile(Paths.get(DIRECTORY + LINKS_FILE_NAME)),
                links
        );
        System.out.println("Links saved successfully :)");
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
            e.printStackTrace();
        }
        return EMPTY_STRING;
    }
}
