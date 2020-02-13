public class Httpc {
    public static void main(String[] args) {
        Request request = new CommandProcessor(args).getRequest();

        System.out.println(request);
    }
}