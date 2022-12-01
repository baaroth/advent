package advent.y2021;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import advent.SpecialInt;
import advent.SpecialInt.X;
import advent.SpecialInt.Y;

public class Ex5 {

	private static final Pattern REGEX_LINE = Pattern.compile("^(\\d+),(\\d+) -> (\\d+),(\\d+)$");

	private final Map<Coor, Integer> weights = new HashMap<>();

	public Ex5(List<String> rawLines) {
		rawLines.stream()
				.map(this::parse)
				.flatMap(Line::stream)
				.forEach(coor -> weights.merge(coor, 1, Integer::sum));
	}

	public static void main(String[] args) throws IOException, URISyntaxException {
		URI input = Ex5.class.getResource("ex5.input.txt").toURI();
		Ex5 ex = new Ex5(Files.readAllLines(Path.of(input)));
		System.out.printf("%d overlaps%n", ex.countOverlaps());
	}

	private long countOverlaps() {
		return weights.values().stream()
				.filter(val -> val > 1)
				.count();
	}

	private Line parse(String raw) {
		Matcher m = REGEX_LINE.matcher(raw);
		if (m.matches()) {
			Coor begin = new Coor(m.group(1), m.group(2));
			Coor end = new Coor(m.group(3), m.group(4));
			return new Line(begin, end);
		}
		throw new IllegalArgumentException("bad line: " + raw);
	}

	record Coor(X x, Y y) {
		public Coor(String rawX, String rawY) {
			this(
					new X(Integer.parseInt(rawX, 10)),
					new Y(Integer.parseInt(rawY, 10))
			);
		}

		@Override
		public String toString() {
			return "%s,%s".formatted(x, y);
		}
	}

	record Line(Coor begin, Coor end) {

		boolean diagonal() {
			int deltaX = Math.abs(begin.x().val() - end.x().val());
			int deltaY = Math.abs(begin.y().val() - end.y().val());
			return deltaX == deltaY;
		}

		boolean horizontal() {
			return begin.y().equals(end.y());
		}

		boolean vertical() {
			return begin.x().equals(end.x());
		}

		public Stream<Coor> stream() {
			if (horizontal())
				return new SpecialInt.Range<>(begin.x(), end.x()).stream()
						.map(x -> new Coor(x, begin.y()));

			if (vertical())
				return new SpecialInt.Range<>(begin.y(), end.y()).stream()
						.map(y -> new Coor(begin.x(), y));

			if (diagonal())
				return DiagonalSpitr.stream(this);

			throw new UnsupportedOperationException("non-streamable line");
		}

		@Override
		public String toString() {
			return "%s -> %s".formatted(begin, end);
		}
	}

	private static class DiagonalSpitr implements Spliterator<Coor> {
		private Coor current;
		private final Coor end;
		private final UnaryOperator<X> nextX;
		private final UnaryOperator<Y> nextY;

		public DiagonalSpitr(Line diagonal) {
			this.current = diagonal.begin();
			this.end = diagonal.end();
			nextX = current.x().unaryOpTo(end.x());
			nextY = current.y().unaryOpTo(end.y());
		}
		static Stream<Coor> stream(Line diagonal) {
			return Stream.concat(
					StreamSupport.stream(new DiagonalSpitr(diagonal), false),
					Stream.of(diagonal.end) // unreachable via spliterator
			);
		}

		@Override
		public boolean tryAdvance(Consumer<? super Coor> action) {
			if (!current.equals(end)) {
				action.accept(current);
				current = new Coor(
						nextX.apply(current.x()),
						nextY.apply(current.y())
				);
				return true;
			}
			return false;
		}

		@Override
		public Spliterator<Coor> trySplit() {
			return null; // no split
		}

		@Override
		public long estimateSize() {
			long base = end.x().val();
			return Math.abs(base - current.x().val());
		}

		@Override
		public int characteristics() {
			return SIZED | IMMUTABLE | SUBSIZED;
		}
	}
}
