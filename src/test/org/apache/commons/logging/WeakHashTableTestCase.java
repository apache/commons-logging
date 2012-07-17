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
 
package org.apache.commons.logging;

import junit.framework.TestCase;

import org.apache.commons.logging.impl.WeakHashtable;

public class WeakHashTableTestCase  extends TestCase {

    private static final int WAIT_FOR_THREAD_COMPLETION = 5000; // 5 seconds
    private static final int RUN_LOOPS = 1500;
    private static final int OUTER_LOOP = 100;
    private static final int THREAD_COUNT = 10;
    
    private static WeakHashtable hashtable;

    public static class StupidThread extends Thread {

        public StupidThread(String name){
            super(name);
        }
        
        public void run() {
            for (int i = 0; i < RUN_LOOPS; i++) {
                hashtable.put("key"+":"+(i%10), Boolean.TRUE);
                if(i%50==0) {
                    yield();
                }
            }
        }
    }
    
    public void testLOGGING_119() throws Exception {
        Thread [] t = new Thread[THREAD_COUNT];
        for (int j=1; j <= OUTER_LOOP; j++) {
            hashtable = new WeakHashtable();
            for (int i = 0; i < t.length; i++) {
                t[i] = new StupidThread("Thread:"+i);
                t[i].setDaemon(true); // Otherwise we cannot exit
                t[i].start();
            }
            for (int i = 0; i < t.length; i++) {
                t[i].join(WAIT_FOR_THREAD_COMPLETION);
                if (t[i].isAlive()) {
                    break; // at least one thread is stuck
                }
            }
            int active=0;
            for (int i = 0; i < t.length; i++) {
                if (t[i].isAlive()) {
                    active++;
                }
            }
            if (active > 0) {
                fail("Attempt: "+j+" Stuck threads: "+active);
            }
        }
    }
}
