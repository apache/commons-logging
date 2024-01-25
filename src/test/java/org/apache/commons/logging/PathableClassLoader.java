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
package org.apache.commons.logging;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;

/**
 * A ClassLoader which sees only specified classes, and which can be
 * set to do parent-first or child-first path lookup.
 * <p>
 * Note that this class loader is not "industrial strength"; users
 * looking for such a class may wish to look at the Tomcat sourcecode
 * instead. In particular, this class may not be threadsafe.
 * <p>
 * Note that the ClassLoader.getResources method isn't overloaded here.
 * It would be nice to ensure that when child-first lookup is set the
 * resources from the child are returned earlier in the list than the
 * resources from the parent. However overriding this method isn't possible
 * as the Java 1.4 version of ClassLoader declares this method final
 * (though the Java 1.5 version has removed the final qualifier). As the
 * ClassLoader Javadoc doesn't specify the order in which resources
 * are returned, it's valid to return the resources in any order (just
 * untidy) so the inherited implementation is technically ok.
 */

public class PathableClassLoader extends URLClassLoader {

    private static final URL[] NO_URLS = {};

    /**
     * A map of package-prefix to ClassLoader. Any class which is in
     * this map is looked up via the specified class loader instead of
     * the classpath associated with this class loader or its parents.
     * <p>
     * This is necessary in order for the rest of the world to communicate
     * with classes loaded via a custom class loader. As an example, junit
     * tests which are loaded via a custom class loader needs to see
     * the same junit classes as the code invoking the test, otherwise
     * they can't pass result objects back.
     * <p>
     * Normally, only a class loader created with a null parent needs to
     * have any lookasides defined.
     */
    private HashMap lookasides;

    /**
     * See setParentFirst.
     */
    private boolean parentFirst = true;

    /**
     * Constructs a new instance.
     * <p>
     * Often, null is passed as the parent, that is, the parent of the new
     * instance is the bootloader. This ensures that the classpath is
     * totally clean; nothing but the standard Java library will be
     * present.
     * <p>
     * When using a null parent class loader with a junit test, it *is*
     * necessary for the junit library to also be visible. In this case, it
     * is recommended that the following code be used:
     * <pre>
     * pathableLoader.useExplicitLoader(
     *   "junit.",
     *   junit.framework.Test.class.getClassLoader());
     * </pre>
     * Note that this works regardless of whether junit is on the system
     * classpath, or whether it has been loaded by some test framework that
     * creates its own class loader to run unit tests in (eg maven2's
     * Surefire plugin).
     */
    public PathableClassLoader(final ClassLoader parent) {
        super(NO_URLS, parent);
    }

    /**
     * Specify a logical library to be included in the classpath used to
     * locate classes.
     * <p>
     * The specified lib name is used as a key into the system properties;
     * there is expected to be a system property defined with that name
     * whose value is a url that indicates where that logical library can
     * be found. Typically this is the name of a jar file, or a directory
     * containing class files.
     * <p>
     * If there is no system property, but the class loader that loaded
     * this class is a URLClassLoader then the set of URLs that the
     * class loader uses for its classpath is scanned; any jar in the
     * URL set whose name starts with the specified string is added to
     * the classpath managed by this instance.
     * <p>
     * Using logical library names allows the calling code to specify its
     * desired classpath without knowing the exact location of the necessary
     * classes.
     */
    public void addLogicalLib(final String logicalLib) {
        // first, check the system properties
        final String fileName = System.getProperty(logicalLib);
        if (fileName != null) {
            try {
                final File file = new File(fileName);
                if (!file.exists()) {
                    Assert.fail("Unable to add logical library " + fileName);
                }
                final URL libUrl = file.toURL();
                addURL(libUrl);
                return;
            } catch (final java.net.MalformedURLException e) {
                throw new UnknownError(
                    "Invalid file [" + fileName + "] for logical lib [" + logicalLib + "]");
            }
        }

        // now check the classpath for a similar-named lib
        final URL libUrl = libFromClasspath(logicalLib);
        if (libUrl != null) {
            addURL(libUrl);
            return;
        }

        // lib not found
        throw new UnknownError(
            "Logical lib [" + logicalLib + "] is not defined"
            + " as a System property.");
    }

    /**
     * Specify a collection of logical libraries. See addLogicalLib.
     */
    public void addLogicalLib(final String[] logicalLibs) {
        for (final String logicalLib : logicalLibs) {
            addLogicalLib(logicalLib);
        }
    }

    /**
     * Allow caller to explicitly add paths. Generally this not a good idea;
     * use addLogicalLib instead, then define the location for that logical
     * library in the build.xml file.
     */
    @Override
    public void addURL(final URL url) {
        super.addURL(url);
    }

    /**
     * Same as parent class method except that when parentFirst is false
     * the resource is looked for in the local classpath before the parent
     * loader is consulted.
     */
    @Override
    public URL getResource(final String name) {
        if (parentFirst) {
            return super.getResource(name);
        }
        final URL local = super.findResource(name);
        if (local != null) {
            return local;
        }
        return super.getResource(name);
    }

    /**
     * Same as parent class method except that when parentFirst is false
     * the resource is looked for in the local classpath before the parent
     * loader is consulted.
     */
    @Override
    public InputStream getResourceAsStream(final String name) {
        if (parentFirst) {
            return super.getResourceAsStream(name);
        }
        final URL local = super.findResource(name);
        if (local != null) {
            try {
                return local.openStream();
            } catch (final IOException e) {
                // TODO: check if this is right or whether we should
                // fall back to trying parent. The Javadoc doesn't say...
                return null;
            }
        }
        return super.getResourceAsStream(name);
    }

    /**
     * Emulate a proper implementation of getResources which respects the
     * setting for parentFirst.
     * <p>
     * Note that it's not possible to override the inherited getResources, as
     * it's declared final in java1.4 (thought that's been removed for 1.5).
     * The inherited implementation always behaves as if parentFirst=true.
     */
    public Enumeration getResourcesInOrder(final String name) throws IOException {
        if (parentFirst) {
            return super.getResources(name);
        }
        final Enumeration localUrls = super.findResources(name);

        final ClassLoader parent = getParent();
        if (parent == null) {
            // Alas, there is no method to get matching resources
            // from a null (BOOT) parent class loader. Calling
            // ClassLoader.getSystemClassLoader isn't right. Maybe
            // calling Class.class.getResources(name) would do?
            //
            // However for the purposes of unit tests, we can
            // simply assume that no relevant resources are
            // loadable from the parent; unit tests will never be
            // putting any of their resources in a "boot" class loader
            // path!
            return localUrls;
        }
        final Enumeration parentUrls = parent.getResources(name);

        final ArrayList localItems = toList(localUrls);
        final ArrayList parentItems = toList(parentUrls);
        localItems.addAll(parentItems);
        return Collections.enumeration(localItems);
    }

    /**
     * If the class loader that loaded this class has this logical lib in its
     * path, then return the matching URL otherwise return null.
     * <p>
     * This only works when the class loader loading this class is an instance
     * of URLClassLoader and thus has a getURLs method that returns the classpath
     * it uses when loading classes. However in practice, the vast majority of the
     * time this type is the class loader used.
     * <p>
     * The classpath of the class loader for this instance is scanned, and any
     * jarfile in the path whose name starts with the logicalLib string is
     * considered a match. For example, passing "foo" will match a url
     * of {@code file:///some/where/foo-2.7.jar}.
     * <p>
     * When multiple classpath entries match the specified logicalLib string,
     * the one with the shortest file name component is returned. This means that
     * if "foo-1.1.jar" and "foobar-1.1.jar" are in the path, then a logicalLib
     * name of "foo" will match the first entry above.
     */
    private URL libFromClasspath(final String logicalLib) {
        final ClassLoader cl = this.getClass().getClassLoader();
        if (!(cl instanceof URLClassLoader)) {
            return null;
        }

        final URLClassLoader ucl = (URLClassLoader) cl;
        final URL[] path = ucl.getURLs();
        URL shortestMatch = null;
        int shortestMatchLen = Integer.MAX_VALUE;
        for (final URL u : path) {
            // extract the file name bit on the end of the URL
            String fileName = u.toString();
            if (!fileName.endsWith(".jar")) {
                // not a jarfile, ignore it
                continue;
            }

            final int lastSlash = fileName.lastIndexOf('/');
            if (lastSlash >= 0) {
                fileName = fileName.substring(lastSlash+1);
            }

            // ok, this is a candidate
            if (fileName.startsWith(logicalLib) && fileName.length() < shortestMatchLen) {
                shortestMatch = u;
                shortestMatchLen = fileName.length();
            }
        }

        return shortestMatch;
    }

    /**
     * Override ClassLoader method.
     * <p>
     * For each explicitly mapped package prefix, if the name matches the
     * prefix associated with that entry then attempt to load the class via
     * that entries' class loader.
     */
    @Override
    protected Class loadClass(final String name, final boolean resolve)
    throws ClassNotFoundException {
        // just for performance, check java and javax
        if (name.startsWith("java.") || name.startsWith("javax.")) {
            return super.loadClass(name, resolve);
        }

        if (lookasides != null) {
            for (final Object element : lookasides.entrySet()) {
                final Map.Entry entry = (Map.Entry) element;
                final String prefix = (String) entry.getKey();
                if (name.startsWith(prefix)) {
                    final ClassLoader loader = (ClassLoader) entry.getValue();
                    final Class clazz = Class.forName(name, resolve, loader);
                    return clazz;
                }
            }
        }

        if (parentFirst) {
            return super.loadClass(name, resolve);
        }
        try {
            Class clazz = findLoadedClass(name);
            if (clazz == null) {
                clazz = super.findClass(name);
            }
            if (resolve) {
                resolveClass(clazz);
            }
            return clazz;
        } catch (final ClassNotFoundException e) {
            return super.loadClass(name, resolve);
        }
    }

    /**
     * Specify whether this class loader should ask the parent class loader
     * to resolve a class first, before trying to resolve it via its own
     * classpath.
     * <p>
     * Checking with the parent first is the normal approach for java, but
     * components within containers such as servlet engines can use
     * child-first lookup instead, to allow the components to override libs
     * which are visible in shared class loaders provided by the container.
     * <p>
     * Note that the method getResources always behaves as if parentFirst=true,
     * because of limitations in Java 1.4; see the Javadoc for method
     * getResourcesInOrder for details.
     * <p>
     * This value defaults to true.
     */
    public void setParentFirst(final boolean state) {
        parentFirst = state;
    }

    /**
     *
     * Clean implementation of list function of
     * {@link java.util.Collection} added in JDK 1.4
     * @param en {@code Enumeration}, possibly null
     * @return {@code ArrayList} containing the enumerated
     * elements in the enumerated order, not null
     */
    private ArrayList toList(final Enumeration en) {
        final ArrayList results = new ArrayList();
        if (en != null) {
            while (en.hasMoreElements()) {
                final Object element = en.nextElement();
                results.add(element);
            }
        }
        return results;
    }

    /**
     * Specify a class loader to use for specific Java packages.
     * <p>
     * The specified class loader is normally a loader that is NOT
     * an ancestor of this class loader. In particular, this loader
     * may have the bootloader as its parent, but be configured to
     * see specific other classes (eg the junit library loaded
     * via the system class loader).
     * <p>
     * The differences between using this method, and using
     * addLogicalLib are:
     * <ul>
     * <li>If code calls getClassLoader on a class loaded via
     * "lookaside", then traces up its inheritance chain, it
     * will see the "real" class loaders. When the class is remapped
     * into this class loader via addLogicalLib, the class loader
     * chain seen is this object plus ancestors.
     * <li>If two different jars contain classes in the same
     * package, then it is not possible to load both jars into
     * the same "lookaside" class loader (eg the system class loader)
     * then map one of those subsets from here. Of course they could
     * be loaded into two different "lookaside" class loaders and
     * then a prefix used to map from here to one of those class loaders.
     * </ul>
     */
    public void useExplicitLoader(final String prefix, final ClassLoader loader) {
        if (lookasides == null) {
            lookasides = new HashMap();
        }
        lookasides.put(prefix, loader);
    }

    /**
     * For classes with the specified prefix, get them from the system
     * classpath <i>which is active at the point this method is called</i>.
     * <p>
     * This method is just a shortcut for
     * <pre>
     * useExplicitLoader(prefix, ClassLoader.getSystemClassLoader());
     * </pre>
     * <p>
     * Of course, this assumes that the classes of interest are already
     * in the classpath of the system class loader.
     */
    public void useSystemLoader(final String prefix) {
        useExplicitLoader(prefix, ClassLoader.getSystemClassLoader());

    }
}
