/*
 * $Header: /home/jerenkrantz/tmp/commons/commons-convert/cvs/home/cvs/jakarta-commons//logging/src/java/org/apache/commons/logging/Log.java,v 1.9 2002/01/17 01:47:49 craigmcc Exp $
 * $Revision: 1.9 $
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

/**
 * <p>A simple logging interface abstracting logging APIs.  In order to be
 * instantiated successfully by {@link LogSource}, classes that implement
 * this interface must have a constructor that takes a single String
 * parameter representing the "name" of this Log.</p>
 *
 * <p> The log level determines whether a particular message
 * should be passed to the logging implementation.
 * Log levels are ordered numerically.
 * For example, if the log level is <code>warn</code> 
 * then the message passed to {@link #error} will be passed to the logging 
 * implementation but if the log level is <code>fatal</code> or higher
 * then the message will not.</p>
 *
 * <p>The logging level constants are provided for the convenience of
 * {@link Log} implementations that wish to support dynamic changes in the
 * logging level configuration.  However, configuration will generally be done
 * external to the Logging APIs, through whatever mechanism is supported by
 * the underlying logging implementation in use.</p>
 *
 * @author Rod Waldhoff
 * @version $Id: Log.java,v 1.9 2002/01/17 01:47:49 craigmcc Exp $
 */
public interface Log {


    // ---------------------------------------------------- Log Level Constants
    
    /** All logging level. */
    public static final int ALL  = Integer.MIN_VALUE;
    /** "Debug" level logging. */
    public static final int DEBUG  = 10000;
    /** "Info" level logging. */
    public static final int INFO   = 20000;
    /** "Warn" level logging. */
    public static final int WARN   = 30000;
    /** "Error" level logging. */
    public static final int ERROR  = 40000;
    /** "Fatal" level logging. */
    public static final int FATAL  = 50000;
    /** No logging level. */
    public static final int OFF  = Integer.MAX_VALUE;
    
    
    // ----------------------------------------------------- Logging Properties
    

    /**
     * <p> Is debug logging currently enabled? </p>
     *
     * <p> Call this method to prevent having to perform expensive operations
     * (for example, <code>String</code> concatination)
     * when the log level is more than debug. </p> 
     */
    public boolean isDebugEnabled();
    

    /**
     * <p> Is error logging currently enabled? </p>
     *
     * <p> Call this method to prevent having to perform expensive operations
     * (for example, <code>String</code> concatination)
     * when the log level is more than error. </p> 
     */
    public boolean isErrorEnabled();
    

    /**
     * <p> Is fatal logging currently enabled? </p>
     *
     * <p> Call this method to prevent having to perform expensive operations
     * (for example, <code>String</code> concatination)
     * when the log level is more than fatal. </p> 
     */
    public boolean isFatalEnabled();
    

    /**
     * <p> Is info logging currently enabled? </p>
     *
     * <p> Call this method to prevent having to perform expensive operations
     * (for example, <code>String</code> concatination)
     * when the log level is more than info. </p> 
     */
    public boolean isInfoEnabled();

    
    /**
     * <p> Is warning logging currently enabled? </p>
     *
     * <p> Call this method to prevent having to perform expensive operations
     * (for example, <code>String</code> concatination)
     * when the log level is more than warning. </p> 
     */
    public boolean isWarnEnabled();

    
    // -------------------------------------------------------- Logging Methods


    /**
     * <p> Log a message with debug log level </p> 
     */
    public void debug(Object message);
    

    /**
     * <p> Log an error with debug log level </p> 
     */
    public void debug(Object message, Throwable t);

    
    /**
     * <p> Log a message with info log level </p> 
     */
    public void info(Object message);
    

    /**
     * <p> Log an error with info log level </p> 
     */
    public void info(Object message, Throwable t);
    
    
    /**
     * <p> Log a message with warn log level </p> 
     */
    public void warn(Object message);


    /**
     * <p> Log an error with warn log level </p> 
     */
    public void warn(Object message, Throwable t);
    
    
    /**
     * <p> Log a message with error log level </p> 
     */
    public void error(Object message);


    /**
     * <p> Log an error with error log level </p> 
     */
    public void error(Object message, Throwable t);
    
    
    /**
     * <p> Log a message with fatal log level </p> 
     */
    public void fatal(Object message);


    /**
     * <p> Log an error with fatal log level </p> 
     */
    public void fatal(Object message, Throwable t);


}
