/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software License
 * version 1.1, a copy of which has been included with this distribution in
 * the LICENSE file.
 */

package org.apache.commons.logging;

/**
 * <p>Default implementation of Log that throws away all messages.  No
 * configurable system properties are supported.</p>
 *
 * @author Rod Waldhoff
 * @version $Id: NoOpLog.java,v 1.6 2001/12/04 04:28:03 craigmcc Exp $
 */
public final class NoOpLog implements Log {
    public NoOpLog() { }
    public NoOpLog(String name) { }
    public void debug(Object message) { }
    public void debug(Object message, Throwable t) { }
    public void info(Object message) { }
    public void info(Object message, Throwable t) { }
    public void warn(Object message) { }
    public void warn(Object message, Throwable t) { }
    public void error(Object message) { }
    public void error(Object message, Throwable t) { }
    public void fatal(Object message) { }
    public void fatal(Object message, Throwable t) { }
    public final boolean isDebugEnabled() { return false; }
    public final boolean isInfoEnabled() { return false; }
    public final void setLevel(int level) { }
    public final int getLevel() { return Log.OFF; }
}
