package advent.y2022;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static java.nio.charset.Charset.defaultCharset;
import static java.util.stream.Collectors.joining;

public class Ex5 {

    public static void main(String[] args) throws IOException, URISyntaxException {
        URI input = Ex5.class.getResource("ex5.input.txt").toURI();
        List<CratesLine> cratesLines = new ArrayList<>();
        List<Move> moves = new ArrayList<>();

        try (BufferedReader reader = Files.newBufferedReader(Path.of(input), defaultCharset())) {
            String line = reader.readLine();
            while (CratesLine.FORMAT.matcher(line).matches()) {
                cratesLines.add(new CratesLine(line));
                line = reader.readLine();
            }
            // exit when read stacks numbering line
            reader.readLine(); // empty line
            line = reader.readLine(); // 1st move
            while (line != null) {
                moves.add(Move.of(line));
                line = reader.readLine();
            }
        }

        final Stack[] stacks = toStacks(cratesLines.toArray(CratesLine[]::new));
        for (Move m : moves) {
            stacks[m.from().index()].move9001(m.qty(), stacks[m.to().index()]);
        }
        String tops = Arrays.stream(stacks)
                .flatMap(Stack::streamTop)
                .collect(joining());
        System.out.println(tops);
    }

    private static Stack[] toStacks(CratesLine[] lines) {
        int nbStacks = lines[0].crates.length;
        final Stack[] stacks = Stack.multiple(lines.length, nbStacks);
        for (int i = lines.length - 1; i >= 0; --i) {
            CratesLine line = lines[i];
            for (int j = 0; j < nbStacks; j++) {
                char crate = line.at(j);
                if (crate != 0) stacks[j].push(crate);
            }
        }
        return stacks;
    }

    private static class CratesLine {
        static final Pattern FORMAT = Pattern.compile("^ *\\[[A-Z]].*$");

        private final char[] crates;
        CratesLine(String encoded) {
            if ((encoded.length() + 1) %4 != 0) throw new IllegalArgumentException("bad input " + encoded);
            crates = new char[(encoded.length() + 1) >>2];
            for (int i = 0; i < encoded.length(); i += 4) {
                if (encoded.charAt(i) == '[') {
                    crates[i >>2] = encoded.charAt(i+1);
                }
            }
        }

        public char at(int index) {
            return crates[index];
        }

        @Override
        public String toString() {
            StringBuilder s = new StringBuilder();
            for (char c : crates) {
                s.append(c == 0 ? ' ' : c);
            }
            return s.toString();
        }
    }

    private record Move(int qty, StackRank from, StackRank to) {
        private static final Pattern FORMAT = Pattern.compile("^move (\\d+) from (\\d) to (\\d)$");
        static Move of(String encoded) {
            Matcher m = FORMAT.matcher(encoded);
            if (!m.matches()) throw new IllegalArgumentException("bad input (" + encoded + ")");
            return new Move(Integer.parseInt(m.group(1)), StackRank.of(m.group(2)), StackRank.of(m.group(3)));
        }
    }

    private record StackRank(int raw) {

        private static final StackRank[] CACHE = new StackRank[10];
        static {
            for (int i = 0; i < CACHE.length; ++i) {
                CACHE[i] = new StackRank(i);
            }
        }

        static StackRank of(int raw) {
            if (raw < 0) throw new IllegalArgumentException("negative rank");
            return raw < CACHE.length ? CACHE[raw] : new StackRank(raw);
        }
        static StackRank of(String raw) {
            return of(Integer.parseInt(raw, 10));
        }

        public int index() {
            return raw - 1;
        }
    }

    private static class Stack {
        private final StackRank rank;
        private final char[] crates;
        private int size;

        private Stack(StackRank rank, int cratesLines, int crateLineLen) {
            this.rank = rank;
            crates = new char[cratesLines * crateLineLen]; // at most: all crates in 1 stack
            size = 0;
        }
        public static Stack[] multiple(int cratesLines, int crateLineLen) {
            Stack[] stacks = new Stack[crateLineLen];
            for (int i = 0; i < crateLineLen; ++i) {
                stacks[i] = new Stack(StackRank.of(i + 1), cratesLines, crateLineLen);
            }
            return stacks;
        }

        public void move(int qty, Stack dest) {
            if (qty > size) throw new IllegalArgumentException(String.format("move too many (at most %d, asked %d)", size, qty));
            for (int i = 0; i < qty; ++i) {
                dest.push(crates[--size]);
            }
        }

        public void move9001(int qty, Stack dest) {
            if (qty > size) throw new IllegalArgumentException(String.format("move too many (at most %d, asked %d)", size, qty));
            System.arraycopy(crates, size - qty, dest.crates, dest.size, qty);
            dest.size += qty;
            this.size -= qty;
        }

        public void push(char crate) {
            crates[size++] = crate;
        }
        public Stream<String> streamTop() {
            return size == 0 ? Stream.empty() : Stream.of(String.valueOf(crates[size - 1]));
        }

        @Override
        public String toString() {
            StringBuilder s = new StringBuilder().append(rank.raw()).append(':');
            for (int i = 0; i < size; ++i) {
                s.append(crates[i]);
            }
            return s.toString();
        }
    }
}
