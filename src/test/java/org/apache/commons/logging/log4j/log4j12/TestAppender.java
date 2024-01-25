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

package org.apache.commons.logging.log4j.log4j12;

import java.util.List;

import org.apache.commons.logging.log4j.StandardTests;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;

/**
 * A custom implementation of {@code org.apache.log4j.Appender} which
 * converts the Log4j-specific log event record into a representation that
 * doesn't have a dependency on log4j and stores that new representation into
 * an external list.
 */

public class TestAppender extends AppenderSkeleton {

    // The set of logged events for this appender
    private final List events;

    /**
     * Constructs a new instance.
     */
    public TestAppender(final List logEvents) {
        events = logEvents;
    }

    @Override
    protected void append(final LoggingEvent event) {
        final StandardTests.LogEvent lev = new StandardTests.LogEvent();

        lev.level = event.getLevel().toString();

        if (event.getMessage() == null) {
            lev.msg = null;
        } else {
            lev.msg = event.getMessage().toString();
        }

        if (event.getThrowableInformation() == null) {
            lev.throwable = null;
        } else {
            lev.throwable = event.getThrowableInformation().getThrowable();
        }

        events.add(lev);
    }

    @Override
    public void close() {
    }

    @Override
    public boolean requiresLayout() {
        return false;
    }

}
