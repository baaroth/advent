package advent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;

public class Walk {

    private final List<Coor> steps;

    public Walk(Coor first) {
        steps = new ArrayList<>();
        steps.add(first);
    }

    public Walk(Walk start, Coor next) {
        steps = new ArrayList<>(start.steps);
        save(next);
    }

    public boolean contains(Coor c) {
        return steps.contains(c);
    }

    public void forEachStep(Consumer<Coor> action) {
        steps.forEach(action);
    }

    public Coor last() {
        return steps.get(steps.size() - 1);
    }

    /**
     * Starting pos. is memorized (to prevent loop) but does not count.
     */
    public int length() {
        return steps.size() - 1;
    }

    public final void save(Coor next) {
        steps.add(next);
    }

    public Stream<Coor> stream() {
        return steps.stream();
    }

    @Override
    public String toString() {
        return steps.stream().map(Coor::toString).collect(joining("→"));
    }
}
