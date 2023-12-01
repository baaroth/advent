package advent.y2023;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.IntSummaryStatistics;
import java.util.function.Consumer;

public class Ex1 implements Consumer<String> {
    private int sum = 0;

    @Override
    public void accept(String cal) {
        int[] digits = { -1, 0 };
        cal.chars().forEach(c -> {
            if (c >= '0' && c <= '9') {
                digits[1] = Character.digit(c, 10);
                if (digits[0] < 0) {
                    digits[0] = digits[1];
                }
            }
        });

        sum += digits[0] * 10 + digits[1];
    }

    public static void main(String[] args) throws IOException, URISyntaxException {
        Ex1 consumer = new Ex1();
        URI input = consumer.getClass().getResource("ex1.input.txt").toURI();
        Files.readAllLines(Path.of(input)).forEach(consumer);
        System.out.println(consumer.sum);
    }
}
