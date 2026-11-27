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

package org.apache.commons.logging.slf4j.jcl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.commons.logging.LogConfigurationException;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.impl.Slf4jLogFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.slf4j.LoggerFactory;

/**
 * Tests rejection of an active SLF4J binding that delegates back to Commons Logging.
 */
class DelegationLoopTest {

    /**
     * Checks that a logger request explains the delegation loop and its remedies.
     *
     * @param request the logger request.
     */
    private static void assertDelegationLoop(final Executable request) {
        final LogConfigurationException exception = assertThrows(LogConfigurationException.class, request);
        assertTrue(exception.getMessage().contains("slf4j-jcl"));
        assertTrue(exception.getMessage().contains("delegation loop"));
        assertTrue(exception.getMessage().contains(LogFactory.FACTORY_PROPERTY));
        assertTrue(exception.getMessage().contains(LogFactory.FACTORY_DEFAULT));
    }

    /**
     * Tests automatic selection with named and class loggers, including repeated requests.
     */
    @Test
    void testAutomaticFactory() {
        assertEquals("org.slf4j.impl.JCLLoggerFactory", LoggerFactory.getILoggerFactory().getClass().getName());
        assertEquals(Slf4jLogFactory.class, LogFactory.getFactory().getClass());
        assertDelegationLoop(() -> LogFactory.getLog("delegation-loop"));
        assertDelegationLoop(() -> LogFactory.getLog("delegation-loop"));
        assertDelegationLoop(() -> LogFactory.getLog(getClass()));
    }

    /**
     * Tests explicit factory construction, which bypasses automatic discovery.
     */
    @Test
    void testExplicitFactory() {
        final Slf4jLogFactory factory = new Slf4jLogFactory();
        assertDelegationLoop(() -> factory.getInstance("explicit-factory"));
    }
}
