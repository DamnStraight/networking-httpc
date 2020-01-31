import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class HttpServer {
    public static class HttpServerBuilder {
        static final String PROTOCOL = "HTTP/1.0";

        private Socket socket;
        private PrintWriter out;
        private Scanner in;
        private StringBuffer request;

        private String url;
        private String method;

        public HttpServerBuilder(final String url) {
            this.url = url;
        }

        public HttpServerBuilder createConnection() {
            try {
                final InetAddress web = InetAddress.getByName(getHost(this.url));
                this.socket = new Socket(web, 80);
                this.out = new PrintWriter(socket.getOutputStream());
                this.in = new Scanner(socket.getInputStream());
                this.request = new StringBuffer();

            } catch (Exception e) {
                Utils.printErrAndExit(e.getMessage());
            }

            return this;
        }

        public HttpServerBuilder initRequestLine(final String method) {
            this.method = method;

            request.append(
                    // METHOD URL VERSION
                    String.format("%s %s %s\r\n", method, getPath(url), PROTOCOL));

            return this;
        }

        public HttpServerBuilder setHeaders(List<String> headers) {
            if (headers != null) {
                for (String header : headers) {
                    request.append(header + "\r\n");
                }
            }
            return this;
        }

        public HttpServerBuilder setBody(Map<String, List<String>> mappedArgs) {
            if (this.method.equalsIgnoreCase("POST")) {
                String body = getBody(mappedArgs);
                request.append("Content-Length: " + body.length() + "\r\n");
                request.append("\r\n" + body);
            }
            request.append("\r\n");

            return this;
        }

        public String submit(final boolean verbose) {
            out.write(request.toString());
            out.flush();

            request.delete(0, request.length());

            boolean printDetailed = verbose;
            boolean headerComplete = false;

            String line;
            while (in.hasNextLine()) {
                line = in.nextLine();

                if (printDetailed || headerComplete) {
                    request.append(line + "\n");
                } else {
                    headerComplete = line.trim().equals("");
                }
            }

            String temp = request.toString();

            try {
                socket.close();
                out.close();
                in.close();
            } catch (Exception e) {}

            return temp;
        }

        private String getHost(String url) {
            String path = "";
            try {
                path = new URL(url).getHost();
            } catch (Exception e) {
                // If we catch something it's invalid
                Utils.printErrAndExit("Invalid URL provided");
            }

            return path.equals("") ? "/" : path;
        }

        private String getPath(String urlString) {
            String path = "";
            try {
                URL url = new URL(urlString);
                path = url.getPath();

                String query = url.getQuery();
                if (query != null) {
                    path += "?" + query;
                }

            } catch (Exception e) {
                // If we catch something it's invalid
                Utils.printErrAndExit("Invalid URL provided");
            }

            return path.equals("") ? "/" : path;
        }

        private String getBody(Map<String, List<String>> mappedArgs) {
            String jsonOutput = "";

            if (mappedArgs.containsKey("-f")) {
                return Utils.extractFileContents(mappedArgs.get("-f").get(0));
            } else if (mappedArgs.containsKey("-d")) {
                jsonOutput = mappedArgs.get("-d").get(0);
            }

            return jsonOutput;
        }
    }
}