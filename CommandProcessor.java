import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import constants.Argument;
import constants.Method;

/**
 * Takes an array of command arguments and turns it into a Request object with all relvant data extracted
 */
public class CommandProcessor {
    private final List<Argument> DUPLICATE_ARGUMENTS = Arrays.asList(Argument.HEADER);
    private final String[] args;
    private Request request;

    public CommandProcessor(final String[] args) {
        this.request = new Request();
        this.args = args;
        evaluate();
    }

    /**
     * Get the Request object created from the command arguments
     * @return
     */
    public Request getRequest() {
        return this.request;
    }

    /**
     * General validation on command line args before splitting and handling
     * validation on a method specific basis
     */
    private void evaluate() {
        if (args.length == 0) {
            Utils.printHelpAndExit();
        }

        final Method method = Utils.parse(Method.class, args[0]);

        // If the method is null or set to HELP print help and exit
        if (method == null || method == Method.HELP) {
            helpOutputHandler();
        }

        // Perform basic validation on the URL field (Should be second argument)
        processUrl(args[1]);

        // Check that arguments don't occur more than once
        // (Except those we define as allowed, such as headers)
        validateDuplicates();

        request.setMethod(method);

        switch (method) {
        case GET:
            getEvaluator();
            break;
        case POST:
            postEvaluator();
            break;
        case HELP:
        default:
            Utils.printHelpAndExit();
        }

    }

        /**
     * Handle validations specific to a GET request
     */
    private void getEvaluator() {
        // If the argument length on a GET request is less than 2 it's invalid
        // At the bare minimum we require: 'GET http://some-url.com'
        if (args.length < 2) {
            Utils.printHelpAndExit();
        }

        for (int i = 2; i < args.length; i++) {
            final Argument currentArg = Utils.parse(Argument.class, args[i]);

            if (currentArg == null) {
                Utils.printHelpAndExit();
            }

            switch (currentArg) {
            case VERBOSE:
                this.request.setVerbose(true);
                continue;
            case HEADER:
                if (isValidNextArg(i, args)) {
                    processHeader(args[++i]);
                    continue;
                }
            case OUTPUT_FILE:
                if (isValidNextArg(i, args)) {
                    this.request.setOutputFile(args[++i]);
                    continue;
                }
            default:
                Utils.printHelpAndExit();
            }
        }
    }

    /**
     * Handle validations specific to a POST request
     */
    private void postEvaluator() {
        // If the arg length is less than 4 on a POST request, the request is invalid
        // At the bare minimum we require: 'POST http://some-url.com/ (-d|-f) ("inline
        // data"|"filename.txt")'
        if (args.length < 4) {
            Utils.printHelpAndExit();
        }

        // if (Arrays.stream(args).anyMatch(x -> x.equalsIgnoreCase(Argument.)))

        for (int i = 2; i < args.length; i++) {
            final Argument currentArg = Utils.parse(Argument.class, args[i]);

            if (currentArg == null) {
                Utils.printHelpAndExit();
            }

            switch (currentArg) {
            case VERBOSE:
                this.request.setVerbose(true);
                continue;
            case HEADER:
                if (isValidNextArg(i, args)) {
                    processHeader(args[++i]);
                    continue;
                }
            case INLINE_DATA:
                if (isValidNextArg(i, args)) {
                    processInlineData(args[++i]);
                    continue;
                }
            case FILE_DATA:
                if (isValidNextArg(i, args)) {
                    processFileInput(args[++i]);
                    continue;
                }
            case OUTPUT_FILE:
                if (isValidNextArg(i, args)) {
                    this.request.setOutputFile(args[++i]);
                    continue;
                }
            default:
                // Don't break
                Utils.printHelpAndExit();
            }
        }
    }

    /**
     * Handle cases with the help command
     * eg. 'help', 'help get' or 'help post'
     */
    private void helpOutputHandler() {
        if (args.length >= 2) {
            final Method method = Utils.parse(Method.class, args[2]);
            Utils.printHelpAndExit(method);
        }

        Utils.printHelpAndExit();
    }

    // ============================== VALIDATION FUNCTIONS ============================== //

    /**
     * With command line arguments we are doing optimistic lookaheads for the value
     * associated with each argument so we make sure
     * 
     * @param currentIndex Index of the current element being accessed
     * @param args
     * @return
     */
    private boolean isValidNextArg(final int currentIndex, final String[] args) {
        final int nextIndex = currentIndex + 1;

        return nextIndex < args.length && Utils.parse(Argument.class, args[nextIndex]) == null;
    }

    /**
     * Validate provided URL and break it into useful pieces we will use in our HTTP request
     * @param maybeURL
     */
    private void processUrl(final String maybeURL) {
        try {
            // Basic URL validation, this will throw on certain malformed URLs
            URL url = new URL(maybeURL);

            if (url.getPath().trim() != "") {
                String queryParameters = "";

                if (url.getQuery() != null) {
                    queryParameters = "?" + url.getQuery();
                }

                this.request.setPath(url.getPath() + queryParameters);
            } else {
                this.request.setPath("/");
            }

            this.request.setHost(url.getHost());
            // this.request.setHost(
            //     String.format("%s://%s", url.getProtocol(), url.getHost())
            // );
            this.request.setUrl(maybeURL);
        } catch (Exception e) {
            // URL is bad
            System.out.println("Error parsing URL");
            Utils.printHelpAndExit();
        }
    }

    /**
     * Validate potential header string and append it to the Request object on
     * success
     * 
     * @param maybeHeader Header stirng
     */
    private void processHeader(final String maybeHeader) {
        String[] splitHeader = maybeHeader.split(":");

        if (splitHeader.length != 2) {
            Utils.printHelpAndExit();
        }

        this.request.insertHeader(maybeHeader);
    }

    /**
     * Validate that a duplicate flag was not set and apply the body to the request
     * object
     * 
     * @param inlineData
     */
    private void processInlineData(final String inlineData) {
        if (this.request.getBody() != null) {
            // Tried to set both inline data and file data
            Utils.printHelpAndExit();
        }

        this.request.setBody(inlineData);
    }

    /**
     * Validate that a duplicate flag was not set and attempt to extract body from
     * file and apply it to request object
     * 
     * @param file
     */
    private void processFileInput(final String file) {
        if (this.request.getBody() != null) {
            // Tried to set both inline data and file data
            Utils.printHelpAndExit();
        }

        this.request.setBody(Utils.extractFileContents(file));
    }

    /**
     * Check that
     */
    private void validateDuplicates() {
        // Create a map with the argument as the key, and the occurence as it's value
        Map<String, Long> valueCount = Arrays.stream(args)
                .collect(Collectors.groupingBy(x -> x, Collectors.counting()));

        valueCount.entrySet().stream().forEach(entry -> {
            final Argument arg = Utils.parse(Argument.class, entry.getKey());

            // If the argument is not in our allowed duplicates array, and its count is
            // greater than 1, throw an error
            if (!DUPLICATE_ARGUMENTS.contains(arg) && entry.getValue() > 1) {
                System.out.println("Duplicate arguments");
                Utils.printHelpAndExit();
            }
        });
    }
}