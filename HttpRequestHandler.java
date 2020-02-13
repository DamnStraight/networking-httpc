import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Scanner;

import constants.Method;

/**
 * Handles data provided from a Request object and formats it for, 
 * HTTP requests, processing responses and output
 */
public class HttpRequestHandler {
    private final Request request;
    private String formattedRequest;
    private String responseHeaders;
    private String response;

    private final String HTTP_PROTOCOL = "HTTP/1.0";
    private final int HTTP_PORT = 80;

    public HttpRequestHandler(Request request) {
        this.request = request;
    }

    public HttpRequestHandler formatRequest() {
        StringBuilder requestBuilder = new StringBuilder();
        List<String> headers = this.request.getHeaders();
        String body = this.request.getBody();

        // Format our request line
        // METHOD URL HTTPVER -> 'GET /get?assignment=1 HTTP/1.0'
        requestBuilder.append(
            String.format("%s %s %s\r\n", 
                request.getMethod().toString(), 
                request.getPath(), 
                HTTP_PROTOCOL
            )
        ); 

        // Check to see that the Content-Length header was defined, and if not, insert it if necessary
        applyContentLength(headers, body);

        // If we have any headers, iterate through them and append them
        headers
            .stream()
            .forEach( header -> requestBuilder.append(header + "\r\n") );

        // Line seperator between headers and body (Or signalling end of file if a get request)
        requestBuilder.append("\r\n");

        // Handle POST entity body
        
        Method requestMethod = this.request.getMethod();

        if (requestMethod.equals(Method.POST) && body != null) {
            requestBuilder.append(body);
        }

        this.formattedRequest = requestBuilder.toString();

        return this;
    }

    public HttpRequestHandler submitRequest() {
        final String host = this.request.getHost();
        StringBuilder responseBuilder = new StringBuilder();

        // Attempt to connect to our provided host URL
        InetAddress web = null;
        try {
            web = InetAddress.getByName(host);
        } catch (UnknownHostException e) {
            System.out.println("Error connecting to host: " + host);
            e.printStackTrace();
            System.exit(0);
        }

        try (
            Socket connection = new Socket(web, HTTP_PORT);
            PrintWriter outputStream = new PrintWriter(connection.getOutputStream());
            Scanner inputStream = new Scanner(connection.getInputStream());
        ) {
            // Submit our request
            outputStream.write(this.formattedRequest);
            outputStream.flush();

            // Store the response in our string variables
            String currentLine;
            boolean headersCaptured = false;
            while(inputStream.hasNextLine()) {
                currentLine = inputStream.nextLine();

                // Check for our seperator between the headers (technically our verbose output) and the entity body
                if (currentLine.trim().equals("") && !headersCaptured) {
                    this.responseHeaders = responseBuilder.toString();

                    // Reset the buffer
                    responseBuilder.setLength(0);
                    headersCaptured = true;
                }

                responseBuilder.append(currentLine + "\n");
            }

            // Store our entity body response
            this.response = responseBuilder.toString();

        } catch (Exception e) {
            System.out.println("Error communicating with host");
            System.exit(0);
        }

        return this;
    }

    /**
     * Outputs the response based on the users command line requets:
     * Handles verbose output and file output.
     */
    public void outputResponse() {
        final String maybeFile = this.request.getOutputFile();

        // Format our output based on whether they selected verbose output or not
        String output = String.format("%s%s", 
            this.request.isVerbose() ? responseHeaders : "", 
            response
        );

        // If an output file is defined, print to it otherwise, output to console
        if (maybeFile != null) {
            Utils.outputToFile(maybeFile, output);
        } else {
            System.out.println(output);
        }
    }

    /**
     * Apply the Content-Length header with the appropriate body size if appropriate
     */
    private void applyContentLength(List<String> headers, String body) {
        final String HEADER_KEY = "Content-Length";
        boolean hasContentLength = headers
            .stream()
            .anyMatch( header -> 
                header
                    .split(":")[0]
                    .trim()
                    .equalsIgnoreCase(HEADER_KEY)
            );

        if (!hasContentLength && body != null) {
            this.request.insertHeader(
                String.format("%s:%d", HEADER_KEY, body.length())
            );
        }
    }

    @Override
    public String toString() {
        return "{" +
            ", formattedRequest='" + this.formattedRequest + "'" +
            ", responseHeaders='" + this.responseHeaders + "'" +
            ", response='" + this.response + "'" +
            "}";
    }

}