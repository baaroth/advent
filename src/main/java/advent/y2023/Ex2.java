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

        public boolean test(String draw) {
            Matcher m = p.matcher(draw);
            int shown = m.find() ? Integer.parseInt(m.group(1)) : 0;
            DEBUG.trace("- draw %d %s", shown, name());
            return shown <= limit;
        }
    }

    @Override
    public void accept(String game) {
        Matcher m = FMT.matcher(game);
        if (m.matches()) {
            int id = Integer.parseInt(m.group(1));
            String[] draws = m.group(2).split(";");
            DEBUG.trace("%ngame #%d", id);
            if (Arrays.stream(draws).allMatch(draw -> cubes.stream().allMatch(c -> c.test(draw)))) {
                sum += id;
            }
        }
    }

    public static void main(String[] args) throws IOException, URISyntaxException {
        Ex2 consumer = new Ex2();
        URI input = consumer.getClass().getResource("ex2.input.txt").toURI();
        Files.readAllLines(Path.of(input)).forEach(consumer);
        System.out.println(consumer.sum);
    }
}
