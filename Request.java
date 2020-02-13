
import java.util.ArrayList;

import constants.Method;

public class Request {
    private Method method = null;
    private boolean verbose = false;

    private String url = null;
    private String path = null;
    private String host = null;

    private ArrayList<String> headers = new ArrayList<>();
    private String body = null;
    private String outputFile = null;

    public Request() {
    }

    public String getHost() {
        return this.host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getPath() {
        return this.path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Request path(String path) {
        this.path = path;
        return this;
    }

    public void insertHeader(final String header) {
        this.headers.add(header);
    }

    public Method getMethod() {
        return this.method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public boolean isVerbose() {
        return this.verbose;
    }

    public boolean getVerbose() {
        return this.verbose;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    public String getUrl() {
        return this.url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public ArrayList<String> getHeaders() {
        return this.headers;
    }

    public void setHeaders(ArrayList<String> headers) {
        this.headers = headers;
    }

    public String getBody() {
        return this.body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getOutputFile() {
        return this.outputFile;
    }

    public void setOutputFile(String outputFile) {
        this.outputFile = outputFile;
    }

    public Request method(Method method) {
        this.method = method;
        return this;
    }

    public Request verbose(boolean verbose) {
        this.verbose = verbose;
        return this;
    }

    public Request url(String url) {
        this.url = url;
        return this;
    }

    public Request headers(ArrayList<String> headers) {
        this.headers = headers;
        return this;
    }

    public Request body(String body) {
        this.body = body;
        return this;
    }

    public Request outputFile(String outputFile) {
        this.outputFile = outputFile;
        return this;
    }

    @Override
    public String toString() {
        return "{" + " method='" + getMethod() + "'" + ", verbose='" + isVerbose() + "'" + ", url='" + getUrl() + "'"
                + ", path='" + getPath() + "'" + ", host='" + getHost() + "'" + ", headers='" + getHeaders() + "'"
                + ", body='" + getBody() + "'" + ", outputFile='" + getOutputFile() + "'" + "}";
    }

}
