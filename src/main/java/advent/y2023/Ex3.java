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
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Math.abs;

public class Ex3   implements Consumer<String> {
    private static final Debug DEBUG = Debug.OFF;
    private static final Pattern NUM = Pattern.compile("(\\d+)");
    private static final Pattern GEAR = Pattern.compile("\\*");

    private int sum = 0;
    private List<Pos> previousGears = List.of();
    private List<PartNum> previousParts = List.of();
    private List<PartNum> previousParts2 = List.of();

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
        List<Pos> gears = find(GEAR, line, Pos::new);
        List<PartNum> parts = find(NUM, line, PartNum::new);
        handleGears(parts);
        // memorize for next line
        previousParts2 = previousParts;
        previousParts = parts;
        previousGears = gears;
    }

    private void handleGears(List<PartNum> parts) {
        DEBUG.trace("---");
        List<PartNum> partsWindow = new ArrayList<>(previousParts2);
        partsWindow.addAll(previousParts);
        partsWindow.addAll(parts);

        for (Pos gear : previousGears) {
            List<PartNum> touching = partsWindow.stream().filter(p -> p.touches(gear)).toList();
            if (touching.size() == 2) {
                int ratio = touching.get(0).val() * touching.get(1).val();
                DEBUG.trace("%s → %d", touching, ratio);
                sum += ratio;
            }
        }
    }

    private <O> List<O> find(Pattern p, String line, Function<Matcher, O> mapper) {
        List<O> out = new ArrayList<>();
        Matcher m = p.matcher(line);
        while (m.find()) {
            out.add(mapper.apply(m));
        }
        return out;
    }

    public static void main(String[] args) throws IOException, URISyntaxException {
        Ex3 consumer = new Ex3();
        URI input = consumer.getClass().getResource("ex3.input.txt").toURI();
        Files.readAllLines(Path.of(input)).forEach(consumer);
        consumer.handleGears(List.of()); // simule une ligne vide pour gérer les engrenages de la dernière ligne du fichier
        System.out.println(consumer.sum);
    }
}
