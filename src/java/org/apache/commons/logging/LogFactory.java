/*
 * $Header: /home/jerenkrantz/tmp/commons/commons-convert/cvs/home/cvs/jakarta-commons//logging/src/java/org/apache/commons/logging/LogFactory.java,v 1.1 2002/02/13 02:18:11 craigmcc Exp $
 * $Revision: 1.1 $
 * $Date: 2002/02/13 02:18:11 $
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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.Properties;


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
 * @version $Revision: 1.1 $ $Date: 2002/02/13 02:18:11 $
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
     * <p>Construct (if necessary) and return a <code>Log</code> instance,
     * using the factory's current set of configuration attributes.</p>
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


    // --------------------------------------------------------- Static Methods


    /**
     * <p>Construct and return a new <code>LogFactory</code> instance, using
     * the following ordered lookup procedure to determine the name of the
     * implementation class to be loaded:</p>
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
    public static LogFactory newFactory() throws LogConfigurationException {

        // Identify the class loader we will be using
        ClassLoader classLoader = findClassLoader();

        // First, try the system property
        try {
            String factoryClass = System.getProperty(FACTORY_PROPERTY);
            if (factoryClass != null) {
                return (newInstance(factoryClass, classLoader));
            }
        } catch (SecurityException e) {
            ;
        }

        // Second, try a properties file
        try {
            InputStream stream =
                classLoader.getResourceAsStream(FACTORY_PROPERTIES);
            if (stream != null) {
                Properties props = new Properties();
                props.load(stream);
                stream.close();
                String factoryClass = props.getProperty(FACTORY_PROPERTY);
                if (factoryClass != null) {
                    LogFactory instance =
                        newInstance(factoryClass, classLoader);
                    Enumeration names = props.propertyNames();
                    while (names.hasMoreElements()) {
                        String name = (String) names.nextElement();
                        String value = props.getProperty(name);
                        instance.setAttribute(name, value);
                    }
                    return (instance);
                }
            }
        } catch (IOException e) {
        } catch (SecurityException e) {
        }

        // Third, try the fallback implementation class
        return (newInstance(FACTORY_DEFAULT, classLoader));

    }


    // ------------------------------------------------------ Protected Methods


    /**
     * Return the class loader to be used for loading the selected
     * <code>LogFactory</code> implementation class.  On a JDK 1.2 or later
     * system, the context class loader for the current thread will be used
     * if it is present.
     *
     * @exception LogConfigurationException if a suitable class loader
     *  cannot be identified
     */
    protected static ClassLoader findClassLoader()
        throws LogConfigurationException {

        // Are we running on a JDK 1.2 or later system?
        Method method = null;
        try {
            method = Thread.class.getMethod("getContextClassLoader", null);
        } catch (NoSuchMethodException e) {
            // Assume we are running on JDK 1.1
            return (LogFactory.class.getClassLoader());
        }

        // Get the thread context class loader (if there is one)
        ClassLoader classLoader = null;
        try {
            classLoader = (ClassLoader)
                method.invoke(Thread.currentThread(), null);
            if (classLoader == null) {
                classLoader = LogFactory.class.getClassLoader();
            }
        } catch (IllegalAccessException e) {
            throw new LogConfigurationException
                ("Unexpected IllegalAccessException", e);
        } catch (InvocationTargetException e) {
            throw new LogConfigurationException
                ("Unexpected InvocationTargetException", e);
        }

        // Return the selected class loader
        return (classLoader);

    }


    /**
     * Return a new instance of the specified <code>LogFactory</code>
     * implementation class, loaded by the specified class loader.
     *
     * @param factoryClass Fully qualified name of the <code>LogFactory</code>
     *  implementation class
     * @param classLoader ClassLoader from which to load this class
     *
     * @exception LogConfigurationException if a suitable instance
     *  cannot be created
     */
    private static LogFactory newInstance(String factoryClass,
                                          ClassLoader classLoader)
        throws LogConfigurationException {

        try {
            Class clazz = null;
            if (classLoader == null) {
                clazz = Class.forName(factoryClass);
            } else {
                clazz = classLoader.loadClass(factoryClass);
            }
            return ((LogFactory) clazz.newInstance());
        } catch (Exception e) {
            throw new LogConfigurationException(e);
        }

    }


}
