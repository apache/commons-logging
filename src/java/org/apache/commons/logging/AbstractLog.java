/*
 * $Header: /home/jerenkrantz/tmp/commons/commons-convert/cvs/home/cvs/jakarta-commons//logging/src/java/org/apache/commons/logging/Attic/AbstractLog.java,v 1.1 2002/01/03 18:52:25 rdonkin Exp $
 * $Revision: 1.1 $
 * $Date: 2002/01/03 18:52:25 $
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


/**
  * <p> This is an abstract implementation of the <code>Log</code> interface.
  * It provides the following common services for the actual concrete implementations:
  * 
  * <h4> Log Level Property </h4> 
  * <p> Property getter and setter for log levels. </p>
  * 
  * <h4> Log Level Enforcement</h4>
  * <p> The current log level is checked and then the message will be passed onto the
  * subclass implementation if the level is currently enabled. </p>
  *
  * @author Robert Burrell Donkin
  * @version $Revision: 1.1 $
 */
public abstract class AbstractLog implements Log {

    // --------------------------------------------------------- Attributes
    
    /** Default log level is currently <code>OFF</code> */
    private int currentLogLevel = Log.OFF;


    // --------------------------------------------------------- Properties

    /**
     * <p> Set logging level. </p> 
     *
     * @param level new logging level
     */
    public void setLevel(int currentLogLevel) {
    
        this.currentLogLevel = currentLogLevel;
    }
    
    /**
     * <p> Get logging level. </p> 
     */
    public int getLevel() {
    
        return currentLogLevel;
    }
    
    /**
     * <p> Are debug messages currently enabled? </p>
     *
     * <p> This allows expensive operations such as <code>String</code> concatination
     * to be avoided when the message will be ignored by the logger. </p>
     *
     * <p> This implementation checks that the log level is debug or lower.
     * If it is, then it passes the request onto {@link #isDebugEnabledImpl}.
     */
    public final boolean isDebugEnabled() {
    
        if (isLevelEnabled(Log.DEBUG)) {
            return isDebugEnabledImpl();
        }
        
        return false;
    }
    
    /**
     * <p> Are info messages currently enabled? </p>
     *
     * <p> This allows expensive operations such as <code>String</code> concatination
     * to be avoided when the message will be ignored by the logger. </p>
     *
     * <p> This implementation checks that the log level is debug or lower.
     * If it is, then it passes the request onto {@link #isInfoEnabledImpl}.
     */
    public final boolean isInfoEnabled() {
    
        if (isLevelEnabled(Log.INFO)) {
            return isInfoEnabledImpl();
        }
        
        return false;
    }  

    
    // --------------------------------------------------------- Logging Methods

    /**
     * <p> Log a message with debug log level.</p> 
     *
     * <p> If debug log level is enabled, 
     * then {@link #debugImpl(Object message)} is called. </p>
     */
    public final void debug(Object message) {
    
        if (isLevelEnabled(Log.DEBUG)) {
            debugImpl(message);
        }
    }
    
    /**
     * <p> Log an error with debug log level.</p> 
     *
     * <p> If debug log level is enabled, 
     * then {@link #debugImpl(Object message, Throwable t)} is called. </p>
     */    
    public final void debug(Object message, Throwable t) {
    
        if (isLevelEnabled(Log.DEBUG)) {
            debugImpl(message, t);
        }
    }

    /**
     * <p> Log a message with info log level.</p> 
     *
     * <p> If info log level is enabled, 
     * then {@link #infoImpl(Object message)} is called. </p>
     */    
    public final void info(Object message) {
    
        if (isLevelEnabled(Log.INFO)) {
            infoImpl(message);
        }
    }
    
    /**
     * <p> Log an error with info log level.</p> 
     *
     * <p> If info log level is enabled, 
     * then {@link #infoImpl(Object message, Throwable t)} is called. </p>
     */
    public final void info(Object message, Throwable t) {
    
        if (isLevelEnabled(Log.INFO)) {
            infoImpl(message, t);
        }
    }
    
    /**
     * <p> Log a message with warn log level.</p> 
     *
     * <p> If warn log level is enabled, 
     * then {@link #warnImpl(Object message)} is called. </p>
     */
    public final void warn(Object message) {
    
        if (isLevelEnabled(Log.WARN)) {
            warnImpl(message);
        }
    }

    /**
     * <p> Log an error with warn log level.</p> 
     *
     * <p> If warn log level is enabled, 
     * then {@link #warnImpl(Object message, Throwable t)} is called. </p>
     */
    public final void warn(Object message, Throwable t) {
    
        if (isLevelEnabled(Log.WARN)) {
            warnImpl(message, t);
        }
    }
    
    /**
     * <p> Log a message with error log level.</p> 
     *
     * <p> If error log level is enabled, 
     * then {@link #errorImpl(Object message)} is called. </p>
     */
    public final void error(Object message) {
    
        if (isLevelEnabled(Log.ERROR)) {
            errorImpl(message);
        }
    }
    
    /**
     * <p> Log an error with error log level.</p> 
     *
     * <p> If error log level is enabled, 
     * then {@link #errorImpl(Object message, Throwable t)} is called. </p>
     */
    public final void error(Object message, Throwable t) {
    
        if (isLevelEnabled(Log.ERROR)) {
            errorImpl(message, t);
        }
    }
    
    /**
     * <p> Log a message with fatal log level.</p> 
     *
     * <p> If fatal log level is enabled, 
     * then {@link #fatalImpl(Object message)} is called. </p>
     */
    public final void fatal(Object message) {
    
        if (isLevelEnabled(Log.FATAL)) {
            fatalImpl(message);
        }
    }

    /**
     * <p> Log an error with fatal log level.</p> 
     *
     * <p> If fatal log level is enabled, 
     * then {@link #fatalImpl(Object message, Throwable t)} is called. </p>
     */    
    public final void fatal(Object message, Throwable t) {
    
        if (isLevelEnabled(Log.FATAL)) {
            fatalImpl(message, t);
        }
    }
  
    
    
    // --------------------------------------------------------- Decorated Implementation
 
    /**
     * <p> [OVERRIDE] Log a message with debug log level. 
     * Subclasses should override this method to implement the logging.</p> 
     */
    protected abstract void debugImpl(Object message);
    
    /**
     * <p> [OVERRIDE] Log an error with debug log level.
     * Subclasses should override this method to implement the logging. </p> 
     */
    protected abstract void debugImpl(Object message, Throwable t);
    
        
    /**
     * <p> [OVERRIDE] Log a message with info log level .
     * Subclasses should override this method to implement the logging.</p> 
     */
    protected abstract void infoImpl(Object message);

    /**
     * <p> [OVERRIDE] Log an error with info log level.
     * Subclasses should override this method to implement the logging.</p> 
     */
    protected abstract void infoImpl(Object message, Throwable t);
    
    
    /**
     * <p> [OVERRIDE] Log a message with warn log level.
     * Subclasses should override this method to implement the logging.</p> 
     */
    protected abstract void warnImpl(Object message);

    /**
     * <p> [OVERRIDE] Log an error with warn log level.
     * Subclasses should override this method to implement the logging.</p> 
     */
    protected abstract void warnImpl(Object message, Throwable t);
    
    
    /**
     * <p> [OVERRIDE] Log a message with error log level.
     * Subclasses should override this method to implement the logging.</p> 
     */
    protected abstract void errorImpl(Object message);
    
    /**
     * <p> [OVERRIDE] Log an error with error log level.
     * Subclasses should override this method to implement the logging.</p> 
     */
    protected abstract void errorImpl(Object message, Throwable t);
    
    
    /**
     * <p> [OVERRIDE] Log a message with fatal log level.
     * Subclasses should override this method to implement the logging.</p> 
     */
    protected abstract void fatalImpl(Object message);


    /**
     * <p> [OVERRIDE] Log an error with fatal log level.
     * Subclasses should override this method to implement the logging.</p> 
     */
    protected abstract void fatalImpl(Object message, Throwable t);
    
    /**
     * <p> Are debug messages currently enabled? </p>
     *
     * <p> Subclasses should override this method if their logger provides 
     * a special implementation. </p>
     *
     * @return true
     */
    protected boolean isDebugEnabledImpl() {
        return true;
    }

    /**
     * <p> Are info messages currently enabled? </p>
     *
     * <p> Subclasses should override this method if their logger provides 
     * a special implementation. </p>
     *
     * @return true
     */
    protected boolean isInfoEnabledImpl() {
        return true;
    }

    
    
    // --------------------------------------------------------- Implementation Methods

    /**
     * Is the given log level currently enabled?
     *
     * @param logLevel is this level enabled?
     */
    protected boolean isLevelEnabled(int logLevel) {
        // log level are numerically ordered so can use simple numeric comparison
        return (logLevel >= currentLogLevel);
    }
}
