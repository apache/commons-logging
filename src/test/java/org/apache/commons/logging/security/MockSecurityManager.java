/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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
import java.security.Permissions;

/**
 * Custom implementation of a security manager, so we can control the
 * security environment for tests in this package.
 */
public class MockSecurityManager extends SecurityManager {

    private static final Permission setSecurityManagerPerm =
        new RuntimePermission("setSecurityManager");
    private final Permissions permissions = new Permissions();

    private int untrustedCodeCount;

    public MockSecurityManager() {
        permissions.add(setSecurityManagerPerm);
    }

    /**
     * Define the set of permissions to be granted to classes in the o.a.c.l package,
     * but NOT to unit-test classes in o.a.c.l.security package.
     */
    public void addPermission(final Permission p) {
        permissions.add(p);
    }

    @Override
    public void checkPermission(final Permission p) throws SecurityException {
        if (setSecurityManagerPerm.implies(p)) {
            // ok, allow this; we don't want to block any calls to setSecurityManager
            // otherwise this custom security manager cannot be reset to the original.
            // System.out.println("setSecurityManager: granted");
            return;
        }

        // Allow read-only access to files, as this is needed to load classes!
        // Ideally, we would limit this to just .class and .jar files.
        if (p instanceof FilePermission) {
          final FilePermission fp = (FilePermission) p;
          if (fp.getActions().equals("read")) {
            // System.out.println("Permit read of files");
            return;
          }
        }

        System.out.println("\n\ntesting permission:" + p.getClass() + ":"+ p);

        final Exception e = new Exception();
        e.fillInStackTrace();
        final StackTraceElement[] stack = e.getStackTrace();

        // scan the call stack from most recent to oldest.
        // start at 1 to skip the entry in the stack for this method
        for(int i=1; i<stack.length; ++i) {
            final String cname = stack[i].getClassName();
            System.out.println("" + i + ":" + stack[i].getClassName() +
              "." + stack[i].getMethodName() + ":" + stack[i].getLineNumber());

            if (cname.equals("java.util.logging.Handler") && stack[i].getMethodName().equals("setLevel")) {
                // LOGGING CODE CAUSES ACCESSCONTROLEXCEPTION
                // http://www-01.ibm.com/support/docview.wss?uid=swg1IZ51152
                return;
            }

            if (cname.equals("java.util.logging.Level") && stack[i].getMethodName().equals("getLocalizedLevelName")) {
                // LOGGING-156: OpenJDK 1.7 JULI code (java.util.logging.Level#getLocalizedLevelName)
                // calls ResourceBundle#getBundle() without using AccessController#doPrivileged()
                // requiring RuntimePermission: "accessClassInPackage.sun.util.logging.resources"
                return;
            }

            if (cname.equals("java.security.AccessController")) {
                // Presumably method name equals "doPrivileged"
                //
                // The previous iteration of this loop verified that the
                // PrivilegedAction.run method associated with this
                // doPrivileged method call had the right permissions,
                // so we just return here. Effectively, the method invoking
                // doPrivileged asserted that it checked the input params
                // and found them safe, and that code is trusted, so we
                // don't need to check the trust level of code higher in
                // the call stack.
                System.out.println("Access controller found: returning");
                return;
            }
            if (cname.startsWith("java.")
                || cname.startsWith("javax.")
                || cname.startsWith("junit.")
                || cname.startsWith("org.apache.tools.ant.")
                || cname.startsWith("sun.")) {
                // Code in these packages is trusted if the caller is trusted.
                //
                // TODO: maybe check class is loaded via system loader or similar rather
                // than checking name? Trusted domains may be different in alternative
                // jvms..
            } else if (cname.startsWith("org.apache.commons.logging.security")) {
                // this is the unit test code; treat this like an untrusted client
                // app that is using JCL
                ++untrustedCodeCount;
                System.out.println("Untrusted code [test] found");
                throw new SecurityException("Untrusted code [test] found");
            } else if (cname.startsWith("org.apache.commons.logging.")) {
                if (!permissions.implies(p)) {
                    System.out.println("Permission refused:" + p.getClass() + ":" + p);
                    throw new SecurityException("Permission refused:" + p.getClass() + ":" + p);
                }
                // Code here is trusted if the caller is trusted
                System.out.println("Permission in allowed set for JCL class");
            } else {
                // we found some code that is not trusted to perform this operation.
                System.out.println("Unexpected code: permission refused:" + p.getClass() + ":" + p);
                throw new SecurityException("Unexpected code: permission refused:" + p.getClass() + ":" + p);
            }
        }
    }

    /**
     * This returns the number of times that a check of a permission failed
     * due to stack-walking tracing up into untrusted code. Any non-zero
     * value indicates a bug in JCL, that is, a situation where code was not
     * correctly wrapped in an AccessController block. The result of such a
     * bug is that signing JCL is not sufficient to allow JCL to perform
     * the operation; the caller would need to be signed too.
     */
    public int getUntrustedCodeCount() {
        return untrustedCodeCount;
    }
}
