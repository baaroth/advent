package advent.y2021;

import advent.Binary;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.PrimitiveIterator;

public class Ex3 {

	private final List<String> lines;
	private Ex3(List<String> lines) {
		this.lines = lines;
	}

	public static void main(String[] args) throws IOException, URISyntaxException {
		URI input = Ex3.class.getResource("ex3.input.txt").toURI();
		Ex3 ex = new Ex3(Files.readAllLines(Path.of(input)));
		System.out.println(ex.powerConsumption());
		System.out.println(ex.lifeSupportRating());
	}

	private int powerConsumption() {
		if (lines.isEmpty()) throw new IllegalStateException("computation without any input");

		Stats stats = Stats.of(lines);
		Binary gamma = gamma(stats);
		Binary epsilon = epsilon(stats);
		System.out.printf("γ=%s ε=%s%n", gamma, epsilon);

		return gamma.val() * epsilon.val();
	}

	private int lifeSupportRating() {
		Binary o2 = o2GeneratorRating();
		Binary co2 = co2ScrubberRating();
		System.out.printf("O2=%s CO2=%s%n", o2, co2);
		return o2.val() * co2.val();
	}

	private Binary epsilon(Stats input) {
		StringBuilder b = new StringBuilder();
		for (int i = 0; i <  input.length(); ++i) {
			b.append(input.leastCommon(i));
		}
		return new Binary(b.toString());
	}

	private Binary gamma(Stats input) {
		StringBuilder b = new StringBuilder();
		for (int i = 0; i <  input.length(); ++i) {
			b.append(input.mostCommon(i));
		}
		return new Binary(b.toString());
	}

	private Binary co2ScrubberRating() {
		List<String> lines = this.lines;
		int i = 0;
		while (lines.size() > 1) {
			lines = filter(lines, i++, Stats::leastCommon);
		}
		return new Binary(lines.get(0));
	}
	private Binary o2GeneratorRating() {
		List<String> lines = this.lines;
		int i = 0;
		while (lines.size() > 1) {
			lines = filter(lines, i++, Stats::mostCommon);
		}
		return new Binary(lines.get(0));
	}

	private List<String> filter(List<String> in, int nextBitCriteriaRank, BitCriteria bitCriteria) {
		char criteria = bitCriteria.of(Stats.of(in), nextBitCriteriaRank);
		return in.stream()
				.filter(s -> s.charAt(nextBitCriteriaRank) == criteria)
				.toList();
	}

	@FunctionalInterface
	private interface BitCriteria {
		char of(Stats s, int rank);
	}

	private static class Stats {

		private final int[] counts;
		private final int total;
		private Stats(int length, int total) {
			this.counts = new int[length];
			this.total = total;
		}
		static Stats of(List<String> inputs) {
			if (inputs.isEmpty()) return new Stats(0, 0);

			Stats me = new Stats(inputs.get(0).length(), inputs.size());
			inputs.forEach(me::ingest);
			return me;
		}

		private void ingest(String raw) {
			int i = 0;
			PrimitiveIterator.OfInt ite = raw.chars().iterator();
			while (ite.hasNext()) {
				if (ite.next() == '1') ++counts[i];
				++i;
			}
		}

		char leastCommon(int rank) {
			int doubleCount = counts[rank] << 1;
			return doubleCount >= total ? '0' : '1';
		}
		char mostCommon(int rank) {
			int doubleCount = counts[rank] << 1;
			return doubleCount < total ? '0' : '1';
		}
		int length() {
			return counts.length;
		}
	}

}
