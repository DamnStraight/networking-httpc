package constants;

public class Messages {
    public static final String HELP_DEFAULT = "\n" + "httpc is a curl-like application but supports HTTP protocol only." + "\n"
            + "Usage:\n" + "\thttpc command [arguments]" + "\n" + "The commands are:" + "\n\t"
            + "get\texecutes a HTTP GET request and prints the response." + "\n\t"
            + "post\texecutes a HTTP POST request and prints the response.\n" + "\thelp\tprints this screen.\n";

    public static final String HELP_GET = "\n" + "usage: httpc get [-v] [-h key:value] URL"
            + "\n\nGet executes a HTTP GET request for a given URL." + "\n"
            + "-v\tPrints the detail of the response such as protocol, status,and headers." + "\n"
            + "-h\tkey:value Associates headers to HTTP Request with the format 'key:value'." + "\n"
            + "-o\tOutput the response contents into given filename.";

    public static final String HELP_POST = "\n" + "usage: httpc post [-v] [-h key:value] [-d inline-data] [-f file] URL"
            + "\n\n" + "Post executes a HTTP POST request for a given URL with inline data or from file." + "\n"
            + "-v\tPrints the detail of the response such as protocol, status,and headers." + "\n"
            + "-h\tkey:value Associates headers to HTTP Request with the format 'key:value'." + "\n"
            + "-d\tstring Associates an inline data to the body HTTP POST request." + "\n"
            + "-f\tfile Associates the content of a file to the body HTTP POST request." + "\n"
            + "-o\tOutput the response contents into given filename.";
}