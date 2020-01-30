import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.io.PrintWriter;

public class Httpc {
    static final List<String> METHODS = Collections.unmodifiableList(List.of("GET", "POST"));

    static final String HELP_OUTPUT = "\n" +
        "httpc is a curl-like application but supports HTTP protocol only.\n" +
        "Usage:\n" +
        "\thttpc command [arguments]\n" +
        "The commands are:\n" +
        "\tget\texecutes a HTTP GET request and prints the response.\n" +
        "\tpost\texecutes a HTTP POST request and prints the response.\n" +
        "\thelp\tprints this screen.\n";

    static final String PROTOCOL = "HTTP/1.0";

    public static void main(String[] args) {
        if (args.length == 0 ||  args[0].equalsIgnoreCase("help")) {
            System.out.println(HELP_OUTPUT);
            System.exit(0);
        } else if (!METHODS.contains(args[0].toUpperCase())) {
            throw new IllegalArgumentException("HTTP Method required");
        }

        Map<String, List<String>> mappedArgs = processArgs(args);

        try {
            // New
            System.out.println("Connecting to: " + getHost(mappedArgs.get("url").get(0)));
            final InetAddress web = InetAddress.getByName(getHost(mappedArgs.get("url").get(0)));

            System.out.println(web.toString());
            Socket socket = new Socket(web, 80);

            System.out.println("Socket: " + socket.toString());

            PrintWriter out = new PrintWriter(socket.getOutputStream());
            // BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            Scanner in = new Scanner(socket.getInputStream());

            StringBuffer httpString = new StringBuffer();


            httpString.append(
                // METHOD URL VERSION
                String.format("%s %s %s\r\n", 
                    args[0].toUpperCase(),
                    getPath(mappedArgs.get("url").get(0)),
                    PROTOCOL
                )
            );

            System.out.println("Base: " + httpString.toString());

            if (mappedArgs.containsKey("-h")) {
                for (String header : mappedArgs.get("-h")) {
                    httpString.append(header + "\r\n");
                }
            }

            if (args[0].equalsIgnoreCase("GET")) {
                // Handle the GET case

                if (mappedArgs.containsKey("-f") || mappedArgs.containsKey("-d"))
                    printErrAndExit("'-d' and '-f' flags not supported by GET requests.");

            } else {
                // Handle POST and its possible flags
                final String jsonBody = getBody(mappedArgs);
                httpString.append("Content-Length: " + jsonBody.length() + "\r\n");
                httpString.append("\r\n" + jsonBody + "\r\n");
            }
            httpString.append("\r\n");

            out.write(httpString.toString());
            out.flush();
            
            while (in.hasNextLine()) {
                System.out.println(in.nextLine());
            }


            out.close();
            in.close();
            socket.close();
            
            System.exit(0);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 
     * 
     * @param cmdArgs Command line arguments
     * @return Map containing arguments that were used and their values
     */
    public static HashMap<String, List<String>> processArgs(String[] cmdArgs) {
        HashMap<String, List<String>> mappedArgs = new HashMap<>();

        // Wrap our loop in a try-catch, because we are going to be looking ahead optimistically
        // That is, we expect certain arguments to have a follow up value in the next
        // array index, if not, we can terminate gracefully
        try {
            for (int i = 1; i < cmdArgs.length; i++) {
                // Remove the quotes around argument values
                String strippedArg = cmdArgs[i].replaceAll("'", "").toLowerCase();

                // Check if the argument is our URL
                if (strippedArg.startsWith("http://")) {
                    throwOnDuplicate(mappedArgs, "url");
                    mappedArgs.put("url", List.of(strippedArg));
                } else {
                    // If the argument is not our URL, we have to treat it acccordingly based on the
                    // parameter
                    switch (strippedArg) {
                        case "-v":
                            mappedArgs.put(strippedArg, List.of("true"));
                            break;
                        case "-d":
                        case "-f":
                            // Ensure that only one is mapped, can't have both!
                            throwOnDuplicate(mappedArgs, "-d");
                            throwOnDuplicate(mappedArgs, "-f");

                            mappedArgs.put(cmdArgs[i], List.of(cmdArgs[i + 1].replaceAll("'", "")));

                            // Increment so we skip over the next iteration, as we assume it was a value
                            i++;
                            break;
                        case "-h":
                            if (mappedArgs.containsKey(cmdArgs[i])) {
                                mappedArgs.get(cmdArgs[i]).add(cmdArgs[i + 1]);
                            } else {
                                mappedArgs.put(cmdArgs[i],
                                        new LinkedList<>(Arrays.asList(cmdArgs[i + 1].replaceAll("'", ""))));
                            }

                            // Increment so we skip over the next iteration, as we assume it was a value
                            i++;
                            break;
                        default:
                            throw new IllegalArgumentException();
                    }
                }

            }
        } catch (IndexOutOfBoundsException e) {
            System.out.println("Ensure all arguments are followed by a value if required, see 'httpc help' for more info.");
            System.exit(0);
        } catch (IllegalArgumentException e) {
            System.out.println("Ensure all arguments are followed by a value");
            System.exit(0);
        }

        return mappedArgs;
    }

    public static void throwOnDuplicate(HashMap<String, List<String>> args, String key) {
        if (args.containsKey(key))
            throw new IllegalArgumentException();
    }

    public static void printErrAndExit(String errorMessage) {
        System.out.println("== ERROR ========================");
        System.out.println("Reason: " + errorMessage);
        System.exit(0);
    }

    public static String getBody(Map<String, List<String>> mappedArgs) {
        String jsonOutput = "";

        if (mappedArgs.containsKey("-f") && mappedArgs.containsKey("-d")) {
            printErrAndExit("POST can contain the '-d' or '-f' flag but not both!");
        } else if (!mappedArgs.containsKey("-f") && !mappedArgs.containsKey("-d")) {
            printErrAndExit("Missing -d / -f flag");
        } else if (mappedArgs.containsKey("-f")) {
            // Load file
            URL path = Httpc.class.getResource(mappedArgs.get("-f").get(0));
            try (BufferedReader reader = new BufferedReader(new FileReader(new File(path.getFile())))) {
                StringBuilder sb = new StringBuilder();

                String line = reader.readLine();

                while (line != null) {
                    sb.append(line);
                    sb.append("\n");
                    line = reader.readLine();
                }

                jsonOutput = sb.toString();
            } catch (Exception e) {
                printErrAndExit(e.getMessage());
            }
        } else if (mappedArgs.containsKey("-d")) {
            jsonOutput = mappedArgs.get("-d").get(0);
        }

        return jsonOutput;
    }

    static String getHost(String url) {
        String path = "";
        try {
            path = new URL(url).getHost();
        } catch (Exception e) {
            //If we catch something it's invalid
            printErrAndExit("Invalid URL provided");
        }
        
        return path.equals("") ? "/" : path;
    }

    static String getPath(String url) {
        String path = "";
        try {
            path = new URL(url).getPath();
        } catch (Exception e) {
            //If we catch something it's invalid
            printErrAndExit("Invalid URL provided");
        }
        
        return path.equals("") ? "/" : path;
    }
}