/*
 * $Header: /home/jerenkrantz/tmp/commons/commons-convert/cvs/home/cvs/jakarta-commons//logging/src/java/org/apache/commons/logging/impl/AvalonLogger.java,v 1.5 2003/10/05 15:58:38 rdonkin Exp $
 * $Revision: 1.5 $
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
import org.apache.avalon.framework.logger.Logger;
import org.apache.commons.logging.Log;

/**
 * Implementation of commons-logging Log interface that delegates all 
 * logging calls to Avalon logging abstraction: the Logger interface.
 * 
 * @author <a href="mailto:neeme@apache.org">Neeme Praks</a>
 * @version $Revision: 1.5 $ $Date: 2003/10/05 15:58:38 $
 */
public class AvalonLogger implements Log, Serializable {

    private static Logger defaultLogger = null;
    private transient Logger logger = null;
    private String name = null;

    /**
     * @param logger the avalon logger implementation to delegate to 
     */
    public AvalonLogger(Logger logger) {
        this.name = name;
        this.logger = logger;
    }

    /**
     * @param name the name of the avalon logger implementation to delegate to 
     */
    public AvalonLogger(String name) {
        if (defaultLogger == null) 
            throw new NullPointerException("default logger has to be specified if this constructor is used!");
        this.logger = getLogger();
    }

    /**
     * @return avalon logger implementation
     */
    public Logger getLogger() {
        if (logger == null) {
            logger = defaultLogger.getChildLogger(name);
        }
        return logger;
    }

    /**
     * @param logger the default avalon logger, in case there is no logger instance supplied in constructor
     */
    public static void setDefaultLogger(Logger logger) {
        defaultLogger = logger;
    }

    /**
     * @see org.apache.commons.logging.Log#debug(java.lang.Object, java.lang.Throwable)
     */
    public void debug(Object o, Throwable t) {
        if (getLogger().isDebugEnabled()) getLogger().debug(String.valueOf(o), t);
    }

    /**
     * @see org.apache.commons.logging.Log#debug(java.lang.Object)
     */
    public void debug(Object o) {
        if (getLogger().isDebugEnabled()) getLogger().debug(String.valueOf(o));
    }

    /**
     * @see org.apache.commons.logging.Log#error(java.lang.Object, java.lang.Throwable)
     */
    public void error(Object o, Throwable t) {
        if (getLogger().isErrorEnabled()) getLogger().error(String.valueOf(o), t);
    }

    /**
     * @see org.apache.commons.logging.Log#error(java.lang.Object)
     */
    public void error(Object o) {
        if (getLogger().isErrorEnabled()) getLogger().error(String.valueOf(o));
    }

    /**
     * @see org.apache.commons.logging.Log#fatal(java.lang.Object, java.lang.Throwable)
     */
    public void fatal(Object o, Throwable t) {
        if (getLogger().isFatalErrorEnabled()) getLogger().fatalError(String.valueOf(o), t);
    }

    /**
     * @see org.apache.commons.logging.Log#fatal(java.lang.Object)
     */
    public void fatal(Object o) {
        if (getLogger().isFatalErrorEnabled()) getLogger().fatalError(String.valueOf(o));
    }

    /**
     * @see org.apache.commons.logging.Log#info(java.lang.Object, java.lang.Throwable)
     */
    public void info(Object o, Throwable t) {
        if (getLogger().isInfoEnabled()) getLogger().info(String.valueOf(o), t);
    }

    /**
     * @see org.apache.commons.logging.Log#info(java.lang.Object)
     */
    public void info(Object o) {
        if (getLogger().isInfoEnabled()) getLogger().info(String.valueOf(o));
    }

    /**
     * @see org.apache.commons.logging.Log#isDebugEnabled()
     */
    public boolean isDebugEnabled() {
        return getLogger().isDebugEnabled();
    }

    /**
     * @see org.apache.commons.logging.Log#isErrorEnabled()
     */
    public boolean isErrorEnabled() {
        return getLogger().isErrorEnabled();
    }

    /**
     * @see org.apache.commons.logging.Log#isFatalEnabled()
     */
    public boolean isFatalEnabled() {
        return getLogger().isFatalErrorEnabled();
    }

    /**
     * @see org.apache.commons.logging.Log#isInfoEnabled()
     */
    public boolean isInfoEnabled() {
        return getLogger().isInfoEnabled();
    }

    /**
     * @see org.apache.commons.logging.Log#isTraceEnabled()
     */
    public boolean isTraceEnabled() {
        return getLogger().isDebugEnabled();
    }

    /**
     * @see org.apache.commons.logging.Log#isWarnEnabled()
     */
    public boolean isWarnEnabled() {
        return getLogger().isWarnEnabled();
    }

    /**
     * @see org.apache.commons.logging.Log#trace(java.lang.Object, java.lang.Throwable)
     */
    public void trace(Object o, Throwable t) {
        if (getLogger().isDebugEnabled()) getLogger().debug(String.valueOf(o), t);
    }

    /**
     * @see org.apache.commons.logging.Log#trace(java.lang.Object)
     */
    public void trace(Object o) {
        if (getLogger().isDebugEnabled()) getLogger().debug(String.valueOf(o));
    }

    /**
     * @see org.apache.commons.logging.Log#warn(java.lang.Object, java.lang.Throwable)
     */
    public void warn(Object o, Throwable t) {
        if (getLogger().isWarnEnabled()) getLogger().warn(String.valueOf(o), t);
    }

    /**
     * @see org.apache.commons.logging.Log#warn(java.lang.Object)
     */
    public void warn(Object o) {
        if (getLogger().isWarnEnabled()) getLogger().warn(String.valueOf(o));
    }

}
