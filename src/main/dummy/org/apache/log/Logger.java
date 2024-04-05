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
package org.apache.log;

/**
 * This is a dummy class used to compile {@link org.apache.commons.logging.impl.LogKitLogger}, without depending on the
 * deprecated LogKit library.
 */
public class Logger {

    public final boolean isDebugEnabled() {
        return false;
    }

    public final void debug(final String message, final Throwable throwable) {
    }

    public final void debug(final String message) {
    }

    public final boolean isInfoEnabled() {
        return false;
    }

    public final void info(final String message, final Throwable throwable) {
    }

    public final void info(final String message) {
    }

    public final boolean isWarnEnabled() {
        return false;
    }

    public final void warn(final String message, final Throwable throwable) {
    }

    public final void warn(final String message) {
    }

    public final boolean isErrorEnabled() {
        return false;
    }

    public final void error(final String message, final Throwable throwable) {
    }

    public final void error(final String message) {

    }

    public final boolean isFatalErrorEnabled() {
        return false;
    }

    public final void fatalError(final String message, final Throwable throwable) {
    }

    public final void fatalError(final String message) {
    }

}
