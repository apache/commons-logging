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

import junit.framework.TestCase;

/**
 */
public class TestJCL extends TestCase {

	public void testNoConfiguration() throws Exception {
		// basic sanity check
		Log log = LogManager.getLog(TestJCL.class);
		log.trace("Message");
		log.trace("Message", new RuntimeException());
		log.debug("Message");
		log.debug("Message", new RuntimeException());
		log.info("Message");
		log.info("Message", new RuntimeException());
		log.warn("Message");
		log.warn("Message", new RuntimeException());
		log.error("Message");
		log.error("Message", new RuntimeException());
		log.fatal("Message");
		log.fatal("Message", new RuntimeException());
	}
	
	
}
