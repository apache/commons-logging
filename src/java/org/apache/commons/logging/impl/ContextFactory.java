/*
 * Copyright 2005 The Apache Software Foundation.
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


import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import java.lang.reflect.Method;
import java.net.URL;
import java.io.InputStream;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.Factory;
import org.apache.commons.logging.LogConfigurationException;

/**
 * Concrete subclass of {@link Factory} that attempts to load a
 * concrete Factory implementation via the current context classloader.
 * <p>
 * Unlike other XXXFactory classes, it does not manage a set of loggers; 
 * instead it keeps a map of Factory objects keyed by context classloader
 * and simply forwards calls on to whichever entry matches the current
 * context classloader. When a new context classloader is seen, it tries
 * to load the class LogFactory via that classloader rather than via the
 * classloader that loaded this class.
 * <p>
 * In a J2EE/ServletEngine type environment, deploying this class in a shared
 * classloader makes it possible for code deployed in the shared classloader
 * to log via a logging library deployed in the child webapp. In particular,
 * this means that code in the shared classloader is invoked by code in the
 * webapp and then logs data, it goes to the same destination as log calls
 * made by code in the webapp.
 * <p>
 * Note that because this class just delegates to another factory, there is
 * no corresponding ContextLogger class.
 * <p>
 * This class requires java 1.2 or later.
 * <p>
 * TODO: Think about providing backwards compatibility so that webapps that
 * deploy the old JCL 1.0 in their child classpath will continue to run when
 * the container uses this class. 
 */

public class ContextFactory extends Factory {

    /**
     * The system property that can be used to specify what the default 
     * factory class should be for callers whose context classloader does
     * not have a custom config file.
     * <p>
     * Using system properties is not really in the style of a static binding
     * approach. However the alternatives is to distribute a set of jars
     * containing ContextFactory statically bound to various concrete
     * libraries in the same manner as LogFactory is. That seems feasable,
     * but rather confusing. Assuming N logging implementations, there would
     * then be N jars containing variants of LogFactory, and another N jars
     * containing variants of ContextFactory. And unlike LogFactory, there
     * only ever needs to be one instance of the ContextFactory class in
     * the entire classpath so using system properties to configure its
     * associated default factory class is feasable; the fact that system
     * properties are global rather than per-webapp is not an issue.
     */
    private static final String DEFAULT_FACTORY_PROPERTY = 
        "org.apache.commons.logging.context.defaultFactory"; 
    
    /**
     * The name of a resource file that will be looked for within the
     * context classloader's path (and *not* the path of any ancestor
     * classloaders). If found, and this properties file contains a
     * property FACTORY_PROPERTY then that factory class shall be used
     * to generate loggers for that context classloader. 
     */
    private static final String CONFIG_FILE_NAME = "commons-logging.properties";

    /**
     * If the context classloader contains a properties file with this property
     * set then the value is expected to be a Factory subclass which will
     * be used to generate loggers for that context classloader.
     * <p>
     * Note that this is the key to a per-classloader properties file, not
     * a system property key.
     */
    public static final String FACTORY_PROPERTY =
        "org.apache.commons.logging.context.Factory";

    /**
     * When getFactory is called for a context classloader which does not
     * have a custom config file, a factory object of this class will be
     * constructed to manage the Logger objects associated with that
     * classloader. See DEFAULT_FACTORY_PROPERTY.
     */
    private Class defaultFactoryClass;
    
    /**
     * A mapping of context classloaders to the associated Factory object
     * that manages its loggers. Note that when the context classloader is
     * no longer in use, the release method needs to be called with that
     * context classloader as parameter in order to clear the associated
     * entry here. Failure to do this will result in the memory associated
     * with that classloader not being recycled (and that can be quite a
     * lot of memory in some cases).
     */
    private HashMap factories = new HashMap();
    
    // ----------------------------------------------------------- Constructors

    /**
     * Constructor.
     */
    public ContextFactory() throws LogConfigurationException {
        super();
        
        String defaultFactoryClassName = System.getProperty(DEFAULT_FACTORY_PROPERTY);
        if (defaultFactoryClassName == null) {
            // Best to make this a mandatory parameter. Without this, we
            // would need to hard-wire some default class, and that makes
            // things more complex. When someone deploys the ContextFactory,
            // they just need to set this property too - pretty simple.
            throw new LogConfigurationException(
                "Commons-logging: Mandatory system property "
                + DEFAULT_FACTORY_PROPERTY + " has not been set.");
        }
        
        try {
            this.defaultFactoryClass = 
                ContextFactory.class.getClassLoader().
                    loadClass(defaultFactoryClassName);
        
            // Verify that the class can correctly load now, so we get failures
            // as early as possible. This also checks that the specified class
            // does actually implement the Factory interface. 
            Factory f = (Factory) defaultFactoryClass.newInstance();
        } catch(Exception ex) {
            throw new LogConfigurationException(ex);
        }
    }

    // --------------------------------------------------------- Public Methods

    /**
     * Fetch a logger using a factory that depends upon the current
     * context classloader. 
     */
    public Log getLog(String name) {
        ClassLoader context = getContextClassLoader();
        
        // ok, here we really need to fetch a factory out of the map.
        Factory factory = (Factory) factories.get(context);
        if (factory == null) {
            factory = newFactory(context);
            factories.put(context, factory);
        }
        
        return factory.getLog(name);
    }

    /**
     * Release all logging resources associated with the specified
     * classloader. This should be called, for example, when a webapp
     * within a servlet container is unloaded.
     */
    public void release(ClassLoader loader) {
        Factory factory = (Factory) factories.get(loader);
        if (factory != null) {
            factory.release(loader);
            factories.remove(loader);
        }
    }

    /**
     * Release all logging resources associated with all classloaders.
     * This is a very dangerous thing to do!
     * <p>
     * TODO: think about whether this is actually a good thing to support.
     * I have struck a few people who have been calling this; boy will
     * they be surprised when their webapp is run in a container with
     * other webapps! Only the container itself should really need to
     * call this, and it should be able to call release(loader) for every
     * webapp to achieve the same effect.
     */
    public void releaseAll() {
        for(Iterator i = factories.entrySet().iterator(); i.hasNext(); ) {
            Map.Entry entry = (Map.Entry) i.next();
            Factory factory = (Factory) entry.getValue();
            factory.releaseAll();
        }
        factories.clear();
    }


    /**
     * Get the context classloader associated with the current thread.
     * <p>
     * This should really be done under the control of an AccessController
     * so that this class can be signed and access the context classloader
     * even when the calling code cannot.
     */
    private static ClassLoader getContextClassLoader() {
        return Thread.currentThread().getContextClassLoader();
    }
        
    /**
     * Return a new Factory subclass to handle logging for the specified
     * classloader. 
     * <p>
     * If the specified classloader contains a custom logging config file,
     * then the returned factory will be of the class specified in that
     * file. 
     * <p>
     * Otherwise if the specified classloader can locate a class of
     * name LogFactory and that class's getFactory method does not return
     * this object(!) then that object is returned. The check for this
     * object is very important, because child classloaders that use
     * parent-first or which don't deploy their own LogFactory class will
     * get the one from the parent, which could well be the one that 
     * created this object. And failing to handle this will result in an
     * endless loop.
     * <p>
     * And otherwise the returned factory will be a new instance of class 
     * defaultFactoryClass.
     * <p>
     * Note that
     */
    private Factory newFactory(ClassLoader cl) {
        // Note that this method is only ever called once per classloader.
        // Performance is therefore not critical here; it only applies
        // when a new webapp or similar is first started within a container.
        
        // first, try properties
        Properties props = getCustomProperties(cl);
        if (props != null) {
            String factoryClassName = (String) props.get(FACTORY_PROPERTY);
            if (factoryClassName == null) {
                throw new LogConfigurationException(
                        "Commons-logging configuration file found"
                        + " but it does not contain key " + FACTORY_PROPERTY);
            }
        
            // load specified class
            // create new instance of it
            // return that instance
        }
        
        // next try fixed name LogFactory
        try {
            Class c = cl.loadClass("org.apache.commons.logging.LogFactory");
            
            Method method = c.getMethod("getFactory", (Class[]) null);
            Object o = method.invoke(null, (Object[]) null);
            
            if (!Factory.class.isAssignableFrom(o.getClass())) {
                // oops, the child probably has its own copy of the Factory
                // class. Output a diagnostic and fall back to a local logger.
                System.err.println("Oops - Factory not assignable from loaded class");
                throw new Exception("Factory in child.");
            }

            if (o == this) {
                // oops, the child didn't have a LogFactory class at all,
                // or the child has parent-first selected. In either case,
                // the LogFactory lookup returned the object that owns this
                // object - so let's just drop back to the default!
                return (Factory) defaultFactoryClass.newInstance();
            }
            
            // ok, we found a new and workable Factory via the child.
            return (Factory) o;
        } catch(Throwable t) {
            // hmm.. should handle errors more elegantly than ignoring them
            System.err.println("Unable to create Factory instance:" + t.getMessage().trim());
            
            try {
                return (Factory) defaultFactoryClass.newInstance();
            } catch(Exception ex) {
                throw new LogConfigurationException(ex);
            }
        }
    }

    /**
     * Look for a custom logging configuration file within the classpath
     * of the specified loader.
     * <p>
     * If the file is found but cannot be read, or is found but is not
     * a properties file, then null will be returned.
     * 
     * @return A Properties file if the logging configuration file is found.
     * Returns null if there is no such file within the specified loader's
     * classpath. Note that occurrences of the configuration file within
     * the path of ancestor classloaders is ignored.
     */
    private static Properties getCustomProperties(ClassLoader loader) {
        URL propsUrl = getClassLoaderResource(CONFIG_FILE_NAME, loader);
        if (propsUrl != null) {
            InputStream source = null;
            try {
                source = propsUrl.openStream();
                Properties p = new Properties();
                p.load(source);
                return p;
            } catch(IOException ex) {
                System.err.println(
                        "Unable to read config props " + propsUrl 
                        + ":" + ex.getMessage());
                return null;
            } finally {
                if (source != null) {
                    try {
                        source.close();
                    } catch(IOException ex) {
                        // ignore
                    }
                }
            }
        }
        
        return null;
        
    }

    /**
     * Return the specified resource via the specified classloader's path
     * even when the classloader has a parent with the same resource and
     * the child has a parent-first loading policy.
     * <p>
     * This is necessary to allow child classloaders to specify configuration
     * resources that override the parent even when the parent has a resource
     * with the same name.
     * <p>
     * Of course this wouldn't be necessary if all the fools who use
     * parent-first loading in containers were just shot.
     * 
     * @return a URL which references a resource with the specified name
     * that is in the specified classloader's path but <i>not</i> in
     * the path of any ancestor classloader. If no such resource is found,
     * then null is returned.
     */
    private static URL getClassLoaderResource(String resourceName, ClassLoader loader) {
        if (loader == null) {
            // alas, while null represents a real classloader (the boot
            // classloader, or the system classloader on JDk1.1) we can't
            // call null.getResource().
            //
            // And while method Class.forName("foo", false, null) can be used
            // to lookup classes on the null classloader, there is no 
            // equivalent to look up resources.
            //
            // However Class does have getResource(name), so we just need to
            // locate a Class object which has been loaded via the boot
            // classloader.
            return Object.class.getResource(resourceName);
        }

        ClassLoader parent = loader.getParent();
        if (parent == null) {
            // Unfortunately in another java library inconsistency, there
            // is no way to call getResources on the null classloader.
            //
            // However when we have an environment that supports multiple 
            // classloaders we can probably safely assume that the bootloader
            // only has the real JDK classes in it, ie won't have any resource
            // files available at all (and certainly not ones this class would
            // be interested in locating). So any resource located via the
            // specified loaded must be from its own classpath...
            return loader.getResource(resourceName);
        }

        try {
            Enumeration parentEnum = parent.getResources(resourceName);
            Enumeration childEnum = loader.getResources(resourceName);
            
            ArrayList parentItems = new ArrayList();
            while (parentEnum.hasMoreElements()) {
                parentItems.add(parentEnum.nextElement());
            }
            
            while (childEnum.hasMoreElements()) {
                URL url = (URL) childEnum.nextElement();
                if (!parentItems.contains(url)) {
                    // ok, we've found one accessable via the child that
                    // is not accessable via the parent, so this is
                    // unique to the child, regardless of whether
                    // the child uses child-first or parent-first
                    // resource lookup order.
                    return url;
                }
            }
        } catch(java.io.IOException ex) {
            System.out.println("IOException while looking for resource");
            return null;
        }
        
        return null;
    }
}
