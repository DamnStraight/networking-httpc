package constants;

public enum Method {
    GET("GET"), POST("POST"), HELP("HELP");

    private final String method;

    Method(final String method) {
        this.method = method;
    }

    public String toString() {
        return method;
    }
}