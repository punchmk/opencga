/*
 * Copyright 2015-2017 OpenCB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opencb.opencga.storage.hadoop.utils;

import org.apache.hadoop.hbase.io.compress.Compression;
import org.apache.hadoop.hbase.util.Bytes;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.ExternalResource;
import org.opencb.opencga.storage.core.variant.VariantStorageBaseTest;
import org.opencb.opencga.storage.hadoop.variant.HadoopVariantStorageTest;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created on 18/05/16
 *
 * @author Jacobo Coll &lt;jacobo167@gmail.com&gt;
 */
public class HBaseLockTest extends VariantStorageBaseTest implements HadoopVariantStorageTest {

    @ClassRule
    public static ExternalResource externalResource = new HadoopExternalResource();

    private HBaseLock hbaseLock;

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
        clearDB(DB_NAME);
        HBaseManager hbaseManager = new HBaseManager(configuration.get());
        hbaseManager.createTableIfNeeded(DB_NAME, Bytes.toBytes("0"), Compression.Algorithm.NONE);
        hbaseLock = new HBaseLock(hbaseManager, DB_NAME, Bytes.toBytes("0"), Bytes.toBytes("R"));
    }

    @Test
    public void testLock() throws Exception {
        int lockId = 1;
        for (int i = 0; i < 10; i++) {
            System.out.println("i = " + i);
            long lock = hbaseLock.lock(getColumn(lockId), 10, 10);
            System.out.println("lock = " + lock);
            hbaseLock.unlock(getColumn(lockId), lock);
        }
    }

    @Test
    public void testConcurrentLock() throws Exception {
        int lockId = 2;

        AtomicInteger counter = new AtomicInteger(0);
        Set<String> threadWithLock = Collections.synchronizedSet(new HashSet<>());

        int nThreads = 20;
        ExecutorService executorService = Executors.newFixedThreadPool(nThreads);
        List<Future> futures = new ArrayList<>();
        for (int t = 0; t < nThreads; t++) {
            futures.add(executorService.submit(() -> {
                try {
                    for (int i = 0; i < 5; i++) {
                        System.out.println("i = " + i);
                        long lock = hbaseLock.lock(getColumn(lockId), 1000, 20000);
                        System.out.println("[" + Thread.currentThread().getName() + "] Enter LOCK");
                        assertEquals(threadWithLock.toString(), 0, threadWithLock.size());
                        threadWithLock.add(Thread.currentThread().getName());
                        assertEquals(threadWithLock.toString(), 1, threadWithLock.size());
                        int value = counter.addAndGet(1);
                        Thread.sleep(100);
                        assertEquals(threadWithLock.toString(), 1, threadWithLock.size());
                        assertEquals(threadWithLock.toString(), value, counter.get());
                        threadWithLock.remove(Thread.currentThread().getName());
                        System.out.println("lock = " + lock);
                        System.out.println("[" + Thread.currentThread().getName() + "] Exit LOCK");
                        hbaseLock.unlock(getColumn(lockId), lock);
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }));
        }
        executorService.shutdown();
        executorService.awaitTermination(2000, TimeUnit.SECONDS);

        for (Future future : futures) {
            assertTrue(future.isDone());
            future.get();
        }

    }

    public byte[] getColumn(int lockId) {
        return Bytes.toBytes(lockId);
    }

    @Test
    public void testLockAndLock() throws Exception {
        int lockId = 3;
        long lock = hbaseLock.lock(getColumn(lockId), 1000, 2000);
        System.out.println("lock = " + lock);

        thrown.expect(TimeoutException.class);
        hbaseLock.lock(getColumn(lockId), 1000, 1000);

    }

    @Test
    public void testLockAfterExpiring() throws Exception {
        int lockId = 4;
        long lock = hbaseLock.lock(getColumn(lockId), 1000, 1000);
        System.out.println("lock = " + lock);

        Thread.sleep(2000);
        System.out.println("Expired lock = " + lock);

        lock = hbaseLock.lock(getColumn(lockId), 1000, 1000);
        System.out.println("Unlock = " + lock);
        hbaseLock.unlock(getColumn(lockId), lock);

    }
}