package constants;

public enum Argument {
    VERBOSE("-v"), HEADER("-h"), INLINE_DATA("-d"), FILE_DATA("-f"), OUTPUT_FILE("-o");

    private final String arg;

    Argument(final String arg) {
        this.arg = arg;
    }

    public String asFlag() {
        return arg;
    }

    public String toString() {
        return arg;
    }
}