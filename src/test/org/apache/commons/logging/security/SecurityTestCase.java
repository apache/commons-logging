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
 
package org.apache.commons.logging.security;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;

import junit.framework.Test;
import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.PathableClassLoader;
import org.apache.commons.logging.PathableTestSuite;

/**
 * Tests for logging with a SecurityManager that forbids access to nearly everything.
 * <p>
 * Performing tests with security permissions disabled is tricky, as building error
 * messages on failure requires certain security permissions. If the security manager
 * blocks these, then the test can fail without the error messages being output.
 */
public class SecurityTestCase extends TestCase
{
    private SecurityManager oldSecMgr;

    /**
     * Return the tests included in this test suite.
     */
    public static Test suite() throws Exception {
        PathableClassLoader parent = new PathableClassLoader(null);
        parent.useSystemLoader("junit.");
        parent.addLogicalLib("commons-logging");
        parent.addLogicalLib("testclasses");

        Class testClass = parent.loadClass(
            "org.apache.commons.logging.security.SecurityTestCase");
        return new PathableTestSuite(testClass, parent);
    }

    public void setUp() {
        // save security manager so it can be restored in tearDown
        oldSecMgr = System.getSecurityManager();
    }
    
    public void tearDown() {
        // Restore, so other tests don't get stuffed up if a test
        // sets a custom security manager.
        System.setSecurityManager(oldSecMgr);
    }

    public void testSimple() {
        SecurityManager mySecurityManager = new MockSecurityManager();
        System.setSecurityManager(mySecurityManager);

        try {
            // Use reflection so that we can control exactly when the static
            // initialiser for the LogFactory class is executed.
            Class c = this.getClass().getClassLoader().loadClass(
                    "org.apache.commons.logging.LogFactory");
            Method m = c.getMethod("getInstance", new Class[] {Class.class});
            Log log = (Log) m.invoke(null, new Object[] {this.getClass()});
            log.info("testing");
        } catch(Throwable t) {
            // Restore original security manager so output can be generated; the
            // PrintWriter constructor tries to read the line.separator
            // system property.
            System.setSecurityManager(oldSecMgr);
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            t.printStackTrace(pw);
            fail("Unexpected exception:" + t.getMessage() + ":" + sw.toString());
        }
    }
}
