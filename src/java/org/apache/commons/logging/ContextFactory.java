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

package org.apache.commons.logging;


import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.lang.reflect.Method;

/**
 * <p>Concrete subclass of {@link Factory} that attempts to load a
 * concrete Factory implementation via the current context classloader.
 * </p>
 * <p>
 * Unlike other XXXFactory classes, it does not manage a set of loggers; 
 * instead it keeps a map of LogFactory objects keyed by context classloader
 * and simply forwards calls on to whichever entry matches the current
 * context classloader. When a new context classloader is seen, it tries
 * to load the class LogFactory via that classloader rather than via the
 * classloader that loaded this class.
 * </p>
 * <p>
 * In a J2EE/ServletEngine type environment, deploying this class in a shared
 * classloader makes it possible for code deployed in the shared classloader
 * to log via a logging library deployed in the child webapp. In particular,
 * this means that code in the shared classloader is invoked by code in the
 * webapp and then logs data, it goes to the same destination as log calls
 * made by code in the webapp.
 * </p>
 * <p>
 * This class requires java 1.2 or later.
 * </p>
 */

public class ContextFactory extends Factory {

    private Factory defaultFactory;
    private HashMap factories = new HashMap();
    
    // ----------------------------------------------------------- Constructors

    /**
     * Constructor.
     */
    public ContextFactory(Factory defaultFactory) {
        super();
        this.defaultFactory = defaultFactory;
    }

    // --------------------------------------------------------- Public Methods

    public Log getLog(String name) {
        ClassLoader context = getContextClassLoader();
        
        // If the context classloader is the same as the classloader for the
        // default factory, then just use the default factory. This means that
        // when code in a child binds to a local LogFactory, then we get a 
        // pretty quick path to the real factory even when the global system
        // property is set.
        if (context == defaultFactory.getClass().getClassLoader()) {
            return defaultFactory.getLog(name);
        }
        
        // ok, here we really need to fetch a factory out of the map.
        Factory factory = (Factory) factories.get(context);
        if (factory == null) {
            factory = newFactory(context);
            factories.put(context, factory);
        }
        
        return factory.getLog(name);
    }

    public void release(ClassLoader loader) {
        Factory factory = (Factory) factories.get(loader);
        if (factory != null) {
            factory.release(loader);
            factories.remove(loader);
        }
    }

    public void releaseAll() {
        for(Iterator i = factories.entrySet().iterator(); i.hasNext(); ) {
            Map.Entry entry = (Map.Entry) i.next();
            Factory factory = (Factory) entry.getValue();
            factory.releaseAll();
        }
        factories.clear();
    }


    private static ClassLoader getContextClassLoader() {
        return Thread.currentThread().getContextClassLoader();
    }
        
    private Factory newFactory(ClassLoader cl) {
        try {
            Class c = cl.loadClass("org.apache.commons.logging.LogFactory");
            
            // dynamically invoke the "getFactory" method on it
            Method method = c.getMethod("getInstance", (Class[]) null);
            Object o = method.invoke(null, (Object[]) null);
            
            if (!Factory.class.isAssignableFrom(o.getClass())) {
                // oops, the child probably has its own copy of the Factory
                // class. Output a diagnostic and fall back to a local logger.
                System.out.println("Oops - Factory not assignable from loaded class");
                throw new Exception("Factory in child.");
            }

            if (o == this) {
                System.out.println("Oops - cyclic");
                // oops, the child didn't have a LogFactory class at all,
                // or the child has parent-first selected. In either case,
                // the LogFactory lookup returned the object that owns this
                // object - so let's just drop back to the default!
                throw new Exception("No LogFactory in child");
            }
            
            // ok, we found a new and workable Factory via the child.
            return (Factory) o;
        } catch(Throwable t) {
            return defaultFactory;
        }
    }
}
