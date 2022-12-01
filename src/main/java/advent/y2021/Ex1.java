package advent.y2021;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

public class Ex1 implements Consumer<String> {

	private final int[] window = new int[4];
	private boolean ready = false;
	private int curr = 0;
	private int incs = 0;

	@Override
	public void accept(String valStr) {
		int val = Integer.parseInt(valStr, 10);
		window[curr] = val;
		curr = (curr + 1) % 4;
		if (!ready && curr == 0) ready = true;
		if (ready) {
			int a = sum(curr);
			int b = sum(curr + 1);
//			StringBuilder s = new StringBuilder().append(a).append(',').append(b);
			if (b > a) {
				++incs;
//				s.append('↑');
			}
			//System.out.println(s);
		}
	}

	private int sum(int start) {
		int sum = 0;
		for (int i = 0; i < 3; ++i) {
			sum += window[(start + i) % 4];
		}
		return sum;
	}

	public static void main(String[] args) throws IOException, URISyntaxException {
		Ex1 consumer = new Ex1();
		URI input = consumer.getClass().getResource("ex1.input.txt").toURI();
		Files.readAllLines(Path.of(input)).forEach(consumer);
		System.out.println(consumer.incs);
	}
}
