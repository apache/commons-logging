/*
 * Copyright 2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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
import java.util.*;

/**
 * <p>Implementation of <code>Hashtable</code> that uses <code>WeakReference</code>'s
 * to hold it's keys thus allowing them to be reclaimed by the garbage collector.
 * This class follows the symantics of <code>Hashtable</code> as closely as possible.
 * It therefore does not accept null values or keys.
 * <p>
 * This implementation is also tuned towards a particular purpose: for use as a replacement
 * for <code>Hashtable</code> in <code>LogFactory</code>. This application requires
 * good liveliness for <code>get</code> and <code>put</code>. Various tradeoffs
 * have been made with this in mind.
 * </p>
 * <p>
 * <strong>Usage:</strong> typical use case is as a drop-in replacement 
 * for the <code>Hashtable</code> use in <code>LogFactory</code> for J2EE enviroments
 * running 1.3+ JVMs. Use of this class will allow classloaders to be collected by 
 * the garbage collector without the need to call release.
 * </p>
 * 
 * @author Brian Stansberry
 */
public final class WeakHashtable extends Hashtable {

    /** Empty array of <code>Entry</code>'s */
    private static final Entry[] EMPTY_ENTRY_ARRAY = {};
    /** 
     * The maximum number of times put() can be called before
     * the map will purged of cleared entries.
     */
    public static final int MAX_PUTS_BEFORE_PURGE = 100;

    /* ReferenceQueue we check for gc'd keys */
    private ReferenceQueue queue = new ReferenceQueue();
    /* Counter used to control how often we purge gc'd entries */
    private int putCount = 0;
    
    /**
     * Constructs a WeakHashtable with the Hashtable default
     * capacity and load factor.
     */
    public WeakHashtable() {}

    /**
     *@see Hashtable
     */
    public boolean contains(Object value) {
        // purge should not be required
        if (value instanceof Referenced) {
            return super.contains(value);
        }
        Referenced referenced = new Referenced(value);
        return super.contains(referenced);
    }
    
    /**
     *@see Hashtable
     */
    public boolean containsKey(Object key) {
        // purge should not be required
        Referenced referenced = new Referenced(key);
        return super.containsKey(referenced);
    }
    
    /**
     *@see Hashtable
     */
    public boolean containsValue(Object value) {
        // purge should not be required
        if (value instanceof Referenced) {
            return super.contains(value);
        }
        Referenced referenced = new Referenced(value);
        return super.containsValue(referenced);
    }
    
    /**
     *@see Hashtable
     */
    public Enumeration elements() {
        purge();
        final Enumeration enum = super.elements();
        return new Enumeration() {
            public boolean hasMoreElements() {
                return enum.hasMoreElements();
            }
            public Object nextElement() {
                 Referenced nextReference = (Referenced) enum.nextElement();
                 return nextReference.getValue();
            }
        };
    }
    
    /**
     *@see Hashtable
     */
    public Set entrySet() {
        purge();
        Set referencedEntries = super.entrySet();
        Set unreferencedEntries = new HashSet();
        for (Iterator it=referencedEntries.iterator(); it.hasNext();) {
            Map.Entry entry = (Map.Entry) it.next();
            Referenced referencedKey = (Referenced) entry.getKey();
            Object key = referencedKey.getValue();
            Referenced referencedValue = (Referenced) entry.getValue();
            Object value = referencedValue.getValue();
            if (key != null) {
                Entry dereferencedEntry = new Entry(key, value);
                unreferencedEntries.add(dereferencedEntry);
            }
        }
        return unreferencedEntries;
    }
    
    /**
     *@see Hashtable
     */
    public Object get(Object key) {
        // for performance reasons, no purge
        Object result = null;
        Referenced referenceKey = new Referenced(key);
        Referenced referencedValue = (Referenced) super.get(referenceKey);
        if (referencedValue != null) {
            result = referencedValue.getValue();
        }
        return result;
    }
    
    /**
     *@see Hashtable
     */
    public Enumeration keys() {
        purge();
        final Enumeration enum = super.keys();
        return new Enumeration() {
            public boolean hasMoreElements() {
                return enum.hasMoreElements();
            }
            public Object nextElement() {
                 Referenced nextReference = (Referenced) enum.nextElement();
                 return nextReference.getValue();
            }
        };
    }
    
        
    /**
     *@see Hashtable
     */
    public Set keySet() {
        purge();
        Set referencedKeys = super.keySet();
        Set unreferencedKeys = new HashSet();
        for (Iterator it=referencedKeys.iterator(); it.hasNext();) {
            Referenced referenceKey = (Referenced) it.next();
            Object keyValue = referenceKey.getValue();
            if (keyValue != null) {
                unreferencedKeys.add(keyValue);
            }
        }
        return unreferencedKeys;
    }
    
    /**
     *@see Hashtable
     */    
    public Object put(Object key, Object value) {
        // check for nulls, ensuring symantics match superclass
        if (key == null) {
            throw new NullPointerException("Null keys are not allowed");
        }
        if (value == null) {
            throw new NullPointerException("Null values are not allowed");
        }

        // for performance reasons, only purge every 
        // MAX_PUTS_BEFORE_PURGE times
        if (putCount++ > MAX_PUTS_BEFORE_PURGE) {
            purge();
            putCount = 0;
        }
        Object result = null;
        Referenced keyRef    = new Referenced(key, value, queue);
        Referenced valueRef  = new Referenced(value);
        Referenced lastValue = (Referenced) super.put(keyRef, valueRef);
        if (lastValue != null) {
            result = lastValue.getValue();
        }
        return result;
    }
    
    /**
     *@see Hashtable
     */    
    public void putAll(Map t) {
        if (t != null) {
            Set entrySet = t.entrySet();
            for (Iterator it=entrySet.iterator(); it.hasNext();) {
                Map.Entry entry = (Map.Entry) it.next();
                put(entry.getKey(), entry.getValue());
            }
        }
    }
    
    /**
     *@see Hashtable
     */      
    public Collection values() {
        purge();
        Collection referencedValues = super.values();
        ArrayList unreferencedValues = new ArrayList();
        for (Iterator it = referencedValues.iterator(); it.hasNext();) {
            Referenced reference = (Referenced) it.next();
            Object value = reference.getValue();
            if (value != null) {
                unreferencedValues.add(value);
            }
        }
        return unreferencedValues;
    }
    
    /**
     *@see Hashtable
     */     
    public Object remove(Object key) {
        return super.remove(new Referenced(key));
    }
    
    /**
     *@see Hashtable
     */    
    public boolean isEmpty() {
        purge();
        return super.isEmpty();
    }
    
    /**
     *@see Hashtable
     */    
    public int size() {
        purge();
        return super.size();
    }
    
    /**
     *@see Hashtable
     */        
    public String toString() {
        purge();
        return super.toString();
    }
    
    /**
     * @see Hashtable
     */
    protected void rehash() {
        // purge here to save the effort of rehashing dead entries
        purge();
        super.rehash();
    }
    
    /**
     * Purges all entries whose wrapped keys
     * have been garbage collected.
     */
    private synchronized void purge() {
        WeakKey key;
        while ( (key = (WeakKey) queue.poll()) != null) {
            super.remove(key.getReferenced());
        }
    }
    
    /** Entry implementation */
    private final static class Entry implements Map.Entry {
    
        private final Object key;
        private final Object value;
        
        private Entry(Object key, Object value) {
            this.key = key;
            this.value = value;
        }
    
        public boolean equals(Object o) {
            boolean result = false;
            if (o != null && o instanceof Map.Entry) {
                Map.Entry entry = (Map.Entry) o;
                result =    (getKey()==null ?
                                            entry.getKey() == null : 
                                            getKey().equals(entry.getKey()))
                            &&
                            (getValue()==null ?
                                            entry.getValue() == null : 
                                            getValue().equals(entry.getValue()));
            }
            return result;
        } 
        
        public int hashCode() {

            return (getKey()==null ? 0 : getKey().hashCode()) ^
                (getValue()==null ? 0 : getValue().hashCode());
        }

        public Object setValue(Object value) {
            throw new UnsupportedOperationException("Entry.setValue is not supported.");
        }
        
        public Object getValue() {
            return value;
        }
        
        public Object getKey() {
            return key;
        }
    }
    
    
    /** Wrapper giving correct symantics for equals and hashcode */
    private final static class Referenced {
        
        private final WeakReference reference;
        private final int           hashCode;

        /**
         * 
         * @throws NullPointerException if referant is <code>null</code>
         */        
        private Referenced(Object referant) {
            reference = new WeakReference(referant);
            // Calc a permanent hashCode so calls to Hashtable.remove()
            // work if the WeakReference has been cleared
            hashCode  = referant.hashCode();
        }
        
        /**
         * 
         * @throws NullPointerException if key is <code>null</code>
         */
        private Referenced(Object key, Object value, ReferenceQueue queue) {
            reference = new WeakKey(key, value, queue, this);
            // Calc a permanent hashCode so calls to Hashtable.remove()
            // work if the WeakReference has been cleared
            hashCode  = key.hashCode();

        }
        
        public int hashCode() {
            return hashCode;
        }
        
        private Object getValue() {
            return reference.get();
        }
        
        public boolean equals(Object o) {
            boolean result = false;
            if (o instanceof Referenced) {
                Referenced otherKey = (Referenced) o;
                Object thisKeyValue = getValue();
                Object otherKeyValue = otherKey.getValue();
                if (thisKeyValue == null) {                     
                    result = (otherKeyValue == null);
                    
                    // Since our hashcode was calculated from the original
                    // non-null referant, the above check breaks the 
                    // hashcode/equals contract, as two cleared Referenced
                    // objects could test equal but have different hashcodes.
                    // We can reduce (not eliminate) the chance of this
                    // happening by comparing hashcodes.
                    if (result == true) {
                        result = (this.hashCode() == otherKey.hashCode());
                    }
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
    }
    
    /**
     * WeakReference subclass that holds a hard reference to an
     * associated <code>value</code> and also makes accessible
     * the Referenced object holding it.
     */
    private final static class WeakKey extends WeakReference {
        
        private final Object     hardValue;
        private final Referenced referenced;
        
        private WeakKey(Object key, 
                        Object value, 
                        ReferenceQueue queue,
                        Referenced referenced) {
            super(key, queue);
            hardValue = value;
            this.referenced = referenced;
        }
        
        private Referenced getReferenced() {
            return referenced;
        }
        
        /* Drop our hard reference to value if we've been cleared
         * by the gc.
         * 
         * Testing shows that with key objects like ClassLoader
         * that don't override hashCode(), get() is never
         * called once the key is in a Hashtable. 
         * So, this method override is commented out. 
         */
        //public Object get() {
        //    Object result = super.get();
        //    if (result == null) {
        //        // We've been cleared, so drop our hard reference to value
        //        hardValue = null;
        //    }
        //    return result;
        //}      
     }
}