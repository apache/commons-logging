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

import java.io.Serializable;
import org.apache.avalon.framework.logger.Logger;
import org.apache.commons.logging.Log;

/**
 * Implementation of commons-logging Log interface that delegates all
 * logging calls to the Avalon logging abstraction: the Logger interface.
 *
 * @author <a href="mailto:neeme@apache.org">Neeme Praks</a>
 * @version $Revision: 1.8 $ $Date: 2004/02/28 21:46:45 $
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
