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


import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.impl.SimpleLog;


/**
 * <p>Decorated instance of SimpleLog to expose internal state and
 * support buffered output.</p>
 */

public class DecoratedSimpleLog extends SimpleLog {


    // ------------------------------------------------------------ Constructor


    /**
     * Generated serial version ID.
     */
    private static final long serialVersionUID = 196544280770017153L;

    // Cache of logged records
    protected ArrayList cache = new ArrayList();


    // ------------------------------------------------------------- Properties

    public DecoratedSimpleLog(final String name) {
        super(name);
    }


    // Clear cache
    public void clearCache() {
        cache.clear();
    }


    // Return cache
    public List getCache() {
        return this.cache;
    }


    public String getDateTimeFormat() {
        return dateTimeFormat;
    }


    public DateFormat getDateTimeFormatter() {
        return dateFormatter;
    }


    // ------------------------------------------------------- Protected Methods


    public String getLogName() {
        return logName;
    }


    // ---------------------------------------------------------- Public Methods


    public boolean getShowDateTime() {
        return showDateTime;
    }


    public boolean getShowShortName() {
        return showShortName;
    }


    // Cache logged messages
    @Override
    protected void log(final int type, final Object message, final Throwable t) {

        super.log(type, message, t);
        cache.add(new LogRecord(type, message, t));

    }


}
