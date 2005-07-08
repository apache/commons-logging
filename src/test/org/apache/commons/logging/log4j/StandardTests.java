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

import java.util.List;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Properties;

import junit.framework.TestCase;

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

    /**
     * Simple structure to store information about messages that actually get
     * logged by the underlying logging library.
     */
    public static class LogEvent {
        public String msg;
        public String level;
        public Throwable throwable;
    }
    
    /**
     * Simple helper class that can configure log4j to redirect all logged
     * messages into a list of LogEvent messages.
     * <p>
     * The TestCase classes that junit will run later have two roles: they
     * hold the tests to run, and they also provide the suite() method that
     * indicates which tests to run. This causes complications for us in the
     * case of log4j because of the binary-incompatible log4j versions. We 
     * can't have any version of log4j to be in the classpath until we are
     * actually running the tests returned by suite() - but junit can't load
     * the class to call suite() on it if the class or any of its ancestors
     * have direct references to log4j APIs (or NoClassDefFound occurs).
     * <p>
     * The answer is to move all the direct log4j calls out of the TestCase
     * classes into a helper which is only loaded via reflection during the
     * test runs (and not during calls to suite()). This class defines the
     * interface required of that helper.
     * <p>
     * See also method getTestHelperClassName.  
     */

    public static interface TestHelper {
        public void forwardMessages(List logEvents);
    }

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

    // ----------------------------------------------------------- 
    // abstract methods
    // ----------------------------------------------------------- 

    protected abstract String getTestHelperClassName();

    // ----------------------------------------------------------- Test Methods

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
     * Verify that we can log messages without exceptions.
     */
    public void testPlainMessages() throws Exception {
        List logEvents = new ArrayList();
        setUpTestAppender(logEvents);
        Log log = LogFactory.getLog("test-category");
        logPlainMessages(log);
        checkLoggingEvents(logEvents, false);
    }

    /**
     * Verify that we can log exception messages.
     */
    public void testExceptionMessages() throws Exception {
        List logEvents = new ArrayList();
        setUpTestAppender(logEvents);
        Log log = LogFactory.getLog("test-category");
        logExceptionMessages(log);
        checkLoggingEvents(logEvents, true);
    }

    /**
     * Test Serializability of Log instance
     */
    public void testSerializable() throws Exception {
        List logEvents = new ArrayList();
        setUpTestAppender(logEvents);
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
        checkLoggingEvents(logEvents, true);
    }

    // -------------------------------------------------------- Support Methods

    /**
     * Modify log4j's setup so that all messages actually logged get redirected
     * into the specified list.
     * <p>
     * This method also sets the logging level to INFO so that we
     * can test whether messages are getting properly filtered.
     */
    private void setUpTestAppender(List logEvents) throws Exception {
        String testHelperClassName = getTestHelperClassName();
        Class clazz = this.getClass().getClassLoader().loadClass(testHelperClassName);
        TestHelper testHelper = (TestHelper) clazz.newInstance();
        testHelper.forwardMessages(logEvents);
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
     * @param logEvents is the list of log events received.
     * 
     * @param thrown False if logPlainMessages was called
     * (ie the TestAppender is expected to have received
     * logevents with no associated exception info). True if
     * logExceptionMessages was called.
     */
    private void checkLoggingEvents(List logEvents, boolean thrown) {
        LogEvent ev;
        
        assertEquals("Unexpected number of log events", 4, logEvents.size());
        
        ev = (LogEvent) logEvents.get(0);
        assertEquals("Info message expected", "info", ev.msg);
        assertEquals("Info level expected", "INFO", ev.level);
        assertEquals("Exception data incorrect", (ev.throwable!=null), thrown);
        
        ev = (LogEvent) logEvents.get(1);
        assertEquals("Warn message expected", "warn", ev.msg);
        assertEquals("Warn level expected", "WARN", ev.level);
        assertEquals("Exception data incorrect", (ev.throwable!=null), thrown);
        
        ev = (LogEvent) logEvents.get(2);
        assertEquals("Error message expected", "error", ev.msg);
        assertEquals("Error level expected", "ERROR", ev.level);
        assertEquals("Exception data incorrect", (ev.throwable!=null), thrown);
        
        ev = (LogEvent) logEvents.get(3);
        assertEquals("Fatal message expected", "fatal", ev.msg);
        assertEquals("Fatal level expected", "FATAL", ev.level);
        assertEquals("Exception data incorrect", (ev.throwable!=null), thrown);
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

    /**
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
