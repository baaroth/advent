package advent;

public enum Debug {
    ON(true),
    OFF(false);

    private final boolean on;
    private Debug(boolean on) {
        this.on = on;
    }

    public void lifePulse() {
        if (on) System.out.print('.');
    }

    public void trace(String format, Object... args) {
        if (on) {
            String lineFeed = format.endsWith("%n") ? format : format + "%n";
            System.out.printf(lineFeed, args);
        }
    }
}
