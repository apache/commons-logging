/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software License
 * version 1.1, a copy of which has been included with this distribution in
 * the LICENSE file.
 */

package org.apache.commons.httpclient.log;

/**
 * @author Rod Waldhoff
 * @version $Id: NoOpLog.java,v 1.2 2001/08/02 22:14:41 rwaldhoff Exp $
 */
public final class NoOpLog implements Log {
    public NoOpLog() { }
    public NoOpLog(String name) { }
    public void assert(boolean assertion, String msg) { }
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
}
