package org.webtide.loom;

import java.security.SecureRandom;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.openjdk.jmh.infra.Blackhole;

public class ManyTasks
{
    private static final Random RANDOM = new SecureRandom();

    static Runnable newTask(CountDownLatch latch)
    {
        long id = RANDOM.nextLong();
        String task = Integer.toHexString(RANDOM.nextInt());
        return () ->
        {
            try
            {
                // pretend to authenticate the user
                Object userdata = FakeDataBase.get(Long.toString(id));

                // small chance auth fails.
                if (userdata.toString().startsWith("6666"))
                    return;

                // pretend to get some app data
                Object data = FakeDataBase.get(task);

                // pretend to process the data
                Blackhole.consumeCPU(1000 + data.hashCode() % 1000);

                // pretend to mutate some app data
                FakeDataBase.put(task, Long.toHexString(data.hashCode() + id).repeat(1 + Math.abs((int)(id % 10))));
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
            finally
            {
                // signal completion
                latch.countDown();
            }
        };
    }

    private static void warmup(int tasks) throws InterruptedException
    {
        // warm up
        CountDownLatch warmup = new CountDownLatch(tasks * 2);
        for (int i = 0 ; i < tasks; i++)
        {
            Thread.ofPlatform().start(newTask(warmup));
            Thread.ofVirtual().start(newTask(warmup));
        }
        warmup.await();
    }

    private static long testThreadPool(int tasks) throws Exception
    {
        CountDownLatch latch = new CountDownLatch(tasks);
        QueuedThreadPool pool = new QueuedThreadPool(100, 100, -1, 0, null, null);
        pool.start();

        try
        {
            long started = System.nanoTime();
            // run tasks with a thread pool
            for (int i = 0; i < tasks; i++)
                pool.execute(newTask(latch));
            latch.await();
            return System.nanoTime() - started;
        }
        finally
        {
            pool.stop();
        }
    }

    private static long testVThreads(int tasks) throws Exception
    {
        CountDownLatch latch = new CountDownLatch(tasks);

        long started = System.nanoTime();
        // run tasks with a thread pool
        for (int i = 0 ; i < tasks; i++)
            Thread.ofVirtual().start(newTask(latch));
        latch.await();
        return System.nanoTime() - started;
    }

    public static void main(String... args) throws Exception
    {
        warmup(100_000);

        System.err.println("======");
        System.err.printf("Pooled  K Threads %,dms%n", TimeUnit.NANOSECONDS.toMillis(testThreadPool(400_000)));
        System.err.printf("Spawned V Threads %,dms%n", TimeUnit.NANOSECONDS.toMillis(testVThreads(400_000)));
        System.err.println("======");
        System.err.println(FakeDataBase.getResult());
    }
}
