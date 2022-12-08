package advent.y2022;

import advent.Debug;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class Ex8 {
    private static final Debug DEBUG = Debug.ON;

    public static void main(String[] args) throws IOException, URISyntaxException {
        URI input = Ex8.class.getResource("ex8.input.txt").toURI();
        Grid g = new Grid();
        Files.readAllLines(Path.of(input)).forEach(g::load);
        System.out.println(g.scenic());
    }


    private static class Grid {

        private int len = 0;
        private final List<Tree[]> lines = new ArrayList<>();

        void load(String encoded) {
            int y = lines.size();
            if (y == 0) len = encoded.length();
            Tree[] line = new Tree[len];
            for (int x = 0; x < len; ++x) {
                line[x] = new Tree(x, y, encoded.charAt(x) - '0');
            }
            lines.add(line);
        }

        int countVisible() {
            Set<Tree> visible = new HashSet<>();
            for (Tree[] line : lines) {
                visible.addAll(left(line));
                visible.addAll(right(line));
            }
            for (int x = 0; x < len; ++x) {
                visible.addAll(top(x));
                visible.addAll(bottom(x));
            }
            return visible.size();
        }
        long scenic() {
            long max = 0;
            for (Tree[] line : lines) {
                for (Tree t : line) {
                    int u = lookUp(t);
                    int r = lookRight(t);
                    int d = lookDown(t);
                    int l = lookLeft(t);
                    long score = u * r * d * l;
                    if (max < score) {
                        DEBUG.trace("%s: %d,%d,%d,%d%n", t, u, r, d, l);
                        max = score;
                    }
                }
            }
            return max;
        }

        private int lookDown(Tree t) {
            int score = 0;
            for (int y = t.y() + 1; y < lines.size(); ++y) {
                ++score;
                if (lines.get(y)[t.x()].compareTo(t) >= 0) break;
            }
            return score;
        }
        private int lookUp(Tree t) {
            int score = 0;
            for (int y = t.y() - 1; y >= 0; --y) {
                ++score;
                if (lines.get(y)[t.x()].compareTo(t) >= 0) break;
            }
            return score;
        }
        private int lookLeft(Tree t) {
            int score = 0;
            Tree[] line = lines.get(t.y());
            for (int x = t.x() - 1; x >= 0; --x) {
                ++score;
                if (line[x].compareTo(t) >= 0) break;
            }
            return score;
        }
        private int lookRight(Tree t) {
            int score = 0;
            Tree[] line = lines.get(t.y());
            for (int x = t.x() + 1; x < len; ++x) {
                ++score;
                if (line[x].compareTo(t) >= 0) break;
            }
            return score;
        }
        private List<Tree> left(Tree[] line) {
            List<Tree> visible = new ArrayList<>();
            Tree max = line[0];
            visible.add(max);
            for (int x = 1; x < len; ++x) {
                if (line[x].compareTo(max) > 0) {
                    max = line[x];
                    visible.add(max);
                }
            }
            DEBUG.trace("left : %s%n", visible);
            return visible;
        }
        private List<Tree> top(int x) {
            List<Tree> visible = new ArrayList<>();
            Iterator<Tree[]> ite = lines.iterator();
            Tree max = ite.next()[x];
            visible.add(max);
            while (ite.hasNext()) {
                Tree t = ite.next()[x];
                if (t.compareTo(max) > 0) {
                    max = t;
                    visible.add(max);
                }
            }
            DEBUG.trace("top(%d): %s%n", x, visible);
            return visible;
        }
        private List<Tree> right(Tree[] line) {
            List<Tree> visible = new ArrayList<>();
            Tree max = line[len - 1];
            visible.add(max);
            for (int x = len - 2; x >= 0; --x) {
                if (line[x].compareTo(max) > 0) {
                    max = line[x];
                    visible.add(max);
                }
            }
            DEBUG.trace("right: %s%n", visible);
            return visible;
        }
        private List<Tree> bottom(int x) {
            List<Tree> visible = new ArrayList<>();
            ListIterator<Tree[]> ite = lines.listIterator(lines.size());
            Tree max = ite.previous()[x];
            visible.add(max);
            while (ite.hasPrevious()) {
                Tree t = ite.previous()[x];
                if (t.compareTo(max) > 0) {
                    max = t;
                    visible.add(max);
                }
            }
            DEBUG.trace("bot(%d): %s%n", x, visible);
            return visible;
        }
    }

    private record Tree(int x, int y, int height) implements Comparable<Tree> {
        @Override
        public int compareTo(Tree other) {
            return Integer.compare(height, other.height);
        }

        @Override
        public String toString() {
            return String.format("%d(%d:%d)", height, x, y);
        }
    }
}
