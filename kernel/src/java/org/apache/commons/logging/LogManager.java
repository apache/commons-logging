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
package org.apache.commons.logging;

/**
 * <p>Simplified superclass for <code>LogFactory</code>.
 * Suitable for static or dynamic binding.
 * </p><p>
 * When dynamically bound, 
 * a single system property {@link #DISCOVERY_CONFIGURATION}
 * is used to choose a discovery implementation 
 * (any <code>LogManager</code> subclass) for the entire environment.
 * For backwards compatibility
 * </p>
 * <p>
 * This class should be simple enough to be compiled on reduced Java 
 * platforms. 
 * </p>
 * <p>
 * The symantics of this basic discovery differ from <code>LogFactory</code>.
 * In the event of a failure within discovery, a safe but minimal log 
 * will be returned.
 * </p>
 * <p>
 * It is of great importance that a truely <strong>minimal</strong>
 * interface is maintained. All members which are not part of the minimal
 * public interface should be private.
 * </p>
 */
public class LogManager {
	
    /** 
     * <code>org.apache.commons.logging.DISCOVERY</code>
     * when set should contain the fully qualified class name
     * of a <code>LogManager</code> implementation
     */
	public static final String DISCOVERY_CONFIGURATION = "org.apache.commons.logging.DISCOVERY";
	
    /**
     * Discovers a <code>LogManager</code> implementation.
     * This is a very simple implementation.
     * The only way of setting this is through a system property.
     * No complex ClassLoader magic is used.
     * It is intended that the implmentations should be
     * bundled within the same jar.
     * @return <code>LogManager</code>
     */
	private static final LogManager discover() {
		LogManager result = null;
		try {
			String discoveryProperty = System.getProperty(DISCOVERY_CONFIGURATION);
			if (discoveryProperty == null) {
				try {
					Class discoveryClass = Class.forName("org.apache.commons.logging.LogFactory");
					result = (LogManager) discoveryClass.newInstance();
				} catch (Throwable t) {
					// swallow
                    // todo: probably want to think about a diagnostic property which would print out useful information here
				}
				
				if (result == null) {
					result = new LogManager();
				}
			} else {
				Class discoveryClass = Class.forName(discoveryProperty);
				result = (LogManager) discoveryClass.newInstance();
			}
		} catch (Throwable t) {
            // todo: probably want to think about a diagnostic property which would print out useful information here
			result = new LogManager();
		}
		return result;
	}
	
    /** Discovery implementation */
	private static final LogManager manager = discover();
	
    /** Singleton fallback logger */
	private static final Log fallbackLog = new MinimalLog();
	
    /**
     * Gets a <code>Log</code> implementation.
     * @param param <code>Object</code> identifying the <code>Log</code>, not null
     * @return <code>Log</code>, not null
     */
	public static Log getLog(Object param) {
		return manager.getLogImpl(param);
	}
	
    /**
     * Gets a <code>Log</code> implementation.
     * Hook for subclassing.
     * @param param  <code>Object</code> identifying the <code>Log</code>, not null
     * @return <code>Log</code>, not null
     */
	protected Log getLogImpl(Object param) {
		return fallbackLog;
	}
	
    /**
     * Implements {@link Log} in a safe but minimal fashion.
     * Logs <code>error</code> and <code>fatal</code> 
     * messages to {@link System#err}, printing
     * stack traces for <code>Throwable</code>'s.
     * All other messages are discarded. 
     */
	private static final class MinimalLog implements Log {

        /** Message discarded */
		public void debug(Object message, Throwable t) {}
        /** Message discarded */
		public void debug(Object message) {}
		
        /** 
         * Logs message to <code>System.err</code>.
         * Stack trace is printed for <code>Throwable</code>.
         */
		public void error(Object message, Throwable t) {
			System.err.println(message);
			t.printStackTrace();
		}
		
        /** Logs message to <code>System.err</code> */
		public void error(Object message) {
			System.err.println(message);
		}
        
        /** 
         * Logs message to <code>System.err</code>.
         * Stack trace is printed for <code>Throwable</code>.
         */
		public void fatal(Object message, Throwable t) {
			System.err.println(message);
			t.printStackTrace();
		}
		
        /** Logs message to <code>System.err</code> */
		public void fatal(Object message) {
			System.err.println(message);
		}

        /** Message discarded */
		public void info(Object message, Throwable t) {}
        /** Message discarded */
		public void info(Object message) {}
        /** Message discarded */
        public void trace(Object message, Throwable t) {}
        /** Message discarded */
        public void trace(Object message) {}
        /** Message discarded */
        public void warn(Object message, Throwable t) {}
        /** Message discarded */
        public void warn(Object message) {}
        
		/** Not enabled */
		public boolean isDebugEnabled() {
			return false;
		}

        /** Enabled */
		public boolean isErrorEnabled() {
			return true;
		}
        
        /** Enabled */
        public boolean isFatalEnabled() {
			return true;
		}
		
        /** Not enabled */
		public boolean isInfoEnabled() {
			return false;
		}

        /** Not enabled */
		public boolean isTraceEnabled() {
			return false;
		}
        
        /** Not enabled */
		public boolean isWarnEnabled() {
			return false;
		}

		/** Indicates that this is the minimal <code>Log</code> implementation */
		public String toString() {
			return "JCL Minimal Log";
		}
}
}
