/*
 * $Header: /home/jerenkrantz/tmp/commons/commons-convert/cvs/home/cvs/jakarta-commons//logging/src/test/org/apache/commons/logging/jdk14/CustomConfigTestCase.java,v 1.8 2003/10/09 21:37:47 rdonkin Exp $
 * $Revision: 1.8 $
 * $Date: 2003/10/09 21:37:47 $
 *
 * ====================================================================
 * 
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001-2003 The Apache Software Foundation.  All rights
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
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgement:  
 *       "This product includes software developed by the 
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgement may appear in the software itself,
 *    if and wherever such third-party acknowledgements normally appear.
 *
 * 4. The names "Apache", "The Jakarta Project", "Commons", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written 
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache" nor may "Apache" appear in their names without prior 
 *    written permission of the Apache Software Foundation.
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

package org.apache.commons.logging.jdk14;


import java.io.InputStream;
import java.util.Iterator;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import junit.framework.Test;
import junit.framework.TestSuite;


/**
 * <p>TestCase for JDK 1.4 logging when running on a JDK 1.4 system with
 * custom configuration, so that JDK 1.4 should be selected and an appropriate
 * logger configured per the configuration properties.</p>
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.8 $ $Date: 2003/10/09 21:37:47 $
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
     * <p>The customized <code>Handler</code> we will be using.</p>
     */
    protected TestHandler handler = null;


    /**
     * <p>The underlying <code>Handler</code>s we will be using.</p>
     */
    protected Handler handlers[] = null;


    /**
     * <p>The underlying <code>Logger</code> we will be using.</p>
     */
    protected Logger logger = null;


    /**
     * <p>The underlying <code>LogManager</code> we will be using.</p>
     */
    protected LogManager manager = null;


    /**
     * <p>The message levels that should have been logged.</p>
     */
    protected Level testLevels[] =
    { Level.FINE, Level.INFO, Level.WARNING, Level.SEVERE, Level.SEVERE };


    /**
     * <p>The message strings that should have been logged.</p>
     */
    protected String testMessages[] =
    { "debug", "info", "warn", "error", "fatal" };


    // ------------------------------------------- JUnit Infrastructure Methods


    /**
     * Set up instance variables required by this test case.
     */
    public void setUp() throws Exception {
        setUpManager
            ("org/apache/commons/logging/jdk14/CustomConfig.properties");
        setUpLogger("TestLogger");
        setUpHandlers();
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
        handlers = null;
        logger = null;
        manager = null;
    }


    // ----------------------------------------------------------- Test Methods


    // Test logging message strings with exceptions
    public void testExceptionMessages() throws Exception {

        logExceptionMessages();
        checkLogRecords(true);

    }


    // Test logging plain message strings
    public void testPlainMessages() throws Exception {

        logPlainMessages();
        checkLogRecords(false);

    }


    // Test pristine Handlers instances
    public void testPristineHandlers() {

        assertNotNull(handlers);
        assertEquals(1, handlers.length);
        assertTrue(handlers[0] instanceof TestHandler);
        assertNotNull(handler);

    }


    // Test pristine Logger instance
    public void testPristineLogger() {

        assertNotNull("Logger exists", logger);
        assertEquals("Logger name", "TestLogger", logger.getName());

        // Assert which logging levels have been enabled
        assertTrue(logger.isLoggable(Level.SEVERE));
        assertTrue(logger.isLoggable(Level.WARNING));
        assertTrue(logger.isLoggable(Level.INFO));
        assertTrue(logger.isLoggable(Level.CONFIG));
        assertTrue(logger.isLoggable(Level.FINE));
        assertTrue(!logger.isLoggable(Level.FINER));
        assertTrue(!logger.isLoggable(Level.FINEST));

    }


    // Test Serializability of Log instance
    public void testSerializable() throws Exception {

        super.testSerializable();
        testExceptionMessages();

    }


    // -------------------------------------------------------- Support Methods


    // Check the log instance
    protected void checkLog() {

        assertNotNull("Log exists", log);
        assertEquals("Log class",
                     "org.apache.commons.logging.impl.Jdk14Logger",
                     log.getClass().getName());

        // Assert which logging levels have been enabled
        assertTrue(log.isFatalEnabled());
        assertTrue(log.isErrorEnabled());
        assertTrue(log.isWarnEnabled());
        assertTrue(log.isInfoEnabled());
        assertTrue(log.isDebugEnabled());
        assertTrue(!log.isTraceEnabled());

    }


    // Check the recorded messages
    protected void checkLogRecords(boolean thrown) {
        Iterator records = handler.records();
        for (int i = 0; i < testMessages.length; i++) {
            assertTrue(records.hasNext());
            LogRecord record = (LogRecord) records.next();
            assertEquals("LogRecord level",
                         testLevels[i], record.getLevel());
            assertEquals("LogRecord message",
                         testMessages[i], record.getMessage());
            assertEquals("LogRecord class",
                         this.getClass().getName(),
                         record.getSourceClassName());
            if (thrown) {
                assertEquals("LogRecord method",
                             "logExceptionMessages",
                             record.getSourceMethodName());
            } else {
                assertEquals("LogRecord method",
                             "logPlainMessages",
                             record.getSourceMethodName());
            }
            if (thrown) {
                assertNotNull("LogRecord thrown", record.getThrown());
                assertTrue("LogRecord thrown type",
                           record.getThrown() instanceof IndexOutOfBoundsException);
            } else {
                assertNull("LogRecord thrown",
                           record.getThrown());
            }
        }
        assertTrue(!records.hasNext());
        handler.flush();
    }


    // Log the messages with exceptions
    protected void logExceptionMessages() {
        Throwable t = new IndexOutOfBoundsException();
        log.trace("trace", t); // Should not actually get logged
        log.debug("debug", t);
        log.info("info", t);
        log.warn("warn", t);
        log.error("error", t);
        log.fatal("fatal", t);
    }


    // Log the plain messages
    protected void logPlainMessages() {
        log.trace("trace"); // Should not actually get logged
        log.debug("debug");
        log.info("info");
        log.warn("warn");
        log.error("error");
        log.fatal("fatal");
    }


    // Set up handlers instance
    protected void setUpHandlers() throws Exception {
        Logger parent = logger;
        while (parent.getParent() != null) {
            parent = parent.getParent();
        }
        handlers = parent.getHandlers();
        if ((handlers != null) && (handlers.length == 1) &&
            (handlers[0] instanceof TestHandler)) {
            handler = (TestHandler) handlers[0];
        }
    }


    // Set up logger instance
    protected void setUpLogger(String name) throws Exception {
        logger = Logger.getLogger(name);
    }


    // Set up LogManager instance
    protected void setUpManager(String config) throws Exception {
        manager = LogManager.getLogManager();
        InputStream is =
            this.getClass().getClassLoader().getResourceAsStream(config);
        manager.readConfiguration(is);
        is.close();
    }


}
