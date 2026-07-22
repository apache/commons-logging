/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.commons.logging.tccl.logfactory;

import junit.framework.Test;
import junit.framework.TestCase;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.PathableClassLoader;
import org.apache.commons.logging.PathableTestSuite;

/**
 * Verifies that if the TCCL is a sibling of the classloader with `commons-logging`
 */
public class SiblingTcclTestCase extends TestCase {

    /**
     * Return the tests included in this test suite.
     */
    public static Test suite() throws Exception {
        // The classloader running the test has access to `commons-logging`
        final PathableClassLoader classLoader = new PathableClassLoader(null);
        classLoader.useExplicitLoader("junit.", Test.class.getClassLoader());
        classLoader.addLogicalLib("commons-logging");
        classLoader.addLogicalLib("testclasses");

        // The TCCL has only access to `log4j-api` and `slf4j-api`
        // See https://issues.apache.org/jira/browse/LOGGING-192
        final PathableClassLoader tcclLoader = new PathableClassLoader(null);
        tcclLoader.addLogicalLib("log4j-api");

        final Class<?> testClass = classLoader.loadClass(SiblingTcclTestCase.class.getName());
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

    public void testFactoryLoading() {
        // Loading the factory does not fail as in LOGGING-192
        final LogFactory factory = LogFactory.getFactory();
        // The selected implementation comes from this classloader
        assertEquals(getClass().getClassLoader(), factory.getClass().getClassLoader());
    }
}
