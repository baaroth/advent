package advent;

public enum Debug {
    ON(true),
    OFF(false);

    private final boolean on;
    private Debug(boolean on) {
        this.on = on;
    }

    public void trace(String format, Object... args) {
        if (on) {
            System.out.printf(format, args);
        }
    }
}
