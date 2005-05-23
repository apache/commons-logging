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


import java.util.Hashtable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.Factory;

/**
 * Concrete subclass of {@link Factory} specific to java.util.logging.
 */
public final class Jdk14Factory extends Factory {

    public Jdk14Factory() {
        super();
    }

    // Previously returned instances, to avoid creation of proxies
    private Hashtable instances = new Hashtable();

    // --------------------------------------------------------- Public Methods

    /**
     * Return a logger associated with the specified category name.
     */
    public Log getLog(String name) {
        Log instance = (Log) instances.get(name);
        if (instance != null)
            return instance;

        instance = new Jdk14Logger(name);
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