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
import java.security.AccessController;
import java.security.PrivilegedAction;
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
 * <code>getInstance()</code> method.  This implementation ignores any
 * configured attributes.</p>
 *
 * @author Rod Waldhoff
 * @author Craig R. McClanahan
 * @author Richard A. Sitze
 * @version $Revision$ $Date$
 */

public class LogFactoryImpl extends LogFactory {

    // ----------------------------------------------------------- Constructors


    /**
     * Public no-arguments constructor required by the lookup mechanism.
     */
    public LogFactoryImpl() {
        super();
        initDiagnostics();  // method on this object
        logDiagnostic("Instance created.");
    }


    // ----------------------------------------------------- Manifest Constants


    /**
     * The name of the system property identifying our {@link Log}
     * implementation class.
     */
    public static final String LOG_PROPERTY =
        "org.apache.commons.logging.Log";


    /**
     * The deprecated system property used for backwards compatibility with
     * the old {@link org.apache.commons.logging.LogSource} class.
     */
    protected static final String LOG_PROPERTY_OLD =
        "org.apache.commons.logging.log";


    /**
     * <p>The name of the {@link Log} interface class.</p>
     */
    private static final String LOG_INTERFACE =
        "org.apache.commons.logging.Log";


    // ----------------------------------------------------- Instance Variables


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
     *
     * @param name Name of the attribute to set
     * @param value Value of the attribute to set, or <code>null</code>
     *  to remove any setting for this attribute
     */
    public void setAttribute(String name, Object value) {

        if (value == null) {
            attributes.remove(name);
        } else {
            attributes.put(name, value);
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
     */
    protected static ClassLoader getContextClassLoader() throws LogConfigurationException {
        return LogFactory.getContextClassLoader();
    }

    /**
     * Workaround for bug in Java1.2; in theory this method is not needed.
     * See LogFactory.isInternalLoggingEnabled.
     */
    protected static boolean isDiagnosticsEnabled() {
        return LogFactory.isDiagnosticsEnabled();
    }

    /**
     * Workaround for bug in Java1.2; in theory this method is not needed.
     * See LogFactory.getClassLoader.
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
        Class clazz = this.getClass();
        ClassLoader classLoader = getClassLoader(clazz);
        diagnosticPrefix = clazz.getName() + "@" + classLoader.toString() + ":";
    }

    /**
     * Output a diagnostic message to a user-specified destination (if the
     * user has enabled diagnostic logging).
     * 
     * @param msg
     */
    protected void logDiagnostic(String msg) {
        if (isDiagnosticsEnabled()) {
            logRawDiagnostic(diagnosticPrefix + msg);
        }
    }

    /**
     * Return the fully qualified Java classname of the {@link Log}
     * implementation we will be using.
     * <p>
     * This method looks in the following places:
     * <ul>
     * <li>Looks for an attribute LOG_PROPERTY or LOG_PROPERTY_OLD in the 
     * "attributes" associated with this class, as set earlier by method 
     * setAttribute.
     * <li>Looks for a property LOG_PROPERTY or LOG_PROPERTY_OLD in the
     * system properties.
     * <li>Looks for log4j, jdk logging and jdk13lumberjack classes in
     * the classpath.
     * </ul>
     */
    protected String getLogClassName() {

        // Return the previously identified class name (if any)
        if (logClassName != null) {
            return logClassName;
        }

        logDiagnostic("Determining the name for the Log implementation.");

        logDiagnostic("Trying to get log class from attribute " + LOG_PROPERTY);
        logClassName = (String) getAttribute(LOG_PROPERTY);

        if (logClassName == null) { // @deprecated
            logDiagnostic("Trying to get log class from attribute " + LOG_PROPERTY_OLD);
            logClassName = (String) getAttribute(LOG_PROPERTY_OLD);
        }

        if (logClassName == null) {
            try {
                logDiagnostic("Trying to get log class from system property " + LOG_PROPERTY);
                logClassName = System.getProperty(LOG_PROPERTY);
            } catch (SecurityException e) {
                ;
            }
        }

        if (logClassName == null) { // @deprecated
            try {
                logDiagnostic("Trying to get log class from system property " + LOG_PROPERTY_OLD);
                logClassName = System.getProperty(LOG_PROPERTY_OLD);
            } catch (SecurityException e) {
                ;
            }
        }

        // no need for internalLog calls below; they are done inside the
        // various isXXXAvailable methods.
        if ((logClassName == null) && isLog4JAvailable()) {
            logClassName = "org.apache.commons.logging.impl.Log4JLogger";
        }

        if ((logClassName == null) && isJdk14Available()) {
            logClassName = "org.apache.commons.logging.impl.Jdk14Logger";
        }

        if ((logClassName == null) && isJdk13LumberjackAvailable()) {
            logClassName = "org.apache.commons.logging.impl.Jdk13LumberjackLogger";
        }

        if (logClassName == null) {
            logClassName = "org.apache.commons.logging.impl.SimpleLog";
        }

        logDiagnostic("Using log class " + logClassName);
        return (logClassName);

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
     */
    protected Constructor getLogConstructor()
        throws LogConfigurationException {

        // Return the previously identified Constructor (if any)
        if (logConstructor != null) {
            return logConstructor;
        }

        String logClassName = getLogClassName();

        // Attempt to load the Log implementation class
        //
        // Question: why is the loginterface being loaded dynamically?
        // Isn't the code below exactly the same as this?
        //    Class logInterface = Log.class;
        
        Class logClass = null;
        Class logInterface = null;
        try {
            ClassLoader cl = getClassLoader(this.getClass());
            if (cl == null) {
                // we are probably in Java 1.1, but may also be running in
                // some sort of embedded system..
                logInterface = loadClass(LOG_INTERFACE);
            } else {
                // normal situation
                logInterface = cl.loadClass(LOG_INTERFACE);
            }

            logClass = loadClass(logClassName);
            if (logClass == null) {
                logDiagnostic(
                    "Unable to find any class named [" + logClassName + "]"
                    + " in either the context classloader"
                    + " or the classloader that loaded this class.");

                throw new LogConfigurationException
                    ("No suitable Log implementation for " + logClassName);
            }
            
            if (!logInterface.isAssignableFrom(logClass)) {
                // oops, we need to cast this logClass we have loaded into
                // a Log object in order to return it. But we won't be
                // able to. See method reportInvalidLogAdapter for more
                // information.
                LogConfigurationException ex = 
                    reportInvalidLogAdapter(logInterface, logClass);
                throw ex;
            }
        } catch (Throwable t) {
            logDiagnostic(
                "An unexpected problem occurred while loading the"
                + " log adapter class: " + t.getMessage());
            throw new LogConfigurationException(t);
        }

        // Identify the <code>setLogFactory</code> method (if there is one)
        try {
            logMethod = logClass.getMethod("setLogFactory",
                                           logMethodSignature);
        } catch (Throwable t) {
            logMethod = null;
        }

        // Identify the corresponding constructor to be used
        try {
            logConstructor = logClass.getConstructor(logConstructorSignature);
            return (logConstructor);
        } catch (Throwable t) {
            throw new LogConfigurationException
                ("No suitable Log constructor " +
                 logConstructorSignature+ " for " + logClassName, t);
        }
    }

    /**
     * Report a problem loading the log adapter, then <i>always</i> throw
     * a LogConfigurationException.
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
     *  user some reasonable feedback.
     * 
     * @param logInterface is the class that this LogFactoryImpl class needs
     * to return the adapter as.
     * @param logClass is the adapter class we successfully loaded (but which
     * could not be cast to type logInterface).
     */
    private LogConfigurationException reportInvalidLogAdapter(
            Class logInterface, Class logClass) { 

        Class interfaces[] = logClass.getInterfaces();
        for (int i = 0; i < interfaces.length; i++) {
            if (LOG_INTERFACE.equals(interfaces[i].getName())) {

                if (isDiagnosticsEnabled()) {
                    ClassLoader logInterfaceClassLoader = getClassLoader(logInterface);
                    ClassLoader logAdapterClassLoader = getClassLoader(logClass);
                    Class logAdapterInterface = interfaces[i];
                    ClassLoader logAdapterInterfaceClassLoader = getClassLoader(logAdapterInterface);
                    logDiagnostic(
                        "Class " + logClassName + " was found in classloader " 
                        + objectId(logAdapterClassLoader)
                        + " but it implements the Log interface as loaded"
                        + " from classloader " + objectId(logAdapterInterfaceClassLoader)
                        + " not the one loaded by this class's classloader "
                        + objectId(logInterfaceClassLoader));
                }
                
                throw new LogConfigurationException
                    ("Invalid class loader hierarchy.  " +
                     "You have more than one version of '" +
                     LOG_INTERFACE + "' visible, which is " +
                     "not allowed.");
            }
        }
            
        return new LogConfigurationException
            ("Class " + logClassName + " does not implement '" +
                    LOG_INTERFACE + "'.");
    }
        
    /**
     * MUST KEEP THIS METHOD PRIVATE.
     *
     * <p>Exposing this method outside of
     * <code>org.apache.commons.logging.LogFactoryImpl</code>
     * will create a security violation:
     * This method uses <code>AccessController.doPrivileged()</code>.
     * </p>
     *
     * Load a class, try first the thread class loader, and
     * if it fails use the loader that loaded this class. Actually, as
     * the thread (context) classloader should always be the same as or a 
     * child of the classloader that loaded this class, the fallback should
     * never be used. 
     */
    private static Class loadClass( final String name )
        throws ClassNotFoundException
    {
        Object result = AccessController.doPrivileged(
            new PrivilegedAction() {
                public Object run() {
                    ClassLoader threadCL = getContextClassLoader();
                    if (threadCL != null) {
                        try {
                            return threadCL.loadClass(name);
                        } catch( ClassNotFoundException ex ) {
                            // ignore
                        }
                    }
                    try {
                        return Class.forName( name );
                    } catch (ClassNotFoundException e) {
                        return e;
                    }
                }
            });

        if (result instanceof Class)
            return (Class)result;

        throw (ClassNotFoundException)result;
    }


    /**
     * Is <em>JDK 1.3 with Lumberjack</em> logging available?
     */
    protected boolean isJdk13LumberjackAvailable() {

        // note: the algorithm here is different from isLog4JAvailable.
        // I think isLog4JAvailable is correct....see bugzilla#31597
        logDiagnostic("Checking for Jdk13Lumberjack.");
        try {
            loadClass("java.util.logging.Logger");
            loadClass("org.apache.commons.logging.impl.Jdk13LumberjackLogger");
            logDiagnostic("Found Jdk13Lumberjack.");
            return true;
        } catch (Throwable t) {
            logDiagnostic("Did not find Jdk13Lumberjack.");
            return false;
        }

    }


    /**
     * <p>Return <code>true</code> if <em>JDK 1.4 or later</em> logging
     * is available.  Also checks that the <code>Throwable</code> class
     * supports <code>getStackTrace()</code>, which is required by
     * Jdk14Logger.</p>
     */
    protected boolean isJdk14Available() {

        // note: the algorithm here is different from isLog4JAvailable.
        // I think isLog4JAvailable is correct....
        logDiagnostic("Checking for Jdk14.");
        try {
            loadClass("java.util.logging.Logger");
            loadClass("org.apache.commons.logging.impl.Jdk14Logger");
            Class throwable = loadClass("java.lang.Throwable");
            if (throwable.getDeclaredMethod("getStackTrace", (Class[]) null) == null) {
                return (false);
            }
            logDiagnostic("Found Jdk14.");
            return true;
        } catch (Throwable t) {
            logDiagnostic("Did not find Jdk14.");
            return false;
        }
    }


    /**
     * Is a <em>Log4J</em> implementation available?
     */
    protected boolean isLog4JAvailable() {

        logDiagnostic("Checking for Log4J");
        try {
            Class adapterClass = loadClass("org.apache.commons.logging.impl.Log4JLogger");
            ClassLoader cl = getClassLoader(adapterClass);
            Class loggerClass = cl.loadClass("org.apache.log4j.Logger" );
            logDiagnostic("Found Log4J");
            return true;
        } catch (Throwable t) {
            logDiagnostic("Did not find Log4J");
            return false;
        }
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
            Object params[] = new Object[1];
            params[0] = name;
            instance = (Log) getLogConstructor().newInstance(params);
            if (logMethod != null) {
                params[0] = this;
                logMethod.invoke(instance, params);
            }
            return (instance);
        } catch (InvocationTargetException e) {
            Throwable c = e.getTargetException();
            if (c != null) {
                throw new LogConfigurationException(c);
            } else {
                throw new LogConfigurationException(e);
            }
        } catch (Throwable t) {
            throw new LogConfigurationException(t);
        }

    }


}
