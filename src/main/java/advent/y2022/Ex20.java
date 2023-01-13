package advent.y2022;

import advent.BigInt;
import advent.Debug;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Ex20 {
    private static final Debug DEBUG = Debug.ON;

    private static final BigInt KEY = new BigInt(811589153L);

    public static void main(String[] args) throws IOException, URISyntaxException {
        URI input = Ex20.class.getResource("ex20.example.txt").toURI();
        BigInt[] values = Files.readAllLines(Path.of(input)).stream()
                .map(val -> new BigInt(val).times(KEY))
                .toArray(BigInt[]::new);
        Mixed wk = new Mixed(values);
        DEBUG.trace("init: %s", wk);
        for (int i = 0; i < 1; ++i) {
            for (int rank = 0; rank < values.length; ++rank) {
                wk.move(rank);
            }
            DEBUG.trace("after %d: %s", i, wk);
        }
        BigInt a = wk.getK(1);
        BigInt b = wk.getK(2);
        BigInt c = wk.getK(3);
        System.out.printf("%s+%s+%s=%s", a, b, c, a.plus(b).plus(c));
    }

    private static class Mixed {

        private final boolean[] touched;
        private final BigInt[] values;
        private int zeroIdx = -1;

        public Mixed(BigInt[] values) {
            this.touched = new boolean[values.length];
            this.values = new BigInt[values.length];
            System.arraycopy(values, 0, this.values, 0, values.length);
        }

        public void move(int rank) {
            int from = firstUntouched();
            BigInt val = values[from];

            int to = wrap(from, val);

            if (to == from) {
                touched[to] = true; // record touch
                return; // but otherwise noop
            }

            if (to == 0 && val.lowerTo(BigInt.ZERO)) putLast(from);
            else if (to < from) backward(from, to);
            else forward(from, to);
        }

        public BigInt getK(int mult) {
            if (zeroIdx < 0) {
                zeroIdx = 0;
                while (zeroIdx < values.length && !values[zeroIdx].equals(BigInt.ZERO)) {
                    ++zeroIdx;
                }
            }
            int offset = zeroIdx + mult * 1000;
            return values[offset % values.length];
        }

        @Override
        public String toString() {
            StringBuilder s = new StringBuilder().append('[');
            for (int i = 0; i < values.length; ++i) {
                if (touched[i]) s.append('!');
                s.append(values[i]).append(',');
            }
            return s.deleteCharAt(s.length() - 1).append(']').toString();
        }

        /** <pre>[..a_bcXd...] → [...aXbcd...]</pre> */
        private void backward(int from, int to) {
            BigInt val = values[from];
            int shifted = from - to;
            System.arraycopy(values, to, values, to + 1, shifted);
            System.arraycopy(touched, to, touched, to + 1, shifted);
            values[to] = val;
            touched[to] = true;
        }

        private int firstUntouched() {
            int i = 0;
            while (i < touched.length && touched[i]) { ++i; }
            if (i == touched.length) throw new IllegalStateException("already finished");
            return i;
        }

        /** <pre>[..aXbc_d...] → [...abcXd...]</pre> */
        private void forward(int from, int to) {
            BigInt val = values[from];
            int shifted = to - from;
            System.arraycopy(values, from + 1, values, from, shifted);
            System.arraycopy(touched, from + 1, touched, from, shifted);
            values[to] = val;
            touched[to] = true;
        }

        /** <pre>[..aXbcd] → [...abcdX]</pre> */
        private void putLast(int from) {
            forward(from, values.length - 1);
        }

        private int wrap(int from, BigInt val) {
            final int len = values.length;
            BigInt i = val.plus(from);
            BigInt d = i.dividedBy(len);
            if (i.lowerTo(BigInt.ZERO)) {
                BigInt backToPositive = BigInt.ONE.minus(d).times(len);
                return i.plus(backToPositive).minus(BigInt.ONE).intValue();
            }
            return i.minus(d.times(len)).plus(d).mod(len);
        }
    }
}
