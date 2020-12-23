package org.webtide.loom;

import java.util.concurrent.CountDownLatch;

public class MaxShallowVThreads
{
    public static void main(String... args) throws Exception
    {
        MaxThreads.findMax(1, true);
    }
}
