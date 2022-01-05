/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache license, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the license for the specific language governing permissions and
 * limitations under the license.
 */


package org.apache.commons.logging;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

// after: https://github.com/apache/logging-log4j2/blob/c47e98423b461731f7791fcb9ea1079cd451f365/log4j-core/src/test/java/org/apache/logging/log4j/core/GarbageCollectionHelper.java
public final class GarbageCollectionHelper implements Closeable, Runnable {
    private static final OutputStream SINK = new OutputStream() {
        @Override
        public void write(int b) {
        }

        @Override
        public void write(byte[] b) {
        }

        @Override
        public void write(byte[] b, int off, int len) {
        }
    };
    private final AtomicBoolean running = new AtomicBoolean();
    private final CountDownLatch latch = new CountDownLatch(1);
    private final Thread gcThread = new Thread(new GcTask());

    class GcTask implements Runnable {
        @Override
        public void run() {
            try {
                while (running.get()) {
                    // Allocate data to help suggest a GC
                    try {
                        // 1mb of heap
                        byte[] buf = new byte[1024 * 1024];
                        SINK.write(buf);
                    } catch (final IOException ignored) {
                    }
                    // May no-op depending on the JVM configuration
                    System.gc();
                }
            } finally {
                latch.countDown();
            }
        }
    }

    @Override
    public void run() {
        if (running.compareAndSet(false, true)) {
            gcThread.start();
        }
    }

    @Override
    public void close() {
        running.set(false);
        try {
            junit.framework.TestCase.assertTrue("GarbageCollectionHelper did not shut down cleanly",
                    latch.await(10, TimeUnit.SECONDS));
        } catch (final InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}

