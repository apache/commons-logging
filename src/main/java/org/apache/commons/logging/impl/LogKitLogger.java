/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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
import org.apache.log.Logger;
import org.apache.commons.logging.Log;

/**
 * Implementation of {@code org.apache.commons.logging.Log}
 * that wraps the <a href="http://avalon.apache.org/logkit/">avalon-logkit</a>
 * logging system. Configuration of {@code LogKit} is left to the user.
 * <p>
 * {@code LogKit} accepts only {@code String} messages.
 * Therefore, this implementation converts object messages into strings
 * by called their {@code toString()} method before logging them.
 *
 */
public class LogKitLogger implements Log, Serializable {

    /** Serializable version identifier. */
    private static final long serialVersionUID = 3768538055836059519L;
    protected final LogKitLoggerClass logKitLoggerClass = new LogKitLoggerClass(this);

    // ------------------------------------------------------------- Attributes

    /** Name of this logger */
    protected String name;

    // ------------------------------------------------------------ Constructor

    /**
     * Construct {@code LogKitLogger} which wraps the {@code LogKit}
     * logger with given name.
     *
     * @param name log name
     */
    public LogKitLogger(final String name) {
        this.name = name;
        this.logKitLoggerClass.logger = logKitLoggerClass.getLogger();
    }

    // --------------------------------------------------------- Public Methods

    /**
     * Return the underlying Logger we are using.
     */
    public Logger getLogger() {
        return logKitLoggerClass.getLogger();
    }

    // ----------------------------------------------------- Log Implementation

    /**
     * Logs a message with {@code org.apache.log.Priority.DEBUG}.
     *
     * @param message to log
     * @see org.apache.commons.logging.Log#trace(Object)
    */
    @Override
    public void trace(final Object message) {
        debug(message);
    }

    /**
     * Logs a message with {@code org.apache.log.Priority.DEBUG}.
     *
     * @param message to log
     * @param t log this cause
     * @see org.apache.commons.logging.Log#trace(Object, Throwable)
     */
    @Override
    public void trace(final Object message, final Throwable t) {
        debug(message, t);
    }

    /**
     * Logs a message with {@code org.apache.log.Priority.DEBUG}.
     *
     * @param message to log
     * @see org.apache.commons.logging.Log#debug(Object)
     */
    @Override
    public void debug(final Object message) {
        if (message != null) {
            logKitLoggerClass.getLogger().debug(String.valueOf(message));
        }
    }

    /**
     * Logs a message with {@code org.apache.log.Priority.DEBUG}.
     *
     * @param message to log
     * @param t log this cause
     * @see org.apache.commons.logging.Log#debug(Object, Throwable)
     */
    @Override
    public void debug(final Object message, final Throwable t) {
        if (message != null) {
            logKitLoggerClass.getLogger().debug(String.valueOf(message), t);
        }
    }

    /**
     * Logs a message with {@code org.apache.log.Priority.INFO}.
     *
     * @param message to log
     * @see org.apache.commons.logging.Log#Logger_info(Object)
     */
    @Override
    public void Logger_info(final Object message) {
        if (message != null) {
            logKitLoggerClass.getLogger().info(String.valueOf(message));
        }
    }

    /**
     * Logs a message with {@code org.apache.log.Priority.INFO}.
     *
     * @param message to log
     * @param t log this cause
     * @see org.apache.commons.logging.Log#Logger_info(Object, Throwable)
     */
    @Override
    public void Logger_info(final Object message, final Throwable t) {
        if (message != null) {
            logKitLoggerClass.getLogger().info(String.valueOf(message), t);
        }
    }

    /**
     * Logs a message with {@code org.apache.log.Priority.WARN}.
     *
     * @param message to log
     * @see org.apache.commons.logging.Log#warn(Object)
     */
    @Override
    public void warn(final Object message) {
        if (message != null) {
            logKitLoggerClass.getLogger().warn(String.valueOf(message));
        }
    }

    /**
     * Logs a message with {@code org.apache.log.Priority.WARN}.
     *
     * @param message to log
     * @param t log this cause
     * @see org.apache.commons.logging.Log#warn(Object, Throwable)
     */
    @Override
    public void warn(final Object message, final Throwable t) {
        if (message != null) {
            logKitLoggerClass.getLogger().warn(String.valueOf(message), t);
        }
    }

    /**
     * Logs a message with {@code org.apache.log.Priority.ERROR}.
     *
     * @param message to log
     * @see org.apache.commons.logging.Log#error(Object)
     */
    @Override
    public void error(final Object message) {
        if (message != null) {
            logKitLoggerClass.getLogger().error(String.valueOf(message));
        }
    }

    /**
     * Logs a message with {@code org.apache.log.Priority.ERROR}.
     *
     * @param message to log
     * @param t log this cause
     * @see org.apache.commons.logging.Log#error(Object, Throwable)
     */
    @Override
    public void error(final Object message, final Throwable t) {
        if (message != null) {
            logKitLoggerClass.getLogger().error(String.valueOf(message), t);
        }
    }

    /**
     * Logs a message with {@code org.apache.log.Priority.FATAL_ERROR}.
     *
     * @param message to log
     * @see org.apache.commons.logging.Log#fatal(Object)
     */
    @Override
    public void fatal(final Object message) {
        if (message != null) {
            logKitLoggerClass.getLogger().fatalError(String.valueOf(message));
        }
    }

    /**
     * Logs a message with {@code org.apache.log.Priority.FATAL_ERROR}.
     *
     * @param message to log
     * @param t log this cause
     * @see org.apache.commons.logging.Log#fatal(Object, Throwable)
     */
    @Override
    public void fatal(final Object message, final Throwable t) {
        if (message != null) {
            logKitLoggerClass.getLogger().fatalError(String.valueOf(message), t);
        }
    }

    /**
     * Checks whether the {@code LogKit} logger will log messages of priority {@code DEBUG}.
     */
    @Override
    public boolean isDebugEnabled() {
        return logKitLoggerClass.getLogger().isDebugEnabled();
    }

    /**
     * Checks whether the {@code LogKit} logger will log messages of priority {@code ERROR}.
     */
    @Override
    public boolean isErrorEnabled() {
        return logKitLoggerClass.getLogger().isErrorEnabled();
    }

    /**
     * Checks whether the {@code LogKit} logger will log messages of priority {@code FATAL_ERROR}.
     */
    @Override
    public boolean isFatalEnabled() {
        return logKitLoggerClass.getLogger().isFatalErrorEnabled();
    }

    /**
     * Checks whether the {@code LogKit} logger will log messages of priority {@code INFO}.
     */
    @Override
    public boolean isInfoEnabled() {
        return logKitLoggerClass.getLogger().isInfoEnabled();
    }

    /**
     * Checks whether the {@code LogKit} logger will log messages of priority {@code DEBUG}.
     */
    @Override
    public boolean isTraceEnabled() {
        return logKitLoggerClass.getLogger().isDebugEnabled();
    }

    /**
     * Checks whether the {@code LogKit} logger will log messages of priority {@code WARN}.
     */
    @Override
    public boolean isWarnEnabled() {
        return logKitLoggerClass.getLogger().isWarnEnabled();
    }

    public String getName() {
        return null;
    }
}
