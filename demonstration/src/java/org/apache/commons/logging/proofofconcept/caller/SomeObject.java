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

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.proofofconcept.staticlogger.StaticLog4JLogger;

/**
 * This simulates some application or library code
 * that uses logging.
 * This separation allows tests to be run
 * where this class is defined by either the parent
 * or the child classloader.
 */
public class SomeObject {

    /**
     * Logs a message to <code>Jakarta Commons Logging</code>.
     */
    public void logToJCL() {
        LogFactory.getLog("a log").info("A message");
    }
    
    /**
     * Logs a message to <code>Log4j</code> via a class
     * which makes a static call.
     */
    public void logToStaticLog4J() {
        StaticLog4JLogger.info("A message");
    }
}
