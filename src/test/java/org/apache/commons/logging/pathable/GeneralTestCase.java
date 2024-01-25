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
package org.apache.commons.logging.pathable;

import java.net.URL;
import java.net.URLClassLoader;

import org.apache.commons.logging.PathableClassLoader;
import org.apache.commons.logging.PathableTestSuite;

import junit.framework.Test;
import junit.framework.TestCase;

/**
 * Tests for the PathableTestSuite class.
 */

public class GeneralTestCase extends TestCase {

    /**
     * Verify that the context class loader is a custom one, then reset it to
     * a non-custom one.
     */
    private static void checkAndSetContext() {
        final ClassLoader contextLoader = Thread.currentThread().getContextClassLoader();
        assertEquals("ContextLoader is of unexpected type",
                contextLoader.getClass().getName(),
                PathableClassLoader.class.getName());

        final URL[] noUrls = {};
        Thread.currentThread().setContextClassLoader(new URLClassLoader(noUrls));
    }

    /**
     * Verify that a certain system property is not set, then set it.
     */
    private static void checkAndSetProperties() {
        String prop = System.getProperty("no.such.property");
        assertNull("no.such.property is unexpectedly defined", prop);
        System.setProperty("no.such.property", "dummy value");
        prop = System.getProperty("no.such.property");
        assertNotNull("no.such.property is unexpectedly undefined", prop);
    }

    /**
     * Sets up a custom class loader hierarchy for this test case.
     */
    public static Test suite() throws Exception {
        final Class thisClass = GeneralTestCase.class;
        final ClassLoader thisClassLoader = thisClass.getClassLoader();

        final PathableClassLoader loader = new PathableClassLoader(null);
        loader.useExplicitLoader("junit.", thisClassLoader);
        loader.addLogicalLib("testclasses");

        // reload this class via the child class loader
        final Class testClass = loader.loadClass(thisClass.getName());

        // and return our custom TestSuite class
        return new PathableTestSuite(testClass, loader);
    }

    /**
     * Verify that when a test method modifies the context class loader it is
     * reset before the next test is run.
     * <p>
     * This method works in conjunction with testResetContext2. There is no
     * way of knowing which test method junit will run first, but it doesn't
     * matter; whichever one of them runs first will modify the contextClassloader.
     * If the PathableTestSuite isn't resetting the contextClassLoader then whichever
     * of them runs second will fail. Of course if other methods are run in-between
     * then those methods might also fail...
     */
    public void testResetContext1() {
        checkAndSetContext();
    }

    /**
     * See testResetContext1.
     */
    public void testResetContext2() {
        checkAndSetContext();
    }

    /**
     * Verify that when a test method modifies the system properties they are
     * reset before the next test is run.
     * <p>
     * This method works in conjunction with testResetProps2. There is no
     * way of knowing which test method junit will run first, but it doesn't
     * matter; whichever one of them runs first will modify the system properties.
     * If the PathableTestSuite isn't resetting the system properties then whichever
     * of them runs second will fail. Of course if other methods are run in-between
     * then those methods might also fail...
     */
    public void testResetProps1() {
        checkAndSetProperties();
    }

    /**
     * See testResetProps1.
     */
    public void testResetProps2() {
        checkAndSetProperties();
    }
}
