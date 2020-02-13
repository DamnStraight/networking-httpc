import constants.Method;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.net.URL;

import constants.Messages;;

public class Utils {
    public static void printHelpAndExit() {
        System.out.println(Messages.HELP_DEFAULT);
        System.exit(0);
    }

    private static void printHelpAndExit(final String message) {
        System.out.println(message);
        System.exit(0);
    }

    public static void printHelpAndExit(final Method method) {
        if (method == null) {
            printHelpAndExit();
        }

        if (method == Method.GET) {
            printHelpAndExit(Messages.HELP_GET);
        }

        printHelpAndExit(Messages.HELP_POST);
    }

    /**
     * Takes an enum class and a string and if the String matches one of the enums
     * values, returns the corresponding enum value.
     * 
     * @param clazz     Enum class to iterate through
     * @param maybeEnum String with a name potentially the same as a enums contained
     *                  value
     * @return Matching enum or null
     */
    public static <E extends Enum<E>> E parse(Class<E> clazz, final String maybeEnum) {
        for (E enumType : clazz.getEnumConstants()) {
            if (enumType.toString().equalsIgnoreCase(maybeEnum)) {
                return enumType;
            }
        }
        return null;
    }

    public static String extractFileContents(final String filePath) {
        // Load file relative to current class directory
        URL path = Httpc.class.getResource(filePath);

        try (  
            BufferedReader reader = new BufferedReader(new FileReader(new File(path.getFile())));
        ) {
            StringBuilder sb = new StringBuilder();

            String line = reader.readLine();

            while (line != null) {
                sb.append(line);
                sb.append("\n");
                line = reader.readLine();
            }

            return sb.toString();

        } catch (Exception e) {
            printHelpAndExit();
        }

        // Technically unreachable
        return "";
    }

    public static void outputToFile(String fileName, String output) {
        if (!fileName.contains(".txt"))
            fileName += ".txt";

        try (
            PrintWriter writer = new PrintWriter(fileName); 
        ) {
            writer.println(output);
        } catch (Exception e) {
            System.out.println("Error writing to file: " + fileName);
        }
    }
}