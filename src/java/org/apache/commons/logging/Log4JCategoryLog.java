/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software License
 * version 1.1, a copy of which has been included with this distribution in
 * the LICENSE file.
 */

package org.apache.commons.logging;

import org.apache.log4j.Category;
import org.apache.log4j.Priority;

/**
 * <p>Implementation of {@link Log} that maps directly to a Log4J
 * <strong>Category</strong>.  Initial configuration of the corresponding
 * Category instances should be done in the usual manner, as outlined in
 * the Log4J documentation.</p>
 *
 * @author Rod Waldhoff
 * @version $Id: Log4JCategoryLog.java,v 1.6 2001/12/04 04:28:03 craigmcc Exp $
 */
public class Log4JCategoryLog  implements Log {
    Category _category = null;

    public Log4JCategoryLog(String name) {
        _category = Category.getInstance(name);
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

    public final void setLevel(int level) {
        switch(level) {
            case Log.DEBUG:
                _category.setPriority(Priority.DEBUG);
                break;
            case Log.INFO:
                _category.setPriority(Priority.INFO);
                break;
            case Log.WARN:
                _category.setPriority(Priority.WARN);
                break;
            case Log.ERROR:
                _category.setPriority(Priority.ERROR);
                break;
            case Log.FATAL:
                _category.setPriority(Priority.FATAL);
                break;
            default:
                _category.setPriority(Priority.toPriority(level));
                break;
        }
    }

    public final int getLevel() {
        return _category.getPriority().toInt();
    }

}
