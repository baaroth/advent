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

import static java.util.stream.Collectors.joining;

public class Ex4 implements Consumer<String> {
    private static final Debug DEBUG = Debug.OFF;
    private static final Pattern FMT = Pattern.compile("^Card +(\\d+): ([^|]+)\\|(.+)$");
    private long sum = 0;
    private int[] winning;
    private final Gains gains = new Gains();

    private static class Gains {
        private int capacity = 10;
        private int[] values = new int[capacity];

        void inc(int val, int size) {
            if (size > capacity) {
                DEBUG.trace("resize %d→%d", capacity, size);
                int[] bigger = new int[size];
                for (int i = 0; i < capacity; ++i) {
                    bigger[i] = values[i] + val;
                }
                Arrays.fill(bigger, capacity, size, val);
                values = bigger;
                capacity = size;
            } else {
                for (int i = 0; i < size; ++i) {
                    values[i] += val;
                }
            }
        }

        int shift() {
            int val = values[0];
            if (val != 0) {
                System.arraycopy(values, 1, values, 0, capacity - 1);
                values[capacity - 1] = 0;
            }
            return val;
        }

        @Override
        public String toString() {
            return Arrays.stream(values)
                    .mapToObj(String::valueOf)
                    .collect(joining(",", "[", "]"));
        }
    }

    @Override
    public void accept(String game) {
        Matcher m = FMT.matcher(game);
        if (m.matches()) {
            int weight = 1 + gains.shift();
            int id = Integer.parseInt(m.group(1));
            winning = toInts(m.group(2)).toArray();
            long wins = toInts(m.group(3)).filter(this::isWin).count();
            gains.inc(weight, (int) wins);
            DEBUG.trace("card #%d: %d, %d wins → %s", id, weight, wins, gains);
            sum += weight;
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
