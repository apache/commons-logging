/*
 * $Header: /home/jerenkrantz/tmp/commons/commons-convert/cvs/home/cvs/jakarta-commons//logging/src/java/org/apache/commons/logging/impl/Jdk13LumberjackLogger.java,v 1.3 2003/10/05 15:58:38 rdonkin Exp $
 * $Revision: 1.3 $
 * $Date: 2003/10/05 15:58:38 $
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


package org.apache.commons.logging.impl;


import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.LogRecord;
import java.util.StringTokenizer;
import java.io.PrintWriter;
import java.io.StringWriter;

import org.apache.commons.logging.Log;


/**
 * <p>Implementation of the <code>org.apache.commons.logging.Log</code>
 * interfaces that wraps the standard JDK logging mechanisms that are
 * available in SourceForge's Lumberjack for JDKs prior to 1.4.</p>
 *
 * @author <a href="mailto:sanders@apache.org">Scott Sanders</a>
 * @author <a href="mailto:bloritsch@apache.org">Berin Loritsch</a>
 * @author <a href="mailto:donaldp@apache.org">Peter Donald</a>
 * @author <a href="mailto:vince256@comcast.net">Vince Eagen</a>
 * @version $Revision: 1.3 $ $Date: 2003/10/05 15:58:38 $
 */

public class Jdk13LumberjackLogger implements Log, Serializable {


    // ----------------------------------------------------- Instance Variables


    /**
     * The underlying Logger implementation we are using.
     */
    protected transient Logger logger = null;
    protected String name = null;
    private String sourceClassName = "unknown";
    private String sourceMethodName = "unknown";
    private boolean classAndMethodFound = false;


    // ----------------------------------------------------------- Constructors


    /**
     * Construct a named instance of this Logger.
     *
     * @param name Name of the logger to be constructed
     */
    public Jdk13LumberjackLogger(String name) {

        this.name = name;
        logger = getLogger();

    }


    // --------------------------------------------------------- Public Methods


    private void log( Level level, String msg, Throwable ex ) {
        if( getLogger().isLoggable(level) ) {
            LogRecord record = new LogRecord(level, msg);
            if( !classAndMethodFound ) {
                getClassAndMethod();
            }
            record.setSourceClassName(sourceClassName);
            record.setSourceMethodName(sourceMethodName);
            if( ex != null ) {
                record.setThrown(ex);
            }
            getLogger().log(record);
        }
    }

    /**
     * <p>Gets the class and method by looking at the stack trace for the
     * first entry that is not this class.</p>
     */
    private void getClassAndMethod() {
        try {
            Throwable throwable = new Throwable();
            throwable.fillInStackTrace();
            StringWriter stringWriter = new StringWriter();
            PrintWriter printWriter = new PrintWriter( stringWriter );
            throwable.printStackTrace( printWriter );
            String traceString = stringWriter.getBuffer().toString();
            StringTokenizer tokenizer =
                new StringTokenizer( traceString, "\n" );
            tokenizer.nextToken();
            String line = tokenizer.nextToken();
            while ( line.indexOf( this.getClass().getName() )  == -1 ) {
                line = tokenizer.nextToken();
            }
            while ( line.indexOf( this.getClass().getName() ) >= 0 ) {
                line = tokenizer.nextToken();
            }
            int start = line.indexOf( "at " ) + 3;
            int end = line.indexOf( '(' );
            String temp = line.substring( start, end );
            int lastPeriod = temp.lastIndexOf( '.' );
            sourceClassName = temp.substring( 0, lastPeriod );
            sourceMethodName = temp.substring( lastPeriod + 1 );
        } catch ( Exception ex ) {
            // ignore - leave class and methodname unknown
        }
        classAndMethodFound = true;
    }

    /**
     * Log a message with debug log level.
     */
    public void debug(Object message) {
        log(Level.FINE, String.valueOf(message), null);
    }


    /**
     * Log a message and exception with debug log level.
     */
    public void debug(Object message, Throwable exception) {
        log(Level.FINE, String.valueOf(message), exception);
    }


    /**
     * Log a message with error log level.
     */
    public void error(Object message) {
        log(Level.SEVERE, String.valueOf(message), null);
    }


    /**
     * Log a message and exception with error log level.
     */
    public void error(Object message, Throwable exception) {
        log(Level.SEVERE, String.valueOf(message), exception);
    }


    /**
     * Log a message with fatal log level.
     */
    public void fatal(Object message) {
        log(Level.SEVERE, String.valueOf(message), null);
    }


    /**
     * Log a message and exception with fatal log level.
     */
    public void fatal(Object message, Throwable exception) {
        log(Level.SEVERE, String.valueOf(message), exception);
    }


    /**
     * Return the native Logger instance we are using.
     */
    public Logger getLogger() {
        if (logger == null) {
            logger = Logger.getLogger(name);
        }
        return (logger);
    }


    /**
     * Log a message with info log level.
     */
    public void info(Object message) {
        log(Level.INFO, String.valueOf(message), null);
    }


    /**
     * Log a message and exception with info log level.
     */
    public void info(Object message, Throwable exception) {
        log(Level.INFO, String.valueOf(message), exception);
    }


    /**
     * Is debug logging currently enabled?
     */
    public boolean isDebugEnabled() {
        return (getLogger().isLoggable(Level.FINE));
    }


    /**
     * Is error logging currently enabled?
     */
    public boolean isErrorEnabled() {
        return (getLogger().isLoggable(Level.SEVERE));
    }


    /**
     * Is fatal logging currently enabled?
     */
    public boolean isFatalEnabled() {
        return (getLogger().isLoggable(Level.SEVERE));
    }


    /**
     * Is info logging currently enabled?
     */
    public boolean isInfoEnabled() {
        return (getLogger().isLoggable(Level.INFO));
    }


    /**
     * Is tace logging currently enabled?
     */
    public boolean isTraceEnabled() {
        return (getLogger().isLoggable(Level.FINEST));
    }


    /**
     * Is warning logging currently enabled?
     */
    public boolean isWarnEnabled() {
        return (getLogger().isLoggable(Level.WARNING));
    }


    /**
     * Log a message with trace log level.
     */
    public void trace(Object message) {
        log(Level.FINEST, String.valueOf(message), null);
    }


    /**
     * Log a message and exception with trace log level.
     */
    public void trace(Object message, Throwable exception) {
        log(Level.FINEST, String.valueOf(message), exception);
    }


    /**
     * Log a message with warn log level.
     */
    public void warn(Object message) {
        log(Level.WARNING, String.valueOf(message), null);
    }


    /**
     * Log a message and exception with warn log level.
     */
    public void warn(Object message, Throwable exception) {
        log(Level.WARNING, String.valueOf(message), exception);
    }


}
