package advent.y2022;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.IntSummaryStatistics;
import java.util.function.Consumer;

public class Ex1 implements Consumer<String> {
    private final int[] max = {-1, -1, -1};
    private int current = -1;

    @Override
    public void accept(String cal) {
        if ("".equals(cal)) {
            current = -1;
            return;
        }

        int v = Integer.parseInt(cal, 10);
        int last = current;
        if (last == -1) {
            current = v;
        } else {
            current += v;
        }
        for (int i = 0; i < max.length;  ++i) {
            if (last == max[i]) {
                max[i] = current;
                return;
            } else if (current > max[i]) {
                int remaining = max.length - i - 1;
                if (remaining > 0) System.arraycopy(max, i, max, i + 1, remaining);
                max[i] = current;
                return;
            }
        }
    }
    IntSummaryStatistics stats() {
        return Arrays.stream(max).summaryStatistics();
    }

    public static void main(String[] args) throws IOException, URISyntaxException {
        Ex1 consumer = new Ex1();
        URI input = consumer.getClass().getResource("ex1.input.txt").toURI();
        Files.readAllLines(Path.of(input)).forEach(consumer);
        System.out.println(consumer.stats());
    }
}
