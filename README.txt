<!--
   Licensed to the Apache Software Foundation (ASF) under one or more
  contributor license agreements.  See the NOTICE file distributed with
  this work for additional information regarding copyright ownership.
  The ASF licenses this file to You under the Apache License, Version 2.0
  (the "License"); you may not use this file except in compliance with
  the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
-->

Logging
-------

Getting started:

1) Build the jar file

  If you have the source distribution you will need to build the jar file
  using Maven 2/3.  For instructions on downloading and installing Maven see
  http://maven.apache.org/.

  To build execute the command './mvnw package verify'.
  The jar file will be built in the target directory.
  
  Note: the unit tests are executed during the verify phase and require that
  the respective jar files have been created successfully.

2) Generate the documentation

  Run the './mvnw verify site' command.  The documentation will be written
  to the target/site directory.  The documentation has some examples of
  how to use this package as well as a troubleshooting guide.

3) Create source and binary distributions

  Run the './mvnw verify site assembly:assembly' command.  The source and binary
  distributions are created in the 'target' directory.

4) Use

  Simply include the jar file built in step #1 in your classpath.  Import the
  classes that you want to use and you are ready to go!

