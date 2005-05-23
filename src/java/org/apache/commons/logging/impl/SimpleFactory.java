/*
 * Copyright 2001-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */ 

package org.apache.commons.logging.impl;


import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Hashtable;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.Factory;

/**
 * Concrete subclass of {@link Factory} specific to SimpleLogger.
 */
public final class SimpleFactory extends Factory {

    public SimpleFactory() {
        super();
        init();
    }

    // ------------------------------------------------------- Attributes

    // Previously returned instances, to avoid creation of proxies
    private Hashtable instances = new Hashtable();

    /** All system properties used by <code>SimpleLog</code> start with this */
    private static final String systemPrefix =
        "org.apache.commons.logging.SimpleLogger.";

    /** The default format to use when formating dates */
    private static final String DEFAULT_DATE_TIME_FORMAT =
        "yyyy/MM/dd HH:mm:ss:SSS zzz";

    /** Properties loaded from SimpleLogger.properties */
    private final Properties simpleLogProps = new Properties();

    /** Include the instance name in the log message? */
    boolean showLogName = false;

    /** Include the short name ( last component ) of the logger in the log
     *  message. Defaults to true - otherwise we'll be lost in a flood of
     *  messages without knowing who sends them.
     */
    boolean showShortName = true;

    /** Include the current time in the log message */
    boolean showDateTime = false;

    /** The date and time format to use in the log message */
    String dateTimeFormat = DEFAULT_DATE_TIME_FORMAT;

    /** Used to format times */
    DateFormat dateFormatter = null;

    
    private String getStringProperty(String name) {
        String prop = null;
    try {
        prop = System.getProperty(name);
    } catch (SecurityException e) {
        ; // Ignore
    }
        return (prop == null) ? simpleLogProps.getProperty(name) : prop;
    }

    private String getStringProperty(String name, String dephault) {
        String prop = getStringProperty(name);
        return (prop == null) ? dephault : prop;
    }

    private boolean getBooleanProperty(String name, boolean dephault) {
        String prop = getStringProperty(name);
        return (prop == null) ? dephault : "true".equalsIgnoreCase(prop);
    }

    private void init() {
        // Initialize class attributes.
        // Load properties file, if found.
        // Override with system properties.
        // Add props from the resource SimpleLogger.properties
        InputStream in = this.getClass().getClassLoader().getResourceAsStream(
                "SimpleLogger.properties");
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

        if(showDateTime) {
            dateTimeFormat = getStringProperty(systemPrefix + "dateTimeFormat",
                                               dateTimeFormat);
            try {
                dateFormatter = new SimpleDateFormat(dateTimeFormat);
            } catch(IllegalArgumentException e) {
                // If the format pattern is invalid - use the default format
                dateTimeFormat = DEFAULT_DATE_TIME_FORMAT;
                dateFormatter = new SimpleDateFormat(dateTimeFormat);
            }
        }
    }


    
    // --------------------------------------------------------- Public Methods

    /**
     * Given a category name, check the system properties to see whether
     * a specific value has been assigned for that level.
     */
    private int getLevel(String name) {
        int level = SimpleLogger.LOG_LEVEL_INFO;

        // Set log level from properties
        String lvl = getStringProperty(systemPrefix + "log." + name);
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
            level = SimpleLogger.LOG_LEVEL_ALL;
        } else if("trace".equalsIgnoreCase(lvl)) {
            level = SimpleLogger.LOG_LEVEL_TRACE;
        } else if("debug".equalsIgnoreCase(lvl)) {
            level = SimpleLogger.LOG_LEVEL_DEBUG;
        } else if("info".equalsIgnoreCase(lvl)) {
            level = SimpleLogger.LOG_LEVEL_INFO;
        } else if("warn".equalsIgnoreCase(lvl)) {
            level = SimpleLogger.LOG_LEVEL_WARN;
        } else if("error".equalsIgnoreCase(lvl)) {
            level = SimpleLogger.LOG_LEVEL_ERROR;
        } else if("fatal".equalsIgnoreCase(lvl)) {
            level = SimpleLogger.LOG_LEVEL_FATAL;
        } else if("off".equalsIgnoreCase(lvl)) {
            level = SimpleLogger.LOG_LEVEL_OFF;
        }
        
        return level;
    }
    
    /**
     * Return a logger associated with the specified category name.
     */
    public Log getLog(String name) {
        Log instance = (Log) instances.get(name);
        if (instance != null)
            return instance;

        int level = getLevel(name);

        instance = new SimpleLogger(this, name, level);
        instances.put(name, instance);
        return instance;
    }


    /**
     * Release any internal references to previously created {@link Log}
     * instances returned by this factory.  This is useful in environments
     * like servlet containers, which implement application reloading by
     * throwing away a ClassLoader.  Dangling references to objects in that
     * class loader would prevent garbage collection.
     */
    public void release(ClassLoader cl) {
        instances.clear();
    }
    
    public void releaseAll() {
        instances.clear();
    }
}