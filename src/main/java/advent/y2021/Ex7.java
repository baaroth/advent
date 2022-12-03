package advent.y2021;

import advent.Debug;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import static java.nio.charset.Charset.defaultCharset;
import static java.util.stream.Collectors.joining;

public class Ex7 {
	private static final Debug DEBUG = Debug.OFF;

	private final Horizontals horizontals;
	Ex7(Horizontals horizontals) {
		this.horizontals = horizontals;
	}

	public static void main(String[] args) throws IOException, URISyntaxException {
		URI input = Ex7.class.getResource("ex7.input.txt").toURI();
		Horizontals horizontals = new Horizontals();
		try (BufferedReader reader = Files.newBufferedReader(Path.of(input), defaultCharset())) {
			Arrays.stream(reader.readLine().split(","))
					.map(Integer::parseInt)
					.forEach(horizontals::register);
		}
		DEBUG.trace("%s%n", horizontals);
		Ex7 ex = new Ex7(horizontals);
		MinFuel minFuel = ex.findMinFuel2();
		System.out.printf("min fuel %s", minFuel);
	}


	private record Offset(int index, int sumBefore, int nbBefore) {
		Offset next(int nbCurrent) {
			int beforeNext = nbBefore + nbCurrent;
			return new Offset(index + 1, sumBefore + beforeNext, beforeNext);
		}
	}

	private record MinFuel(int qty, int atIndex) {}

	private record Move(int totalFuel, Offset nextOffset) {}

	MinFuel findMinFuel() {
		Move first = horizontals.evalMove(horizontals.firstOffset());
		int minQty = first.totalFuel();
		int minIdx = horizontals.minIdx;
		final int lastIdx = horizontals.maxIdx;
		Move current = first;
		DEBUG.trace("findMin> @%d: %d, next: %s%n", minIdx, current.totalFuel(), current.nextOffset());
		while (current.nextOffset().index() <= lastIdx) {
			Offset toEval = current.nextOffset();
			current = horizontals.evalMove(toEval);
			DEBUG.trace("findMin> @%d: %d, next: %s%n", toEval.index(), current.totalFuel(), current.nextOffset());
			if (current.totalFuel() < minQty) {
				minQty = current.totalFuel();
				minIdx = toEval.index();
			}
		}
		return new MinFuel(minQty, minIdx);
	}

	MinFuel findMinFuel2() {
		int minIdx = horizontals.minIdx;
		int minQty = horizontals.evalPart2(minIdx);
		final int lastIdx = horizontals.maxIdx;
		for (int i = minIdx + 1; i <= lastIdx; ++i) {
			int fuel = horizontals.evalPart2(i);
			if (fuel < minQty) {
				minIdx = i;
				minQty = fuel;
			}
		}
		return new MinFuel(minQty, minIdx);
	}

	private static class Horizontals {
		private int[] values = new int[1000];
		private int maxIdx = 0;
		private int minIdx = -1;

		@Override
		public String toString() {
			return Arrays.stream(values)
					.skip(minIdx)
					.limit(maxIdx + 1)
					.mapToObj(String::valueOf)
					.collect(joining(",", minIdx + "[", "]"+ maxIdx));
		}

		Offset firstOffset() {
			if (minIdx < 0) throw new IllegalStateException("empty instance");
			return new Offset(minIdx, 0, 0);
		}

		/**
		 * cannot optimise via {@link Offset#sumBefore()} anymore
		 */
		int evalPart2(int index) {
			int total = 0;
			for (int i = minIdx; i < index; ++i) {
				total += computeFuel(values[i], index - i);
			}
			for (int i = index + 1; i <= maxIdx; ++i) {
				total += computeFuel(values[i], i - index);
			}
			return total;
		}

		private int computeFuel(int crabs, int distance) {
			if (crabs == 0) return 0;

			int fuel = (distance % 2 == 0)
					? (distance >> 1) * (distance + 1)
					: distance * ((distance + 1) >> 1);
			return crabs * fuel;
		}

		Move evalMove(Offset offset) {
			int total = offset.sumBefore();
			for (int i = offset.index() + 1; i <= maxIdx; ++i) {
				int crabs = values[i];
				if (crabs > 0) {
					int distance = i - offset.index();
					total += crabs * distance;
					DEBUG.trace("evalMove %d> @%d: %d crabs moving %d (Σ=%d)%n", offset.index, i, crabs, distance, total);
				}
			}
			return new Move(total, offset.next(values[offset.index()]));
		}

		void register(int index) {
			if (index >= values.length) grow(index + 1);
			if (index > maxIdx) maxIdx = index;
			if (minIdx < 0 || index < minIdx) minIdx = index;
			++values[index];
		}

		private void grow(int minCapacity) {
			int oldCapacity = values.length;
			int newCapacity = Math.max(oldCapacity << 1, minCapacity);
			int[] bigger = new int[newCapacity];
			System.arraycopy(values, 0, bigger, 0, values.length);
			this.values = bigger;
		}
	}
}
