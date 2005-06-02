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
 * <code>getInstance()</code> method.
 *
 * @author Rod Waldhoff
 * @author Craig R. McClanahan
 * @author Richard A. Sitze
 * @author Brian Stansberry
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
     * See LogFactory.isDiagnosticsEnabled.
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
     * MUST KEEP THIS METHOD PRIVATE.
     *
     * <p>Exposing this method outside of
     * <code>org.apache.commons.logging.LogFactoryImpl</code>
     * will create a security violation:
     * This method uses <code>AccessController.doPrivileged()</code>.
     * </p>
     *
     * Load a class, try first the thread class loader, and
     * if it fails use the loader that loaded this class.
     * 
     * @param name fully qualified class name of the class to load
     *                          
     * @throws LinkageError if the linkage fails
     * @throws ExceptionInInitializerError if the initialization provoked
     *            by this method fails
     * @throws ClassNotFoundException if the class cannot be located
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
                "org.apache.commons.logging.impl.Log4JLogger");
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
        logDiagnostic("Checking for " + name + ".");
        try {
            Log log = createLogFromClass(
                        classname, 
                        this.getClass().getName(), // dummy category
                        false);

            if (log == null) {
                logDiagnostic("Did not find " + name + ".");
                return false;
            } else {
                logDiagnostic("Found " + name + ".");
                return true;
            }
        } catch(LogConfigurationException e) {
            logDiagnostic("Logging system " + name + " is available but not useable.");
            return false;
        }
    }
  

    /**
     * Attempts to create a Log instance for the given category name.
     * Follows the discovery process described in the class javadoc.
     * 
     * @param logCategory the name of the log category
     * 
     * @throws LogConfigurationException if an error in discovery occurs, 
     *                                   or if no adapter at all can be 
     *                                   instantiated
     */
    private Log discoverLogImplementation(String logCategory)
    throws LogConfigurationException
    {
        logDiagnostic("Attempting to discover a Log implementation.");
        
        Log result = null;
        
        // See if the user specified the Log implementation to use
        String specifiedLogClassName = findUserSpecifiedLogClassName();

        if (specifiedLogClassName != null) {
            // note: createLogFromClass never returns null..
            result = createLogFromClass(specifiedLogClassName,
                                        logCategory,
                                        true);
            if (result == null) {
                throw new LogConfigurationException(
                        "User-specified log class " + specifiedLogClassName
                        + " cannot be found or is not useable.");
            }
            
            return result;
        }
        
        // No user specified log; try to discover what's on the classpath
        
        // Try Log4j
        result = createLogFromClass("org.apache.commons.logging.impl.Log4JLogger",
                                    logCategory,
                                    true);

        if (result == null) {
            result = createLogFromClass("org.apache.commons.logging.impl.Jdk14Logger",
                                        logCategory,
                                        true);
        }

        if (result == null) {
            result = createLogFromClass("org.apache.commons.logging.impl.Jdk13LumberjackLogger",
                                        logCategory,
                                        true);
        }

        if (result == null) {
            result = createLogFromClass("org.apache.commons.logging.impl.SimpleLog",
                                        logCategory,
                                        true);
        }
        
        if (result == null) {
            throw new LogConfigurationException
                        ("No suitable Log implementation");
        }
        
        return result;        
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
        logDiagnostic("Trying to get log class from attribute " + LOG_PROPERTY);
        String specifiedClass = (String) getAttribute(LOG_PROPERTY);

        if (specifiedClass == null) { // @deprecated
            logDiagnostic("Trying to get log class from attribute " + 
                          LOG_PROPERTY_OLD);
            specifiedClass = (String) getAttribute(LOG_PROPERTY_OLD);
        }

        if (specifiedClass == null) {
            logDiagnostic("Trying to get log class from system property " + 
                          LOG_PROPERTY);
            try {
                specifiedClass = System.getProperty(LOG_PROPERTY);
            } catch (SecurityException e) {
                ;
            }
        }

        if (specifiedClass == null) { // @deprecated
            logDiagnostic("Trying to get log class from system property " + 
                          LOG_PROPERTY_OLD);
            try {
                specifiedClass = System.getProperty(LOG_PROPERTY_OLD);
            } catch (SecurityException e) {
                ;
            }
        }
        
        return specifiedClass;
        
    }

    
    /**
     * Attempts to load the given class, find a suitable constructor,
     * and instantiate an instance of Log.
     * 
     * @param   logAdapterClass classname of the Log implementation
     * @param   logCategory  argument to pass to the Log implementation's
     *                       constructor
     * @param   affectState  <code>true</code> if this object's state should
     *                       be affected by this method call, <code>false</code>
     *                       otherwise.
     * 
     * @return  an instance of the given class, or null if the logging
     * library associated with the specified adapter is not available.
     *                          
     * @throws LogConfigurationException if there was a serious error with
     * configuration and the handleFlawedDiscovery method decided this
     * problem was fatal.
     */                                  
    private Log createLogFromClass(String logAdapterClass,
                                   String logCategory,
                                   boolean affectState) 
            throws LogConfigurationException {       

        logDiagnostic("Attempting to instantiate " + logAdapterClass);
        
        Class logClass = null;
        
        Object[] params = { logCategory };
        Log logAdapter = null;
        Constructor constructor = null;
        try {
            logClass = loadClass(logAdapterClass);
            constructor = logClass.getConstructor(logConstructorSignature);
            logAdapter = (Log) constructor.newInstance(params);
        } catch (NoClassDefFoundError e) {
            // We were able to load the adapter but it had references to
            // other classes that could not be found. This simply means that
            // the underlying logger library could not be found.
            String msg = "" + e.getMessage();
            logDiagnostic(
                    "The logging library used by "
                    + logAdapterClass
                    + " is not available: " 
                    + msg.trim());
            return null;
        } catch (ExceptionInInitializerError e) {
            // A static initializer block or the initializer code associated 
            // with a static variable on the log adapter class has thrown
            // an exception.
            //
            // We treat this as meaning the adapter's underlying logging
            // library could not be found.
            String msg = "" + e.getMessage();
            logDiagnostic(
                    "The logging library used by "
                    + logAdapterClass
                    + " is not available: "
                    + msg.trim());
            return null;
        } catch(Throwable t) {
            // handleFlawedDiscovery will determine whether this is a fatal
            // problem or not. If it is fatal, then a LogConfigurationException
            // will be thrown.
            handleFlawedDiscovery(logAdapterClass, logClass, t);
            return null;
        }
        
        if (affectState) {
            // We've succeeded, so set instance fields
            this.logClassName   = logClass.getName();
            this.logConstructor = constructor;
            
            // Identify the <code>setLogFactory</code> method (if there is one)
            try {
                this.logMethod = logClass.getMethod("setLogFactory",
                                               logMethodSignature);
                logDiagnostic("Found method setLogFactory(LogFactory) in " 
                              + logClassName);
            } catch (Throwable t) {
                this.logMethod = null;
                logDiagnostic(logAdapterClass + " does not declare method "
                              + "setLogFactory(LogFactory)");
            }
        }
        
        return logAdapter;
    }
    
    
    /**
     * Generates an internal diagnostic logging of the discovery failure and 
     * then throws a <code>LogConfigurationException</code> that wraps 
     * the passed <code>Throwable</code>.
     * 
     * @param logClassName  the class name of the Log implementation
     *                      that could not be instantiated. Cannot be
     *                      <code>null</code>.
     * @param adapterClass  <code>Code</code> whose name is
     *                      <code>logClassName</code>, or <code>null</code> if
     *                      discovery was unable to load the class.
     * @param discoveryFlaw Throwable thrown during discovery.
     * 
     * @throws LogConfigurationException    ALWAYS
     */
    private void handleFlawedDiscovery(String logClassName,
                                       Class adapterClass,
                                       Throwable discoveryFlaw) {
        
        // Output diagnostics
        
        // For ClassCastException use the more complex diagnostic
        // that analyzes the classloader hierarchy
        if (   discoveryFlaw instanceof ClassCastException
            && adapterClass != null) {
            // reportInvalidAdapter returns a LogConfigurationException
            // that wraps the ClassCastException; replace variable 
            // 'discoveryFlaw' with that so we can rethrow the LCE
            discoveryFlaw = reportInvalidLogAdapter(adapterClass, 
                                                    discoveryFlaw);
        }
        else {
            logDiagnostic("Could not instantiate Log "
                          + logClassName + " -- "
                          + discoveryFlaw.getLocalizedMessage());        
        }
    
        
        if (discoveryFlaw instanceof LogConfigurationException) {
            throw (LogConfigurationException) discoveryFlaw;
        }
        else {
            throw new LogConfigurationException(discoveryFlaw);
        }
        
    }

    
    /**
     * Report a problem loading the log adapter, then return
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
     * @param logClass is the adapter class we successfully loaded (but which
     * could not be cast to type logInterface). Cannot be <code>null</code>.
     * @param cause is the <code>Throwable</code> to wrap.
     * 
     * @return  <code>LogConfigurationException</code> that wraps 
     *          <code>cause</code> and includes a diagnostic message.
     */
    private LogConfigurationException reportInvalidLogAdapter(Class logClass,
                                                              Throwable cause) {
        
        Class interfaces[] = logClass.getInterfaces();
        for (int i = 0; i < interfaces.length; i++) {
            if (LOG_INTERFACE.equals(interfaces[i].getName())) {

                if (isDiagnosticsEnabled()) {                    
                    
                    try {
                        // Need to load the log interface so we know its
                        // classloader for diagnostics
                        Class logInterface = null;
                        ClassLoader cl = getClassLoader(this.getClass());
                        if (cl == null) {
                            // we are probably in Java 1.1, but may also be 
                            // running in some sort of embedded system..
                            logInterface = loadClass(LOG_INTERFACE);
                        } else {
                            // normal situation
                            logInterface = cl.loadClass(LOG_INTERFACE);
                        }

                        ClassLoader logInterfaceClassLoader = getClassLoader(logInterface);
                        ClassLoader logAdapterClassLoader = getClassLoader(logClass);
                        Class logAdapterInterface = interfaces[i];
                        ClassLoader logAdapterInterfaceClassLoader = getClassLoader(logAdapterInterface);
                        logDiagnostic(
                            "Class " + logClass.getName()
                            + " was found in classloader " 
                            + objectId(logAdapterClassLoader)
                            + " but it implements the Log interface as loaded"
                            + " from classloader " 
                            + objectId(logAdapterInterfaceClassLoader)
                            + " not the one loaded by this class's classloader "
                            + objectId(logInterfaceClassLoader));
                    } catch (Throwable t) {
                        ;
                    }
                }
                
                return new LogConfigurationException
                    ("Invalid class loader hierarchy.  " +
                     "You have more than one version of '" +
                     LOG_INTERFACE + "' visible, which is " +
                     "not allowed.", cause);
            }
        }
            
        return new LogConfigurationException
            ("Class " + logClassName + " does not implement '" +
                    LOG_INTERFACE + "'.", cause);
    }

}
