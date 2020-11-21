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
package org.apache.commons.logging.tccl.custom;

import org.apache.commons.logging.Log;

public class MyLog implements Log {

    public MyLog(final String category) {}

    public boolean isDebugEnabled() { return false; }
    public boolean isErrorEnabled() { return false; }
    public boolean isFatalEnabled() { return false; }
    public boolean isInfoEnabled()  { return false; }
    public boolean isTraceEnabled() { return false; }
    public boolean isWarnEnabled()  { return false; }

    public void trace(final Object message) {}
    public void trace(final Object message, final Throwable t) {}
    public void debug(final Object message) {}
    public void debug(final Object message, final Throwable t) {}
    public void info(final Object message) {}
    public void info(final Object message, final Throwable t) {}
    public void warn(final Object message) {}
    public void warn(final Object message, final Throwable t) {}
    public void error(final Object message) {}
    public void error(final Object message, final Throwable t) {}
    public void fatal(final Object message) {}
    public void fatal(final Object message, final Throwable t) {}
}
