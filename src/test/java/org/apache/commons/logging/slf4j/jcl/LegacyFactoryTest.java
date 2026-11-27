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
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.impl.LogFactoryImpl;
import org.apache.commons.logging.impl.NoOpLog;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

/**
 * Tests explicit legacy factory configuration with an SLF4J binding to Commons Logging.
 */
class LegacyFactoryTest {

    /**
     * Tests that both logging APIs reach the configured NoOpLog without a delegation loop.
     */
    @Test
    void testExplicitLegacyFactory() {
        assertEquals("org.slf4j.impl.JCLLoggerFactory", LoggerFactory.getILoggerFactory().getClass().getName());
        assertEquals(LogFactoryImpl.class, LogFactory.getFactory().getClass());
        assertEquals(NoOpLog.class, LogFactory.getLog(getClass()).getClass());
        assertFalse(LoggerFactory.getLogger(getClass()).isErrorEnabled());
    }
}
