/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software License
 * version 1.1, a copy of which has been included with this distribution in
 * the LICENSE file.
 */

package org.apache.commons.httpclient.log;

import org.apache.log4j.Category;
import org.apache.log4j.Priority;

/**
 * @author Rod Waldhoff
 * @version $Id: Log4JCategoryLog.java,v 1.2 2001/08/02 22:14:41 rwaldhoff Exp $
 */
public class Log4JCategoryLog  implements Log {
    Category _category = null;

    public Log4JCategoryLog(String name) {
        _category = Category.getInstance(name);
    }

    public final void assert(boolean assertion, String msg) {
        _category.assert(assertion,msg);
    }

    public final void debug(Object message) {
        _category.debug(message);
    }

    public final void debug(Object message, Throwable t) {
        _category.debug(message,t);
    }

    public final void info(Object message) {
        _category.info(message);
    }

    public final void info(Object message, Throwable t) {
        _category.info(message,t);
    }

    public final void warn(Object message) {
        _category.warn(message);
    }
    public final void warn(Object message, Throwable t) {
        _category.warn(message,t);
    }

    public final void error(Object message) {
        _category.error(message);
    }

    public final void error(Object message, Throwable t) {
        _category.error(message,t);
    }

    public final void fatal(Object message) {
        _category.fatal(message);
    }

    public final void fatal(Object message, Throwable t) {
        _category.fatal(message,t);
    }

    public final boolean isDebugEnabled() {
        return _category.isDebugEnabled();
    }

    public final boolean isInfoEnabled() {
        return _category.isInfoEnabled();
    }

    public final boolean isEnabledFor(Priority p) {
        return _category.isEnabledFor(p);
    }
}
