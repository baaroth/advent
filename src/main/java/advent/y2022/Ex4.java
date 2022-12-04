package advent.y2022;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Ex4 implements Consumer<String> {

    private final static Pattern REGEX_SECTIONS = Pattern.compile("^(\\d+)-(\\d+),(\\d+)-(\\d+)$");

    private int result = 0;

    @Override
    public void accept(String pair) {
        Matcher m = REGEX_SECTIONS.matcher(pair);
        if (!m.matches()) throw new IllegalArgumentException("bad pair " + pair);
        Sections a = new Sections(m.group(1), m.group(2));
        Sections b = new Sections(m.group(3), m.group(4));
        //result += duplicates(a, b);
        if (a.overlaps(b)) ++result;
    }

    private static int duplicates(Sections a, Sections b) {
        return a.contains(b) || b.contains(a) ? 1 : 0;
    }

    public static void main(String[] args) throws IOException, URISyntaxException {
        Ex4 consumer = new Ex4();
        URI input = consumer.getClass().getResource("ex4.input.txt").toURI();
        Files.readAllLines(Path.of(input)).forEach(consumer);
        System.out.println("R=" + consumer.result);
    }

    private record Sections(int begin, int end) {
        Sections(String begin, String end) {
            this(Integer.parseInt(begin, 10), Integer.parseInt(end, 10));
        }

        boolean contains(Sections other) {
            return begin <= other.begin && end >= other.end;
        }

        public boolean overlaps(Sections other) {
            return begin <= other.begin
                    ? end >= other.begin
                    : other.end >= begin;
        }
    }
}
