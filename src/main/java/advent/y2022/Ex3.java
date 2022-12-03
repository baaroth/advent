package advent.y2022;

import advent.Debug;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.function.Consumer;


public class Ex3  implements Consumer<String> {

    private final static Debug DEBUG = Debug.ON;

    private int priority = 0;
    private int[] potentialBadges;
    private int rank = 0;

    @Override
    public void accept(String content) {
        compute2(content);
    }

    private static int compute1(String content) {
        int mid = content.length() >> 1;
        for (int i = 0; i < mid; ++i) {
            char item = content.charAt(i);
            if (content.indexOf(item, mid) > 0) {
                final int prio = prioritize(item);
                DEBUG.trace("found %s (%d)%n", item, prio);
                return prio;
            }
        }
        return 0;
    }

    private void compute2(String content) {
        ++rank;
        DEBUG.trace("%d. ", rank);
        if (rank % 3 == 1) {
            potentialBadges = content.chars().distinct().toArray();
            DEBUG.trace("%d uniques%n", potentialBadges.length);
            return;
        }

        potentialBadges = Arrays.stream(potentialBadges)
                .filter(pb -> content.chars().anyMatch(item -> pb == item))
                .toArray();
        DEBUG.trace("%d remaining%n", potentialBadges.length);

        if (rank % 3 == 0) {
            if (potentialBadges.length != 1) {
                throw new IllegalStateException("bad group (" + potentialBadges.length + " dups)");
            }
            priority += prioritize(potentialBadges[0]);
        }
    }

    private static int prioritize(int item) {
        return (item < 'a')
                ? item - 'A' + 27
                : item - 'a' + 1;
    }

    public static void main(String[] args) throws IOException, URISyntaxException {
        Ex3 consumer = new Ex3();
        URI input = consumer.getClass().getResource("ex3.input.txt").toURI();
        Files.readAllLines(Path.of(input)).forEach(consumer);
        System.out.println("R=" + consumer.priority);
    }

}
