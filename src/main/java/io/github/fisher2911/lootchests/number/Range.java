package io.github.fisher2911.lootchests.number;

import io.github.fisher2911.lootchests.util.RandomUtil;

public class Range {

    private int min;
    private int max;

    public Range(final int min, final int max) {
        this.min = min;
        this.max = max;
    }

    public int getRandom() {
        return RandomUtil.RANDOM.nextInt(this.min, this.max);
    }

    public int getMin() {
        return min;
    }

    public void setMin(final int min) {
        this.min = min;
    }

    public int getMax() {
        return max;
    }

    public void setMax(final int max) {
        this.max = max;
    }

    @Override
    public String toString() {
        return "Range{" +
                "min=" + min +
                ", max=" + max +
                '}';
    }
}
