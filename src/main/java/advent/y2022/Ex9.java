package advent.y2022;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Ex9 {

    private static final Pattern MOVE_FORMAT = Pattern.compile("^([UDRL]) (\\d+)$");
    private Coor head = new Coor(0, 0);
    private Coor tail = head;
    private final Set<Coor> visited = new HashSet<>();
    private Ex9() {
        visited.add(tail); // starting position is considered visited
    }

    public static void main(String[] args) throws IOException, URISyntaxException {
        URI input = Ex9.class.getResource("ex9.input.txt").toURI();
        Ex9 ex = new Ex9();
        Files.readAllLines(Path.of(input)).forEach(ex::moveHead);
        System.out.println(ex.visited.size());
    }

    private void moveHead(String encoded) {
        Matcher m = MOVE_FORMAT.matcher(encoded);
        if (!m.matches()) throw new IllegalArgumentException("bad move " + encoded);

        Direction direction = Direction.valueOf(m.group(1));
        int steps = Integer.parseInt(m.group(2));
        while (steps > 0) {
            head = head.move(direction);
            tail = head.pull(tail);
            visited.add(tail);
            --steps;
        }
    }

    private enum Direction {
        U, D, L, R;
    }

    private record Coor(int x, int y) {

        Coor pull(Coor tail) {
            if (stable(tail)) return tail;
            Coor after = tail;
            if (x != tail.x) after = x > tail.x ? after.right() : after.left();
            if (y != tail.y) after = y > tail.y ? after.up() : after.down();
            return after;
        }

        public Coor move(Direction d) {
            return switch (d) {
                case U -> up();
                case D -> down();
                case L -> left();
                case R -> right();
            };
        }

        private boolean stable(Coor tail) {
            return Math.abs(x - tail.x) <= 1 && Math.abs(y - tail.y) <= 1;
        }
        private Coor up() {
            return new Coor(x, y + 1);
        }
        private Coor down() {
            return new Coor(x, y - 1);
        }
        private Coor left() {
            return new Coor(x - 1, y);
        }
        private Coor right() {
            return new Coor(x + 1, y);
        }
    }
}
