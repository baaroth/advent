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
    private static final Debug DEBUG = Debug.OFF;

    private static final IntBinaryOperator NOOP = (c, x) -> x;
    /**
     * alias
     * @see #parse(String)
     */
    private static final IntBinaryOperator PAD = NOOP;

    public static void main(String[] args) throws IOException, URISyntaxException {
        URI input = Ex10.class.getResource("ex10.input.txt").toURI();
        final Screen screen = new Screen();
        Files.readAllLines(Path.of(input)).stream()
                .flatMap(Ex10::parse)
                .forEach(screen::tick);
        System.out.println(screen);
    }

    private static Stream<IntBinaryOperator> parse(String instruction) {
        if (instruction.equals("noop")) return Stream.of(NOOP);
        Matcher m = AddX.FORMAT.matcher(instruction);
        if (m.matches()) return Stream.of(PAD, AddX.of(m));
        throw new IllegalArgumentException("bad instruction " + instruction);
    }

    private static class Cpu {
        private int cycle = 0;
        private int x = 1;

        private int clock() {
            return cycle;
        }
        public int tick(IntBinaryOperator instruction) {
            ++cycle;
            x = instruction.applyAsInt(cycle, x);
            return x;
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

    private static class Screen {
        private static final int WIDTH = 40;
        private final Cpu cpu = new Cpu();
        private final char[] pixels = new char[6 * WIDTH];
        private int spriteMiddle;
        Screen() {
            spriteMiddle = cpu.x;
        }

        public void tick(IntBinaryOperator instruction) {
            int currentlyDrawing = cpu.clock();
            pixels[currentlyDrawing] = spriteMatch(currentlyDrawing) ? '#' : '.';
            spriteMiddle = cpu.tick(instruction);
            DEBUG.trace("cycle %d: %s%n", cpu.clock(), this);
        }

        private boolean spriteMatch(int currentlyDrawing) {
            int linePos = currentlyDrawing % WIDTH;
            return Math.abs(linePos - spriteMiddle) <= 1;
        }

        @Override
        public String toString() {
            StringBuilder s = new StringBuilder();
            for (int i = 0; i < pixels.length; ++i) {
                if (i % WIDTH == 0) s.append('\n');
                char pixel;
                if (i == spriteMiddle - 1) {
                    pixel = switch (pixels[i]) {
                        case '.' -> '¿';
                        case '#' -> '«';
                        default -> '<';
                    };
                } else if (i == spriteMiddle + 1) {
                    pixel = switch (pixels[i]) {
                        case '.' -> '?';
                        case '#' -> '»';
                        default -> '>';
                    };
                } else pixel = pixels[i];
                s.append(pixel);
            }
            return s.toString();
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
