package advent.y2022;

import advent.BigInt;
import advent.Coor;
import advent.Debug;
import advent.Walk;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.function.UnaryOperator;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.nio.charset.Charset.defaultCharset;

public class Ex17 {
    private static final BigInt BLIP_PERIOD = new BigInt(10_000_000);
    private static final BigInt BLIP_LN_PERIOD = BLIP_PERIOD.times(100);
    private static final BigInt CUT_PERIOD = new BigInt(1_000);
    private static final Debug DEBUG = Debug.OFF;
    private static final Debug INFO = Debug.ON;

    public static void main(String[] args) throws IOException, URISyntaxException {
        URI input = Ex17.class.getResource("ex17.input.txt").toURI();

        String directions;
        try (BufferedReader reader = Files.newBufferedReader(Path.of(input), defaultCharset())) {
            directions = reader.readLine();
        }

        Vent v = new Vent(directions);
        Area a = new Area();
        Generator g = new Generator(a, new BigInt("1000000000000"));
        Shape s = g.next();
        while (g.hasNext()) {
            v.blow(s, a);
            if (s.canDown(a)) s.push(Coor::down);
            else {
                a.add(s);
                if (g.at(CUT_PERIOD)) a.tryCut();
                if (g.at(BLIP_PERIOD)) {
                    if (g.at(BLIP_LN_PERIOD)) INFO.trace("!");
                    else INFO.lifePulse();
                }
                s = g.next();
            }
        }
        System.out.printf("high=%s%n%s", a.highest, a.print(20));
    }

    private static class Area {
        private static final String BOTTOM = "-------";
        private static final String EMPTY  = ".......";

        private BigInt highest = BigInt.ZERO;
        private List<char[]> lines = new ArrayList<>();
        private BigInt floor;
        public Area() {
            lines.add(BOTTOM.toCharArray());
            floor = BigInt.ZERO;
        }
        private Area(Area org, int depth) {
            int first = Math.max(org.lines.size() - depth, 0);
            this.floor = org.floor.plus(new BigInt(first));
            Iterator<char[]> ite = org.lines.listIterator(first);
            while (ite.hasNext()) {
                char[] orgLine = ite.next();
                char[] copy = new char[orgLine.length];
                System.arraycopy(orgLine, 0, copy, 0, orgLine.length);
                lines.add(copy);
            }
        }

        public void add(Shape s) {
            s.points().forEach(p -> occupy(p, '#'));
        }

        public int relativeHighest() { return highest.minus(floor).intValue(); }

        public boolean isFree(Coor relative) {
            if (relative.x() < 0 || relative.x() >= BOTTOM.length()) return false;
            int y = relative.y();
            return y >= lines.size() || lines.get(y)[relative.x()] == '.';
        }

        public int maxX() {
            return BOTTOM.length() - 1;
        }
        public int maxY() {
            return lines.size();
        }

        public String plot(Shape s, int depth) {
            Area copy = new Area(this, depth);
            s.points().forEach(p -> copy.occupy(p, '@'));
            return copy.print(depth);
        }

        @Override
        public String toString() {
            return print(10);
        }

        public void tryCut() {
            int minDepth = findMinDepth();
            if (minDepth > 0) {
                List<char[]> copy = new ArrayList<>(minDepth);
                for (int y = minDepth; y < lines.size(); ++y) {
                    copy.add(lines.get(y));
                }
                this.lines = copy; // drop previous, for GC
                this.floor = floor.plus(minDepth);
                DEBUG.trace("cut @%d (%d remains)", floor, copy.size());
                //DEBUG.trace("%s", print(20));
            }
        }

        private int findMinDepth() {
            int width = BOTTOM.length();
            Scanner scanner = new Scanner(width);
            for (int y = lines.size() - 1; y >= 0 && scanner.keepScanning(); --y) {
                scanner.scan(lines.get(y), y);
            }
            // scanned all but still contains 0 → keep all
            if (scanner.keepScanning()) return 0;
            return scanner.continuousMin(this);
        }

        private void occupy(Coor p, char marker) {
            while (p.y() >= lines.size()) lines.add(EMPTY.toCharArray());
            char[] line = lines.get(p.y());
            line[p.x()] = marker;
            if (p.y() > relativeHighest()) highest = floor.plus(p.y());
        }

        private String print(int depth) {
            StringBuilder printed = new StringBuilder();
            final int downTo = Math.max(lines.size() - depth, 0);
            for (int i = lines.size() -1; i >= downTo; --i) {
                printed.append('|').append(lines.get(i)).append('|');
                BigInt lineNum = floor.plus(i);
                if (lineNum.divisibleBy(5)) printed.append(lineNum);
                printed.append('\n');
            }
            return printed.toString();
        }
    }

    private static class Scanner {
        private final int[] depths;
        private int min = Integer.MAX_VALUE;

        Scanner(int width) {
            depths = new int[width];
        }

        public int continuousMin(Area area) {
            int ref = depths[0];
            int corrected = ref;
            for (int x = 1; x < depths.length; ++x) {
                int y = depths[x];
                if (adjacent(ref, y) || adjacent(corrected, y)) corrected = y;
                else corrected = findPath(x, y, area);
                ref = y;
            }
            return min;
        }

        private static boolean adjacent(int ref, int y) {
            return Math.abs(y - ref) <= 1;
        }

        public boolean keepScanning() {
            return Arrays.stream(depths).anyMatch(d -> d == 0);
        }

        public void scan(char[] line, int y) {
            for (int x = 0; x < depths.length; ++x) {
                if (line[x] != '.' && depths[x] == 0) {
                    depths[x] = y;
                    if (min > y) min = y;
                }
            }
        }

        private int findPath(int x, int y, Area area) {
            final Coor start = new Coor(x - 1, depths[x - 1]);
            final Coor goal = new Coor(x, y);
            final Set<Coor> deadEnds = new HashSet<>();
            Walkers walkers = Walkers.of(new Walker(start, goal, area, deadEnds));
            while (walkers.isNotEmpty()) {
                Walkers nexts = new Walkers();
                for (Walker w : walkers) {
                    if (w.arrived()) {
                        DEBUG.trace("DONE in %d", w.walked());
                        int absoluteMin = w.walk.stream().mapToInt(Coor::y).min().orElseThrow();
                        if (absoluteMin < this.min) this.min = absoluteMin; 
                        return w.walk.stream().filter(c -> c.x() == x).mapToInt(Coor::y).min().orElseThrow();
                    }

                    nexts.addAll(w.multiStep());
                }
                DEBUG.trace("next round %d", nexts.size());
                walkers = nexts;
            }
            return y;
        }
    }

    private static class Walker {

        private final Coor goal;
        private Coor current;
        private final Area area;
        private final Walk walk;
        private final Set<Coor> deadEnds;
        Walker(Coor start, Coor goal, Area area, Set<Coor> deadEnds) {
            this.area = area;
            this.current = start;
            this.deadEnds = deadEnds;
            this.goal = goal;
            walk = new Walk(start);
        }
        private Walker(Walker parent, Coor current) {
            this.area = parent.area;
            this.current = current;
            this.deadEnds = parent.deadEnds;
            this.goal = parent.goal;
            walk = new Walk(parent.walk, current);
        }

        public boolean arrived() { return current.equals(goal); }

        public void flagSteps(Walker w) {
            w.walk.forEachStep(deadEnds::add);
        }

        public List<Walker> multiStep() {
            List<Coor> eligibleArrivals = Stream.of(
                    tryDownRight(), tryDown(), tryDownLeft(),
                    tryLeft(),
                    tryUpLeft(), tryUp(), tryUpRight(),
                    tryRight()
            ).flatMap(Optional::stream).toList();
 
            if (eligibleArrivals.isEmpty()) {
                DEBUG.lifePulse();
                walk.forEachStep(deadEnds::add);
                return List.of();
            }
            if (eligibleArrivals.size() == 1) {
                current = eligibleArrivals.get(0);
                walk.save(current);
                return List.of(this);
            }
            return eligibleArrivals.stream()
                    .map(sub -> new Walker(this, sub))
                    .toList();
        }

        public boolean sameLastStep(Walker w) { return current.equals(w.current); }

        public int walked() { return walk.length(); }

        private Optional<Coor> eval(Coor candidate) {
            if (walk.contains(candidate)) return Optional.empty();
            if (area.isFree(candidate)) return Optional.empty();
            if (deadEnds.contains(candidate)) return Optional.empty();
            return Optional.of(candidate);
        }

        private Optional<Coor> tryDownRight() {
            if (current.y() == 0 || current.x() == area.maxX()) return Optional.empty(); // border
            return eval(current.down().right());
        }
        private Optional<Coor> tryDown() {
            if (current.y() == 0) return Optional.empty(); // border
            return eval(current.down());
        }
        private Optional<Coor> tryDownLeft() {
            if (current.y() == 0 || current.x() == 0) return Optional.empty(); // border
            return eval(current.down().left());
        }
        private Optional<Coor> tryLeft() {
            if (current.x() == 0) return Optional.empty(); // border
            return eval(current.left());
        }
        private Optional<Coor> tryUpLeft() {
            if (current.y() == area.maxY() || current.x() == 0) return Optional.empty(); // border
            return eval(current.up().left());
        }
        private Optional<Coor> tryUp() {
            if (current.y() == area.maxY()) return Optional.empty(); // border
            return eval(current.up());
        }
        private Optional<Coor> tryUpRight() {
            if (current.y() == area.maxY() || current.x() == area.maxX()) return Optional.empty(); // border
            return eval(current.up().right());
        }
        private Optional<Coor> tryRight() {
            if (current.x() == area.maxX()) return Optional.empty(); // border
            return eval(current.right());
        }
    }

    private static class Walkers implements Iterable<Walker> {

        private final List<Walker> inner = new ArrayList<>();
        static Walkers of(Walker first) {
            Walkers w = new Walkers();
            w.inner.add(first);
            return w;
        }

        public void add(Walker candidate) {
            ListIterator<Walker> ite = inner.listIterator();
            while (ite.hasNext()) {
                Walker w = ite.next();
                if (candidate.sameLastStep(w)) {
                    // only keep shortest walk to destination
                    if (candidate.walked() < w.walked()) {
                        candidate.flagSteps(w);
                        ite.set(candidate);
                    } else {
                        w.flagSteps(candidate);
                    }
                    return;
                }
            }
            inner.add(candidate);
        }

        public void addAll(Collection<Walker> values) {
            values.forEach(this::add);
        }

        public boolean isNotEmpty() {
            return !inner.isEmpty();
        }

        @Override
        public Iterator<Walker> iterator() {
            return inner.iterator();
        }

        public int size() {
            return inner.size();
        }
    }

    private static class Generator implements Iterator<Shape> {

        private final Area a;
        private final BigInt limit;
        private BigInt spawns;
        private final List<IntFunction<? extends Shape>> templates = List.of(
                Horiz::new,
                Cross::new,
                RevL::new,
                Vert::new,
                Square::new
        );
        private final int nbTemplates = templates.size();

        public Generator(Area a, BigInt limit) {
            this.a = a;
            this.limit = limit;
            spawns = BigInt.ZERO;
        }

        public boolean at(BigInt len) {
            return spawns.divisibleBy(len);
        }

        @Override
        public boolean hasNext() {
            return spawns.lowerTo(limit);
        }

        public Shape next() {
            Shape next = templates.get(spawns.mod(nbTemplates)).apply(a.relativeHighest());
            spawns = spawns.inc();
            return next;
        }
    }

    private interface Shape {
        boolean canDown(Area in);
        boolean canLeft(Area in);
        boolean canRight(Area in);
        Stream<Coor> points();
        void push(UnaryOperator<Coor> direction);
    }

    /**
     * <pre>
     *    tm
     *    |
     * ml—+—mr
     *    |
     *    bm</pre>
     */
    private static class Cross implements Shape {
        private Coor tm, ml, mr, bm;
        Cross(int highest) {
            tm = new Coor(3, highest + 6);
            ml = new Coor(2, highest + 5);
            mr = new Coor(4, highest + 5);
            bm = new Coor(3, highest + 4);
        }

        @Override
        public boolean canDown(Area in) {
            return in.isFree(bm.down()) && in.isFree(ml.down()) && in.isFree(mr.down());
        }
        @Override
        public boolean canLeft(Area in) {
            return in.isFree(ml.left()) && in.isFree(tm.left()) && in.isFree(bm.left());
        }
        @Override
        public boolean canRight(Area in) {
            return in.isFree(mr.right()) && in.isFree(tm.right()) && in.isFree(bm.right());
        }
        @Override
        public Stream<Coor> points() { return Stream.of(tm, mr, ml, bm, mid()); }

        private Coor mid() {
            return new Coor(tm.x(), ml.y());
        }

        @Override
        public void push(UnaryOperator<Coor> direction) {
            tm = direction.apply(tm);
            ml = direction.apply(ml);
            mr = direction.apply(mr);
            bm = direction.apply(bm);
        }
    }

    /**
     * <pre>
     * l—(l+3)</pre>
     */
    private static class Horiz implements Shape {
        private Coor l;
        Horiz(int highest) {
            int y = highest + 4;
            l = new Coor(2, y);
        }

        @Override
        public boolean canDown(Area in) {
            return points().map(Coor::down).allMatch(in::isFree);
        }
        @Override
        public boolean canLeft(Area in) {
            return in.isFree(l.left());
        }
        @Override
        public boolean canRight(Area in) {
            return in.isFree(new Coor(l.x() + 4, l.y()));
        }
        @Override
        public Stream<Coor> points() {
            return IntStream.rangeClosed(l.x(), l.x() + 3).mapToObj(x -> new Coor(x, l.y()));
        }

        @Override
        public void push(UnaryOperator<Coor> direction) {
            l = direction.apply(l);
        }
    }

    /**
     * <pre>
     *    tr
     *    |
     * bl—br</pre>
     */
    private static class RevL implements Shape {
        private Coor tr, bl, br;
        RevL(int highest) {
            tr = new Coor(4, highest + 6);
            bl = new Coor(2, highest + 4);
            br = new Coor(4, highest + 4);
        }

        @Override
        public boolean canDown(Area in) {
            return bottomPoints().map(Coor::down).allMatch(in::isFree);
        }

        @Override
        public boolean canLeft(Area in) {
            return in.isFree(bl.left()) && vertPoints().map(Coor::left).allMatch(in::isFree);
        }

        @Override
        public boolean canRight(Area in) {
            return in.isFree(br.right()) && vertPoints().map(Coor::right).allMatch(in::isFree);
        }

        @Override
        public Stream<Coor> points() { return Stream.concat(vertPoints(), bottomPoints()); }

        @Override
        public void push(UnaryOperator<Coor> direction) {
            tr = direction.apply(tr);
            bl = direction.apply(bl);
            br = direction.apply(br);
        }

        private Stream<Coor> bottomPoints() {
            return IntStream.rangeClosed(bl.x(), br.x()).mapToObj(x -> new Coor(x, bl.y()));
        }

        private Stream<Coor> vertPoints() {
            return IntStream.rangeClosed(br.y() + 1, tr.y()).mapToObj(y -> new Coor(tr.x(), y));
        }
    }

    /**
     * <pre>
     * tl—tr
     * |  |
     * bl—br</pre>
     */
    private static class Square implements Shape {
        private Coor tl, tr, bl, br;
        Square(int highest) {
            tl = new Coor(2, highest + 5);
            tr = new Coor(3, highest + 5);
            bl = new Coor(2, highest + 4);
            br = new Coor(3, highest + 4);
        }

        @Override
        public boolean canDown(Area in) {
            return in.isFree(bl.down()) && in.isFree(br.down());
        }
        @Override
        public boolean canLeft(Area in) {
            return in.isFree(tl.left()) && in.isFree(bl.left());
        }
        @Override
        public boolean canRight(Area in) {
            return in.isFree(tr.right()) && in.isFree(br.right());
        }
        @Override
        public Stream<Coor> points() { return Stream.of(tl, tr, bl, br); }

        @Override
        public void push(UnaryOperator<Coor> direction) {
            tl = direction.apply(tl);
            tr = direction.apply(tr);
            bl = direction.apply(bl);
            br = direction.apply(br);
        }
    }

    /**
     * <pre>
     * (b+3)
     *   |
     *   b</pre>
     */
    private static class Vert implements Shape {
        private Coor b;
        Vert(int highest) {
            b = new Coor(2, highest + 4);
        }

        @Override
        public boolean canDown(Area in) {
            return in.isFree(b.down());
        }
        @Override
        public boolean canLeft(Area in) {
            return points().map(Coor::left).allMatch(in::isFree);
        }
        @Override
        public boolean canRight(Area in) {
            return points().map(Coor::right).allMatch(in::isFree);
        }
        @Override
        public Stream<Coor> points() {
            return IntStream.rangeClosed(b.y(), b.y() + 3).mapToObj(y -> new Coor(b.x(), y));
        }

        @Override
        public void push(UnaryOperator<Coor> direction) {
            b = direction.apply(b);
        }
    }

    private enum Direction implements BiPredicate<Shape, Area>, Consumer<Shape> {
        LEFT(Shape::canLeft, Coor::left),
        RIGHT(Shape::canRight, Coor::right);

        private final BiPredicate<Shape, Area> condition;
        private final UnaryOperator<Coor> action;
        Direction(BiPredicate<Shape, Area> condition, UnaryOperator<Coor> action) {
            this.condition = condition;
            this.action = action;
        }

        @Override
        public void accept(Shape shape) { shape.push(action); }
        @Override
        public boolean test(Shape shape, Area area) { return condition.test(shape, area); }
    }

    private static class Vent {
        private final Map<Character, Direction> directions = Map.of(
                '>', Direction.RIGHT,
                '<', Direction.LEFT
        );
        private int idx = 0;
        private final String pushes;
        public Vent(String pushes) {
            this.pushes = pushes;
        }

        public void blow(Shape s, Area a) {
            Direction d = nextPush();
            if (d.test(s, a)) d.accept(s);
        }

        private Direction nextPush() {
            Direction d = directions.get(pushes.charAt(idx));
            idx = (idx + 1) % pushes.length();
            return d;
        }
    }
}
