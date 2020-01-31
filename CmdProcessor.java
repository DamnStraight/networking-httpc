import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class CmdProcessor {
    private final String[] cmdArgs;
    private HashMap<String, List<String>> mappedArgs;
    private String url;
    private HttpMethod method;

    public CmdProcessor(final String[] cmdArgs) {
        this.cmdArgs = cmdArgs;
        validate();
        validateArgs();
        processArgs();
    }

    public HashMap<String, List<String>> getMappedArgs() {
        return mappedArgs;
    }

    public String getUrl() {
        return url;
    }

    public boolean isPostRequest() {
        return this.method == HttpMethod.POST;
    }

    public String getMethod() {
        return this.method.toString();
    }

    /**
     * Validate our unprocessed arguments so we don't have to handle a bunch of
     * exceptions in our processor
     * 
     */
    boolean validate() {
        // Some assertions we need to handle
        // arg[0] - first argument should be the METHOD
        // arg[1] - should be a url

        if (cmdArgs.length < 2 || cmdArgs[0].equalsIgnoreCase("HELP")) {
            Utils.printErrAndExit("");
        }

        // Check that arg[0] is a valid Http method
        if (!Arrays.stream(HttpMethod.values()).anyMatch(e -> e.toString().equalsIgnoreCase(cmdArgs[0]))) {
            Utils.printErrAndExit("First argument should be a valid supported HTTP Method");
        } else {
            method = parseMethod(cmdArgs[0]);
        }

        // Check that arg[1] is a URL, includes basic validity check
        try {
            // This is a super basic url test that will still let
            // some malformed URL's through, but without using
            // a million character regex this will do
            URL maybeValid = new URL(cmdArgs[1]);

            this.url = cmdArgs[1];
            // maybeValid.toURI();
        } catch (Exception e) {
            // URL is bad
            Utils.printErrAndExit("Second argument should contain a valid URL");
        }

        if (HttpMethod.GET.toString().equalsIgnoreCase(cmdArgs[0])) {
            // Handle GET specific validation here

        } else {
            // Handle POST specific validation

            boolean hasBody = false;

            for (String arg : cmdArgs) {
                // Check that
                if (arg.equalsIgnoreCase(CmdArgs.FILE_DATA.asFlag())
                        || arg.equalsIgnoreCase(CmdArgs.INLINE_DATA.asFlag())) {
                    hasBody = true;
                }
            }

            if (!hasBody)
                Utils.printErrAndExit("POST Method missing either -d or -f flag");
        }

        return true;
    }

    /**
     * Ensure that all flags expecting a follow up argument are defined
     */
    void validateArgs() {
        boolean bodyFlagged = false;
        boolean verboseFlagged = false;

        // if (!Arrays.stream(HttpMethod.values()).anyMatch(e -> e.toString().equalsIgnoreCase(cmdArgs[2]))) {
        //     Utils.printErrAndExit("First argument should be a valid supported HTTP Method");
        // }

        // Start at 2 because we already ensured the first two arguments were specified
        for (int i = 2; i < cmdArgs.length; i++) {
            CmdArgs currentArg = parseArg(cmdArgs[i]);

            if (currentArg == null) {
                Utils.printErrAndExit("Invalid argument provided { " + cmdArgs[i] + " }");
            }

            switch (currentArg) {
                // -v
                case VERBOSE:
                    if (verboseFlagged) {
                        Utils.printErrAndExit("Duplicate arguments detected {" + cmdArgs[i] + "}");
                    }
                    verboseFlagged = true;
                    // If it's the -v flag, simply continue as we expect the next arg to be another
                    // flag
                    continue;
                // -d
                case INLINE_DATA:
                    // -f (Fallthrough to -f)
                case FILE_DATA:
                    // -h
                    if (bodyFlagged) {
                        Utils.printErrAndExit("Duplicate arguments detected {" + cmdArgs[i] + "}");
                    }
                    // Check that the next argument is indeed defined as we expect a followup value
                    // Also check that the next argument is not a flag
                    if ((i + 1) < cmdArgs.length && !cmdArgs[i + 1].startsWith("-")) {
                        bodyFlagged = true;

                        // Add one to our iterator so we skip the next argument as we determined it's
                        // not a flag
                        i++;
                        continue;
                    }
                    break;
                case HEADER:
                    if ((i + 1) < cmdArgs.length && isValidHeader(cmdArgs[i + 1])) {
                        // Add one to our iterator so we skip the next argument as we determined it's
                        // not a flag
                        i++;
                        continue;
                    }
                    Utils.printErrAndExit("Invalid header value provided");
                default:
                    Utils.printErrAndExit("Invalid flag or argument provided");
            }
        }
    }

    /**
     * Basic header validator, it can still be invalid, this simply checks that it
     * follows the format of String:String
     */
    boolean isValidHeader(String header) {
        return header.split(":").length == 2;
    }

    /**
     * Turn our array of arguments and values into an easier to use map
     */
    void processArgs() {
        HashMap<String, List<String>> mappedArgs = new HashMap<>();

        for (int i = 3; i < cmdArgs.length; i++) {
            // Remove the quotes around argument values
            // String strippedArg = cmdArgs[i].replaceAll("'", "").toLowerCase();

            // If the argument is not our URL, we have to treat it acccordingly based on the
            // parameter
            switch (cmdArgs[i]) {
                case "-v":
                    mappedArgs.put(cmdArgs[i], List.of("true"));
                    break;
                case "-d":
                case "-f":
                    mappedArgs.put(cmdArgs[i], List.of(cmdArgs[i + 1]));

                    // Increment so we skip over the next iteration, as we assume it was a value
                    i++;
                    break;
                case "-h":
                    if (mappedArgs.containsKey(cmdArgs[i])) {
                        mappedArgs.get(cmdArgs[i]).add(cmdArgs[i + 1]);
                    } else {
                        mappedArgs.put(cmdArgs[i], new LinkedList<>(Arrays.asList(cmdArgs[i + 1])));
                    }

                    // Increment so we skip over the next iteration, as we assume it was a value
                    i++;
                    break;
                default:
                    throw new IllegalArgumentException();
            }
        }

        this.mappedArgs = mappedArgs;
    }

    /**
     * Enum utility function for args so we can use our enum in a switch
     */
    public static CmdArgs parseArg(final String maybeEnum) {
        for (CmdArgs arg : CmdArgs.values()) {
            if (arg.asFlag().equalsIgnoreCase(maybeEnum)) {
                return arg;
            }
        }
        return null;
    }

    /**
     * Enum utility function for args so we can use our enum in a switch
     */
    public static HttpMethod parseMethod(final String maybeEnum) {
        for (HttpMethod method : HttpMethod.values()) {
            if (method.toString().equalsIgnoreCase(maybeEnum)) {
                return method;
            }
        }
        return null;
    }

    public static <E extends Enum<E>> E parse(Class<E> clazz, final String maybeEnum) {
        for (E method : clazz.getEnumConstants()) {
            if (method.toString().equalsIgnoreCase(maybeEnum)) {
                return method;
            }
        }
        return null;
    }

    public static enum HttpMethod {
        GET("GET"), POST("POST");

        private final String httpMethod;

        HttpMethod(final String httpMethod) {
            this.httpMethod = httpMethod;
        }

        public String toString() {
            return httpMethod;
        }
    }

    public static enum CmdArgs {
        VERBOSE("-v"), INLINE_DATA("-d"), FILE_DATA("-f"), HEADER("-h");

        private final String arg;

        CmdArgs(final String arg) {
            this.arg = arg;
        }

        public String asFlag() {
            return arg;
        }
    }
}