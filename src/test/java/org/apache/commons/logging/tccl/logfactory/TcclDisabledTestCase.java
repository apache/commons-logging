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

package org.apache.commons.logging.tccl.logfactory;

import java.net.URL;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.PathableClassLoader;
import org.apache.commons.logging.PathableTestSuite;

import junit.framework.Test;
import junit.framework.TestCase;

/**
 * Verify that a commons-logging.properties file can prevent a custom
 * LogFactoryImpl being loaded from the tccl class loader.
 */

public class TcclDisabledTestCase extends TestCase {

    public static final String MY_LOG_FACTORY_PKG =
        "org.apache.commons.logging.tccl.custom";

    public static final String MY_LOG_FACTORY_IMPL =
        MY_LOG_FACTORY_PKG + ".MyLogFactoryImpl";

    /**
     * Return the tests included in this test suite.
     */
    public static Test suite() throws Exception {
        final Class thisClass = TcclDisabledTestCase.class;

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

        // Now set up the desired class loader hierarchy. Everything goes into
        // the parent classpath, but we exclude the custom LogFactoryImpl
        // class.
        //
        // We then create a tccl class loader that can see the custom
        // LogFactory class. Therefore if that class can be found, then the
        // TCCL must have been used to load it.
        final PathableClassLoader emptyLoader = new PathableClassLoader(null);

        final PathableClassLoader parentLoader = new PathableClassLoader(null);
        parentLoader.useExplicitLoader("junit.", Test.class.getClassLoader());
        parentLoader.addLogicalLib("commons-logging");
        parentLoader.addLogicalLib("testclasses");
        // hack to ensure that the test class loader can't see
        // the custom MyLogFactoryImpl
        parentLoader.useExplicitLoader(
            MY_LOG_FACTORY_PKG + ".", emptyLoader);

        final URL propsEnableUrl = new URL(baseUrl, "props_disable_tccl/");
        parentLoader.addURL(propsEnableUrl);

        final PathableClassLoader tcclLoader = new PathableClassLoader(parentLoader);
        tcclLoader.addLogicalLib("testclasses");

        final Class testClass = parentLoader.loadClass(thisClass.getName());
        return new PathableTestSuite(testClass, tcclLoader);
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
     * Verify that MyLogFactoryImpl is only loadable via the tccl.
     */
    public void testLoader() throws Exception {

        final ClassLoader thisClassLoader = this.getClass().getClassLoader();
        final ClassLoader tcclLoader = Thread.currentThread().getContextClassLoader();

        // the tccl loader should NOT be the same as the loader that loaded this test class.
        assertNotSame("tccl not same as test class loader", thisClassLoader, tcclLoader);

        // MyLogFactoryImpl should not be loadable via parent loader
        try {
            final Class clazz = thisClassLoader.loadClass(MY_LOG_FACTORY_IMPL);
            fail("Unexpectedly able to load MyLogFactoryImpl via test class class loader");
            assertNotNull(clazz); // silence warning about unused var
        } catch (final ClassNotFoundException ex) {
            // ok, expected
        }

        // MyLogFactoryImpl should be loadable via tccl loader
        try {
            final Class clazz = tcclLoader.loadClass(MY_LOG_FACTORY_IMPL);
            assertNotNull(clazz);
        } catch (final ClassNotFoundException ex) {
            fail("Unexpectedly unable to load MyLogFactoryImpl via tccl class loader");
        }
    }

    /**
     * Verify that the custom LogFactory implementation which is only accessible
     * via the TCCL has NOT been loaded. Because this is only accessible via the
     * TCCL, and we've use a commons-logging.properties that disables TCCL loading,
     * we should see the default LogFactoryImpl rather than the custom one.
     */
    public void testTcclLoading() throws Exception {
        try {
            final LogFactory instance = LogFactory.getFactory();
            fail("Unexpectedly succeeded in loading custom factory, though TCCL disabled.");
            assertNotNull(instance); // silence warning about unused var
        } catch (final org.apache.commons.logging.LogConfigurationException ex) {
            // ok, custom MyLogFactoryImpl as specified in props_disable_tccl
            // could not be found.
            final int index = ex.getMessage().indexOf(MY_LOG_FACTORY_IMPL);
            assertTrue("MylogFactoryImpl not found", index >= 0);
        }
    }
}
