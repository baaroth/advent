package advent.y2022;

import advent.Debug;

import java.io.IOException;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.UnaryOperator;

public class Ex11 {

    private static final Debug DEBUG = Debug.ON;

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
            monkeyBuilder.withGiveDivisor(input.substring(21));
        } else if (input.startsWith("    If true: throw to monkey ")) {
            monkeyBuilder.withGiveTruthy(input.substring(29));
        } else if (input.startsWith("    If false: throw to monkey ")) {
            monkeyBuilder.withGiveFalsy(input.substring(30));
            // last instruction by contract
            monkeys.add(monkeyBuilder.build());
            monkeyBuilder = null;
        } else {
            throw new UnsupportedOperationException("unrecognized part: " + input);
        }
    }

    public static void main(String[] args) throws IOException, URISyntaxException {
        Ex11 ex = new Ex11();
        URI input = ex.getClass().getResource("ex11.input.txt").toURI();
        Files.readAllLines(Path.of(input)).forEach(ex::load);
        DEBUG.trace("initial: %s%n", ex.monkeys);
        for (int i = 0; i < 20; ++i) {
            for (Monkey m : ex.monkeys) {
                m.takeTurn(ex.monkeys);
            }
            DEBUG.trace("after turn %d: %s%n", i+1, ex.monkeys);
        }
        
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

    private static class Monkey {
        private final int id;
        private int inspected;
        private final List<Item> items;
        private final UnaryOperator<BigInteger> stressEvolution;
        private final GiveRule giveRule;

        public Monkey(int id, UnaryOperator<BigInteger> stressEvolution, GiveRule giveRule) {
            this.giveRule = giveRule;
            this.id = id;
            inspected = 0;
            items = new ArrayList<>();
            this.stressEvolution = stressEvolution;
        }

        public void receive(Item it) {
            items.add(it);
        }

        public void takeTurn(List<Monkey> all) {
            if (items.isEmpty()) return;
            for (Item it : items) {
                ++inspected;
                giveRule.apply(it.evolve(stressEvolution), all);
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
            private UnaryOperator<BigInteger> stressEvo;
            private BigInteger giveDivisor;
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
                if (stressEvo.equals("* old")) this.stressEvo = a -> a.pow(2);
                else {
                    final BigInteger operand = new BigInteger(stressEvo.substring(2));
                    if (stressEvo.startsWith("* ")) this.stressEvo = a -> a.multiply(operand);
                    else if (stressEvo.startsWith("+ ")) this.stressEvo = a -> a.add(operand);
                    else throw new UnsupportedOperationException("neither add nor multiply! " + stressEvo);
                }
                return this;
            }

            public Builder withGiveDivisor(String giveDivisor) {
                this.giveDivisor = new BigInteger(giveDivisor);
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

        private final BigInteger divisor;
        private final int truthyId, falsyId;
        public GiveRule(BigInteger divisor, int truthyId, int falsyId) {
            this.divisor = divisor;
            this.truthyId = truthyId;
            this.falsyId = falsyId;
        }

        public void apply(Item it, List<Monkey> all) {
            int monkeyId = it.divisible(divisor) ? truthyId : falsyId;
            all.get(monkeyId).receive(it);
        }

    }

    private record Item(BigInteger stress) {

        public static final BigInteger RELIEF = BigInteger.valueOf(3);

        static Item parse(String encoded) {
            return new Item(new BigInteger(encoded));
        }

        private boolean divisible(BigInteger divisor) {
            return stress.mod(divisor).equals(BigInteger.ZERO);
        }

        public Item evolve(UnaryOperator<BigInteger> stressEvolution) {
            return new Item(stressEvolution.apply(stress).divide(RELIEF));
        }

        @Override
        public String toString() {
            return String.valueOf(stress);
        }
    }
}
