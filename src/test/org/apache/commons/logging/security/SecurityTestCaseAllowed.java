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
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.security.AllPermission;
import java.util.Hashtable;

import junit.framework.Test;
import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.PathableClassLoader;
import org.apache.commons.logging.PathableTestSuite;

/**
 * Tests for logging with a security policy that allows JCL access to everything.
 * <p>
 * This class has only one unit test, as we are (in part) checking behaviour in
 * the static block of the LogFactory class. As that class cannot be unloaded after
 * being loaded into a classloader, the only workaround is to use the 
 * PathableClassLoader approach to ensure each test is run in its own
 * classloader, and use a separate testcase class for each test.
 */
public class SecurityTestCaseAllowed extends TestCase
{
    private SecurityManager oldSecMgr;

    // Dummy special hashtable, so we can tell JCL to use this instead of
    // the standard one.
    public static class CustomHashtable extends Hashtable {
    }

    /**
     * Return the tests included in this test suite.
     */
    public static Test suite() throws Exception {
        PathableClassLoader parent = new PathableClassLoader(null);
        parent.useSystemLoader("junit.");
        parent.addLogicalLib("commons-logging");
        parent.addLogicalLib("testclasses");

        Class testClass = parent.loadClass(
            "org.apache.commons.logging.security.SecurityTestCaseAllowed");
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

    /**
     * Test what happens when JCL is run with all permissions enabled. Custom
     * overrides should take effect.
     */
    public void testAllAllowed() {
        System.setProperty(
                LogFactory.HASHTABLE_IMPLEMENTATION_PROPERTY,
                CustomHashtable.class.getName());
        MockSecurityManager mySecurityManager = new MockSecurityManager();
        mySecurityManager.addPermission(new AllPermission());
        System.setSecurityManager(mySecurityManager);

        try {
            // Use reflection so that we can control exactly when the static
            // initialiser for the LogFactory class is executed.
            Class c = this.getClass().getClassLoader().loadClass(
                    "org.apache.commons.logging.LogFactory");
            Method m = c.getMethod("getLog", new Class[] {Class.class});
            Log log = (Log) m.invoke(null, new Object[] {this.getClass()});
            log.info("testing");
            
            // check that the default map implementation was loaded, as JCL was
            // forbidden from reading the HASHTABLE_IMPLEMENTATION_PROPERTY property.
            System.setSecurityManager(null);
            Field factoryField = c.getDeclaredField("factories");
            factoryField.setAccessible(true);
            Object factoryTable = factoryField.get(null); 
            assertNotNull(factoryTable);
            assertEquals(CustomHashtable.class.getName(), factoryTable.getClass().getName());
            
            assertEquals(0, mySecurityManager.getUntrustedCodeCount());
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
