/*
 * Created on 23/05/2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
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
