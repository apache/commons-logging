/*
 * Copyright 2001-2004 The Apache Software Foundation.
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
package org.apache.commons.logging;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Test cases for situations where getClassLoader or getContextClassLoader
 * return null. This can happen when using JDK 1.1. It can also happen when
 * JCL is deployed via the bootclassloader - something that could be done when
 * using java in embedded systems.
 */
public class NullClassLoaderTest extends TestCase{

    //---------------------- Main ---------------------------------    

    /**
     * Main method so this test case can be run direct from the command line.
     */
    public static void main(String[] args){
        String[] testCaseName = { NullClassLoaderTest.class.getName() };
        junit.textui.TestRunner.main(testCaseName);
    }

    //---------------------- Constructors ---------------------------------    

    /** Creates a new instance of LoadTest */
    public NullClassLoaderTest(String testName) {
        super(testName);
    }

    //---------------------- JUnit methods ---------------------------------    
    
    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTestSuite(NullClassLoaderTest.class);
        return suite;
    }
    
    //---------------------- unit tests ---------------------------------    
    
    /**
     * This tests that when getContextClassLoader returns null, the
     * LogFactory.getLog(name) method still correctly returns the same
     * log object when called multiple times with the same name.
     */
    public void testSameLogObject() throws Exception {
        ClassLoader oldContextClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            // emulate an app (not a webapp) running code loaded via the 
            // "null" classloader (bootclassloader for JDK1.2+, or
            // systemclassloader for jdk1.1).
            Thread.currentThread().setContextClassLoader(null);
            
            Log log1 = LogFactory.getLog("foo");
            Log log2 = LogFactory.getLog("foo");
            
            assertSame(
                "Calling getLog twice with the same category " +
                "resulted in different objects!", 
                log1, log2);
        } finally {
            Thread.currentThread().setContextClassLoader(oldContextClassLoader);
        }
    }
}
