package org.apache.commons.logging;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * 
 *
 * 
 * 
 * 
 * 
 */
public class LogTest extends AbstractLogTest
{

    /**
     * 
     * 
     * @param testName
     * 
     */
    public LogTest(String testName)
    {
        super(testName);
    }

    /**
     * 
     * 
     * 
     */
    public Log getLogObject()
    {
        /**
         * Pickup whatever is found/configured!
         */
        return LogFactory.getLog(this.getClass().getName());
    }

    public static void main(String[] args)
    {
            String[] testCaseName = { LogTest.class.getName() };
            junit.textui.TestRunner.main(testCaseName);    
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite();
        
        suite.addTestSuite(LogTest.class);
        
        return suite;
    }
    
}
