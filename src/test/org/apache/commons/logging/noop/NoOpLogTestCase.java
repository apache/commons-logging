/*
 * Copyright 2001-2005 The Apache Software Foundation.
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
 
package org.apache.commons.logging.noop;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.impl.NoOpLog;
import org.apache.commons.logging.AbstractLogTest;

/**
 * Tests for NoOpLog logging adapter.
 * <p>
 * This simply applies the tests defined in AbstractLogTest to this class.
 */
public class NoOpLogTestCase extends AbstractLogTest
{

	/**
	 * Override the abstract method from the parent class so that the
     * inherited tests can access the right Log object type. 
	 */
	public Log getLogObject()
	{
		return (Log) new NoOpLog(this.getClass().getName());
	}
}
