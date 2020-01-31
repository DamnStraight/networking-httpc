import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.URL;

public class Utils {
    static final String HELP_OUTPUT = "\n" + "httpc is a curl-like application but supports HTTP protocol only.\n"
            + "Usage:\n" + "\thttpc command [arguments]\n" + "The commands are:\n"
            + "\tget\texecutes a HTTP GET request and prints the response.\n"
            + "\tpost\texecutes a HTTP POST request and prints the response.\n" + "\thelp\tprints this screen.\n";

    public static void printErrAndExit(final String message) {
        System.out.println("== ERROR ========================");
        System.out.println("Reason: " + message + "\n");
        printHelp();
        System.exit(0);
    }

    public static void printHelp() {
        System.out.println(HELP_OUTPUT);
    }

    public static String extractFileContents(final String filePath) {
        // Load file relative to current class directory
        URL path = Httpc.class.getResource(filePath);
        try (BufferedReader reader = new BufferedReader(new FileReader(new File(path.getFile())))) {
            StringBuilder sb = new StringBuilder();

            String line = reader.readLine();

            while (line != null) {
                sb.append(line);
                sb.append("\n");
                line = reader.readLine();
            }

            return sb.toString();
        } catch (Exception e) {
            Utils.printErrAndExit(e.getMessage());
        }

        // Technically unreachable
        return "";
    }
}