package advent.y2022;

import advent.Debug;

import java.io.IOException;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.UnaryOperator;

public class Ex11 {

    private static final Debug DEBUG = Debug.ON;

    private final Set<Integer> candidateDivisors = new TreeSet<>();
    private final List<Monkey> monkeys = new ArrayList<>();
    private Monkey.Builder monkeyBuilder;

    private void load(String input) {
        if (input.isEmpty()) return;

        if (input.startsWith("Monkey ")) {
            int id = Integer.parseInt(input.substring(7, input.length() - 1));
            if (id != monkeys.size()) throw new IllegalStateException("declaring monkey #%d out of order".formatted(id));
            monkeyBuilder = new Monkey.Builder(id);
        } else if (input.startsWith("  Starting items: ")) {
            monkeyBuilder.withItems(input.substring(18));
        } else if (input.startsWith("  Operation: new = old ")) {
            monkeyBuilder.withStressEvo(input.substring(23));
        } else if (input.startsWith("  Test: divisible by ")) {
            int div = Integer.parseInt(input.substring(21));
            monkeyBuilder.withGiveDivisor(div);
            candidateDivisors.add(div);
        } else if (input.startsWith("    If true: throw to monkey ")) {
            monkeyBuilder.withGiveTruthy(input.substring(29));
        } else if (input.startsWith("    If false: throw to monkey ")) {
            Monkey m = monkeyBuilder.withGiveFalsy(input.substring(30))
                    // last instruction by contract
                    .build();
            monkeys.add(m);
            monkeyBuilder = null;
        } else {
            throw new UnsupportedOperationException("unrecognized part: " + input);
        }
    }

    public static void main(String[] args) throws IOException, URISyntaxException {
        Ex11 ex = new Ex11();
        URI input = ex.getClass().getResource("ex11.example.txt").toURI();
        Files.readAllLines(Path.of(input)).forEach(ex::load);
        ex.categorizeAllItems();
        DEBUG.trace("initial: %s%n", ex.monkeys);
        for (int i = 0; i < 1_000; ++i) {
            for (Monkey m : ex.monkeys) {
                m.takeTurn(ex.monkeys);
            }
            if (i == 19) DEBUG.trace("20:%s%n", ex.monkeys);
            if (i % 100 == 99) DEBUG.trace("."); // console life pulse
            if (i % 1000 == 999) DEBUG.trace("%d:%s%n", i+1, ex.monkeys);
        }
        DEBUG.trace("---%n");
        int max1 = -1;
        int max2 = -1;
        for (Monkey m : ex.monkeys) {
            DEBUG.trace("[%d,%d] Monkey %d inspected items %d times.%n", max1, max2, m.id, m.inspected);
            if (max1 < m.inspected) {
                if (max2 < max1) max2 = max1;
                max1 = m.inspected;
            } else if (max2 < m.inspected) {
                max2 = m.inspected;
            }
        }
        System.out.printf("%d×%d=%d%n", max1, max2, max1 * max2);
    }

    private void categorizeAllItems() {
        DEBUG.trace("cadidate currentDivisors: %s%n", candidateDivisors);
        monkeys.forEach(m -> m.categorizeEachItem(candidateDivisors));
    }

    private static boolean isDivisible(BigInteger value, int candidateDivisor) {
        return value.mod(BigInteger.valueOf(candidateDivisor)).equals(BigInteger.ZERO);
    }

    private static class Monkey {
        private final int id;
        private int inspected;
        private final List<Item> items;
        private final UnaryOperator<Item> stressEvolution;
        private final GiveRule giveRule;

        public Monkey(int id, UnaryOperator<Item> stressEvolution, GiveRule giveRule) {
            this.giveRule = giveRule;
            this.id = id;
            inspected = 0;
            items = new ArrayList<>();
            this.stressEvolution = stressEvolution;
        }

        public void categorizeEachItem(Set<Integer> candidateDivisors) {
            List<Item> categorized = items.stream()
                    .map(it -> it.categorize(candidateDivisors))
                    .toList();
            items.clear();
            items.addAll(categorized);
        }

        public void receive(Item it) {
            items.add(it);
        }

        public void takeTurn(List<Monkey> all) {
            if (items.isEmpty()) return;
            for (Item it : items) {
                ++inspected;
                giveRule.apply(stressEvolution.apply(it), all);
            }
            items.clear();
        }

        @Override
        public String toString() {
            return String.format("%n  m%d(%d)%s", id, inspected, items);
        }

        private static class Builder {
            private final int id;
            private String[] items;
            private UnaryOperator<Item> stressEvo;
            private int giveDivisor;
            private int giveTruthy, giveFalsy;

            public Builder(int id) {
                this.id = id;
            }

            public Monkey build() {
                Monkey m = new Monkey(id,
                        stressEvo,
                        new GiveRule(giveDivisor, giveTruthy, giveFalsy)
                );
                Arrays.stream(items).map(Item::parse).forEach(m::receive);
                return m;
            }

            public Builder withItems(String items) {
                this.items = items.split(", ");
                return this;
            }

            public Builder withStressEvo(String stressEvo) {
                if (stressEvo.equals("* old")) this.stressEvo = new Square();
                else {
                    final int operand = Integer.parseInt(stressEvo.substring(2));
                    if (stressEvo.startsWith("* ")) this.stressEvo = new Multiply(operand);
                    else if (stressEvo.startsWith("+ ")) this.stressEvo = new Add(operand);
                    else throw new UnsupportedOperationException("neither add nor multiply! " + stressEvo);
                }
                return this;
            }

            public Builder withGiveDivisor(int giveDivisor) {
                this.giveDivisor = giveDivisor;
                return this;
            }

            public Builder withGiveTruthy(String giveTruthy) {
                this.giveTruthy = Integer.parseInt(giveTruthy);
                return this;
            }

            public Builder withGiveFalsy(String giveFalsy) {
                this.giveFalsy = Integer.parseInt(giveFalsy);
                return this;
            }
        }
    }

    private static class GiveRule {

        private final int divisor;
        private final int truthyId, falsyId;
        public GiveRule(int divisor, int truthyId, int falsyId) {
            this.divisor = divisor;
            this.truthyId = truthyId;
            this.falsyId = falsyId;
        }

        public void apply(Item it, List<Monkey> all) {
            int monkeyId = it.divisible(divisor) ? truthyId : falsyId;
            all.get(monkeyId).receive(it);
        }

    }

    private record Item(BigInteger initial,
                        BigInteger current,
                        Map<Integer, BigInteger> adds,
                        Set<Integer> allDivisors,
                        Set<Integer> currentDivisors,
                        Set<Integer> nonDivisors) {

        static Item parse(String encoded) {
            BigInteger val = new BigInteger(encoded);
            return new Item(val, val, Map.of(), Set.of(), Set.of(), Set.of());
        }

        private boolean divisible(int divisor) {
            return currentDivisors.contains(divisor);
        }

        public boolean externalDivisor(int d) {
            return !isDivisible(initial, d);
        }

        public Item categorize(Set<Integer> candidateDivisors) {
            Set<Integer> mutableDivisors = new HashSet<>();
            Set<Integer> mutableNonDivisors = new HashSet<>();
            for (int div : candidateDivisors) {
                (isDivisible(initial, div) ? mutableDivisors : mutableNonDivisors).add(div);
            }
            Set<Integer> divisors = Set.copyOf(mutableDivisors);
            return new Item(initial, initial, adds, divisors, divisors, Set.copyOf(mutableNonDivisors));
        }

        @Override
        public String toString() {
            return "%d{%d+,%d/,%d¬/}".formatted(initial, adds.size(), currentDivisors.size(), nonDivisors.size());
        }
    }

    private record Add(int operand) implements UnaryOperator<Item> {
        @Override
        public Item apply(Item it) {
            final BigInteger newVal = it.current().add(BigInteger.valueOf(operand));
            final BigInteger nb = it.adds().getOrDefault(operand, BigInteger.ZERO).add(BigInteger.ONE);
            Map<Integer, BigInteger> mutableAdds = new HashMap<>(it.adds());
            mutableAdds.put(operand, nb);

            BigInteger summedAdds = mutableAdds.entrySet().stream()
                    .map(e -> BigInteger.valueOf(e.getKey()).multiply(e.getValue()))
                    .reduce(BigInteger.ZERO, BigInteger::add);
            BigInteger multipliedDivs = it.allDivisors().stream()
                    .filter(it::externalDivisor)
                    .map(BigInteger::valueOf)
                    .reduce(BigInteger.ONE, BigInteger::multiply);
            //BigInteger result = it.initial.multiply(multipliedDivs).add(summedAdds);
            BigInteger result = it.initial.add(summedAdds);

            Set<Integer> mutableDivisors = new HashSet<>(it.currentDivisors());
            Set<Integer> mutableAllDivisors = new HashSet<>(it.allDivisors());
            Set<Integer> mutableNonDivisors = new HashSet<>(it.nonDivisors());
            boolean archivedNew = false;

            Iterator<Integer> nonDivisorIte = mutableNonDivisors.iterator();
            while (nonDivisorIte.hasNext()) {
                int prevNonDivisor = nonDivisorIte.next();
                if (isDivisible(result, prevNonDivisor)) {
                    checkDivisible(newVal, prevNonDivisor, it, result);
                    mutableDivisors.add(prevNonDivisor);
                    archivedNew |= mutableAllDivisors.add(prevNonDivisor);
                    nonDivisorIte.remove();
                } else {
                    checkNonDivisible(newVal, prevNonDivisor, it, result);
                }
            }

            Iterator<Integer> divisorIte = mutableDivisors.iterator();
            while (divisorIte.hasNext()) {
                int prevDivisor = divisorIte.next();
                if (it.currentDivisors().contains(prevDivisor) && !isDivisible(summedAdds, prevDivisor)) {
                    mutableNonDivisors.add(prevDivisor);
                    divisorIte.remove();
                    checkNonDivisible(newVal, prevDivisor, it, summedAdds);
                } else {
                    checkDivisible(newVal, prevDivisor, it, summedAdds);
                }
            }

            return new Item(it.initial(),
                    newVal,
                    Map.copyOf(mutableAdds),
                    archivedNew ? Set.copyOf(mutableAllDivisors) : it.allDivisors(),
                    Set.copyOf(mutableDivisors),
                    Set.copyOf(mutableNonDivisors)
            );
        }

        void checkDivisible(BigInteger full, int divisor, Item it, BigInteger computed) {
            if (!isDivisible(full, divisor))
                throw new IllegalStateException("%s + %d: %d ¬div %d but does %d"
                        .formatted(it, operand, divisor, full, computed));
        }
        void checkNonDivisible(BigInteger full, int divisor, Item it, BigInteger computed) {
            if (isDivisible(full, divisor))
                throw new IllegalStateException("%s + %d: %d div %d but not %d"
                        .formatted(it, operand, divisor, full, computed));
        }
    }

    private record Multiply(int operand) implements UnaryOperator<Item> {
        @Override
        public Item apply(Item it) {
            BigInteger newVal = it.current().multiply(BigInteger.valueOf(operand));
            Set<Integer> mutableDivisors = new HashSet<>(it.currentDivisors());
            Set<Integer> mutableAllDivisors = new HashSet<>(it.allDivisors());
            Set<Integer> mutableNonDivisors = new HashSet<>(it.nonDivisors());
            boolean archivedNew = false;

            Iterator<Integer> nonDivisorIte = mutableNonDivisors.iterator();
            while (nonDivisorIte.hasNext()) {
                int prevNonDivisor = nonDivisorIte.next();
                if (operand % prevNonDivisor == 0) {
                    if (!isDivisible(newVal, prevNonDivisor))
                        throw new IllegalStateException("%s × %d: %d ¬div %d but does %d"
                                .formatted(it, operand, prevNonDivisor, newVal, operand));
                    mutableDivisors.add(prevNonDivisor);
                    archivedNew |= mutableAllDivisors.add(prevNonDivisor);
                    nonDivisorIte.remove();
                }
            }
            Map<Integer, BigInteger> mutableAdds = new HashMap<>();
            it.adds.forEach( (k, v) -> mutableAdds.put(k, v.multiply(BigInteger.valueOf(operand))) );
            return new Item(it.initial(),
                    newVal,
                    Map.copyOf(mutableAdds),
                    archivedNew ? Set.copyOf(mutableAllDivisors) : it.allDivisors(),
                    Set.copyOf(mutableDivisors),
                    Set.copyOf(mutableNonDivisors)
            );
        }
    }

    private static class Square implements UnaryOperator<Item> {
        @Override
        public Item apply(Item it) {
            BigInteger summedAdds = it.adds.entrySet().stream()
                    .map(e -> BigInteger.valueOf(e.getKey()).multiply(e.getValue()))
                    .reduce(BigInteger.ZERO, BigInteger::add);
            Map<Integer, BigInteger> mutableAdds = new HashMap<>();
            it.adds.forEach( (k, v) -> mutableAdds.put(k, v.multiply(summedAdds)) );
            return new Item(it.initial(),
                    it.current.pow(2),
                    Map.copyOf(mutableAdds),
                    it.allDivisors(),
                    it.currentDivisors(),
                    it.nonDivisors()
            );
        }
    }
}
