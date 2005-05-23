The code here is an experiment based very heavily upon ideas from SLF4J/UGLI but 
including the best of JCL too.

The code is currently fairly rough around the edges. Complaints about minor 
issues are not likely to be received with enthusiasm!

The LogFactory and Log classes should be compatible with 99.99% of existing
code that uses JCL. Code that *implements* adapters for libraries are simply
not compatible - but that's not likely to be a major issue.

Run "ant jcl-app" to generate the following jars in target:
  * jcl-spi.jar
  * jcl-NoOp.jar
  * jcl-Simple.jar
  * jcl-Jdk14.jar
  * jcl-Log4J12.jar

Ordinary applications just deploy jcl-spi.jar AND the appropriate
jar for whatever library they want to bind to.

In webserver environments:
 * deploy jcl-spi *only once*, via a classloader that is not lower
   than a jcl-XXXX adapter jar is deployed. 
 * deploy jcl-XXXX adapter jars as often as desired.
 * if you want code in non-leaf classloaders which use JCL to try
   to log via loglibs in context classloaders, then set system
   property "org.apache.commons.logging.context=true". Without
   this, you get a true "static binding" where code always logs
   to logging libs in their own classloader.


I've got a basic test-harness that demonstrates things, but would
prefer to turn it into unit tests before committing it. That should
be coming soon..

Benefits:
 * no auto-detection of logging libs; you deploy jcl-xxx.jar, you
   get logging via xxx. Well, unless you've enabled the "context"
   feature in which case it might go to whatever jcl-yyy.jar is
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
