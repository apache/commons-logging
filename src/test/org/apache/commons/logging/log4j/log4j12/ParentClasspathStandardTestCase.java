/*
 * Copyright 2005 The Apache Software Foundation.
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

package org.apache.commons.logging.log4j.log4j12;

import junit.framework.Test;

import org.apache.commons.logging.PathableTestSuite;
import org.apache.commons.logging.PathableClassLoader;
import org.apache.commons.logging.log4j.StandardTests;

/**
 * Tests for Log4J logging that emulate a webapp running within
 * a container where all the necessary libs are in the parent.
 */

public class ParentClasspathStandardTestCase extends StandardTests {

    /**
     * Return the tests included in this test suite.
     */
    public static Test suite() throws Exception {
        Class thisClass = ParentClasspathStandardTestCase.class;
        
        PathableClassLoader parent = new PathableClassLoader(null);
        parent.useSystemLoader("junit.");
        parent.addLogicalLib("commons-logging");
        parent.addLogicalLib("log4j12");

        PathableClassLoader child = new PathableClassLoader(parent);
        child.addLogicalLib("testclasses");
        
        Class testClass = child.loadClass(thisClass.getName());
        return new PathableTestSuite(testClass, child);
    }
    
    /**
     * Return the name of a class that makes all direct calls to log4j
     * apis. See StandardTests.TestHelper for more information.
     */
    public String getTestHelperClassName() {
        return "org.apache.commons.logging.log4j.log4j12.TestHelper";
    }
}
