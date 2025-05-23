/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.commons.logging;

import java.util.Hashtable;

public class AltHashtable extends Hashtable {

    /**
     * Generated serial version ID.
     */
    private static final long serialVersionUID = 8927996458633688095L;

    public static Object lastKey;
    public static Object lastValue;

    @Override
    public Object put(final Object key, final Object value) {
        lastKey = key;
        lastValue = value;
        return super.put(key, value);
    }
}
