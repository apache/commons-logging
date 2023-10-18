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

package org.apache.commons.logging.simple;


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
import org.apache.commons.logging.impl.SimpleLog;


/**
 * <p>TestCase for simple logging when running with zero configuration
 * other than selecting the SimpleLog implementation.</p>
 */

public class DefaultConfigTestCase extends TestCase {

    /**
     * Return the tests included in this test suite.
     * <p>
     * We need to use a PathableClassLoader here because the SimpleLog class
     * is a pile of junk and chock-full of static variables. Any other test
     * (like simple.CustomConfigTestCase) that has used the SimpleLog class
     * will already have caused it to do once-only initialization that we
     * can't reset, even by calling LogFactory.releaseAll, because of those
     * ugly statics. The only clean solution is to load a clean copy of
     * commons-logging including SimpleLog via a nice clean class loader.
     * Or we could fix SimpleLog to be sane...
     */
    public static Test suite() throws Exception {
        final Class thisClass = DefaultConfigTestCase.class;

        final PathableClassLoader loader = new PathableClassLoader(null);
        loader.useExplicitLoader("junit.", Test.class.getClassLoader());
        loader.addLogicalLib("testclasses");
        loader.addLogicalLib("commons-logging");

        final Class testClass = loader.loadClass(thisClass.getName());
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

    // Check the decorated log instance
    protected void checkDecorated() {
        assertNotNull("Log exists", log);
        assertEquals("Log class",
                     "org.apache.commons.logging.simple.DecoratedSimpleLog",
                     log.getClass().getName());

        // Can we call level checkers with no exceptions?
        assertFalse(log.isDebugEnabled());
        assertTrue(log.isErrorEnabled());
        assertTrue(log.isFatalEnabled());
        assertTrue(log.isInfoEnabled());
        assertFalse(log.isTraceEnabled());
        assertTrue(log.isWarnEnabled());

        // Can we retrieve the current log level?
        assertEquals(SimpleLog.LOG_LEVEL_INFO, ((SimpleLog) log).getLevel());

        // Can we validate the extra exposed properties?
        assertEquals("yyyy/MM/dd HH:mm:ss:SSS zzz",
                     ((DecoratedSimpleLog) log).getDateTimeFormat());
        assertEquals("DecoratedLogger",
                     ((DecoratedSimpleLog) log).getLogName());
        assertFalse(((DecoratedSimpleLog) log).getShowDateTime());
        assertTrue(((DecoratedSimpleLog) log).getShowShortName());
    }

    // Check the standard log instance
    protected void checkStandard() {
        assertNotNull("Log exists", log);
        assertEquals("Log class",
                     "org.apache.commons.logging.impl.SimpleLog",
                     log.getClass().getName());

        // Can we call level checkers with no exceptions?
        assertFalse(log.isDebugEnabled());
        assertTrue(log.isErrorEnabled());
        assertTrue(log.isFatalEnabled());
        assertTrue(log.isInfoEnabled());
        assertFalse(log.isTraceEnabled());
        assertTrue(log.isWarnEnabled());

        // Can we retrieve the current log level?
        assertEquals(SimpleLog.LOG_LEVEL_INFO, ((SimpleLog) log).getLevel());
    }

    /**
     * Set system properties that will control the LogFactory/Log objects
     * when they are created. Subclasses can override this method to
     * define properties that suit them.
     */
    public void setProperties() {
        System.setProperty(
            "org.apache.commons.logging.Log",
            "org.apache.commons.logging.impl.SimpleLog");
    }

    /**
     * Set up instance variables required by this test case.
     */
    @Override
    public void setUp() throws Exception {
        LogFactory.releaseAll();
        setProperties();
        setUpFactory();
        setUpLog("TestLogger");
    }

    // Set up decorated log instance
    protected void setUpDecorated(final String name) {
        log = new DecoratedSimpleLog(name);
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


    // Test pristine DecoratedSimpleLog instance
    public void testPristineDecorated() {
        setUpDecorated("DecoratedLogger");
        checkDecorated();
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
        checkStandard();
    }


    // Test Serializability of standard instance
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
        checkStandard();

    }

}
