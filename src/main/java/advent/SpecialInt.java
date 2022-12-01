package advent;

import java.util.Comparator;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public sealed interface SpecialInt<SELF extends SpecialInt<SELF>> extends Comparable<SELF> {

	int val();

	SELF spawn(int val);

	@Override
	default int compareTo(SELF other) {
		return val() - other.val();
	}

	default SELF dec() {
		return spawn(val() - 1);
	}
	default SELF inc() {
		return spawn(val() + 1);
	}

	default UnaryOperator<SELF> unaryOpTo(SELF other) {
		return compareTo(other) < 0 ? SELF::inc : SELF::dec;
	}

	class Range<T extends SpecialInt<T>> {
		private final T max, min;

		public Range(T a, T b) {
			if (a.compareTo(b) < 0) {
				this.max = b;
				this.min = a;
			} else {
				this.max = a;
				this.min = b;
			}
		}

		public Stream<T> stream() {
			return StreamSupport.stream(new Spitr<>(min, max), false);
		}
	}

	class Spitr<T extends SpecialInt<T>> implements Spliterator<T> {
		private T current;
		private final T end;

		public Spitr(T begin, T end) {
			if (begin.compareTo(end) > 0) throw new IllegalArgumentException("begin after end");
			this.current = begin;
			this.end = end;
		}

		@Override
		public boolean tryAdvance(Consumer<? super T> action) {
			if (current.compareTo(end) <= 0) {
				action.accept(current);
				current = current.inc();
				return true;
			}
			return false;
		}

		@Override
		public Spliterator<T> trySplit() {
			int lo = current.val();
			int mid = ((lo + end.val()) >>> 1) & ~1; // force midpoint to be even

			if (lo < mid) {
				T begin = current;
				T split = current.spawn(mid);
				current = split; // reset this Spliterator's origin
				return new Spitr<>(begin, split);
			}
			else return null; // too small to split
		}

		@Override
		public long estimateSize() {
			long base = end.val();
			return base - current.val();
		}

		@Override
		public int characteristics() {
			return ORDERED | SORTED | SIZED | IMMUTABLE | SUBSIZED;
		}

		@Override
		public Comparator<? super T> getComparator() {
			return null; // natural order
		}
	}

	record X(int val) implements SpecialInt<X> {
		@Override
		public X spawn(int val) {
			return new X(val);
		}

		@Override
		public String toString() {
			return String.valueOf(val);
		}
	}

	record Y(int val) implements SpecialInt<Y> {
		@Override
		public Y spawn(int val) {
			return new Y(val);
		}

		@Override
		public String toString() {
			return String.valueOf(val);
		}
	}
}
