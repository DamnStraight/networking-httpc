public class Httpc {
    public static void main(String[] args) {
        Request request = new CommandProcessor(args).getRequest();

        HttpRequestHandler handler = new HttpRequestHandler(request);
 
        handler
            .formatRequest()
            .submitRequest()
            .outputResponse();
    }
}