package nwen303.shared;

import java.math.BigInteger;

public class Range {
    private BigInteger start;
    private BigInteger end;

    public Range(BigInteger start, BigInteger end) {
        this.start = start;
        this.end = end;
    }

    @Override
    public String toString() {
        return "Range { start=" + start + ", end=" + end + " }";
    }

    public BigInteger getStart() {
        synchronized (this) {
            return start;
        }
    }

    public BigInteger getEnd() {
        return end;
    }

    public Range split(long chunkSize) {
        synchronized (this) {
            start = start.add(BigInteger.valueOf(chunkSize));
        }
        if(start.compareTo(end) > -1) {
            return null;
        }
        return new Range(start.subtract(BigInteger.valueOf(chunkSize)), start);
    }
}
