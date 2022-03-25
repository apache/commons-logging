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

import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.logging.Log;

/**
 * Implementation of the {@code org.apache.commons.logging.Log}
 * interface that wraps the standard JDK logging mechanisms that were
 * introduced in the Merlin release (JDK 1.4).
 *
 */
public class Jdk14Logger implements Log, Serializable {

    /** Serializable version identifier. */
    private static final long serialVersionUID = 4784713551416303804L;

    // ----------------------------------------------------------- Constructors

    /**
     * Construct a named instance of this Logger.
     *
     * @param name Name of the logger to be constructed
     */
    public Jdk14Logger(final String name) {
        this.name = name;
        logger = getLogger();
    }

    // ----------------------------------------------------- Instance Variables

    /**
     * The underlying Logger implementation we are using.
     */
    protected transient Logger logger;

    /**
     * The name of the logger we are wrapping.
     */
    protected String name;

    // --------------------------------------------------------- Protected Methods

    protected void log( final Level level, final String msg, final Throwable ex ) {
        final Logger logger = getLogger();
        if (logger.isLoggable(level)) {
            // Hack (?) to get the stack trace.
            final Throwable dummyException = new Throwable();
            final StackTraceElement locations[] = dummyException.getStackTrace();
            // LOGGING-132: use the provided logger name instead of the class name
            final String cname = name;
            String method = "unknown";
            // Caller will be the third element
            if( locations != null && locations.length > 2 ) {
                final StackTraceElement caller = locations[2];
                method = caller.getMethodName();
            }
            if( ex == null ) {
                logger.logp( level, cname, method, msg );
            } else {
                logger.logp( level, cname, method, msg, ex );
            }
        }
    }

    // --------------------------------------------------------- Public Methods

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
     * Return the native Logger instance we are using.
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
     * @see org.apache.commons.logging.Log#Logger_info(Object)
     */
    @Override
    public void Logger_info(final Object message) {
        log(Level.INFO, String.valueOf(message), null);
    }

    /**
     * Logs a message with {@code java.util.logging.Level.INFO}.
     *
     * @param message to log
     * @param exception log this cause
     * @see org.apache.commons.logging.Log#Logger_info(Object, Throwable)
     */
    @Override
    public void Logger_info(final Object message, final Throwable exception) {
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
