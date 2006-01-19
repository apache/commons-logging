/*
 * Copyright 2001-2005 The Apache Software Foundation.
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
import org.apache.log4j.Level;

/**
 * Implementation of {@link Log} that maps directly to a
 * <strong>Logger</strong> for log4J version 1.3 or later.
 * <p>
 * Initial configuration of the corresponding Logger instances should be done
 * in the usual manner, as outlined in the Log4J documentation.
 * <p>
 * The reason this logger is distinct from the 1.2 logger is that in version 1.3
 * of Log4J, classes Logger and Level should be used. However code that uses 
 * those classes and is compiled against log4j1.2 will not run against 1.3. And
 * code that uses those classes and is compiled against log4j1.3 will not run
 * against 1.2. 
 *
 * @author <a href="mailto:sanders@apache.org">Scott Sanders</a>
 * @author Rod Waldhoff
 * @author Robert Burrell Donkin
 * @version $Id$
 */
public class Log4J13Logger implements Log, Serializable {

    // Verify that log4j is available, and that it is version 1.3 or later.
    // If an ExceptionInInitializerError is generated, then LogFactoryImpl
    // will treat that as meaning that the appropriate underlying logging
    // library is just not present - if discovery is in progress then
    // discovery will continue.
    //
    // Note that in log4j 1.2, Priority is effectively deprecated. Its
    // replacement, Level, extends Priority. In log4j 1.3, Priority is still
    // included but instead extends Level. In later versions, Priority may
    // not be included at all.
    static {
        Class levelSuperclass = Level.class.getSuperclass();
        if (levelSuperclass.getName().equals("org.apache.log4j.Priority")) {
            // nope, this is log4j 1.2, so force an ExceptionInInitializerError
            throw new InstantiationError("Log4J 1.3 not available");
        }
    }

    // ------------------------------------------------------------- Attributes

    /** The fully qualified name of the Log4JLogger class. */
    private static final String FQCN = Log4J13Logger.class.getName();
    
    /** Log to this logger */
    private transient Logger logger = null;

    /** Logger name */
    private String name = null;


    // ------------------------------------------------------------ Constructor

    public Log4J13Logger() {
    }


    /**
     * Base constructor.
     */
    public Log4J13Logger(String name) {
        this.name = name;
        this.logger = getLogger();
    }

    /** For use with a log4j factory.
     */
    public Log4J13Logger(Logger logger ) {
        this.name = logger.getName();
        this.logger=logger;
    }


    // --------------------------------------------------------- Implementation


    /**
     * Logs a message with <code>org.apache.log4j.Level.TRACE</code>.
     *
     * @param message to log
     * @see org.apache.commons.logging.Log#trace(Object)
     */
    public void trace(Object message) {
        getLogger().log(FQCN, Level.TRACE, message, null );
    }


    /**
     * Logs a message with <code>org.apache.log4j.Level.TRACE</code>.
     *
     * @param message to log
     * @param t log this cause
     * @see org.apache.commons.logging.Log#trace(Object, Throwable)
     */
    public void trace(Object message, Throwable t) {
        getLogger().log(FQCN, Level.TRACE, message, t );
    }


    /**
     * Logs a message with <code>org.apache.log4j.Level.DEBUG</code>.
     *
     * @param message to log
     * @see org.apache.commons.logging.Log#debug(Object)
     */
    public void debug(Object message) {
        getLogger().log(FQCN, Level.DEBUG, message, null );
    }

    /**
     * Logs a message with <code>org.apache.log4j.Level.DEBUG</code>.
     *
     * @param message to log
     * @param t log this cause
     * @see org.apache.commons.logging.Log#debug(Object, Throwable)
     */
    public void debug(Object message, Throwable t) {
        getLogger().log(FQCN, Level.DEBUG, message, t );
    }


    /**
     * Logs a message with <code>org.apache.log4j.Level.INFO</code>.
     *
     * @param message to log
     * @see org.apache.commons.logging.Log#info(Object)
     */
    public void info(Object message) {
        getLogger().log(FQCN, Level.INFO, message, null );
    }


    /**
     * Logs a message with <code>org.apache.log4j.Level.INFO</code>.
     *
     * @param message to log
     * @param t log this cause
     * @see org.apache.commons.logging.Log#info(Object, Throwable)
     */
    public void info(Object message, Throwable t) {
        getLogger().log(FQCN, Level.INFO, message, t );
    }


    /**
     * Logs a message with <code>org.apache.log4j.Level.WARN</code>.
     *
     * @param message to log
     * @see org.apache.commons.logging.Log#warn(Object)
     */
    public void warn(Object message) {
        getLogger().log(FQCN, Level.WARN, message, null );
    }


    /**
     * Logs a message with <code>org.apache.log4j.Level.WARN</code>.
     *
     * @param message to log
     * @param t log this cause
     * @see org.apache.commons.logging.Log#warn(Object, Throwable)
     */
    public void warn(Object message, Throwable t) {
        getLogger().log(FQCN, Level.WARN, message, t );
    }


    /**
     * Logs a message with <code>org.apache.log4j.Level.ERROR</code>.
     *
     * @param message to log
     * @see org.apache.commons.logging.Log#error(Object)
     */
    public void error(Object message) {
        getLogger().log(FQCN, Level.ERROR, message, null );
    }


    /**
     * Logs a message with <code>org.apache.log4j.Level.ERROR</code>.
     *
     * @param message to log
     * @param t log this cause
     * @see org.apache.commons.logging.Log#error(Object, Throwable)
     */
    public void error(Object message, Throwable t) {
        getLogger().log(FQCN, Level.ERROR, message, t );
    }


    /**
     * Logs a message with <code>org.apache.log4j.Level.FATAL</code>.
     *
     * @param message to log
     * @see org.apache.commons.logging.Log#fatal(Object)
     */
    public void fatal(Object message) {
        getLogger().log(FQCN, Level.FATAL, message, null );
    }


    /**
     * Logs a message with <code>org.apache.log4j.Level.FATAL</code>.
     *
     * @param message to log
     * @param t log this cause
     * @see org.apache.commons.logging.Log#fatal(Object, Throwable)
     */
    public void fatal(Object message, Throwable t) {
        getLogger().log(FQCN, Level.FATAL, message, t );
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
        return getLogger().isEnabledFor(Level.ERROR);
    }


    /**
     * Check whether the Log4j Logger used is enabled for <code>FATAL</code> priority.
     */
    public boolean isFatalEnabled() {
        return getLogger().isEnabledFor(Level.FATAL);
    }


    /**
     * Check whether the Log4j Logger used is enabled for <code>INFO</code> priority.
     */
    public boolean isInfoEnabled() {
        return getLogger().isInfoEnabled();
    }


    /**
     * Check whether the Log4j Logger used is enabled for <code>TRACE</code> priority.
     */
    public boolean isTraceEnabled() {
        return getLogger().isTraceEnabled();
    }

    /**
     * Check whether the Log4j Logger used is enabled for <code>WARN</code> priority.
     */
    public boolean isWarnEnabled() {
        return getLogger().isEnabledFor(Level.WARN);
    }
}
