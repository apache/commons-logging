/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software License
 * version 1.1, a copy of which has been included with this distribution in
 * the LICENSE file.
 */

package org.apache.commons.logging;

import java.util.HashMap;
import java.lang.reflect.Constructor;
import java.util.Iterator;
import java.lang.reflect.InvocationTargetException;

/**
 * <p>Factory for creating {@link Log} instances.  Applications should call
 * the <code>makeNewLogInstance()</code> method to instantiate new instances
 * of the configured {@link Log} implementation class.</p>
 *
 * @author Rod Waldhoff
 * @version $Id: LogSource.java,v 1.4 2001/12/04 04:28:03 craigmcc Exp $
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
        } catch(ExceptionInInitializerError e) {
            _log4jIsAvailable = false;
        } catch(LinkageError e) {
            _log4jIsAvailable = false;
        }
    }

    static protected Constructor _logimplctor = null;
    static {
        try {
            setLogImplementation(System.getProperty("org.apache.commons.logging.log","org.apache.commons.logging.NoOpLog"));
        } catch(SecurityException e) {
            _logimplctor = null;
        } catch(LinkageError e) {
            _logimplctor = null;
        } catch(NoSuchMethodException e) {
            _logimplctor = null;
        } catch(ClassNotFoundException e) {
            _logimplctor = null;
        }
    }

    private LogSource() {
    }

    /**
     * Set the log implementation/log implementation factory
     * by the name of the class.  The given class
     * must implement {@link Log}, and provide a constructor that
     * takes a single {@link String} argument (containing the name
     * of the log).
     */
    static public void setLogImplementation(String classname) throws
                       LinkageError, ExceptionInInitializerError,
                       NoSuchMethodException, SecurityException,
                       ClassNotFoundException {
        Class logclass = Class.forName(classname);
        Class[] argtypes = new Class[1];
        argtypes[0] = "".getClass();
        _logimplctor = logclass.getConstructor(argtypes);
    }

    /**
     * Set the log implementation/log implementation factory
     * by class.  The given class must implement {@link Log},
     * and provide a constructor that takes a single {@link String}
     * argument (containing the name of the log).
     */
    static public void setLogImplementation(Class logclass) throws
                       LinkageError, ExceptionInInitializerError,
                       NoSuchMethodException, SecurityException {
        Class[] argtypes = new Class[1];
        argtypes[0] = "".getClass();
        _logimplctor = logclass.getConstructor(argtypes);
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
     * <tt>org.apache.commons.logging.log</tt> property.
     * The value of <tt>org.apache.commons.logging.log</tt> may be set to
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
        try {
            Object[] args = new Object[1];
            args[0] = name;
            log = (Log)(_logimplctor.newInstance(args));
        } catch (InstantiationException e) {
            log = null;
        } catch (IllegalAccessException e) {
            log = null;
        } catch (IllegalArgumentException e) {
            log = null;
        } catch (InvocationTargetException e) {
            log = null;
        } catch (NullPointerException e) {
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

    /**
     * Sets the log level for all {@link Log}s known
     * to me.
     */
    static public void setLevel(int level) {
        Iterator it = _logs.entrySet().iterator();
        while(it.hasNext()) {
            Log log = (Log)(it.next());
            log.setLevel(level);
        }
    }

    /**
     * Returns a {@link String} array containing the names of
     * all logs known to me.
     */
    static public String[] getLogNames() {
        return (String[])(_logs.keySet().toArray(new String[_logs.size()]));
    }

}
