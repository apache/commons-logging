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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.Factory;
import java.io.PrintStream;

/**
 * A Factory that is intended to be used in unit tests. 
 * <p>
 * This factory allows the output to be directed to an explicit
 * PrintStream object, so that the caller can intercept the messages
 * logged. Because this facility allows code in a child classloader to
 * modify configuration on a Factory object residing in a shared classloader,
 * it is not safe for use in environments where code in child classloaders
 * is not trusted; that's why this is called TestFactory and not
 * RedirectablyFactory or somesuch name.
 */
public class TestFactory extends Factory {
    private PrintStream stream = System.out;
    private Log logger = null;
    
    public TestFactory() {
        super();
    }

    // --------------------------------------------------------- Public Methods

    /**
     * Return a logger associated with the specified category name.
     */
    public Log getLog(String name) {
        if (logger == null) {
            // we only construct logger on demand so that users have a
            // chance to call setStream before the logger is created.
            logger = new TestLogger(stream);
        }
        return logger;
    }

    public void release(ClassLoader cl) {
    }
    
    public void releaseAll() {
    }

    /**
     * This method is not part of the Factory interface; in order to 
     * access this method, call LogFactory.getFactory(), then downcast
     * to TestFactory.
     */
    public void setStream(PrintStream stream) {
        this.stream = stream;
    }
}
