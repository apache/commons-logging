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

package org.apache.commons.logging.noop;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.apache.commons.logging.AbstractLogTest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.impl.NoOpLog;

/**
 * Tests for NoOpLog logging adapter.
 * <p>
 * This simply applies the tests defined in AbstractLogTest to this class.
 */
public class NoOpLogTestCase extends AbstractLogTest {
    private void checkLog(final Log log) {

        assertNotNull("Log exists", log);
        assertEquals("Log class",
                     "org.apache.commons.logging.impl.NoOpLog",
                     log.getClass().getName());

        // Can we call level checkers with no exceptions?
        // Note that *everything* is permanently disabled for NoOpLog
        assertFalse(log.isTraceEnabled());
        assertFalse(log.isDebugEnabled());
        assertFalse(log.isInfoEnabled());
        assertFalse(log.isWarnEnabled());
        assertFalse(log.isErrorEnabled());
        assertFalse(log.isFatalEnabled());
    }

    /**
     * Override the abstract method from the parent class so that the
     * inherited tests can access the right Log object type.
     */
    @Override
    public Log getLogObject()
    {
        return new NoOpLog(this.getClass().getName());
    }

    /**
     * Sets up instance variables required by this test case.
     */
    @Override
    public void setUp() throws Exception {
        LogFactory.releaseAll();

        System.setProperty(
                "org.apache.commons.logging.Log",
                "org.apache.commons.logging.impl.NoOpLog");
    }

    /**
     * Tear down instance variables required by this test case.
     */
    @Override
    public void tearDown() {
        LogFactory.releaseAll();
        System.getProperties().remove("org.apache.commons.logging.Log");
    }

    // Test Serializability of standard instance
    public void testSerializable() throws Exception {
        Log log = LogFactory.getLog(this.getClass().getName());
        checkLog(log);

        // Serialize and deserialize the instance
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(log);
        oos.close();
        final ByteArrayInputStream bais =
            new ByteArrayInputStream(baos.toByteArray());
        final ObjectInputStream ois = new ObjectInputStream(bais);
        log = (Log) ois.readObject();
        ois.close();

        checkLog(log);
    }
}
