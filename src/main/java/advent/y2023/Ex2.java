package advent.y2023;

import advent.Debug;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Ex2  implements Consumer<String> {
    private static final Debug DEBUG = Debug.OFF;
    private static final Pattern FMT = Pattern.compile("^Game (\\d+): (.+)$");

    private int sum = 0;
    private List<Cube> cubes = List.of(Cube.values());

    private enum Cube implements Predicate<String> {
        RED(12),
        GREEN(13),
        BLUE(14);

        final int limit;
        final Pattern p;
        Cube(int limit) {
            this.limit = limit;
            p = Pattern.compile("(\\d+) " + name().toLowerCase());
        }

        public int count(String draw) {
            Matcher m = p.matcher(draw);
            return m.find() ? Integer.parseInt(m.group(1)) : 0;
        }
        public boolean test(String draw) {
            int shown = count(draw);
            DEBUG.trace("- draw %d %s", shown, name());
            return shown <= limit;
        }
    }

    private static class AtLeast {
        private int val;

        void update(int challenge) {
            if (val < challenge) {
                val = challenge;
            }
        }
    }

    private static class Mins implements Consumer<String> {
        private final AtLeast reds = new AtLeast();
        private final AtLeast greens = new AtLeast();
        private final AtLeast blues = new AtLeast();

        @Override
        public void accept(String draw) {
            reds.update(Cube.RED.count(draw));
            greens.update(Cube.GREEN.count(draw));
            blues.update(Cube.BLUE.count(draw));
        }

        int power() {
            return reds.val * greens.val * blues.val;
        }
    }

    @Override
    public void accept(String game) {
        Matcher m = FMT.matcher(game);
        if (m.matches()) {
            int id = Integer.parseInt(m.group(1));
            Mins updateMins = new Mins();
            String[] draws = m.group(2).split(";");
            Arrays.stream(draws).forEach(updateMins);
            int power = updateMins.power();
            DEBUG.trace("game #%d: %d", id, power);
            sum += power;
        }
    }

    public static void main(String[] args) throws IOException, URISyntaxException {
        Ex2 consumer = new Ex2();
        URI input = consumer.getClass().getResource("ex2.input.txt").toURI();
        Files.readAllLines(Path.of(input)).forEach(consumer);
        System.out.println(consumer.sum);
    }
}
