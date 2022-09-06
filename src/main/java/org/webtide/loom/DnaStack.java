package org.webtide.loom;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.Function;
import java.util.function.Predicate;

public class DnaStack
{
    private final Random RANDOM = new Random();

    private static final LongAdder result = new LongAdder();

    static void warmup(Sample sample, int maxDepth)
    {
        DnaStack dna = new DnaStack();
        try
        {
            String d = dna.next("", s -> s.length() >= maxDepth, l -> l.get(Math.abs(l.get(0).hashCode()) % l.size()));
            System.err.printf("%s: %s%n", Thread.currentThread(), d);
            result.add(d.hashCode());
            sample.add(dna.getMaxDepth());
        }
        catch(Throwable t)
        {
            System.err.printf("%s: max=%d, %s%n", Thread.currentThread(), maxDepth, t.toString());
        }
    }

    public static void warmup() throws Exception
    {
        Sample kThreadSample = new Sample();
        Sample vThreadSample = new Sample();

        // Warm up the JIT with real examples so that it actually expects real recursion
        for (int i = 1; i < 1000; i++)
        {
            final int warmup = i;
            Thread.ofPlatform().start(() -> DnaStack.warmup(kThreadSample, warmup)).join();
            Thread.ofVirtual().start(() -> DnaStack.warmup(vThreadSample, warmup)).join();

            System.err.println("WARMUP result: " + result.longValue());
            System.err.println("WARMUP kthread maxDepth: " + kThreadSample);
            System.err.println("WARMUP vthread maxDepth: " + vThreadSample);
            kThreadSample.reset();
            vThreadSample.reset();
        }
    }

    public int getMaxDepth()
    {
        return maxDepth;
    }

    private int maxDepth;

    public String next(String dna, Predicate<String> evolved, Function<List<String>, String> fittest)
    {
        if (dna.length() > maxDepth)
            maxDepth = dna.length();
        return switch ("ACGT".charAt(RANDOM.nextInt(4)))
            {
                case 'A' -> proteinA(dna + 'A', evolved, fittest);
                case 'C' -> proteinC(dna + 'C', evolved, fittest);
                case 'G' -> proteinG(dna + 'G', evolved, fittest);
                case 'T' -> proteinT(dna + 'T', evolved, fittest);
                default -> throw new IllegalStateException();
            };
    }

    public String proteinA(String dna, Predicate<String> evolved, Function<List<String>, String> fittest)
    {
        if (evolved.test(dna))
            return dna;

        if (dna.hashCode() % 256 != 0)
            return next(dna, evolved, fittest);

        int m = 1 + RANDOM.nextInt(3);
        List<String> mutations = new ArrayList<>(m);
        for (int i = 0; i < m; i++)
        {
            mutations.add(next(dna, evolved, fittest));
        }
        return fittest.apply(mutations);
    }

    public String proteinC(String dna, Predicate<String> evolved, Function<List<String>, String> fittest)
    {
        if (evolved.test(dna))
            return dna;

        return next(dna, evolved, fittest);
    }

    public String proteinG(String dna, Predicate<String> evolved, Function<List<String>, String> fittest)
    {
        if (evolved.test(dna))
            return dna;

        if (dna.hashCode() % 256 != 0)
            return next(dna, evolved, fittest);

        String dnaLeft = next(dna, evolved, fittest);
        String dnaRight = next(dna, evolved, fittest);
        int split = RANDOM.nextInt(Math.min(dnaLeft.length(), dnaRight.length()));

        return fittest.apply(List.of(
            dnaLeft.substring(0, split) + dnaRight.substring(split),
            dnaRight.substring(0, split) + dnaLeft.substring(split)));
    }

    public String proteinT(String dna, Predicate<String> evolved, Function<List<String>, String> fittest)
    {
        if (evolved.test(dna))
            return dna;

        if (dna.hashCode() % 128 != 0)
            return next(dna, evolved, fittest);

        String standard = next(dna, evolved, fittest);
        for (int i = 2; i-- > 0;)
        {
            String candidate = next(dna, evolved, fittest);
            if (candidate.equals(fittest.apply(List.of(standard, candidate))))
                return candidate;
        }
        return standard;
    }

    public static void main(String... arg ) throws Exception
    {
        warmup();
    }
}
