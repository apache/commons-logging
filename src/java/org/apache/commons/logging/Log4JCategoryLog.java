/*
 * $Header: /home/jerenkrantz/tmp/commons/commons-convert/cvs/home/cvs/jakarta-commons//logging/src/java/org/apache/commons/logging/Attic/Log4JCategoryLog.java,v 1.7 2002/01/03 18:59:57 rdonkin Exp $
 * $Revision: 1.7 $
 * $Date: 2002/01/03 18:59:57 $
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
 * <p> Log level management is now independent of the Log4J configuration.
 * Log4J will not be called unless the log level is currently enabled. </p>
 *
 * @author Rod Waldhoff
 * @author Robert Burrell Donkin
 *
 * @version $Id: Log4JCategoryLog.java,v 1.7 2002/01/03 18:59:57 rdonkin Exp $
 */
public final class Log4JCategoryLog  extends AbstractLog {
    
    // --------------------------------------------------------- Attributes
    
    /** Log to this category */
    Category _category = null;



    // --------------------------------------------------------- Constructor
    
    
    /** 
     * Base constructor 
     */
    public Log4JCategoryLog(String name) {
        // the default log level for log4j should be ALL
        // so that control of logging is delegated to Log4J.
        // of course, this can be override programmatically for a particular log instance.
        setLevel(Log.ALL);
        _category = Category.getInstance(name);
    }

    // --------------------------------------------------------- Implmentation

    /**
     * Simply call log4j category.
     */
    protected final void debugImpl(Object message) {
        _category.debug(message);
    }

    /**
     * Simply call log4j category.
     */
    protected final void debugImpl(Object message, Throwable t) {
        _category.debug(message,t);
    }

    /**
     * Simply call log4j category.
     */
    protected final void infoImpl(Object message) {
        _category.info(message);
    }

    /**
     * Simply call log4j category.
     */
    protected final void infoImpl(Object message, Throwable t) {
        _category.info(message,t);
    }

    /**
     * Simply call log4j category.
     */
    protected final void warnImpl(Object message) {
        _category.warn(message);
    }
    
    /**
     * Simply call log4j category.
     */
    protected final void warnImpl(Object message, Throwable t) {
        _category.warn(message,t);
    }

    /**
     * Simply call log4j category.
     */
    protected final void errorImpl(Object message) {
        _category.error(message);
    }

    /**
     * Simply call log4j category.
     */
    protected final void errorImpl(Object message, Throwable t) {
        _category.error(message,t);
    }

    /**
     * Simply call log4j category.
     */
    protected final void fatalImpl(Object message) {
        _category.fatal(message);
    }

    /**
     * Simply call log4j category.
     */
    protected final void fatalImpl(Object message, Throwable t) {
        _category.fatal(message,t);
    }

    /**
     * Simply call log4j category.
     */
    protected final boolean isDebugEnabledImpl() {
        return _category.isDebugEnabled();
    }

    /**
     * Simply call log4j category.
     */
    protected final boolean isInfoEnabledImpl() {
        return _category.isInfoEnabled();
    }
}
