public class Utils {
    static final String HELP_OUTPUT = "\n" +
        "httpc is a curl-like application but supports HTTP protocol only.\n" +
        "Usage:\n" +
        "\thttpc command [arguments]\n" +
        "The commands are:\n" +
        "\tget\texecutes a HTTP GET request and prints the response.\n" +
        "\tpost\texecutes a HTTP POST request and prints the response.\n" +
        "\thelp\tprints this screen.\n";

    public static void printErrAndExit(final String message) {
        System.out.println("== ERROR ========================");
        System.out.println("Reason: " + message + "\n");
        printHelp();
        System.exit(0);
    }

    public static void printHelp() {
        System.out.println(HELP_OUTPUT);
    }
}