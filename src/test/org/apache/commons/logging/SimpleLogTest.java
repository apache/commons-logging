package org.apache.commons.logging;

import org.apache.commons.logging.impl.SimpleLog;
import junit.framework.*;

/**
 * 
 *
 * 
 * 
 * 
 * 
 */
public class SimpleLogTest extends AbstractLogTest
{

	/**
	 * 
	 * 
	 * @param testName
	 * 
	 */
	public SimpleLogTest(String testName)
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
		return (Log) new SimpleLog(this.getClass().getName());
	}

	public static void main(String[] args)
	{
        String[] testCaseName = { SimpleLogTest.class.getName() };
        junit.textui.TestRunner.main(testCaseName);	
    }
	
    public static Test suite() {
        TestSuite suite = new TestSuite();
        
        suite.addTestSuite(SimpleLogTest.class);
        
        return suite;
    }
	
}
