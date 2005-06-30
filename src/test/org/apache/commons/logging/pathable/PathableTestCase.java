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
package org.apache.commons.logging.pathable;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.commons.logging.PathableTestSuite;
import org.apache.commons.logging.PathableClassLoader;

/**
 * Tests for the PathableTestSuite and PathableClassLoader functionality.
 * <p>
 * These tests assume:
 * <ul>
 * <li>junit is in system classpath
 * <li>nothing else is in system classpath
 * </ul>
 */

public class PathableTestCase extends TestCase {
    public static Test suite() throws Exception {
        // make the parent a direct child of the bootloader to hide all
        // other classes in the system classpath
        PathableClassLoader parent = new PathableClassLoader(null);
        
        // make the junit classes from the system classpath visible, though,
        // as junit won't be able to call this class at all without this..
        parent.useSystemLoader("junit.");
        
        // make the commons-logging-api.jar classes visible via the parent
        parent.addLogicalLib("commons-logging-api");
        
        // create a child classloader to load the test case through
        PathableClassLoader child = new PathableClassLoader(parent);
        
        // obviously, the child classloader needs to have the test classes
        // in its path!
        child.addLogicalLib("testclasses");
        
        // create a third classloader to be the context classloader.
        PathableClassLoader context = new PathableClassLoader(child);

        // reload this class via the child classloader
        Class testClass = child.loadClass(PathableTestCase.class.getName());
        
        // and return our custom TestSuite class
        return new PathableTestSuite(testClass, context);
    }
    
    public void testPaths() throws Exception {
        // the context classloader is not expected to be null
        ClassLoader contextLoader = Thread.currentThread().getContextClassLoader();
        assertNotNull("Context classloader is null", contextLoader);
        assertEquals("Context classloader has unexpected type",
                PathableClassLoader.class.getName(),
                contextLoader.getClass().getName());
        
        // the classloader that loaded this class is obviously not null
        ClassLoader thisLoader = this.getClass().getClassLoader();
        assertNotNull("thisLoader is null", thisLoader);
        assertEquals("thisLoader has unexpected type",
                PathableClassLoader.class.getName(),
                thisLoader.getClass().getName());
        
        // the suite method specified that the context classloader's parent
        // is the loader that loaded this test case.
        assertSame("Context classloader is not child of thisLoader",
                thisLoader, contextLoader.getParent());

        // thisLoader's parent should be available
        ClassLoader parentLoader = thisLoader.getParent();
        assertNotNull("Parent classloader is null", parentLoader);
        assertEquals("Parent classloader has unexpected type",
                PathableClassLoader.class.getName(),
                parentLoader.getClass().getName());
        
        // parent should have a parent of null
        assertNull("Parent classloader has non-null parent", parentLoader.getParent());

        // getSystemClassloader is not a PathableClassLoader; it's of a
        // built-in type. This also verifies that system classloader is none of
        // (context, child, parent).
        ClassLoader systemLoader = ClassLoader.getSystemClassLoader();
        assertNotNull("System classloader is null", systemLoader);
        assertFalse("System classloader has unexpected type",
                PathableClassLoader.class.getName().equals(
                        systemLoader.getClass().getName()));

        // junit classes should be visible; their classloader is system.
        // this will of course throw an exception if not found.
        Class junitTest = contextLoader.loadClass("junit.framework.Test");
        assertSame("Junit not loaded via systemloader",
                systemLoader, junitTest.getClassLoader());

        // jcl api classes should be visible via the parent
        Class logClass = contextLoader.loadClass("org.apache.commons.logging.Log");
        assertSame("Log class not loaded via parent",
                logClass.getClassLoader(), parentLoader);

        // jcl non-api classes should not be visible
        try {
            Class log4jClass = contextLoader.loadClass(
                "org.apache.commons.logging.impl.Log4J12Logger");
            fail("Class Log4J12Logger is unexpectedly available");
        } catch(ClassNotFoundException ex) {
            // ok
        }

        // String class classloader is null
        Class stringClass = contextLoader.loadClass("java.lang.String");
        assertNull("String class classloader is not null!",
                stringClass.getClassLoader());
    }
}
