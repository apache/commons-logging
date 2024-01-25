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
package org.apache.commons.logging;

import junit.framework.TestCase;

/**
 * Test cases for situations where getClassLoader or getContextClassLoader
 * return null. This can happen when using JDK 1.1. It can also happen when
 * JCL is deployed via the bootclass loader - something that could be done when
 * using Java in embedded systems.
 */
public class NullClassLoaderTestCase extends TestCase {

    //---------------------- unit tests ---------------------------------

    /**
     * This tests that when getContextClassLoader returns null, the
     * LogFactory.getLog(name) method still correctly returns the same
     * log object when called multiple times with the same name.
     */
    public void testSameLogObject() throws Exception {
        // unfortunately, there just isn't any way to emulate JCL being
        // accessible via the null class loader in "standard" systems, so
        // we can't include this test in our standard unit tests.
    }
}
