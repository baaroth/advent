package advent.y2023;

import advent.Debug;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Math.abs;

public class Ex3   implements Consumer<String> {
    private static final Debug DEBUG = Debug.OFF;
    private static final Pattern NUM = Pattern.compile("(\\d+)");
    private static final Pattern SYM = Pattern.compile("[^0-9.]");

    private int sum = 0;
    private List<PartNum> previousParts = List.of();
    private List<Pos> previousSymbols = List.of();

    record Pos(int start, int end) {
        Pos(Matcher m) {
            this(m.start(), m.end());
        }

        boolean includes(Pos other) {
            return start <= other.start && end >= other.end();
        }
        boolean touches(Pos other) {
            return abs(start - other.start) <= 1
                    || abs(end - other.end) <= 1
                    || includes(other);
        }

        @Override
        public String toString() {
            return "(%d,%d)".formatted(start, end);
        }
    }
    record PartNum(int val, Pos pos) {
        PartNum(Matcher m) {
            this(Integer.parseInt(m.group(1)), new Pos(m));
        }

        boolean touches(Pos p) {
            return pos.touches(p);
        }
        @Override
        public String toString() {
            return "%s:%d".formatted(pos, val);
        }
    }

    @Override
    public void accept(String line) {
        List<Pos> symbols = findSymbols(line);
        previousParts = handleParts(line, symbols);
        previousSymbols = symbols;
    }

    private List<PartNum> handleParts(String line, List<Pos> symbols) {
        Matcher mNum = NUM.matcher(line);
        List<PartNum> unaffected = new ArrayList<>();

        DEBUG.trace("---");
        while (mNum.find()) {
            PartNum p = new PartNum(mNum);
            if (symbols.stream().anyMatch(p::touches)
                    || previousSymbols.stream().anyMatch(p::touches)) {
                DEBUG.trace("%s", p);
                sum += p.val();
            } else {
                DEBUG.trace("¬%s", p);
                unaffected.add(p);
            }
        }

        for (PartNum previous : previousParts) {
            if (symbols.stream().anyMatch(previous::touches)) {
                DEBUG.trace("%s", previous);
                sum += previous.val();
            }
        }
        return unaffected;
    }

    private List<Pos> findSymbols(String line) {
        List<Pos> symbols = new ArrayList<>();
        Matcher m = SYM.matcher(line);
        while (m.find()) {
            symbols.add(new Pos(m));
        }
        return symbols;
    }

    public static void main(String[] args) throws IOException, URISyntaxException {
        Ex3 consumer = new Ex3();
        URI input = consumer.getClass().getResource("ex3.input.txt").toURI();
        Files.readAllLines(Path.of(input)).forEach(consumer);
        System.out.println(consumer.sum);
    }
}
