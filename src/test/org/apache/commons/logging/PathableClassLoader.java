/*
 * Created on 24/06/2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */

package org.apache.commons.logging;

import java.net.URL;
import java.net.URLClassLoader;

// TODO: use Hashtable instead of HashMap
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;

import java.util.Enumeration;
import java.util.Vector;
import java.io.File;
import java.io.InputStream;
import java.io.IOException;

/**
 * A ClassLoader which sees only the specified classes.
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
     * Specify whether this classloader should ask the parent classloader
     * to resolve a class first, before trying to resolve it via its own
     * classpath.
     * <p> 
     * Checking with the parent first is the normal approach for java, but
     * components within containers such as servlet engines can use 
     * child-first lookup instead, to allow the components to override libs
     * which are visible in shared classloaders provided by the container.
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
            // ok, implement child-first
            try {
                Class clazz = super.findClass(name);
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
    
    /**
     * Same as parent class method except that when parentFirst is false
     * the resources available from this class are returned at the head of
     * the list instead of the tail.
     */
    public Enumeration getResources(String name) throws IOException {
        if (parentFirst) {
            return super.getResources(name);
        } else {
            Enumeration local = super.findResources(name);
            Enumeration parent = getParent().getResources(name);
            
            if (!local.hasMoreElements()) {
                return parent;
            }
            
            if (!parent.hasMoreElements()) {
                return local;
            }

            Vector v = new Vector();
            while (local.hasMoreElements()) {
                v.add(local.nextElement());
            }
            while (parent.hasMoreElements()) {
                v.add(parent.nextElement());
            }
            return v.elements();
        }
    }
}
