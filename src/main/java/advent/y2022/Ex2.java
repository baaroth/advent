package advent.y2022;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.function.Consumer;

public class Ex2 implements Consumer<String> {

    private int score = 0;

    @Override
    public void accept(String draw) {
        score += compute2(draw);
    }

    private static final Map<Character, Shape> ME_CODES = Map.of(
            'X', Shape.ROCK,
            'Y', Shape.PAPER,
            'Z', Shape.SCISSORS
    );
    private static int compute1(String draw) {
        Shape other = Shape.BY_CODE.get(draw.charAt(0));
        Shape me = ME_CODES.get(draw.charAt(2));
        Outcome outcome = Outcome.evalFirst(me, other);
        return me.score + outcome.score;
    }

    private int compute2(String draw) {
        Shape other = Shape.BY_CODE.get(draw.charAt(0));
        Outcome outcome = Outcome.BY_CODE.get(draw.charAt(2));
        Shape me = switch (outcome) {
            case DRAW -> other;
            case WIN -> Outcome.LOOSING.get(other);
            case LOSS -> Outcome.WINNING.get(other);
        };
        return me.score + outcome.score;
    }

    public static void main(String[] args) throws IOException, URISyntaxException {
        Ex2 consumer = new Ex2();
        URI input = consumer.getClass().getResource("ex2.input.txt").toURI();
        Files.readAllLines(Path.of(input)).forEach(consumer);
        System.out.println(consumer.score);
    }

    private enum Outcome {
        WIN(6),
        DRAW(3),
        LOSS(0);

        private static final Map<Character, Outcome> BY_CODE = Map.of(
                'X', LOSS,
                'Y', DRAW,
                'Z', WIN
        );

        final int score;
        Outcome(int score) {
            this.score = score;
        }

        private static final Map<Shape, Shape> LOOSING = Map.of(
                Shape.ROCK, Shape.PAPER,
                Shape.PAPER, Shape.SCISSORS,
                Shape.SCISSORS, Shape.ROCK
        );
        private static final Map<Shape, Shape> WINNING = Map.of(
                Shape.PAPER, Shape.ROCK,
                Shape.SCISSORS, Shape.PAPER,
                Shape.ROCK, Shape.SCISSORS
        );
        static Outcome evalFirst(Shape a, Shape b) {
            if (a == b) return DRAW;
            return WINNING.get(a) == b ? WIN : LOSS;
        }
    }

    private enum Shape {
        ROCK(1),
        PAPER(2),
        SCISSORS(3);

        static final Map<Character, Shape> BY_CODE = Map.of(
                'A', ROCK,
                'B', PAPER,
                'C', SCISSORS
        );

        final int score;
        Shape(int score) {
            this.score = score;
        }
    }

}
