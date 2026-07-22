/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.commons.logging;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;

import junit.framework.TestCase;

public class LogFactoryWeakReferenceTestCase extends TestCase {
    private static final long MAX_WAIT_FOR_REF_NULLED_BY_GC = 15_000;

    public void testNotLeakingThisClassLoader() throws Exception {
        // create an isolated loader
        PathableClassLoader loader = new PathableClassLoader(null);
        loader.addLogicalLib("commons-logging");

        // load the LogFactory class through this loader
        Class<?> logFactoryClass = loader.loadClass(LogFactory.class.getName());

        // reflection hacks to obtain the weak reference
        Field field = logFactoryClass.getDeclaredField("thisClassLoaderRef");
        field.setAccessible(true);
        final WeakReference thisClassLoaderRef = (WeakReference) field.get(null);

        // the ref should at this point contain the loader
        assertSame(loader, thisClassLoaderRef.get());

        // null out the hard refs
        field = null;
        logFactoryClass = null;
        loader.close();
        loader = null;

        final GarbageCollectionHelper gcHelper = new GarbageCollectionHelper();
        gcHelper.run();
        try {
            final long start = System.currentTimeMillis();
            while (thisClassLoaderRef.get() != null) {
                if (System.currentTimeMillis() - start > MAX_WAIT_FOR_REF_NULLED_BY_GC) {
                    fail("After waiting " + MAX_WAIT_FOR_REF_NULLED_BY_GC + "ms, the weak ref still yields a non-null value.");
                }
                Thread.sleep(100);
            }
        } finally {
            gcHelper.close();
        }
    }
}
