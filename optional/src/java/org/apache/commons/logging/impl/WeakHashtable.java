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
 */
public final class WeakHashtable extends Hashtable {

    /** Empty array of <code>Entry</code>'s */
    private static final Entry[] EMPTY_ENTRY_ARRAY = {};
    
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
        // for performance reasons, no purge
        // check for nulls, ensuring symantics match superclass
        if (key == null) {
            throw new NullPointerException("Null keys are not allowed");
        }
        if (value == null) {
            throw new NullPointerException("Null values are not allowed");
        }
        
        Object result = null;
        Referenced lastValue = (Referenced) super.put(new Referenced(key), new Referenced(value));
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
     * Purges all entries whose wrapped keys or values
     * have been garbage collected.
     */
    private synchronized void purge() {
        Set entrySet = super.entrySet();
        for (Iterator it=entrySet.iterator(); it.hasNext();) {
            Map.Entry entry = (Map.Entry) it.next();
            Referenced referencedKey = (Referenced) entry.getKey();
            Referenced referencedValue = (Referenced) entry.getValue();
            
            // test whether either referant has been collected
            if (referencedKey.getValue() == null || referencedValue.getValue() == null) {
                // if so, purge this entry
                it.remove();
            }
        }
    }
    
    
    
    /** Entry implementation */
    private final static class Entry implements Map.Entry {
    
        private Object key;
        private Object value;
        
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
        
        private Referenced(Object referant) {
            reference = new WeakReference(referant);
        }
        
        public int hashCode() {
            int result = 0;
            Object keyValue = getValue();
            if (keyValue != null) {
                result = keyValue.hashCode();
            }
            return result;
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
                }
                else
                {
                    result = thisKeyValue.equals(otherKeyValue);
                }
            }
            return result;
        }
    }
}