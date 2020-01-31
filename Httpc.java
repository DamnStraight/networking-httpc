import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.URL;
import java.util.List;
import java.util.Map;

public class Httpc {
    public static void main(String[] args) {
        CmdProcessor processor = new CmdProcessor(args);

        Map<String, List<String>> mappedArgs = processor.getMappedArgs();

        String response = new HttpServer.HttpServerBuilder(processor.getUrl())
            .createConnection()
            .initRequestLine(processor.getMethod())
            .setHeaders(mappedArgs.get("-h"))
            .setBody(processor.getMappedArgs())
            .submit();

        System.out.println(response);
        System.exit(0);
    }
}