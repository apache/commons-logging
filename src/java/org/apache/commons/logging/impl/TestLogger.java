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

package org.apache.commons.logging.impl;

import org.apache.commons.logging.Log;
import java.io.PrintStream; 

/**
 * A logger class that writes to a specified PrintStream.
 */
public class TestLogger implements Log {

    private PrintStream stream;
    
    public TestLogger(PrintStream stream) {
        this.stream = stream;
    }

    public boolean isTraceEnabled() {
        return true;
    }

    public boolean isDebugEnabled() {
        return true;
    }

    public boolean isInfoEnabled() {
        return true;
    }

    public boolean isWarnEnabled() {
        return true;
    }

    public boolean isErrorEnabled() {
        return true;
    }

    public boolean isFatalEnabled() {
        return true;
    }

    public void trace(Object message) {
        log("TRACE: " + message);
    }

    public void trace(Object message, Throwable t) {
        log("TRACE: " + message);
    }

    public void debug(Object message) {
        log("DEBUG: " + message);
    }

    public void debug(Object message, Throwable t) {
        log("DEBUG: " + message);
    }

    public void info(Object message) {
        log("INFO: " + message);
    }

    public void info(Object message, Throwable t) {
        log("INFO: " + message);
    }

    public void warn(Object message) {
        log("WARN: " + message);
    }

    public void warn(Object message, Throwable t) {
        log("WARN: " + message);
    }

    public void error(Object message) {
        log("ERROR: " + message);
    }

    public void error(Object message, Throwable t) {
        log("ERROR: " + message);
    }

    public void fatal(Object message) {
        log("FATAL: " + message);
    }

    public void fatal(Object message, Throwable t) {
        log("FATAL: " + message);
    }

    private void log(String msg) {
        stream.println(msg);
    }
}
