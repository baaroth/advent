package advent.y2022;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Ex6 {

    private static int start(String buffer) {
        final Memory mem = new Memory();
        mem.init(buffer);
        int count = 3;
        while (count < buffer.length()) {
            char next = buffer.charAt(count++);
            if (mem.endSeq(next)) return count;
            mem.put(next);
        }

        return -1;
    }

    public static void main(String[] args) throws IOException, URISyntaxException {
        URI input = Ex6.class.getResource("ex6.input.txt").toURI();
        Files.readAllLines(Path.of(input)).forEach(line -> System.out.println(start(line)));
    }

    private static class Memory {
        public static final int LEN = 13;
        private final char[] values = new char[LEN];
        private int next;
        private boolean distinct;

        void init(String buffer) {
            for (next = 0; next < LEN - 1; ++next) {
                values[next] = buffer.charAt(next);
            }
            put(buffer.charAt(2)); // to set `distinct`
        }

        boolean endSeq(char challenge) {
            if (!distinct) return false;
            return miss(challenge, 0);
        }

        private boolean miss(char challenge, int start) {
            for (int i = start; i < LEN; ++i) {
                if (challenge == values[i]) return false;
            }
            return true;
        }

        void put(char c) {
            values[next] = c;
            next = (next + 1) % LEN;
            for (int i = 0; i < LEN - 1; ++i) {
                if (!miss(values[i], i + 1)) {
                    distinct = false;
                    return;
                }
            }
            distinct = true;
        }
    }
}
