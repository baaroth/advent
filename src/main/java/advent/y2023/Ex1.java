package advent.y2023;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;

public class Ex1 implements Consumer<String> {
    private int sum = 0;

    private static final List<Digit> VALS = List.of(
            new Digit("1", 1),
            new Digit("one", 1),
            new Digit("2", 2),
            new Digit("two", 2),
            new Digit("3", 3),
            new Digit("three", 3),
            new Digit("4", 4),
            new Digit("four", 4),
            new Digit("5", 5),
            new Digit("five", 5),
            new Digit("6", 6),
            new Digit("six", 6),
            new Digit("7", 7),
            new Digit("seven", 7),
            new Digit("8", 8),
            new Digit("eight", 8),
            new Digit("9", 9),
            new Digit("nine", 9)
    );

    record Digit(String raw, int val) {
        int len() {
            return raw.length();
        }
    }

    @Override
    public void accept(String cal) {
        int[] digits = { -1, 0 };
        for (int i = 0; i < cal.length(); ++i) {
            final int from = i;
            VALS.stream()
                    .filter(v -> cal.startsWith(v.raw, from))
                    .findFirst()
                    .ifPresent(v -> {
                        digits[1] = v.val;
                        if (digits[0] < 0) {
                            digits[0] = digits[1];
                        }
                    });
        }

        sum += digits[0] * 10 + digits[1];
    }

    public static void main(String[] args) throws IOException, URISyntaxException {
        Ex1 consumer = new Ex1();
        URI input = consumer.getClass().getResource("ex1.input.txt").toURI();
        Files.readAllLines(Path.of(input)).forEach(consumer);
        System.out.println(consumer.sum);
    }
}
