/*
 * $Header: /home/jerenkrantz/tmp/commons/commons-convert/cvs/home/cvs/jakarta-commons//logging/src/java/org/apache/commons/logging/Attic/LogKitLogger.java,v 1.2 2002/01/17 01:47:49 craigmcc Exp $
 * $Revision: 1.2 $
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

import org.apache.log.Logger;
import org.apache.log.Hierarchy;

/**
 * <p> Wrapper for jakarta-avalon-logkit logging system. </p>
 *
 * @author Robert Burrell Donkin
 *
 * @version $Id: LogKitLogger.java,v 1.2 2002/01/17 01:47:49 craigmcc Exp $
 */

public final class LogKitLogger implements Log {


    // ------------------------------------------------------------- Attributes


    /** Logging goes to this <code>LogKit</code> logger */
    protected Logger logger = null;


    // ------------------------------------------------------------ Constructor
    

    /**  
     * Construct <code>LogKitLogger</code> which wraps the <code>LogKit</code>
     * logger with given name.
     * 
     * @param name log name
     */
    public LogKitLogger(String name) {
        logger = Hierarchy.getDefaultHierarchy().getLoggerFor(name);
    }


    // ----------------------------------------------------- Log Implementation


    /**
     * Log to <code>LogKit</code> logger.
     */
    public void debug(Object message) {
        if (message != null) {
            logger.debug(message.toString());
        }
    }


    /**
     * Log to <code>LogKit</code> logger.
     */
    public void debug(Object message, Throwable t) {
        if (message != null) {
            logger.debug(message.toString(), t);
        }
    }


    /**
     * Log to <code>LogKit</code> logger.
     */
    public void info(Object message) {
        if (message != null) {
            logger.info(message.toString());
        }
    }


    /**
     * Log to <code>LogKit</code> logger.
     */
    public void info(Object message, Throwable t) {
        if (message != null) {
            logger.info(message.toString(), t);
        }
    }


    /**
     * Log to <code>LogKit</code> logger.
     */
    public void warn(Object message) {
        if (message != null) {
            logger.warn(message.toString());
        }
    }
    

    /**
     * Log to <code>LogKit</code> logger.
     */
    public void warn(Object message, Throwable t) {
        if (message != null) {
            logger.warn(message.toString(), t);
        }
    }


    /**
     * Log to <code>LogKit</code> logger.
     */
    public void error(Object message) {
        if (message != null) {
            logger.error(message.toString());
        }
    }


    /**
     * Log to <code>LogKit</code> logger.
     */
    public void error(Object message, Throwable t) {
        if (message != null) {
            logger.error(message.toString(), t);
        }
    }


    /**
     * Log to <code>LogKit</code> logger.
     */
    public void fatal(Object message) {
        if (message != null) {
            logger.fatalError(message.toString());
        }
    }


    /**
     * Log to <code>LogKit</code> logger.
     */
    public void fatal(Object message, Throwable t) {
        if (message != null) {
            logger.fatalError(message.toString(), t);
        }
    }


    /**
     * Check that debug is enabled for <code>LogKit</code> logger.
     */       
    public boolean isDebugEnabled() {
        return logger.isDebugEnabled();
    }


    /**
     * Check that error is enabled for <code>LogKit</code> logger.
     */       
    public boolean isErrorEnabled() {
        return logger.isErrorEnabled();
    }


    /**
     * Check that fatal is enabled for <code>LogKit</code> logger.
     */       
    public boolean isFatalEnabled() {
        return logger.isFatalErrorEnabled();
    }


    /**
     * Check that info is enabled for <code>LogKit</code> logger.
     */    
    public boolean isInfoEnabled() {
        return logger.isInfoEnabled();
    }


    /**
     * Check that warn is enabled for <code>LogKit</code> logger.
     */       
    public boolean isWarnEnabled() {
        return logger.isWarnEnabled();
    }


}
