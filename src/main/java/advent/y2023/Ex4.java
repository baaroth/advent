package advent.y2023;

import advent.Debug;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

public class Ex4 implements Consumer<String> {
    private static final Debug DEBUG = Debug.OFF;
    private static final Pattern FMT = Pattern.compile("^Card +(\\d+): ([^|]+)\\|(.+)$");
    private long sum = 0;
    private int[] winning;

    @Override
    public void accept(String card) {
        Matcher m = FMT.matcher(card);
        if (m.matches()) {
            int id = Integer.parseInt(m.group(1));
            winning = toInts(m.group(2)).toArray();
            long wins = toInts(m.group(3)).filter(this::isWin).count();
            DEBUG.trace("%d: %d wins", id, wins);
            if (wins > 0) {
                long val = (long) Math.pow(2, wins - 1);
                DEBUG.trace("  → %d pts", val);
                sum += val;
            }
        }
    }

    private boolean isWin(int i) {
        for (int w : winning) {
            if (w == i) return true;
        }
        return false;
    }

    private static IntStream toInts(String str) {
        return Arrays.stream(str.trim().split(" +"))
                .mapToInt(Integer::parseInt);
    }

    public static void main(String[] args) throws IOException, URISyntaxException {
        Ex4 consumer = new Ex4();
        URI input = consumer.getClass().getResource("ex4.input.txt").toURI();
        Files.readAllLines(Path.of(input)).forEach(consumer);
        System.out.println(consumer.sum);
    }
}
