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
package org.apache.commons.logging.log4j2;

import java.util.List;

import junit.framework.TestCase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.impl.Log4jApiLogFactory;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.test.appender.ListAppender;
import org.apache.logging.log4j.message.ObjectMessage;
import org.apache.logging.log4j.message.SimpleMessage;

public class CallerInformationTestCase extends TestCase {

    private static final Object OBJ = new Object();
    private static final String STRING = "String";
    private static final Throwable T = new RuntimeException();
    private static final Marker MARKER = MarkerManager.getMarker("COMMONS-LOGGING");

    private static final Level[] levels = {Level.FATAL, Level.ERROR, Level.WARN, Level.INFO, Level.DEBUG, Level.TRACE};

    private LogFactory factory;
    private Log log;
    private ListAppender appender;

    @Override
    public void setUp() {
        factory = LogFactory.getFactory();
        log = factory.getInstance(getClass());
        final LoggerContext context = LoggerContext.getContext(false);
        final Configuration config = context.getConfiguration();
        appender = config.getAppender("LIST");
        assertNotNull("Missing Log4j 2.x appender.", appender);
    }

    public void testFactoryClassName() {
        assertEquals(Log4jApiLogFactory.class, factory.getClass());
    }

    public void testLocationInfo() {
        appender.clear();
        // The following value must match the line number
        final int currentLineNumber = 65;
        log.fatal(OBJ);
        log.fatal(OBJ, T);
        log.error(OBJ);
        log.error(OBJ, T);
        log.warn(OBJ);
        log.warn(OBJ, T);
        log.info(OBJ);
        log.info(OBJ, T);
        log.debug(OBJ);
        log.debug(OBJ, T);
        log.trace(OBJ);
        log.trace(OBJ, T);
        final ObjectMessage expectedMessage = new ObjectMessage(OBJ);
        final List<LogEvent> events = appender.getEvents();
        assertEquals("All events received.", levels.length * 2, events.size());
        for (int lev = 0; lev < levels.length; lev++) {
            for (int hasThrowable = 0; hasThrowable <= 1; hasThrowable++) {
                final LogEvent event = events.get(2 * lev + hasThrowable);
                assertEquals("Correct message.", expectedMessage, event.getMessage());
                assertEquals("Correct marker.", MARKER, event.getMarker());
                assertEquals("Level matches.", levels[lev], event.getLevel());
                final StackTraceElement location = event.getSource();
                assertNotNull("Has location", location);
                assertEquals("Correct source file.", "CallerInformationTestCase.java", location.getFileName());
                assertEquals("Correct method name.", "testLocationInfo", location.getMethodName());
                assertEquals("Correct location class.", getClass().getName(), location.getClassName());
                assertEquals("Correct location line.",
                        currentLineNumber + 2 * lev + hasThrowable + 1,
                        location.getLineNumber());
                assertEquals("Correct exception", hasThrowable > 0 ? T : null, event.getThrown());
            }
        }
    }

    public void testMessageType() {
        appender.clear();
        log.info(OBJ);
        log.info(STRING);
        final List<LogEvent> events = appender.getEvents();
        assertEquals("Correct number of messages.", 2, events.size());
        assertEquals("Correct message type.", new ObjectMessage(OBJ), events.get(0).getMessage());
        assertEquals("Correct message type.", new SimpleMessage(STRING), events.get(1).getMessage());
    }
}
