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
 * Runs parent first demonstrations.
 * @see #main(String[])
 */
public class ParentFirstRunner extends ClassLoaderRunner {

        
    public ParentFirstRunner() throws MalformedURLException {
    }
    
    public void testCase1()  {
        int parentUrls = JCL_JAR + STATIC_JAR;
        int childUrls = JCL_JAR + STATIC_JAR + CALLER_JAR + LOG4J_JAR;
        run("1", parentUrls, childUrls, false, false);
    }
    
    public void testCase2() {
        int parentUrls = JCL_JAR + STATIC_JAR + CALLER_JAR;
        int childUrls = JCL_JAR + STATIC_JAR + LOG4J_JAR;
        run("2", parentUrls, childUrls, false, false);
    }
    
    public void testCase3() {
        int parentUrls = JCL_JAR + STATIC_JAR;
        int childUrls = JCL_JAR + STATIC_JAR + CALLER_JAR + LOG4J_JAR;
        run("3", parentUrls, childUrls, true, false);
    }
    
    public void testCase4() {
        int parentUrls = JCL_JAR + STATIC_JAR + CALLER_JAR;
        int childUrls = JCL_JAR + STATIC_JAR + LOG4J_JAR;
        run("4", parentUrls, childUrls, true, false);
    }

    public void testCase5()  {
        int parentUrls = JCL_JAR + STATIC_JAR + LOG4J_JAR;
        int childUrls = JCL_JAR + STATIC_JAR + CALLER_JAR + LOG4J_JAR;
        run("5", parentUrls, childUrls, false, false);
    }
    
    public void testCase6() {
        int parentUrls = JCL_JAR + STATIC_JAR + CALLER_JAR + LOG4J_JAR;
        int childUrls = JCL_JAR + STATIC_JAR + LOG4J_JAR;
        run("6", parentUrls, childUrls, false, false);
    }
    
    public void testCase7() {
        int parentUrls = JCL_JAR + STATIC_JAR + LOG4J_JAR;
        int  childUrls = JCL_JAR + STATIC_JAR + CALLER_JAR + LOG4J_JAR;
        run("7", parentUrls, childUrls, true, false);
    }
    
    public void testCase8() {
        int parentUrls = JCL_JAR + STATIC_JAR + CALLER_JAR + LOG4J_JAR;
        int childUrls = JCL_JAR + STATIC_JAR + LOG4J_JAR;
        run("8", parentUrls, childUrls, true, false);
    }
    

    public void testCase9()  {
        int parentUrls = API_JAR + STATIC_JAR;
        int childUrls = JCL_JAR + STATIC_JAR + CALLER_JAR + LOG4J_JAR;
        run("9", parentUrls, childUrls, false, false);
    }
    
    public void testCase10() {
        int parentUrls = API_JAR + STATIC_JAR + CALLER_JAR;
        int childUrls = JCL_JAR + STATIC_JAR + LOG4J_JAR;
        run("10", parentUrls, childUrls, false, false);
    }
    
    public void testCase11() {
        int parentUrls = API_JAR + STATIC_JAR;
        int childUrls = JCL_JAR + STATIC_JAR + CALLER_JAR + LOG4J_JAR;
        run("11", parentUrls, childUrls, true, false);
    }
    
    public void testCase12() {
        int parentUrls = API_JAR + STATIC_JAR + CALLER_JAR;
        int childUrls = JCL_JAR + STATIC_JAR + LOG4J_JAR;
        run("12", parentUrls, childUrls, true, false);
    }

    public void testCase13()  {
        int parentUrls = API_JAR + STATIC_JAR + LOG4J_JAR;
        int childUrls = JCL_JAR + STATIC_JAR + CALLER_JAR + LOG4J_JAR;
        run("13", parentUrls, childUrls, false, false);
    }
    
    public void testCase14() {
        int parentUrls = API_JAR + STATIC_JAR + CALLER_JAR + LOG4J_JAR;
        int childUrls = JCL_JAR + STATIC_JAR + LOG4J_JAR;
        run("14", parentUrls, childUrls, false, false);
    }
    
    public void testCase15() {
        int parentUrls = API_JAR + STATIC_JAR + LOG4J_JAR;
        int childUrls = JCL_JAR + STATIC_JAR + CALLER_JAR + LOG4J_JAR;
        run("15", parentUrls, childUrls, true, false);
    }
    
    public void testCase16() {
        int parentUrls = API_JAR + STATIC_JAR + CALLER_JAR + LOG4J_JAR;
        int childUrls = JCL_JAR + STATIC_JAR + LOG4J_JAR;
        run("16", parentUrls, childUrls, true, false);
    }
    
    /**
     * Runs all parent first cases.
     * @param args
     */
    public static void main(String[] args)
    {
        ParentFirstRunner runner;
        try {
            runner = new ParentFirstRunner();            
            
            System.out.println("");
            System.out.println("");
            System.out.println("Running Parent First Cases...");
            System.out.println("");
            System.out.println("");
            runner.testCase1();
            runner.testCase2();
            runner.testCase3();
            runner.testCase4();
            runner.testCase5();
            runner.testCase6();
            runner.testCase7();
            runner.testCase8();
            runner.testCase9();
            runner.testCase10();
            runner.testCase11();
            runner.testCase12();
            runner.testCase13();
            runner.testCase14();
            runner.testCase15();
            runner.testCase16();
        } catch (MalformedURLException e) {
            System.out.println("Cannot find required jars");
            e.printStackTrace();
        }

    }
}
