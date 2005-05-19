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
package org.apache.commons.logging.proofofconcept.runner;

import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;


/**
 * Runs demonstrations with complex classloader hierarchies
 * and outputs formatted descriptions of the tests run.
 */
public class ClassLoaderRunner {

    private static final Class[] EMPTY_CLASSES = {};
    private static final Object[] EMPTY_OBJECTS = {};
    private static final URL[] EMPTY_URLS = {};
    
    public static final int LOG4J_JAR = 1 << 0;
    public static final int STATIC_JAR = 1 << 1;
    public static final int JCL_JAR = 1 << 2;
    public static final int CALLER_JAR = 1 << 3;
    public static final int API_JAR = 1 << 4;
    
    private final URL log4jUrl;
    private final URL staticUrl;
    private final URL jclUrl;
    private final URL callerUrl;
    private final URL apiUrl;
        
    /**
     * Loads URLs.
     * @throws MalformedURLException when any of these URLs cannot 
     * be resolved
     */
    public ClassLoaderRunner() throws MalformedURLException {
        log4jUrl = new URL("file:log4j.jar");
        staticUrl = new URL("file:static.jar");
        jclUrl = new URL("file:commons-logging.jar");
        callerUrl = new URL("file:caller.jar");
        apiUrl = new URL("file:commons-logging-api.jar");
    }
    
    /**
     * Runs a demonstration.
     * @param caseName human name for test (used for output)
     * @param parentJars bitwise code for jars to be definable 
     * by the parent classloader
     * @param childJars bitwise code for jars to be definable
     * by the child classloader
     * @param setContextClassloader true if the context classloader
     * should be set to the child classloader,
     * false preserves the default
     * @param childFirst true if the child classloader 
     * should delegate only if it cannot define a class, 
     * false otherwise
     */
    public void run(String caseName, int parentJars, int childJars, 
            boolean setContextClassloader, boolean childFirst) {
        
        System.out.println("");
        System.out.println("*****************************");
        System.out.println("");
        System.out.println("Running case " + caseName + "...");
        System.out.println("");
        URL[] parentUrls = urlsForJars(parentJars, "Parent Classloader: ");
        URL[] childUrls = urlsForJars(childJars, "Child Classloader:  ");
        System.out.println("Child context classloader: " + setContextClassloader);
        System.out.println("Child first: " + childFirst);
        System.out.println("");
        run("org.apache.commons.logging.proofofconcept.caller.JCLDemonstrator",
                parentUrls, childUrls, setContextClassloader, childFirst);
        System.out.println("*****************************");
    }
    
    /**
     * Converts a bitwise jar code into an array of URLs
     * containing approapriate URLs.
     * @param jars bitwise jar code
     * @param humanLoaderName human name for classloader
     * @return <code>URL</code> array, not null possibly empty
     */
    private URL[] urlsForJars(int jars, String humanLoaderName) {
        List urls = new ArrayList();;
        if ((LOG4J_JAR & jars) > 0) {
            urls.add(log4jUrl);
        }
        if ((STATIC_JAR & jars) > 0) {
            urls.add(staticUrl);
        }
        if ((JCL_JAR & jars) > 0) {
            urls.add(jclUrl);
        }
        if ((API_JAR & jars) > 0) {
            urls.add(apiUrl);
        }
        if ((CALLER_JAR & jars) > 0) {
            urls.add(callerUrl);
        }
        System.out.println(humanLoaderName + " " + urls);
        URL[] results = (URL[]) urls.toArray(EMPTY_URLS);
        return results;
    }
    
    /**
     * Runs a demonstration.
     * @param testName the human name for this test
     * @param parentClassloaderUrls the <code>URL</code>'s which should
     * be definable by the parent classloader, not null
     * @param childClassloaderUrls the <code>URL</code>'s which should
     * be definable by the child classloader, not null
     * @param setContextClassloader true if the context
     * classloader should be set to the child classloader,
     * false if the default context classloader should
     * be maintained
     * @param childFirst true if the child classloader
     * should delegate only when it cannot define the class,
     * false otherwise
     */
    public void run (String testName,  
                    URL[] parentClassloaderUrls, 
                    URL[] childClassloaderUrls,
                    boolean setContextClassloader,
                    boolean childFirst) {

        URLClassLoader parent = new URLClassLoader(parentClassloaderUrls);
        URLClassLoader child = null;
        if (childFirst) {
            child = new ChildFirstClassLoader(childClassloaderUrls, parent);
        } else {
            child = new URLClassLoader(childClassloaderUrls, parent);
        }
        
        if (setContextClassloader) {
            Thread.currentThread().setContextClassLoader(child);
        } else  {
            ClassLoader system = ClassLoader.getSystemClassLoader();
            Thread.currentThread().setContextClassLoader(system);
        }
        
        logDefiningLoaders(child, parent);
        
        try {
            
            Class callerClass = child.loadClass(testName);
            Method runMethod = callerClass.getDeclaredMethod("run", EMPTY_CLASSES);
            Object caller = callerClass.newInstance();
            runMethod.invoke(caller, EMPTY_OBJECTS);
            
        } catch (Exception e) {
            System.out.println("Cannot execute test: " + e.getMessage());
            e.printStackTrace();
        }
        
    }
    
    /**
     * Logs the classloaders which define important classes
     * @param child child <code>ClassLoader</code>, not null
     * @param parent parent <code>ClassLoader</code>, not null
     */
    private void logDefiningLoaders(ClassLoader child, ClassLoader parent)
    {
        System.out.println("");
        logDefiningLoaders(child, parent, "org.apache.commons.logging.LogFactory", "JCL          ");
        logDefiningLoaders(child, parent, "org.apache.log4j.Logger", "Log4j        ");
        logDefiningLoaders(child, parent, "org.apache.commons.logging.proofofconcept.staticlogger.StaticLog4JLogger", "Static Logger");
        logDefiningLoaders(child, parent, "org.apache.commons.logging.proofofconcept.caller.SomeObject", "Caller       ");
        System.out.println("");
    }
    
    /**
     * Logs the classloader which defines the class with the given name whose 
     * loading is initiated by the child classloader.
     * @param child child <code>ClassLoader</code>, not null
     * @param parent parent <code>ClassLoader</code>, not null
     * @param className name of the class to be loaded 
     * @param humanName the human name for the class
     */
    private void logDefiningLoaders(ClassLoader child, ClassLoader parent, String className, String humanName) {
        try {
            Class clazz = child.loadClass(className);
            ClassLoader definingLoader = clazz.getClassLoader();
            if (definingLoader == null)
            {
                System.out.println(humanName + " defined by SYSTEM class loader");
            }
            else if (definingLoader.equals(child))
            {
                System.out.println(humanName + " defined by CHILD  class loader");
            }
            else if (definingLoader.equals(parent))
            {
                System.out.println(humanName + " defined by PARENT class loader");
            }
            else
            {
                System.out.println(humanName + " defined by OTHER  class loader");
            }
        } catch (Exception e) {
            System.out.println(humanName + " NOT LOADABLE by application classloader");
        }
    }

}
