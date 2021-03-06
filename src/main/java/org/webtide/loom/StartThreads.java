package org.webtide.loom;

import java.util.function.Consumer;

public class StartThreads
{
    public static void main(String... args) throws Exception
    {
        Sample kStart = new Sample();
        Consumer<Long> kSample = start -> kStart.add(System.nanoTime() - start);
        Sample vStart = new Sample();
        Consumer<Long> vSample = start -> vStart.add(System.nanoTime() - start);

        // warmup
        for (int i = 0; i < 1000; i++)
        {
            long start = System.nanoTime();
            Thread.builder().task(() -> kSample.accept(start)).start().join();
        }
        for (int i = 0; i < 1000; i++)
        {
            long start = System.nanoTime();
            Thread.builder().virtual().task(() -> vSample.accept(start)).start().join();
        }

        kStart.reset();
        vStart.reset();

        // measure
        for (int i = 0; i < 1000; i++)
        {
            long start = System.nanoTime();
            Thread.builder().task(() -> kSample.accept(start)).start().join();
        }
        for (int i = 0; i < 1000; i++)
        {
            long start = System.nanoTime();
            Thread.builder().virtual().task(() -> vSample.accept(start)).start().join();
        }

        System.err.printf("kStart(ns) %s%n", kStart);
        System.err.printf("vStart(ns) %s%n", vStart);
    }
}
