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
 * </p>
 * <p>
 * <strong>Usage:</strong> typical use case is as a drop-in replacement 
 * for the <code>Hashtable</code> use in <code>LogFactory</code> for J2EE enviroments
 * running 1.3+ JVMs. Use of this class will allow classloaders to be collected by 
 * the garbage collector without the need to call release.
 * </p>
 */
public final class WeakHashtable extends Hashtable {

    
    public WeakHashtable() {}

    /**
     *@see Hashtable
     */
    public boolean contains(Object value) {
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
        Referenced referenced = new Referenced(key);
        return super.containsKey(referenced);
    }
    
    /**
     *@see Hashtable
     */
    public boolean containsValue(Object value) {
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