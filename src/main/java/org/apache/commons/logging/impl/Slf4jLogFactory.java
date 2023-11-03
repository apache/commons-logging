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

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogConfigurationException;
import org.apache.commons.logging.LogFactory;
import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.slf4j.spi.LocationAwareLogger;

import static org.slf4j.spi.LocationAwareLogger.DEBUG_INT;
import static org.slf4j.spi.LocationAwareLogger.ERROR_INT;
import static org.slf4j.spi.LocationAwareLogger.INFO_INT;
import static org.slf4j.spi.LocationAwareLogger.TRACE_INT;
import static org.slf4j.spi.LocationAwareLogger.WARN_INT;

/**
 * Logger factory hardcoded to send everything to SLF4J.
 *
 * @since 1.3
 */
public final class Slf4jLogFactory extends LogFactory {

    private static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];
    private static final String[] EMPTY_STRING_ARRAY = new String[0];

    /**
     * Marker used by all messages coming from Apache Commons Logging.
     */
    private static final Marker MARKER = MarkerFactory.getMarker("COMMONS-LOGGING");

    /**
     * Caches Log instances.
     * <p>
     * The SLF4J reference implementation (Logback) has a single logger context, so each call to
     * {@link #getInstance(String)}
     * should give the same result.
     * </p>
     */
    private final ConcurrentMap<String, Log> loggers = new ConcurrentHashMap<>();

    private final ConcurrentMap<String, Object> attributes = new ConcurrentHashMap<>();

    @Override
    public Log getInstance(final String name) {
        return loggers.computeIfAbsent(name, n -> {
            final Logger logger = LoggerFactory.getLogger(n);
            return logger instanceof LocationAwareLogger ? new Slf4jLocationAwareLog((LocationAwareLogger) logger) : new Slf4jLog(
                    logger);
        });
    }

    @Override
    public Object getAttribute(final String name) {
        return attributes.get(name);
    }

    @Override
    public String[] getAttributeNames() {
        return attributes.keySet().toArray(EMPTY_STRING_ARRAY);
    }

    @Override
    public Log getInstance(final Class clazz) throws LogConfigurationException {
        return getInstance(clazz.getName());
    }

    /**
     * This method is supposed to clear all loggers.
     * <p>
     * In this implementation it calls a "stop" method if the logger factory supports it. This is the case of
     * Logback.
     * </p>
     */
    @Override
    public void release() {
        final ILoggerFactory factory = LoggerFactory.getILoggerFactory();
        try {
            factory.getClass().getMethod("stop").invoke(factory);
        } catch (final ReflectiveOperationException ignored) {
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

    private static class Slf4jLog implements Log {

        private final Logger logger;

        public Slf4jLog(final Logger logger) {
            this.logger = logger;
        }

        @Override
        public void debug(Object message) {
            logger.debug(MARKER, String.valueOf(message));
        }

        @Override
        public void debug(Object message, Throwable t) {
            logger.debug(MARKER, String.valueOf(message), t);
        }

        @Override
        public void error(Object message) {
            logger.error(MARKER, String.valueOf(message));
        }

        @Override
        public void error(Object message, Throwable t) {
            logger.debug(MARKER, String.valueOf(message), t);
        }

        @Override
        public void fatal(Object message) {
            error(message);
        }

        @Override
        public void fatal(Object message, Throwable t) {
            error(message, t);
        }

        @Override
        public void info(Object message) {
            logger.info(MARKER, String.valueOf(message));
        }

        @Override
        public void info(Object message, Throwable t) {
            logger.info(MARKER, String.valueOf(message), t);
        }

        @Override
        public boolean isDebugEnabled() {
            return logger.isDebugEnabled(MARKER);
        }

        @Override
        public boolean isErrorEnabled() {
            return logger.isErrorEnabled(MARKER);
        }

        @Override
        public boolean isFatalEnabled() {
            return isErrorEnabled();
        }

        @Override
        public boolean isInfoEnabled() {
            return logger.isInfoEnabled(MARKER);
        }

        @Override
        public boolean isTraceEnabled() {
            return logger.isTraceEnabled(MARKER);
        }

        @Override
        public boolean isWarnEnabled() {
            return logger.isWarnEnabled(MARKER);
        }

        @Override
        public void trace(Object message) {
            logger.trace(MARKER, String.valueOf(message));
        }

        @Override
        public void trace(Object message, Throwable t) {
            logger.trace(MARKER, String.valueOf(message), t);
        }

        @Override
        public void warn(Object message) {
            logger.warn(MARKER, String.valueOf(message));
        }

        @Override
        public void warn(Object message, Throwable t) {
            logger.warn(MARKER, String.valueOf(message), t);
        }
    }

    private static final class Slf4jLocationAwareLog implements Log {

        private static final String FQCN = Slf4jLocationAwareLog.class.getName();

        private final LocationAwareLogger logger;

        public Slf4jLocationAwareLog(final LocationAwareLogger logger) {
            this.logger = logger;
        }

        @Override
        public void debug(Object message) {
            log(DEBUG_INT, message, null);
        }

        @Override
        public void debug(Object message, Throwable t) {
            log(DEBUG_INT, message, t);
        }

        @Override
        public void error(Object message) {
            log(ERROR_INT, message, null);
        }

        @Override
        public void error(Object message, Throwable t) {
            log(ERROR_INT, message, t);
        }

        @Override
        public void fatal(Object message) {
            error(message);
        }

        @Override
        public void fatal(Object message, Throwable t) {
            error(message, t);
        }

        @Override
        public void info(Object message) {
            log(INFO_INT, message, null);
        }

        @Override
        public void info(Object message, Throwable t) {
            log(INFO_INT, message, t);
        }


        @Override
        public boolean isDebugEnabled() {
            return logger.isDebugEnabled(MARKER);
        }

        @Override
        public boolean isErrorEnabled() {
            return logger.isErrorEnabled(MARKER);
        }

        @Override
        public boolean isFatalEnabled() {
            return isErrorEnabled();
        }

        @Override
        public boolean isInfoEnabled() {
            return logger.isInfoEnabled(MARKER);
        }

        @Override
        public boolean isTraceEnabled() {
            return logger.isTraceEnabled(MARKER);
        }

        @Override
        public boolean isWarnEnabled() {
            return logger.isWarnEnabled(MARKER);
        }

        @Override
        public void trace(Object message) {
            log(TRACE_INT, message, null);
        }

        @Override
        public void trace(Object message, Throwable t) {
            log(TRACE_INT, message, t);
        }

        @Override
        public void warn(Object message) {
            log(WARN_INT, message, null);
        }

        @Override
        public void warn(Object message, Throwable t) {
            log(WARN_INT, message, t);
        }

        private void log(final int level, final Object message, final Throwable t) {
            logger.log(MARKER, FQCN, level, String.valueOf(message), EMPTY_OBJECT_ARRAY, t);
        }
    }
}
