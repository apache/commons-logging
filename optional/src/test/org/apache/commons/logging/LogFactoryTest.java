/*
 * Copyright 2004 The Apache Software Foundation.
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

import java.lang.ref.WeakReference;
import java.util.Hashtable;

import junit.framework.TestCase;

import org.apache.commons.logging.impl.LogFactoryImpl;
import org.apache.commons.logging.impl.WeakHashtable;

public class LogFactoryTest extends TestCase {

    
    /** Maximum number of iterations before our test fails */
    private static final int MAX_GC_ITERATIONS = 50;

    private ClassLoader origLoader          = null;
    private String      origFactoryProperty = null;

    public LogFactoryTest(String testName) {
        super(testName);
    }
    
    public void testLogFactoryType() {
        assertTrue(LogFactory.factories instanceof WeakHashtable);
    }
    
    /**
     * Tests that LogFactories are not removed from the map
     * if their creating ClassLoader is still alive.
     */ 
    public void testHoldFactories()
    {     
        // Get a factory and create a WeakReference to it that
        // we can check to see if the factory has been removed
        // from LogFactory.properties
        LogFactory factory = LogFactory.getFactory();
        WeakReference weakFactory = new WeakReference(factory);
        
        // Remove any hard reference to the factory
        factory = null; 
        
        // Run the gc, confirming that the original factory
        // is not dropped from the map even though there are 
        // no other references to it
        int iterations = 0;
        int bytz = 2;
        while(iterations++ < MAX_GC_ITERATIONS) {
            System.gc();
            
            assertNotNull("LogFactory released while ClassLoader still active.",
                          weakFactory.get());                
            
            // create garbage:
            byte[] b;
            try {
              b  =  new byte[bytz];            
              bytz = bytz * 2;
            }
            catch (OutOfMemoryError oom) {
                // This error means the test passed, as it means the LogFactory
                // was never released.  So, we have to catch and deal with it
                
                // Doing this is probably a no-no, but it seems to work ;-)
                b = null;
                System.gc();
                break;
            }
        }
    }
    
    /**
     * Tests that a LogFactory is eventually removed from the map
     * after its creating ClassLoader is garbage collected. 
     */
    public void testReleaseFactories()
    {
        // Create a temporary classloader        
        ClassLoader childLoader = new ClassLoader() {};
        Thread.currentThread().setContextClassLoader(childLoader);
        
        // Get a factory using the child loader.
        LogFactory factory        = LogFactory.getFactory();
        // Hold a WeakReference to the factory. When this reference
        // is cleared we know the factory has been cleared from
        // LogFactory.factories as well
        WeakReference weakFactory = new WeakReference(factory);
        
        // Get a WeakReference to the child loader so we know when it
        // has been gc'ed
        WeakReference weakLoader = new WeakReference(childLoader);
        
        // Remove any hard reference to the childLoader and the factory
        Thread.currentThread().setContextClassLoader(origLoader);
        childLoader = null;
        factory     = null;
        
        // Run the gc, confirming that the original childLoader
        // is dropped from the map
        int iterations = 0;
        int bytz = 2;
        while(true) {
            System.gc();
            if(iterations++ > MAX_GC_ITERATIONS){
                fail("Max iterations reached before childLoader released.");
            }
            
            if(weakLoader.get() == null) {
                break;                
            } else {
                // create garbage:
                byte[] b;
                try {
                    b =  new byte[bytz];
                    bytz = bytz * 2;
                }
                catch (OutOfMemoryError oom) {
                    // Doing this is probably a no-no, but it seems to work ;-)
                    b = null;
                    System.gc();
                    fail("OutOfMemory before childLoader released.");
                }
            }
        }
        
        // Confirm that the original factory is removed from the map
        // within the maximum allowed number of calls to put() +
        // the maximum number of subsequent gc iterations
        iterations = 0;
        while(true) {
            System.gc();
            if(iterations++ > WeakHashtable.MAX_PUTS_BEFORE_PURGE + MAX_GC_ITERATIONS){
                Hashtable table = LogFactory.factories;
                fail("Max iterations reached before factory released.");
            }           
            
            // Create a new child loader and use it to add to the map.
            ClassLoader newChildLoader  = new ClassLoader() {};
            Thread.currentThread().setContextClassLoader(newChildLoader);
            LogFactory.getFactory();
            Thread.currentThread().setContextClassLoader(origLoader);
            
            if(weakFactory.get() == null) {
                break;                
            } else {
                // create garbage:
                byte[] b;
                try {
                    b =  new byte[bytz];
                    bytz = bytz * 2;
                }
                catch (OutOfMemoryError oom) {
                    // Doing this is probably a no-no, but it seems to work ;-)
                    b = null;
                    bytz = 2; // start over
                    System.gc();
                }
            }
        }
        
    }    
    
    protected void setUp() throws Exception {
        // Preserve the original classloader and factory implementation
        // class so we can restore them when we are done
        origLoader          = Thread.currentThread().getContextClassLoader();
        origFactoryProperty = System.getProperty(LogFactory.FACTORY_PROPERTY);
        
        // Ensure we use LogFactoryImpl as our factory
        System.setProperty(LogFactory.FACTORY_PROPERTY, 
                           LogFactoryImpl.class.getName());
        
        super.setUp();
    }
    
    protected void tearDown() throws Exception {
        // Set the  classloader back to whatever it originally was
        Thread.currentThread().setContextClassLoader(origLoader);
        
        // Set the factory implementation class back to 
        // whatever it originally was
        if (origFactoryProperty != null) {
            System.setProperty(LogFactory.FACTORY_PROPERTY, 
                               origFactoryProperty);
        }
        else {
            System.getProperties().remove(LogFactory.FACTORY_PROPERTY);
        }
        
        super.tearDown();
    }
    
}
