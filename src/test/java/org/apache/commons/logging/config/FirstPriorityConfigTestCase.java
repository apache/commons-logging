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

package org.apache.commons.logging.config;

import java.net.URL;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.PathableClassLoader;
import org.apache.commons.logging.PathableTestSuite;

import junit.framework.Test;
import junit.framework.TestCase;

/**
 * Tests that verify that the process of configuring logging on startup
 * works correctly by selecting the file with the highest priority.
 * <p>
 * This test sets up a classpath where:
 * <ul>
 * <li> first file found has priority=20
 * <li> second file found has priority=10
 * </ul>
 * The result should be that the first file is used.
 */
public class FirstPriorityConfigTestCase extends TestCase {

    /**
     * Return the tests included in this test suite.
     */
    public static Test suite() throws Exception {
        final Class thisClass = FirstPriorityConfigTestCase.class;

        // Determine the URL to this .class file, so that we can then
        // append the priority dirs to it. For tidiness, load this
        // class through a dummy loader though this is not absolutely
        // necessary...
        final PathableClassLoader dummy = new PathableClassLoader(null);
        dummy.useExplicitLoader("junit.", Test.class.getClassLoader());
        dummy.addLogicalLib("testclasses");
        dummy.addLogicalLib("commons-logging");

        final String thisClassPath = thisClass.getName().replace('.', '/') + ".class";
        final URL baseUrl = dummy.findResource(thisClassPath);

        // Now set up the desired class loader hierarchy. We'll put JCL
        // in the container path, the test in a webapp path, and
        // both config files into the webapp path too.
        final PathableClassLoader containerLoader = new PathableClassLoader(null);
        containerLoader.useExplicitLoader("junit.", Test.class.getClassLoader());
        containerLoader.addLogicalLib("commons-logging");

        final PathableClassLoader webappLoader = new PathableClassLoader(containerLoader);
        webappLoader.addLogicalLib("testclasses");

        final URL pri20URL = new URL(baseUrl, "priority20/");
        webappLoader.addURL(pri20URL);

        final URL pri10URL = new URL(baseUrl, "priority10/");
        webappLoader.addURL(pri10URL);

        // load the test class via webapp loader, and use the webapp loader
        // as the tccl loader too.
        final Class testClass = webappLoader.loadClass(thisClass.getName());
        return new PathableTestSuite(testClass, webappLoader);
    }

    /**
     * Sets up instance variables required by this test case.
     */
    @Override
    public void setUp() throws Exception {
        LogFactory.releaseAll();
    }

    /**
     * Tear down instance variables required by this test case.
     */
    @Override
    public void tearDown() {
        LogFactory.releaseAll();
    }

    /**
     * Verify that the config file being used is the one containing
     * the desired configId value.
     */
    public void testPriority() throws Exception {
        final LogFactory instance = LogFactory.getFactory();

        final ClassLoader thisClassLoader = this.getClass().getClassLoader();
        final ClassLoader lfClassLoader = instance.getClass().getClassLoader();
        final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();

        // context class loader should be thisClassLoader
        assertEquals(thisClassLoader, contextClassLoader);

        // lfClassLoader should be parent of this class loader
        assertEquals(lfClassLoader, thisClassLoader.getParent());
        assertEquals(PathableClassLoader.class.getName(),
                lfClassLoader.getClass().getName());

        final String id = (String) instance.getAttribute("configId");
        assertEquals("Correct config file loaded", "priority20", id );
    }
}
