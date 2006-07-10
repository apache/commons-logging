/*
 * Copyright 2006 The Apache Software Foundation.
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
 
package org.apache.commons.logging.security;

import java.io.FilePermission;
import java.security.Permission;
import java.util.PropertyPermission;


/**
 * Custom implementation of a security manager, so we can control the
 * security environment for tests in this package.
 * <p>
 * Note that we don't want to refuse permission to any junit method; otherwise
 * any call to an assert will not be able to output its data!  
 */
public class MockSecurityManager extends SecurityManager {
    public void checkPermission(Permission p) throws SecurityException {
        // System.out.println("\n\ntesting permission:" + p.getClass() + ":"+ p);
        
        // allow read-only access to files, as this is needed to load classes!
        if (p instanceof FilePermission) {
            FilePermission fp = (FilePermission) p;
            if (fp.getActions().equals("read")) {
                return;
            }
        }

        Exception e = new Exception();
        e.fillInStackTrace();
        StackTraceElement[] stack = e.getStackTrace();
        
        boolean isControlled = false;
        // start at 1 to skip the entry in the stack for this method
        for(int i=1; i<stack.length; ++i) {
            String mname = stack[i].getMethodName();
            if (mname.equals("setSecurityManager")) {
                // ok, allow this; we don't want to block any calls to setSecurityManager
                // otherwise this custom security manager cannot be reset to the original
                // one...
                // System.out.println("Allow setSecurityManager");
                return;
            }

            String cname = stack[i].getClassName();
            //System.out.println("" + i + ":" + stack[i].getClassName() + 
            //  "." + stack[i].getMethodName());
            if (cname.startsWith("org.apache.commons.logging")) {
                isControlled = true;
                break;
            }
        }
        
        if (!isControlled) {
            // we have scanned the entire stack, and found no logging classes, so
            // this must have been called from junit
            // System.out.println("Not relevant to test; returning success");
            return;
        }
        
        if (p instanceof PropertyPermission) {
            // emulate an applet environment where system properties are not accessable
            throw new SecurityException(
               "Permission refused to access property:" 
                    + ((PropertyPermission)p).getName());
        }

        // emulate an environment where *everything* is refused
        throw new SecurityException("Permission refused:" + p.getClass() + ":" + p);
    }
}
