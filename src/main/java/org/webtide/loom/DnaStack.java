package org.webtide.loom;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Function;
import java.util.function.Predicate;

public class DnaStack
{
    public final Random RANDOM = new SecureRandom();
    public int maxDepth;

    public String next(String dna, Predicate<String> evolved, Function<List<String>, String> fittest)
    {
        if (dna.length() > maxDepth)
            maxDepth = dna.length();
        return switch ("ACGT".charAt(RANDOM.nextInt(4)))
            {
                case 'A' -> proteinA(dna, evolved, fittest);
                case 'C' -> proteinC(dna, evolved, fittest);
                case 'G' -> proteinG(dna, evolved, fittest);
                case 'T' -> proteinT(dna, evolved, fittest);
                default -> throw new IllegalStateException();
            };
    }

    public String proteinA(String dna, Predicate<String> evolved, Function<List<String>, String> fittest)
    {
        dna = dna + 'A';
        if (evolved.test(dna))
            return dna;

        int m = 1 + RANDOM.nextInt(7);
        List<String> mutations = new ArrayList<>(m);
        for (int i = 0; i < m; i++)
        {
            mutations.add(next(dna, evolved, fittest));
        }
        return fittest.apply(mutations);
    }

    public String proteinC(String dna, Predicate<String> evolved, Function<List<String>, String> fittest)
    {
        dna = dna + 'C';
        if (evolved.test(dna))
            return dna;

        String dnaA = proteinA(dna, evolved, fittest);
        String dnaC = proteinC(dna, evolved, fittest);
        String dnaG = proteinG(dna, evolved, fittest);
        String dnaT = proteinT(dna, evolved, fittest);
        return fittest.apply(List.of(dnaA, dnaC, dnaG, dnaT));
    }

    public String proteinG(String dna, Predicate<String> evolved, Function<List<String>, String> fittest)
    {
        dna = dna + 'G';
        if (evolved.test(dna))
            return dna;

        String dnaLeft = next(dna, evolved, fittest);
        String dnaRight = next(dna, evolved, fittest);
        int split = RANDOM.nextInt(Math.min(dnaLeft.length(), dnaRight.length()));

        return fittest.apply(List.of(
            dnaLeft.substring(0, split) + dnaRight.substring(split),
            dnaRight.substring(0, split) + dnaLeft.substring(split)));
    }

    public String proteinT(String dna, Predicate<String> evolved, Function<List<String>, String> fittest)
    {
        dna = dna + 'T';
        if (evolved.test(dna))
            return dna;

        String standard = next(dna, evolved, fittest);
        while (true)
        {
            String candidate = next(dna, evolved, fittest);
            if (candidate.equals(fittest.apply(List.of(standard, candidate))))
                return candidate;
        }
    }

    private static void unlimitedTrial()
    {
        DnaStack dna = new DnaStack();
        try
        {
            System.err.println(dna.next("", s -> false, l -> l.get(l.hashCode() % l.size())));
        }
        catch(Throwable t)
        {
            System.err.printf("%s: %s%n", Thread.currentThread(), t.toString());
        }
        finally
        {
            System.err.printf("%s: maxDepth=%d%n", Thread.currentThread(), dna.maxDepth);
        }
    }

    public static void main(String... args) throws Exception
    {
        unlimitedTrial();

        Thread.builder().virtual().task(DnaStack::unlimitedTrial).start().join();
    }

}
