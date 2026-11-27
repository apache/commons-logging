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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.impl.Slf4jLogFactory;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

/**
 * Tests that a legacy JCL binding ignored by SLF4J 2 does not prevent logging.
 */
class IgnoredBindingTest {

    /**
     * Tests logger creation and reuse with a valid provider and a dormant reverse binding.
     */
    @Test
    void testIgnoredBinding() {
        assertNotNull(getClass().getClassLoader().getResource("org/slf4j/impl/JCLLoggerFactory.class"));
        assertEquals("ch.qos.logback.classic.LoggerContext", LoggerFactory.getILoggerFactory().getClass().getName());
        assertEquals(Slf4jLogFactory.class, LogFactory.getFactory().getClass());
        final Log log = LogFactory.getLog(getClass());
        assertSame(log, LogFactory.getLog(getClass().getName()));
        log.info("A dormant SLF4J binding does not cause a delegation loop.");
    }
}
