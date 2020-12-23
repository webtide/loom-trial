package org.webtide.loom;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.LongAdder;

public class MaxShallowKThreads
{
    private static final LongAdder result = new LongAdder();

    public static void main(String... args) throws Exception
    {
        MaxThreads.findMax(1, false);
    }
}
