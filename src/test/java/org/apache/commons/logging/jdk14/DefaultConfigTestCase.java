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

package org.apache.commons.logging.jdk14;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import junit.framework.Test;
import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.PathableClassLoader;
import org.apache.commons.logging.PathableTestSuite;

/**
 * <p>TestCase for JDK 1.4 logging when running on a JDK 1.4 system with
 * zero configuration, and with Log4J not present (so JDK 1.4 logging
 * should be automatically configured.</p>
 */
public class DefaultConfigTestCase extends TestCase {

    /**
     * Return the tests included in this test suite.
     */
    public static Test suite() throws Exception {
        final PathableClassLoader loader = new PathableClassLoader(null);
        loader.useExplicitLoader("junit.", Test.class.getClassLoader());
        loader.addLogicalLib("testclasses");
        loader.addLogicalLib("commons-logging");

        final Class testClass = loader.loadClass(DefaultConfigTestCase.class.getName());
        return new PathableTestSuite(testClass, loader);
    }

    /**
     * <p>The {@link LogFactory} implementation we have selected.</p>
     */
    protected LogFactory factory;

    /**
     * <p>The {@link Log} implementation we have selected.</p>
     */
    protected Log log;

    /**
     * <p>Construct a new instance of this test case.</p>
     *
     * @param name Name of the test case
     */
    public DefaultConfigTestCase(final String name) {
        super(name);
    }

    // Check the log instance
    protected void checkLog() {

        assertNotNull("Log exists", log);
        assertEquals("Log class",
                     "org.apache.commons.logging.impl.Jdk14Logger",
                     log.getClass().getName());

        // Can we call level checkers with no exceptions?
        log.isDebugEnabled();
        log.isErrorEnabled();
        log.isFatalEnabled();
        log.isInfoEnabled();
        log.isTraceEnabled();
        log.isWarnEnabled();

    }

    /**
     * Sets up instance variables required by this test case.
     */
    @Override
    public void setUp() throws Exception {
        setUpFactory();
        setUpLog("TestLogger");
    }

    // Set up factory instance
    protected void setUpFactory() throws Exception {
        factory = LogFactory.getFactory();
    }

    // Set up log instance
    protected void setUpLog(final String name) throws Exception {
        log = LogFactory.getLog(name);
    }

    /**
     * Tear down instance variables required by this test case.
     */
    @Override
    public void tearDown() {
        log = null;
        factory = null;
        LogFactory.releaseAll();
    }

    // Test pristine LogFactory instance
    public void testPristineFactory() {
        assertNotNull("LogFactory exists", factory);
        assertEquals("LogFactory class",
                     "org.apache.commons.logging.impl.LogFactoryImpl",
                     factory.getClass().getName());

        final String[] names = factory.getAttributeNames();
        assertNotNull("Names exists", names);
        assertEquals("Names empty", 0, names.length);
    }

    // Test pristine Log instance
    public void testPristineLog() {
        checkLog();
    }

    // Test Serializability of Log instance
    public void testSerializable() throws Exception {

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

        // Check the characteristics of the resulting object
        checkLog();

    }

}
