package org.apache.commons.logging;

import org.apache.commons.logging.impl.NoOpLog;
import junit.framework.*;

/**
 * 
 *
 * 
 * 
 * 
 * 
 */
public class NoOpLogTest extends AbstractLogTest
{

	/**
	 * 
	 * 
	 * @param testName
	 * 
	 */
	public NoOpLogTest(String testName)
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
		return (Log) new NoOpLog(this.getClass().getName());
	}

	public static void main(String[] args)
	{
        String[] testCaseName = { NoOpLogTest.class.getName() };
        junit.textui.TestRunner.main(testCaseName);	
	}
	
    public static Test suite() {
        TestSuite suite = new TestSuite();
        
        suite.addTestSuite(NoOpLogTest.class);
        
        return suite;
    }
	
}
