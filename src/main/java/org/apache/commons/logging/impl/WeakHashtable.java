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

package org.apache.commons.logging.impl;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Implementation of {@code Hashtable} that uses {@code WeakReference}'s
 * to hold its keys thus allowing them to be reclaimed by the garbage collector.
 * The associated values are retained using strong references.
 * <p>
 * This class follows the semantics of {@code Hashtable} as closely as
 * possible. It therefore does not accept null values or keys.
 * <p>
 * <strong>Note:</strong>
 * This is <em>not</em> intended to be a general purpose hash table replacement.
 * This implementation is also tuned towards a particular purpose: for use as a replacement
 * for {@code Hashtable} in {@code LogFactory}. This application requires
 * good liveliness for {@code get} and {@code put}. Various tradeoffs
 * have been made with this in mind.
 * <p>
 * <strong>Usage:</strong> typical use case is as a drop-in replacement
 * for the {@code Hashtable} used in {@code LogFactory} for J2EE environments
 * running 1.3+ JVMs. Use of this class <i>in most cases</i> (see below) will
 * allow class loaders to be collected by the garbage collector without the need
 * to call {@link org.apache.commons.logging.LogFactory#release(ClassLoader) LogFactory.release(ClassLoader)}.
 * <p>
 * {@code org.apache.commons.logging.LogFactory} checks whether this class
 * can be supported by the current JVM, and if so then uses it to store
 * references to the {@code LogFactory} implementation it loads
 * (rather than using a standard Hashtable instance).
 * Having this class used instead of {@code Hashtable} solves
 * certain issues related to dynamic reloading of applications in J2EE-style
 * environments. However this class requires Java 1.3 or later (due to its use
 * of {@link java.lang.ref.WeakReference} and associates).
 * And by the way, this extends {@code Hashtable} rather than {@code HashMap}
 * for backwards compatibility reasons. See the documentation
 * for method {@code LogFactory.createFactoryStore} for more details.
 * <p>
 * The reason all this is necessary is due to a issue which
 * arises during hot deploy in a J2EE-like containers.
 * Each component running in the container owns one or more class loaders; when
 * the component loads a LogFactory instance via the component class loader
 * a reference to it gets stored in the static LogFactory.factories member,
 * keyed by the component's class loader so different components don't
 * stomp on each other. When the component is later unloaded, the container
 * sets the component's class loader to null with the intent that all the
 * component's classes get garbage-collected. However there's still a
 * reference to the component's class loader from a key in the "global"
 * {@code LogFactory}'s factories member! If {@code LogFactory.release()}
 * is called whenever component is unloaded, the class loaders will be correctly
 * garbage collected; this <i>should</i> be done by any container that
 * bundles commons-logging by default. However, holding the class loader
 * references weakly ensures that the class loader will be garbage collected
 * without the container performing this step.
 * <p>
 * <strong>Limitations:</strong>
 * There is still one (unusual) scenario in which a component will not
 * be correctly unloaded without an explicit release. Though weak references
 * are used for its keys, it is necessary to use strong references for its values.
 * <p>
 * If the abstract class {@code LogFactory} is
 * loaded by the container class loader but a subclass of
 * {@code LogFactory} [LogFactory1] is loaded by the component's
 * class loader and an instance stored in the static map associated with the
 * base LogFactory class, then there is a strong reference from the LogFactory
 * class to the LogFactory1 instance (as normal) and a strong reference from
 * the LogFactory1 instance to the component class loader via
 * {@code getClass().getClassLoader()}. This chain of references will prevent
 * collection of the child class loader.
 * <p>
 * Such a situation occurs when the commons-logging.jar is
 * loaded by a parent class loader (e.g. a server level class loader in a
 * servlet container) and a custom {@code LogFactory} implementation is
 * loaded by a child class loader (e.g. a web app class loader).
 * <p>
 * To avoid this scenario, ensure
 * that any custom LogFactory subclass is loaded by the same class loader as
 * the base {@code LogFactory}. Creating custom LogFactory subclasses is,
 * however, rare. The standard LogFactoryImpl class should be sufficient
 * for most or all users.
 *
 * @since 1.1
 * @deprecated No longer used.
 */
@Deprecated
public final class WeakHashtable extends Hashtable {

    /** Entry implementation */
    private final static class Entry implements Map.Entry {

        private final Object key;
        private final Object value;

        private Entry(final Object key, final Object value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public boolean equals(final Object o) {
            boolean result = false;
            if (o instanceof Map.Entry) {
                final Map.Entry entry = (Map.Entry) o;
                result =    (getKey()==null ?
                                            entry.getKey() == null :
                                            getKey().equals(entry.getKey())) &&
                            (getValue()==null ?
                                            entry.getValue() == null :
                                            getValue().equals(entry.getValue()));
            }
            return result;
        }

        @Override
        public Object getKey() {
            return key;
        }

        @Override
        public Object getValue() {
            return value;
        }

        @Override
        public int hashCode() {
            return (getKey()==null ? 0 : getKey().hashCode()) ^
                (getValue()==null ? 0 : getValue().hashCode());
        }

        @Override
        public Object setValue(final Object value) {
            throw new UnsupportedOperationException("Entry.setValue is not supported.");
        }
    }

    /** Wrapper giving correct symantics for equals and hash code */
    private final static class Referenced {

        private final WeakReference reference;
        private final int           hashCode;

        /**
         *
         * @throws NullPointerException if referant is {@code null}
         */
        private Referenced(final Object referant) {
            reference = new WeakReference(referant);
            // Calc a permanent hashCode so calls to Hashtable.remove()
            // work if the WeakReference has been cleared
            hashCode  = referant.hashCode();
        }

        /**
         *
         * @throws NullPointerException if key is {@code null}
         */
        private Referenced(final Object key, final ReferenceQueue queue) {
            reference = new WeakKey(key, queue, this);
            // Calc a permanent hashCode so calls to Hashtable.remove()
            // work if the WeakReference has been cleared
            hashCode  = key.hashCode();

        }

        @Override
        public boolean equals(final Object o) {
            boolean result = false;
            if (o instanceof Referenced) {
                final Referenced otherKey = (Referenced) o;
                final Object thisKeyValue = getValue();
                final Object otherKeyValue = otherKey.getValue();
                if (thisKeyValue == null) {
                    result = otherKeyValue == null;

                    // Since our hash code was calculated from the original
                    // non-null referant, the above check breaks the
                    // hash code/equals contract, as two cleared Referenced
                    // objects could test equal but have different hash codes.
                    // We can reduce (not eliminate) the chance of this
                    // happening by comparing hash codes.
                    result = result && this.hashCode() == otherKey.hashCode();
                    // In any case, as our c'tor does not allow null referants
                    // and Hashtable does not do equality checks between
                    // existing keys, normal hashtable operations should never
                    // result in an equals comparison between null referants
                }
                else
                {
                    result = thisKeyValue.equals(otherKeyValue);
                }
            }
            return result;
        }

        private Object getValue() {
            return reference.get();
        }

        @Override
        public int hashCode() {
            return hashCode;
        }
    }

    /**
     * WeakReference subclass that holds a hard reference to an
     * associated {@code value} and also makes accessible
     * the Referenced object holding it.
     */
    private final static class WeakKey extends WeakReference {

        private final Referenced referenced;

        private WeakKey(final Object key,
                        final ReferenceQueue queue,
                        final Referenced referenced) {
            super(key, queue);
            this.referenced = referenced;
        }

        private Referenced getReferenced() {
            return referenced;
        }
     }

    /** Serializable version identifier. */
    private static final long serialVersionUID = -1546036869799732453L;

    /**
     * The maximum number of times put() or remove() can be called before
     * the map will be purged of all cleared entries.
     */
    private static final int MAX_CHANGES_BEFORE_PURGE = 100;

    /**
     * The maximum number of times put() or remove() can be called before
     * the map will be purged of one cleared entry.
     */
    private static final int PARTIAL_PURGE_COUNT     = 10;

    /** ReferenceQueue we check for GC'd keys. */
    private final transient ReferenceQueue queue = new ReferenceQueue();

    /** Counter used to control how often we purge gc'd entries. */
    private int changeCount;

    /**
     * Constructs a WeakHashtable with the Hashtable default
     * capacity and load factor.
     */
    public WeakHashtable() {}

    /**
     *@see Hashtable
     */
    @Override
    public boolean containsKey(final Object key) {
        // purge should not be required
        final Referenced referenced = new Referenced(key);
        return super.containsKey(referenced);
    }

    /**
     *@see Hashtable
     */
    @Override
    public Enumeration elements() {
        purge();
        return super.elements();
    }

    /**
     *@see Hashtable
     */
    @Override
    public Set entrySet() {
        purge();
        final Set referencedEntries = super.entrySet();
        final Set unreferencedEntries = new HashSet();
        for (final Object referencedEntry : referencedEntries) {
            final Map.Entry entry = (Map.Entry) referencedEntry;
            final Referenced referencedKey = (Referenced) entry.getKey();
            final Object key = referencedKey.getValue();
            final Object value = entry.getValue();
            if (key != null) {
                final Entry dereferencedEntry = new Entry(key, value);
                unreferencedEntries.add(dereferencedEntry);
            }
        }
        return unreferencedEntries;
    }

    /**
     *@see Hashtable
     */
    @Override
    public Object get(final Object key) {
        // for performance reasons, no purge
        final Referenced referenceKey = new Referenced(key);
        return super.get(referenceKey);
    }

    /**
     *@see Hashtable
     */
    @Override
    public boolean isEmpty() {
        purge();
        return super.isEmpty();
    }

    /**
     *@see Hashtable
     */
    @Override
    public Enumeration keys() {
        purge();
        final Enumeration enumer = super.keys();
        return new Enumeration() {
            @Override
            public boolean hasMoreElements() {
                return enumer.hasMoreElements();
            }
            @Override
            public Object nextElement() {
                 final Referenced nextReference = (Referenced) enumer.nextElement();
                 return nextReference.getValue();
            }
        };
    }

    /**
     *@see Hashtable
     */
    @Override
    public Set keySet() {
        purge();
        final Set referencedKeys = super.keySet();
        final Set unreferencedKeys = new HashSet();
        for (final Object referencedKey : referencedKeys) {
            final Referenced referenceKey = (Referenced) referencedKey;
            final Object keyValue = referenceKey.getValue();
            if (keyValue != null) {
                unreferencedKeys.add(keyValue);
            }
        }
        return unreferencedKeys;
    }

    /**
     * Purges all entries whose wrapped keys
     * have been garbage collected.
     */
    private void purge() {
        final List toRemove = new ArrayList();
        synchronized (queue) {
            WeakKey key;
            while ((key = (WeakKey) queue.poll()) != null) {
                toRemove.add(key.getReferenced());
            }
        }

        // LOGGING-119: do the actual removal of the keys outside the sync block
        // to prevent deadlock scenarios as purge() may be called from
        // non-synchronized methods too
        final int size = toRemove.size();
        for (int i = 0; i < size; i++) {
            super.remove(toRemove.get(i));
        }
    }

    /**
     * Purges one entry whose wrapped key
     * has been garbage collected.
     */
    private void purgeOne() {
        synchronized (queue) {
            final WeakKey key = (WeakKey) queue.poll();
            if (key != null) {
                super.remove(key.getReferenced());
            }
        }
    }

    /**
     *@see Hashtable
     */
    @Override
    public synchronized Object put(final Object key, final Object value) {
        // check for nulls, ensuring semantics match superclass
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(value, "value");

        // for performance reasons, only purge every
        // MAX_CHANGES_BEFORE_PURGE times
        if (changeCount++ > MAX_CHANGES_BEFORE_PURGE) {
            purge();
            changeCount = 0;
        }
        // do a partial purge more often
        else if (changeCount % PARTIAL_PURGE_COUNT == 0) {
            purgeOne();
        }

        final Referenced keyRef = new Referenced(key, queue);
        return super.put(keyRef, value);
    }

    /**
     *@see Hashtable
     */
    @Override
    public void putAll(final Map t) {
        if (t != null) {
            final Set entrySet = t.entrySet();
            for (final Object element : entrySet) {
                final Map.Entry entry = (Map.Entry) element;
                put(entry.getKey(), entry.getValue());
            }
        }
    }

    /**
     * @see Hashtable
     */
    @Override
    protected void rehash() {
        // purge here to save the effort of rehashing dead entries
        purge();
        super.rehash();
    }

    /**
     *@see Hashtable
     */
    @Override
    public synchronized Object remove(final Object key) {
        // for performance reasons, only purge every
        // MAX_CHANGES_BEFORE_PURGE times
        if (changeCount++ > MAX_CHANGES_BEFORE_PURGE) {
            purge();
            changeCount = 0;
        }
        // do a partial purge more often
        else if (changeCount % PARTIAL_PURGE_COUNT == 0) {
            purgeOne();
        }
        return super.remove(new Referenced(key));
    }

    /**
     *@see Hashtable
     */
    @Override
    public int size() {
        purge();
        return super.size();
    }

    /**
     *@see Hashtable
     */
    @Override
    public String toString() {
        purge();
        return super.toString();
    }

    /**
     *@see Hashtable
     */
    @Override
    public Collection values() {
        purge();
        return super.values();
    }
}
