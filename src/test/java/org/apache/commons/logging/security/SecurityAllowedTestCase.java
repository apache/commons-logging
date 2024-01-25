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

package org.apache.commons.logging.security;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.security.AllPermission;
import java.util.Hashtable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.PathableClassLoader;
import org.apache.commons.logging.PathableTestSuite;

import junit.framework.Test;
import junit.framework.TestCase;

/**
 * Tests for logging with a security policy that allows JCL access to everything.
 * <p>
 * This class has only one unit test, as we are (in part) checking behavior in
 * the static block of the LogFactory class. As that class cannot be unloaded after
 * being loaded into a class loader, the only workaround is to use the
 * PathableClassLoader approach to ensure each test is run in its own
 * class loader, and use a separate test class for each test.
 */
public class SecurityAllowedTestCase extends TestCase {

    // Dummy special hashtable, so we can tell JCL to use this instead of
    // the standard one.
    public static class CustomHashtable extends Hashtable {

        /**
         * Generated serial version ID.
         */
        private static final long serialVersionUID = 8941017300059246720L;
    }

    /**
     * Return the tests included in this test suite.
     */
    public static Test suite() throws Exception {
        final PathableClassLoader parent = new PathableClassLoader(null);
        parent.useExplicitLoader("junit.", Test.class.getClassLoader());
        parent.addLogicalLib("commons-logging");
        parent.addLogicalLib("testclasses");

        final Class testClass = parent.loadClass(
            "org.apache.commons.logging.security.SecurityAllowedTestCase");
        return new PathableTestSuite(testClass, parent);
    }

    private SecurityManager oldSecMgr;

    @Override
    public void setUp() {
        // save security manager so it can be restored in tearDown
        oldSecMgr = System.getSecurityManager();
    }

    @Override
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
        // Ignore on Java 21
        if (System.getProperty("java.version").startsWith("21.")) {
            return;
        }
        System.setProperty(
                LogFactory.HASHTABLE_IMPLEMENTATION_PROPERTY,
                CustomHashtable.class.getName());
        final MockSecurityManager mySecurityManager = new MockSecurityManager();
        mySecurityManager.addPermission(new AllPermission());
        System.setSecurityManager(mySecurityManager);

        try {
            // Use reflection so that we can control exactly when the static
            // initializer for the LogFactory class is executed.
            final Class c = this.getClass().getClassLoader().loadClass(
                    "org.apache.commons.logging.LogFactory");
            final Method m = c.getMethod("getLog", Class.class);
            final Log log = (Log) m.invoke(null, this.getClass());

            // Check whether we had any security exceptions so far (which were
            // caught by the code). We should not, as every secure operation
            // should be wrapped in an AccessController. Any security exceptions
            // indicate a path that is missing an appropriate AccessController.
            //
            // We don't wait until after the log.info call to get this count
            // because java.util.logging tries to load a resource bundle, which
            // requires permission accessClassInPackage. JCL explicitly does not
            // wrap calls to log methods in AccessControllers because writes to
            // a log file *should* only be permitted if the original caller is
            // trusted to access that file.
            final int untrustedCodeCount = mySecurityManager.getUntrustedCodeCount();
            log.info("testing");

            // check that the default map implementation was loaded, as JCL was
            // forbidden from reading the HASHTABLE_IMPLEMENTATION_PROPERTY property.
            System.setSecurityManager(null);
            final Field factoryField = c.getDeclaredField("factories");
            factoryField.setAccessible(true);
            final Object factoryTable = factoryField.get(null);
            assertNotNull(factoryTable);
            assertEquals(CustomHashtable.class.getName(), factoryTable.getClass().getName());

            // we better compare that we have no security exception during the call to log
            // IBM JVM tries to load bundles during the invoke call, which increase the count
            assertEquals("Untrusted code count", untrustedCodeCount, mySecurityManager.getUntrustedCodeCount());
        } catch (final Throwable t) {
            // Restore original security manager so output can be generated; the
            // PrintWriter constructor tries to read the line.separator
            // system property.
            System.setSecurityManager(oldSecMgr);
            final StringWriter sw = new StringWriter();
            final PrintWriter pw = new PrintWriter(sw);
            t.printStackTrace(pw);
            fail("Unexpected exception:" + t.getMessage() + ":" + sw.toString());
        }
    }
}
