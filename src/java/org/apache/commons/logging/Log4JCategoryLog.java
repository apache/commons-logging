/*
 * $Header: /home/jerenkrantz/tmp/commons/commons-convert/cvs/home/cvs/jakarta-commons//logging/src/java/org/apache/commons/logging/Attic/Log4JCategoryLog.java,v 1.8 2002/01/17 01:47:49 craigmcc Exp $
 * $Revision: 1.8 $
 * $Date: 2002/01/17 01:47:49 $
 *
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1999-2002 The Apache Software Foundation.  All rights
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
 *
 */


package org.apache.commons.logging;

import org.apache.log4j.Category;
import org.apache.log4j.Priority;

/**
 * <p>Implementation of {@link Log} that maps directly to a Log4J
 * <strong>Category</strong>.  Initial configuration of the corresponding
 * Category instances should be done in the usual manner, as outlined in
 * the Log4J documentation.</p>
 *
 * @author Rod Waldhoff
 * @author Robert Burrell Donkin
 *
 * @version $Id: Log4JCategoryLog.java,v 1.8 2002/01/17 01:47:49 craigmcc Exp $
 */
public final class Log4JCategoryLog implements Log {
    

    // ------------------------------------------------------------- Attributes
    

    /** Log to this category */
    Category category = null;


    // ------------------------------------------------------------ Constructor
    
    
    /** 
     * Base constructor 
     */
    public Log4JCategoryLog(String name) {
        category = Category.getInstance(name);
    }


    // ---------------------------------------------------------- Implmentation


    /**
     * Simply call log4j category.
     */
    public void debug(Object message) {
        category.debug(message);
    }


    /**
     * Simply call log4j category.
     */
    public void debug(Object message, Throwable t) {
        category.debug(message,t);
    }


    /**
     * Simply call log4j category.
     */
    public void info(Object message) {
        category.info(message);
    }


    /**
     * Simply call log4j category.
     */
    public void info(Object message, Throwable t) {
        category.info(message,t);
    }


    /**
     * Simply call log4j category.
     */
    public void warn(Object message) {
        category.warn(message);
    }

    
    /**
     * Simply call log4j category.
     */
    public void warn(Object message, Throwable t) {
        category.warn(message,t);
    }


    /**
     * Simply call log4j category.
     */
    public void error(Object message) {
        category.error(message);
    }


    /**
     * Simply call log4j category.
     */
    public void error(Object message, Throwable t) {
        category.error(message,t);
    }


    /**
     * Simply call log4j category.
     */
    public void fatal(Object message) {
        category.fatal(message);
    }


    /**
     * Simply call log4j category.
     */
    public void fatal(Object message, Throwable t) {
        category.fatal(message,t);
    }


    /**
     * Simply call log4j category.
     */
    public boolean isDebugEnabled() {
        return category.isDebugEnabled();
    }


    /**
     * Simply call log4j category.
     */
    public boolean isErrorEnabled() {
        return category.isEnabledFor(Priority.ERROR);
    }


    /**
     * Simply call log4j category.
     */
    public boolean isFatalEnabled() {
        return category.isEnabledFor(Priority.FATAL);
    }


    /**
     * Simply call log4j category.
     */
    public boolean isInfoEnabled() {
        return category.isInfoEnabled();
    }


    /**
     * Simply call log4j category.
     */
    public boolean isWarnEnabled() {
        return category.isEnabledFor(Priority.WARN);
    }


}
