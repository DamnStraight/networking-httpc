public enum CmdArgs {
    VERBOSE("-v"),
    INLINE_DATA("-f"),
    FILE_DATA("-d"),
    HEADER("-h");

    private final String arg;

    CmdArgs(final String arg) {
        this.arg = arg;
    }

    public String toString() {
        return arg;
    }
}