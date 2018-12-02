package org.apache.commons.logging;

import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.apache.commons.logging.LogInterfaceDefaultMethodsTestCase.TestImplementation.LogLevel;

import junit.framework.TestCase;

/*
 * Test for the default methods uses Supplier on the Log interface for avoid unnecessary payload creation
 */
public class LogInterfaceDefaultMethodsTestCase extends TestCase {
    static class TestImplementation implements Log {
        enum LogLevel {
            DEBUG, ERROR, FATAL, INFO, TRACE, WARN
        }
        private LogLevel lastLoggedLevel = null;
        private Object lastLoggedMessage = null;
        private Throwable lastLoggedException = null;
        
        private Set<LogLevel> enabledLevels = EnumSet.noneOf(LogLevel.class);
        
        
        LogLevel getLastLoggedLevel() {
            return this.lastLoggedLevel;
        }

        Object getLastLoggedMessage() {
            return this.lastLoggedMessage;
        }

        Throwable getLastLoggedException() {
            return this.lastLoggedException;
        }
        
        void reset() {
            this.recordLogEntry(null, null, null);
            this.enabledLevels.clear();
        }
        
        void enableLogLevel(LogLevel level) {
            this.enabledLevels.add(level);
        }
        
        void disableLogLevel(LogLevel level) {
            this.enabledLevels.remove(level);
        }

        Set<LogLevel> getEnabledLevels() {
            return enabledLevels;
        }

        private void recordLogEntry(LogLevel level, Object message, Throwable t) {
            this.lastLoggedLevel = level;
            this.lastLoggedMessage = message;
            this.lastLoggedException = t;
        }

        @Override
        public void debug(Object message) {
            this.recordLogEntry(LogLevel.DEBUG, message, null);
        }

        @Override
        public void debug(Object message, Throwable t) {
            this.recordLogEntry(LogLevel.DEBUG, message, t);
        }

        @Override
        public void error(Object message) {
            this.recordLogEntry(LogLevel.ERROR, message, null);
        }

        @Override
        public void error(Object message, Throwable t) {
            this.recordLogEntry(LogLevel.ERROR, message, t);
        }

        @Override
        public void fatal(Object message) {
            this.recordLogEntry(LogLevel.FATAL, message, null);
        }

        @Override
        public void fatal(Object message, Throwable t) {
            this.recordLogEntry(LogLevel.FATAL, message, t);
        }

        @Override
        public void info(Object message) {
            this.recordLogEntry(LogLevel.INFO, message, null);
        }

        @Override
        public void info(Object message, Throwable t) {
            this.recordLogEntry(LogLevel.INFO, message, t);
        }

        @Override
        public void trace(Object message) {
            this.recordLogEntry(LogLevel.TRACE, message, null);
        }

        @Override
        public void trace(Object message, Throwable t) {
            this.recordLogEntry(LogLevel.TRACE, message, t);
        }

        @Override
        public void warn(Object message) {
            this.recordLogEntry(LogLevel.WARN, message, null);
        }

        @Override
        public void warn(Object message, Throwable t) {
            this.recordLogEntry(LogLevel.WARN, message, t);
        }

        @Override
        public boolean isDebugEnabled() {
            return this.enabledLevels.contains(LogLevel.DEBUG);
        }

        @Override
        public boolean isErrorEnabled() {
            return this.enabledLevels.contains(LogLevel.ERROR);
        }

        @Override
        public boolean isFatalEnabled() {
            return this.enabledLevels.contains(LogLevel.FATAL);
        }

        @Override
        public boolean isInfoEnabled() {
            return this.enabledLevels.contains(LogLevel.INFO);
        }

        @Override
        public boolean isTraceEnabled() {
            return this.enabledLevels.contains(LogLevel.TRACE);
        }

        @Override
        public boolean isWarnEnabled() {
            return this.enabledLevels.contains(LogLevel.WARN);
        }
    }
    
    @SuppressWarnings("serial")
    static class TestException extends RuntimeException {
        //
    }
    static class TestObject {
        TestObject() {
            throw new TestException();
        }
    }
    
    private void testReset(TestImplementation log) {
        log.reset();
        assertNull("Reset method must clear the last logged level", log.getLastLoggedLevel());
        assertNull("Reset method must clear the last logged message", log.getLastLoggedMessage());
        assertNull("Reset method must clear the last logged exception", log.getLastLoggedException());
        assertTrue("Reset method must clear the enabled log levels", log.getEnabledLevels().isEmpty());
    }
    
    private void testLevel(TestImplementation log,
        LogLevel level,
        Consumer<Object> withoutException, 
        BiConsumer<Object,Throwable> withException,
        Consumer<Supplier<Object>> supplierWithoutException, 
        BiConsumer<Supplier<Object>,Throwable> supplierWithException) {
        
        // System.out.println("Test default methods on level: "+level.name());
        
        Object testMessage = UUID.randomUUID();
        Throwable testException = new TestException();
        
        // be sure the normal methods works as expected before test the default methods
        withoutException.accept(testMessage);
        assertEquals("Dummy implementation should save the level even if the level is diabled ["+level+"]", level, log.getLastLoggedLevel());
        assertEquals("Dummy implementation should save the message even if the level is diabled ["+level+"]", testMessage, log.getLastLoggedMessage());
        assertNull("Dummy implementation should save the exception even if the level is diabled ["+level+"]", log.getLastLoggedException());
        testReset(log);
        
        withException.accept(testMessage, testException);
        assertEquals("Dummy implementation should save the level even if the level is diabled ["+level+"]", level, log.getLastLoggedLevel());
        assertEquals("Dummy implementation should save the message even if the level is diabled ["+level+"]", testMessage, log.getLastLoggedMessage());
        assertEquals("Dummy implementation should save the exception even if the level is diabled ["+level+"]", testException, log.getLastLoggedException());
        testReset(log);

        // debug log level not enabled, message should not appear
        supplierWithoutException.accept(() -> testMessage);
        assertNull("Default method should prevent to save the level if it is disabled ["+level+"]", log.getLastLoggedLevel());
        assertNull("Default method should prevent to save the message if it is disabled ["+level+"]", log.getLastLoggedMessage());
        assertNull("Default method should prevent to save the exception if it is disabled ["+level+"]", log.getLastLoggedException());

        supplierWithException.accept(() -> testMessage, testException);
        assertNull("Default method should prevent to save the level if it is disabled ["+level+"]", log.getLastLoggedLevel());
        assertNull("Default method should prevent to save the message if it is disabled ["+level+"]", log.getLastLoggedMessage());
        assertNull("Default method should prevent to save the exception if it is disabled ["+level+"]", log.getLastLoggedException());
        
        try {
            supplierWithoutException.accept(() -> new TestObject());
        } catch (TestException e) {
            fail("Supplier should not executed with disabled log level");
        }

        try {
            supplierWithException.accept(() -> new TestObject(), testException);
        } catch (TestException e) {
            fail("Supplier should not executed with disabled log level");
        }

        testReset(log);

        // test default methods with enabled log level
        log.enableLogLevel(level);
        assertTrue("Requested level must be enabled before the test execution ["+level+"]", log.getEnabledLevels().contains(level));
        supplierWithoutException.accept(() -> testMessage);
        assertEquals("Stored log level does not match with the expected", level, log.getLastLoggedLevel());
        assertEquals("Stored log message does not match with the expected", testMessage, log.getLastLoggedMessage());
        assertNull("Stored log exception does not match with the expected", log.getLastLoggedException());
        testReset(log);
        
        log.enableLogLevel(level);
        assertTrue("Requested level must be enabled before the test execution ["+level+"]", log.getEnabledLevels().contains(level));
        supplierWithException.accept(() -> testMessage, testException);
        assertEquals("Stored log level does not match with the expected", level, log.getLastLoggedLevel());
        assertEquals("Stored log message does not match with the expected", testMessage, log.getLastLoggedMessage());
        assertEquals("Stored log exception does not match with the expected", testException, log.getLastLoggedException());
        testReset(log);

        // test default methods with enabled log level with NULL supplier
        log.enableLogLevel(level);
        assertTrue("Requested level must be enabled before the test execution ["+level+"]", log.getEnabledLevels().contains(level));
        supplierWithoutException.accept((Supplier<Object>)null);
        assertEquals("Stored log level does not match with the expected", level, log.getLastLoggedLevel());
        assertNull("Stored log message does not match with the expected", log.getLastLoggedMessage());
        assertNull("Stored log exception does not match with the expected", log.getLastLoggedException());
        testReset(log);
        
        log.enableLogLevel(level);
        assertTrue("Requested level must be enabled before the test execution ["+level+"]", log.getEnabledLevels().contains(level));
        supplierWithException.accept((Supplier<Object>)null, testException);
        assertEquals("Stored log level does not match with the expected", level, log.getLastLoggedLevel());
        assertNull("Stored log message does not match with the expected", log.getLastLoggedMessage());
        assertEquals("Stored log exception does not match with the expected", testException, log.getLastLoggedException());
        testReset(log);

        // test default methods with enabled log level with supplier returns null
        log.enableLogLevel(level);
        assertTrue("Requested level must be enabled before the test execution ["+level+"]", log.getEnabledLevels().contains(level));
        supplierWithoutException.accept(() -> null);
        assertEquals("Stored log level does not match with the expected", level, log.getLastLoggedLevel());
        assertNull("Stored log message does not match with the expected", log.getLastLoggedMessage());
        assertNull("Stored log exception does not match with the expected", log.getLastLoggedException());
        testReset(log);
        
        log.enableLogLevel(level);
        assertTrue("Requested level must be enabled before the test execution ["+level+"]", log.getEnabledLevels().contains(level));
        supplierWithException.accept(() -> null, testException);
        assertEquals("Stored log level does not match with the expected", level, log.getLastLoggedLevel());
        assertNull("Stored log message does not match with the expected", log.getLastLoggedMessage());
        assertEquals("Stored log exception does not match with the expected", testException, log.getLastLoggedException());
        testReset(log);

        // test default methods with enabled log level and supplier throws exception
        log.enableLogLevel(level);
        assertTrue("Requested level must be enabled before the test execution ["+level+"]", log.getEnabledLevels().contains(level));
        try {
            supplierWithoutException.accept(() -> new TestObject());
            fail("Supplier should executed and throw an exception");
        } catch (TestException e) {
            //
        }
        testReset(log);

        log.enableLogLevel(level);
        assertTrue("Requested level must be enabled before the test execution ["+level+"]", log.getEnabledLevels().contains(level));
        try {
            supplierWithException.accept(() -> new TestObject(), testException);
            fail("Supplier should executed and throw an exception");
        } catch (TestException e) {
            //
        }
        testReset(log);
    }

    public void testDefaultMethods() {
        Log log = new TestImplementation();
        testLevel((TestImplementation)log, LogLevel.DEBUG, log::debug, log::debug, log::debug, log::debug);
        testLevel((TestImplementation)log, LogLevel.ERROR, log::error, log::error, log::error, log::error);
        testLevel((TestImplementation)log, LogLevel.FATAL, log::fatal, log::fatal, log::fatal, log::fatal);
        testLevel((TestImplementation)log, LogLevel.INFO, log::info, log::info, log::info, log::info);
        testLevel((TestImplementation)log, LogLevel.TRACE, log::trace, log::trace, log::trace, log::trace);
        testLevel((TestImplementation)log, LogLevel.WARN, log::warn, log::warn, log::warn, log::warn);
    }
}
