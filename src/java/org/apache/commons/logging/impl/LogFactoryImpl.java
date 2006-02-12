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


import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogConfigurationException;
import org.apache.commons.logging.LogFactory;


/**
 * <p>Concrete subclass of {@link LogFactory} that implements the
 * following algorithm to dynamically select a logging implementation
 * class to instantiate a wrapper for.</p>
 * <ul>
 * <li>Use a factory configuration attribute named
 *     <code>org.apache.commons.logging.Log</code> to identify the
 *     requested implementation class.</li>
 * <li>Use the <code>org.apache.commons.logging.Log</code> system property
 *     to identify the requested implementation class.</li>
 * <li>If <em>Log4J</em> is available, return an instance of
 *     <code>org.apache.commons.logging.impl.Log4JLogger</code>.</li>
 * <li>If <em>JDK 1.4 or later</em> is available, return an instance of
 *     <code>org.apache.commons.logging.impl.Jdk14Logger</code>.</li>
 * <li>Otherwise, return an instance of
 *     <code>org.apache.commons.logging.impl.SimpleLog</code>.</li>
 * </ul>
 *
 * <p>If the selected {@link Log} implementation class has a
 * <code>setLogFactory()</code> method that accepts a {@link LogFactory}
 * parameter, this method will be called on each newly created instance
 * to identify the associated factory.  This makes factory configuration
 * attributes available to the Log instance, if it so desires.</p>
 *
 * <p>This factory will remember previously created <code>Log</code> instances
 * for the same name, and will return them on repeated requests to the
 * <code>getInstance()</code> method.</p>
 *
 * @author Rod Waldhoff
 * @author Craig R. McClanahan
 * @author Richard A. Sitze
 * @author Brian Stansberry
 * @version $Revision$ $Date$
 */

public class LogFactoryImpl extends LogFactory {


    /** Log4JLogger class name */
    private static final String LOGGING_IMPL_LOG4J_LOGGER = "org.apache.commons.logging.impl.Log4JLogger";
    /** Jdk14Logger class name */
    private static final String LOGGING_IMPL_JDK14_LOGGER = "org.apache.commons.logging.impl.Jdk14Logger";
    /** Jdk13LumberjackLogger class name */
    private static final String LOGGING_IMPL_LUMBERJACK_LOGGER = "org.apache.commons.logging.impl.Jdk13LumberjackLogger";
    /** SimpleLog class name */
    private static final String LOGGING_IMPL_SIMPLE_LOGGER = "org.apache.commons.logging.impl.SimpleLog";

    
    // ----------------------------------------------------------- Constructors

   

    /**
     * Public no-arguments constructor required by the lookup mechanism.
     */
    public LogFactoryImpl() {
        super();
        initDiagnostics();  // method on this object
        if (isDiagnosticsEnabled()) {
            logDiagnostic("Instance created.");
        }
    }


    // ----------------------------------------------------- Manifest Constants


    /**
     * The name (<code>org.apache.commons.logging.Log</code>) of the system 
     * property identifying our {@link Log} implementation class.
     */
    public static final String LOG_PROPERTY =
        "org.apache.commons.logging.Log";


    /**
     * The deprecated system property used for backwards compatibility with
     * old versions of JCL.
     */
    protected static final String LOG_PROPERTY_OLD =
        "org.apache.commons.logging.log";

    /**
     * The name (<code>org.apache.commons.logging.Log.allowFlawedContext</code>) 
     * of the system property which can be set true/false to
     * determine system behaviour when a bad context-classloader is encountered.
     * When set to false
     * 
     * Default behaviour: true (tolerates bad context classloaders)
     * 
     * See also method setAttribute.
     */
    public static final String ALLOW_FLAWED_CONTEXT_PROPERTY = 
        "org.apache.commons.logging.Log.allowFlawedContext";

    /**
     * The name (<code>org.apache.commons.logging.Log.allowFlawedDiscovery</code>) 
     * of the system property which can be set true/false to
     * determine system behaviour when a bad logging adapter class is
     * encountered during logging discovery. When set to false, an
     * exception will be thrown and the app will fail to start. When set
     * to true, discovery will continue (though the user might end up
     * with a different logging implementation than they expected).
     * 
     * Default behaviour: true (tolerates bad logging adapters)
     * 
     * See also method setAttribute.
     */
    public static final String ALLOW_FLAWED_DISCOVERY_PROPERTY = 
        "org.apache.commons.logging.Log.allowFlawedDiscovery";

    /**
     * The name (<code>org.apache.commons.logging.Log.allowFlawedHierarchy</code>) 
     * of the system property which can be set true/false to
     * determine system behaviour when a logging adapter class is
     * encountered which has bound to the wrong Log class implementation.
     * When set to false, an exception will be thrown and the app will fail
     * to start. When set to true, discovery will continue (though the user
     * might end up with a different logging implementation than they expected).
     * 
     * Default behaviour: true (tolerates bad Log class hierarchy)
     * 
     * See also method setAttribute.
     */
    public static final String ALLOW_FLAWED_HIERARCHY_PROPERTY = 
        "org.apache.commons.logging.Log.allowFlawedHierarchy";


    /**
     * The names of classes that will be tried (in order) as logging
     * adapters. Each class is expected to implement the Log interface,
     * and to throw NoClassDefFound or ExceptionInInitializerError when
     * loaded if the underlying logging library is not available. Any 
     * other error indicates that the underlying logging library is available
     * but broken/unusable for some reason.
     */
    private static final String[] classesToDiscover = {
            LOGGING_IMPL_LOG4J_LOGGER,
            "org.apache.commons.logging.impl.Jdk14Logger",
            "org.apache.commons.logging.impl.Jdk13LumberjackLogger",
            "org.apache.commons.logging.impl.SimpleLog"
    };
    

    // ----------------------------------------------------- Instance Variables

    /**
     * Determines whether logging classes should be loaded using the thread-context
     * classloader, or via the classloader that loaded this LogFactoryImpl class.
     */
    private boolean useTCCL = true;

    /**
     * The string prefixed to every message output by the logDiagnostic method.
     */
    private String diagnosticPrefix;


    /**
     * Configuration attributes.
     */
    protected Hashtable attributes = new Hashtable();


    /**
     * The {@link org.apache.commons.logging.Log} instances that have
     * already been created, keyed by logger name.
     */
    protected Hashtable instances = new Hashtable();


    /**
     * Name of the class implementing the Log interface.
     */
    private String logClassName;


    /**
     * The one-argument constructor of the
     * {@link org.apache.commons.logging.Log}
     * implementation class that will be used to create new instances.
     * This value is initialized by <code>getLogConstructor()</code>,
     * and then returned repeatedly.
     */
    protected Constructor logConstructor = null;


    /**
     * The signature of the Constructor to be used.
     */
    protected Class logConstructorSignature[] =
    { java.lang.String.class };


    /**
     * The one-argument <code>setLogFactory</code> method of the selected
     * {@link org.apache.commons.logging.Log} method, if it exists.
     */
    protected Method logMethod = null;


    /**
     * The signature of the <code>setLogFactory</code> method to be used.
     */
    protected Class logMethodSignature[] =
    { LogFactory.class };

    /**
     * See getBaseClassLoader and initConfiguration.
     */
    private boolean allowFlawedContext;
    
    /**
     * See handleFlawedDiscovery and initConfiguration.
     */
    private boolean allowFlawedDiscovery;
    
    /**
     * See handleFlawedHierarchy and initConfiguration.
     */
    private boolean allowFlawedHierarchy;
    
    // --------------------------------------------------------- Public Methods


    /**
     * Return the configuration attribute with the specified name (if any),
     * or <code>null</code> if there is no such attribute.
     *
     * @param name Name of the attribute to return
     */
    public Object getAttribute(String name) {

        return (attributes.get(name));

    }


    /**
     * Return an array containing the names of all currently defined
     * configuration attributes.  If there are no such attributes, a zero
     * length array is returned.
     */
    public String[] getAttributeNames() {

        Vector names = new Vector();
        Enumeration keys = attributes.keys();
        while (keys.hasMoreElements()) {
            names.addElement((String) keys.nextElement());
        }
        String results[] = new String[names.size()];
        for (int i = 0; i < results.length; i++) {
            results[i] = (String) names.elementAt(i);
        }
        return (results);

    }


    /**
     * Convenience method to derive a name from the specified class and
     * call <code>getInstance(String)</code> with it.
     *
     * @param clazz Class for which a suitable Log name will be derived
     *
     * @exception LogConfigurationException if a suitable <code>Log</code>
     *  instance cannot be returned
     */
    public Log getInstance(Class clazz) throws LogConfigurationException {

        return (getInstance(clazz.getName()));

    }


    /**
     * <p>Construct (if necessary) and return a <code>Log</code> instance,
     * using the factory's current set of configuration attributes.</p>
     *
     * <p><strong>NOTE</strong> - Depending upon the implementation of
     * the <code>LogFactory</code> you are using, the <code>Log</code>
     * instance you are returned may or may not be local to the current
     * application, and may or may not be returned again on a subsequent
     * call with the same name argument.</p>
     *
     * @param name Logical name of the <code>Log</code> instance to be
     *  returned (the meaning of this name is only known to the underlying
     *  logging implementation that is being wrapped)
     *
     * @exception LogConfigurationException if a suitable <code>Log</code>
     *  instance cannot be returned
     */
    public Log getInstance(String name) throws LogConfigurationException {

        Log instance = (Log) instances.get(name);
        if (instance == null) {
            instance = newInstance(name);
            instances.put(name, instance);
        }
        return (instance);

    }


    /**
     * Release any internal references to previously created
     * {@link org.apache.commons.logging.Log}
     * instances returned by this factory.  This is useful in environments
     * like servlet containers, which implement application reloading by
     * throwing away a ClassLoader.  Dangling references to objects in that
     * class loader would prevent garbage collection.
     */
    public void release() {

        logDiagnostic("Releasing all known loggers");
        instances.clear();
    }


    /**
     * Remove any configuration attribute associated with the specified name.
     * If there is no such attribute, no action is taken.
     *
     * @param name Name of the attribute to remove
     */
    public void removeAttribute(String name) {

        attributes.remove(name);

    }


    /**
     * Set the configuration attribute with the specified name.  Calling
     * this with a <code>null</code> value is equivalent to calling
     * <code>removeAttribute(name)</code>.
     * <p>
     * This method can be used to set logging configuration programmatically
     * rather than via system properties. It can also be used in code running
     * within a container (such as a webapp) to configure behaviour on a
     * per-component level instead of globally as system properties would do.
     * To use this method instead of a system property, call
     * <pre>
     * LogFactory.getFactory().setAttribute(...)
     * </pre>
     * This must be done before the first Log object is created; configuration
     * changes after that point will be ignored.
     * <p>
     * This method is also called automatically if LogFactory detects a
     * commons-logging.properties file; every entry in that file is set
     * automatically as an attribute here.
     *
     * @param name Name of the attribute to set
     * @param value Value of the attribute to set, or <code>null</code>
     *  to remove any setting for this attribute
     */
    public void setAttribute(String name, Object value) {

        if (logConstructor != null) {
            logDiagnostic("setAttribute: call too late; configuration already performed.");
        }

        if (value == null) {
            attributes.remove(name);
        } else {
            attributes.put(name, value);
        }
        
        if (name.equals(TCCL_KEY)) {
            useTCCL = Boolean.valueOf(value.toString()).booleanValue();
        }

    }


    // ------------------------------------------------------ 
    // Static Methods
    //
    // These methods only defined as workarounds for a java 1.2 bug;
    // theoretically none of these are needed.
    // ------------------------------------------------------ 
    
    /**
     * Gets the context classloader.
     * This method is a workaround for a java 1.2 compiler bug.
     * @since 1.1
     */
    protected static ClassLoader getContextClassLoader() throws LogConfigurationException {
        return LogFactory.getContextClassLoader();
    }

    
    /**
     * Workaround for bug in Java1.2; in theory this method is not needed.
     * See LogFactory.isDiagnosticsEnabled.
     */
    protected static boolean isDiagnosticsEnabled() {
        return LogFactory.isDiagnosticsEnabled();
    }

    
    /**
     * Workaround for bug in Java1.2; in theory this method is not needed.
     * See LogFactory.getClassLoader.
     * @since 1.1
     */
    protected static ClassLoader getClassLoader(Class clazz) {
        return LogFactory.getClassLoader(clazz);
    }


    // ------------------------------------------------------ Protected Methods

    /**
     * Calculate and cache a string that uniquely identifies this instance,
     * including which classloader the object was loaded from.
     * <p>
     * This string will later be prefixed to each "internal logging" message
     * emitted, so that users can clearly see any unexpected behaviour.
     * <p>
     * Note that this method does not detect whether internal logging is 
     * enabled or not, nor where to output stuff if it is; that is all
     * handled by the parent LogFactory class. This method just computes
     * its own unique prefix for log messages.
     */
    private void initDiagnostics() {
        // It would be nice to include an identifier of the context classloader
        // that this LogFactoryImpl object is responsible for. However that
        // isn't possible as that information isn't available. It is possible
        // to figure this out by looking at the logging from LogFactory to
        // see the context & impl ids from when this object was instantiated,
        // in order to link the impl id output as this object's prefix back to
        // the context it is intended to manage.
        // Note that this prefix should be kept consistent with that 
        // in LogFactory.
        Class clazz = this.getClass();
        ClassLoader classLoader = getClassLoader(clazz);
        String classLoaderName;
        try {
            if (classLoader == null) {
                classLoaderName = "BOOTLOADER";
            } else {
                classLoaderName = objectId(classLoader);
            }
        } catch(SecurityException e) {
            classLoaderName = "UNKNOWN";
        }
        diagnosticPrefix = "[LogFactoryImpl@" + System.identityHashCode(this) + " -> " + classLoaderName + "] ";
    }

    
    /**
     * Output a diagnostic message to a user-specified destination (if the
     * user has enabled diagnostic logging).
     * 
     * @param msg diagnostic message
     * @since 1.1
     */
    protected void logDiagnostic(String msg) {
        if (isDiagnosticsEnabled()) {
            logRawDiagnostic(diagnosticPrefix + msg);
        }
    }

    /**
     * Return the fully qualified Java classname of the {@link Log}
     * implementation we will be using.  
     * 
     * @deprecated  Never invoked by this class; subclasses should not assume
     *              it will be.
     */
    protected String getLogClassName() {

        if (logClassName == null) {
            discoverLogImplementation(getClass().getName());
        }
        
        return logClassName;
    }


    /**
     * <p>Return the <code>Constructor</code> that can be called to instantiate
     * new {@link org.apache.commons.logging.Log} instances.</p>
     *
     * <p><strong>IMPLEMENTATION NOTE</strong> - Race conditions caused by
     * calling this method from more than one thread are ignored, because
     * the same <code>Constructor</code> instance will ultimately be derived
     * in all circumstances.</p>
     *
     * @exception LogConfigurationException if a suitable constructor
     *  cannot be returned   
     * 
     * @deprecated  Never invoked by this class; subclasses should not assume
     *              it will be.
     */
    protected Constructor getLogConstructor()
        throws LogConfigurationException {

        // Return the previously identified Constructor (if any)
        if (logConstructor == null) {
            discoverLogImplementation(getClass().getName());
        }

        return logConstructor;
    }
    

    /**
     * Is <em>JDK 1.3 with Lumberjack</em> logging available?   
     * 
     * @deprecated  Never invoked by this class; subclasses should not assume
     *              it will be.
     */
    protected boolean isJdk13LumberjackAvailable() {
        return isLogLibraryAvailable(
                "Jdk13Lumberjack",
                "org.apache.commons.logging.impl.Jdk13LumberjackLogger");
    }


    /**
     * <p>Return <code>true</code> if <em>JDK 1.4 or later</em> logging
     * is available.  Also checks that the <code>Throwable</code> class
     * supports <code>getStackTrace()</code>, which is required by
     * Jdk14Logger.</p>  
     * 
     * @deprecated  Never invoked by this class; subclasses should not assume
     *              it will be.
     */
    protected boolean isJdk14Available() {
        return isLogLibraryAvailable(
                "Jdk14",
                "org.apache.commons.logging.impl.Jdk14Logger");
    }


    /**
     * Is a <em>Log4J</em> implementation available? 
     * 
     * @deprecated  Never invoked by this class; subclasses should not assume
     *              it will be.
     */
    protected boolean isLog4JAvailable() {
        return isLogLibraryAvailable(
                "Log4J",
                LOGGING_IMPL_LOG4J_LOGGER);
    }


    /**
     * Create and return a new {@link org.apache.commons.logging.Log}
     * instance for the specified name.
     *
     * @param name Name of the new logger
     *
     * @exception LogConfigurationException if a new instance cannot
     *  be created
     */
    protected Log newInstance(String name) throws LogConfigurationException {

        Log instance = null;
        try {
            if (logConstructor == null) {
                instance = discoverLogImplementation(name);
            }
            else {
                Object params[] = { name };
                instance = (Log) logConstructor.newInstance(params);
            }
            
            if (logMethod != null) {
                Object params[] = { this };
                logMethod.invoke(instance, params);
            }
            
            return (instance);
            
        } catch (LogConfigurationException lce) {
            
            // this type of exception means there was a problem in discovery
            // and we've already output diagnostics about the issue, etc.; 
            // just pass it on
            throw (LogConfigurationException) lce;
            
        } catch (InvocationTargetException e) {
            // A problem occurred invoking the Constructor or Method 
            // previously discovered
            Throwable c = e.getTargetException();
            if (c != null) {
                throw new LogConfigurationException(c);
            } else {
                throw new LogConfigurationException(e);
            }
        } catch (Throwable t) {
            // A problem occurred invoking the Constructor or Method 
            // previously discovered
            throw new LogConfigurationException(t);
        }
    }
    

    //  ------------------------------------------------------ Private Methods
    
    /**
     * Utility method to check whether a particular logging library is
     * present and available for use. Note that this does <i>not</i>
     * affect the future behaviour of this class.
     */
    private boolean isLogLibraryAvailable(String name, String classname) {
        if (isDiagnosticsEnabled()) {
            logDiagnostic("Checking for " + name + ".");
        }
        try {
            Log log = createLogFromClass(
                        classname, 
                        this.getClass().getName(), // dummy category
                        false);

            if (log == null) {
                if (isDiagnosticsEnabled()) {
                    logDiagnostic("Did not find " + name + ".");
                }
                return false;
            } else {
                if (isDiagnosticsEnabled()) {
                    logDiagnostic("Found " + name + ".");
                }
                return true;
            }
        } catch(LogConfigurationException e) {
            if (isDiagnosticsEnabled()) {
                logDiagnostic("Logging system " + name + " is available but not useable.");
            }
            return false;
        }
    }

    /**
     * Attempt to find an attribute (see method setAttribute) or a 
     * system property with the provided name and return its value.
     * <p>
     * The attributes associated with this object are checked before
     * system properties in case someone has explicitly called setAttribute,
     * or a configuration property has been set in a commons-logging.properties
     * file.
     * 
     * @return the value associated with the property, or null.
     */
    private String getConfigurationValue(String property) {
        if (isDiagnosticsEnabled()) {
            logDiagnostic("[ENV] Trying to get configuration for item " + property);
        }
        if (isDiagnosticsEnabled()) {
            logDiagnostic("[ENV] Looking for attribute " + property);
        }
        Object valueObj =  getAttribute(property);
        if (valueObj != null) {
            if (isDiagnosticsEnabled()) {
                logDiagnostic("[ENV] Found value [" + valueObj + "] for " + property);
            }
            return valueObj.toString();
        }
        
        if (isDiagnosticsEnabled()) {
            logDiagnostic("[ENV] Looking for system property " + property);
        }
        try {
            String value = System.getProperty(property);
            if (isDiagnosticsEnabled()) {
                logDiagnostic("[ENV] Found value [" + value + "] for " + property);
            }
            return value;
        } catch (SecurityException e) {
            if (isDiagnosticsEnabled()) {
                logDiagnostic("[ENV] Security prevented reading system property.");
            }
        }
        
        return null;
    }
    
    /**
     * Get the setting for the user-configurable behaviour specified by key.
     * If nothing has explicitly been set, then return dflt.  
     */
    private boolean getBooleanConfiguration(String key, boolean dflt) {
        String val = getConfigurationValue(key);
        if (val == null)
            return dflt;
        return Boolean.valueOf(val).booleanValue();
    }

    /**
     * Initialize a number of variables that control the behaviour of this
     * class and that can be tweaked by the user. This is done when the first
     * logger is created, not in the constructor of this class, because we
     * need to give the user a chance to call method setAttribute in order to
     * configure this object.
     */
    private void initConfiguration() {
        allowFlawedContext = getBooleanConfiguration(ALLOW_FLAWED_CONTEXT_PROPERTY, true);
        allowFlawedDiscovery = getBooleanConfiguration(ALLOW_FLAWED_DISCOVERY_PROPERTY, true);
        allowFlawedHierarchy = getBooleanConfiguration(ALLOW_FLAWED_HIERARCHY_PROPERTY, true);
    }
  

    /**
     * Attempts to create a Log instance for the given category name.
     * Follows the discovery process described in the class javadoc.
     * 
     * @param logCategory the name of the log category
     * 
     * @throws LogConfigurationException if an error in discovery occurs, 
     * or if no adapter at all can be instantiated
     */
    private Log discoverLogImplementation(String logCategory)
    throws LogConfigurationException
    {
        if (isDiagnosticsEnabled()) {
            logDiagnostic("Attempting to discover a Log implementation...");
        }
        
        initConfiguration();
        
        Log result = null;
        
        // See if the user specified the Log implementation to use
        String specifiedLogClassName = findUserSpecifiedLogClassName();

        if (specifiedLogClassName != null) {
            result = createLogFromClass(specifiedLogClassName,
                                        logCategory,
                                        true);
            if (result == null) {
                StringBuffer messageBuffer =  new StringBuffer("User-specified log class '");
                messageBuffer.append(specifiedLogClassName);
                messageBuffer.append("' cannot be found or is not useable.");
                
                //
                // Mistyping or misspelling names is a common fault.
                // Construct a good error message, if we can
                if (specifiedLogClassName != null) {
                    final String trimmedName = specifiedLogClassName.trim();
                    informUponSimilarName(messageBuffer, trimmedName, LOGGING_IMPL_LOG4J_LOGGER);
                    informUponSimilarName(messageBuffer, trimmedName, LOGGING_IMPL_JDK14_LOGGER);
                    informUponSimilarName(messageBuffer, trimmedName, LOGGING_IMPL_LUMBERJACK_LOGGER);
                    informUponSimilarName(messageBuffer, trimmedName, LOGGING_IMPL_SIMPLE_LOGGER);
                }
                throw new LogConfigurationException(messageBuffer.toString());
            }
            
            return result;
        }
        
        // No user specified log; try to discover what's on the classpath
        //
        // Note that we deliberately loop here over classesToDiscover and
        // expect method createLogFromClass to loop over the possible source
        // classloaders. The effect is:
        //   for each discoverable log adapter
        //      for each possible classloader
        //          see if it works
        //
        // It appears reasonable at first glance to do the opposite: 
        //   for each possible classloader
        //     for each discoverable log adapter
        //        see if it works
        //
        // The latter certainly has advantages for user-installable logging
        // libraries such as log4j; in a webapp for example this code should
        // first check whether the user has provided any of the possible
        // logging libraries before looking in the parent classloader. 
        // Unfortunately, however, Jdk14Logger will always work in jvm>=1.4,
        // and SimpleLog will always work in any JVM. So the loop would never
        // ever look for logging libraries in the parent classpath. Yet many
        // users would expect that putting log4j there would cause it to be
        // detected (and this is the historical JCL behaviour). So we go with
        // the first approach. A user that has bundled a specific logging lib
        // in a webapp should use a commons-logging.properties file or a
        // service file in META-INF to force use of that logging lib anyway,
        // rather than relying on discovery.
        
        for(int i=0; (i<classesToDiscover.length) && (result == null); ++i) {
            result = createLogFromClass(classesToDiscover[i], logCategory, true);
        }
        
        if (result == null) {
            throw new LogConfigurationException
                        ("No suitable Log implementation");
        }
        
        return result;        
    }


    
    /**
     * Appends message if the given name is similar to the candidate.
     * @param messageBuffer <code>StringBuffer</code> the message should be appended to, 
     * not null
     * @param name the (trimmed) name to be test against the candidate, not null
     * @param candidate the candidate name 
     */
    private void informUponSimilarName(final StringBuffer messageBuffer, final String name, 
            final String candidate) {
        // this formular (first four letters of the name excluding package) 
        // gives a reason guess
        if (candidate.regionMatches(true, 0, name, 0, 38)) {
            messageBuffer.append(" Did you mean '");
            messageBuffer.append(candidate);
            messageBuffer.append("'?");
        }
    }
    
    
    /**
     * Checks system properties and the attribute map for 
     * a Log implementation specified by the user under the 
     * property names {@link #LOG_PROPERTY} or {@link #LOG_PROPERTY_OLD}.
     * 
     * @return classname specified by the user, or <code>null</code>
     */
    private String findUserSpecifiedLogClassName()
    {
        if (isDiagnosticsEnabled()) {
            logDiagnostic("Trying to get log class from attribute '" + LOG_PROPERTY + "'");
        }
        String specifiedClass = (String) getAttribute(LOG_PROPERTY);

        if (specifiedClass == null) { // @deprecated
            if (isDiagnosticsEnabled()) {
                logDiagnostic("Trying to get log class from attribute '" + 
                              LOG_PROPERTY_OLD + "'");
            }
            specifiedClass = (String) getAttribute(LOG_PROPERTY_OLD);
        }

        if (specifiedClass == null) {
            if (isDiagnosticsEnabled()) {
                logDiagnostic("Trying to get log class from system property '" + 
                          LOG_PROPERTY + "'");
            }
            try {
                specifiedClass = System.getProperty(LOG_PROPERTY);
            } catch (SecurityException e) {
                if (isDiagnosticsEnabled()) {
                    logDiagnostic("No access allowed to system property '" + 
                        LOG_PROPERTY + "' - " + e.getMessage());
                }
            }
        }

        if (specifiedClass == null) { // @deprecated
            if (isDiagnosticsEnabled()) {
                logDiagnostic("Trying to get log class from system property '" + 
                          LOG_PROPERTY_OLD + "'");
            }
            try {
                specifiedClass = System.getProperty(LOG_PROPERTY_OLD);
            } catch (SecurityException e) {
                if (isDiagnosticsEnabled()) {
                    logDiagnostic("No access allowed to system property '" + 
                        LOG_PROPERTY_OLD + "' - " + e.getMessage());
                }
            }
        }
        
        return specifiedClass;
        
    }

    
    /**
     * Attempts to load the given class, find a suitable constructor,
     * and instantiate an instance of Log.
     * 
     * @param logAdapterClassName classname of the Log implementation
     * 
     * @param logCategory  argument to pass to the Log implementation's
     * constructor
     * 
     * @param affectState  <code>true</code> if this object's state should
     * be affected by this method call, <code>false</code> otherwise.
     * 
     * @return  an instance of the given class, or null if the logging
     * library associated with the specified adapter is not available.
     *                          
     * @throws LogConfigurationException if there was a serious error with
     * configuration and the handleFlawedDiscovery method decided this
     * problem was fatal.
     */                                  
    private Log createLogFromClass(String logAdapterClassName,
                                   String logCategory,
                                   boolean affectState) 
            throws LogConfigurationException {       

        if (isDiagnosticsEnabled()) {
            logDiagnostic("Attempting to instantiate '" + logAdapterClassName + "'");
        }
        
        Object[] params = { logCategory };
        Log logAdapter = null;
        Constructor constructor = null;
        
        Class logAdapterClass = null;
        ClassLoader currentCL = getBaseClassLoader();
        
        //
        // This variable is used to ensure that the system classloader
        // is tried only once when getParent is null.
        boolean systemClassloaderTried = false;
        
        for(;;) {
            // Loop through the classloader hierarchy trying to find
            // a viable classloader.
            logDiagnostic(
                    "Trying to load '"
                    + logAdapterClassName
                    + "' from classloader "
                    + objectId(currentCL));
            try {
                Class c = null;
                try {
                    c = Class.forName(logAdapterClassName, true, currentCL);
                } catch (ClassNotFoundException originalClassNotFoundException) {
                    // The current classloader was unable to find the log adapter 
                    // in this or any ancestor classloader. There's no point in
                    // trying higher up in the hierarchy in this case..
                    String msg = "" + originalClassNotFoundException.getMessage();
                    logDiagnostic(
                        "The log adapter '"
                        + logAdapterClassName
                        + "' is not available via classloader " 
                        + objectId(currentCL)
                        + ": "
                        + msg.trim());
                    try {
                        // Try the class classloader.
                        // This may work in cases where the TCCL
                        // does not contain the code executed or JCL.
                        // This behaviour indicates that the application 
                        // classloading strategy is not consistent with the
                        // Java 1.2 classloading guidelines but JCL can
                        // and so should handle this case.
                        c = Class.forName(logAdapterClassName);
                    } catch (ClassNotFoundException secondaryClassNotFoundException) {
                        // no point continuing: this adapter isn't available
                        msg = "" + secondaryClassNotFoundException.getMessage();
                        logDiagnostic(
                            "The log adapter '"
                            + logAdapterClassName
                            + "' is not available via the LogFactoryImpl class classloader: "
                            + msg.trim());
                        break;
                    }
                }
                constructor = c.getConstructor(logConstructorSignature);
                Object o = constructor.newInstance(params);

                // Note that we do this test after trying to create an instance
                // [rather than testing Log.class.isAssignableFrom(c)] so that
                // we don't complain about Log hierarchy problems when the
                // adapter couldn't be instantiated anyway.
                if (o instanceof Log) {
                    logAdapterClass = c;
                    logAdapter = (Log) o;
                    break;
                }
            
                
                // Oops, we have a potential problem here. An adapter class
                // has been found and its underlying lib is present too, but
                // there are multiple Log interface classes available making it
                // impossible to cast to the type the caller wanted. We 
                // certainly can't use this logger, but we need to know whether
                // to keep on discovering or terminate now.
                //
                // The handleFlawedHierarchy method will throw 
                // LogConfigurationException if it regards this problem as
                // fatal, and just return if not.
                handleFlawedHierarchy(currentCL, c);
            } catch (NoClassDefFoundError e) {
                // We were able to load the adapter but it had references to
                // other classes that could not be found. This simply means that
                // the underlying logger library is not present in this or any
                // ancestor classloader. There's no point in trying higher up
                // in the hierarchy in this case..
                String msg = "" + e.getMessage();
                logDiagnostic(
                    "The log adapter '"
                    + logAdapterClassName
                    + "' is missing dependencies when loaded via classloader "
                    + objectId(currentCL)
                    + ": "
                    + msg.trim());
                break;
            } catch (ExceptionInInitializerError e) {
                // A static initializer block or the initializer code associated 
                // with a static variable on the log adapter class has thrown
                // an exception.
                //
                // We treat this as meaning the adapter's underlying logging
                // library could not be found.
                String msg = "" + e.getMessage();
                logDiagnostic(
                    "The log adapter '"
                    + logAdapterClassName
                    + "' is unable to initialize itself when loaded via classloader "
                    + objectId(currentCL)
                    + ": "
                    + msg.trim());
                break;
            } catch(LogConfigurationException e) {
                // call to handleFlawedHierarchy above must have thrown
                // a LogConfigurationException, so just throw it on                
                throw e;
            } catch(Throwable t) {
                // handleFlawedDiscovery will determine whether this is a fatal
                // problem or not. If it is fatal, then a LogConfigurationException
                // will be thrown.
                handleFlawedDiscovery(logAdapterClassName, currentCL, t);
            }
                        
            if (currentCL == null) {
                break;
            }
            
            // try the parent classloader
            final ClassLoader parentCL = currentCL.getParent();
            
            //
            // getParent may return null to indicate that the parent
            // is the 'bootstrap classloader'. This term is difficult.
            // A reasonable way to interpret this is as
            // the system classloader which is provided as a base
            // for delegating classloaders.
            // 
            // Note that this functionality cannot be easily tested
            // since it depends upon an optional behaviour of the basic
            // java libraries. The Sun libraries do not behave in this 
            // fashion. It may be possible to create a test that
            // uses a customized boot classpath containing a special 
            // implementation but this approach
            // would need to wait until an open source Java implementation
            // exists. So sadly, this code path is not unit tested.
            //
            if (parentCL == null) {
                if (systemClassloaderTried == true)
                {
                    logDiagnostic("Parent classloader is NULL. But System ClassLoader has already been tried.");
                    break;
                }
                // try system classloader
                try {
                    final ClassLoader systemClassLoader = ClassLoader.getSystemClassLoader();
                    if (systemClassLoader == null) {
                        logDiagnostic("System classloader is NULL. Cannot find parent of classloader " 
                                        + objectId(currentCL));
                        break;
                    } else if (systemClassLoader.equals(currentCL)) {
                        // the system classloader has already been tried and failed
                        logDiagnostic("System classloader tried and failed.");
                        break;
                    } else {
                        // the parent is null indicating that the parent is the boot classloader
                        // so retry with system classloader
                        currentCL = systemClassLoader;
                        
                        //
                        // avoid infinite loops by trying the system loader only the
                        // first time a classloader 
                        systemClassloaderTried = true;
                        logDiagnostic("Parent classloader is NULL. Trying System ClassLoader.");
                    }
                } catch (Throwable t) {
                    // getSystemClassLoader is allowed to fail in 
                    // many strange ways: so need to catch everything
                    // including errors
                    logDiagnostic("Failed to get system classloader: '" 
                                        + t.getMessage() 
                                        + "'. Cannot find parent of classloader " 
                                        + objectId(currentCL));
                    break;
                }
            } else {
                currentCL = parentCL;
            }
                
        }

        if ((logAdapter != null) && affectState) {
            // We've succeeded, so set instance fields
            this.logClassName   = logAdapterClassName;
            this.logConstructor = constructor;
            
            // Identify the <code>setLogFactory</code> method (if there is one)
            try {
                this.logMethod = logAdapterClass.getMethod("setLogFactory",
                                               logMethodSignature);
                logDiagnostic("Found method setLogFactory(LogFactory) in '" 
                              + logAdapterClassName + "'");
            } catch (Throwable t) {
                this.logMethod = null;
                logDiagnostic(
                    "[INFO] '" + logAdapterClassName 
                    + "' from classloader " + objectId(currentCL)
                    + " does not declare optional method "
                    + "setLogFactory(LogFactory)");
            }
            
            logDiagnostic(
                "Log adapter '" + logAdapterClassName 
                + "' from classloader " + objectId(currentCL)
                + " has been selected for use.");
        }
        
        return logAdapter;
    }
    
    
    /**
     * Return the classloader from which we should try to load the logging
     * adapter classes.
     * <p>
     * This method usually returns the context classloader. However if it
     * is discovered that the classloader which loaded this class is a child
     * of the context classloader <i>and</i> the allowFlawedContext option
     * has been set then the classloader which loaded this class is returned
     * instead.
     * <p>
     * The only time when the classloader which loaded this class is a
     * descendant (rather than the same as or an ancestor of the context
     * classloader) is when an app has created custom classloaders but
     * failed to correctly set the context classloader. This is a bug in
     * the calling application; however we provide the option for JCL to
     * simply generate a warning rather than fail outright.
     * 
     */
    private ClassLoader getBaseClassLoader() throws LogConfigurationException {
        ClassLoader thisClassLoader = getClassLoader(LogFactoryImpl.class);
        
        if (useTCCL == false) {
            return thisClassLoader;
        }

        ClassLoader contextClassLoader = getContextClassLoader();

        ClassLoader baseClassLoader = getLowestClassLoader(
                contextClassLoader, thisClassLoader);
        
        if (baseClassLoader == null) {
           // The two classloaders are not part of a parent child relationship.
           // In some classloading setups (e.g. JBoss with its 
           // UnifiedLoaderRepository) this can still work, so if user hasn't
           // forbidden it, just return the contextClassLoader.
           if (allowFlawedContext) {   
              if (isDiagnosticsEnabled()) {
                   logDiagnostic(
                           "Warning: the context classloader is not part of a"
                           + " parent-child relationship with the classloader that"
                           + " loaded LogFactoryImpl.");
              }
              // If contextClassLoader were null, getLowestClassLoader() would
              // have returned thisClassLoader.  The fact we are here means
              // contextClassLoader is not null, so we can just return it.
              return contextClassLoader;
           }
           else {
            throw new LogConfigurationException(
                "Bad classloader hierarchy; LogFactoryImpl was loaded via"
                + " a classloader that is not related to the current context"
                + " classloader.");
           }           
        }

        if (baseClassLoader != contextClassLoader) {
            // We really should just use the contextClassLoader as the starting
            // point for scanning for log adapter classes. However it is expected
            // that there are a number of broken systems out there which create
            // custom classloaders but fail to set the context classloader so
            // we handle those flawed systems anyway.
            if (allowFlawedContext) {
                if (isDiagnosticsEnabled()) {
                    logDiagnostic(
                            "Warning: the context classloader is an ancestor of the"
                            + " classloader that loaded LogFactoryImpl; it should be"
                            + " the same or a descendant. The application using"
                            + " commons-logging should ensure the context classloader"
                            + " is used correctly.");
                }
            } else {
                throw new LogConfigurationException(
                        "Bad classloader hierarchy; LogFactoryImpl was loaded via"
                        + " a classloader that is not related to the current context"
                        + " classloader."); 
            }
        }
        
        return baseClassLoader;
    }

    /**
     * Given two related classloaders, return the one which is a child of
     * the other.
     * <p>
     * @param c1 is a classloader (including the null classloader)
     * @param c2 is a classloader (including the null classloader)
     * 
     * @return c1 if it has c2 as an ancestor, c2 if it has c1 as an ancestor,
     * and null if neither is an ancestor of the other.
     */
    private ClassLoader getLowestClassLoader(ClassLoader c1, ClassLoader c2) {
        // TODO: use AccessController when dealing with classloaders here
        
        if (c1 == null)
            return c2;
        
        if (c2 == null)
            return c1;
        
        ClassLoader current;

        // scan c1's ancestors to find c2
        current = c1;
        while (current != null) {
            if (current == c2)
                return c1;
            current = current.getParent();
        }
       
       // scan c2's ancestors to find c1
        // scan c1's ancestors to find c2
        current = c2;
        while (current != null) {
            if (current == c1)
                return c2;
            current = current.getParent();
        }

        return null;
    }

    /**
     * Generates an internal diagnostic logging of the discovery failure and 
     * then throws a <code>LogConfigurationException</code> that wraps 
     * the passed <code>Throwable</code>.
     * 
     * @param logAdapterClassName is the class name of the Log implementation
     * that could not be instantiated. Cannot be <code>null</code>.
     * 
     * @param classLoader is the classloader that we were trying to load the
     * logAdapterClassName from when the exception occurred.
     * 
     * @param discoveryFlaw is the Throwable created by the classloader
     * 
     * @throws LogConfigurationException    ALWAYS
     */
    private void handleFlawedDiscovery(String logAdapterClassName,
                                       ClassLoader classLoader,
                                       Throwable discoveryFlaw) {
        
        if (isDiagnosticsEnabled()) {
            logDiagnostic("Could not instantiate Log '"
                      + logAdapterClassName + "' -- "
                      + discoveryFlaw.getLocalizedMessage());       
        }
        
        if (!allowFlawedDiscovery) {
            throw new LogConfigurationException(discoveryFlaw);
        }
    }

    
    /**
     * Report a problem loading the log adapter, then either return 
     * (if the situation is considered recoverable) or throw a
     * LogConfigurationException.
     *  <p>
     * There are two possible reasons why we successfully loaded the 
     * specified log adapter class then failed to cast it to a Log object:
     * <ol>
     * <li>the specific class just doesn't implement the Log interface 
     *     (user screwed up), or
     * <li> the specified class has bound to a Log class loaded by some other
     *      classloader; Log@classloaderX cannot be cast to Log@classloaderY.
     * </ol>
     * <p>
     * Here we try to figure out which case has occurred so we can give the
     * user some reasonable feedback.
     * 
     * @param badClassLoader is the classloader we loaded the problem class from,
     * ie it is equivalent to badClass.getClassLoader().
     * 
     * @param badClass is a Class object with the desired name, but which 
     * does not implement Log correctly.
     * 
     * @throws LogConfigurationException when the situation
     * should not be recovered from.
     */
    private void handleFlawedHierarchy(ClassLoader badClassLoader, Class badClass)
    throws LogConfigurationException {

        boolean implementsLog = false;
        String logInterfaceName = Log.class.getName();
        Class interfaces[] = badClass.getInterfaces();
        for (int i = 0; i < interfaces.length; i++) {
            if (logInterfaceName.equals(interfaces[i].getName())) {
                implementsLog = true;
                break;
            }
        }
        
        if (implementsLog) {
            // the class does implement an interface called Log, but
            // it is in the wrong classloader
            if (isDiagnosticsEnabled()) {
                try {
                    ClassLoader logInterfaceClassLoader = getClassLoader(Log.class);
                    logDiagnostic(
                        "Class " + badClass.getName()
                        + " was found in classloader " 
                        + objectId(badClassLoader)
                        + ". It is bound to a Log interface which is not"
                        + " the one loaded from classloader "
                        + objectId(logInterfaceClassLoader));
                } catch (Throwable t) {
                    logDiagnostic(
                        "Error while trying to output diagnostics about"
                        + " bad class " + badClass);
                }
            }
            
            if (!allowFlawedHierarchy) {
                StringBuffer msg = new StringBuffer();
                msg.append("Terminating logging for this context ");
                msg.append("due to bad log hierarchy. ");
                msg.append("You have more than one version of ");
                msg.append(Log.class.getName());
                msg.append(" visible.");
                if (isDiagnosticsEnabled()) {
                    logDiagnostic(msg.toString());
                } 
                throw new LogConfigurationException(msg.toString());
            }
        
            if (isDiagnosticsEnabled()) {
                StringBuffer msg = new StringBuffer();
                msg.append("Warning: bad log hierarchy. ");
                msg.append("You have more than one version of ");
                msg.append(Log.class.getName());
                msg.append(" visible.");
                logDiagnostic(msg.toString());
            }
        } else {
            // this is just a bad adapter class
            if (!allowFlawedDiscovery) {
                StringBuffer msg = new StringBuffer();
                msg.append("Terminating logging for this context. ");
                msg.append("Log class ");
                msg.append(badClass.getName());
                msg.append(" does not implement the Log interface.");
                if (isDiagnosticsEnabled()) {
                    logDiagnostic(msg.toString());
                }
                
                throw new LogConfigurationException(msg.toString());
            }

            if (isDiagnosticsEnabled()) {
                StringBuffer msg = new StringBuffer();
                msg.append("Warning: Log class ");
                msg.append(badClass.getName());
                msg.append(" does not implement the Log interface.");
                logDiagnostic(msg.toString());
            }
        }
    }
}
