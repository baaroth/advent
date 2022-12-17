package advent.y2022;

import advent.Debug;
import advent.RecursionMonitor;
import advent.Trampoline;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Stream;

import static java.nio.charset.Charset.defaultCharset;

public class Ex13 {
	private static final Debug DEBUG = Debug.OFF;
	private static final RecursionMonitor RECURSE = new RecursionMonitor();

	public static void main(String[] args) throws IOException, URISyntaxException {
		URI input = Ex13.class.getResource("ex13.input.txt").toURI();

		List<Packet> packets = new ArrayList<>();
		try (BufferedReader reader = Files.newBufferedReader(Path.of(input), defaultCharset())) {
			String last = "";
			while (last != null) {
				packets.add(new Packet(reader.readLine()));
				packets.add(new Packet(reader.readLine()));
				last = reader.readLine(); // line-feed
			}
		}
		Packet[] sorted = packets.stream().sorted().toArray(Packet[]::new);
		int divideBegin = -Arrays.binarySearch(sorted, new Packet("[[2]]"));
		// add 1 to simulate other divider packet in the list
		int divideEnd = 1 - Arrays.binarySearch(sorted, new Packet("[[6]]"));
		System.out.printf("%d×%d=%d in %d max recursions", divideBegin, divideEnd, divideBegin * divideEnd, RECURSE.max());
	}

	private enum Order { EQUAL, GREATER_THAN, LESS_THAN }

	private interface BouncyOrder extends Trampoline<Order> {
		static BouncyOrder stable(Order o) { return () -> o; }
		static BouncyOrder unstable(Region a, Region b) {
			DEBUG.trace("  %s vs. %s", a, b);
			Trampoline<Trampoline<Order>> eval = () -> a.compareWith(b);
			return new BouncyOrder() {
				@Override
				public Trampoline<Order> bounce() { return eval.result(); }

				@Override
				public boolean finished() { return false; }

				@Override
				public Order result() {
					Trampoline<Order> seed = this;
					return Stream.iterate(seed, Trampoline::bounce)
							.filter(Trampoline::finished)
							.findFirst()
							.map(Trampoline::result)
							.orElseThrow();
				}
			};
		}

		default boolean isTie() {
			return finished() && result().equals(Order.EQUAL);
		}
	}

	private interface LazyOrdered<T extends LazyOrdered<T>> {
		BouncyOrder compareWith(T other);
	}

	private record Packet(String raw) implements Comparable<Packet>, LazyOrdered<Packet> {

		@Override
		public int compareTo(Packet other) {
			return switch (compareWith(other).result()) {
				case EQUAL -> 0;
				case LESS_THAN -> -1;
				case GREATER_THAN -> 1;
			};
		}

		@Override
		public BouncyOrder compareWith(Packet other) {
			DEBUG.trace("compare %s%n   with %s", this, other);
			Region a = new Region.Array(raw);
			Region b = new Region.Array(other.raw);
			return a.compareWith(b);
		}

		@Override
		public String toString() {
			return raw;
		}
	}

	private sealed interface Region extends LazyOrdered<Region> {
		int end();

		final class Array implements Region, Iterator<Region> {
			private final Array parent;
			private final String orig;
			private final int start, end;
			private int cursor;

			public Array(String orig) {
				this.orig = orig;
				this.start = 1;
				cursor = 1;
				end = findEnd(orig, start);
				parent = null;
			}
			Array(Num single) {
				orig = "{%d]".formatted(single.val); // open as curly brace to recognize pretended array is logs
				start = 1;
				cursor = 1;
				end = orig.length() - 1;
				parent = single.parent;
			}
			private Array(Array parent) {
				this.orig = parent.orig;
				this.start = parent.cursor + 1;
				this.parent = parent;
				cursor = start;
				end = findEnd(orig, this.start);
			}

			@Override
			public BouncyOrder compareWith(Region other) {
				RECURSE.in();
				Array otherArr = asArray(other);
				while (hasNext() && otherArr.hasNext()) {
					BouncyOrder cmp = compareNext(otherArr);
					if (!cmp.isTie()) {
						return RECURSE.out(cmp);
					}
				}
				if (hasNext()) {
					DEBUG.trace("  >");
					return RECURSE.out(BouncyOrder.stable(Order.GREATER_THAN)); // they exhausted before us
				} else if (otherArr.hasNext()) {
					DEBUG.trace("  <");
					return RECURSE.out(BouncyOrder.stable(Order.LESS_THAN)); // we exhausted first
				} else if (parent != null) {
					return RECURSE.out(BouncyOrder.unstable(parent, otherArr.parent)); // exhausted together, break tie with parent
				} else {
					DEBUG.trace("  =");
					return RECURSE.out(BouncyOrder.stable(Order.EQUAL)); // truly tied
				}
			}

			private BouncyOrder compareNext(Array other) {
				Region a = next();
				Region b = other.next();
				return a instanceof Array
						? BouncyOrder.unstable(a, b)
						: a.compareWith(b); // allow single-depth recursion
			}

			@Override
			public int end() { return end; }

			@Override
			public boolean hasNext() { return cursor < end; }

			@Override
			public Region next() {
				if (!hasNext()) throw new NoSuchElementException("exhausted region");
				Region nextRegion = nested() ? new Array(this) : new Num(this, cursor);
				cursor = nextRegion.end() + 1;
				if (cursor < end && orig.charAt(cursor) == ',') ++cursor;
				return nextRegion;
			}

			@Override
			public String toString() {
				StringBuilder s = new StringBuilder();
				s.append(orig, start - 1, cursor);
				s.append('↓');
				s.append(orig, cursor, end + 1);
				return s.toString();
			}

			private static Array asArray(Region other) {
				if (other instanceof Array arr) return arr;
				else if (other instanceof Num num) return new Array(num);
				throw new UnsupportedOperationException("not arrayable " + other.getClass());
			}

			private static int findEnd(String s, int start) {
				int depth = 0;
				for (int i = start; i < s.length(); ++i) {
					if (s.charAt(i) == ']') {
						if (depth == 0) return i;
						else --depth;
					}
					if (s.charAt(i) == '[') ++depth;
				}
				throw new IllegalArgumentException("unfinished region");
			}

			private boolean nested() {
				return orig.charAt(cursor) == '[';
			}
		}

		final class Num implements Region {
			private final Array parent;
			private final int val, end;

			public Num(Array parent, int start) {
				int comma = parent.orig.indexOf(',', start);
				this.end = comma < 0 || comma > parent.end ? parent.end : comma;
				this.parent = parent;
				val = Integer.parseInt(parent.orig.substring(start, end));
			}

			@Override
			public BouncyOrder compareWith(Region other) {
				RECURSE.in();
				if (other instanceof Num num) {
					return RECURSE.out(BouncyOrder.stable(compareNums(num)));
				}
				if (other instanceof Array arr) {
					return RECURSE.out(BouncyOrder.unstable(new Array(this), arr));
				}
				throw RECURSE.out(new UnsupportedOperationException("comparing " + other.getClass()));
			}

			@Override
			public int end() { return end; }

			private Order compareNums(Num num) {
				if (val < num.val) return Order.LESS_THAN;
				else if (val == num.val) return Order.EQUAL;
				else return Order.GREATER_THAN;
			}

			@Override
			public String toString() {
				return String.valueOf(val);
			}
		}
	}
}
