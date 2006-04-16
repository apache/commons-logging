/*
 * Copyright 2006 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

import junit.framework.Test;
import junit.framework.TestCase;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.PathableClassLoader;
import org.apache.commons.logging.PathableTestSuite;


/**
 * Tests that verify that the process of configuring logging on startup
 * works correctly by selecting the file with the highest priority.
 * <p>
 * This test sets up a classpath where:
 * <ul>
 * <li> first file found has priority=20
 * <li> second file (in parent path) has priority=10 (parentFirst=false)
 * </ul>
 * The result should be that the first file is used.
 */

public class FirstPriorityConfigTestCase extends TestCase {

    // ------------------------------------------- JUnit Infrastructure Methods


    /**
     * Return the tests included in this test suite.
     */
    public static Test suite() throws Exception {
        Class thisClass = PriorityConfigTestCase.class;

        // Determine the URL to this .class file, so that we can then
        // append the priority dirs to it. For tidiness, load this
        // class through a dummy loader though this is not absolutely
        // necessary...
        PathableClassLoader dummy = new PathableClassLoader(null);
        dummy.useSystemLoader("junit.");
        dummy.addLogicalLib("testclasses");
        dummy.addLogicalLib("commons-logging");
        
        String thisClassPath = thisClass.getName().replace('.', '/') + ".class";
        URL baseUrl = dummy.findResource(thisClassPath);

        // Now set up the desired classloader hierarchy. We'll put a config
        // file of priority=10 in the container path, and ones of both
        // "no priority" and priority=20 in the webapp path.
        //
        // A second properties file with priority=20 is also added,
        // so we can check that the first one in the classpath is
        // used.
        PathableClassLoader containerLoader = new PathableClassLoader(null);
        containerLoader.useSystemLoader("junit.");
        containerLoader.addLogicalLib("commons-logging");
        
        URL pri10URL = new URL(baseUrl, "priority10/");
        containerLoader.addURL(pri10URL);

        PathableClassLoader webappLoader = new PathableClassLoader(containerLoader);
        webappLoader.setParentFirst(false);
        webappLoader.addLogicalLib("testclasses");
        
        URL pri20URL = new URL(baseUrl, "priority20/");
        webappLoader.addURL(pri20URL);
        
        // load the test class via webapp loader, and use the webapp loader
        // as the tccl loader too.
        Class testClass = webappLoader.loadClass(thisClass.getName());
        return new PathableTestSuite(testClass, webappLoader);
    }

    /**
     * Set up instance variables required by this test case.
     */
    public void setUp() throws Exception {
        LogFactory.releaseAll();
    }

    /**
     * Tear down instance variables required by this test case.
     */
    public void tearDown() {
        LogFactory.releaseAll();
    }

    // ----------------------------------------------------------- Test Methods

    /**
     * Verify that the config file being used is the one containing
     * the desired configId value.
     */
    public void testPriority() throws Exception {
        LogFactory instance = LogFactory.getFactory();
        String id = (String) instance.getAttribute("configId");
        assertEquals("Correct config file loaded", "priority20", id );
        
        fail("deliberate failure");
    }
}
