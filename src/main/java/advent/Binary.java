package advent;

public record Binary(String str) {

	public int val() {
		return Integer.parseInt(str, 2);
	}

	@Override
	public String toString() {
		return str;
	}
}
