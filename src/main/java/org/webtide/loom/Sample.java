package org.webtide.loom;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

public class Sample
{
    private final LongAdder samples = new LongAdder();
    private final LongAdder total = new LongAdder();
    private final AtomicLong min = new AtomicLong(Long.MAX_VALUE);
    private final AtomicLong max = new AtomicLong(Long.MIN_VALUE);

    public void add(long sample)
    {
        samples.increment();
        total.add(sample);
        min.accumulateAndGet(sample, Math::min);
        max.accumulateAndGet(sample, Math::max);
    }

    public void reset()
    {
        samples.reset();
        total.reset();
        min.set(Long.MAX_VALUE);
        max.set(Long.MIN_VALUE);
    }

    private long average()
    {
        long s = samples.longValue();
        if (s == 0)
            return 0;
        return total.longValue() / s;
    }

    @Override
    public String toString()
    {
        return String.format("ave:%d from:%d min:%d max:%d", average(), samples.longValue(), min.get(), max.get());
    }
}
