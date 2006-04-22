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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * A ClassLoader which sees only specified classes, and which can be
 * set to do parent-first or child-first path lookup.
 * <p>
 * Note that this classloader is not "industrial strength"; users
 * looking for such a class may wish to look at the Tomcat sourcecode
 * instead. In particular, this class may not be threadsafe.
 * <p>
 * Note that the ClassLoader.getResources method isn't overloaded here.
 * It would be nice to ensure that when child-first lookup is set the
 * resources from the child are returned earlier in the list than the
 * resources from the parent. However overriding this method isn't possible
 * as the java 1.4 version of ClassLoader declares this method final
 * (though the java 1.5 version has removed the final qualifier). As the
 * ClassLoader javadoc doesn't specify the order in which resources
 * are returned, it's valid to return the resources in any order (just
 * untidy) so the inherited implementation is technically ok.
 */

public class PathableClassLoader extends URLClassLoader {
    
    private static final URL[] NO_URLS = new URL[0];
    
    /**
     * A map of package-prefix to ClassLoader. Any class which is in
     * this map is looked up via the specified classloader instead of
     * the classpath associated with this classloader or its parents.
     * <p>
     * This is necessary in order for the rest of the world to communicate
     * with classes loaded via a custom classloader. As an example, junit
     * testcases which are loaded via a custom classloader needs to see
     * the same junit classes as the code invoking the testcase, otherwise
     * they can't pass result objects back. 
     * <p>
     * Normally, only a classloader created with a null parent needs to
     * have any lookasides defined.
     */
    private HashMap lookasides = null;

    /**
     * See setParentFirst.
     */
    private boolean parentFirst = true;
    
    /**
     * Constructor.
     */
    public PathableClassLoader(ClassLoader parent) {
        super(NO_URLS, parent);
    }
    
    /**
     * Allow caller to explicitly add paths. Generally this not a good idea;
     * use addLogicalLib instead, then define the location for that logical
     * library in the build.xml file.
     */
    public void addURL(URL url) {
        super.addURL(url);
    }

    /**
     * Specify whether this classloader should ask the parent classloader
     * to resolve a class first, before trying to resolve it via its own
     * classpath.
     * <p> 
     * Checking with the parent first is the normal approach for java, but
     * components within containers such as servlet engines can use 
     * child-first lookup instead, to allow the components to override libs
     * which are visible in shared classloaders provided by the container.
     * <p>
     * Note that the method getResources always behaves as if parentFirst=true,
     * because of limitations in java 1.4; see the javadoc for method
     * getResourcesInOrder for details.
     * <p>
     * This value defaults to true.
     */
    public void setParentFirst(boolean state) {
        parentFirst = state;
    }

    /**
     * For classes with the specified prefix, get them from the system
     * classpath <i>which is active at the point this method is called</i>.
     * <p>
     * This method is just a shortcut for
     * <pre>
     * useExplicitLoader(prefix, ClassLoader.getSystemClassLoader());
     * </pre>
     */
    public void useSystemLoader(String prefix) {
        useExplicitLoader(prefix, ClassLoader.getSystemClassLoader());
        
    }

    /**
     * Specify a classloader to use for specific java packages.
     */
    public void useExplicitLoader(String prefix, ClassLoader loader) {
        if (lookasides == null) {
            lookasides = new HashMap();
        }
        lookasides.put(prefix, loader);
    }

    /**
     * Specify a collection of logical libraries. See addLogicalLib.
     */
    public void addLogicalLib(String[] logicalLibs) {
        for(int i=0; i<logicalLibs.length; ++i) {
            addLogicalLib(logicalLibs[i]);
        }
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
     * Using logical library names allows the calling code to specify its
     * desired classpath without knowing the exact location of the necessary
     * classes. 
     */
    public void addLogicalLib(String logicalLib) {
        String filename = System.getProperty(logicalLib);
        if (filename == null) {
            throw new UnknownError(
                "Logical lib [" + logicalLib + "] is not defined"
                + " as a System property.");
        }

        try {
            URL url = new File(filename).toURL();
            addURL(url);
        } catch(java.net.MalformedURLException e) {
            throw new UnknownError(
                "Invalid file [" + filename + "] for logical lib [" + logicalLib + "]");
        }
    }
    
    /**
     * Override ClassLoader method.
     * <p>
     * For each explicitly mapped package prefix, if the name matches the 
     * prefix associated with that entry then attempt to load the class via 
     * that entries' classloader.
     */
    protected Class loadClass(String name, boolean resolve) 
    throws ClassNotFoundException {
        // just for performance, check java and javax
        if (name.startsWith("java.") || name.startsWith("javax.")) {
            return super.loadClass(name, resolve);
        }

        if (lookasides != null) {
            for(Iterator i = lookasides.entrySet().iterator(); i.hasNext(); ) {
                Map.Entry entry = (Map.Entry) i.next();
                String prefix = (String) entry.getKey();
                if (name.startsWith(prefix) == true) {
                    ClassLoader loader = (ClassLoader) entry.getValue();
                    Class clazz = Class.forName(name, resolve, loader);
                    return clazz;
                }
            }
        }
        
        if (parentFirst) {
            return super.loadClass(name, resolve);
        } else {
            // Implement child-first. 
            //
            // It appears that the findClass method doesn't check whether the
            // class has already been loaded. This seems odd to me, but without
            // first checking via findLoadedClass we can get java.lang.LinkageError
            // with message "duplicate class definition" which isn't good.
            
            try {
                Class clazz = findLoadedClass(name);
                if (clazz == null) {
                    clazz = super.findClass(name);
                }
                if (resolve) {
                    resolveClass(clazz);
                }
                return clazz;
            } catch(ClassNotFoundException e) {
                return super.loadClass(name, resolve);
            }
        }
    }
    
    /**
     * Same as parent class method except that when parentFirst is false
     * the resource is looked for in the local classpath before the parent
     * loader is consulted.
     */
    public URL getResource(String name) {
        if (parentFirst) {
            return super.getResource(name);
        } else {
            URL local = super.findResource(name);
            if (local != null) {
                return local;
            }
            return super.getResource(name);
        }
    }
    
    /**
     * Emulate a proper implementation of getResources which respects the
     * setting for parentFirst.
     * <p>
     * Note that it's not possible to override the inherited getResources, as
     * it's declared final in java1.4 (thought that's been removed for 1.5).
     * The inherited implementation always behaves as if parentFirst=true.
     */
    public Enumeration getResourcesInOrder(String name) throws IOException {
        if (parentFirst) {
            return super.getResources(name);
        } else {
            Enumeration localUrls = super.findResources(name);
            
            ClassLoader parent = getParent();
            if (parent == null) {
                // Alas, there is no method to get matching resources
                // from a null (BOOT) parent classloader. Calling
                // ClassLoader.getSystemClassLoader isn't right. Maybe
                // calling Class.class.getResources(name) would do?
                //
                // However for the purposes of unit tests, we can
                // simply assume that no relevant resources are
                // loadable from the parent; unit tests will never be
                // putting any of their resources in a "boot" classloader
                // path!
                return localUrls;
            }
            Enumeration parentUrls = parent.getResources(name);

            ArrayList localItems = toList(localUrls);
            ArrayList parentItems = toList(parentUrls);
            localItems.addAll(parentItems);
            return Collections.enumeration(localItems);
        }
    }
    
    /**
     * 
     * Clean implementation of list function of 
     * {@link java.utils.Collection} added in JDK 1.4 
     * @param en <code>Enumeration</code>, possibly null
     * @return <code>ArrayList</code> containing the enumerated
     * elements in the enumerated order, not null
     */
    private ArrayList toList(Enumeration en) {
        ArrayList results = new ArrayList();
        if (en != null) {
            while (en.hasMoreElements()){
                Object element = en.nextElement();
                results.add(element);
            }
        }
        return results;
    }
    
    /**
     * Same as parent class method except that when parentFirst is false
     * the resource is looked for in the local classpath before the parent
     * loader is consulted.
     */
    public InputStream getResourceAsStream(String name) {
        if (parentFirst) {
            return super.getResourceAsStream(name);
        } else {
            URL local = super.findResource(name);
            if (local != null) {
                try {
                    return local.openStream();
                } catch(IOException e) {
                    // TODO: check if this is right or whether we should
                    // fall back to trying parent. The javadoc doesn't say...
                    return null;
                }
            }
            return super.getResourceAsStream(name);
        }
    }
}
