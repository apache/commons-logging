/*
 * $Header: /home/jerenkrantz/tmp/commons/commons-convert/cvs/home/cvs/jakarta-commons//logging/src/java/org/apache/commons/logging/LogSource.java,v 1.5 2002/01/03 18:58:00 rdonkin Exp $
 * $Revision: 1.5 $
 * $Date: 2002/01/03 18:58:00 $
 *
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1999-2001 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Commons", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
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
 * @version $Id: LogSource.java,v 1.5 2002/01/03 18:58:00 rdonkin Exp $
 */
public class LogSource {

    // --------------------------------------------------------- Class Attributes
    
    static protected HashMap _logs = new HashMap();
    /** Is log4j available (in the current classpath) */
    static protected boolean _log4jIsAvailable = false;
    
    
    // --------------------------------------------------------- Class Initializers
    
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

    /** Constructor for current log class */
    static protected Constructor _logimplctor = null;
    static {
        try {
            setLogImplementation(
                System.getProperty(
                    "org.apache.commons.logging.log","org.apache.commons.logging.NoOpLog"));
                    
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


    // --------------------------------------------------------- Constructor
    
    /** Don't allow others to create instances */
    private LogSource() {
    }

    // --------------------------------------------------------- Class Methods

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


    /** Get a <code>Log</code> instance by class name */
    static public Log getInstance(String name) {
        Log log = (Log)(_logs.get(name));
        if(null == log) {
            log = makeNewLogInstance(name);
            _logs.put(name,log);
        }
        return log;
    }

    /** Get a <code>Log</code> instance by class */
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
     * When <tt>org.apache.commons.logging.log</tt> is not set,
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
