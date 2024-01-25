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

package org.apache.commons.logging.log4j.log4j12;

import org.apache.commons.logging.PathableClassLoader;
import org.apache.commons.logging.PathableTestSuite;
import org.apache.commons.logging.impl.Log4JLogger;
import org.apache.commons.logging.impl.LogFactoryImpl;

import junit.framework.Test;
import junit.framework.TestCase;

/**
 * Tests for Log4J logging that emulate a webapp running within
 * a container where all the necessary libs are in the parent.
 */

public class ParentClasspathStandardTestCase extends TestCase {

    /**
     * Return the tests included in this test suite.
     */
    public static Test suite() throws Exception {
        // Ensure Log4JLogger is used
        System.setProperty(LogFactoryImpl.LOG_PROPERTY, Log4JLogger.class.getName());
        final PathableClassLoader parent = new PathableClassLoader(null);
        parent.useExplicitLoader("junit.", Test.class.getClassLoader());
        parent.addLogicalLib("commons-logging");
        parent.addLogicalLib("log4j12");

        final PathableClassLoader child = new PathableClassLoader(parent);
        child.addLogicalLib("testclasses");

        final Class testClass = child.loadClass(
            "org.apache.commons.logging.log4j.log4j12.Log4j12StandardTests");
        return new PathableTestSuite(testClass, child);
    }
}
