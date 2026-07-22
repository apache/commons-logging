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
package org.apache.commons.logging.slf4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.impl.Slf4jLogFactory;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.ThrowableProxy;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.read.ListAppender;
import ch.qos.logback.core.spi.FilterReply;

public class CallerInformationTestCase extends TestCase {

    private static final String STRING = "String";
    private static final Throwable T = new RuntimeException();
    private static final List<Marker> MARKERS = Collections.singletonList(MarkerFactory.getMarker("COMMONS-LOGGING"));

    private static final Level[] levels = {Level.ERROR, // SLF4J has no FATAL level
            Level.ERROR, Level.WARN, Level.INFO, Level.DEBUG, Level.TRACE};

    private LogFactory factory;
    private Log log;
    private ListAppender<ILoggingEvent> appender;

    @Override
    public void setUp() {
        factory = LogFactory.getFactory();
        log = factory.getInstance(getClass());
        final LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        final Logger logger = context.getLogger(Logger.ROOT_LOGGER_NAME);
        appender = (ListAppender) logger.getAppender("LIST");
        appender.clearAllFilters();
        appender.addFilter(new Filter<ILoggingEvent>() {
            @Override
            public FilterReply decide(final ILoggingEvent event) {
                // Force the registration of caller data
                event.getCallerData();
                return FilterReply.NEUTRAL;
            }
        });
    }

    public void testFactoryClassName() {
        assertEquals(Slf4jLogFactory.class, factory.getClass());
    }

    public void testLocationInfo() {
        appender.list.clear();
        // The following value must match the line number
        final int currentLineNumber = 79;
        log.fatal(STRING);
        log.fatal(STRING, T);
        log.error(STRING);
        log.error(STRING, T);
        log.warn(STRING);
        log.warn(STRING, T);
        log.info(STRING);
        log.info(STRING, T);
        log.debug(STRING);
        log.debug(STRING, T);
        log.trace(STRING);
        log.trace(STRING, T);
        final List<ILoggingEvent> events = new ArrayList<>(appender.list);
        assertEquals("All events received.", levels.length * 2, events.size());
        for (int lev = 0; lev < levels.length; lev++) {
            for (int hasThrowable = 0; hasThrowable <= 1; hasThrowable++) {
                final ILoggingEvent event = events.get(2 * lev + hasThrowable);
                assertEquals("Correct message.", STRING, event.getMessage());
                assertEquals("Correct marker.", MARKERS, event.getMarkerList());
                assertEquals("Level matches.", levels[lev], event.getLevel());
                final StackTraceElement[] callerData = event.getCallerData();
                assertTrue("Has location", callerData != null && callerData.length > 0);
                final StackTraceElement location = callerData[0];
                assertEquals("Correct location class.", getClass().getName(), location.getClassName());
                assertEquals("Correct location line.", currentLineNumber + 2 * lev + hasThrowable + 1, location.getLineNumber());
                final ThrowableProxy throwableProxy = (ThrowableProxy) event.getThrowableProxy();
                assertEquals("Correct exception", hasThrowable > 0 ? T : null, throwableProxy != null ? throwableProxy.getThrowable() : null);
            }
        }
    }
}
