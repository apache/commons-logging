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
 * @version $Id: Log4JCategoryLog.java,v 1.15 2004/02/28 21:46:45 craigmcc Exp $
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
