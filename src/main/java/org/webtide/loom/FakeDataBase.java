package org.webtide.loom;

import java.security.SecureRandom;
import java.util.Random;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Stream;

import org.openjdk.jmh.infra.Blackhole;

public class FakeDataBase
{
    private static final Random RANDOM = new SecureRandom();
    private static final Semaphore POOL = new Semaphore(100);
    private static LongAdder result = new LongAdder();

    public static Object get(String key) throws InterruptedException
    {
        // pretend to get a connection from a JDBC connection pool
        POOL.acquire();

        // pretend to talk to a remote server
        Thread.sleep(1 + RANDOM.nextInt(5));

        // pretend to get a result from the database
        Object result = Long.toHexString(key.hashCode() + RANDOM.nextLong()).repeat(5 + RANDOM.nextInt(5));

        // Consume some CPU with semaphore
        Blackhole.consumeCPU(1000 + result.hashCode() % 1000);

        // Put our thread back into the database.
        POOL.release();

        return result;
    }

    public static long getResult()
    {
        return result.longValue();
    }

    public static void put(String key, Object value) throws InterruptedException
    {
        // pretend to marshal the update to the database
        long data = Stream.of(value.toString().toCharArray()).count();

        // pretend to get a connection from a JDBC connection pool
        POOL.acquire();

        // pretend to talk to a remote server
        Thread.sleep(2 + RANDOM.nextInt((int)(data % 3)));

        // pretend to care about the value
        result.add(key.hashCode());
        result.add(value.hashCode());
        result.add(data);

        // Put our thread back into the database.
        POOL.release();
    }
}
