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
package org.apache.commons.logging.proofofconcept.caller;

/**
 * Tests the behaviour of calls to logging
 * and formats the results.
 * The actual logging calls are execute by {@link SomeObject}. 
 */
public class JCLDemonstrator{
    
    /**
     * Runs {@link #runJCL()} and {@link #runStatic()}
     *
     */
    public void run() {
        runJCL();
        runStatic();
    }
    
    /**
     * Runs a test that logs to JCL
     * and outputs the results to <code>System.out</code>.
     */
    public void runJCL() {
        try {
            SomeObject someObject = new SomeObject();
            System.out.println("--------------------");
            System.out.println("Logging to JCL:");
            someObject.logToJCL();
            System.out.println("JCL Logging OK");
        } catch (Throwable t) {
            System.out.println("JCL Logging FAILED: " + t.getClass());
            System.out.println(t.getMessage());
            System.out.println("");
        }
    }

    /**
     * Runs a test that logs to Log4J via static calls
     * and outputs the results to <code>System.out</code>.
     */
    public void runStatic() {
        try {
            SomeObject someObject = new SomeObject();
            System.out.println("--------------------");
            System.out.println("Logging to Static:");
            someObject.logToStaticLog4J();
            System.out.println("Static Logging: OK");
        } catch (Throwable t) {
            System.out.println("Static Logging FAILED: " + t.getClass());
            System.out.println(t.getMessage());
            System.out.println("");
        }
    }
}
