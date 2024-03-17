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

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogConfigurationException;

/**
 * Simple implementation of Log that sends all enabled log messages,
 * for all defined loggers, to System.err.  The following system properties
 * are supported to configure the behavior of this logger:
 * <ul>
 * <li>{@code org.apache.commons.logging.simplelog.defaultlog} -
 *     Default logging detail level for all instances of SimpleLog.
 *     Must be one of ("trace", "debug", "info", "warn", "error", or "fatal").
 *     If not specified, defaults to "info". </li>
 * <li>{@code org.apache.commons.logging.simplelog.log.xxxxx} -
 *     Logging detail level for a SimpleLog instance named "xxxxx".
 *     Must be one of ("trace", "debug", "info", "warn", "error", or "fatal").
 *     If not specified, the default logging detail level is used.</li>
 * <li>{@code org.apache.commons.logging.simplelog.showlogname} -
 *     Set to {@code true} if you want the Log instance name to be
 *     included in output messages. Defaults to {@code false}.</li>
 * <li>{@code org.apache.commons.logging.simplelog.showShortLogname} -
 *     Set to {@code true} if you want the last component of the name to be
 *     included in output messages. Defaults to {@code true}.</li>
 * <li>{@code org.apache.commons.logging.simplelog.showdatetime} -
 *     Set to {@code true} if you want the current date and time
 *     to be included in output messages. Default is {@code false}.</li>
 * <li>{@code org.apache.commons.logging.simplelog.dateTimeFormat} -
 *     The date and time format to be used in the output messages.
 *     The pattern describing the date and time format is the same that is
 *     used in {@link java.text.SimpleDateFormat}. If the format is not
 *     specified or is invalid, the default format is used.
 *     The default format is {@code yyyy/MM/dd HH:mm:ss:SSS zzz}.</li>
 * </ul>
 * <p>
 * In addition to looking for system properties with the names specified
 * above, this implementation also checks for a class loader resource named
 * {@code "simplelog.properties"}, and includes any matching definitions
 * from this resource (if it exists).
 */
public class SimpleLog implements Log, Serializable {

    /** Serializable version identifier. */
    private static final long serialVersionUID = 136942970684951178L;

    /** All system properties used by {@code SimpleLog} start with this */
    static protected final String systemPrefix = "org.apache.commons.logging.simplelog.";

    /** Properties loaded from simplelog.properties */
    static protected final Properties simpleLogProps = new Properties();

    /** The default format to use when formating dates */
    static protected final String DEFAULT_DATE_TIME_FORMAT = "yyyy/MM/dd HH:mm:ss:SSS zzz";

    /** Include the instance name in the log message? */
    static volatile protected boolean showLogName;

    /**
     * Include the short name (last component) of the logger in the log
     * message. Defaults to true - otherwise we'll be lost in a flood of
     * messages without knowing who sends them.
     */
    static volatile protected boolean showShortName = true;

    /** Include the current time in the log message */
    static volatile protected boolean showDateTime;

    /** The date and time format to use in the log message */
    static volatile protected String dateTimeFormat = DEFAULT_DATE_TIME_FORMAT;

    /**
     * Used to format times.
     * <p>
     * Any code that accesses this object should first obtain a lock on it,
     * that is, use synchronized(dateFormatter); this requirement was introduced
     * in 1.1.1 to fix an existing thread safety bug (SimpleDateFormat.format
     * is not thread-safe).
     * </p>
     */
    static protected DateFormat dateFormatter;

    /** "Trace" level logging. */
    public static final int LOG_LEVEL_TRACE  = 1;
    /** "Debug" level logging. */
    public static final int LOG_LEVEL_DEBUG  = 2;
    /** "Info" level logging. */
    public static final int LOG_LEVEL_INFO   = 3;
    /** "Warn" level logging. */
    public static final int LOG_LEVEL_WARN   = 4;
    /** "Error" level logging. */
    public static final int LOG_LEVEL_ERROR  = 5;
    /** "Fatal" level logging. */
    public static final int LOG_LEVEL_FATAL  = 6;

    /** Enable all logging levels */
    public static final int LOG_LEVEL_ALL    = LOG_LEVEL_TRACE - 1;

    /** Enable no logging levels */
    public static final int LOG_LEVEL_OFF    = LOG_LEVEL_FATAL + 1;

    // Initialize class attributes.
    // Load properties file, if found.
    // Override with system properties.
    static {
        // Add props from the resource simplelog.properties
        try (InputStream in = getResourceAsStream("simplelog.properties")) {
            if (null != in) {
                simpleLogProps.load(in);
            }
        } catch (final IOException ignore) {
            // Ignore
        }

        showLogName = getBooleanProperty(systemPrefix + "showlogname", showLogName);
        showShortName = getBooleanProperty(systemPrefix + "showShortLogname", showShortName);
        showDateTime = getBooleanProperty(systemPrefix + "showdatetime", showDateTime);

        if (showDateTime) {
            dateTimeFormat = getStringProperty(systemPrefix + "dateTimeFormat", dateTimeFormat);
            try {
                dateFormatter = new SimpleDateFormat(dateTimeFormat);
            } catch (final IllegalArgumentException e) {
                // If the format pattern is invalid - use the default format
                dateTimeFormat = DEFAULT_DATE_TIME_FORMAT;
                dateFormatter = new SimpleDateFormat(dateTimeFormat);
            }
        }
    }

    private static boolean getBooleanProperty(final String name, final boolean defaultValue) {
        final String prop = getStringProperty(name);
        return prop == null ? defaultValue : Boolean.parseBoolean(prop);
    }

    /**
     * Gets the thread context class loader if available. Otherwise return null.
     *
     * The thread context class loader is available if certain security conditions are met.
     *
     * @throws LogConfigurationException if a suitable class loader cannot be identified.
     */
    private static ClassLoader getContextClassLoader() {
        ClassLoader classLoader = null;

        // Get the thread context class loader (if there is one)
        try {
            classLoader = Thread.currentThread().getContextClassLoader();
        } catch (final RuntimeException e) {
            /**
             * getContextClassLoader() throws SecurityException when the context class loader isn't an ancestor of the calling class's class loader, or if
             * security permissions are restricted.
             *
             * In the first case (not related), we want to ignore and keep going. We cannot help but also ignore the second with the logic below, but other
             * calls elsewhere (to obtain a class loader) will trigger this exception where we can make a distinction.
             */
            // Capture 'e.getTargetException()' exception for details
            // alternate: log 'e.getTargetException()', and pass back 'e'.
            if (!(e instanceof SecurityException)) {
                throw new LogConfigurationException("Unexpected SecurityException", e);
            }
        }

        if (classLoader == null) {
            classLoader = SimpleLog.class.getClassLoader();
        }

        // Return the selected class loader
        return classLoader;
    }

    private static InputStream getResourceAsStream(final String name) {
        return AccessController.doPrivileged((PrivilegedAction<InputStream>) () -> {
            final ClassLoader threadCL = getContextClassLoader();
            if (threadCL != null) {
                return threadCL.getResourceAsStream(name);
            }
            return ClassLoader.getSystemResourceAsStream(name);
        });
    }

    private static String getStringProperty(final String name) {
        String prop = null;
        try {
            prop = System.getProperty(name);
        } catch (final SecurityException e) {
            // Ignore
        }
        return prop == null ? simpleLogProps.getProperty(name) : prop;
    }
    private static String getStringProperty(final String name, final String defaultValue) {
        final String prop = getStringProperty(name);
        return prop == null ? defaultValue : prop;
    }
    /** The name of this simple log instance */
    protected volatile String logName;

    /** The current log level */
    protected volatile int currentLogLevel;

    /** The short name of this simple log instance */
    private volatile String shortLogName;

    /**
     * Constructs a simple log with given name.
     *
     * @param name log name
     */
    public SimpleLog(String name) {
        logName = name;

        // Set initial log level
        // Used to be: set default log level to ERROR
        // IMHO it should be lower, but at least info (costin).
        setLevel(SimpleLog.LOG_LEVEL_INFO);

        // Set log level from properties
        String lvl = getStringProperty(systemPrefix + "log." + logName);
        int i = String.valueOf(name).lastIndexOf(".");
        while(null == lvl && i > -1) {
            name = name.substring(0,i);
            lvl = getStringProperty(systemPrefix + "log." + name);
            i = String.valueOf(name).lastIndexOf(".");
        }

        if (null == lvl) {
            lvl =  getStringProperty(systemPrefix + "defaultlog");
        }

        if ("all".equalsIgnoreCase(lvl)) {
            setLevel(SimpleLog.LOG_LEVEL_ALL);
        } else if ("trace".equalsIgnoreCase(lvl)) {
            setLevel(SimpleLog.LOG_LEVEL_TRACE);
        } else if ("debug".equalsIgnoreCase(lvl)) {
            setLevel(SimpleLog.LOG_LEVEL_DEBUG);
        } else if ("info".equalsIgnoreCase(lvl)) {
            setLevel(SimpleLog.LOG_LEVEL_INFO);
        } else if ("warn".equalsIgnoreCase(lvl)) {
            setLevel(SimpleLog.LOG_LEVEL_WARN);
        } else if ("error".equalsIgnoreCase(lvl)) {
            setLevel(SimpleLog.LOG_LEVEL_ERROR);
        } else if ("fatal".equalsIgnoreCase(lvl)) {
            setLevel(SimpleLog.LOG_LEVEL_FATAL);
        } else if ("off".equalsIgnoreCase(lvl)) {
            setLevel(SimpleLog.LOG_LEVEL_OFF);
        }
    }

    /**
     * Logs a message with
     * {@code org.apache.commons.logging.impl.SimpleLog.LOG_LEVEL_DEBUG}.
     *
     * @param message to log
     * @see org.apache.commons.logging.Log#debug(Object)
     */
    @Override
    public final void debug(final Object message) {
        if (isLevelEnabled(SimpleLog.LOG_LEVEL_DEBUG)) {
            log(SimpleLog.LOG_LEVEL_DEBUG, message, null);
        }
    }

    /**
     * Logs a message with
     * {@code org.apache.commons.logging.impl.SimpleLog.LOG_LEVEL_DEBUG}.
     *
     * @param message to log
     * @param t log this cause
     * @see org.apache.commons.logging.Log#debug(Object, Throwable)
     */
    @Override
    public final void debug(final Object message, final Throwable t) {
        if (isLevelEnabled(SimpleLog.LOG_LEVEL_DEBUG)) {
            log(SimpleLog.LOG_LEVEL_DEBUG, message, t);
        }
    }

    /**
     * Logs a message with {@code org.apache.commons.logging.impl.SimpleLog.LOG_LEVEL_ERROR}.
     *
     * @param message to log
     * @see org.apache.commons.logging.Log#error(Object)
     */
    @Override
    public final void error(final Object message) {
        if (isLevelEnabled(SimpleLog.LOG_LEVEL_ERROR)) {
            log(SimpleLog.LOG_LEVEL_ERROR, message, null);
        }
    }

    /**
     * Logs a message with {@code org.apache.commons.logging.impl.SimpleLog.LOG_LEVEL_ERROR}.
     *
     * @param message to log
     * @param t log this cause
     * @see org.apache.commons.logging.Log#error(Object, Throwable)
     */
    @Override
    public final void error(final Object message, final Throwable t) {
        if (isLevelEnabled(SimpleLog.LOG_LEVEL_ERROR)) {
            log(SimpleLog.LOG_LEVEL_ERROR, message, t);
        }
    }

    /**
     * Log a message with {@code org.apache.commons.logging.impl.SimpleLog.LOG_LEVEL_FATAL}.
     *
     * @param message to log
     * @see org.apache.commons.logging.Log#fatal(Object)
     */
    @Override
    public final void fatal(final Object message) {
        if (isLevelEnabled(SimpleLog.LOG_LEVEL_FATAL)) {
            log(SimpleLog.LOG_LEVEL_FATAL, message, null);
        }
    }

    /**
     * Logs a message with {@code org.apache.commons.logging.impl.SimpleLog.LOG_LEVEL_FATAL}.
     *
     * @param message to log
     * @param t log this cause
     * @see org.apache.commons.logging.Log#fatal(Object, Throwable)
     */
    @Override
    public final void fatal(final Object message, final Throwable t) {
        if (isLevelEnabled(SimpleLog.LOG_LEVEL_FATAL)) {
            log(SimpleLog.LOG_LEVEL_FATAL, message, t);
        }
    }

    /**
     * Gets logging level.
     *
     * @return  logging level.
     */
    public int getLevel() {
        return currentLogLevel;
    }

    /**
     * Logs a message with {@code org.apache.commons.logging.impl.SimpleLog.LOG_LEVEL_INFO}.
     *
     * @param message to log
     * @see org.apache.commons.logging.Log#info(Object)
     */
    @Override
    public final void info(final Object message) {
        if (isLevelEnabled(SimpleLog.LOG_LEVEL_INFO)) {
            log(SimpleLog.LOG_LEVEL_INFO,message,null);
        }
    }

    /**
     * Logs a message with {@code org.apache.commons.logging.impl.SimpleLog.LOG_LEVEL_INFO}.
     *
     * @param message to log
     * @param t log this cause
     * @see org.apache.commons.logging.Log#info(Object, Throwable)
     */
    @Override
    public final void info(final Object message, final Throwable t) {
        if (isLevelEnabled(SimpleLog.LOG_LEVEL_INFO)) {
            log(SimpleLog.LOG_LEVEL_INFO, message, t);
        }
    }

    /**
     * Are debug messages currently enabled?
     * <p>
     * This allows expensive operations such as {@code String}
     * concatenation to be avoided when the message will be ignored by the
     * logger.
     */
    @Override
    public final boolean isDebugEnabled() {
        return isLevelEnabled(SimpleLog.LOG_LEVEL_DEBUG);
    }

    /**
     * Are error messages currently enabled?
     * <p>
     * This allows expensive operations such as {@code String}
     * concatenation to be avoided when the message will be ignored by the
     * logger.
     */
    @Override
    public final boolean isErrorEnabled() {
        return isLevelEnabled(SimpleLog.LOG_LEVEL_ERROR);
    }

    /**
     * Are fatal messages currently enabled?
     * <p>
     * This allows expensive operations such as {@code String}
     * concatenation to be avoided when the message will be ignored by the
     * logger.
     */
    @Override
    public final boolean isFatalEnabled() {
        return isLevelEnabled(SimpleLog.LOG_LEVEL_FATAL);
    }

    /**
     * Are info messages currently enabled?
     * <p>
     * This allows expensive operations such as {@code String}
     * concatenation to be avoided when the message will be ignored by the
     * logger.
     */
    @Override
    public final boolean isInfoEnabled() {
        return isLevelEnabled(SimpleLog.LOG_LEVEL_INFO);
    }

    /**
     * Tests whether the given log level currently enabled.
     *
     * @param logLevel is this level enabled?
     * @return whether the given log level currently enabled.
     */
    protected boolean isLevelEnabled(final int logLevel) {
        // log level are numerically ordered so can use simple numeric
        // comparison
        return logLevel >= currentLogLevel;
    }

    /**
     * Are trace messages currently enabled?
     * <p>
     * This allows expensive operations such as {@code String}
     * concatenation to be avoided when the message will be ignored by the
     * logger.
     */
    @Override
    public final boolean isTraceEnabled() {
        return isLevelEnabled(SimpleLog.LOG_LEVEL_TRACE);
    }

    /**
     * Are warn messages currently enabled?
     * <p>
     * This allows expensive operations such as {@code String}
     * concatenation to be avoided when the message will be ignored by the
     * logger.
     */
    @Override
    public final boolean isWarnEnabled() {
        return isLevelEnabled(SimpleLog.LOG_LEVEL_WARN);
    }

    /**
     * Do the actual logging.
     * <p>
     * This method assembles the message and then calls {@code write()}
     * to cause it to be written.
     *
     * @param type One of the LOG_LEVEL_XXX constants defining the log level
     * @param message The message itself (typically a String)
     * @param t The exception whose stack trace should be logged
     */
    protected void log(final int type, final Object message, final Throwable t) {
        // Use a string buffer for better performance
        final StringBuilder buf = new StringBuilder();

        // Append date-time if so configured
        if (showDateTime) {
            final Date now = new Date();
            String dateText;
            synchronized (dateFormatter) {
                dateText = dateFormatter.format(now);
            }
            buf.append(dateText);
            buf.append(" ");
        }

        // Append a readable representation of the log level
        switch (type) {
        case SimpleLog.LOG_LEVEL_TRACE:
            buf.append("[TRACE] ");
            break;
        case SimpleLog.LOG_LEVEL_DEBUG:
            buf.append("[DEBUG] ");
            break;
        case SimpleLog.LOG_LEVEL_INFO:
            buf.append("[INFO] ");
            break;
        case SimpleLog.LOG_LEVEL_WARN:
            buf.append("[WARN] ");
            break;
        case SimpleLog.LOG_LEVEL_ERROR:
            buf.append("[ERROR] ");
            break;
        case SimpleLog.LOG_LEVEL_FATAL:
            buf.append("[FATAL] ");
            break;
        default:
            // Or throw?
            buf.append("[UNDEFINED] ");
            break;
        }

        // Append the name of the log instance if so configured
        if (showShortName) {
            if (shortLogName == null) {
                // Cut all but the last component of the name for both styles
                final String slName = logName.substring(logName.lastIndexOf(".") + 1);
                shortLogName = slName.substring(slName.lastIndexOf("/") + 1);
            }
            buf.append(String.valueOf(shortLogName)).append(" - ");
        } else if (showLogName) {
            buf.append(String.valueOf(logName)).append(" - ");
        }

        // Append the message
        buf.append(String.valueOf(message));

        // Append stack trace if not null
        if (t != null) {
            buf.append(" <");
            buf.append(t.toString());
            buf.append(">");

            final StringWriter sw = new StringWriter(1024);
            try (PrintWriter pw = new PrintWriter(sw)) {
                t.printStackTrace(pw);
            }
            buf.append(sw.toString());
        }

        // Print to the appropriate destination
        write(buf);
    }

    /**
     * Sets logging level.
     *
     * @param currentLogLevel new logging level
     */
    public void setLevel(final int currentLogLevel) {
        this.currentLogLevel = currentLogLevel;
    }

    /**
     * Logs a message with {@code org.apache.commons.logging.impl.SimpleLog.LOG_LEVEL_TRACE}.
     *
     * @param message to log
     * @see org.apache.commons.logging.Log#trace(Object)
     */
    @Override
    public final void trace(final Object message) {
        if (isLevelEnabled(SimpleLog.LOG_LEVEL_TRACE)) {
            log(SimpleLog.LOG_LEVEL_TRACE, message, null);
        }
    }

    /**
     * Logs a message with {@code org.apache.commons.logging.impl.SimpleLog.LOG_LEVEL_TRACE}.
     *
     * @param message to log
     * @param t log this cause
     * @see org.apache.commons.logging.Log#trace(Object, Throwable)
     */
    @Override
    public final void trace(final Object message, final Throwable t) {
        if (isLevelEnabled(SimpleLog.LOG_LEVEL_TRACE)) {
            log(SimpleLog.LOG_LEVEL_TRACE, message, t);
        }
    }

    /**
     * Logs a message with {@code org.apache.commons.logging.impl.SimpleLog.LOG_LEVEL_WARN}.
     *
     * @param message to log
     * @see org.apache.commons.logging.Log#warn(Object)
     */
    @Override
    public final void warn(final Object message) {
        if (isLevelEnabled(SimpleLog.LOG_LEVEL_WARN)) {
            log(SimpleLog.LOG_LEVEL_WARN, message, null);
        }
    }

    /**
     * Logs a message with {@code org.apache.commons.logging.impl.SimpleLog.LOG_LEVEL_WARN}.
     *
     * @param message to log
     * @param t log this cause
     * @see org.apache.commons.logging.Log#warn(Object, Throwable)
     */
    @Override
    public final void warn(final Object message, final Throwable t) {
        if (isLevelEnabled(SimpleLog.LOG_LEVEL_WARN)) {
            log(SimpleLog.LOG_LEVEL_WARN, message, t);
        }
    }

    /**
     * Write the content of the message accumulated in the specified
     * {@code StringBuffer} to the appropriate output destination.  The
     * default implementation writes to {@code System.err}.
     *
     * @param buffer A {@code StringBuffer} containing the accumulated
     *  text to be logged
     */
    private void write(final Object buffer) {
        System.err.println(buffer.toString());
    }

    /**
     * Write the content of the message accumulated in the specified
     * {@code StringBuffer} to the appropriate output destination.  The
     * default implementation writes to {@code System.err}.
     *
     * @param buffer A {@code StringBuffer} containing the accumulated
     *  text to be logged
     */
    protected void write(final StringBuffer buffer) {
        System.err.println(buffer.toString());
    }
}

