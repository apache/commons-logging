/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software License
 * version 1.1, a copy of which has been included with this distribution in
 * the LICENSE file.
 */

package org.apache.commons.httpclient.log;

import java.util.HashMap;
import java.lang.reflect.Constructor;

/**
 * @author Rod Waldhoff
 * @version $Id: LogSource.java,v 1.1 2001/08/02 16:27:06 rwaldhoff Exp $
 */
public class LogSource {
    static protected HashMap _logs = new HashMap();
    static protected boolean _log4jIsAvailable = false;
    static {
        try {
            if(null != Class.forName("org.apache.log4j.Category")) {
                _log4jIsAvailable = true;
            } else {
                _log4jIsAvailable = false;
            }
        } catch(ClassNotFoundException e) {
            _log4jIsAvailable = false;
        }
    }

    private LogSource() {
    }

    static public Log getInstance(String name) {
        Log log = (Log)(_logs.get(name));
        if(null == log) {
            log = makeNewLogInstance(name);
            _logs.put(name,log);
        }
        return log;
    }

    static public Log getInstance(Class clazz) {
        return getInstance(clazz.getName());
    }

    /**
     * Create a new {@link Log} implementation, based
     * on the given <i>name</i>
     * <p>
     * The specific {@link Log} implementation returned
     * is determined by the value of the
     * <tt>httpclient.log</tt> property.
     * The value of <tt>httpclient.log</tt> may be set to
     * the fully specified name of a class that implements
     * the {@link Log} interface.  This class must also
     * have a public constructor that takes a single
     * {@link String} argument (containing the <i>name</i>
     * of the {@link Log} to be constructed.
     * <p>
     * When <tt>httpclient.log</tt> is not set,
     * or when no corresponding class can be found,
     * this method will return a {@link Log4JCategoryLog}
     * if the log4j {@link org.apache.log4j.Category} class is
     * available in the {@link LogSource}'s classpath, or
     * a {@link NoOpLog} if it is not.
     *
     * @param name the log name (or category)
     */
    static public Log makeNewLogInstance(String name) {
        Log log = null;
        String logclassname = System.getProperty("httpclient.log","org.apache.commons.httpclient.log.NoOpLog");
        try {
            Class logclass = Class.forName(logclassname);
            Class[] argtypes = new Class[1];
            argtypes[0] = "".getClass();
            Constructor ctor = logclass.getConstructor(argtypes);
            Object[] args = new Object[1];
            args[0] = name;
            log = (Log)(ctor.newInstance(args));
        } catch(Exception e) {
            log = null;
        }
        if(null == log) {
            if(_log4jIsAvailable) {
                return new Log4JCategoryLog(name);
            } else {
                log = new NoOpLog(name);
            }
        }
        return log;
    }
}
