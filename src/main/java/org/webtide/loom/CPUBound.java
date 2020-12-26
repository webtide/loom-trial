package org.webtide.loom;

import java.util.concurrent.CountDownLatch;

import org.openjdk.jmh.infra.Blackhole;

public class CPUBound
{
    private static Runnable newTask(CountDownLatch latch)
    {
        return () ->
        {
            Blackhole.consumeCPU(1_000_000_000);
            latch.countDown();
        };
    }

    public static void main(String... arg) throws Exception
    {
        // warmup
        int warmup = 10;
        CountDownLatch warm = new CountDownLatch(2 * warmup);
        for (int i = 0; i < warmup; i++)
        {
            Thread.builder().task(newTask(warm)).start();
            Thread.builder().virtual().task(newTask(warm)).start();
        }
        warm.await();

        int tasks = Runtime.getRuntime().availableProcessors() + 1;

        // kernel threads
        long start = System.nanoTime();
        CountDownLatch latch = new CountDownLatch(tasks);
        for (int i = 0; i < tasks; i++)
            Thread.builder().task(newTask(latch)).start();
        latch.await();
        System.err.printf("Kernel threads : %,dns%n", System.nanoTime() - start);

        // virtual threads
        start = System.nanoTime();
        latch = new CountDownLatch(tasks);
        for (int i = 0; i < tasks; i++)
            Thread.builder().virtual().task(newTask(latch)).start();
        latch.await();
        System.err.printf("Virtual threads: %,dns%n", System.nanoTime() - start);

        // virtual threads
        start = System.nanoTime();
        latch = new CountDownLatch(tasks);
        for (int i = 0; i < tasks; i++)
            Thread.builder().virtual().task(newTask(latch)).start();
        latch.await();
        System.err.printf("Virtual threads: %,dns%n", System.nanoTime() - start);

        // kernel threads
        start = System.nanoTime();
        latch = new CountDownLatch(tasks);
        for (int i = 0; i < tasks; i++)
            Thread.builder().task(newTask(latch)).start();
        latch.await();
        System.err.printf("Kernel threads : %,dns%n", System.nanoTime() - start);
    }
}
