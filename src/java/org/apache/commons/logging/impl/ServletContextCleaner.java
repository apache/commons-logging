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

import javax.servlet.ServletContextListener;
import javax.servlet.ServletContextEvent;

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
    
    /**
     * Invoked when a webapp is undeployed, this tells the LogFactory
     * class to release any logging information related to the current
     * contextClassloader.
     */
    public void contextDestroyed(ServletContextEvent sce) {
        LogFactory.release(Thread.currentThread().getContextClassLoader());
    }
    
    /**
     * Invoked when a webapp is deployed. Nothing needs to be done here.
     */
    public void contextInitialized(ServletContextEvent sce) {
        // do nothing
    }
}
