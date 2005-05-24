The code here is an experiment based very heavily upon ideas from SLF4J/UGLI but 
including the best of JCL too.

The code is currently fairly rough around the edges. Comments about fundamental
architectural issues are very welcome. Complaints about minor issues like
error-checking or spelling are not likely to be received with enthusiasm!

The LogFactory and Log classes should be compatible with 99.99% of existing
code that uses JCL. Code that *implements* adapters for libraries are simply
not compatible - but that's not likely to be a major issue.

Run "ant jcl-all" to generate the following jars in target:
  * commons-logging-core.jar
  * commons-logging-test.jar
  * commons-logging-nop.jar
  * commons-logging-simple.jar
  * commons-logging-jdk14.jar
  * commons-logging-log4j12.jar

Run "ant test" to execute the (currently very rough) unit tests.

Ordinary applications just deploy commons-logging-core.jar AND the appropriate
jar for whatever library they want to bind to.

In webserver environments:
 * deploy commons-logging-core *only once*, via a classloader that is not lower
   than a commons-logging-xxxx adapter jar is deployed. 
 * deploy commons-logging-xxxx adapter jars as often as desired.
 * if you want code in non-leaf classloaders which use JCL to try
   to log via loglibs in context classloaders, then set system
   property "org.apache.commons.logging.context=true". Without
   this, you get a true "static binding" where code always logs
   to logging libs in their own classloader.


I've got a basic test-harness that demonstrates things, but would
prefer to turn it into unit tests before committing it. That should
be coming soon..

Benefits:
 * no auto-detection of logging libs; you deploy commons-logging-xxx.jar, you
   get logging via xxx. Well, unless you've enabled the "context"
   feature in which case it might go to whatever commons-logging-yyy.jar is
   deployed in the context classloader.
 * very simple
 * very fast
 * very small code
 * JDK1.1 compatible unless you enable the "context" feature. And
   people shouldn't be running container environments on JDK1.1
   anyway!
 * compatible with almost all existing JCL users

Regards,

Simon
