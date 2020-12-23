package org.webtide.loom;

import java.util.concurrent.atomic.LongAdder;

public class MaxStackDepth
{
    private static final LongAdder result = new LongAdder();

    private static void trial(Sample sample)
    {
        DnaStack dna = new DnaStack();
        try
        {
            String d = dna.next("", s -> false, l -> l.get(Math.abs(l.get(0).hashCode()) % l.size()));
            result.add(d.hashCode());
            sample.add(dna.getMaxDepth());
        }
        catch(Throwable t)
        {
            int maxDepth = dna.getMaxDepth();
            sample.add(maxDepth);
            System.err.printf("%s: max=%d, %s%n", Thread.currentThread(), maxDepth, t.toString());
        }
    }

    public static void main(String... args) throws Exception
    {
        DnaStack.warmup();
        Sample kThreadSample = new Sample();
        Sample vThreadSample = new Sample();

        // Test the stacks depths without limit to see what can be achieved
        Thread thread0 = Thread.builder().task(() -> trial(kThreadSample)).start();
        Thread vthread0 = Thread.builder().virtual().task(() -> trial(vThreadSample)).start();
        Thread vthread1 = Thread.builder().virtual().task(() -> trial(vThreadSample)).start();
        Thread thread1 = Thread.builder().task(() -> trial(kThreadSample)).start();
        thread0.join();
        vthread0.join();
        vthread1.join();
        thread1.join();

        System.err.println("result: " + result.longValue());
        System.err.println("kthread maxDepth: " + kThreadSample);
        System.err.println("vthread maxDepth: " + vThreadSample);
    }
}
