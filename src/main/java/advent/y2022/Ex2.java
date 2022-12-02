package advent.y2022;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.function.Consumer;

public class Ex2 implements Consumer<String> {
    private enum Shape {
        ROCK(1),
        PAPER(2),
        SCISSORS(3);

        final int score;

        Shape(int score) {
            this.score = score;
        }
    }
    private static final Map<Character, Shape> OTHER_CODES = Map.of(
            'A', Shape.ROCK,
            'B', Shape.PAPER,
            'C', Shape.SCISSORS
    );
    private static final Map<Character, Shape> ME_CODES = Map.of(
            'X', Shape.ROCK,
            'Y', Shape.PAPER,
            'Z', Shape.SCISSORS
    );


    private enum Outcome {
        WIN(6),
        DRAW(3),
        LOSS(0);

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
        static Outcome evalMe(Shape other, Shape me) {
            if (other == me) return DRAW;
            return WINNING.get(me) == other ? WIN : LOSS;
        }
    }
    private static final Map<Character, Outcome> OUTCOME_CODES = Map.of(
            'X', Outcome.LOSS,
            'Y', Outcome.DRAW,
            'Z', Outcome.WIN
    );
    private int score = 0;

    @Override
    public void accept(String draw) {
        score += compute2(draw);
    }

    private int compute1(String draw) {
        Shape other = OTHER_CODES.get(draw.charAt(0));
        Shape me = ME_CODES.get(draw.charAt(2));
        Outcome outcome = Outcome.evalMe(other, me);
        return me.score + outcome.score;
    }

    private int compute2(String draw) {
        Shape other = OTHER_CODES.get(draw.charAt(0));
        Outcome outcome = OUTCOME_CODES.get(draw.charAt(2));
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

}
