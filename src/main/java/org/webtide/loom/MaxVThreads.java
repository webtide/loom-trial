package org.webtide.loom;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class MaxVThreads
{
    public static void main(String... args) throws Exception
    {
        List<Thread> threads = new ArrayList<>();
        CountDownLatch hold = new CountDownLatch(1);
        while (threads.size() < 1_000_000)
        {
            CountDownLatch started = new CountDownLatch(1);
            Thread thread = Thread.builder().virtual().task(() ->
            {
                try
                {
                    started.countDown();
                    hold.await();
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
            }).start();
            threads.add(thread);
            started.await();
            System.err.printf("%s: %,d: memory=%,d%n",
                thread,
                threads.size(),
                Runtime.getRuntime().totalMemory());
        }
        hold.countDown();
        for (Thread thread : threads)
        {
            // System.err.printf("%s: joining...%n", thread);
            thread.join();
        }
    }
}
