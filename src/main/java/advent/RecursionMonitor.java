package advent;

public class RecursionMonitor {
	private int current = 0;
	private int max = -1;

	public void in() {
		++current;
		if (current > max) max = current;
	}

	public void outVoid() {
		--current;
	}
	public <T> T out(T result) {
		outVoid();
		return result;
	}

	public int max() {
		return max;
	}
}
