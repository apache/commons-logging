/*
 * Copyright 2005 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */ 

package org.apache.commons.logging.log4j;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Properties;

import junit.framework.TestCase;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.spi.LoggingEvent;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.impl.Log4J12Logger;


/**
 * Abstract set of tests that can be executed with various classpaths set.
 * <p>
 * The tests verify that when running on a system with Log4J present,
 * Log4J is selected and that the logger basically works.
 */

public abstract class StandardTests extends TestCase {

    // -------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------- 

    /**
     * The set of message strings that methods logPlainMessages and 
     * logExceptionMessages output when called.
     */
    private static final String TEST_MESSAGES[] = { 
        "info", "warn", "error", "fatal" 
    };

    /**
     * The message levels that the messages in TEST_MESSAGES are logged at.
     */
    private static final Level TEST_LEVELS[] = { 
        Level.INFO, Level.WARN, Level.ERROR, Level.FATAL 
    };

    // ------------------------------------------------------------------- 
    // JUnit Infrastructure Methods
    // ------------------------------------------------------------------- 

    /**
     * Set up instance variables required by this test case.
     */
    public void setUp() throws Exception {
        LogFactory.releaseAll();
    }

    /**
     * Tear down instance variables required by this test case.
     */
    public void tearDown() {
        LogFactory.releaseAll();
    }

    // ----------------------------------------------------------- Test Methods

    /**
     * Test that our test harness code works, ie that we are able to
     * configure log4j on the fly to write to an instance of TestAppender.
     */
    public void testAppender() throws Exception {
        setUpTestAppender();
        TestAppender testAppender = getTestAppender();
        assertNotNull("Appender exists", testAppender);
    }

    /**
     * Test that a LogFactory gets created as expected.
     */
    public void testCreateFactory() {
        LogFactory factory = LogFactory.getFactory();
        assertNotNull("LogFactory exists", factory);
        assertEquals("LogFactory class",
                     "org.apache.commons.logging.impl.LogFactoryImpl",
                     factory.getClass().getName());

        String names[] = factory.getAttributeNames();
        assertNotNull("Names exists", names);
        assertEquals("Names empty", 0, names.length);
    }

    /**
     * Test that a Log object gets created as expected.
     */
    public void testCreateLog() throws Exception {
        setUpTestAppender();
        Log log = LogFactory.getLog("test-category");
        
        // check that it is of the expected type, that we can access
        // the underlying real logger and that the logger level has
        // been set as expected after the call to setUpTestAppender.
        Log4J12Logger log4j12 = (Log4J12Logger) log;
        Logger logger = log4j12.getLogger();
        assertEquals("Logger name", "test-category", logger.getName());
        assertEquals("Logger level", Level.INFO, logger.getEffectiveLevel());
    }

    /**
     * Verify that we can log messages without exceptions.
     */
    public void testPlainMessages() throws Exception {
        setUpTestAppender();
        Log log = LogFactory.getLog("test-category");
        logPlainMessages(log);
        checkLoggingEvents(false);
    }

    /**
     * Verify that we can log exception messages.
     */
    public void testExceptionMessages() throws Exception {
        setUpTestAppender();
        Log log = LogFactory.getLog("test-category");
        logExceptionMessages(log);
        checkLoggingEvents(true);
    }

    /**
     * Test Serializability of Log instance
     */
    public void testSerializable() throws Exception {
        Log log = LogFactory.getLog("test-category");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(log);
        oos.close();
        ByteArrayInputStream bais =
            new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        Log newLog = (Log) ois.readObject();
        ois.close();

        // Check the characteristics of the resulting object
        logExceptionMessages(newLog);
        checkLoggingEvents(true);
    }

    // -------------------------------------------------------- Support Methods

    /**
     * Call log4j's PropertyConfigurator passing specific config info
     * in order to force log4j to create an instance of class TestAppender
     * and send all logged messages to that appender object.
     * <p>
     * The TestAppender class stores all its messages in memory, so we
     * can later check what messages it received from log4j.
     * <p>
     * This method also sets the logging level to INFO so that we
     * can test whether messages are getting properly filtered.
     */
    private void setUpTestAppender() throws Exception {
        Properties props = new Properties();
        props.put("log4j.rootLogger", "INFO, A1");
        props.put("log4j.appender.A1", "org.apache.commons.logging.log4j.TestAppender");
        PropertyConfigurator.configure(props);
    }

    /**
     * Get the custom TestAppender that has been set to recieve all
     * messages logged via log4j. It is presumed that method setUpTestAppender
     * has been called earlier to force a TestAppender to be used by log4j.
     */
    private TestAppender getTestAppender() {
        Enumeration appenders = Logger.getRootLogger().getAllAppenders();
        return (TestAppender) appenders.nextElement();
    }

    /**
     * Verify that the TestAppender has received the expected
     * number of messages. This assumes that:
     * <ul>
     * <li>setUpTestAppender has been called
     * <li>logPlainMessages or logExceptionMessages has been
     * called to log a known number of messages at known levels.
     * </ul>
     * 
     * @param thrown False if logPlainMessages was called
     * (ie the TestAppender is expected to have received
     * logevents with no associated exception info). True if
     * logExceptionMessages was called.
     */
    private void checkLoggingEvents(boolean thrown) {
        TestAppender appender = getTestAppender();
        Iterator events = appender.events();
        for (int i = 0; i < TEST_MESSAGES.length; i++) {
            assertTrue("Logged event " + i + " exists",events.hasNext());
            LoggingEvent event = (LoggingEvent) events.next();
            assertEquals("LoggingEvent level",
                         TEST_LEVELS[i], event.getLevel());
            assertEquals("LoggingEvent message",
                         TEST_MESSAGES[i], event.getMessage());

            if (thrown) {
                assertNotNull("LoggingEvent thrown",
                              event.getThrowableInformation().getThrowableStrRep());
                assertTrue("LoggingEvent thrown type",
                           event.getThrowableInformation()
                                .getThrowableStrRep()[0]
                                    .indexOf("IndexOutOfBoundsException")>0);
            } else {
                assertNull("LoggingEvent thrown",
                           event.getThrowableInformation());
            }
        }
        assertTrue(!events.hasNext());
        appender.flush();
    }


    /**
     * Log plain messages.
     */
    private void logPlainMessages(Log log) {
        log.trace("trace"); // Should not actually get logged
        log.debug("debug"); // Should not actually get logged
        log.info("info");
        log.warn("warn");
        log.error("error");
        log.fatal("fatal");
    }

    /*
     * Log messages with exceptions
     */
    private void logExceptionMessages(Log log) {
        Throwable t = new IndexOutOfBoundsException();
        log.trace("trace", t); // Should not actually get logged
        log.debug("debug", t); // Should not actually get logged
        log.info("info", t);
        log.warn("warn", t);
        log.error("error", t);
        log.fatal("fatal", t);
    }
}
