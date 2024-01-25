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

import java.io.Serializable;

public class LogRecord implements Serializable {

    /**
     * Generated serial version ID.
     */
    private static final long serialVersionUID = -5254831759209770665L;

    public int type;

    public Object message;
    public Throwable t;
    public LogRecord(final int type, final Object message, final Throwable t) {
        this.type = type;
        this.message = message;
        this.t = t;
    }

}
