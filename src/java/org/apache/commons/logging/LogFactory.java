/*
 * $Header: /home/jerenkrantz/tmp/commons/commons-convert/cvs/home/cvs/jakarta-commons//logging/src/java/org/apache/commons/logging/LogFactory.java,v 1.8 2002/06/11 22:29:14 rsitze Exp $
 * $Revision: 1.8 $
 * $Date: 2002/06/11 22:29:14 $
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


import java.io.InputStream;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;
import java.lang.SecurityException;


/**
 * <p>Factory for creating {@link Log} instances, with discovery and
 * configuration features similar to that employed by standard Java APIs
 * such as JAXP.</p>
 *
 * <p><strong>IMPLEMENTATION NOTE</strong> - This implementation is heavily
 * based on the SAXParserFactory and DocumentBuilderFactory implementations
 * (corresponding to the JAXP pluggability APIs) found in Apache Xerces.</p>
 *
 * @author Craig R. McClanahan
 * @author Costin Manolache
 * @version $Revision: 1.8 $ $Date: 2002/06/11 22:29:14 $
 */

public abstract class LogFactory {


    // ----------------------------------------------------- Manifest Constants


    /**
     * The fully qualified class name of the fallback <code>LogFactory</code>
     * implementation class to use, if no other can be found.
     */
    protected static final String FACTORY_DEFAULT =
        "org.apache.commons.logging.impl.LogFactoryImpl";


    /**
     * The name of the properties file to search for.
     */
    protected static final String FACTORY_PROPERTIES =
        "commons-logging.properties";

    /**
     * JDK1.3+ 'Service Provider' specification 
     * ( http://java.sun.com/j2se/1.3/docs/guide/jar/jar.html )
     */
    protected static final String SERVICE_ID =
        "META-INF/services/org.apache.commons.logging.LogFactory";


    /**
     * The name of the property used to identify the LogFactory implementation
     * class name.
     */
    public static final String FACTORY_PROPERTY =
        "org.apache.commons.logging.LogFactory";


    // ----------------------------------------------------------- Constructors


    /**
     * Protected constructor that is not available for public use.
     */
    protected LogFactory() { }


    // --------------------------------------------------------- Public Methods


    /**
     * Return the configuration attribute with the specified name (if any),
     * or <code>null</code> if there is no such attribute.
     *
     * @param name Name of the attribute to return
     */
    public abstract Object getAttribute(String name);


    /**
     * Return an array containing the names of all currently defined
     * configuration attributes.  If there are no such attributes, a zero
     * length array is returned.
     */
    public abstract String[] getAttributeNames();


    /**
     * Convenience method to derive a name from the specified class and
     * call <code>getInstance(String)</code> with it.
     *
     * @param clazz Class for which a suitable Log name will be derived
     *
     * @exception LogConfigurationException if a suitable <code>Log</code>
     *  instance cannot be returned
     */
    public abstract Log getInstance(Class clazz)
        throws LogConfigurationException;


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
    public abstract Log getInstance(String name)
        throws LogConfigurationException;


    /**
     * Release any internal references to previously created {@link Log}
     * instances returned by this factory.  This is useful environments
     * like servlet containers, which implement application reloading by
     * throwing away a ClassLoader.  Dangling references to objects in that
     * class loader would prevent garbage collection.
     */
    public abstract void release();


    /**
     * Remove any configuration attribute associated with the specified name.
     * If there is no such attribute, no action is taken.
     *
     * @param name Name of the attribute to remove
     */
    public abstract void removeAttribute(String name);


    /**
     * Set the configuration attribute with the specified name.  Calling
     * this with a <code>null</code> value is equivalent to calling
     * <code>removeAttribute(name)</code>.
     *
     * @param name Name of the attribute to set
     * @param value Value of the attribute to set, or <code>null</code>
     *  to remove any setting for this attribute
     */
    public abstract void setAttribute(String name, Object value);


    // ------------------------------------------------------- Static Variables


    /**
     * The previously constructed <code>LogFactory</code> instances, keyed by
     * the <code>ClassLoader</code> with which it was created.
     */
    protected static Hashtable factories = new Hashtable();


    // --------------------------------------------------------- Static Methods


    /**
     * <p>Construct (if necessary) and return a <code>LogFactory</code>
     * instance, using the following ordered lookup procedure to determine
     * the name of the implementation class to be loaded:</p>
     * <ul>
     * <li>The <code>org.apache.commons.logging.LogFactory</code> system
     *     property.</li>
     * <li>Use the properties file <code>commons-logging.properties</code>
     *     file, if found in the class path of this class.  The configuration
     *     file is in standard <code>java.util.Propertis</code> format and
     *     contains the fully qualified name of the implementation class
     *     with the key being the system property defined above.</li>
     * <li>Fall back to a default implementation class
     *     (<code>org.apache.commons.logging.impl.LogFactoryImpl</code>).</li>
     * </ul>
     *
     * <p><em>NOTE</em> - If the properties file method of identifying the
     * <code>LogFactory</code> implementation class is utilized, all of the
     * properties defined in this file will be set as configuration attributes
     * on the corresponding <code>LogFactory</code> instance.</p>
     *
     * @exception LogConfigurationException if the implementation class is not
     *  available or cannot be instantiated.
     */
    public static LogFactory getFactory() throws LogConfigurationException {

        // Identify the class loader we will be using
        ClassLoader contextClassLoader = getContextClassLoader();

        // Return any previously registered factory for this class loader
        LogFactory factory = getCachedFactory(contextClassLoader);
        if (factory != null)
            return factory;

        // First, try the system property
        try {
            String factoryClass = System.getProperty(FACTORY_PROPERTY);
            if (factoryClass != null) {
                factory = newFactory(factoryClass, contextClassLoader);
            }
        } catch (SecurityException e) {
            ;  // ignore
        }

        // Second, try to find a service by using the JDK1.3 jar
        // discovery mechanism. This will allow users to plug a logger
        // by just placing it in the lib/ directory of the webapp ( or in
        // CLASSPATH or equivalent ). This is similar with the second
        // step, except that it uses the (standard?) jdk1.3 location in the jar.

        if (factory == null) {
            try {
                InputStream is = (contextClassLoader == null
                                  ? ClassLoader.getSystemResourceAsStream( SERVICE_ID )
                                  : contextClassLoader.getResourceAsStream( SERVICE_ID ));

                if( is != null ) {
                    // This code is needed by EBCDIC and other strange systems.
                    // It's a fix for bugs reported in xerces
                    BufferedReader rd;
                    try {
                        rd = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                    } catch (java.io.UnsupportedEncodingException e) {
                        rd = new BufferedReader(new InputStreamReader(is));
                    }
                    
                    String factoryClassName = rd.readLine();
                    rd.close();
                    
                    if (factoryClassName != null &&
                        ! "".equals(factoryClassName)) {
                        
                        factory= newFactory( factoryClassName, contextClassLoader );
                    }
                }
            } catch( Exception ex ) {
                ;
            }
        }


        Properties props=null;
        
        // Third try a properties file. 
        // If the properties file exists, it'll be read and the properties
        // used. IMHO ( costin ) System property and JDK1.3 jar service
        // should be enough for detecting the class name. The properties
        // should be used to set the attributes ( which may be specific to
        // the webapp, even if a default logger is set at JVM level by a
        // system property )

        try {
            InputStream stream =
                contextClassLoader.getResourceAsStream(FACTORY_PROPERTIES);
            if (stream != null) {
                props = new Properties();
                props.load(stream);
                stream.close();
                String factoryClass = props.getProperty(FACTORY_PROPERTY);
                if( factory==null ) {
                    if (factoryClass == null) {
                        factoryClass = FACTORY_DEFAULT;
                    }
                    factory = newFactory(factoryClass, contextClassLoader);
                }
            }
            // the properties will be set at the end.
        } catch (IOException e) {
        } catch (SecurityException e) {
        }

        // Fourth, try the fallback implementation class
        if (factory == null) {
            factory = newFactory(FACTORY_DEFAULT, LogFactory.class.getClassLoader());
        }

        if( props!=null ) {
            Enumeration names = props.propertyNames();
            while (names.hasMoreElements()) {
                String name = (String) names.nextElement();
                String value = props.getProperty(name);
                factory.setAttribute(name, value);
            }
        }
        
        return factory;
    }


    /**
     * Convenience method to return a named logger, without the application
     * having to care about factories.
     *
     * @param clazz Class for which a log name will be derived
     *
     * @exception LogConfigurationException if a suitable <code>Log</code>
     *  instance cannot be returned
     */
    public static Log getLog(Class clazz)
        throws LogConfigurationException {

        return (getFactory().getInstance(clazz));

    }


    /**
     * Convenience method to return a named logger, without the application
     * having to care about factories.
     *
     * @param name Logical name of the <code>Log</code> instance to be
     *  returned (the meaning of this name is only known to the underlying
     *  logging implementation that is being wrapped)
     *
     * @exception LogConfigurationException if a suitable <code>Log</code>
     *  instance cannot be returned
     */
    public static Log getLog(String name)
        throws LogConfigurationException {

        return (getFactory().getInstance(name));

    }


    /**
     * Release any internal references to previously created {@link LogFactory}
     * instances, after calling the instance method <code>release()</code> on
     * each of them.  This is useful environments like servlet containers,
     * which implement application reloading by throwing away a ClassLoader.
     * Dangling references to objects in that class loader would prevent
     * garbage collection.
     */
    public static void releaseAll() {

        synchronized (factories) {
            Enumeration elements = factories.elements();
            while (elements.hasMoreElements()) {
                LogFactory element = (LogFactory) elements.nextElement();
                element.release();
            }
            factories.clear();
        }

    }


    // ------------------------------------------------------ Protected Methods


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
    protected static ClassLoader getContextClassLoader()
        throws LogConfigurationException
    {
        ClassLoader classLoader = null;

        try {
            // Are we running on a JDK 1.2 or later system?
            Method method = Thread.class.getMethod("getContextClassLoader", null);

            // Get the thread context class loader (if there is one)
            try {
                classLoader = (ClassLoader)method.invoke(Thread.currentThread(), null);
            } catch (IllegalAccessException e) {
                throw new LogConfigurationException
                    ("Unexpected IllegalAccessException", e);
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
            classLoader = LogFactory.class.getClassLoader();
        }

        // Return the selected class loader
        return classLoader;
    }

    /**
     * Check cached factories (keyed by classLoader)
     */
    private static LogFactory getCachedFactory(ClassLoader contextClassLoader)
    {
        LogFactory factory = null;
        
        if (contextClassLoader != null)
            factory = (LogFactory) factories.get(contextClassLoader);
            
        if (factory==null)
            factory = (LogFactory) factories.get(LogFactory.class.getClassLoader());
        
        return factory;
    }
    
    private static void cacheFactory(ClassLoader classLoader, LogFactory factory)
    {
        if (classLoader != null && factory != null)
            factories.put(classLoader, factory);
    }

    /**
     * Return a new instance of the specified <code>LogFactory</code>
     * implementation class, loaded by the specified class loader.
     * If that fails, try the class loader used to load this
     * (abstract) LogFactory.
     *
     * @param factoryClass Fully qualified name of the <code>LogFactory</code>
     *  implementation class
     * @param classLoader ClassLoader from which to load this class
     *
     * @exception LogConfigurationException if a suitable instance
     *  cannot be created
     */
    protected static LogFactory newFactory(String factoryClass,
                                           ClassLoader classLoader)
        throws LogConfigurationException
    {
        
        try {
            if (classLoader == null)
                classLoader = LogFactory.class.getClassLoader();

            Class clazz = null;
            try {
                // first the thread class loader
                clazz = classLoader.loadClass(factoryClass);
            } catch (ClassNotFoundException ex) {
                // if this failed (i.e. no implementation is
                // found in the webapp), try the caller's loader
                // if we haven't already...
                if (classLoader != LogFactory.class.getClassLoader()) {
                    classLoader = LogFactory.class.getClassLoader();
                    clazz = classLoader.loadClass(factoryClass);
                }
            }
            
            LogFactory factory = (LogFactory)clazz.newInstance();
            
            // Cache using correct classLoader
            cacheFactory(classLoader, factory);
            
            return factory;
        } catch (Exception e) {
            throw new LogConfigurationException(e);
        }

    }
}