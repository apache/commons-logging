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

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/*
 * Helper class to retrieve the names of all this project artifacts.
 */
public final class Artifacts {

    private static final String ARTIFACT_ID = "commons-logging";
    private static final String VERSION;

    static {
        try (final InputStream pomProperties = Artifacts.class.getResourceAsStream(
                "/META-INF/maven/commons-logging/commons-logging/pom.properties")) {
            final Properties props = new Properties();
            props.load(pomProperties);
            VERSION = props.getProperty("version");
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getMainJarName() {
        return ARTIFACT_ID + "-" + VERSION + ".jar";
    }

    public static String getAdaptersJarName() {
        return ARTIFACT_ID + "-" + VERSION + "-adapters.jar";
    }

    private Artifacts() {
    }
}
