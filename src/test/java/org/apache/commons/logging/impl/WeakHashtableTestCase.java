/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.apache.commons.logging.impl;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;

public class WeakHashtableTestCase extends TestCase {

    private static final int WAIT_FOR_THREAD_COMPLETION = 5000; // 5 seconds
    private static final int RUN_LOOPS = 3000;
    private static final int OUTER_LOOP = 400;
    private static final int THREAD_COUNT = 10;

    private static WeakHashtable hashtable;

    /** Maximum number of iterations before our test fails */
    private static final int MAX_GC_ITERATIONS = 50;

    private WeakHashtable weakHashtable;
    private Long keyOne;
    private Long keyTwo;
    private Long keyThree;
    private Long valueOne;
    private Long valueTwo;
    private Long valueThree;

    public WeakHashtableTestCase(final String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        weakHashtable = new WeakHashtable();

        keyOne = 1L;
        keyTwo = 2L;
        keyThree = 3L;
        valueOne = 100L;
        valueTwo = 200L;
        valueThree = 300L;

        weakHashtable.put(keyOne, valueOne);
        weakHashtable.put(keyTwo, valueTwo);
        weakHashtable.put(keyThree, valueThree);
    }

    /** Tests public boolean contains(Object value) */
    public void testContains() throws Exception {
        assertFalse(weakHashtable.contains(1L));
        assertFalse(weakHashtable.contains(2L));
        assertFalse(weakHashtable.contains(3L));
        assertTrue(weakHashtable.contains(100L));
        assertTrue(weakHashtable.contains(200L));
        assertTrue(weakHashtable.contains(300L));
        assertFalse(weakHashtable.contains(400L));
    }

    /** Tests public boolean containsKey(Object key) */
    public void testContainsKey() throws Exception {
        assertTrue(weakHashtable.containsKey(1L));
        assertTrue(weakHashtable.containsKey(2L));
        assertTrue(weakHashtable.containsKey(3L));
        assertFalse(weakHashtable.containsKey(100L));
        assertFalse(weakHashtable.containsKey(200L));
        assertFalse(weakHashtable.containsKey(300L));
        assertFalse(weakHashtable.containsKey(400L));
    }

    /** Tests public boolean containsValue(Object value) */
    public void testContainsValue() throws Exception {
        assertFalse(weakHashtable.containsValue(1L));
        assertFalse(weakHashtable.containsValue(2L));
        assertFalse(weakHashtable.containsValue(3L));
        assertTrue(weakHashtable.containsValue(100L));
        assertTrue(weakHashtable.containsValue(200L));
        assertTrue(weakHashtable.containsValue(300L));
        assertFalse(weakHashtable.containsValue(400L));
    }

    /** Tests public Enumeration elements() */
    public void testElements() throws Exception {
        final ArrayList elements = new ArrayList();
        for (final Enumeration e = weakHashtable.elements(); e.hasMoreElements();) {
            elements.add(e.nextElement());
        }
        assertEquals(3, elements.size());
        assertTrue(elements.contains(valueOne));
        assertTrue(elements.contains(valueTwo));
        assertTrue(elements.contains(valueThree));
    }

    /** Tests public Set entrySet() */
    public void testEntrySet() throws Exception {
        final Set entrySet = weakHashtable.entrySet();
        for (final Object element : entrySet) {
            final Map.Entry entry = (Map.Entry) element;
            final Object key = entry.getKey();
            if (keyOne.equals(key)) {
                assertEquals(valueOne, entry.getValue());
            } else if (keyTwo.equals(key)) {
                assertEquals(valueTwo, entry.getValue());
            } else if (keyThree.equals(key)) {
                assertEquals(valueThree, entry.getValue());
            } else {
                fail("Unexpected key");
            }
        }
    }

    /** Tests public Object get(Object key) */
    public void testGet() throws Exception {
        assertEquals(valueOne, weakHashtable.get(keyOne));
        assertEquals(valueTwo, weakHashtable.get(keyTwo));
        assertEquals(valueThree, weakHashtable.get(keyThree));
        assertNull(weakHashtable.get(50L));
    }

    /** Tests public Enumeration keys() */
    public void testKeys() throws Exception {
        final ArrayList keys = new ArrayList();
        for (final Enumeration e = weakHashtable.keys(); e.hasMoreElements();) {
            keys.add(e.nextElement());
        }
        assertEquals(3, keys.size());
        assertTrue(keys.contains(keyOne));
        assertTrue(keys.contains(keyTwo));
        assertTrue(keys.contains(keyThree));
    }

    /** Tests public Set keySet() */
    public void testKeySet() throws Exception {
        final Set keySet = weakHashtable.keySet();
        assertEquals(3, keySet.size());
        assertTrue(keySet.contains(keyOne));
        assertTrue(keySet.contains(keyTwo));
        assertTrue(keySet.contains(keyThree));
    }

    /** Tests public Object put(Object key, Object value) */
    public void testPut() throws Exception {
        final Long anotherKey = 2004L;
        weakHashtable.put(anotherKey, 1066L);

        assertEquals(1066L, weakHashtable.get(anotherKey));

        // Test compliance with the hashtable API re nulls
        Exception caught = null;
        try {
            weakHashtable.put(null, new Object());
        }
        catch (final Exception e) {
            caught = e;
        }
        assertNotNull("did not throw an exception adding a null key", caught);
        caught = null;
        try {
            weakHashtable.put(new Object(), null);
        }
        catch (final Exception e) {
            caught = e;
        }
        assertNotNull("did not throw an exception adding a null value", caught);
    }

    /** Tests public void putAll(Map t) */
    public void testPutAll() throws Exception {
        final Map newValues = new HashMap();
        final Long newKey = 1066L;
        final Long newValue = 1415L;
        newValues.put(newKey, newValue);
        final Long anotherNewKey = 1645L;
        final Long anotherNewValue = 1815L;
        newValues.put(anotherNewKey, anotherNewValue);
        weakHashtable.putAll(newValues);

        assertEquals(5, weakHashtable.size());
        assertEquals(newValue, weakHashtable.get(newKey));
        assertEquals(anotherNewValue, weakHashtable.get(anotherNewKey));
    }

    /** Tests public Object remove(Object key) */
    public void testRemove() throws Exception {
        weakHashtable.remove(keyOne);
        assertEquals(2, weakHashtable.size());
        assertNull(weakHashtable.get(keyOne));
    }

    /** Tests public Collection values() */
    public void testValues() throws Exception {
        final Collection values = weakHashtable.values();
        assertEquals(3, values.size());
        assertTrue(values.contains(valueOne));
        assertTrue(values.contains(valueTwo));
        assertTrue(values.contains(valueThree));
    }

    /**
     * Disabled this test as it makes wrong assumptions wrt the GC.
     * This test especially fails with:
     *
     * Java(TM) SE Runtime Environment (build pxi3260sr12-20121025_01(SR12))
     * IBM J9 VM (build 2.4, JRE 1.6.0 IBM J9 2.4 Linux x86-32 jvmxi3260sr12-20121024_1
     */
    public void xxxIgnoretestRelease() throws Exception {
        assertNotNull(weakHashtable.get(1L));
        final ReferenceQueue testQueue = new ReferenceQueue();
        final WeakReference weakKeyOne = new WeakReference(keyOne, testQueue);

        // lose our references
        keyOne = null;
        keyTwo = null;
        keyThree = null;
        valueOne = null;
        valueTwo = null;
        valueThree = null;

        int iterations = 0;
        int bytz = 2;
        while(true) {
            System.gc();
            if(iterations++ > MAX_GC_ITERATIONS){
                fail("Max iterations reached before resource released.");
            }

            if(weakHashtable.get(1L) == null) {
                break;

            }
            // create garbage:
            final byte[] b =  new byte[bytz];
            bytz = bytz * 2;
        }

        // some JVMs seem to take a little time to put references on
        // the reference queue once the reference has been collected
        // need to think about whether this is enough to justify
        // stepping through the collection each time...
        while(testQueue.poll() == null) {}

        // Test that the released objects are not taking space in the table
        assertEquals("underlying table not emptied", 0, weakHashtable.size());
    }

    public static class StupidThread extends Thread {

        public StupidThread(final String name) {
            super(name);
        }

        @Override
        public void run() {
            for (int i = 0; i < RUN_LOOPS; i++) {
                hashtable.put("key" + ":" + i%10, Boolean.TRUE);
                if(i%50 == 0) {
                    yield();
                }
            }
        }
    }

    public void testLOGGING_119() throws Exception {
        final Thread [] t = new Thread[THREAD_COUNT];
        for (int j=1; j <= OUTER_LOOP; j++) {
            hashtable = new WeakHashtable();
            for (int i = 0; i < t.length; i++) {
                t[i] = new StupidThread("Thread:" + i);
                t[i].setDaemon(true); // Otherwise we cannot exit
                t[i].start();
            }
            for (final Thread element : t) {
                element.join(WAIT_FOR_THREAD_COMPLETION);
                if (element.isAlive()) {
                    break; // at least one thread is stuck
                }
            }
            int active=0;
            for (final Thread element : t) {
                if (element.isAlive()) {
                    active++;
                }
            }
            if (active > 0) {
                fail("Attempt: " + j + " Stuck threads: " + active);
            }
        }
    }
}
