/*
 * $Header: /home/jerenkrantz/tmp/commons/commons-convert/cvs/home/cvs/jakarta-commons//logging/src/test/org/apache/commons/logging/Attic/TestLogLevels.java,v 1.1 2002/01/03 18:49:27 rdonkin Exp $
 * $Revision: 1.1 $
 * $Date: 2002/01/03 18:49:27 $
 *
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1999-2001 The Apache Software Foundation.  All rights
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
 */
 
 
package org.apache.commons.logging;

import junit.framework.*;

/**
  * <p> 
  *
  * @author Robert Burrell Donkin
  * @version $Revision: 1.1 $
 */
public class TestLogLevels extends TestCase {

    public static Test suite() {
        return new TestSuite(TestLogLevels.class);
    }
    
    public TestLogLevels(String testName) {
        super(testName);
    }

    /**
     * Test that <code>AbstractLog</code> correctly passes
     * on calls as per current log level.
     */ 
    public void testAbstractLogLevelCheck() {
    
        TestLog log = new TestLog();
        
        // test OFF log level
        log.setLevel(Log.OFF);
        
        log.setMessage(null);
        log.debug("DEBUG");
        assertNull(
            "Log level checking failed (DEBUG/OFF)",
            log.getMessage());
            
        log.setMessage(null);
        log.info("INFO");
        assertNull(
            "Log level checking failed (INFO/OFF)",
            log.getMessage());

        log.setMessage(null);
        log.error("ERROR");
        assertNull(
            "Log level checking failed (ERROR/OFF)",
            log.getMessage());

        log.setMessage(null);
        log.fatal("FATAL");
        assertNull(
            "Log level checking failed (FATAL/OFF)",
            log.getMessage());
        
        
        // test ALL log level
        log.setLevel(Log.ALL);
        
        log.setMessage(null);
        log.debug("DEBUG");
        assertEquals(
            "Log level checking failed (DEBUG/ALL)",
            "DEBUG",
            log.getMessage());
            
        log.setMessage(null);
        log.info("INFO");
        assertEquals(
            "Log level checking failed (INFO/ALL)",
            "INFO",
            log.getMessage());

        log.setMessage(null);
        log.error("ERROR");
        assertEquals(
            "Log level checking failed (ERROR/ALL)",
            "ERROR",
            log.getMessage());

        log.setMessage(null);
        log.fatal("FATAL");
        assertEquals(
            "Log level checking failed (FATAL/ALL)",
            "FATAL",
            log.getMessage());
        
        
        // test DEBUG log level
        log.setLevel(Log.DEBUG);
        
        log.setMessage(null);
        log.debug("DEBUG");
        assertEquals(
            "Log level checking failed (DEBUG/DEBUG)",
            "DEBUG",
            log.getMessage());
            
        log.setMessage(null);
        log.info("INFO");
        assertEquals(
            "Log level checking failed (INFO/DEBUG)",
            "INFO",
            log.getMessage());

        log.setMessage(null);
        log.error("ERROR");
        assertEquals(
            "Log level checking failed (ERROR/DEBUG)",
            "ERROR",
            log.getMessage());

        log.setMessage(null);
        log.fatal("FATAL");
        assertEquals(
            "Log level checking failed (FATAL/DEBUG)",
            "FATAL",
            log.getMessage());


        // test INFO log level
        log.setLevel(Log.INFO);
        
        log.setMessage(null);
        log.debug("DEBUG");
        assertNull(
            "Log level checking failed (DEBUG/INFO)",
            log.getMessage());
            
        log.setMessage(null);
        log.info("INFO");
        assertEquals(
            "Log level checking failed (INFO/INFO)",
            "INFO",
            log.getMessage());

        log.setMessage(null);
        log.error("ERROR");
        assertEquals(
            "Log level checking failed (ERROR/INFO)",
            "ERROR",
            log.getMessage());

        log.setMessage(null);
        log.fatal("FATAL");
        assertEquals(
            "Log level checking failed (FATAL/INFO)",
            "FATAL",
            log.getMessage());      

        // test ERROR log level
        log.setLevel(Log.ERROR);
        
        log.setMessage(null);
        log.debug("DEBUG");
        assertNull(
            "Log level checking failed (DEBUG/ERROR)",
            log.getMessage());
            
        log.setMessage(null);
        log.info("INFO");
        assertNull(
            "Log level checking failed (INFO/ERROR)",
            log.getMessage());

        log.setMessage(null);
        log.error("ERROR");
        assertEquals(
            "Log level checking failed (ERROR/ERROR)",
            "ERROR",
            log.getMessage());

        log.setMessage(null);
        log.fatal("FATAL");
        assertEquals(
            "Log level checking failed (FATAL/ERROR)",
            "FATAL",
            log.getMessage());
            
        // test FATAL log level
        log.setLevel(Log.FATAL);
        
        log.setMessage(null);
        log.debug("DEBUG");
        assertNull(
            "Log level checking failed (DEBUG/FATAL)",
            log.getMessage());
            
        log.setMessage(null);
        log.info("INFO");
        assertNull(
            "Log level checking failed (INFO/FATAL)",
            log.getMessage());

        log.setMessage(null);
        log.error("ERROR");
        assertNull(
            "Log level checking failed (ERROR/FATAL)",
            log.getMessage());

        log.setMessage(null);
        log.fatal("FATAL");
        assertEquals(
            "Log level checking failed (FATAL/FATAL)",
            "FATAL",
            log.getMessage());

    }
}

