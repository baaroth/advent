package advent.y2022;

import advent.Debug;
import advent.RecursionMonitor;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import static java.nio.charset.Charset.defaultCharset;

public class Recurse13 {
    private static final Debug DEBUG = Debug.OFF;
    private static final RecursionMonitor RECURSE = new RecursionMonitor();

    public static void main(String[] args) throws IOException, URISyntaxException {
        URI input = Ex13.class.getResource("ex13.input.txt").toURI();

        List<Packet> packets = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(Path.of(input), defaultCharset())) {
            String last = "";
            while (last != null) {
                packets.add(new Packet(reader.readLine()));
                packets.add(new Packet(reader.readLine()));
                last = reader.readLine(); // line-feed
            }
        }
        Packet[] sorted = packets.stream().sorted().toArray(Packet[]::new);
        int divideBegin = -Arrays.binarySearch(sorted, new Packet("[[2]]"));
        // add 1 to simulate other divider packet in the list
        int divideEnd = 1 - Arrays.binarySearch(sorted, new Packet("[[6]]"));
        System.out.printf("%d×%d=%d in %d max recursions", divideBegin, divideEnd, divideBegin * divideEnd, RECURSE.max());
    }

    private record Packet(String raw) implements Comparable<Packet> {

        @Override
        public int compareTo(Packet other) {
            DEBUG.trace("compare %s%n   with %s", this, other);
            Region a = new Region.Array(raw);
            Region b = new Region.Array(other.raw);
            return a.compareTo(b);
        }

        @Override
        public String toString() {
            return raw;
        }
    }

    private sealed interface Region extends Comparable<Region> {
        int end();

        final class Array implements Region, Iterator<Region> {
            private final String orig;
            private final int start, end;
            private int cursor;

            public Array(String orig) {
                this.orig = orig;
                this.start = 1;
                cursor = 1;
                end = findEnd(orig, start);
            }
            Array(Num single) {
                orig = "{%d]".formatted(single.val); // open as curly brace to recognize pretended array is logs
                start = 1;
                cursor = 1;
                end = orig.length() - 1;
            }
            private Array(Array parent) {
                this.orig = parent.orig;
                this.start = parent.cursor + 1;
                cursor = start;
                end = findEnd(orig, this.start);
            }

            @Override
            public int compareTo(Region other) {
                RECURSE.in();
                Array otherArr = asArray(other);
                while (hasNext() && otherArr.hasNext()) {
                    int cmp = next().compareTo(otherArr.next());
                    if (cmp != 0) {
                        return RECURSE.out(cmp);
                    }
                }
                if (hasNext()) {
                    DEBUG.trace("  >");
                    return RECURSE.out(1); // they exhausted before us
                } else if (otherArr.hasNext()) {
                    DEBUG.trace("  <");
                    return RECURSE.out(-1); // we exhausted first
                } else {
                    DEBUG.trace("  =");
                    return RECURSE.out(0); // exhausted together
                }
            }

            @Override
            public int end() { return end; }

            @Override
            public boolean hasNext() { return cursor < end; }

            @Override
            public Region next() {
                if (!hasNext()) throw new NoSuchElementException("exhausted region");
                Region nextRegion = nested() ? new Array(this) : new Num(this, cursor);
                cursor = nextRegion.end() + 1;
                if (cursor < end && orig.charAt(cursor) == ',') ++cursor;
                return nextRegion;
            }

            @Override
            public String toString() {
                StringBuilder s = new StringBuilder();
                s.append(orig, start - 1, cursor);
                s.append('↓');
                s.append(orig, cursor, end + 1);
                return s.toString();
            }

            private static Array asArray(Region other) {
                if (other instanceof Array arr) return arr;
                else if (other instanceof Num num) return new Array(num);
                throw new UnsupportedOperationException("not arrayable " + other.getClass());
            }

            private static int findEnd(String s, int start) {
                int depth = 0;
                for (int i = start; i < s.length(); ++i) {
                    if (s.charAt(i) == ']') {
                        if (depth == 0) return i;
                        else --depth;
                    }
                    if (s.charAt(i) == '[') ++depth;
                }
                throw new IllegalArgumentException("unfinished region");
            }

            private boolean nested() {
                return orig.charAt(cursor) == '[';
            }
        }

        final class Num implements Region {
            private final int val, end;

            public Num(Array parent, int start) {
                int comma = parent.orig.indexOf(',', start);
                this.end = comma < 0 || comma > parent.end ? parent.end : comma;
                val = Integer.parseInt(parent.orig.substring(start, end));
            }

            @Override
            public int compareTo(Region other) {
                RECURSE.in();
                if (other instanceof Num num) {
                    return RECURSE.out(Integer.compare(val, num.val));
                }
                if (other instanceof Array arr) {
                    return RECURSE.out(new Array(this).compareTo(arr));
                }
                throw RECURSE.out(new UnsupportedOperationException("comparing " + other.getClass()));
            }

            @Override
            public int end() { return end; }

            @Override
            public String toString() {
                return String.valueOf(val);
            }
        }
    }
}
