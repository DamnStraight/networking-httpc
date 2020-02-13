package constants;

public enum Method {
    GET("get"), POST("post"), HELP("help");

    private final String method;

    Method(final String method) {
        this.method = method;
    }

    public String toString() {
        return method;
    }
}