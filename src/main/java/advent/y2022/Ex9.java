package advent.y2022;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Ex9 {

    private static final Pattern MOVE_FORMAT = Pattern.compile("^([UDRL]) (\\d+)$");
    public static final int TRAILING_LEN = 9;
    private static final int TAIL_IDX = TRAILING_LEN - 1;

    private Coor head = new Coor(0, 0);
    private final Coor[] knots = new Coor[TRAILING_LEN];
    private final Set<Coor> tailVisited = new HashSet<>();
    private Ex9() {
        Arrays.fill(knots, head);
        tailVisited.add(knots[TAIL_IDX]); // starting position is considered visited
    }

    public static void main(String[] args) throws IOException, URISyntaxException {
        URI input = Ex9.class.getResource("ex9.input.txt").toURI();
        Ex9 ex = new Ex9();
        Files.readAllLines(Path.of(input)).forEach(ex::moveHead);
        System.out.println(ex.tailVisited.size());
    }

    private void moveHead(String encoded) {
        Matcher m = MOVE_FORMAT.matcher(encoded);
        if (!m.matches()) throw new IllegalArgumentException("bad move " + encoded);

        Direction direction = Direction.valueOf(m.group(1));
        int steps = Integer.parseInt(m.group(2));
        while (steps > 0) {
            head = head.move(direction);
            head.pullAll(knots);
            tailVisited.add(knots[TAIL_IDX]);
            --steps;
        }
    }

    private enum Direction {
        U, D, L, R;
    }

    private record Coor(int x, int y) {

        public Coor pull(Coor tail) {
            return stable(tail) ? tail : tail.follow(this);
        }
        public void pullAll(Coor[] tail) {
            Coor last = this;
            for (int i = 0; i < TRAILING_LEN && !last.stable(tail[i]); ++i) {
                tail[i] = last = tail[i].follow(last);
            }
        }

        public Coor move(Direction d) {
            return switch (d) {
                case U -> up();
                case D -> down();
                case L -> left();
                case R -> right();
            };
        }

        private Coor follow(Coor ref) {
            Coor c = this;
            if (x != ref.x) c = x < ref.x ? c.right() : c.left();
            if (y != ref.y) c = y < ref.y ? c.up() : c.down();
            return c;
        }

        private boolean stable(Coor tail) {
            return Math.abs(x - tail.x) <= 1 && Math.abs(y - tail.y) <= 1;
        }

        private Coor up()    { return new Coor(x, y + 1); }
        private Coor down()  { return new Coor(x, y - 1); }
        private Coor left()  { return new Coor(x - 1, y); }
        private Coor right() { return new Coor(x + 1, y); }
        @Override
        public String toString() { return "(%d,%d)".formatted(x, y); }
    }
}
