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

package org.apache.commons.logging.impl;

import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.apache.commons.logging.Log;

/**
 * Implementation of the {@code org.apache.commons.logging.Log}
 * interface that wraps the standard JDK logging mechanisms that are
 * available in SourceForge's Lumberjack for JDKs prior to 1.4.
 *
 * @since 1.1
 * @deprecated Scheduled for removal because the Lumberjack Project has been discontinued.
 */
@Deprecated
public class Jdk13LumberjackLogger implements Log, Serializable {

    /** Serializable version identifier. */
    private static final long serialVersionUID = -8649807923527610591L;


    /**
     * This member variable simply ensures that any attempt to initialize
     * this class in a pre-1.4 JVM will result in an ExceptionInInitializerError.
     * It must not be private, as an optimising compiler could detect that it
     * is not used and optimise it away.
     */
    protected static final Level dummyLevel = Level.FINE;

    /**
     * The underlying Logger implementation we are using.
     */
    protected transient Logger logger;

    /**
     * Name.
     */
    protected String name;

    /** Source class name. */
    private String sourceClassName = "unknown";

    /** Source method name. */
    private String sourceMethodName = "unknown";

    /** Class and method found flag. */
    private boolean classAndMethodFound;


    /**
     * Constructs a named instance of this Logger.
     *
     * @param name Name of the logger to be constructed
     */
    public Jdk13LumberjackLogger(final String name) {
        this.name = name;
        logger = getLogger();
    }


    /**
     * Logs a message with {@code java.util.logging.Level.FINE}.
     *
     * @param message to log
     * @see org.apache.commons.logging.Log#debug(Object)
     */
    @Override
    public void debug(final Object message) {
        log(Level.FINE, String.valueOf(message), null);
    }

    /**
     * Logs a message with {@code java.util.logging.Level.FINE}.
     *
     * @param message to log
     * @param exception log this cause
     * @see org.apache.commons.logging.Log#debug(Object, Throwable)
     */
    @Override
    public void debug(final Object message, final Throwable exception) {
        log(Level.FINE, String.valueOf(message), exception);
    }

    /**
     * Logs a message with {@code java.util.logging.Level.SEVERE}.
     *
     * @param message to log
     * @see org.apache.commons.logging.Log#error(Object)
     */
    @Override
    public void error(final Object message) {
        log(Level.SEVERE, String.valueOf(message), null);
    }

    /**
     * Logs a message with {@code java.util.logging.Level.SEVERE}.
     *
     * @param message to log
     * @param exception log this cause
     * @see org.apache.commons.logging.Log#error(Object, Throwable)
     */
    @Override
    public void error(final Object message, final Throwable exception) {
        log(Level.SEVERE, String.valueOf(message), exception);
    }

    /**
     * Logs a message with {@code java.util.logging.Level.SEVERE}.
     *
     * @param message to log
     * @see org.apache.commons.logging.Log#fatal(Object)
     */
    @Override
    public void fatal(final Object message) {
        log(Level.SEVERE, String.valueOf(message), null);
    }

    /**
     * Logs a message with {@code java.util.logging.Level.SEVERE}.
     *
     * @param message to log
     * @param exception log this cause
     * @see org.apache.commons.logging.Log#fatal(Object, Throwable)
     */
    @Override
    public void fatal(final Object message, final Throwable exception) {
        log(Level.SEVERE, String.valueOf(message), exception);
    }

    /**
     * Gets the class and method by looking at the stack trace for the
     * first entry that is not this class.
     */
    private void getClassAndMethod() {
        try {
            final Throwable throwable = new Throwable();
            throwable.fillInStackTrace();
            final StringWriter stringWriter = new StringWriter();
            final PrintWriter printWriter = new PrintWriter( stringWriter );
            throwable.printStackTrace( printWriter );
            final String traceString = stringWriter.getBuffer().toString();
            final StringTokenizer tokenizer =
                new StringTokenizer( traceString, "\n" );
            tokenizer.nextToken();
            String line = tokenizer.nextToken();
            while (!line.contains(this.getClass().getName())) {
                line = tokenizer.nextToken();
            }
            while (line.contains(this.getClass().getName())) {
                line = tokenizer.nextToken();
            }
            final int start = line.indexOf( "at " ) + 3;
            final int end = line.indexOf( '(' );
            final String temp = line.substring( start, end );
            final int lastPeriod = temp.lastIndexOf( '.' );
            sourceClassName = temp.substring( 0, lastPeriod );
            sourceMethodName = temp.substring( lastPeriod + 1 );
        } catch ( final Exception ex ) {
            // ignore - leave class and methodname unknown
        }
        classAndMethodFound = true;
    }

    /**
     * Return the native Logger instance we are using.
     *
     * @return the native Logger instance we are using.
     */
    public Logger getLogger() {
        if (logger == null) {
            logger = Logger.getLogger(name);
        }
        return logger;
    }

    /**
     * Logs a message with {@code java.util.logging.Level.INFO}.
     *
     * @param message to log
     * @see org.apache.commons.logging.Log#info(Object)
     */
    @Override
    public void info(final Object message) {
        log(Level.INFO, String.valueOf(message), null);
    }

    /**
     * Logs a message with {@code java.util.logging.Level.INFO}.
     *
     * @param message to log
     * @param exception log this cause
     * @see org.apache.commons.logging.Log#info(Object, Throwable)
     */
    @Override
    public void info(final Object message, final Throwable exception) {
        log(Level.INFO, String.valueOf(message), exception);
    }

    /**
     * Is debug logging currently enabled?
     */
    @Override
    public boolean isDebugEnabled() {
        return getLogger().isLoggable(Level.FINE);
    }

    /**
     * Is error logging currently enabled?
     */
    @Override
    public boolean isErrorEnabled() {
        return getLogger().isLoggable(Level.SEVERE);
    }

    /**
     * Is fatal logging currently enabled?
     */
    @Override
    public boolean isFatalEnabled() {
        return getLogger().isLoggable(Level.SEVERE);
    }

    /**
     * Is info logging currently enabled?
     */
    @Override
    public boolean isInfoEnabled() {
        return getLogger().isLoggable(Level.INFO);
    }

    /**
     * Is trace logging currently enabled?
     */
    @Override
    public boolean isTraceEnabled() {
        return getLogger().isLoggable(Level.FINEST);
    }

    /**
     * Is warn logging currently enabled?
     */
    @Override
    public boolean isWarnEnabled() {
        return getLogger().isLoggable(Level.WARNING);
    }

    private void log( final Level level, final String msg, final Throwable ex ) {
        if ( getLogger().isLoggable(level) ) {
            final LogRecord record = new LogRecord(level, msg);
            if ( !classAndMethodFound ) {
                getClassAndMethod();
            }
            record.setSourceClassName(sourceClassName);
            record.setSourceMethodName(sourceMethodName);
            if ( ex != null ) {
                record.setThrown(ex);
            }
            getLogger().log(record);
        }
    }

    /**
     * Logs a message with {@code java.util.logging.Level.FINEST}.
     *
     * @param message to log
     * @see org.apache.commons.logging.Log#trace(Object)
     */
    @Override
    public void trace(final Object message) {
        log(Level.FINEST, String.valueOf(message), null);
    }

    /**
     * Logs a message with {@code java.util.logging.Level.FINEST}.
     *
     * @param message to log
     * @param exception log this cause
     * @see org.apache.commons.logging.Log#trace(Object, Throwable)
     */
    @Override
    public void trace(final Object message, final Throwable exception) {
        log(Level.FINEST, String.valueOf(message), exception);
    }

    /**
     * Logs a message with {@code java.util.logging.Level.WARNING}.
     *
     * @param message to log
     * @see org.apache.commons.logging.Log#warn(Object)
     */
    @Override
    public void warn(final Object message) {
        log(Level.WARNING, String.valueOf(message), null);
    }

    /**
     * Logs a message with {@code java.util.logging.Level.WARNING}.
     *
     * @param message to log
     * @param exception log this cause
     * @see org.apache.commons.logging.Log#warn(Object, Throwable)
     */
    @Override
    public void warn(final Object message, final Throwable exception) {
        log(Level.WARNING, String.valueOf(message), exception);
    }
}
