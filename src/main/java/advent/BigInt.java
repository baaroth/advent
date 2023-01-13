package advent;

import java.math.BigInteger;

public class BigInt implements Comparable<BigInt> {
    public static final BigInt ZERO = new BigInt(BigInteger.ZERO);
    public static final BigInt ONE = new BigInt(BigInteger.ONE);

    private final BigInteger val;

    public BigInt(BigInteger val) {
        this.val = val;
    }
    public BigInt(long val) {
        this(BigInteger.valueOf(val));
    }
    public BigInt(String val) {
        this(new BigInteger(val));
    }

    @Override
    public int compareTo(BigInt o) {
        return val.compareTo(o.val);
    }

    public BigInt dividedBy(int divisor) {
        return new BigInt(val.divide(BigInteger.valueOf(divisor)));
    }

    public boolean divisibleBy(BigInt d) {
        return divisibleBy(d.val);
    }
    public boolean divisibleBy(BigInteger d) {
        return val.mod(d).equals(BigInteger.ZERO);
    }
    public boolean divisibleBy(long d) {
        return divisibleBy(BigInteger.valueOf(d));
    }
    public BigInt inc() {
        return new BigInt(val.add(BigInteger.ONE));
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof BigInt other) && val.equals(other.val);
    }

    public boolean lowerTo(BigInt other) {
        return compareTo(other) < 0;
    }

    @Override
    public String toString() {
        return val.toString();
    }

    public int mod(int div) {
        return val.mod(BigInteger.valueOf(div)).intValue();
    }

    public BigInt plus(BigInt amount) {
        return new BigInt(val.add(amount.val));
    }

    public BigInt plus(long amount) {
        return new BigInt(val.add(BigInteger.valueOf(amount)));
    }

    public BigInt minus(BigInt amount) {
        return new BigInt(val.subtract(amount.val));
    }

    public int intValue() {
        return val.intValue();
    }

    public BigInt times(int mult) {
        return new BigInt(val.multiply(BigInteger.valueOf(mult)));
    }

    public BigInt times(BigInt mult) {
        return new BigInt(val.multiply(mult.val));
    }
}
