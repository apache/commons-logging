/* $Id$
*
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

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.net.URL;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.OutputStream;
import java.lang.reflect.Method;

/**
 * A rough test case for the basic operations of JCL.
 * <p>
 * This is very much a work-in-progress!
 */

public class BasicTestCase extends TestCase {

   public BasicTestCase(String name) {
       super(name);
   }

   public static Test suite() {
       return (new TestSuite(BasicTestCase.class));
   }

   /**
    * Test a single classloader with core and test jars in classpath.
    */
   public void testDirect() throws Exception {
       URL cwdDir = new URL("file:");
       URL coreJarPath = new URL(cwdDir, 
                   TestConstants.JAR_DIR + "/" + TestConstants.CORE_JAR);
       URL testJarPath = new URL(cwdDir, 
                   TestConstants.JAR_DIR + "/" + TestConstants.TEST_JAR);
       URL testPath = new URL(cwdDir,
                   TestConstants.JAR_DIR + "/" + "tests/");
       
       ParentFirstClassLoader loader = new ParentFirstClassLoader(null);
       loader.addURL(coreJarPath);
       loader.addURL(testJarPath);
       loader.addURL(testPath);

       String cwd = System.getProperty("user.dir");
       System.out.println("Current working dir:" + cwd);
       URL[] urls = loader.getURLs();
       for(int i=0; i<urls.length; ++i) {
           System.out.println("URL: " + urls[i]);
       }
       // create an in-memory stream that logging can output to.
       OutputStream logBuffer = new ByteArrayOutputStream();
       PrintStream logStream = new PrintStream(logBuffer);

       // Check that the factory class that LogFactory binds to is in fact the
       // test factory. Then set the destination stream for logging generated
       // by the test factory loggers.
       Class logFactoryClass = loader.loadClass(LogFactory.class.getName());
       Method getFactoryMethod = logFactoryClass.getMethod("getFactory", (Class[]) null);
       Object factory = getFactoryMethod.invoke(null, (Object[]) null);
       assertTrue(
               factory.getClass().getName()
               .equals("org.apache.commons.logging.impl.TestFactory"));
       Method setStreamMethod = factory.getClass().getMethod(
               "setStream", new Class[] {PrintStream.class});
       setStreamMethod.invoke(factory, new Object[] {logStream});

       // ok, now get some code in the loader to execute some log calls
       // and see if they appear in our in-memory buffer.
       Class logTesterClass = loader.loadClass(LogTester.class.getName());
       Object logTester = logTesterClass.newInstance();
       
       String logOutput = logBuffer.toString();
       assertTrue("No log output", logOutput.length() > 0);
       assertEquals("Unexpected log output", "INFO: LogTester test message\n", logOutput);
   }
   
   /**
    * Test what happens when the core jar is not in the classpath.
    */
   public void testNoCore() {
   }
   
   /**
    * Test what happens when no LogFactory implementation is in the classpath.
    */
   public void testNoLogFactory() {
   }
   
   /**
    * Test what happens when the core jar is deployed in both a parent
    * and a child classloader. A warning should be reported....
    */
   public void testMultipleCore() {
   }
   
   /**
    * Given the possible factors:
    * <ul>
    * <li>spi is in parent or child</li>
    * <li>adapter is in parent or child</li>
    * <li>caller is in parent or child</li>
    * <li>context is parent or child</li>
    * <li>child classloader policy is child-first or parent-first</li>
    * </ul>
    * <p>
    * Actually, we should iterate over all possible combinations of the
    * above factors, and for each assert whether logging should have
    * worked or not (and which logging got bound to!).
    */
   public void testClassloaderCombinations() {
   }
}
