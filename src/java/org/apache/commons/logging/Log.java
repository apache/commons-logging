/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software License
 * version 1.1, a copy of which has been included with this distribution in
 * the LICENSE file.
 */

package org.apache.commons.logging;

/**
 * A simple logging interface abstracting logging APIs.  In order to be
 * instantiated successfully by {@link LogFactory}, classes that implement
 * this interface must have a constructor that takes a single String
 * parameter representing the "name" of this Log.
 *
 * @author Rod Waldhoff
 * @version $Id: Log.java,v 1.6 2001/12/04 04:28:03 craigmcc Exp $
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
    public void setLevel(int level);
    public int getLevel();

    /** All logging level. */
    public static final int ALL  = Integer.MIN_VALUE;
    /** "Debug" level logging. */
    public static final int DEBUG  = 10000;
    /** "Info" level logging. */
    public static final int INFO   = 20000;
    /** "Warn" level logging. */
    public static final int WARN   = 30000;
    /** "Error" level logging. */
    public static final int ERROR  = 40000;
    /** "Fatal" level logging. */
    public static final int FATAL  = 50000;
    /** No logging level. */
    public static final int OFF  = Integer.MAX_VALUE;
}
