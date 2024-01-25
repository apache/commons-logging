/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.logging.avalon;

import org.apache.avalon.framework.logger.NullLogger;
import org.apache.commons.logging.AbstractLogTest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.impl.AvalonLogger;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 */
public class AvalonLoggerTestCase extends AbstractLogTest {

    public static Test suite() {
        final TestSuite suite = new TestSuite();
        suite.addTestSuite(AvalonLoggerTestCase.class);
        return suite;
    }

    @Override
    public Log getLogObject() {
        // Output does not seem to be used, so don't display it.
        final Log log = new AvalonLogger(new NullLogger());
        return log;
    }
}
