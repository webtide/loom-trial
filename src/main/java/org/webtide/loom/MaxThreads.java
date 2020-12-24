package org.webtide.loom;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;

public class MaxThreads
{
    private static final LongAdder result = new LongAdder();

    public static void findMax(int depth, boolean virtual) throws Exception
    {
        DnaStack.warmup();

        DnaStack dna = new DnaStack();
        CountDownLatch hold = new CountDownLatch(1);
        List<Thread> threads = new ArrayList<>();
        while (threads.size() < 1_000_000)
        {
            long start = System.nanoTime();
            CountDownLatch latch = new CountDownLatch(1);
            Thread.Builder builder = Thread.builder();
            if (virtual)
                builder = builder.virtual();
            Thread t = builder.task(() ->
            {
                String d = dna.next("", s ->
                    {
                        if (s.length() < depth)
                            return false;
                        latch.countDown();
                        try
                        {
                            hold.await();
                        }
                        catch (InterruptedException e)
                        {
                            e.printStackTrace();
                        }
                        return true;
                    },
                    l -> l.get(0));
                result.add(d.hashCode());
            }).start();
            threads.add(t);
            System.err.printf("%,d: memory=%,d : %s%n",
                threads.size(),
                Runtime.getRuntime().totalMemory(),
                t);

            latch.await();
            long wait = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
            if (wait > 1_000)
            {
                System.err.printf("SLOW %dms%n", wait);
                if (wait >= 5_000)
                {
                    System.err.println("TOO SLOW!!!");
                    break;
                }
            }
        }
        hold.countDown();
        for (Thread thread : threads)
        {
            System.err.printf("joining %s%n", thread);
            thread.join();
        }
        System.err.println(result.longValue());
    }
}
