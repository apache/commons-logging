/*
 * $Header: /home/jerenkrantz/tmp/commons/commons-convert/cvs/home/cvs/jakarta-commons//logging/src/java/org/apache/commons/logging/impl/Attic/Log4JCategoryLog.java,v 1.14 2004/02/28 17:54:14 rdonkin Exp $
 * $Revision: 1.14 $
 * $Date: 2004/02/28 17:54:14 $
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


package org.apache.commons.logging.impl;

import org.apache.commons.logging.Log;
import org.apache.log4j.Category;
import org.apache.log4j.Priority;

/**
 * <p>Implementation of {@link Log} that maps directly to a Log4J
 * <strong>Category</strong>.  Initial configuration of the corresponding
 * Category instances should be done in the usual manner, as outlined in
 * the Log4J documentation.</p>
 *
 * @deprecated Use {@link Log4JLogger} instead.
 *
 * @author <a href="mailto:sanders@apache.org">Scott Sanders</a>
 * @author Rod Waldhoff
 * @author Robert Burrell Donkin
 * @version $Id: Log4JCategoryLog.java,v 1.14 2004/02/28 17:54:14 rdonkin Exp $
 */
public final class Log4JCategoryLog implements Log {


    // ------------------------------------------------------------- Attributes

    /** The fully qualified name of the Log4JCategoryLog class. */
    private static final String FQCN = Log4JCategoryLog.class.getName();

    /** Log to this category */
    private Category category = null;


    // ------------------------------------------------------------ Constructor

    public Log4JCategoryLog() {
    }


    /**
     * Base constructor.
     */
    public Log4JCategoryLog(String name) {
        this.category=Category.getInstance(name);
    }

    /** For use with a log4j factory.
     */
    public Log4JCategoryLog(Category category ) {
        this.category=category;
    }


    // ---------------------------------------------------------- Implmentation


    /**
     * Log a message to the Log4j Category with <code>TRACE</code> priority.
     * Currently logs to <code>DEBUG</code> level in Log4J.
     */
    public void trace(Object message) {
        category.log(FQCN, Priority.DEBUG, message, null);
    }


    /**
     * Log an error to the Log4j Category with <code>TRACE</code> priority.
     * Currently logs to <code>DEBUG</code> level in Log4J.
     */
    public void trace(Object message, Throwable t) {
        category.log(FQCN, Priority.DEBUG, message, t );
    }


    /**
     * Log a message to the Log4j Category with <code>DEBUG</code> priority.
     */
    public void debug(Object message) {
        category.log(FQCN, Priority.DEBUG, message, null);
    }

    /**
     * Log an error to the Log4j Category with <code>DEBUG</code> priority.
     */
    public void debug(Object message, Throwable t) {
        category.log(FQCN, Priority.DEBUG, message, t );
    }


    /**
     * Log a message to the Log4j Category with <code>INFO</code> priority.
     */
    public void info(Object message) {
        category.log(FQCN, Priority.INFO, message, null );
    }


    /**
     * Log an error to the Log4j Category with <code>INFO</code> priority.
     */
    public void info(Object message, Throwable t) {
        category.log(FQCN, Priority.INFO, message, t );
    }


    /**
     * Log a message to the Log4j Category with <code>WARN</code> priority.
     */
    public void warn(Object message) {
        category.log(FQCN, Priority.WARN, message, null );
    }


    /**
     * Log an error to the Log4j Category with <code>WARN</code> priority.
     */
    public void warn(Object message, Throwable t) {
        category.log(FQCN, Priority.WARN, message, t );
    }


    /**
     * Log a message to the Log4j Category with <code>ERROR</code> priority.
     */
    public void error(Object message) {
        category.log(FQCN, Priority.ERROR, message, null );
    }


    /**
     * Log an error to the Log4j Category with <code>ERROR</code> priority.
     */
    public void error(Object message, Throwable t) {
        category.log(FQCN, Priority.ERROR, message, t );
    }


    /**
     * Log a message to the Log4j Category with <code>FATAL</code> priority.
     */
    public void fatal(Object message) {
        category.log(FQCN, Priority.FATAL, message, null );
    }


    /**
     * Log an error to the Log4j Category with <code>FATAL</code> priority.
     */
    public void fatal(Object message, Throwable t) {
        category.log(FQCN, Priority.FATAL, message, t );
    }


    /**
     * Return the native Category instance we are using.
     */
    public Category getCategory() {
        return (this.category);
    }


    /**
     * Check whether the Log4j Category used is enabled for <code>DEBUG</code> priority.
     */
    public boolean isDebugEnabled() {
        return category.isDebugEnabled();
    }


     /**
     * Check whether the Log4j Category used is enabled for <code>ERROR</code> priority.
     */
    public boolean isErrorEnabled() {
        return category.isEnabledFor(Priority.ERROR);
    }


    /**
     * Check whether the Log4j Category used is enabled for <code>FATAL</code> priority.
     */
    public boolean isFatalEnabled() {
        return category.isEnabledFor(Priority.FATAL);
    }


    /**
     * Check whether the Log4j Category used is enabled for <code>INFO</code> priority.
     */
    public boolean isInfoEnabled() {
        return category.isInfoEnabled();
    }


    /**
     * Check whether the Log4j Category used is enabled for <code>TRACE</code> priority.
     * For Log4J, this returns the value of <code>isDebugEnabled()</code>
     */
    public boolean isTraceEnabled() {
        return category.isDebugEnabled();
    }

    /**
     * Check whether the Log4j Category used is enabled for <code>WARN</code> priority.
     */
    public boolean isWarnEnabled() {
        return category.isEnabledFor(Priority.WARN);
    }
}
