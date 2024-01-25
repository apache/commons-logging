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

package org.apache.commons.logging.jdk14;

import org.apache.commons.logging.PathableClassLoader;
import org.apache.commons.logging.PathableTestSuite;

import junit.framework.Test;

/**
 * TestCase for Jdk14 logging when the commons-logging-api jar file is in
 * the parent classpath and commons-logging.jar is in the child.
 */

public class CustomConfigAPITestCase extends CustomConfigTestCase {

    /**
     * Return the tests included in this test suite.
     */
    public static Test suite() throws Exception {
        final PathableClassLoader parent = new PathableClassLoader(null);
        parent.useExplicitLoader("junit.", Test.class.getClassLoader());

        // the TestHandler class must be accessible from the System class loader
        // in order for java.util.logging.LogManager.readConfiguration to
        // be able to instantiate it. And this test case must see the same
        // class in order to be able to access its data. Yes this is ugly
        // but the whole jdk14 API is a ******* mess anyway.
        final ClassLoader scl = ClassLoader.getSystemClassLoader();
        loadTestHandler(HANDLER_NAME, scl);
        parent.useExplicitLoader(HANDLER_NAME, scl);
        parent.addLogicalLib("commons-logging-api");

        final PathableClassLoader child = new PathableClassLoader(parent);
        child.addLogicalLib("testclasses");
        child.addLogicalLib("commons-logging");

        final Class testClass = child.loadClass(CustomConfigAPITestCase.class.getName());
        return new PathableTestSuite(testClass, child);
    }

    public CustomConfigAPITestCase(final String name) {
        super(name);
    }
}
