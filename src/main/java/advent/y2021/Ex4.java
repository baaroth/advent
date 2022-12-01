package advent.y2021;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

import static java.nio.charset.Charset.defaultCharset;

public class Ex4 {

	private String draws;
	private final List<Grid> grids = new ArrayList<>();

	public static void main(String[] args) throws IOException, URISyntaxException {
		URI input = Ex4.class.getResource("ex4.input.txt").toURI();
		Ex4 ex = new Ex4();
		int gridId = 1;
		try (BufferedReader reader = Files.newBufferedReader(Path.of(input), defaultCharset())) {
			ex.draws = reader.readLine();
			String line = reader.readLine();
			while (line != null) {
				Grid g = new Grid(gridId++);
				g.load(reader.readLine());
				g.load(reader.readLine());
				g.load(reader.readLine());
				g.load(reader.readLine());
				g.load(reader.readLine());
				ex.grids.add(g);
				line = reader.readLine();
			}
		}
		ex.run();
	}

	private void run() {
		Set<Integer> bingoed = new HashSet<>();
		for (String raw : draws.split(",")) {
			Draw mark = new Draw(Integer.parseInt(raw, 10));
			grids.stream()
					.filter(grid -> !bingoed.contains(grid.id))
					.forEach(mark);
			bingoed.addAll(mark.bingoed);

			if (bingoed.size() == grids.size()) {
				System.out.printf("last BINGO scored %d%n", mark.lastScore());
				return;
			}
		}
	}

	private static class Draw implements Consumer<Grid> {
		private final Set<Integer> bingoed = new HashSet<>();
		private final int value;
		private Grid lastBingo;

		public Draw(int value) {
			this.value = value;
		}

		@Override
		public void accept(Grid grid) {
			if (grid.mark(value) == State.BINGO) {
				lastBingo = grid;
				System.out.printf("%d » BINGO #%d%n", value, grid.id);
				bingoed.add(grid.id);
			}
		}

		public int lastScore() {
			if (lastBingo == null) throw new IllegalStateException("no bingo");
			return lastBingo.score(value);
		}
	}

	private static class Grid {
		private static final Pattern INPUT_FORMAT = Pattern.compile("^([ 1-9][0-9]) ([ 1-9][0-9]) ([ 1-9][0-9]) ([ 1-9][0-9]) ([ 1-9][0-9])$");

		private final int id;
		private final Square[][] squares;
		private int j;

		public Grid(int id) {
			this.id = id;
			j = 0;
			squares = new Square[5][];
		}

		void load(String input) {
			if (j > 4) throw new IllegalStateException("already finished");
			Matcher m = INPUT_FORMAT.matcher(input);
			if (m.matches()) {
				Square[] line = new Square[5];
				for (int i = 0; i < 5; ++i) {
					line[i] = new Square(Integer.parseInt(m.group(i + 1).trim(), 10));
				}
				squares[j++] = line;
			} else {
				throw new IllegalArgumentException("bad grid line: " + input);
			}
		}

		boolean isBingo() {
			return IntStream.range(0, 5).anyMatch(i -> isBingoLine(i) || isBingoCol(i));
		}

		State mark(int value) {
			boolean[] marked = {false};
			Arrays.stream(squares).flatMap(Arrays::stream)
					.filter(square -> square.val() == value)
					.forEach(square -> {
						square.mark();
						marked[0] = true;
					});

			if (marked[0] && isBingo()) {
				return State.BINGO;
			}
			return State.DRAW_NEXT;
		}

		int score(int lastCall) {
			int unmarkedSum = Arrays.stream(squares).flatMap(Arrays::stream)
					.filter(Square::isUnmarked)
					.mapToInt(Square::val)
					.sum();
			return unmarkedSum * lastCall;
		}

		@Override
		public String toString() {
			StringBuilder s = new StringBuilder();
			for (Square[] line : squares) {
				for (Square col : line) {
					s.append(col);
				}
				s.append("\n");
			}
			return s.toString();
		}

		private boolean isBingoLine(int line) {
			return Arrays.stream(squares[line]).noneMatch(Square::isUnmarked);
		}
		private boolean isBingoCol(int col) {
			return IntStream.range(0, 5).noneMatch(line -> squares[line][col].isUnmarked());
		}
	}

	private enum State {
		DRAW_NEXT, BINGO
	}

	private static class Square {
		private final int value;
		private boolean marked;

		public Square(int value) {
			this.value = value;
		}

		public void mark() {
			this.marked = true;
		}
		public boolean isUnmarked() {
			return !marked;
		}
		public int val() {
			return value;
		}

		@Override
		public String toString() {
			StringBuilder s = new StringBuilder();
			if (value < 10) s.append(' ');
			s.append(value).append(marked ? '!' : ' ');
			return s.toString();
		}
	}
}
