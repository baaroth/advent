package advent.y2021;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Ex2 implements Consumer<String> {

	private static final Pattern DIRECTION = Pattern.compile("^(up|down|forward) (\\d+)$");

	int aim = 0;
	int h = 0;
	int d = 0;

	@Override
	public void accept(String raw) {
		Matcher m = DIRECTION.matcher(raw);
		if (m.matches()) {
			int x = Integer.parseInt(m.group(2), 10);
			switch (m.group(1)) {
				case "up" -> up(x);
				case "down" -> down(x);
				case "forward" -> forward(x);
				default -> throw new UnsupportedOperationException(m.group(1));
			}
		}
	}

	private void down(int x) {
		aim += x;
	}
	private void forward(int x) {
		h += x;
		d += aim * x;
	}
	private void up(int x) {
		aim -= x;
	}

	public static void main(String[] args) throws IOException, URISyntaxException {
		Ex2 consumer = new Ex2();
		URI input = consumer.getClass().getResource("ex2.input.txt").toURI();
		Files.readAllLines(Path.of(input)).forEach(consumer);
		System.out.printf("(%d,%d) » %d", consumer.h, consumer.d, consumer.h * consumer.d);
	}
}
