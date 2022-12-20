package advent.y2022;

import advent.Debug;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Ex20 {
    private static final Debug DEBUG = Debug.OFF;

    public static void main(String[] args) throws IOException, URISyntaxException {
        URI input = Ex20.class.getResource("ex20.input.txt").toURI();
        int[] values = Files.readAllLines(Path.of(input)).stream()
                .mapToInt(Integer::parseInt)
                .toArray();
        Mixed wk = new Mixed(values);
        DEBUG.trace("init: %s", wk);
        for (int val : values) {
            wk.move(val);
            DEBUG.trace("after %d: %s", val, wk);
        }
        int a = wk.getK(1);
        int b = wk.getK(2);
        int c = wk.getK(3);
        System.out.printf("%d+%d+%d=%d", a, b, c, a+b+c);
    }

    private static class Mixed {

        private final boolean[] touched;
        private final int[] values;
        private int zeroIdx = -1;

        public Mixed(int[] values) {
            this.touched = new boolean[values.length];
            this.values = new int[values.length];
            System.arraycopy(values, 0, this.values, 0, values.length);
        }

        public void move(int canary) {
            int from = firstUntouched();
            int val = values[from];
            if (val != canary) throw new IllegalStateException("lost head! (expected %d, got %d)".formatted(canary, val));

            int to = wrap(from + val);

            if (to == from) {
                touched[to] = true; // record touch
                return; // but otherwise noop
            }

            if (to == 0 && val < 0) putLast(from);
            else if (to < from) backward(from, to);
            else forward(from, to);
        }

        public int getK(int mult) {
            if (zeroIdx < 0) {
                zeroIdx = 0;
                while (zeroIdx < values.length && values[zeroIdx] != 0) {
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
            int val = values[from];
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
            int val = values[from];
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

        private int wrap(int raw) {
            int i = raw;
            while (i < 0) i = i + values.length - 1;
            while (i >= values.length) i = i - values.length + 1;
            return i;
        }
    }
}
