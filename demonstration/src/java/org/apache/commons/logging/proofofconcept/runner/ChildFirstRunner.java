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

import java.net.MalformedURLException;


/**
 * Runs child first demonstrations.
 * @see #main(String[])
 */
public class ChildFirstRunner extends ClassLoaderRunner {

        
    public ChildFirstRunner() throws MalformedURLException {
    }
 
    
    public void testCase17()  {
        int parentUrls = JCL_JAR + STATIC_JAR;
        int childUrls = JCL_JAR + STATIC_JAR + CALLER_JAR + LOG4J_JAR;
        run("17", parentUrls, childUrls, false, true);
    }
    
    public void testCase18() {
        int parentUrls = JCL_JAR + STATIC_JAR + CALLER_JAR;
        int childUrls = JCL_JAR + STATIC_JAR + LOG4J_JAR;
        run("18", parentUrls, childUrls, false, true);
    }
    
    public void testCase19() {
        int parentUrls = JCL_JAR + STATIC_JAR;
        int childUrls = JCL_JAR + STATIC_JAR + CALLER_JAR + LOG4J_JAR;
        run("19", parentUrls, childUrls, true, true);
    }
    
    public void testCase20() {
        int parentUrls = JCL_JAR + STATIC_JAR + CALLER_JAR;
        int childUrls = JCL_JAR + STATIC_JAR + LOG4J_JAR;
        run("20", parentUrls, childUrls, true, true);
    }

    public void testCase21()  {
        int parentUrls = JCL_JAR + STATIC_JAR + LOG4J_JAR;
        int childUrls = JCL_JAR + STATIC_JAR + CALLER_JAR + LOG4J_JAR;
        run("21", parentUrls, childUrls, false, true);
    }
    
    public void testCase22() {
        int parentUrls = JCL_JAR + STATIC_JAR + CALLER_JAR + LOG4J_JAR;
        int childUrls = JCL_JAR + STATIC_JAR + LOG4J_JAR;
        run("22", parentUrls, childUrls, false, true);
    }
    
    public void testCase23() {
        int parentUrls = JCL_JAR + STATIC_JAR + LOG4J_JAR;
        int  childUrls = JCL_JAR + STATIC_JAR + CALLER_JAR + LOG4J_JAR;
        run("23", parentUrls, childUrls, true, true);
    }
    
    public void testCase24() {
        int parentUrls = JCL_JAR + STATIC_JAR + CALLER_JAR + LOG4J_JAR;
        int childUrls = JCL_JAR + STATIC_JAR + LOG4J_JAR;
        run("24", parentUrls, childUrls, true, true);
    }
    

    public void testCase25()  {
        int parentUrls = API_JAR + STATIC_JAR;
        int childUrls = JCL_JAR + STATIC_JAR + CALLER_JAR + LOG4J_JAR;
        run("25", parentUrls, childUrls, false, true);
    }
    
    public void testCase26() {
        int parentUrls = API_JAR + STATIC_JAR + CALLER_JAR;
        int childUrls = JCL_JAR + STATIC_JAR + LOG4J_JAR;
        run("26", parentUrls, childUrls, false, true);
    }
    
    public void testCase27() {
        int parentUrls = API_JAR + STATIC_JAR;
        int childUrls = JCL_JAR + STATIC_JAR + CALLER_JAR + LOG4J_JAR;
        run("27", parentUrls, childUrls, true, true);
    }
    
    public void testCase28() {
        int parentUrls = API_JAR + STATIC_JAR + CALLER_JAR;
        int childUrls = JCL_JAR + STATIC_JAR + LOG4J_JAR;
        run("28", parentUrls, childUrls, true, true);
    }

    public void testCase29()  {
        int parentUrls = API_JAR + STATIC_JAR + LOG4J_JAR;
        int childUrls = JCL_JAR + STATIC_JAR + CALLER_JAR + LOG4J_JAR;
        run("29", parentUrls, childUrls, false, true);
    }
    
    public void testCase30() {
        int parentUrls = API_JAR + STATIC_JAR + CALLER_JAR + LOG4J_JAR;
        int childUrls = JCL_JAR + STATIC_JAR + LOG4J_JAR;
        run("30", parentUrls, childUrls, false, true);
    }
    
    public void testCase31() {
        int parentUrls = API_JAR + STATIC_JAR + LOG4J_JAR;
        int childUrls = JCL_JAR + STATIC_JAR + CALLER_JAR + LOG4J_JAR;
        run("31", parentUrls, childUrls, true, true);
    }
    
    public void testCase32() {
        int parentUrls = API_JAR + STATIC_JAR + CALLER_JAR + LOG4J_JAR;
        int childUrls = JCL_JAR + STATIC_JAR + LOG4J_JAR;
        run("32", parentUrls, childUrls, true, true);
    }
    
    /**
     * Runs all child first cases.
     * @param args
     */
    public static void main(String[] args)
    {
        ChildFirstRunner runner;
        try {
            runner = new ChildFirstRunner();
            System.out.println("");
            System.out.println("");
            System.out.println("Running Child First Cases...");
            System.out.println("");
            System.out.println("");
            runner.testCase17();
            runner.testCase18();
            runner.testCase19();
            runner.testCase20();
            runner.testCase21();
            runner.testCase22();
            runner.testCase23();
            runner.testCase24();
            runner.testCase25();
            runner.testCase26();
            runner.testCase27();
            runner.testCase28();
            runner.testCase29();
            runner.testCase30();
            runner.testCase31();
            runner.testCase32();
        } catch (MalformedURLException e) {
            System.out.println("Cannot find required jars");
            e.printStackTrace();
        }

    }
}
