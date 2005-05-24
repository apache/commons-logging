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

package org.apache.commons.logging;

/**
 * The base class that all logging adapter Factory classes extend.
 */

public abstract class Factory {
    public abstract Log getLog(String name);
    public abstract void release(ClassLoader classLoader);
    public abstract void releaseAll();
    
    static {
        // here, we check whether the parent classloader can also load this
        // class (ie whether any ancestor can load this class assuming a sane
        // parent classloader). If this is true, then we emit a warning message
        // as we are almost certainly running in a container environment and
        // the user has screwed up the deployment.
        Class thisClass = Factory.class;
        ClassLoader cl = thisClass.getClassLoader();
        System.out.println("Factory-classloader=" + cl);
        if (cl != null) {
            cl = cl.getParent();
            try {
                System.out.println("Looking in classloader" + cl);
                Class.forName(thisClass.getName(), false, cl);
                System.err.println(
                        "DEPLOYMENT ERROR: Multiple copies of the JCL spi classes"
                        + " are in the classpath!");
            } catch(ClassNotFoundException ex) {
                // ok, this is fine
            }
        }
    }
}
