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
            .setBody(mappedArgs)
            .submit(mappedArgs.containsKey("-v"));

        System.out.println(response);
        System.exit(0);
    }
}