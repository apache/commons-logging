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
package org.apache.avalon.framework.logger;

/**
 * This is a dummy class used to compile {@link org.apache.commons.logging.impl.AvalonLogger}, without depending on
 * the deprecated Avalon library.
 */
public interface Logger {
    void debug(String var1);

    void debug(String var1, Throwable var2);

    boolean isDebugEnabled();

    void info(String var1);

    void info(String var1, Throwable var2);

    boolean isInfoEnabled();

    void warn(String var1);

    void warn(String var1, Throwable var2);

    boolean isWarnEnabled();

    void error(String var1);

    void error(String var1, Throwable var2);

    boolean isErrorEnabled();

    void fatalError(String var1);

    void fatalError(String var1, Throwable var2);

    boolean isFatalErrorEnabled();

    Logger getChildLogger(String var1);
}
