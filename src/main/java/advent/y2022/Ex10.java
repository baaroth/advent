package advent.y2022;

import advent.Debug;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.IntBinaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.LongStream;
import java.util.stream.Stream;

public class Ex10 {
    private static final Debug DEBUG = Debug.ON;

    private static final IntBinaryOperator NOOP = (c, x) -> x;
    /**
     * alias
     * @see #parse(String)
     */
    private static final IntBinaryOperator PAD = NOOP;

    public static void main(String[] args) throws IOException, URISyntaxException {
        URI input = Ex9.class.getResource("ex10.input.txt").toURI();
        final Cpu cpu = new Cpu();
        long totalScore = Files.readAllLines(Path.of(input)).stream()
                .flatMap(Ex10::parse)
                .flatMapToLong(cpu::tick)
                .limit(CycleOfInterest.LAST)
                .reduce(0, Long::sum);
        System.out.println(totalScore);
    }

    private static Stream<IntBinaryOperator> parse(String instruction) {
        if (instruction.equals("noop")) return Stream.of(NOOP);
        Matcher m = AddX.FORMAT.matcher(instruction);
        if (m.matches()) return Stream.of(PAD, AddX.of(m));
        throw new IllegalArgumentException("bad instruction " + instruction);
    }

    private static class Cpu {
        private final CycleOfInterest reporter = new CycleOfInterest();
        private int cycle = 0;
        private int x = 1;

        public LongStream tick(IntBinaryOperator instruction) {
            ++cycle;
            x = instruction.applyAsInt(cycle, x);
            return reporter.strength(cycle, x);
        }
    }

    private static class CycleOfInterest {
        private static final int[] POINTS = { 20, 60, 100, 140, 180, 220 };
        public static final int LAST = POINTS[POINTS.length - 1];

        private int next = 0;

        LongStream strength(int cycle, int x) {
            if (next < POINTS.length && cycle == POINTS[next]) {
                long score = cycle * (long) x;
                DEBUG.trace("cycle %d: %d%n", cycle, score);
                ++next;
                return LongStream.of(score);
            }
            return LongStream.empty();
        }
    }

    private record AddX(int val) implements IntBinaryOperator {
        static final Pattern FORMAT = Pattern.compile("^addx (-?\\d+)$");

        public static AddX of(Matcher m) {
            return new AddX(Integer.parseInt(m.group(1), 10));
        }

        @Override
        public int applyAsInt(int c, int x) {
            DEBUG.trace("cycle %d: %d + %d%n", c, x, val);
            return x + val;
        }
    }
}
