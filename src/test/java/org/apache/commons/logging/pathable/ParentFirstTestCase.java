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
package org.apache.commons.logging.pathable;

import static org.junit.Assert.assertNotEquals;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestCase;

import org.apache.commons.logging.Artifacts;
import org.apache.commons.logging.PathableClassLoader;
import org.apache.commons.logging.PathableTestSuite;

/**
 * Tests for the PathableTestSuite and PathableClassLoader functionality,
 * where lookup order for the PathableClassLoader is parent-first.
 * <p>
 * These tests assume:
 * <ul>
 * <li>junit is in system classpath
 * <li>nothing else is in system classpath
 * </ul>
 */

public class ParentFirstTestCase extends TestCase {

    /**
     * Sets up a custom class loader hierarchy for this test case.
     * The hierarchy is:
     * <ul>
     * <li> contextloader: parent-first.
     * <li> childloader: parent-first, used to load test case.
     * <li> parentloader: parent-first, parent is the boot class loader.
     * </ul>
     */
    public static Test suite() throws Exception {
        final Class<ParentFirstTestCase> thisClass = ParentFirstTestCase.class;
        final ClassLoader thisClassLoader = thisClass.getClassLoader();

        // Make the parent a direct child of the bootloader to hide all
        // other classes in the system classpath
        final PathableClassLoader parent = new PathableClassLoader(null);

        // Make the junit classes visible as a special case, as junit
        // won't be able to call this class at all without this. The
        // junit classes must be visible from the class loader that loaded
        // this class, so use that as the source for future access to classes
        // from the junit package.
        parent.useExplicitLoader("junit.", thisClassLoader);
        parent.useExplicitLoader("org.junit.", thisClassLoader);

        // make the commons-logging.jar classes visible via the parent
        parent.addLogicalLib("commons-logging");

        // create a child class loader to load the test case through
        final PathableClassLoader child = new PathableClassLoader(parent);

        // obviously, the child class loader needs to have the test classes
        // in its path!
        child.addLogicalLib("testclasses");
        child.addLogicalLib("commons-logging-adapters");

        // create a third class loader to be the context class loader.
        final PathableClassLoader context = new PathableClassLoader(child);

        // reload this class via the child class loader
        final Class<?> testClass = child.loadClass(thisClass.getName());

        // and return our custom TestSuite class
        return new PathableTestSuite(testClass, context);
    }

    /**
     * Utility method to convert an enumeration-of-URLs into an array of URLs.
     */
    private static URL[] toURLArray(final Enumeration<URL> e) {
        final ArrayList<URL> l = new ArrayList<>();
        while (e.hasMoreElements()) {
            l.add(e.nextElement());
        }
        final URL[] tmp = new URL[l.size()];
        return l.toArray(tmp);
    }

    /**
     * Utility method to return the set of all class loaders in the
     * parent chain starting from the one that loaded the class for
     * this object instance.
     */
    private Set<ClassLoader> getAncestorCLs() {
        final Set<ClassLoader> s = new HashSet<>();
        ClassLoader cl = this.getClass().getClassLoader();
        while (cl != null) {
            s.add(cl);
            cl = cl.getParent();
        }
        return s;
    }

    /**
     * Test that the class loader hierarchy is as expected, and that
     * calling loadClass() on various class loaders works as expected.
     * Note that for this test case, parent-first classloading is
     * in effect.
     */
    public void testPaths() throws Exception {
        // the context class loader is not expected to be null
        final ClassLoader contextLoader = Thread.currentThread().getContextClassLoader();
        assertNotNull("Context class loader is null", contextLoader);
        assertEquals("Context class loader has unexpected type",
                PathableClassLoader.class.getName(),
                contextLoader.getClass().getName());

        // the class loader that loaded this class is obviously not null
        final ClassLoader thisLoader = this.getClass().getClassLoader();
        assertNotNull("thisLoader is null", thisLoader);
        assertEquals("thisLoader has unexpected type",
                PathableClassLoader.class.getName(),
                thisLoader.getClass().getName());

        // the suite method specified that the context class loader's parent
        // is the loader that loaded this test case.
        assertSame("Context class loader is not child of thisLoader",
                thisLoader, contextLoader.getParent());

        // thisLoader's parent should be available
        final ClassLoader parentLoader = thisLoader.getParent();
        assertNotNull("Parent class loader is null", parentLoader);
        assertEquals("Parent class loader has unexpected type",
                PathableClassLoader.class.getName(),
                parentLoader.getClass().getName());

        // parent should have a parent of null
        assertNull("Parent class loader has non-null parent", parentLoader.getParent());

        // getSystemClassloader is not a PathableClassLoader; it's of a
        // built-in type. This also verifies that system class loader is none of
        // (context, child, parent).
        final ClassLoader systemLoader = ClassLoader.getSystemClassLoader();
        assertNotNull("System class loader is null", systemLoader);
        assertNotEquals("System class loader has unexpected type", PathableClassLoader.class.getName(),
                systemLoader.getClass().getName());

        // junit classes should be visible; their class loader is not
        // in the hierarchy of parent class loaders for this class,
        // though it is accessible due to trickery in the PathableClassLoader.
        final Class<?> junitTest = contextLoader.loadClass("junit.framework.Test");
        final Set<?> ancestorCLs = getAncestorCLs();
        assertFalse("Junit not loaded by ancestor class loader",
                ancestorCLs.contains(junitTest.getClassLoader()));

        // jcl api classes should be visible only via the parent
        final Class<?> logClass = contextLoader.loadClass("org.apache.commons.logging.Log");
        assertSame("Log class not loaded via parent",
                logClass.getClassLoader(), parentLoader);

        // jcl adapter classes should be visible via both parent and child. However
        // as the class loaders are parent-first we should see the parent one.
        final Class<?> log4jClass = contextLoader.loadClass("org.apache.commons.logging.impl.Log4JLogger");
        assertSame("Log4JLogger not loaded via parent",
                log4jClass.getClassLoader(), parentLoader);

        // test classes should be visible via the child only
        final Class<?> testClass = contextLoader.loadClass("org.apache.commons.logging.PathableTestSuite");
        assertSame("PathableTestSuite not loaded via child",
                testClass.getClassLoader(), thisLoader);

        // test loading of class that is not available
        try {
            final Class<?> noSuchClass = contextLoader.loadClass("no.such.class");
            fail("Class no.such.class is unexpectedly available");
            assertNotNull(noSuchClass); // silence warning about unused var
        } catch (final ClassNotFoundException ex) {
            // ok
        }

        // String class class loader is null
        final Class<?> stringClass = contextLoader.loadClass("java.lang.String");
        assertNull("String class class loader is not null!",
                stringClass.getClassLoader());
    }

    /**
     * Test that the various flavors of ClassLoader.getResource work as expected.
     */
    public void testResource() {
        URL resource;

        final ClassLoader contextLoader = Thread.currentThread().getContextClassLoader();
        final ClassLoader childLoader = contextLoader.getParent();

        // getResource where it doesn't exist
        resource = childLoader.getResource("nosuchfile");
        assertNull("Non-null URL returned for invalid resource name", resource);

        // getResource where it is accessible only to parent class loader
        resource = childLoader.getResource("org/apache/commons/logging/Log.class");
        assertNotNull("Unable to locate Log.class resource", resource);

        // getResource where it is accessible only to child class loader
        resource = childLoader.getResource("org/apache/commons/logging/PathableTestSuite.class");
        assertNotNull("Unable to locate PathableTestSuite.class resource", resource);

        // getResource where it is accessible to both class loaders. The one visible
        // to the parent should be returned. The URL returned will be of form
        //  jar:file:/x/y.jar!path/to/resource. The file name part should include the jarname
        // of form commons-logging-nnnn.jar, not commons-logging-adapters-nnnn.jar
        resource = childLoader.getResource("org/apache/commons/logging/impl/Log4JLogger.class");
        assertNotNull("Unable to locate Log4JLogger.class resource", resource);
        assertTrue("Incorrect source for Log4JLogger class",
                resource.toString().indexOf(Artifacts.getMainJarName()) > 0);
    }

    /**
     * Test that getResourceAsStream works.
     */
    public void testResourceAsStream() throws Exception {
        // verify the class loader hierarchy
        final ClassLoader contextLoader = Thread.currentThread().getContextClassLoader();
        final ClassLoader childLoader = contextLoader.getParent();
        final ClassLoader parentLoader = childLoader.getParent();
        final ClassLoader bootLoader = parentLoader.getParent();
        assertNull("Unexpected class loader hierarchy", bootLoader);

        // getResourceAsStream where no instances exist
        InputStream is = childLoader.getResourceAsStream("nosuchfile");
        assertNull("Invalid resource returned non-null stream", is);

        // getResourceAsStream where resource does exist
        is = childLoader.getResourceAsStream("org/apache/commons/logging/Log.class");
        assertNotNull("Null returned for valid resource", is);
        is.close();

        // It would be nice to test parent-first ordering here, but that would require
        // having a resource with the same name in both the parent and child loaders,
        // but with different contents. That's a little tricky to set up so we'll
        // skip that for now.
    }

    /**
     * Test that the various flavors of ClassLoader.getResources work as expected.
     */
    public void testResources() throws Exception {
        // verify the class loader hierarchy
        final ClassLoader contextLoader = Thread.currentThread().getContextClassLoader();
        final ClassLoader childLoader = contextLoader.getParent();
        final ClassLoader parentLoader = childLoader.getParent();
        final ClassLoader bootLoader = parentLoader.getParent();
        assertNull("Unexpected class loader hierarchy", bootLoader);

        // getResources where no instances exist
        Enumeration<URL> resources = childLoader.getResources("nosuchfile");
        URL[] urls = toURLArray(resources);
        assertEquals("Non-null URL returned for invalid resource name", 0, urls.length);

        // getResources where the resource only exists in the parent
        resources = childLoader.getResources("org/apache/commons/logging/Log.class");
        urls = toURLArray(resources);
        assertEquals("Unexpected number of Log.class resources found", 1, urls.length);

        // getResources where the resource only exists in the child
        resources = childLoader.getResources("org/apache/commons/logging/PathableTestSuite.class");
        urls = toURLArray(resources);
        assertEquals("Unexpected number of PathableTestSuite.class resources found", 1, urls.length);

        // getResources where the resource exists in both.
        // resources should be returned in order (parent-resource, child-resource)
        resources = childLoader.getResources("org/apache/commons/logging/impl/Log4JLogger.class");
        urls = toURLArray(resources);
        assertEquals("Unexpected number of Log4JLogger.class resources found", 2, urls.length);

        // There is no gaurantee about the ordering of results returned from getResources
        // To make this test portable across JVMs, sort the string to give them a known order
        final String[] urlsToStrings = new String[2];
        urlsToStrings[0] = urls[0].toString();
        urlsToStrings[1] = urls[1].toString();
        Arrays.sort(urlsToStrings);
        assertTrue("Incorrect source for Log4JLogger class",
                urlsToStrings[0].indexOf(Artifacts.getAdaptersJarName()) > 0);
        assertTrue("Incorrect source for Log4JLogger class",
                urlsToStrings[1].indexOf(Artifacts.getMainJarName()) > 0);

    }
}
