package advent.y2022;

import advent.Debug;
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
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.nio.charset.Charset.defaultCharset;
import static java.util.stream.Collectors.joining;

public class Ex13 {
	private static final Debug DEBUG = Debug.OFF;

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
		System.out.printf("%d×%d=%d", divideBegin, divideEnd, divideBegin * divideEnd);
	}

	private enum Order { EQUAL, GREATER_THAN, LESS_THAN }

	private interface BouncyOrder extends Trampoline<Order> {
		static BouncyOrder stable(Order o) { return () -> o; }
		static BouncyOrder unstable(Trampoline<Trampoline<Order>> eval) {
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

		default boolean is(Order o) {
			return finished() && result().equals(o);
		}
	}

	private record Packet(String raw) implements Comparable<Packet> {

		@Override
		public int compareTo(Packet other) {
			Region a = new Region.Array(raw, 1);
			Region b = new Region.Array(other.raw, 1);
			return switch (a.compareWith(b).result()) {
				case EQUAL -> 0;
				case LESS_THAN -> -1;
				case GREATER_THAN -> 1;
			};
		}

		@Override
		public String toString() {
			return raw;
		}
	}

	private sealed interface Region {
		BouncyOrder compareWith(Region other);
		int end();

		final class Array implements Region, Iterator<Region> {
			private final String orig;
			private final int start, end;
			private int current, next;

			public Array(String orig, int start) {
				this.orig = orig;
				this.start = start;
				current = start - 1;
				next = start;
				end = findEnd(orig, start);
			}
			private Array(Array parent) {
				this.orig = parent.orig;
				this.start = parent.next + 1;
				current = parent.next;
				next = start;
				end = findEnd(orig, this.start);
			}

			@Override
			public BouncyOrder compareWith(Region other) {
				DEBUG.trace("compare %s vs %s", this, other);
				Array otherArr = asArray(other);
				while (hasNext() && otherArr.hasNext()) {
					BouncyOrder cmp = compareNext(otherArr);
					if (!cmp.is(Order.EQUAL)) {
						return cmp;
					}
				}
				if (hasNext()) {
					return BouncyOrder.stable(Order.GREATER_THAN); // they exhausted before us
				} else if (otherArr.hasNext()) {
					return BouncyOrder.stable(Order.LESS_THAN); // we exhausted first
				} else {
					return BouncyOrder.stable(Order.EQUAL);// both exhausted at the same time
				}
			}

			private BouncyOrder compareNext(Array other) {
				BouncyOrder comparison = next().compareWith(other.next());
				if (comparison.is(Order.LESS_THAN)) {
					DEBUG.trace("%s%n%s%n<<", this, other);
				} else if (comparison.is(Order.GREATER_THAN)) {
					DEBUG.trace("%s%n%s%n>>", this, other);
				}
				return comparison;
			}

			@Override
			public int end() { return end; }

			@Override
			public boolean hasNext() { return next < end; }

			@Override
			public Region next() {
				if (!hasNext()) throw new NoSuchElementException("exhausted region");
				Region nextRegion = nested() ? new Array(this) : new Num(this, next);
				current = next;
				next = nextRegion.end() + 1;
				if (next < end && orig.charAt(next) == ',') ++next;
				return nextRegion;
			}

			@Override
			public String toString() {
				StringBuilder s = new StringBuilder();
				s.append(orig, start - 1, current);
				s.append('→');
				s.append(orig, current, end + 1);
				return s.toString();
			}

			private static Array asArray(Region other) {
				if (other instanceof Array arr) return arr;
				else if (other instanceof Num num) return num.pretendArray();
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
				return orig.charAt(next) == '[';
			}
		}

		final class Num implements Region {
			private final int val, end;

			public Num(Array parent, int start) {
				int comma = parent.orig.indexOf(',', start);
				this.end = comma < 0 || comma > parent.end ? parent.end : comma;
				val = Integer.parseInt(parent.orig.substring(start, end));
			}

			@Override
			public int end() { return end; }

			@Override
			public BouncyOrder compareWith(Region other) {
				if (other instanceof Num num) {
					final Order order;
					if (val < num.val) order = Order.LESS_THAN;
					else if (val == num.val) order = Order.EQUAL;
					else order = Order.GREATER_THAN;
					return BouncyOrder.stable(order);
				}
				if (other instanceof Array arr) {
					return BouncyOrder.unstable(() -> pretendArray().compareWith(arr));
				}
				throw new UnsupportedOperationException("comparing " + other.getClass());
			}

			public Array pretendArray() {
				return new Array("[%d]".formatted(val), 1);
			}

			@Override
			public String toString() {
				return String.valueOf(val);
			}
		}
	}
}
