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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.logging.LogFactory;


/**
 * This class is capable of receiving notifications about the undeployment of
 * a webapp, and responds by ensuring that commons-logging releases all
 * memory associated with the undeployed webapp.
 * <p>
 * This class is necessary when code using commons-logging is deployed within
 * a servlet container and commons-logging is deployed via a shared classloader.
 * When commons-logging is deployed in a "shared" classloader that is visible 
 * to all webapps, commons-logging still ensures that each webapp gets its own
 * logging configuration by setting up a separate LogFactory object for each 
 * context classloader, and ensuring that the correct LogFactory object is 
 * used based on the context classloader set whenever LogFactory.getLog is 
 * called. However the negative side of this is that LogFactory needs to keep 
 * a static map of LogFactory objects keyed by context classloader; when the 
 * webapp is "undeployed" this means there is still a reference to the 
 * undeployed classloader preventing the memory used by all its classes from 
 * being reclaimed.  
 * <p>
 * To use this class, configure the webapp deployment descriptor to call
 * this class on webapp undeploy; the contextDestroyed method will tell
 * LogFactory that the entry in its map for the current webapp's context
 * classloader should be cleared.
 */

public class ServletContextCleaner implements ServletContextListener {

    private Class[] RELEASE_SIGNATURE = {ClassLoader.class};
    
    /**
     * Invoked when a webapp is undeployed, this tells the LogFactory
     * class to release any logging information related to the current
     * contextClassloader.
     */
    public void contextDestroyed(ServletContextEvent sce) {
        ClassLoader tccl = Thread.currentThread().getContextClassLoader();

        Object[] params = new Object[1];
        params[0] = tccl;

        // Walk up the tree of classloaders, finding all the available
        // LogFactory classes and releasing any objects associated with
        // the tccl (ie the webapp).
        ClassLoader loader = tccl;
        while (loader != null) {
            // Load via the current loader. Note that if the class is not accessable
            // via this loader, but is accessable via some ancestor then that class
            // will be returned.
            try {
                Class logFactoryClass = loader.loadClass("org.apache.commons.logging.LogFactory");
                Method releaseMethod = logFactoryClass.getMethod("release", RELEASE_SIGNATURE);
                releaseMethod.invoke(null, params);
                loader = logFactoryClass.getClassLoader().getParent();
            } catch(ClassNotFoundException ex) {
                // Neither the current classloader nor any of its ancestors could find
                // the LogFactory class, so we can stop now.
                loader = null;
            } catch(NoSuchMethodException ex) {
                // This is not expected; every version of JCL has this method
                System.err.println("LogFactory instance found which does not support release method!");
                loader = null;
            } catch(IllegalAccessException ex) {
                // This is not expected; every ancestor class should be accessable
                System.err.println("LogFactory instance found which is not accessable!");
                loader = null;
            } catch(InvocationTargetException ex) {
                // This is not expected
                System.err.println("LogFactory instance release method failed!");
                loader = null;
            }
        }
        
        // Just to be sure, invoke release on the LogFactory that is visible from
        // this ServletContextCleaner class too. This should already have been caught
        // by the above loop but just in case...
        LogFactory.release(tccl);
    }
    
    /**
     * Invoked when a webapp is deployed. Nothing needs to be done here.
     */
    public void contextInitialized(ServletContextEvent sce) {
        // do nothing
    }
}
