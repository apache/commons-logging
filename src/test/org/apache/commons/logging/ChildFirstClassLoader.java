/* $Id$
*
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

import java.net.URL;
import java.net.URLClassLoader;

/**
 * (Rather slack) implementation of a child first classloader.
 * Should be fit for the purpose intended (which is demonstration)
 * but a more complete and robust implementation should be
 * preferred for more general purposes.
 */
public class ChildFirstClassLoader extends ModifiableClassLoader {

    public ChildFirstClassLoader(ClassLoader parent) {
        super(parent);
    }

    protected synchronized Class loadClass(String name, boolean resolve)
            throws ClassNotFoundException {
        
        // very basic implementation
        Class result = findLoadedClass(name);
        if (result == null) {
            try {
                result = findClass(name);
                if (resolve) {
                    resolveClass(result);
                }
            } catch (ClassNotFoundException e) {
                result = super.loadClass(name, resolve);
            }
        } 
        
        return result;
    }

}
