/*
 * $Header: /home/jerenkrantz/tmp/commons/commons-convert/cvs/home/cvs/jakarta-commons//logging/src/java/org/apache/commons/logging/impl/Log4JLogger.java,v 1.7 2003/10/05 15:58:30 rdonkin Exp $
 * $Revision: 1.7 $
 * $Date: 2003/10/05 15:58:30 $
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
 * @version $Id: Log4JLogger.java,v 1.7 2003/10/05 15:58:30 rdonkin Exp $
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
     * Base constructor
     */
    public Log4JLogger(String name) {
        this.name = name;
        this.logger = getLogger();
    }

    /** For use with a log4j factory
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
