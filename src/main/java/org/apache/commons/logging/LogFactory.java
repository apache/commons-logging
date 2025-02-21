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

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Objects;
import java.util.Properties;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import java.util.function.Supplier;

/**
 * Factory for creating {@link Log} instances, with discovery and
 * configuration features similar to that employed by standard Java APIs
 * such as JAXP.
 * <p>
 * <strong>IMPLEMENTATION NOTE</strong> - This implementation is
 * based on the SAXParserFactory and DocumentBuilderFactory implementations
 * (corresponding to the JAXP pluggability APIs) found in Apache Xerces.
 * </p>
 */
public abstract class LogFactory {
    // Implementation note re AccessController usage
    //
    // It is important to keep code invoked via an AccessController to small
    // auditable blocks. Such code must carefully evaluate all user input
    // (parameters, system properties, configuration file contents, etc). As an
    // example, a Log implementation should not write to its log file
    // with an AccessController anywhere in the call stack, otherwise an
    // insecure application could configure the log implementation to write
    // to a protected file using the privileges granted to JCL rather than
    // to the calling application.
    //
    // Under no circumstance should a non-private method return data that is
    // retrieved via an AccessController. That would allow an insecure application
    // to invoke that method and obtain data that it is not permitted to have.
    //
    // Invoking user-supplied code with an AccessController set is not a major
    // issue (for example, invoking the constructor of the class specified by
    // HASHTABLE_IMPLEMENTATION_PROPERTY). That class will be in a different
    // trust domain, and therefore must have permissions to do whatever it
    // is trying to do regardless of the permissions granted to JCL. There is
    // a slight issue in that untrusted code may point that environment variable
    // to another trusted library, in which case the code runs if both that
    // library and JCL have the necessary permissions even when the untrusted
    // caller does not. That's a pretty hard route to exploit though.

    /**
     * The name ({@code priority}) of the key in the configuration file used to
     * specify the priority of that particular configuration file. The associated value
     * is a floating-point number; higher values take priority over lower values.
     */
    public static final String PRIORITY_KEY = "priority";

    /**
     * The name ({@code use_tccl}) of the key in the configuration file used
     * to specify whether logging classes should be loaded via the thread
     * context class loader (TCCL), or not. By default, the TCCL is used.
     */
    public static final String TCCL_KEY = "use_tccl";

    /**
     * The name ({@code org.apache.commons.logging.LogFactory}) of the property
     * used to identify the LogFactory implementation
     * class name. This can be used as a system property, or as an entry in a
     * configuration properties file.
     */
    public static final String FACTORY_PROPERTY = "org.apache.commons.logging.LogFactory";

    private static final String FACTORY_LOG4J_API = "org.apache.commons.logging.impl.Log4jApiLogFactory";

    private static final String LOG4J_TO_SLF4J_BRIDGE = "org.apache.logging.slf4j.SLF4JProvider";

    private static final String FACTORY_SLF4J = "org.apache.commons.logging.impl.Slf4jLogFactory";

    /**
     * The fully qualified class name of the fallback {@code LogFactory}
     * implementation class to use, if no other can be found.
     */
    public static final String FACTORY_DEFAULT = "org.apache.commons.logging.impl.LogFactoryImpl";

    /**
     * The name ({@code commons-logging.properties}) of the properties file to search for.
     */
    public static final String FACTORY_PROPERTIES = "commons-logging.properties";

    /**
     * JDK 1.3+ <a href="https://java.sun.com/j2se/1.3/docs/guide/jar/jar.html#Service%20Provider">
     * 'Service Provider' specification</a>.
     */
    protected static final String SERVICE_ID = "META-INF/services/org.apache.commons.logging.LogFactory";

    /**
     * The name ({@code org.apache.commons.logging.diagnostics.dest})
     * of the property used to enable internal commons-logging
     * diagnostic output, in order to get information on what logging
     * implementations are being discovered, what class loaders they
     * are loaded through, etc.
     * <p>
     * If a system property of this name is set then the value is
     * assumed to be the name of a file. The special strings
     * STDOUT or STDERR (case-sensitive) indicate output to
     * System.out and System.err respectively.
     * <p>
     * Diagnostic logging should be used only to debug problematic
     * configurations and should not be set in normal production use.
     */
    public static final String DIAGNOSTICS_DEST_PROPERTY = "org.apache.commons.logging.diagnostics.dest";

    /**
     * When null (the usual case), no diagnostic output will be
     * generated by LogFactory or LogFactoryImpl. When non-null,
     * interesting events will be written to the specified object.
     */
    private static final PrintStream DIAGNOSTICS_STREAM;

    /**
     * A string that gets prefixed to every message output by the
     * logDiagnostic method, so that users can clearly see which
     * LogFactory class is generating the output.
     */
    private static final String DIAGNOSTICS_PREFIX;

    /**
     * Setting this system property
     * ({@code org.apache.commons.logging.LogFactory.HashtableImpl})
     * value allows the {@code Hashtable} used to store
     * class loaders to be substituted by an alternative implementation.
     * <p>
     * <strong>Note:</strong> {@code LogFactory} will print:
     * </p>
     * <pre>
     * [ERROR] LogFactory: Load of custom hash table failed
     * </pre>
     * <p>
     * to system error and then continue using a standard Hashtable.
     * </p>
     * <p>
     * <strong>Usage:</strong> Set this property when Java is invoked
     * and {@code LogFactory} will attempt to load a new instance
     * of the given implementation class.
     * For example, running the following ant scriplet:
     * </p>
     * <pre>
     *  &lt;java classname="${test.runner}" fork="yes" failonerror="${test.failonerror}"&gt;
     *     ...
     *     &lt;sysproperty
     *        key="org.apache.commons.logging.LogFactory.HashtableImpl"
     *        value="org.apache.commons.logging.AltHashtable"/&gt;
     *  &lt;/java&gt;
     * </pre>
     * <p>
     * will mean that {@code LogFactory} will load an instance of
     * {@code org.apache.commons.logging.AltHashtable}.
     * </p>
     * <p>
     * A typical use case is to allow a custom
     * Hashtable implementation using weak references to be substituted.
     * This will allow class loaders to be garbage collected without
     * the need to release them (on 1.3+ JVMs only, of course ;).
     * </p>
     */
    public static final String HASHTABLE_IMPLEMENTATION_PROPERTY = "org.apache.commons.logging.LogFactory.HashtableImpl";

    /** Name used to load the weak hash table implementation by names. */
    private static final String WEAK_HASHTABLE_CLASSNAME = "org.apache.commons.logging.impl.WeakHashtable";

    /**
     * A reference to the class loader that loaded this class. This is the
     * same as LogFactory.class.getClassLoader(). However computing this
     * value isn't quite as simple as that, as we potentially need to use
     * AccessControllers etc. It's more efficient to compute it once and
     * cache it here.
     */
    private static final WeakReference<ClassLoader> thisClassLoaderRef;

    /**
     * Maximum number of {@link ServiceLoader} errors to ignore, while
     * looking for an implementation.
     */
    private static final int MAX_BROKEN_SERVICES = 3;

    /**
     * The previously constructed {@code LogFactory} instances, keyed by
     * the {@code ClassLoader} with which it was created.
     */
    protected static Hashtable<ClassLoader, LogFactory> factories;

    /**
     * Previously constructed {@code LogFactory} instance as in the
     * {@code factories} map, but for the case where
     * {@code getClassLoader} returns {@code null}.
     * This can happen when:
     * <ul>
     * <li>using JDK1.1 and the calling code is loaded via the system
     *  class loader (very common)</li>
     * <li>using JDK1.2+ and the calling code is loaded via the boot
     *  class loader (only likely for embedded systems work).</li>
     * </ul>
     * Note that {@code factories} is a <em>Hashtable</em> (not a HashMap),
     * and hash tables don't allow null as a key.
     * @deprecated since 1.1.2
     */
    @Deprecated
    protected static volatile LogFactory nullClassLoaderFactory;

    static {
        // note: it's safe to call methods before initDiagnostics (though
        // diagnostic output gets discarded).
        final ClassLoader thisClassLoader = getClassLoader(LogFactory.class);
        thisClassLoaderRef = new WeakReference<>(thisClassLoader);
        // In order to avoid confusion where multiple instances of JCL are
        // being used via different class loaders within the same app, we
        // ensure each logged message has a prefix of form
        // [LogFactory from class loader OID]
        //
        // Note that this prefix should be kept consistent with that
        // in LogFactoryImpl. However here we don't need to output info
        // about the actual *instance* of LogFactory, as all methods that
        // output diagnostics from this class are static.
        String classLoaderName;
        try {
            classLoaderName = thisClassLoader != null ? objectId(thisClassLoader) : "BOOTLOADER";
        } catch (final SecurityException e) {
            classLoaderName = "UNKNOWN";
        }
        DIAGNOSTICS_PREFIX = "[LogFactory from " + classLoaderName + "] ";
        DIAGNOSTICS_STREAM = initDiagnostics();
        logClassLoaderEnvironment(LogFactory.class);
        factories = createFactoryStore();
        logDiagnostic("BOOTSTRAP COMPLETED");
    }

    /**
     * Remember this factory, so later calls to LogFactory.getCachedFactory
     * can return the previously created object (together with all its
     * cached Log objects).
     *
     * @param classLoader should be the current context class loader. Note that
     *  this can be null under some circumstances; this is ok.
     * @param factory should be the factory to cache. This should never be null.
     */
    private static void cacheFactory(final ClassLoader classLoader, final LogFactory factory) {
        // Ideally we would assert(factory != null) here. However reporting
        // errors from within a logging implementation is a little tricky!
        if (factory != null) {
            if (classLoader == null) {
                nullClassLoaderFactory = factory;
            } else {
                factories.put(classLoader, factory);
            }
        }
    }

    /**
     * Creates a LogFactory object or a LogConfigurationException object.
     *
     * @param factoryClassName Factory class.
     * @param classLoader      used to load the specified factory class. This is expected to be either the TCCL or the class loader which loaded this class.
     *                         Note that the class loader which loaded this class might be "null" (for example, the boot loader) for embedded systems.
     * @return either a LogFactory object or a LogConfigurationException object.
     * @since 1.1
     */
    protected static Object createFactory(final String factoryClassName, final ClassLoader classLoader) {
        // This will be used to diagnose bad configurations
        // and allow a useful message to be sent to the user
        Class<?> logFactoryClass = null;
        try {
            if (classLoader != null) {
                try {
                    // First the given class loader param (thread class loader)

                    // Warning: must typecast here & allow exception
                    // to be generated/caught & recast properly.
                    logFactoryClass = classLoader.loadClass(factoryClassName);
                    if (LogFactory.class.isAssignableFrom(logFactoryClass)) {
                        if (isDiagnosticsEnabled()) {
                            logDiagnostic("Loaded class " + logFactoryClass.getName() + " from class loader " + objectId(classLoader));
                        }
                    } else //
                    // This indicates a problem with the ClassLoader tree.
                    // An incompatible ClassLoader was used to load the
                    // implementation.
                    // As the same classes
                    // must be available in multiple class loaders,
                    // it is very likely that multiple JCL jars are present.
                    // The most likely fix for this
                    // problem is to remove the extra JCL jars from the
                    // ClassLoader hierarchy.
                    //
                    if (isDiagnosticsEnabled()) {
                        logDiagnostic("Factory class " + logFactoryClass.getName() + " loaded from class loader " + objectId(logFactoryClass.getClassLoader())
                                + " does not extend '" + LogFactory.class.getName() + "' as loaded by this class loader.");
                        logHierarchy("[BAD CL TREE] ", classLoader);
                    }
                    // Force a ClassCastException
                    return LogFactory.class.cast(logFactoryClass.getConstructor().newInstance());

                } catch (final ClassNotFoundException ex) {
                    if (classLoader == thisClassLoaderRef.get()) {
                        // Nothing more to try, onwards.
                        if (isDiagnosticsEnabled()) {
                            logDiagnostic("Unable to locate any class called '" + factoryClassName + "' via class loader " + objectId(classLoader));
                        }
                        throw ex;
                    }
                    // ignore exception, continue
                } catch (final NoClassDefFoundError e) {
                    if (classLoader == thisClassLoaderRef.get()) {
                        // Nothing more to try, onwards.
                        if (isDiagnosticsEnabled()) {
                            logDiagnostic("Class '" + factoryClassName + "' cannot be loaded" + " via class loader " + objectId(classLoader)
                                    + " - it depends on some other class that cannot be found.");
                        }
                        throw e;
                    }
                    // ignore exception, continue
                } catch (final ClassCastException e) {
                    if (classLoader == thisClassLoaderRef.get()) {
                        // There's no point in falling through to the code below that
                        // tries again with thisClassLoaderRef, because we've just tried
                        // loading with that loader (not the TCCL). Just throw an
                        // appropriate exception here.
                        final boolean implementsLogFactory = implementsLogFactory(logFactoryClass);
                        //
                        // Construct a good message: users may not actual expect that a custom implementation
                        // has been specified. Several well known containers use this mechanism to adapt JCL
                        // to their native logging system.
                        //
                        final StringBuilder msg = new StringBuilder();
                        msg.append("The application has specified that a custom LogFactory implementation should be used but Class '");
                        msg.append(factoryClassName);
                        msg.append("' cannot be converted to '");
                        msg.append(LogFactory.class.getName());
                        msg.append("'. ");
                        if (implementsLogFactory) {
                            msg.append("The conflict is caused by the presence of multiple LogFactory classes in incompatible class loaders. Background can");
                            msg.append(" be found in https://commons.apache.org/logging/tech.html. If you have not explicitly specified a custom LogFactory");
                            msg.append(" then it is likely that the container has set one without your knowledge. In this case, consider using the ");
                            msg.append("commons-logging-adapters.jar file or specifying the standard LogFactory from the command line. ");
                        } else {
                            msg.append("Please check the custom implementation. ");
                        }
                        msg.append("Help can be found at https://commons.apache.org/logging/troubleshooting.html.");
                        logDiagnostic(msg.toString());
                        throw new ClassCastException(msg.toString());
                    }
                    // Ignore exception, continue. Presumably the class loader was the
                    // TCCL; the code below will try to load the class via thisClassLoaderRef.
                    // This will handle the case where the original calling class is in
                    // a shared classpath but the TCCL has a copy of LogFactory and the
                    // specified LogFactory implementation; we will fall back to using the
                    // LogFactory implementation from the same class loader as this class.
                    //
                    // Issue: this doesn't handle the reverse case, where this LogFactory
                    // is in the webapp, and the specified LogFactory implementation is
                    // in a shared classpath. In that case:
                    // (a) the class really does implement LogFactory (bad log msg above)
                    // (b) the fallback code will result in exactly the same problem.
                }
            }

            /*
             * At this point, either classLoader == null, OR classLoader was unable to load factoryClass.
             *
             * In either case, we call Class.forName, which is equivalent to LogFactory.class.getClassLoader().load(name), that is, we ignore the class loader
             * parameter the caller passed, and fall back to trying the class loader associated with this class. See the Javadoc for the newFactory method for
             * more info on the consequences of this.
             *
             * Notes: * LogFactory.class.getClassLoader() may return 'null' if LogFactory is loaded by the bootstrap class loader.
             */
            // Warning: must typecast here & allow exception
            // to be generated/caught & recast properly.
            if (isDiagnosticsEnabled()) {
                logDiagnostic(
                        "Unable to load factory class via class loader " + objectId(classLoader) + " - trying the class loader associated with this LogFactory.");
            }
            logFactoryClass = Class.forName(factoryClassName);
            // Force a ClassCastException
            return LogFactory.class.cast(logFactoryClass.getConstructor().newInstance());
        } catch (final Exception e) {
            // Check to see if we've got a bad configuration
            if (isDiagnosticsEnabled()) {
                logDiagnostic("Unable to create LogFactory instance.");
            }
            if (logFactoryClass != null && !LogFactory.class.isAssignableFrom(logFactoryClass)) {
                return new LogConfigurationException("The chosen LogFactory implementation does not extend LogFactory. Please check your configuration.", e);
            }
            return new LogConfigurationException(e);
        }
    }

    /**
     * Creates the hash table which will be used to store a map of
     * (context class loader -> logfactory-object). Version 1.2+ of Java
     * supports "weak references", allowing a custom Hashtable class
     * to be used which uses only weak references to its keys. Using weak
     * references can fix memory leaks on webapp unload in some cases (though
     * not all). Version 1.1 of Java does not support weak references, so we
     * must dynamically determine which we are using. And just for fun, this
     * code also supports the ability for a system property to specify an
     * arbitrary Hashtable implementation name.
     * <p>
     * Note that the correct way to ensure no memory leaks occur is to ensure
     * that LogFactory.release(contextClassLoader) is called whenever a
     * webapp is undeployed.
     * </p>
     */
    private static Hashtable<ClassLoader, LogFactory> createFactoryStore() {
        Hashtable<ClassLoader, LogFactory> result = null;
        String storeImplementationClass;
        try {
            storeImplementationClass = getSystemProperty(HASHTABLE_IMPLEMENTATION_PROPERTY, null);
        } catch (final SecurityException ex) {
            // Permissions don't allow this to be accessed. Default to the "modern"
            // weak hash table implementation if it is available.
            storeImplementationClass = null;
        }
        if (storeImplementationClass == null) {
            storeImplementationClass = WEAK_HASHTABLE_CLASSNAME;
        }
        try {
            final Class<Hashtable<ClassLoader, LogFactory>> implementationClass = (Class<Hashtable<ClassLoader, LogFactory>>) Class
                    .forName(storeImplementationClass);
            result = implementationClass.getConstructor().newInstance();
        } catch (final Throwable t) {
            handleThrowable(t); // may re-throw t
            // ignore
            if (!WEAK_HASHTABLE_CLASSNAME.equals(storeImplementationClass)) {
                // if the user's trying to set up a custom implementation, give a clue
                if (isDiagnosticsEnabled()) {
                    // use internal logging to issue the warning
                    logDiagnostic("[ERROR] LogFactory: Load of custom Hashtable failed");
                } else {
                    // we *really* want this output, even if diagnostics weren't
                    // explicitly enabled by the user.
                    System.err.println("[ERROR] LogFactory: Load of custom Hashtable failed");
                }
            }
        }
        if (result == null) {
            result = new Hashtable<>();
        }
        return result;
    }

    /**
     * Gets the thread context class loader if available; otherwise return null.
     * <p>
     * Most/all code should call getContextClassLoaderInternal rather than
     * calling this method directly.
     * </p>
     * <p>
     * The thread context class loader is available for JDK 1.2
     * or later, if certain security conditions are met.
     * </p>
     * <p>
     * Note that no internal logging is done within this method because
     * this method is called every time LogFactory.getLogger() is called,
     * and we don't want too much output generated here.
     * </p>
     *
     * @throws LogConfigurationException if a suitable class loader
     *  cannot be identified.
     * @return the thread's context class loader or {@code null} if the Java security
     *  policy forbids access to the context class loader from one of the classes
     *  in the current call stack.
     * @since 1.1
     */
    protected static ClassLoader directGetContextClassLoader() throws LogConfigurationException {
        ClassLoader classLoader = null;
        try {
            classLoader = Thread.currentThread().getContextClassLoader();
        } catch (final SecurityException ignore) {
            // getContextClassLoader() throws SecurityException when
            // the context class loader isn't an ancestor of the
            // calling class's class loader, or if security
            // permissions are restricted.
            //
            // We ignore this exception to be consistent with the previous
            // behavior (e.g. 1.1.3 and earlier).
        }
        // Return the selected class loader
        return classLoader;
    }

    /**
     * Gets a cached log factory (keyed by contextClassLoader)
     *
     * @param contextClassLoader is the context class loader associated
     * with the current thread. This allows separate LogFactory objects
     * per component within a container, provided each component has
     * a distinct context class loader set. This parameter may be null
     * in JDK1.1, and in embedded systems where jcl-using code is
     * placed in the bootclasspath.
     *
     * @return the factory associated with the specified class loader if
     *  one has previously been created, or null if this is the first time
     *  we have seen this particular class loader.
     */
    private static LogFactory getCachedFactory(final ClassLoader contextClassLoader) {
        if (contextClassLoader == null) {
            // We have to handle this specially, as factories is a Hashtable
            // and those don't accept null as a key value.
            //
            // nb: nullClassLoaderFactory might be null. That's ok.
            return nullClassLoaderFactory;
        }
        return factories.get(contextClassLoader);
    }

    /**
     * Safely get access to the class loader for the specified class.
     * <p>
     * Theoretically, calling getClassLoader can throw a security exception,
     * and so should be done under an AccessController in order to provide
     * maximum flexibility. However in practice people don't appear to use
     * security policies that forbid getClassLoader calls. So for the moment
     * all code is written to call this method rather than Class.getClassLoader,
     * so that we could put AccessController stuff in this method without any
     * disruption later if we need to.
     * </p>
     * <p>
     * Even when using an AccessController, however, this method can still
     * throw SecurityException. Commons Logging basically relies on the
     * ability to access class loaders. A policy that forbids all
     * class loader access will also prevent commons-logging from working:
     * currently this method will throw an exception preventing the entire app
     * from starting up. Maybe it would be good to detect this situation and
     * just disable all commons-logging? Not high priority though - as stated
     * above, security policies that prevent class loader access aren't common.
     * </p>
     * <p>
     * Note that returning an object fetched via an AccessController would
     * technically be a security flaw anyway; untrusted code that has access
     * to a trusted JCL library could use it to fetch the class loader for
     * a class even when forbidden to do so directly.
     * </p>
     *
     * @param clazz Class.
     * @return a ClassLoader.
     * @since 1.1
     */
    protected static ClassLoader getClassLoader(final Class<?> clazz) {
        try {
            return clazz.getClassLoader();
        } catch (final SecurityException ex) {
            logDiagnostic(() -> "Unable to get class loader for class '" + clazz + "' due to security restrictions - " + ex.getMessage());
            throw ex;
        }
    }

    /**
     * Gets a user-provided configuration file.
     * <p>
     * The classpath of the specified classLoader (usually the context class loader)
     * is searched for properties files of the specified name. If none is found,
     * null is returned. If more than one is found, then the file with the greatest
     * value for its PRIORITY property is returned. If multiple files have the
     * same PRIORITY value then the first in the classpath is returned.
     * </p>
     * <p>
     * This differs from the 1.0.x releases; those always use the first one found.
     * However as the priority is a new field, this change is backwards compatible.
     * </p>
     * <p>
     * The purpose of the priority field is to allow a webserver administrator to
     * override logging settings in all webapps by placing a commons-logging.properties
     * file in a shared classpath location with a priority > 0; this overrides any
     * commons-logging.properties files without priorities which are in the
     * webapps. Webapps can also use explicit priorities to override a configuration
     * file in the shared classpath if needed.
     * </p>
     */
    private static Properties getConfigurationFile(final ClassLoader classLoader, final String fileName) {
        Properties props = null;
        double priority = 0.0;
        URL propsUrl = null;
        try {
            final Enumeration<URL> urls = getResources(classLoader, fileName);
            if (urls == null) {
                return null;
            }
            while (urls.hasMoreElements()) {
                final URL url = urls.nextElement();
                final Properties newProps = getProperties(url);
                if (newProps != null) {
                    if (props == null) {
                        propsUrl = url;
                        props = newProps;
                        final String priorityStr = props.getProperty(PRIORITY_KEY);
                        priority = 0.0;
                        if (priorityStr != null) {
                            priority = Double.parseDouble(priorityStr);
                        }
                        if (isDiagnosticsEnabled()) {
                            logDiagnostic("[LOOKUP] Properties file found at '" + url + "'" + " with priority " + priority);
                        }
                    } else {
                        final String newPriorityStr = newProps.getProperty(PRIORITY_KEY);
                        double newPriority = 0.0;
                        if (newPriorityStr != null) {
                            newPriority = Double.parseDouble(newPriorityStr);
                        }
                        if (newPriority > priority) {
                            if (isDiagnosticsEnabled()) {
                                logDiagnostic("[LOOKUP] Properties file at '" + url + "'" + " with priority " + newPriority + " overrides file at '" + propsUrl
                                        + "'" + " with priority " + priority);
                            }
                            propsUrl = url;
                            props = newProps;
                            priority = newPriority;
                        } else if (isDiagnosticsEnabled()) {
                            logDiagnostic("[LOOKUP] Properties file at '" + url + "'" + " with priority " + newPriority + " does not override file at '"
                                    + propsUrl + "'" + " with priority " + priority);
                        }
                    }

                }
            }
        } catch (final SecurityException e) {
            logDiagnostic("SecurityException thrown while trying to find/read config files.");
        }
        if (isDiagnosticsEnabled()) {
            if (props == null) {
                logDiagnostic("[LOOKUP] No properties file of name '" + fileName + "' found.");
            } else {
                logDiagnostic("[LOOKUP] Properties file of name '" + fileName + "' found at '" + propsUrl + '"');
            }
        }
        return props;
    }

    /**
     * Gets the current context class loader.
     * <p>
     * In versions prior to 1.1, this method did not use an AccessController.
     * In version 1.1, an AccessController wrapper was incorrectly added to
     * this method, causing a minor security flaw.
     * </p>
     * <p>
     * In version 1.1.1 this change was reverted; this method no longer uses
     * an AccessController. User code wishing to obtain the context class loader
     * must invoke this method via AccessController.doPrivileged if it needs
     * support for that.
     * </p>
     *
     * @return the context class loader associated with the current thread,
     *  or null if security doesn't allow it.
     * @throws LogConfigurationException if there was some weird error while
     *  attempting to get the context class loader.
     */
    protected static ClassLoader getContextClassLoader() throws LogConfigurationException {
        return directGetContextClassLoader();
    }

    /**
     * Calls {@link LogFactory#directGetContextClassLoader()} under the control of an
     * AccessController class. This means that Java code running under a
     * security manager that forbids access to ClassLoaders will still work
     * if this class is given appropriate privileges, even when the caller
     * doesn't have such privileges. Without using an AccessController, the
     * the entire call stack must have the privilege before the call is
     * allowed.
     *
     * @return the context class loader associated with the current thread,
     *  or null if security doesn't allow it.
     * @throws LogConfigurationException if there was some weird error while
     *  attempting to get the context class loader.
     */
    private static ClassLoader getContextClassLoaderInternal() throws LogConfigurationException {
        return AccessController.doPrivileged((PrivilegedAction<ClassLoader>) LogFactory::directGetContextClassLoader);
    }

    /**
     * Constructs (if necessary) and return a {@code LogFactory} instance, using the following ordered lookup procedure to determine the name of the
     * implementation class to be loaded.
     * <ul>
     * <li>The {@code org.apache.commons.logging.LogFactory} system property.</li>
     * <li>The JDK 1.3 Service Discovery mechanism</li>
     * <li>Use the properties file {@code commons-logging.properties} file, if found in the class path of this class. The configuration file is in standard
     * {@link java.util.Properties} format and contains the fully qualified name of the implementation class with the key being the system property defined
     * above.</li>
     * <li>Fall back to a default implementation class ({@code org.apache.commons.logging.impl.LogFactoryImpl}).</li>
     * </ul>
     * <p>
     * <em>NOTE</em> - If the properties file method of identifying the {@code LogFactory} implementation class is utilized, all of the properties defined in
     * this file will be set as configuration attributes on the corresponding {@code LogFactory} instance.
     * </p>
     * <p>
     * <em>NOTE</em> - In a multi-threaded environment it is possible that two different instances will be returned for the same class loader environment.
     * </p>
     *
     * @return a {@code LogFactory}.
     * @throws LogConfigurationException if the implementation class is not available or cannot be instantiated.
     */
    public static LogFactory getFactory() throws LogConfigurationException {
        // Identify the class loader we will be using
        final ClassLoader contextClassLoader = getContextClassLoaderInternal();

        // This is an odd enough situation to report about. This
        // output will be a nuisance on JDK1.1, as the system
        // class loader is null in that environment.
        if (contextClassLoader == null) {
            logDiagnostic("Context class loader is null.");
        }

        // Return any previously registered factory for this class loader
        LogFactory factory = getCachedFactory(contextClassLoader);
        if (factory != null) {
            return factory;
        }

        if (isDiagnosticsEnabled()) {
            logDiagnostic(
                    "[LOOKUP] LogFactory implementation requested for the first time for context class loader " +
                    objectId(contextClassLoader));
            logHierarchy("[LOOKUP] ", contextClassLoader);
        }

        // Load properties file.
        //
        // If the properties file exists, then its contents are used as
        // "attributes" on the LogFactory implementation class. One particular
        // property may also control which LogFactory concrete subclass is
        // used, but only if other discovery mechanisms fail.
        //
        // As the properties file (if it exists) will be used one way or
        // another in the end we may as well look for it first.

        final Properties props = getConfigurationFile(contextClassLoader, FACTORY_PROPERTIES);

        // Determine whether we will be using the thread context class loader to
        // load logging classes or not by checking the loaded properties file (if any).
        boolean useTccl = contextClassLoader != null;
        if (props != null) {
            final String useTCCLStr = props.getProperty(TCCL_KEY);
            useTccl &= useTCCLStr == null || Boolean.parseBoolean(useTCCLStr);
        }
        // If TCCL is still enabled at this point, we check if it resolves this class
        if (useTccl) {
            try {
                if (!LogFactory.class.equals(Class.forName(LogFactory.class.getName(), false, contextClassLoader))) {
                    logDiagnostic(() -> "The class " + LogFactory.class.getName() + " loaded by the context class loader " + objectId(contextClassLoader)
                            + " and this class differ. Disabling the usage of the context class loader."
                            + "Background can be found in https://commons.apache.org/logging/tech.html. ");
                    logHierarchy("[BAD CL TREE] ", contextClassLoader);
                    useTccl = false;
                }
            } catch (final ClassNotFoundException ignored) {
                logDiagnostic(() -> "The class " + LogFactory.class.getName() + " is not present in the the context class loader "
                        + objectId(contextClassLoader) + ". Disabling the usage of the context class loader."
                        + "Background can be found in https://commons.apache.org/logging/tech.html. ");
                logHierarchy("[BAD CL TREE] ", contextClassLoader);
                useTccl = false;
            }
        }
        final ClassLoader baseClassLoader = useTccl ? contextClassLoader : thisClassLoaderRef.get();

        // Determine which concrete LogFactory subclass to use.
        // First, try a global system property
        logDiagnostic(() -> "[LOOKUP] Looking for system property [" + FACTORY_PROPERTY +
                      "] to define the LogFactory subclass to use...");

        try {
            final String factoryClass = getSystemProperty(FACTORY_PROPERTY, null);
            if (factoryClass != null) {
                logDiagnostic(() -> "[LOOKUP] Creating an instance of LogFactory class '" + factoryClass +
                              "' as specified by system property " + FACTORY_PROPERTY);
                factory = newFactory(factoryClass, baseClassLoader, contextClassLoader);
            } else {
                logDiagnostic(() -> "[LOOKUP] No system property [" + FACTORY_PROPERTY + "] defined.");
            }
        } catch (final SecurityException e) {
            logDiagnostic(() -> "[LOOKUP] A security exception occurred while trying to create an instance of the custom factory class" + ": ["
                    + trim(e.getMessage()) + "]. Trying alternative implementations...");
            // ignore
        } catch (final RuntimeException e) {
            // This is not consistent with the behavior when a bad LogFactory class is
            // specified in a services file.
            //
            // One possible exception that can occur here is a ClassCastException when
            // the specified class wasn't castable to this LogFactory type.
            logDiagnostic(() -> "[LOOKUP] An exception occurred while trying to create an instance of the custom factory class: [" + trim(e.getMessage())
                    + "] as specified by a system property.");
            throw e;
        }
        //
        // Second, try to find a service by using the JDK 1.3 class
        // discovery mechanism, which involves putting a file with the name
        // of an interface class in the META-INF/services directory, where the
        // contents of the file is a single line specifying a concrete class
        // that implements the desired interface.
        if (factory == null) {
            logDiagnostic("[LOOKUP] Using ServiceLoader  to define the LogFactory subclass to use...");
            try {
                final ServiceLoader<LogFactory> serviceLoader = ServiceLoader.load(LogFactory.class, baseClassLoader);
                final Iterator<LogFactory> iterator = serviceLoader.iterator();

                int i = MAX_BROKEN_SERVICES;
                while (factory == null && i-- > 0) {
                    try {
                        if (iterator.hasNext()) {
                            factory = iterator.next();
                        }
                    } catch (final ServiceConfigurationError | LinkageError ex) {
                        logDiagnostic(() -> "[LOOKUP] An exception occurred while trying to find an instance of LogFactory: [" + trim(ex.getMessage())
                                + "]. Trying alternative implementations...");
                    }
                }
            } catch (final Exception ex) {
                // note: if the specified LogFactory class wasn't compatible with LogFactory
                // for some reason, a ClassCastException will be caught here, and attempts will
                // continue to find a compatible class.
                logDiagnostic(() -> "[LOOKUP] A security exception occurred while trying to create an instance of the custom factory class: ["
                        + trim(ex.getMessage()) + "]. Trying alternative implementations...");
                // ignore
            }
        }
        //
        // Third try looking into the properties file read earlier (if found)
        if (factory == null) {
            if (props != null) {
                logDiagnostic(() ->
                    "[LOOKUP] Looking in properties file for entry with key '" + FACTORY_PROPERTY +
                    "' to define the LogFactory subclass to use...");
                final String factoryClass = props.getProperty(FACTORY_PROPERTY);
                if (factoryClass != null) {
                    logDiagnostic(() ->
                        "[LOOKUP] Properties file specifies LogFactory subclass '" + factoryClass + "'");
                    factory = newFactory(factoryClass, baseClassLoader, contextClassLoader);
                    // TODO: think about whether we need to handle exceptions from newFactory
                } else {
                    logDiagnostic("[LOOKUP] Properties file has no entry specifying LogFactory subclass.");
                }
            } else {
                logDiagnostic("[LOOKUP] No properties file available to determine LogFactory subclass from..");
            }
        }
        //
        // Fourth, try one of the three provided factories first from the specified classloader
        // and then from the current one.
        if (factory == null) {
            factory = newStandardFactory(baseClassLoader);
        }
        if (factory == null && baseClassLoader != thisClassLoaderRef.get()) {
            factory = newStandardFactory(thisClassLoaderRef.get());
        }
        if (factory != null) {
            if (isDiagnosticsEnabled()) {
                logDiagnostic("Created object " + objectId(factory) + " to manage class loader " + objectId(contextClassLoader));
            }
        } else {
            logDiagnostic(() ->
                "[LOOKUP] Loading the default LogFactory implementation '" + FACTORY_DEFAULT +
                "' via the same class loader that loaded this LogFactory class (ie not looking in the context class loader).");
            // Note: unlike the above code which can try to load custom LogFactory
            // implementations via the TCCL, we don't try to load the default LogFactory
            // implementation via the context class loader because:
            // * that can cause problems (see comments in newFactory method)
            // * no-one should be customizing the code of the default class
            // Yes, we do give up the ability for the child to ship a newer
            // version of the LogFactoryImpl class and have it used dynamically
            // by an old LogFactory class in the parent, but that isn't
            // necessarily a good idea anyway.
            factory = newFactory(FACTORY_DEFAULT, thisClassLoaderRef.get(), contextClassLoader);
        }
        if (factory != null) {
            /**
             * Always cache using context class loader.
             */
            cacheFactory(contextClassLoader, factory);
            if (props != null) {
                final Enumeration<?> names = props.propertyNames();
                while (names.hasMoreElements()) {
                    final String name = Objects.toString(names.nextElement(), null);
                    final String value = props.getProperty(name);
                    factory.setAttribute(name, value);
                }
            }
        }
        return factory;
    }

    /**
     * Gets a named logger, without the application having to care about factories.
     *
     * @param clazz Class from which a log name will be derived
     * @return a named logger.
     * @throws LogConfigurationException if a suitable {@code Log} instance cannot be returned
     */
    public static Log getLog(final Class<?> clazz) throws LogConfigurationException {
        return getFactory().getInstance(clazz);
    }

    /**
     * Gets a named logger, without the application having to care about factories.
     *
     * @param name Logical name of the {@code Log} instance to be returned (the meaning of this name is only known to the underlying logging implementation that
     *             is being wrapped)
     * @return a named logger.
     * @throws LogConfigurationException if a suitable {@code Log} instance cannot be returned
     */
    public static Log getLog(final String name) throws LogConfigurationException {
        return getFactory().getInstance(name);
    }

    /**
     * Given a URL that refers to a .properties file, load that file.
     * This is done under an AccessController so that this method will
     * succeed when this JAR file is privileged but the caller is not.
     * This method must therefore remain private to avoid security issues.
     * <p>
     * {@code Null} is returned if the URL cannot be opened.
     * </p>
     */
    private static Properties getProperties(final URL url) {
        return AccessController.doPrivileged((PrivilegedAction<Properties>) () -> {
            // We must ensure that useCaches is set to false, as the
            // default behavior of java is to cache file handles, and
            // this "locks" files, preventing hot-redeploy on windows.
            try {
                final URLConnection connection = url.openConnection();
                connection.setUseCaches(false);
                try (InputStream stream = connection.getInputStream()) {
                    if (stream != null) {
                        final Properties props = new Properties();
                        props.load(stream);
                        return props;
                    }
                } catch (final IOException e) {
                    logDiagnostic(() -> "Unable to close stream for URL " + url);
                }
            } catch (final IOException e) {
                logDiagnostic(() -> "Unable to read URL " + url);
            }

            return null;
        });
    }

    /**
     * Given a file name, return an enumeration of URLs pointing to
     * all the occurrences of that file name in the classpath.
     * <p>
     * This is just like ClassLoader.getResources except that the
     * operation is done under an AccessController so that this method will
     * succeed when this jarfile is privileged but the caller is not.
     * This method must therefore remain private to avoid security issues.
     * </p>
     * <p>
     * If no instances are found, an Enumeration is returned whose
     * hasMoreElements method returns false (ie an "empty" enumeration).
     * If resources could not be listed for some reason, null is returned.
     * </p>
     */
    private static Enumeration<URL> getResources(final ClassLoader loader, final String name) {
        return AccessController.doPrivileged((PrivilegedAction<Enumeration<URL>>) () -> {
            try {
                if (loader != null) {
                    return loader.getResources(name);
                }
                return ClassLoader.getSystemResources(name);
            } catch (final IOException e) {
                logDiagnostic(() -> "Exception while trying to find configuration file " + name + ":" + e.getMessage());
                return null;
            } catch (final NoSuchMethodError e) {
                // we must be running on a 1.1 JVM which doesn't support
                // ClassLoader.getSystemResources; just return null in
                // this case.
                return null;
            }
        });
    }

    /**
     * Read the specified system property, using an AccessController so that
     * the property can be read if JCL has been granted the appropriate
     * security rights even if the calling code has not.
     * <p>
     * Take care not to expose the value returned by this method to the
     * calling application in any way; otherwise the calling app can use that
     * info to access data that should not be available to it.
     * </p>
     */
    private static String getSystemProperty(final String key, final String def)
            throws SecurityException {
        return AccessController.doPrivileged((PrivilegedAction<String>) () -> System.getProperty(key, def));
    }

    /**
     * Checks whether the supplied Throwable is one that needs to be
     * re-thrown and ignores all others.
     *
     * The following errors are re-thrown:
     * <ul>
     *   <li>ThreadDeath</li>
     *   <li>VirtualMachineError</li>
     * </ul>
     *
     * @param t the Throwable to check
     */
    protected static void handleThrowable(final Throwable t) {
        if (t instanceof ThreadDeath) {
            throw (ThreadDeath) t;
        }
        if (t instanceof VirtualMachineError) {
            throw (VirtualMachineError) t;
        }
        // All other instances of Throwable will be silently ignored
    }

    /**
     * Determines whether the given class actually implements {@code LogFactory}.
     * Diagnostic information is also logged.
     * <p>
     * <strong>Usage:</strong> to diagnose whether a class loader conflict is the cause
     * of incompatibility. The test used is whether the class is assignable from
     * the {@code LogFactory} class loaded by the class's class loader.
     * @param logFactoryClass {@code Class} which may implement {@code LogFactory}
     * @return true if the {@code logFactoryClass} does extend
     * {@code LogFactory} when that class is loaded via the same
     * class loader that loaded the {@code logFactoryClass}.
     * </p>
     */
    private static boolean implementsLogFactory(final Class<?> logFactoryClass) {
        boolean implementsLogFactory = false;
        if (logFactoryClass != null) {
            try {
                final ClassLoader logFactoryClassLoader = logFactoryClass.getClassLoader();
                if (logFactoryClassLoader == null) {
                    logDiagnostic("[CUSTOM LOG FACTORY] was loaded by the boot class loader");
                } else {
                    logHierarchy("[CUSTOM LOG FACTORY] ", logFactoryClassLoader);
                    final Class<?> factoryFromCustomLoader = Class.forName("org.apache.commons.logging.LogFactory", false, logFactoryClassLoader);
                    implementsLogFactory = factoryFromCustomLoader.isAssignableFrom(logFactoryClass);
                    final String logFactoryClassName = logFactoryClass.getName();
                    if (implementsLogFactory) {
                        logDiagnostic(() -> "[CUSTOM LOG FACTORY] " + logFactoryClassName + " implements LogFactory but was loaded by an incompatible class loader.");
                    } else {
                        logDiagnostic(() -> "[CUSTOM LOG FACTORY] " + logFactoryClassName + " does not implement LogFactory.");
                    }
                }
            } catch (final SecurityException e) {
                //
                // The application is running within a hostile security environment.
                // This will make it very hard to diagnose issues with JCL.
                // Consider running less securely whilst debugging this issue.
                //
                logDiagnostic(
                        () -> "[CUSTOM LOG FACTORY] SecurityException caught trying to determine whether the compatibility was caused by a class loader conflict: "
                                + e.getMessage());
            } catch (final LinkageError e) {
                //
                // This should be an unusual circumstance.
                // LinkageError's usually indicate that a dependent class has incompatibly changed.
                // Another possibility may be an exception thrown by an initializer.
                // Time for a clean rebuild?
                //
                logDiagnostic(
                        () -> "[CUSTOM LOG FACTORY] LinkageError caught trying to determine whether the compatibility was caused by a class loader conflict: "
                                + e.getMessage());
            } catch (final ClassNotFoundException e) {
                //
                // LogFactory cannot be loaded by the class loader which loaded the custom factory implementation.
                // The custom implementation is not viable until this is corrected.
                // Ensure that the JCL jar and the custom class are available from the same class loader.
                // Running with diagnostics on should give information about the class loaders used
                // to load the custom factory.
                //
                logDiagnostic(() -> "[CUSTOM LOG FACTORY] LogFactory class cannot be loaded by the class loader which loaded "
                        + "the custom LogFactory implementation. Is the custom factory in the right class loader?");
            }
        }
        return implementsLogFactory;
    }

    /**
     * Tests whether the user wants internal diagnostic output. If so,
     * returns an appropriate writer object. Users can enable diagnostic
     * output by setting the system property named {@link #DIAGNOSTICS_DEST_PROPERTY} to
     * a file name, or the special values STDOUT or STDERR.
     */
    private static PrintStream initDiagnostics() {
        String dest;
        try {
            dest = getSystemProperty(DIAGNOSTICS_DEST_PROPERTY, null);
            if (dest == null) {
                return null;
            }
        } catch (final SecurityException ex) {
            // We must be running in some very secure environment.
            // We just have to assume output is not wanted.
            return null;
        }

        if (dest.equals("STDOUT")) {
            return System.out;
        }
        if (dest.equals("STDERR")) {
            return System.err;
        }
        try {
            // open the file in append mode
            final FileOutputStream fos = new FileOutputStream(dest, true);
            return new PrintStream(fos, false, StandardCharsets.UTF_8.name());
        } catch (final IOException ex) {
            // We should report this to the user - but how?
            return null;
        }
    }

    private static boolean isClassAvailable(final String className, final ClassLoader classLoader) {
        logDiagnostic(() -> "Checking if class '" + className + "' is available in class loader " + objectId(classLoader));
        try {
            Class.forName(className, true, classLoader);
            return true;
        } catch (final ClassNotFoundException | LinkageError e) {
            logDiagnostic(() -> "Failed to load class '" + className + "' from class loader " + objectId(classLoader) + ": " + e.getMessage());
        }
        return false;
    }

    /**
     * Tests whether the user enabled internal logging.
     * <p>
     * By the way, sorry for the incorrect grammar, but calling this method
     * areDiagnosticsEnabled just isn't Java beans style.
     * </p>
     *
     * @return true if calls to logDiagnostic will have any effect.
     * @since 1.1
     */
    protected static boolean isDiagnosticsEnabled() {
        return DIAGNOSTICS_STREAM != null;
    }

    /**
     * Generates useful diagnostics regarding the class loader tree for
     * the specified class.
     * <p>
     * As an example, if the specified class was loaded via a webapp's
     * class loader, then you may get the following output:
     * </p>
     * <pre>
     * Class com.acme.Foo was loaded via class loader 11111
     * ClassLoader tree: 11111 -> 22222 (SYSTEM) -> 33333 -> BOOT
     * </pre>
     * <p>
     * This method returns immediately if isDiagnosticsEnabled()
     * returns false.
     * </p>
     *
     * @param clazz is the class whose class loader + tree are to be
     * output.
     */
    private static void logClassLoaderEnvironment(final Class<?> clazz) {
        if (!isDiagnosticsEnabled()) {
            return;
        }
        try {
            // Deliberately use System.getProperty here instead of getSystemProperty; if
            // the overall security policy for the calling application forbids access to
            // these variables then we do not want to output them to the diagnostic stream.
            logDiagnostic("[ENV] Extension directories (java.ext.dir): " + System.getProperty("java.ext.dir"));
            logDiagnostic("[ENV] Application classpath (java.class.path): " + System.getProperty("java.class.path"));
        } catch (final SecurityException ex) {
            logDiagnostic("[ENV] Security setting prevent interrogation of system classpaths.");
        }
        final String className = clazz.getName();
        ClassLoader classLoader;
        try {
            classLoader = getClassLoader(clazz);
        } catch (final SecurityException ex) {
            // not much useful diagnostics we can print here!
            logDiagnostic("[ENV] Security forbids determining the class loader for " + className);
            return;
        }
        logDiagnostic("[ENV] Class " + className + " was loaded via class loader " + objectId(classLoader));
        logHierarchy("[ENV] Ancestry of class loader which loaded " + className + " is ", classLoader);
    }

    /**
     * Writes the specified message to the internal logging destination.
     * <p>
     * Note that this method is private; concrete subclasses of this class
     * should not call it because the diagnosticPrefix string this
     * method puts in front of all its messages is LogFactory@....,
     * while subclasses should put SomeSubClass@...
     * </p>
     * <p>
     * Subclasses should instead compute their own prefix, then call
     * logRawDiagnostic. Note that calling isDiagnosticsEnabled is
     * fine for subclasses.
     * </p>
     * <p>
     * Note that it is safe to call this method before initDiagnostics
     * is called; any output will just be ignored (as isDiagnosticsEnabled
     * will return false).
     * </p>
     *
     * @param msg is the diagnostic message to be output.
     */
    private static void logDiagnostic(final String msg) {
        if (DIAGNOSTICS_STREAM != null) {
            logDiagnosticDirect(msg);
        }
    }

    /**
     * Writes the specified message to the internal logging destination.
     * <p>
     * Note that this method is private; concrete subclasses of this class
     * should not call it because the diagnosticPrefix string this
     * method puts in front of all its messages is LogFactory@....,
     * while subclasses should put SomeSubClass@...
     * </p>
     * <p>
     * Subclasses should instead compute their own prefix, then call
     * logRawDiagnostic. Note that calling isDiagnosticsEnabled is
     * fine for subclasses.
     * </p>
     * <p>
     * Note that it is safe to call this method before initDiagnostics
     * is called; any output will just be ignored (as isDiagnosticsEnabled
     * will return false).
     * </p>
     *
     * @param msg is the diagnostic message to be output.
     */
    private static void logDiagnostic(final Supplier<String> msg) {
        if (DIAGNOSTICS_STREAM != null) {
            logDiagnosticDirect(msg.get());
        }
    }

    private static void logDiagnosticDirect(final String msg) {
        DIAGNOSTICS_STREAM.print(DIAGNOSTICS_PREFIX);
        DIAGNOSTICS_STREAM.println(msg);
        DIAGNOSTICS_STREAM.flush();
    }

    /**
     * Logs diagnostic messages about the given class loader
     * and it's hierarchy. The prefix is prepended to the message
     * and is intended to make it easier to understand the logs.
     * @param prefix
     * @param classLoader
     */
    private static void logHierarchy(final String prefix, ClassLoader classLoader) {
        if (!isDiagnosticsEnabled()) {
            return;
        }
        ClassLoader systemClassLoader;
        if (classLoader != null) {
            logDiagnostic(prefix + objectId(classLoader) + " == '" + classLoader.toString() + "'");
        }
        try {
            systemClassLoader = ClassLoader.getSystemClassLoader();
        } catch (final SecurityException ex) {
            logDiagnostic(prefix + "Security forbids determining the system class loader.");
            return;
        }
        if (classLoader != null) {
            final StringBuilder buf = new StringBuilder(prefix + "ClassLoader tree:");
            for(;;) {
                buf.append(objectId(classLoader));
                if (classLoader == systemClassLoader) {
                    buf.append(" (SYSTEM) ");
                }
                try {
                    classLoader = classLoader.getParent();
                } catch (final SecurityException ex) {
                    buf.append(" --> SECRET");
                    break;
                }
                buf.append(" --> ");
                if (classLoader == null) {
                    buf.append("BOOT");
                    break;
                }
            }
            logDiagnostic(buf.toString());
        }
    }

    /**
     * Writes the specified message to the internal logging destination.
     *
     * @param msg is the diagnostic message to be output.
     * @since 1.1
     */
    protected static final void logRawDiagnostic(final String msg) {
        if (DIAGNOSTICS_STREAM != null) {
            DIAGNOSTICS_STREAM.println(msg);
            DIAGNOSTICS_STREAM.flush();
        }
    }

    /**
     * Method provided for backwards compatibility; see newFactory version that
     * takes 3 parameters.
     * <p>
     * This method would only ever be called in some rather odd situation.
     * Note that this method is static, so overriding in a subclass doesn't
     * have any effect unless this method is called from a method in that
     * subclass. However this method only makes sense to use from the
     * getFactory method, and as that is almost always invoked via
     * LogFactory.getFactory, any custom definition in a subclass would be
     * pointless. Only a class with a custom getFactory method, then invoked
     * directly via CustomFactoryImpl.getFactory or similar would ever call
     * this. Anyway, it's here just in case, though the "managed class loader"
     * value output to the diagnostics will not report the correct value.
     * </p>
     *
     * @param factoryClass factory class.
     * @param classLoader class loader.
     * @return a LogFactory.
     */
    protected static LogFactory newFactory(final String factoryClass,
                                           final ClassLoader classLoader) {
        return newFactory(factoryClass, classLoader, null);
    }

    /**
     * Gets a new instance of the specified {@code LogFactory} implementation class, loaded by the specified class loader. If that fails, try the class loader
     * used to load this (abstract) LogFactory.
     * <p>
     * <strong>ClassLoader conflicts</strong>
     * </p>
     * <p>
     * Note that there can be problems if the specified ClassLoader is not the same as the class loader that loaded this class, that is, when loading a concrete
     * LogFactory subclass via a context class loader.
     * </p>
     * <p>
     * The problem is the same one that can occur when loading a concrete Log subclass via a context class loader.
     * </p>
     * <p>
     * The problem occurs when code running in the context class loader calls class X which was loaded via a parent class loader, and class X then calls
     * LogFactory.getFactory (either directly or via LogFactory.getLog). Because class X was loaded via the parent, it binds to LogFactory loaded via the
     * parent. When the code in this method finds some LogFactoryYYYY class in the child (context) class loader, and there also happens to be a LogFactory class
     * defined in the child class loader, then LogFactoryYYYY will be bound to LogFactory@childloader. It cannot be cast to LogFactory@parentloader, that is,
     * this method cannot return the object as the desired type. Note that it doesn't matter if the LogFactory class in the child class loader is identical to
     * the LogFactory class in the parent class loader, they are not compatible.
     * </p>
     * <p>
     * The solution taken here is to simply print out an error message when this occurs then throw an exception. The deployer of the application must ensure
     * they remove all occurrences of the LogFactory class from the child class loader in order to resolve the issue. Note that they do not have to move the
     * custom LogFactory subclass; that is ok as long as the only LogFactory class it can find to bind to is in the parent class loader.
     * </p>
     *
     * @param factoryClass       Fully qualified name of the {@code LogFactory} implementation class
     * @param classLoader        ClassLoader from which to load this class
     * @param contextClassLoader is the context that this new factory will manage logging for.
     * @return a new instance of the specified {@code LogFactory}.
     * @throws LogConfigurationException if a suitable instance cannot be created
     * @since 1.1
     */
    protected static LogFactory newFactory(final String factoryClass,
                                           final ClassLoader classLoader,
                                           final ClassLoader contextClassLoader)
            throws LogConfigurationException {
        // Note that any unchecked exceptions thrown by the createFactory
        // method will propagate out of this method; in particular a
        // ClassCastException can be thrown.
        final Object result = AccessController.doPrivileged((PrivilegedAction<?>) () -> createFactory(factoryClass, classLoader));
        if (result instanceof LogConfigurationException) {
            final LogConfigurationException ex = (LogConfigurationException) result;
            logDiagnostic(() -> "An error occurred while loading the factory class:" + ex.getMessage());
            throw ex;
        }
        logDiagnostic(() -> "Created object " + objectId(result) + " to manage class loader " + objectId(contextClassLoader));
        return (LogFactory) result;
    }

    /**
     * Tries to load one of the standard three implementations from the given classloader.
     * <p>
     *     We assume that {@code classLoader} can load this class.
     * </p>
     * @param classLoader The classloader to use.
     * @return An implementation of this class.
     */
    private static LogFactory newStandardFactory(final ClassLoader classLoader) {
        if (isClassAvailable(LOG4J_TO_SLF4J_BRIDGE, classLoader)) {
            try {
                return (LogFactory) Class.forName(FACTORY_SLF4J, true, classLoader).getConstructor().newInstance();
            } catch (final LinkageError | ReflectiveOperationException ignored) {
            } finally {
                logDiagnostic(() ->
                        "[LOOKUP] Log4j API to SLF4J redirection detected. Loading the SLF4J LogFactory implementation '" + FACTORY_SLF4J + "'.");
            }
        }
        try {
            return (LogFactory) Class.forName(FACTORY_LOG4J_API, true, classLoader).getConstructor().newInstance();
        } catch (final LinkageError | ReflectiveOperationException ignored) {
        } finally {
            logDiagnostic(() -> "[LOOKUP] Loading the Log4j API LogFactory implementation '" + FACTORY_LOG4J_API + "'.");
        }
        try {
            return (LogFactory) Class.forName(FACTORY_SLF4J, true, classLoader).getConstructor().newInstance();
        } catch (final LinkageError | ReflectiveOperationException ignored) {
        } finally {
            logDiagnostic(() -> "[LOOKUP] Loading the SLF4J LogFactory implementation '" + FACTORY_SLF4J + "'.");
        }
        try {
            return (LogFactory) Class.forName(FACTORY_DEFAULT, true, classLoader).getConstructor().newInstance();
        } catch (final LinkageError | ReflectiveOperationException ignored) {
        } finally {
            logDiagnostic(() -> "[LOOKUP] Loading the legacy LogFactory implementation '" + FACTORY_DEFAULT + "'.");
        }
        return null;
    }

    /**
     * Returns a string that uniquely identifies the specified object, including
     * its class.
     * <p>
     * The returned string is of form {@code "className@hashCode"}, that is, is the same as
     * the return value of the {@link Object#toString()} method, but works even when
     * the specified object's class has overridden the toString method.
     * </p>
     *
     * @param obj may be null.
     * @return a string of form {@code className@hashCode}, or "null" if obj is null.
     * @since 1.1
     */
    public static String objectId(final Object obj) {
        if (obj == null) {
            return "null";
        }
        return obj.getClass().getName() + "@" + System.identityHashCode(obj);
    }

    /**
     * Releases any internal references to previously created {@link LogFactory}
     * instances that have been associated with the specified class loader
     * (if any), after calling the instance method {@code release()} on
     * each of them.
     *
     * @param classLoader ClassLoader for which to release the LogFactory
     */
    public static void release(final ClassLoader classLoader) {
        logDiagnostic(() -> "Releasing factory for class loader " + objectId(classLoader));
        // factories is not final and could be replaced in this block.
        final Hashtable<ClassLoader, LogFactory> factories = LogFactory.factories;
        synchronized (factories) {
            if (classLoader == null) {
                if (nullClassLoaderFactory != null) {
                    nullClassLoaderFactory.release();
                    nullClassLoaderFactory = null;
                }
            } else {
                final LogFactory factory = factories.get(classLoader);
                if (factory != null) {
                    factory.release();
                    factories.remove(classLoader);
                }
            }
        }
    }

    /**
     * Release any internal references to previously created {@link LogFactory}
     * instances, after calling the instance method {@code release()} on
     * each of them.  This is useful in environments like servlet containers,
     * which implement application reloading by throwing away a ClassLoader.
     * Dangling references to objects in that class loader would prevent
     * garbage collection.
     */
    public static void releaseAll() {
        logDiagnostic("Releasing factory for all class loaders.");
        // factories is not final and could be replaced in this block.
        final Hashtable<ClassLoader, LogFactory> factories = LogFactory.factories;
        synchronized (factories) {
            factories.values().forEach(LogFactory::release);
            factories.clear();
            if (nullClassLoaderFactory != null) {
                nullClassLoaderFactory.release();
                nullClassLoaderFactory = null;
            }
        }
    }

    /** Trims the given string in a null-safe manner. */
    private static String trim(final String src) {
        return src != null ? src.trim() : null;
    }

    /**
     * Constructs a new instance.
     */
    protected LogFactory() {
    }

    /**
     * Gets the configuration attribute with the specified name (if any),
     * or {@code null} if there is no such attribute.
     *
     * @param name Name of the attribute to return
     * @return the configuration attribute with the specified name.
     */
    public abstract Object getAttribute(String name);

    /**
     * Gets an array containing the names of all currently defined configuration attributes. If there are no such attributes, a zero length array is returned.
     *
     * @return an array containing the names of all currently defined configuration attributes
     */
    public abstract String[] getAttributeNames();

    /**
     * Gets a Log for the given class.
     *
     * @param clazz Class for which a suitable Log name will be derived
     * @return a name from the specified class.
     * @throws LogConfigurationException if a suitable {@code Log} instance cannot be returned
     */
    public abstract Log getInstance(Class<?> clazz) throws LogConfigurationException;

    /**
     * Gets a (possibly new) {@code Log} instance, using the factory's current set of configuration attributes.
     * <p>
     * <strong>NOTE</strong> - Depending upon the implementation of the {@code LogFactory} you are using, the {@code Log} instance you are returned may or may
     * not be local to the current application, and may or may not be returned again on a subsequent call with the same name argument.
     * </p>
     *
     * @param name Logical name of the {@code Log} instance to be returned (the meaning of this name is only known to the underlying logging implementation that
     *             is being wrapped)
     * @return a {@code Log} instance.
     * @throws LogConfigurationException if a suitable {@code Log} instance cannot be returned
     */
    public abstract Log getInstance(String name)
        throws LogConfigurationException;

    /**
     * Releases any internal references to previously created {@link Log}
     * instances returned by this factory.  This is useful in environments
     * like servlet containers, which implement application reloading by
     * throwing away a ClassLoader.  Dangling references to objects in that
     * class loader would prevent garbage collection.
     */
    public abstract void release();

    /**
     * Removes any configuration attribute associated with the specified name.
     * If there is no such attribute, no action is taken.
     *
     * @param name Name of the attribute to remove
     */
    public abstract void removeAttribute(String name);

    //
    // We can't do this in the class constructor, as there are many
    // static methods on this class that can be called before any
    // LogFactory instances are created, and they depend upon this
    // stuff having been set up.
    //
    // Note that this block must come after any variable declarations used
    // by any methods called from this block, as we want any static initializer
    // associated with the variable to run first. If static initializers for
    // variables run after this code, then (a) their value might be needed
    // by methods called from here, and (b) they might *override* any value
    // computed here!
    //
    // So the wisest thing to do is just to place this code at the very end
    // of the class file.

    /**
     * Sets the configuration attribute with the specified name.  Calling
     * this with a {@code null} value is equivalent to calling
     * {@code removeAttribute(name)}.
     *
     * @param name Name of the attribute to set
     * @param value Value of the attribute to set, or {@code null}
     *  to remove any setting for this attribute
     */
    public abstract void setAttribute(String name, Object value);

}
