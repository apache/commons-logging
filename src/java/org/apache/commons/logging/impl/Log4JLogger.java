/*
 * Copyright 2001-2004 The Apache Software Foundation.
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


package org.apache.commons.logging.impl;

import java.io.Serializable;
import org.apache.commons.logging.Log;
import org.apache.log4j.Logger;
import org.apache.log4j.Priority;

/**
 * <p>Implementation of {@link Log} that maps directly to a Log4J
 * <strong>Logger</strong>.  Initial configuration of the corresponding
 * Logger instances should be done in the usual manner, as outlined in
 * the Log4J documentation.</p>
 *
 * @author <a href="mailto:sanders@apache.org">Scott Sanders</a>
 * @author Rod Waldhoff
 * @author Robert Burrell Donkin
 * @version $Id: Log4JLogger.java,v 1.10 2004/02/28 21:46:45 craigmcc Exp $
 */
public class Log4JLogger implements Log, Serializable {


    // ------------------------------------------------------------- Attributes

    /** The fully qualified name of the Log4JLogger class. */
    private static final String FQCN = Log4JLogger.class.getName();

    /** Log to this logger */
    private transient Logger logger = null;

    /** Logger name */
    private String name = null;


    // ------------------------------------------------------------ Constructor

    public Log4JLogger() {
    }


    /**
     * Base constructor.
     */
    public Log4JLogger(String name) {
        this.name = name;
        this.logger = getLogger();
    }

    /** For use with a log4j factory.
     */
    public Log4JLogger(Logger logger ) {
        this.name = logger.getName();
        this.logger=logger;
    }


    // --------------------------------------------------------- Implementation


    /**
     * Log a message to the Log4j Logger with <code>TRACE</code> priority.
     * Currently logs to <code>DEBUG</code> level in Log4J.
     */
    public void trace(Object message) {
        getLogger().log(FQCN, Priority.DEBUG, message, null);
    }


    /**
     * Log an error to the Log4j Logger with <code>TRACE</code> priority.
     * Currently logs to <code>DEBUG</code> level in Log4J.
     */
    public void trace(Object message, Throwable t) {
        getLogger().log(FQCN, Priority.DEBUG, message, t );
    }


    /**
     * Log a message to the Log4j Logger with <code>DEBUG</code> priority.
     */
    public void debug(Object message) {
        getLogger().log(FQCN, Priority.DEBUG, message, null);
    }

    /**
     * Log an error to the Log4j Logger with <code>DEBUG</code> priority.
     */
    public void debug(Object message, Throwable t) {
        getLogger().log(FQCN, Priority.DEBUG, message, t );
    }


    /**
     * Log a message to the Log4j Logger with <code>INFO</code> priority.
     */
    public void info(Object message) {
        getLogger().log(FQCN, Priority.INFO, message, null );
    }


    /**
     * Log an error to the Log4j Logger with <code>INFO</code> priority.
     */
    public void info(Object message, Throwable t) {
        getLogger().log(FQCN, Priority.INFO, message, t );
    }


    /**
     * Log a message to the Log4j Logger with <code>WARN</code> priority.
     */
    public void warn(Object message) {
        getLogger().log(FQCN, Priority.WARN, message, null );
    }


    /**
     * Log an error to the Log4j Logger with <code>WARN</code> priority.
     */
    public void warn(Object message, Throwable t) {
        getLogger().log(FQCN, Priority.WARN, message, t );
    }


    /**
     * Log a message to the Log4j Logger with <code>ERROR</code> priority.
     */
    public void error(Object message) {
        getLogger().log(FQCN, Priority.ERROR, message, null );
    }


    /**
     * Log an error to the Log4j Logger with <code>ERROR</code> priority.
     */
    public void error(Object message, Throwable t) {
        getLogger().log(FQCN, Priority.ERROR, message, t );
    }


    /**
     * Log a message to the Log4j Logger with <code>FATAL</code> priority.
     */
    public void fatal(Object message) {
        getLogger().log(FQCN, Priority.FATAL, message, null );
    }


    /**
     * Log an error to the Log4j Logger with <code>FATAL</code> priority.
     */
    public void fatal(Object message, Throwable t) {
        getLogger().log(FQCN, Priority.FATAL, message, t );
    }


    /**
     * Return the native Logger instance we are using.
     */
    public Logger getLogger() {
        if (logger == null) {
            logger = Logger.getLogger(name);
        }
        return (this.logger);
    }


    /**
     * Check whether the Log4j Logger used is enabled for <code>DEBUG</code> priority.
     */
    public boolean isDebugEnabled() {
        return getLogger().isDebugEnabled();
    }


     /**
     * Check whether the Log4j Logger used is enabled for <code>ERROR</code> priority.
     */
    public boolean isErrorEnabled() {
        return getLogger().isEnabledFor(Priority.ERROR);
    }


    /**
     * Check whether the Log4j Logger used is enabled for <code>FATAL</code> priority.
     */
    public boolean isFatalEnabled() {
        return getLogger().isEnabledFor(Priority.FATAL);
    }


    /**
     * Check whether the Log4j Logger used is enabled for <code>INFO</code> priority.
     */
    public boolean isInfoEnabled() {
        return getLogger().isInfoEnabled();
    }


    /**
     * Check whether the Log4j Logger used is enabled for <code>TRACE</code> priority.
     * For Log4J, this returns the value of <code>isDebugEnabled()</code>
     */
    public boolean isTraceEnabled() {
        return getLogger().isDebugEnabled();
    }

    /**
     * Check whether the Log4j Logger used is enabled for <code>WARN</code> priority.
     */
    public boolean isWarnEnabled() {
        return getLogger().isEnabledFor(Priority.WARN);
    }
}
