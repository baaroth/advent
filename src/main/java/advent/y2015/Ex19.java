package advent.y2015;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static java.nio.charset.Charset.defaultCharset;

public class Ex19 {
	private static final Pattern REGEX_TRANS = Pattern.compile("^(\\w+) => (\\w+)$");
	private final List<Transfo> transfos = new ArrayList<>();

	public static void main(String[] args) throws IOException, URISyntaxException {
		URI input = Ex19.class.getResource("ex19.input.txt").toURI();
		Ex19 ex = new Ex19();
		try (BufferedReader reader = Files.newBufferedReader(Path.of(input), defaultCharset())) {
			String line = reader.readLine();
			while (!line.isEmpty()) {
				ex.register(line);
				line = reader.readLine();
			}
			String baseMolecule = reader.readLine();
			Set<String> molecules = new HashSet<>();
			for (int i = 0; i < baseMolecule.length(); ++i) {
				final int idx = i; // ̂pour la Closure
				ex.transfos.stream()
						.flatMap(transfo -> transfo.apply(baseMolecule, idx))
						.forEach(molecules::add);
			}
			//System.out.println(molecules);
			System.out.printf("%s distinct after transfo", molecules.size());
		}
	}

	private void register(String raw) {
		Matcher m = REGEX_TRANS.matcher(raw);
		if (!m.matches()) throw new IllegalArgumentException("bad line " + raw);
		transfos.add(new Transfo(m.group(1), m.group(2)));
	}

	private record Transfo(String from, String to){

		public Stream<String> apply(String in, int atIndex) {
			if (in.startsWith(from, atIndex)) {
				return Stream.of(in.substring(0, atIndex) + to + in.substring(atIndex + from.length()));
			}
			return Stream.empty();
		}

		@Override
		public String toString() {
			return "%s => %s".formatted(from, to);
		}
	}

}
