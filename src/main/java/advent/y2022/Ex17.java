package advent.y2022;

import advent.Debug;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.nio.charset.Charset.defaultCharset;

public class Ex17 {
    private static final Debug DEBUG = Debug.OFF;

    public static void main(String[] args) throws IOException, URISyntaxException {
        URI input = Ex17.class.getResource("ex17.input.txt").toURI();

        String directions = null;
        try (BufferedReader reader = Files.newBufferedReader(Path.of(input), defaultCharset())) {
            directions = reader.readLine();
        }

        Vent v = new Vent(directions);
        Area a = new Area();
        Generator g = new Generator(a, 2023);
        Shape s = g.next();
        while (g.hasNext()) {
            v.blow(s, a);
            if (s.canDown(a)) s.push(Coor::down);
            else {
                a.add(s);
                s = g.next();
                DEBUG.trace("spawn at %d%n%s", v.ticked(), a.plot(s));
            }
        }
        System.out.printf("high=%s", a.highest());
    }

    private record Coor(int x, int y) {
        private Coor down()  { return new Coor(x, y - 1); }
        private Coor left()  { return new Coor(x - 1, y); }
        private Coor right() { return new Coor(x + 1, y); }
        @Override
        public String toString() { return "(%d,%d)".formatted(x, y); }
    }

    private static class Area {
        private static final String BOTTOM = "+-------+";
        private static final String EMPTY  = "|.......|";

        private Coor highest = new Coor(0, 0);
        private final List<char[]> lines = new ArrayList<>();
        public Area() {
            lines.add(BOTTOM.toCharArray());
        }

        public void add(Shape s) {
            s.points().forEach(p -> occupy(p, '#'));
        }

        public Coor highest() { return highest; }

        public boolean isFree(Coor c) {
            return c.y() >= lines.size()
                    ? c.x() > 0 && c.x() < BOTTOM.length() - 1
                    : lines.get(c.y())[c.x()] == '.';
        }

        public String plot(Shape s) {
            Area copy = new Area();
            for (int i = 1; i < lines.size(); ++i) {
                char[] org = lines.get(i);
                char[] line = new char[org.length];
                System.arraycopy(org, 0, line, 0, org.length);
                copy.lines.add(line);
            }
            s.points().forEach(p -> copy.occupy(p, '@'));
            return copy.toString();
        }

        @Override
        public String toString() {
            StringBuilder printed = new StringBuilder();
            for (int i = lines.size() -1; i >= 0; --i) {
                printed.append(lines.get(i));
                if (i % 5 == 0) printed.append(i);
                printed.append('\n');
            }
            return printed.toString();
        }

        private void occupy(Coor p, char marker) {
            while (p.y() >= lines.size()) lines.add(EMPTY.toCharArray());
            char[] line = lines.get(p.y());
            line[p.x()] = marker;
            if (p.y() > highest.y()) highest = p;
        }
    }

    private static class Generator implements Iterator<Shape> {

        private final Area a;
        private final int limit;
        private int spawns;
        private final List<Function<Coor, ? extends Shape>> templates = List.of(
                Horiz::new,
                Cross::new,
                RevL::new,
                Vert::new,
                Square::new
        );

        public Generator(Area a, int limit) {
            this.a = a;
            this.limit = limit;
            spawns = 0;
        }

        @Override
        public boolean hasNext() {
            return spawns < limit;
        }

        public Shape next() {
            Shape next = templates.get(spawns % templates.size()).apply(a.highest());
            ++spawns;
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
        Cross(Coor highest) {
            tm = new Coor(4, highest.y() + 6);
            ml = new Coor(3, highest.y() + 5);
            mr = new Coor(5, highest.y() + 5);
            bm = new Coor(4, highest.y() + 4);
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
        Horiz(Coor highest) {
            int y = highest.y() + 4;
            l = new Coor(3, y);
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
        RevL(Coor highest) {
            tr = new Coor(5, highest.y() + 6);
            bl = new Coor(3, highest.y() + 4);
            br = new Coor(5, highest.y() + 4);
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
        Square(Coor highest) {
            tl = new Coor(3, highest.y() + 5);
            tr = new Coor(4, highest.y() + 5);
            bl = new Coor(3, highest.y() + 4);
            br = new Coor(4, highest.y() + 4);
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
        Vert(Coor highest) {
            b = new Coor(3, highest.y() + 4);
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
        private int ticks = 0;
        private final String pushes;
        public Vent(String pushes) {
            this.pushes = pushes;
        }

        public void blow(Shape s, Area a) {
            Direction d = nextPush();
            if (d.test(s, a)) d.accept(s);
        }

        public int ticked() {
            return ticks;
        }

        private Direction nextPush() {
            Direction d = directions.get(pushes.charAt(ticks % pushes.length()));
            ++ticks;
            return d;
        }
    }
}
