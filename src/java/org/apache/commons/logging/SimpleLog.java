/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software License
 * version 1.1, a copy of which has been included with this distribution in
 * the LICENSE file.
 */

package org.apache.commons.logging;

import java.util.Properties;
import java.util.Enumeration;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.text.DateFormat;
import java.util.Date;

/**
 * <p>Simple implementation of Log that sends all enabled log messages,
 * for all defined loggers, to System.out.  The following system properties
 * are supported to configure the behavior of this logger:</p>
 * <ul>
 * <li><code>org.apache.commons.logging.simplelog.defaultlog</code> -
 *     Default logging detail level for all instances of SimpleLog.
 *     Must be one of ("debug", "info", "warn", "error", or "fatal").
 *     If not specified, defaults to "error".</li>
 * <li><code>org.apache.commons.logging.simplelog.log.xxxxx</code> -
 *     Logging detail level for a SimpleLog instance named "xxxxx".
 *     Must be one of ("debug", "info", "warn", "error", or "fatal").
 *     If not specified, the default logging detail level is used.</li>
 * <li><code>org.apache.commons.logging.simplelog.showlogname</code> -
 *     Set to <code>true</code> if you want the Log instance name to be
 *     included in output messages.</li>
 * <li><code>org.apache.commons.logging.simplelog.showtime</code> -
 *     Set to <code>true</code> if you want the current date and time
 *     to be included in output messages.</li>
 * </ul>
 *
 * <p>In addition to looking for system properties with the names specified
 * above, this implementation also checks for a class loader resource named
 * <code>"simplelog.properties"</code>, and includes any matching definitions
 * from this resource (if it exists).</p>
 *
 * @author Rod Waldhoff
 * @version $Id: SimpleLog.java,v 1.6 2001/12/04 04:28:03 craigmcc Exp $
 */
public class SimpleLog implements Log {
    static protected final String _prefix =
        "org.apache.commons.logging.simplelog.";
    static protected final Properties _simplelogProps = new Properties();
    static protected boolean _showlogname = false;
    static protected boolean _showtime = false;
    static protected DateFormat _df = null;

    static {
        // add all system props that start with the specified prefix
        Enumeration enum = System.getProperties().propertyNames();
        while(enum.hasMoreElements()) {
            String name = (String)(enum.nextElement());
            if(null != name && name.startsWith(_prefix)) {
                _simplelogProps.setProperty(name,System.getProperty(name));
            }
        }

        // add props from the resource simplelog.properties
        InputStream in =
            ClassLoader.getSystemResourceAsStream("simplelog.properties");
        if(null != in) {
            try {
                _simplelogProps.load(in);
                in.close();
            } catch(java.io.IOException e) {
                // ignored
            }
        }
        try {
        } catch(Throwable t) {
            // ignored
        }
        _showlogname = "true".equalsIgnoreCase(_simplelogProps.getProperty(_prefix + "showlogname","true"));
        _showtime = "true".equalsIgnoreCase(_simplelogProps.getProperty(_prefix + "showdate","true"));
        if(_showtime) {
            _df = new SimpleDateFormat(_simplelogProps.getProperty(_prefix + "dateformat","yyyy/MM/dd HH:mm:ss:SSS zzz"));
        }
    }

    protected static final int DEBUG  = 5;
    protected static final int INFO   = 4;
    protected static final int WARN   = 3;
    protected static final int ERROR  = 2;
    protected static final int FATAL  = 1;
    protected int _logLevel = 2;

    protected String _name = null;

    public SimpleLog(String name) {
        _name = name;

        String lvl = _simplelogProps.getProperty(_prefix + "log." + _name);
        int i = String.valueOf(name).lastIndexOf(".");
        while(null == lvl && i > -1) {
            name = name.substring(0,i);
            lvl = _simplelogProps.getProperty(_prefix + "log." + name);
            i = String.valueOf(name).lastIndexOf(".");
        }
        if(null == lvl) {
            lvl =  _simplelogProps.getProperty(_prefix + "defaultlog");
        }

        if("debug".equalsIgnoreCase(lvl)) {
            _logLevel = DEBUG;
        } else if("info".equalsIgnoreCase(lvl)) {
            _logLevel = INFO;
        } else if("warn".equalsIgnoreCase(lvl)) {
            _logLevel = WARN;
        } else if("error".equalsIgnoreCase(lvl)) {
            _logLevel = ERROR;
        } else if("fatal".equalsIgnoreCase(lvl)) {
            _logLevel = FATAL;
        }
    }

    protected void log(int type, Object message, Throwable t) {
        if(_logLevel >= type) {
            StringBuffer buf = new StringBuffer();
            if(_showtime) {
                buf.append(_df.format(new Date()));
                buf.append(" ");
            }
            switch(type) {
                case DEBUG: buf.append("[DEBUG] "); break;
                case INFO:  buf.append("[INFO] ");  break;
                case WARN:  buf.append("[WARN] ");  break;
                case ERROR: buf.append("[ERROR] "); break;
                case FATAL: buf.append("[FATAL] "); break;
            }
            if(_showlogname) {
                buf.append(String.valueOf(_name)).append(" - ");
            }
            buf.append(String.valueOf(message));
            if(t != null) {
                buf.append(" <");
                buf.append(t.toString());
                buf.append(">");
                t.printStackTrace();
            }
            System.out.println(buf.toString());
        }
    }

    public final void debug(Object message) {
        log(DEBUG,message,null);
    }

    public final void debug(Object message, Throwable t) {
        log(DEBUG,message,t);
    }

    public final void info(Object message) {
        log(INFO,message,null);
    }

    public final void info(Object message, Throwable t) {
        log(INFO,message,t);
    }

    public final void warn(Object message) {
        log(WARN,message,null);
    }
    public final void warn(Object message, Throwable t) {
        log(WARN,message,t);
    }

    public final void error(Object message) {
        log(ERROR,message,null);
    }

    public final void error(Object message, Throwable t) {
        log(ERROR,message,t);
    }

    public final void fatal(Object message) {
        log(FATAL,message,null);
    }

    public final void fatal(Object message, Throwable t) {
        log(FATAL,message,t);
    }

    public final boolean isDebugEnabled() {
        return (_logLevel >= DEBUG);
    }

    public final boolean isInfoEnabled() {
        return (_logLevel >= INFO);
    }

    public final void setLevel(int level) {
        _logLevel = level;
    }

    public final int getLevel() {
        return _logLevel;
    }

}
