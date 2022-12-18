package advent;

import java.math.BigInteger;

public record Coor(int x, int y) {
    @Override
    public String toString() {
        return "(%d,%d)".formatted(x, y);
    }
    public Coor up()    { return new Coor(x, y + 1); }
    public Coor down()  { return new Coor(x, y - 1); }
    public Coor left()  { return new Coor(x - 1, y); }
    public Coor right() { return new Coor(x + 1, y); }
}
