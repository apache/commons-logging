/*
 * $Header: /home/jerenkrantz/tmp/commons/commons-convert/cvs/home/cvs/jakarta-commons//logging/src/java/org/apache/commons/logging/Attic/SimpleLog.java,v 1.8 2002/01/17 01:47:49 craigmcc Exp $
 * $Revision: 1.8 $
 * $Date: 2002/01/17 01:47:49 $
 *
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1999-2002 The Apache Software Foundation.  All rights
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
 *     If not specified, defaults to "error". </li>
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
 * @author Robert Burrell Donkin
 *
 * @version $Id: SimpleLog.java,v 1.8 2002/01/17 01:47:49 craigmcc Exp $
 */
public class SimpleLog extends AbstractLog {


    // ------------------------------------------------------- Class Attributes
    
    /** All system properties used by <code>Simple</code> start with this */
    static protected final String _prefix =
        "org.apache.commons.logging.simplelog.";
    
    /** All system properties which start with {@link #_prefix} */
    static protected final Properties _simplelogProps = new Properties();
    /** Include the instance name in the log message? */
    static protected boolean _showlogname = false;
    /** Include the current time in the log message */
    static protected boolean _showtime = false;
    /** Used to format times */
    static protected DateFormat _df = null;



    // ------------------------------------------------------------ Initializer

    // initialize class attributes
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
        
        _showlogname = "true".equalsIgnoreCase(
                _simplelogProps.getProperty(
                    _prefix + "showlogname","true"));
                    
        _showtime = "true".equalsIgnoreCase(
                _simplelogProps.getProperty(
                    _prefix + "showdate","true"));
                    
        if(_showtime) {
            _df = new SimpleDateFormat(
                _simplelogProps.getProperty(
                    _prefix + "dateformat","yyyy/MM/dd HH:mm:ss:SSS zzz"));
        }
    }


    // ------------------------------------------------------------- Attributes

    /** The name of this simple log instance */
    protected String _name = null;


    // ------------------------------------------------------------ Constructor
    
    /** 
     * Construct a simple log with given name.
     *
     * @param name log name
     */
    public SimpleLog(String name) {
    
        _name = name;

        // set initial log level
        // set default log level to ERROR
        setLevel(Log.ERROR);
        
        // set log level from properties
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
            setLevel(Log.DEBUG);
        } else if("info".equalsIgnoreCase(lvl)) {
            setLevel(Log.INFO);
        } else if("warn".equalsIgnoreCase(lvl)) {
            setLevel(Log.WARN);
        } else if("error".equalsIgnoreCase(lvl)) {
            setLevel(Log.ERROR);
        } else if("fatal".equalsIgnoreCase(lvl)) {
            setLevel(Log.FATAL);
        }
    }


    // -------------------------------------------------------- Logging Methods
    

    /**
     * <p> Do the actual logging.
     * This method assembles the message 
     * and then prints to <code>System.out</code>.</p>
     */
    protected void log(int type, Object message, Throwable t) {
        // use a string buffer for better performance 
        StringBuffer buf = new StringBuffer();
        
        // append date-time if so configured
        if(_showtime) {
            buf.append(_df.format(new Date()));
            buf.append(" ");
        }
        
        // append a readable representation of the log leve
        switch(type) {
            case DEBUG: buf.append("[DEBUG] "); break;
            case INFO:  buf.append("[INFO] ");  break;
            case WARN:  buf.append("[WARN] ");  break;
            case ERROR: buf.append("[ERROR] "); break;
            case FATAL: buf.append("[FATAL] "); break;
        }
        
        // append the name of the log instance if so configured
        if(_showlogname) {
            buf.append(String.valueOf(_name)).append(" - ");
        }
        
        // append the message
        buf.append(String.valueOf(message));
        
        // append stack trace if not null
        if(t != null) {
            buf.append(" <");
            buf.append(t.toString());
            buf.append(">");
            t.printStackTrace();
        }
        
        // print to System.out
        System.out.println(buf.toString());
    }


    // ----------------------------------------------------- Log Implementation

    /**
     * Prepare then call {@link #log}.
     */
    protected final void debugImpl(Object message) {
        log(Log.DEBUG,message,null);
    }

    /**
     * Prepare then call {@link #log}.
     */
    protected final void debugImpl(Object message, Throwable t) {
        log(Log.DEBUG,message,t);
    }

    /**
     * Prepare then call {@link #log}.
     */
    protected final void infoImpl(Object message) {
        log(Log.INFO,message,null);
    }

    /**
     * Prepare then call {@link #log}.
     */
    protected final void infoImpl(Object message, Throwable t) {
        log(Log.INFO,message,t);
    }

    /**
     * Prepare then call {@link #log}.
     */
    protected final void warnImpl(Object message) {
        log(Log.WARN,message,null);
    }
    
    /**
     * Prepare then call {@link #log}.
     */
    protected final void warnImpl(Object message, Throwable t) {
        log(Log.WARN,message,t);
    }

    /**
     * Prepare then call {@link #log}.
     */
    protected final void errorImpl(Object message) {
        log(Log.ERROR,message,null);
    }

    /**
     * Prepare then call {@link #log}.
     */
    protected final void errorImpl(Object message, Throwable t) {
        log(Log.ERROR,message,t);
    }

    /**
     * Prepare then call {@link #log}.
     */
    protected final void fatalImpl(Object message) {
        log(Log.FATAL,message,null);
    }

    /**
     * Prepare then call {@link #log}.
     */
    protected final void fatalImpl(Object message, Throwable t) {
        log(Log.FATAL,message,t);
    }
}
