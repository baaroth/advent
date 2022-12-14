package advent;

import java.util.stream.Stream;
@FunctionalInterface
public interface Trampoline<R> {

	R result();

	default Trampoline<R> bounce() { return this; }

	default boolean finished() { return true; }

	static <T> Trampoline<T> done(T result) {
		return () -> result;
	}

	static <T> Trampoline<T> more(Trampoline<Trampoline<T>> t) {
		return new Trampoline<>() {
			@Override
			public Trampoline<T> bounce() { return t.result(); }

			@Override
			public boolean finished() { return false; }

			@Override
			public T result() {
				Trampoline<T> seed = this;
				return Stream.iterate(seed, Trampoline::bounce)
						.filter(Trampoline::finished)
						.findFirst()
						.map(Trampoline::result)
						.orElseThrow();
			}
		};
	}
}
