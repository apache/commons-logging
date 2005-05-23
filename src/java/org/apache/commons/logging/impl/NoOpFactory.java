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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.Factory;

/**
 * Concrete subclass of {@link Factory} specific to NoOpLogger.
 */
public final class NoOpFactory extends Factory {

    private static Log logger = new NoOpLogger();
    
    public NoOpFactory() {
        super();
    }

    // --------------------------------------------------------- Public Methods

    /**
     * Return a logger associated with the specified category name.
     */
    public Log getLog(String name) {
        return logger;
    }

    public void release(ClassLoader cl) {
    }
    
    public void releaseAll() {
    }
}