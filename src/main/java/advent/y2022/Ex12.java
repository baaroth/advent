package advent.y2022;

import advent.Debug;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.IntSummaryStatistics;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;

public class Ex12 {

	private static final Debug INFO = Debug.ON;
	private static final Debug DEBUG = Debug.OFF;

	public static void main(String[] args) throws IOException, URISyntaxException {
		URI input = Ex12.class.getResource("ex12.input.txt").toURI();
		Grid g = new Grid();
		Files.readAllLines(Path.of(input)).forEach(g::load);
		int best = g.streamStarts()
				.map(g::findBestExit)
				.flatMapToInt(OptionalInt::stream)
				.min()
				.orElse(-1);
		System.out.println("min=" + best);
	}

	private static class Walker {
		private final Grid grid;
		private Coor current;
		private final Walk walk;
		private final Set<Coor> deadEnds;

		public Walker(Grid grid, Coor start, Set<Coor> deadEnds) {
			this.grid = grid;
			this.current = start;
			this.deadEnds = deadEnds;
			walk = new Walk(current);
		}
		private Walker(Walker parent, Coor current) {
			this.grid = parent.grid;
			this.current = current;
			this.deadEnds = parent.deadEnds;
			walk = new Walk(parent.walk, current);
		}

		public boolean arrived() {
			return current.equals(grid.end);
		}

		public List<Walker> multiStep() {
			List<Coor> eligibleArrivals = Stream.of(tryUp(), tryDown(), tryLeft(), tryRight())
						.flatMap(Optional::stream)
						.toList();
			//DEBUG.trace("%s can go %s", walk, eligibleArrivals);
			if (eligibleArrivals.isEmpty()) {
				DEBUG.lifePulse();
				deadEnds.addAll(walk.steps);
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

		public boolean sameLastStep(Walker w) {
			return current.equals(w.current);
		}

		private Optional<Coor> eval(Coor candidate) {
			if (walk.contains(candidate)) return Optional.empty();
			if (tooSteep(candidate)) return Optional.empty();
			if (deadEnds.contains(candidate)) return Optional.empty();
			return Optional.of(candidate);
		}

		private boolean tooSteep(Coor candidate) {
			return grid.get(candidate) - grid.get(current) > 1;
		}

		private Optional<Coor> tryDown() {
			if (current.y == grid.maxY()) return Optional.empty(); // border
			return eval(current.down());
		}

		private Optional<Coor> tryLeft() {
			if (current.x == 0) return Optional.empty(); // border
			return eval(current.left());
		}

		private Optional<Coor> tryRight() {
			if (current.x == grid.maxX()) return Optional.empty(); // border
			return eval(current.right());
		}

		private Optional<Coor> tryUp() {
			if (current.y == 0) return Optional.empty(); // border
			return eval(current.up());
		}

		private int walked() {
			return walk.length();
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
					if (candidate.walked() < w.walked()) ite.set(candidate);
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

	private static class Walk {

		private final List<Coor> steps;
		public Walk(Coor first) {
			steps = new ArrayList<>();
			steps.add(first);
		}
		public Walk(Walk start, Coor next) {
			steps = new ArrayList<>(start.steps);
			save(next);
		}

		public boolean contains(Coor c) { return steps.contains(c); }
		public Coor last() { return steps.get(steps.size() - 1); }
		/** Starting pos. is memorized (see {@link Walker#eval(Coor)}) but does not count. */
		public int length() { return steps.size() - 1; }
		@Override
		public String toString() {
			return steps.stream().map(Coor::toString).collect(joining("→"));
		}
		public String treePrint() {
			if (steps.size() == 1) return steps.get(0).toString();
			int last = steps.size() - 1;
			String pad = "  ".repeat(last);
			return "%s→%s".formatted(pad, steps.get(last));
		}
		public final void save(Coor next) { steps.add(next); }
	}

	private static class Grid {

		private final List<char[]> lines = new ArrayList<>();
		private int len;
		private Coor start, end;

		public char get(Coor c) {
			return lines.get(c.y())[c.x()];
		}

		public void load(String encoded) {
			int y = lines.size();
			if (y == 0) len = encoded.length();
			char[] line = new char[len];
			for (int x = 0; x < len; ++x) {
				char height = switch (encoded.charAt(x)) {
					case 'S' -> { start = new Coor(x, y); yield 'a'; }
					case 'E' -> { end = new Coor(x, y); yield 'z'; }
					default -> encoded.charAt(x);
				};
				line[x] = height;
			}
			lines.add(line);
		}

		public Stream<Coor> streamStarts() {
			final List<Coor> starts = new ArrayList<>();
			int y = 0;
			for (char[] line : lines) {
				for (int x = 0; x < len; ++x)
					if (line[x] == 'a')
						starts.add(new Coor(x, y));
				++y;
			}
			return starts.stream();
		}

		@Override
		public String toString() {
			StringBuilder s = new StringBuilder();
			for (char[] line : lines) {
				for (char c : line) {
					s.append(c).append(' ');
				}
				s.append('\n');
			}
			s.append("start=").append(start);
			s.append(" end=").append(end);
			return s.toString();
		}

		public int maxX() {
			return len - 1;
		}
		public int maxY() {
			return lines.size() - 1;
		}

		public OptionalInt findBestExit(Coor start) {
			DEBUG.trace("== %s ==", start);
			final Set<Coor> deadEnds = new HashSet<>();
			final List<Walk> wins = new ArrayList<>();
			Walkers walkers = Walkers.of(new Walker(this, start, deadEnds));
			while (walkers.isNotEmpty()) {
				Walkers nexts = new Walkers();
				for (Walker w : walkers) {
					if (w.arrived()) {
						DEBUG.trace("DONE in %d", w.walked());
						wins.add(w.walk);
						continue;
					}

					nexts.addAll(w.multiStep());
				}
				DEBUG.trace("next round %d", nexts.size());
				walkers = nexts;
			}
			OptionalInt min = wins.stream()
					.mapToInt(Walk::length)
					.min();
			INFO.trace("%s: %d", start, min.orElse(-1));
			return min;
		}
	}

	private record Coor(int x, int y) {
		@Override
		public String toString() {
			return "(%d,%d)".formatted(x, y);
		}
		private Coor up() { return new Coor(x, y - 1); }
		private Coor down() { return new Coor(x, y + 1); }
		private Coor left() { return new Coor(x - 1, y); }
		private Coor right() { return new Coor(x + 1, y); }
	}
}
