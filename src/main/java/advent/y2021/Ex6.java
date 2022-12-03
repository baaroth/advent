package advent.y2021;

import advent.Debug;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static java.nio.charset.Charset.defaultCharset;
import static java.util.stream.Collectors.joining;

public class Ex6 {
	private static final Debug DEBUG = Debug.ON;

	private final List<Jellyfish> fishes = new ArrayList<>();
	private final long[] populations = new long[9];

	public static void main(String[] args) throws IOException, URISyntaxException {
		URI input = Ex6.class.getResource("ex6.input.txt").toURI();
		Ex6 ex = new Ex6();
		try (BufferedReader reader = Files.newBufferedReader(Path.of(input), defaultCharset())) {
			Arrays.stream(reader.readLine().split(","))
					.map(Byte::parseByte)
					/*.map(Jellyfish::new)
					.forEach(ex.fishes::add);*/
					.forEach(idx -> ++ex.populations[idx]);
		}
		DEBUG.trace("initial: %s%n", ex.print());
		final int stop = 256;
		for (int cycle = 1; cycle <= stop; ++cycle) {
			ex.tick(cycle);
		}
		//int total = ex.fishes.size();
		long total = ex.populations[0];
		for (int i = 1; i < ex.populations.length; ++i) {
			total += ex.populations[i];
		}
		System.out.printf("%d total after %d cycles%n", total, stop);
	}

	private void tick(int cycle) {
		/*List<Jellyfish> newborns = fishes.stream().flatMap(Jellyfish::tick).toList();
		fishes.addAll(newborns);
		DEBUG.trace("cycle #%02d: %s%n", cycle, print());*/
		long hatching = populations[0];
		System.arraycopy(populations, 1, populations, 0, populations.length - 1);
		populations[Jellyfish.RESET] += hatching;
		populations[Jellyfish.INITIAL] = hatching;
		if (cycle % 50 == 0) {
			DEBUG.trace("cycle #%d: %s%n", cycle, print());
		}
	}

	private String print() {
		return Arrays.stream(populations)
				.mapToObj(String::valueOf)
		/*return fishes.stream()
				.map(Jellyfish::toString)*/
				.collect(joining(","));
	}

	static class Jellyfish {
		private static final byte INITIAL = 8;
		private static final byte RESET = 6;

		private byte spawnCountdown;
		public Jellyfish(byte spawnCountdown) {
			this.spawnCountdown = spawnCountdown;
		}

		public Stream<Jellyfish> tick() {
			if (spawnCountdown == 0) {
				spawnCountdown = RESET;
				return Stream.of(new Jellyfish(INITIAL));
			}
			--spawnCountdown;
			return Stream.empty();
		}

		@Override
		public String toString() {
			return String.valueOf(spawnCountdown);
		}
	}
}
