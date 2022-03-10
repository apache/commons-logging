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
import org.apache.commons.logging.Log;

/**
 * Trivial implementation of Log that throws away all messages.  No
 * configurable system properties are supported.
 *
 */
public class NoOpLog implements Log, Serializable {

    /** Serializable version identifier. */
    private static final long serialVersionUID = 561423906191706148L;

    /** Convenience constructor */
    public NoOpLog() { }
    /** Base constructor */
    public NoOpLog(final String name) { }
    /** Do nothing */
    @Override
    public void trace(final Object message) { }
    /** Do nothing */
    @Override
    public void trace(final Object message, final Throwable t) { }
    /** Do nothing */
    @Override
    public void debug(final Object message) { }
    /** Do nothing */
    @Override
    public void debug(final Object message, final Throwable t) { }
    /** Do nothing */
    @Override
    public void info(final Object message) { }
    /** Do nothing */
    @Override
    public void info(final Object message, final Throwable t) { }
    /** Do nothing */
    @Override
    public void warn(final Object message) { }
    /** Do nothing */
    @Override
    public void warn(final Object message, final Throwable t) { }
    /** Do nothing */
    @Override
    public void error(final Object message) { }
    /** Do nothing */
    @Override
    public void error(final Object message, final Throwable t) { }
    /** Do nothing */
    @Override
    public void fatal(final Object message) { }
    /** Do nothing */
    @Override
    public void fatal(final Object message, final Throwable t) { }

    /**
     * Debug is never enabled.
     *
     * @return false
     */
    @Override
    public final boolean isDebugEnabled() { return false; }

    /**
     * Error is never enabled.
     *
     * @return false
     */
    @Override
    public final boolean isErrorEnabled() { return false; }

    /**
     * Fatal is never enabled.
     *
     * @return false
     */
    @Override
    public final boolean isFatalEnabled() { return false; }

    /**
     * Info is never enabled.
     *
     * @return false
     */
    @Override
    public final boolean isInfoEnabled() { return false; }

    /**
     * Trace is never enabled.
     *
     * @return false
     */
    @Override
    public final boolean isTraceEnabled() { return false; }

    /**
     * Warn is never enabled.
     *
     * @return false
     */
    @Override
    public final boolean isWarnEnabled() { return false; }
}
