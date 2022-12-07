package advent.y2022;

import advent.Debug;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class Ex7 implements Consumer<String> {
    private static final Debug DEBUG = Debug.ON;
    public static final long DISK_SIZE = 70_000_000L;
    private Dir current = Dir.ROOT;

    @Override
    public void accept(String input) {
        if (input.equals("$ cd ..")) {
            current = current.parent;
            return;
        }
        if (input.equals("$ cd /")) {
            current = Dir.ROOT;
            DEBUG.trace("←%n");
            return;
        }
        if (input.startsWith("$ cd ")) {
            current = current.putDir(input.substring(5));
            return;
        }
        Matcher m = File.FORMAT.matcher(input);
        if (m.matches()) {
            current.putFile(new File(m));
        }
    }

    public static void main(String[] args) throws IOException, URISyntaxException {
        Ex7 ex = new Ex7();
        URI input = ex.getClass().getResource("ex7.input.txt").toURI();
        Files.readAllLines(Path.of(input)).forEach(ex);
        long left = DISK_SIZE - Dir.ROOT.totalSize;
        long minDelete = 30_000_000L - left;
        System.out.println(Dir.ROOT.stream(minDelete).mapToLong(Dir::size).min());
    }

    private static long lt100k(Dir d) {
        long sum = 0;
        if (d.totalSize <= 100_000L) {
            DEBUG.trace("accept %s%n", d);
            sum += d.totalSize;
        }
        if (!d.subdirs.isEmpty()) {
            for (Dir child : d.subdirs) {
                sum += lt100k(child);
            }
        }
        return sum;
    }

    private record File(String name, long size) {
        static final Pattern FORMAT = Pattern.compile("^(\\d+) (.+)$");
        File(Matcher formatted) {
            this(formatted.group(2), Long.parseLong(formatted.group(1), 10));
        }

        @Override
        public String toString() {
            return String.format("%s : %d", name, size);
        }
    }

    private static class Dir {
        static final Dir ROOT = new Dir("/", null);

        private final String name; 
        private long totalSize;

        private final List<Dir> subdirs;
        private final Dir parent;

        private Dir(String name, Dir parent) {
            this.name = name;
            this.subdirs = new ArrayList<>();
            this.parent = parent;
        }

        public void putFile(File f) {
            totalSize += f.size();
            Dir p = parent;
            while (p != null) {
                p.totalSize += f.size();
                p = p.parent;
            }
        }
        public Dir putDir(String name) {
            Dir child = new Dir(name, this);
            subdirs.add(child);
            return child;
        }

        public long size() {
            return totalSize;
        }

        public Stream<Dir> stream(long atLeast) {
            if (totalSize < atLeast) {
                return Stream.empty();
            } else {
                DEBUG.trace("%s matches%n", this);
                if (subdirs.isEmpty()) {
                    return Stream.of(this);
                } else {
                    return Stream.concat(Stream.of(this), subdirs.stream().flatMap(child -> child.stream(atLeast)));
                }
            }
        }

        @Override
        public String toString() {
            return String.format("%s : %d", name, totalSize);
        }
    }
}
