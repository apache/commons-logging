/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software License
 * version 1.1, a copy of which has been included with this distribution in
 * the LICENSE file.
 */

package org.apache.commons.httpclient.log;

/**
 * A simple logging interface abstracting log4j.
 * @author Rod Waldhoff
 * @version $Id: Log.java,v 1.3 2001/08/07 17:37:22 rwaldhoff Exp $
 */
public interface Log {
    public void debug(Object message);
    public void debug(Object message, Throwable t);
    public void info(Object message);
    public void info(Object message, Throwable t);
    public void warn(Object message);
    public void warn(Object message, Throwable t);
    public void error(Object message);
    public void error(Object message, Throwable t);
    public void fatal(Object message);
    public void fatal(Object message, Throwable t);
    public boolean isDebugEnabled();
    public boolean isInfoEnabled();
}
