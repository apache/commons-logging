/*
 * $Header: /home/jerenkrantz/tmp/commons/commons-convert/cvs/home/cvs/jakarta-commons//logging/src/test/org/apache/commons/logging/simple/CustomConfigTestCase.java,v 1.2 2003/10/05 15:57:17 rdonkin Exp $
 * $Revision: 1.2 $
 * $Date: 2003/10/05 15:57:17 $
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
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
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

package org.apache.commons.logging.simple;


import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.commons.logging.impl.SimpleLog;


/**
 * <p>TestCase for sipmle logging when running with custom configuration
 * properties.</p>
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.2 $ $Date: 2003/10/05 15:57:17 $
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
     * <p>The expected log records.</p>
     */
    protected List expected;


    /**
     * <p>The message levels that should have been logged.</p>
     */
    /*
    protected Level testLevels[] =
    { Level.FINE, Level.INFO, Level.WARNING, Level.SEVERE, Level.SEVERE };
    */


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
        expected = new ArrayList();
        setUpFactory();
        setUpLog("DecoratedLogger");
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
        expected = null;
    }


    // ----------------------------------------------------------- Test Methods


    // Test logging message strings with exceptions
    public void testExceptionMessages() throws Exception {

        ((DecoratedSimpleLog) log).clearCache();
        logExceptionMessages();
        checkExpected();

    }


    // Test logging plain message strings
    public void testPlainMessages() throws Exception {

        ((DecoratedSimpleLog) log).clearCache();
        logPlainMessages();
        checkExpected();

    }


    // Test Serializability of standard instance
    public void testSerializable() throws Exception {

        ((DecoratedSimpleLog) log).clearCache();
        logPlainMessages();
        super.testSerializable();
        logExceptionMessages();
        checkExpected();

    }


    // -------------------------------------------------------- Support Methods


    // Check the decorated log instance
    protected void checkDecorated() {

        assertNotNull("Log exists", log);
        assertEquals("Log class",
                     "org.apache.commons.logging.simple.DecoratedSimpleLog",
                     log.getClass().getName());

        // Can we call level checkers with no exceptions?
        assertTrue(log.isDebugEnabled());
        assertTrue(log.isErrorEnabled());
        assertTrue(log.isFatalEnabled());
        assertTrue(log.isInfoEnabled());
        assertTrue(!log.isTraceEnabled());
        assertTrue(log.isWarnEnabled());

        // Can we retrieve the current log level?
        assertEquals(SimpleLog.LOG_LEVEL_DEBUG, ((SimpleLog) log).getLevel());

        // Can we validate the extra exposed properties?
        assertEquals("DecoratedLogger",
                     ((DecoratedSimpleLog) log).getLogName());
        assertTrue(!((DecoratedSimpleLog) log).getShowDateTime());
        assertTrue(((DecoratedSimpleLog) log).getShowShortName());

    }


    // Check the actual log records against the expected ones
    protected void checkExpected() {

        List acts = ((DecoratedSimpleLog) log).getCache();
        Iterator exps = expected.iterator();
        int n = 0;
        while (exps.hasNext()) {
            LogRecord exp = (LogRecord) exps.next();
            LogRecord act = (LogRecord) acts.get(n++);
            assertEquals("Row " + n + " type", exp.type, act.type);
            assertEquals("Row " + n + " message", exp.message, act.message);
            assertEquals("Row " + n + " throwable", exp.t, act.t);
        }

    }


    // Check the standard log instance
    protected void checkStandard() {

        checkDecorated();

    }


    // Log the messages with exceptions
    protected void logExceptionMessages() {

        // Generate log records
        Throwable t = new IndexOutOfBoundsException();
        log.trace("trace", t); // Should not actually get logged
        log.debug("debug", t);
        log.info("info", t);
        log.warn("warn", t);
        log.error("error", t);
        log.fatal("fatal", t);

        // Record the log records we expect
        expected.add(new LogRecord(SimpleLog.LOG_LEVEL_DEBUG, "debug", t));
        expected.add(new LogRecord(SimpleLog.LOG_LEVEL_INFO, "info", t));
        expected.add(new LogRecord(SimpleLog.LOG_LEVEL_WARN, "warn", t));
        expected.add(new LogRecord(SimpleLog.LOG_LEVEL_ERROR, "error", t));
        expected.add(new LogRecord(SimpleLog.LOG_LEVEL_FATAL, "fatal", t));

    }


    // Log the plain messages
    protected void logPlainMessages() {

        // Generate log records
        log.trace("trace"); // Should not actually get logged
        log.debug("debug");
        log.info("info");
        log.warn("warn");
        log.error("error");
        log.fatal("fatal");

        // Record the log records we expect
        expected.add(new LogRecord(SimpleLog.LOG_LEVEL_DEBUG, "debug", null));
        expected.add(new LogRecord(SimpleLog.LOG_LEVEL_INFO, "info", null));
        expected.add(new LogRecord(SimpleLog.LOG_LEVEL_WARN, "warn", null));
        expected.add(new LogRecord(SimpleLog.LOG_LEVEL_ERROR, "error", null));
        expected.add(new LogRecord(SimpleLog.LOG_LEVEL_FATAL, "fatal", null));

    }


}
