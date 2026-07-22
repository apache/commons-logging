/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.logging.impl;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.apache.logging.log4j.spi.AbstractLoggerAdapter;
import org.apache.logging.log4j.spi.ExtendedLogger;
import org.apache.logging.log4j.spi.LoggerAdapter;
import org.apache.logging.log4j.spi.LoggerContext;
import org.apache.logging.log4j.util.StackLocatorUtil;

/**
 * Logger factory hardcoded to send everything to Log4j API.
 * <p>
 * Based on the `log4j-jcl` artifact from Apache Logging Services.
 * </p>
 *
 * @since 1.3.0
 */
public final class Log4jApiLogFactory extends LogFactory {

    private static final class Log4j2Log implements Log {

        private static final String FQCN = Log4j2Log.class.getName();

        private final ExtendedLogger logger;

        public Log4j2Log(final ExtendedLogger logger) {
            this.logger = logger;
        }

        @Override
        public void debug(final Object message) {
            logIfEnabled(Level.DEBUG, message, null);
        }

        @Override
        public void debug(final Object message, final Throwable t) {
            logIfEnabled(Level.DEBUG, message, t);
        }

        @Override
        public void error(final Object message) {
            logIfEnabled(Level.ERROR, message, null);
        }

        @Override
        public void error(final Object message, final Throwable t) {
            logIfEnabled(Level.ERROR, message, t);
        }

        @Override
        public void fatal(final Object message) {
            logIfEnabled(Level.FATAL, message, null);
        }

        @Override
        public void fatal(final Object message, final Throwable t) {
            logIfEnabled(Level.FATAL, message, t);
        }

        @Override
        public void info(final Object message) {
            logIfEnabled(Level.INFO, message, null);
        }

        @Override
        public void info(final Object message, final Throwable t) {
            logIfEnabled(Level.INFO, message, t);
        }

        @Override
        public boolean isDebugEnabled() {
            return isEnabled(Level.DEBUG);
        }

        private boolean isEnabled(final Level level) {
            return logger.isEnabled(level, MARKER, null);
        }

        @Override
        public boolean isErrorEnabled() {
            return isEnabled(Level.ERROR);
        }

        @Override
        public boolean isFatalEnabled() {
            return isEnabled(Level.FATAL);
        }

        @Override
        public boolean isInfoEnabled() {
            return isEnabled(Level.INFO);
        }

        @Override
        public boolean isTraceEnabled() {
            return isEnabled(Level.TRACE);
        }

        @Override
        public boolean isWarnEnabled() {
            return isEnabled(Level.WARN);
        }

        private void logIfEnabled(final Level level, final Object message, final Throwable t) {
            if (message instanceof CharSequence) {
                logger.logIfEnabled(FQCN, level, MARKER, (CharSequence) message, t);
            } else {
                logger.logIfEnabled(FQCN, level, MARKER, message, t);
            }
        }

        @Override
        public void trace(final Object message) {
            logIfEnabled(Level.TRACE, message, null);
        }

        @Override
        public void trace(final Object message, final Throwable t) {
            logIfEnabled(Level.TRACE, message, t);
        }

        @Override
        public void warn(final Object message) {
            logIfEnabled(Level.WARN, message, null);
        }

        @Override
        public void warn(final Object message, final Throwable t) {
            logIfEnabled(Level.WARN, message, t);
        }
    }
    private static final class LogAdapter extends AbstractLoggerAdapter<Log> {

        @Override
        protected LoggerContext getContext() {
            return getContext(LogManager.getFactory().isClassLoaderDependent() ? StackLocatorUtil.getCallerClass(
                    LogFactory.class) : null);
        }

        @Override
        protected Log newLogger(final String name, final LoggerContext context) {
            return new Log4j2Log(context.getLogger(name));
        }

    }

    private static final String[] EMPTY_ARRAY = {};

    /**
     * Marker used by all messages coming from Apache Commons Logging.
     */
    private static final Marker MARKER = MarkerManager.getMarker("COMMONS-LOGGING");

    /**
     * Caches Log instances
     */
    private final LoggerAdapter<Log> adapter = new LogAdapter();

    private final ConcurrentMap<String, Object> attributes = new ConcurrentHashMap<>();

    /**
     * Constructs a new instance.
     */
    public Log4jApiLogFactory() {
        // empty
    }

    @Override
    public Object getAttribute(final String name) {
        return attributes.get(name);
    }

    @Override
    public String[] getAttributeNames() {
        return attributes.keySet().toArray(EMPTY_ARRAY);
    }

    @Override
    public Log getInstance(final Class<?> clazz) {
        return getInstance(clazz.getName());
    }

    @Override
    public Log getInstance(final String name) {
        return adapter.getLogger(name);
    }

    /**
     * This method is supposed to clear all loggers. In this implementation it will clear all the logger
     * wrappers but the loggers managed by the underlying logger context will not be.
     */
    @Override
    public void release() {
        try {
            adapter.close();
        } catch (final IOException ignored) {
            // Ignore
        }
    }

    @Override
    public void removeAttribute(final String name) {
        attributes.remove(name);
    }

    @Override
    public void setAttribute(final String name, final Object value) {
        if (value != null) {
            attributes.put(name, value);
        } else {
            removeAttribute(name);
        }
    }
}
