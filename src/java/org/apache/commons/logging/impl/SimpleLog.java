/*
 * $Header: /home/jerenkrantz/tmp/commons/commons-convert/cvs/home/cvs/jakarta-commons//logging/src/java/org/apache/commons/logging/impl/SimpleLog.java,v 1.13 2003/10/05 15:58:38 rdonkin Exp $
 * $Revision: 1.13 $
 * $Date: 2003/10/05 15:58:38 $
 *
 * ====================================================================
 * 
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001-2003 The Apache Software Foundation.  All rights
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
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgement:  
 *       "This product includes software developed by the 
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "Apache", "The Jakarta Project", "Commons", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written 
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache" nor may "Apache" appear in their names without prior 
 *    written permission of the Apache Software Foundation.
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


package org.apache.commons.logging.impl;

import java.io.InputStream;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogConfigurationException;

/**
 * <p>Simple implementation of Log that sends all enabled log messages,
 * for all defined loggers, to System.err.  The following system properties
 * are supported to configure the behavior of this logger:</p>
 * <ul>
 * <li><code>org.apache.commons.logging.simplelog.defaultlog</code> -
 *     Default logging detail level for all instances of SimpleLog.
 *     Must be one of ("trace", "debug", "info", "warn", "error", or "fatal").
 *     If not specified, defaults to "info". </li>
 * <li><code>org.apache.commons.logging.simplelog.log.xxxxx</code> -
 *     Logging detail level for a SimpleLog instance named "xxxxx".
 *     Must be one of ("trace", "debug", "info", "warn", "error", or "fatal").
 *     If not specified, the default logging detail level is used.</li>
 * <li><code>org.apache.commons.logging.simplelog.showlogname</code> -
 *     Set to <code>true</code> if you want the Log instance name to be
 *     included in output messages. Defaults to <code>false</code>.</li>
 * <li><code>org.apache.commons.logging.simplelog.showShortLogname</code> -
 *     Set to <code>true</code> if you want the last componet of the name to be
 *     included in output messages. Defaults to <code>true</code>.</li>
 * <li><code>org.apache.commons.logging.simplelog.showdatetime</code> -
 *     Set to <code>true</code> if you want the current date and time
 *     to be included in output messages. Default is false.</li>
 * </ul>
 *
 * <p>In addition to looking for system properties with the names specified
 * above, this implementation also checks for a class loader resource named
 * <code>"simplelog.properties"</code>, and includes any matching definitions
 * from this resource (if it exists).</p>
 *
 * @author <a href="mailto:sanders@apache.org">Scott Sanders</a>
 * @author Rod Waldhoff
 * @author Robert Burrell Donkin
 *
 * @version $Id: SimpleLog.java,v 1.13 2003/10/05 15:58:38 rdonkin Exp $
 */
public class SimpleLog implements Log, Serializable {


    // ------------------------------------------------------- Class Attributes

    /** All system properties used by <code>Simple</code> start with this */
    static protected final String systemPrefix =
        "org.apache.commons.logging.simplelog.";

    /** Properties loaded from simplelog.properties */
    static protected final Properties simpleLogProps = new Properties();

    /** Include the instance name in the log message? */
    static protected boolean showLogName = false;
    /** Include the short name ( last component ) of the logger in the log
        message. Default to true - otherwise we'll be lost in a flood of
        messages without knowing who sends them.
    */
    static protected boolean showShortName = true;
    /** Include the current time in the log message */
    static protected boolean showDateTime = false;
    /** Used to format times */
    static protected DateFormat dateFormatter = null;

    // ---------------------------------------------------- Log Level Constants


    /** "Trace" level logging. */
    public static final int LOG_LEVEL_TRACE  = 1;
    /** "Debug" level logging. */
    public static final int LOG_LEVEL_DEBUG  = 2;
    /** "Info" level logging. */
    public static final int LOG_LEVEL_INFO   = 3;
    /** "Warn" level logging. */
    public static final int LOG_LEVEL_WARN   = 4;
    /** "Error" level logging. */
    public static final int LOG_LEVEL_ERROR  = 5;
    /** "Fatal" level logging. */
    public static final int LOG_LEVEL_FATAL  = 6;

    /** Enable all logging levels */
    public static final int LOG_LEVEL_ALL    = (LOG_LEVEL_TRACE - 1);

    /** Enable no logging levels */
    public static final int LOG_LEVEL_OFF    = (LOG_LEVEL_FATAL + 1);

    // ------------------------------------------------------------ Initializer
    
    private static String getStringProperty(String name) {
        String prop = null;
	try {
	    prop = System.getProperty(name);
	} catch (SecurityException e) {
	    ; // Ignore
	}
        return (prop == null) ? simpleLogProps.getProperty(name) : prop;
    }

    private static String getStringProperty(String name, String dephault) {
        String prop = getStringProperty(name);
        return (prop == null) ? dephault : prop;
    }

    private static boolean getBooleanProperty(String name, boolean dephault) {
        String prop = getStringProperty(name);
        return (prop == null) ? dephault : "true".equalsIgnoreCase(prop);
    }

    // initialize class attributes
    // load properties file, if found.
    // override with system properties.
    static {
        // add props from the resource simplelog.properties
        InputStream in = getResourceAsStream("simplelog.properties");
        if(null != in) {
            try {
                simpleLogProps.load(in);
                in.close();
            } catch(java.io.IOException e) {
                // ignored
            }
        }

        showLogName = getBooleanProperty( systemPrefix + "showlogname", showLogName);
        showShortName = getBooleanProperty( systemPrefix + "showShortLogname", showShortName);
        showDateTime = getBooleanProperty( systemPrefix + "showdatetime", showDateTime);
        showLogName = getBooleanProperty( systemPrefix + "showlogname", showLogName);

        if(showDateTime) {
            dateFormatter = new SimpleDateFormat(
                getStringProperty(systemPrefix + "dateformat",
                                  "yyyy/MM/dd HH:mm:ss:SSS zzz"));
        }
    }


    // ------------------------------------------------------------- Attributes

    /** The name of this simple log instance */
    protected String logName = null;
    /** The current log level */
    protected int currentLogLevel;

    private String prefix=null;

    
    // ------------------------------------------------------------ Constructor

    /**
     * Construct a simple log with given name.
     *
     * @param name log name
     */
    public SimpleLog(String name) {

        logName = name;

        // set initial log level
        // Used to be: set default log level to ERROR
        // IMHO it should be lower, but at least info ( costin ).
        setLevel(SimpleLog.LOG_LEVEL_INFO);

        // set log level from properties
        String lvl = getStringProperty(systemPrefix + "log." + logName);
        int i = String.valueOf(name).lastIndexOf(".");
        while(null == lvl && i > -1) {
            name = name.substring(0,i);
            lvl = getStringProperty(systemPrefix + "log." + name);
            i = String.valueOf(name).lastIndexOf(".");
        }

        if(null == lvl) {
            lvl =  getStringProperty(systemPrefix + "defaultlog");
        }

        if("all".equalsIgnoreCase(lvl)) {
            setLevel(SimpleLog.LOG_LEVEL_ALL);
        } else if("trace".equalsIgnoreCase(lvl)) {
            setLevel(SimpleLog.LOG_LEVEL_TRACE);
        } else if("debug".equalsIgnoreCase(lvl)) {
            setLevel(SimpleLog.LOG_LEVEL_DEBUG);
        } else if("info".equalsIgnoreCase(lvl)) {
            setLevel(SimpleLog.LOG_LEVEL_INFO);
        } else if("warn".equalsIgnoreCase(lvl)) {
            setLevel(SimpleLog.LOG_LEVEL_WARN);
        } else if("error".equalsIgnoreCase(lvl)) {
            setLevel(SimpleLog.LOG_LEVEL_ERROR);
        } else if("fatal".equalsIgnoreCase(lvl)) {
            setLevel(SimpleLog.LOG_LEVEL_FATAL);
        } else if("off".equalsIgnoreCase(lvl)) {
            setLevel(SimpleLog.LOG_LEVEL_OFF);
        }

    }


    // -------------------------------------------------------- Properties

    /**
     * <p> Set logging level. </p>
     *
     * @param currentLogLevel new logging level
     */
    public void setLevel(int currentLogLevel) {

        this.currentLogLevel = currentLogLevel;

    }


    /**
     * <p> Get logging level. </p>
     */
    public int getLevel() {

        return currentLogLevel;
    }


    // -------------------------------------------------------- Logging Methods


    /**
     * <p> Do the actual logging.
     * This method assembles the message
     * and then prints to <code>System.err</code>.</p>
     */
    protected void log(int type, Object message, Throwable t) {
        // use a string buffer for better performance
        StringBuffer buf = new StringBuffer();

        // append date-time if so configured
        if(showDateTime) {
            buf.append(dateFormatter.format(new Date()));
            buf.append(" ");
        }

        // append a readable representation of the log leve
        switch(type) {
            case SimpleLog.LOG_LEVEL_TRACE: buf.append("[TRACE] "); break;
            case SimpleLog.LOG_LEVEL_DEBUG: buf.append("[DEBUG] "); break;
            case SimpleLog.LOG_LEVEL_INFO:  buf.append("[INFO] ");  break;
            case SimpleLog.LOG_LEVEL_WARN:  buf.append("[WARN] ");  break;
            case SimpleLog.LOG_LEVEL_ERROR: buf.append("[ERROR] "); break;
            case SimpleLog.LOG_LEVEL_FATAL: buf.append("[FATAL] "); break;
        }

        // append the name of the log instance if so configured
 	if( showShortName) {
            if( prefix==null ) {
                // cut all but the last component of the name for both styles
                prefix = logName.substring( logName.lastIndexOf(".") +1) + " - ";
                prefix = prefix.substring( prefix.lastIndexOf("/") +1) + "-";
            }
            buf.append( prefix );
        } else if(showLogName) {
            buf.append(String.valueOf(logName)).append(" - ");
        }

        // append the message
        buf.append(String.valueOf(message));

        // append stack trace if not null
        if(t != null) {
            buf.append(" <");
            buf.append(t.toString());
            buf.append(">");

            java.io.StringWriter sw= new java.io.StringWriter(1024); 
            java.io.PrintWriter pw= new java.io.PrintWriter(sw); 
            t.printStackTrace(pw);
            pw.close();
            buf.append(sw.toString());
        }

        // print to System.err
        System.err.println(buf.toString());
    }


    /**
     * Is the given log level currently enabled?
     *
     * @param logLevel is this level enabled?
     */
    protected boolean isLevelEnabled(int logLevel) {
        // log level are numerically ordered so can use simple numeric
        // comparison
        return (logLevel >= currentLogLevel);
    }


    // -------------------------------------------------------- Log Implementation


    /**
     * <p> Log a message with debug log level.</p>
     */
    public final void debug(Object message) {

        if (isLevelEnabled(SimpleLog.LOG_LEVEL_DEBUG)) {
            log(SimpleLog.LOG_LEVEL_DEBUG, message, null);
        }
    }


    /**
     * <p> Log an error with debug log level.</p>
     */
    public final void debug(Object message, Throwable t) {

        if (isLevelEnabled(SimpleLog.LOG_LEVEL_DEBUG)) {
            log(SimpleLog.LOG_LEVEL_DEBUG, message, t);
        }
    }


    /**
     * <p> Log a message with debug log level.</p>
     */
    public final void trace(Object message) {

        if (isLevelEnabled(SimpleLog.LOG_LEVEL_TRACE)) {
            log(SimpleLog.LOG_LEVEL_TRACE, message, null);
        }
    }


    /**
     * <p> Log an error with debug log level.</p>
     */
    public final void trace(Object message, Throwable t) {

        if (isLevelEnabled(SimpleLog.LOG_LEVEL_TRACE)) {
            log(SimpleLog.LOG_LEVEL_TRACE, message, t);
        }
    }


    /**
     * <p> Log a message with info log level.</p>
     */
    public final void info(Object message) {

        if (isLevelEnabled(SimpleLog.LOG_LEVEL_INFO)) {
            log(SimpleLog.LOG_LEVEL_INFO,message,null);
        }
    }


    /**
     * <p> Log an error with info log level.</p>
     */
    public final void info(Object message, Throwable t) {

        if (isLevelEnabled(SimpleLog.LOG_LEVEL_INFO)) {
            log(SimpleLog.LOG_LEVEL_INFO, message, t);
        }
    }


    /**
     * <p> Log a message with warn log level.</p>
     */
    public final void warn(Object message) {

        if (isLevelEnabled(SimpleLog.LOG_LEVEL_WARN)) {
            log(SimpleLog.LOG_LEVEL_WARN, message, null);
        }
    }


    /**
     * <p> Log an error with warn log level.</p>
     */
    public final void warn(Object message, Throwable t) {

        if (isLevelEnabled(SimpleLog.LOG_LEVEL_WARN)) {
            log(SimpleLog.LOG_LEVEL_WARN, message, t);
        }
    }


    /**
     * <p> Log a message with error log level.</p>
     */
    public final void error(Object message) {

        if (isLevelEnabled(SimpleLog.LOG_LEVEL_ERROR)) {
            log(SimpleLog.LOG_LEVEL_ERROR, message, null);
        }
    }


    /**
     * <p> Log an error with error log level.</p>
     */
    public final void error(Object message, Throwable t) {

        if (isLevelEnabled(SimpleLog.LOG_LEVEL_ERROR)) {
            log(SimpleLog.LOG_LEVEL_ERROR, message, t);
        }
    }


    /**
     * <p> Log a message with fatal log level.</p>
     */
    public final void fatal(Object message) {

        if (isLevelEnabled(SimpleLog.LOG_LEVEL_FATAL)) {
            log(SimpleLog.LOG_LEVEL_FATAL, message, null);
        }
    }


    /**
     * <p> Log an error with fatal log level.</p>
     */
    public final void fatal(Object message, Throwable t) {

        if (isLevelEnabled(SimpleLog.LOG_LEVEL_FATAL)) {
            log(SimpleLog.LOG_LEVEL_FATAL, message, t);
        }
    }


    /**
     * <p> Are debug messages currently enabled? </p>
     *
     * <p> This allows expensive operations such as <code>String</code>
     * concatenation to be avoided when the message will be ignored by the
     * logger. </p>
     */
    public final boolean isDebugEnabled() {

        return isLevelEnabled(SimpleLog.LOG_LEVEL_DEBUG);
    }


    /**
     * <p> Are error messages currently enabled? </p>
     *
     * <p> This allows expensive operations such as <code>String</code>
     * concatenation to be avoided when the message will be ignored by the
     * logger. </p>
     */
    public final boolean isErrorEnabled() {

        return isLevelEnabled(SimpleLog.LOG_LEVEL_ERROR);
    }


    /**
     * <p> Are fatal messages currently enabled? </p>
     *
     * <p> This allows expensive operations such as <code>String</code>
     * concatenation to be avoided when the message will be ignored by the
     * logger. </p>
     */
    public final boolean isFatalEnabled() {

        return isLevelEnabled(SimpleLog.LOG_LEVEL_FATAL);
    }


    /**
     * <p> Are info messages currently enabled? </p>
     *
     * <p> This allows expensive operations such as <code>String</code>
     * concatenation to be avoided when the message will be ignored by the
     * logger. </p>
     */
    public final boolean isInfoEnabled() {

        return isLevelEnabled(SimpleLog.LOG_LEVEL_INFO);
    }


    /**
     * <p> Are trace messages currently enabled? </p>
     *
     * <p> This allows expensive operations such as <code>String</code>
     * concatenation to be avoided when the message will be ignored by the
     * logger. </p>
     */
    public final boolean isTraceEnabled() {

        return isLevelEnabled(SimpleLog.LOG_LEVEL_TRACE);
    }


    /**
     * <p> Are warn messages currently enabled? </p>
     *
     * <p> This allows expensive operations such as <code>String</code>
     * concatenation to be avoided when the message will be ignored by the
     * logger. </p>
     */
    public final boolean isWarnEnabled() {

        return isLevelEnabled(SimpleLog.LOG_LEVEL_WARN);
    }


    /**
     * Return the thread context class loader if available.
     * Otherwise return null.
     * 
     * The thread context class loader is available for JDK 1.2
     * or later, if certain security conditions are met.
     *
     * @exception LogConfigurationException if a suitable class loader
     * cannot be identified.
     */
    private static ClassLoader getContextClassLoader()
    {
        ClassLoader classLoader = null;

        if (classLoader == null) {
            try {
                // Are we running on a JDK 1.2 or later system?
                Method method = Thread.class.getMethod("getContextClassLoader", null);
    
                // Get the thread context class loader (if there is one)
                try {
                    classLoader = (ClassLoader)method.invoke(Thread.currentThread(), null);
                } catch (IllegalAccessException e) {
                    ;  // ignore
                } catch (InvocationTargetException e) {
                    /**
                     * InvocationTargetException is thrown by 'invoke' when
                     * the method being invoked (getContextClassLoader) throws
                     * an exception.
                     * 
                     * getContextClassLoader() throws SecurityException when
                     * the context class loader isn't an ancestor of the
                     * calling class's class loader, or if security
                     * permissions are restricted.
                     * 
                     * In the first case (not related), we want to ignore and
                     * keep going.  We cannot help but also ignore the second
                     * with the logic below, but other calls elsewhere (to
                     * obtain a class loader) will trigger this exception where
                     * we can make a distinction.
                     */
                    if (e.getTargetException() instanceof SecurityException) {
                        ;  // ignore
                    } else {
                        // Capture 'e.getTargetException()' exception for details
                        // alternate: log 'e.getTargetException()', and pass back 'e'.
                        throw new LogConfigurationException
                            ("Unexpected InvocationTargetException", e.getTargetException());
                    }
                }
            } catch (NoSuchMethodException e) {
                // Assume we are running on JDK 1.1
                ;  // ignore
            }
        }
    
        if (classLoader == null) {
            classLoader = SimpleLog.class.getClassLoader();
        }

        // Return the selected class loader
        return classLoader;
    }
    
    private static InputStream getResourceAsStream(final String name)
    {
        return (InputStream)AccessController.doPrivileged(
            new PrivilegedAction() {
                public Object run() {
                    ClassLoader threadCL = getContextClassLoader();

                    if (threadCL != null) {
                        return threadCL.getResourceAsStream(name);
                    } else {
                        return ClassLoader.getSystemResourceAsStream(name);
                    }
                }
            });
    }
}

