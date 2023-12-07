package advent.y2023;

import advent.BigInt;
import advent.Debug;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static java.nio.charset.Charset.defaultCharset;
import static java.util.Comparator.comparing;
import static java.util.Comparator.naturalOrder;

public class Ex5 {
    private static final Debug DEBUG = Debug.OFF;
    private Function<BigInt, BigInt> indirect = UnaryOperator.identity();

    private static class Seeds {
        private static final Pattern FMT = Pattern.compile("^seeds: ([0-9 ]+)$");
        private final List<BigInt> inner;
        private Seeds(List<BigInt> inner) {
            this.inner = inner;
        }

        static Seeds of(String raw) {
            Matcher m = FMT.matcher(raw);
            if (!m.matches()) throw new IllegalArgumentException("bad seeds: " + raw);
            String[] parts = m.group(1).trim().split(" ");
            return new Seeds(Arrays.stream(parts).map(BigInt::new).toList());
        }

        Stream<BigInt> stream() {
            return inner.stream();
        }
    }

    private record Indirect(BigInt src, BigInt dst, int len, BigInt fence) {
        private static final Pattern FMT = Pattern.compile("^([0-9]+) ([0-9]+) ([0-9]+)$");
        static Indirect of(String raw) {
            Matcher m = FMT.matcher(raw);
            if (!m.matches()) throw new IllegalArgumentException("bad definition: " + raw);
            BigInt src = new BigInt(m.group(2));
            int len = Integer.parseInt(m.group(3));
            return new Indirect(
                    src,
                    new BigInt(m.group(1)),
                    len,
                    src.plus(len)
            );
        }

        @Override
        public String toString() {
            return "[%s,%s[ → %s".formatted(src, fence, dst);
        }
    }

    private static class Indirects implements UnaryOperator<BigInt> {
        private final List<Indirect> inner = new ArrayList<>();
        private final String name;
        Indirects(String name) {
            this.name = name;
        }

        void add(String raw) {
            inner.add(Indirect.of(raw));
        }

        Indirects sort() {
            inner.sort(comparing(Indirect::src));
            return this;
        }

        public BigInt apply(BigInt in) {
            DEBUG.trace("%s %s in %s", name, in, inner);
            for (Indirect i : inner) {
                if (in.lowerTo(i.src())) {
                    break; // `in` isn't overloaded
                } else if (in.equals(i.src())) {
                    return i.dst(); // trivial overload
                } else if (in.lowerTo(i.fence())) {
                    // `i.src < in < i.fence` → compute overload
                    BigInt delta = in.minus(i.src());
                    return i.dst().plus(delta);
                }
            }
            return in;
        }
    }

    public static void main(String[] args) throws IOException, URISyntaxException {
        URI input = Ex5.class.getResource("ex5.input.txt").toURI();
        Ex5 ex = new Ex5();
        Seeds seeds;

        try (BufferedReader reader = Files.newBufferedReader(Path.of(input), defaultCharset())) {
            String line = reader.readLine();
            seeds = Seeds.of(line);
            reader.readLine(); // empty line
            line = reader.readLine(); // 1st section title
            while (line != null) {
                Indirects indirects = new Indirects(line);
                line = reader.readLine(); // 1st overload definition
                while (line != null && !line.isEmpty()) {
                    indirects.add(line);
                    line = reader.readLine();
                }
                // empty line consumed (made us exit loop)
                ex.indirect = ex.indirect.andThen(indirects.sort());
                if (line != null) {
                    line = reader.readLine(); // next section title (or null if EOF)
                }
            }
        }

        BigInt min = seeds.stream().map(ex.indirect).min(naturalOrder())
                .orElse(BigInt.ZERO);
        System.out.println(min);
    }
}
