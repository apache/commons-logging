/*
 * $Header: /home/jerenkrantz/tmp/commons/commons-convert/cvs/home/cvs/jakarta-commons//logging/src/test/org/apache/commons/logging/log4j/CustomConfigTestCase.java,v 1.4 2003/07/18 14:11:45 rsitze Exp $
 * $Revision: 1.4 $
 * $Date: 2003/07/18 14:11:45 $
 *
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1999-2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Commons", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */

package org.apache.commons.logging.log4j;


import java.io.InputStream;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Properties;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.spi.LoggingEvent;


/**
 * <p>TestCase for Log4J logging when running on a system with Log4J present,
 * so that Log4J should be selected and an appropriate
 * logger configured per the configuration properties.</p>
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.4 $ $Date: 2003/07/18 14:11:45 $
 */

public class CustomConfigTestCase extends DefaultConfigTestCase {


    // ----------------------------------------------------------- Constructors


    /**
     * <p>Construct a new instance of this test case.</p>
     *
     * @param name Name of the test case
     */
    public CustomConfigTestCase(String name) {
        super(name);
    }


    // ----------------------------------------------------- Instance Variables


    /**
     * <p>The <code>Appender</code> we are utilizing.</p>
     */
    protected TestAppender appender = null;


    /**
     * <p>The <code>Logger</code> we are utilizing.</p>
     */
    protected Logger logger = null;


    /**
     * <p>The message levels that should have been logged.</p>
     */
    protected Level testLevels[] =
    { Level.INFO, Level.WARN, Level.ERROR, Level.FATAL };


    /**
     * <p>The message strings that should have been logged.</p>
     */
    protected String testMessages[] =
    { "info", "warn", "error", "fatal" };


    // ------------------------------------------- JUnit Infrastructure Methods


    /**
     * Set up instance variables required by this test case.
     */
    public void setUp() throws Exception {
        setUpAppender
            ("org/apache/commons/logging/log4j/CustomConfig.properties");
        setUpLogger("TestLogger");
        setUpFactory();
        setUpLog("TestLogger");
    }


    /**
     * Return the tests included in this test suite.
     */
    public static Test suite() {
        return (new TestSuite(CustomConfigTestCase.class));
    }

    /**
     * Tear down instance variables required by this test case.
     */
    public void tearDown() {
        super.tearDown();
        Logger.getRootLogger().removeAppender(appender);
        appender = null;
        logger = null;
    }


    // ----------------------------------------------------------- Test Methods


    // Test logging message strings with exceptions
    public void testExceptionMessages() throws Exception {

        logExceptionMessages();
        checkLoggingEvents(true);

    }


    // Test logging plain message strings
    public void testPlainMessages() throws Exception {

        logPlainMessages();
        checkLoggingEvents(false);

    }


    // Test pristine Appender instance
    public void testPristineAppender() {

        assertNotNull("Appender exists", appender);

    }


    // Test pristine Log instance
    public void testPristineLog() {

        super.testPristineLog();

        // Assert which logging levels have been enabled
        assertTrue(log.isErrorEnabled());
        assertTrue(log.isWarnEnabled());
        assertTrue(log.isInfoEnabled());
        assertTrue(!log.isDebugEnabled());
        assertTrue(!log.isTraceEnabled());

    }


    // Test pristine Logger instance
    public void testPristineLogger() {

        assertNotNull("Logger exists", logger);
        assertEquals("Logger level", Level.INFO, logger.getEffectiveLevel());
        assertEquals("Logger name", "TestLogger", logger.getName());

    }


    // -------------------------------------------------------- Support Methods


    // Check the recorded messages
    protected void checkLoggingEvents(boolean thrown) {
        Iterator events = appender.events();
        for (int i = 0; i < testMessages.length; i++) {
            assertTrue("Logged event " + i + " exists",events.hasNext());
            LoggingEvent event = (LoggingEvent) events.next();
            assertEquals("LoggingEvent level",
                         testLevels[i], event.getLevel());
            assertEquals("LoggingEvent message",
                         testMessages[i], event.getMessage());
            /* Does not appear to be logged correctly?
            assertEquals("LoggingEvent class",
                         this.getClass().getName(),
                         event.getLocationInformation().getClassName());
            */
            /* Does not appear to be logged correctly?
            if (thrown) {
                assertEquals("LoggingEvent method",
                             "logExceptionMessages",
                             event.getLocationInformation().getMethodName());
            } else {
                assertEquals("LoggingEvent method",
                             "logPlainMessages",
                             event.getLocationInformation().getMethodName());
            }
            */
            if (thrown) {
                assertNotNull("LoggingEvent thrown",
                              event.getThrowableInformation().getThrowable());
                assertTrue("LoggingEvent thrown type",
                           event.getThrowableInformation().getThrowable()
                             instanceof IndexOutOfBoundsException);
            } else {
                assertNull("LoggingEvent thrown",
                           event.getThrowableInformation());
            }
        }
        assertTrue(!events.hasNext());
        appender.flush();
    }


    // Log the messages with exceptions
    protected void logExceptionMessages() {
        Throwable t = new IndexOutOfBoundsException();
        log.trace("trace", t); // Should not actually get logged
        log.debug("debug", t); // Should not actually get logged
        log.info("info", t);
        log.warn("warn", t);
        log.error("error", t);
        log.fatal("fatal", t);
    }


    // Log the plain messages
    protected void logPlainMessages() {
        log.trace("trace"); // Should not actually get logged
        log.debug("debug"); // Should not actually get logged
        log.info("info");
        log.warn("warn");
        log.error("error");
        log.fatal("fatal");
    }


    // Set up our custom Appender
    protected void setUpAppender(String config) throws Exception {
        Properties props = new Properties();
        InputStream is =
            this.getClass().getClassLoader().getResourceAsStream(config);
        props.load(is);
        is.close();
        PropertyConfigurator.configure(props);
        Enumeration appenders = Logger.getRootLogger().getAllAppenders();
        appender = (TestAppender) appenders.nextElement();
    }


    // Set up our custom Logger
    protected void setUpLogger(String name) throws Exception {
        logger = Logger.getLogger(name);
    }


}
