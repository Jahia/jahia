/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.jahia.content;

import org.slf4j.Logger;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The purpose of this class is to construct ContentObject references that can
 * be used both externally and internally to references Jahia content objects
 * such as fields, containers, pages and other objects...
 *
 * The tentative format of this key is (subject to change in order to comply
 * with existing norms) :
 *
 * type_IDInType
 *
 * where type is a String specifying the type of the object, while the IDInType
 * is an integer that specifies which instances of the specific object type.
 *
 * @author Serge Huber
 */
public abstract class ObjectKey implements
    ObjectKeyInterface,
    Serializable, Comparable<ObjectKey> {

    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(ObjectKey.class);

    private static final long serialVersionUID = 7375719911472500146L;

    private static Map<String, ObjectKeyInterface> keyTypeInstances;

    public static final String OBJECT_TYPE = "object";

    static {
    	keyTypeInstances = new ConcurrentHashMap<String, ObjectKeyInterface>();
    	registerType(CategoryKey.CATEGORY_TYPE, CategoryKey.class);
    }

    protected final String key;
    private String type = OBJECT_TYPE;
    private String IDInType;

    /**
     * Making default constructor protected, should not be used, except for
     * factory objects
     */
    protected ObjectKey() {
        this.key = null;
    }

    /**
     * Object constructor method.
     * @param type a String specifying the object type. Normally this is used
     * mostly by children of this class but could be used for some "hacks".
     * @param IDInType the unique identifier within the type.
     */
    protected ObjectKey(String type,
                     String IDInType) {
        this.type = type;
        this.IDInType = IDInType;
        StringBuilder buf = new StringBuilder(50);
        buf.append(type);
        buf.append(KEY_SEPARATOR);
        buf.append(IDInType);
        this.key = buf.toString();
    }

    /**
     * Optimized constructor, for calls where we have both the
     * seperate and constructed key.
     * @param type a String specifying the object type. Normally this is used
     * mostly by children of this class but could be used for some "hacks".
     * @param IDInType the unique identifier within the type.
     * @param key the combined object key of the type and the IDInType. This is
     * generated with type + KEY_SEPARATOR + IDInType
     */
    protected ObjectKey(String type,
                     String IDInType, String key) {
        this.type = type;
        this.IDInType = IDInType;
        this.key = key;
    }

    /**
     * Registers a new type for a sub class of ObjectKey in the factory
     * hash table. Use this if you want to be able to create instance of the
     * new class by using a complete key. The new sub class MUST implement a
     * static method with the following signature :
     *     public static ObjectKey getChildInstance(String IDInType);
     *
     * @param type the name of the type, usually defined as a constant in the
     * ObjectKey sub class.
     * @param klass the class. Can be obtained with a called
     * similar to ObjectKey.class (replacing the ObjectKey with the
     * subclass of course.
     */
    private static void registerType(String type, Class<? extends ObjectKey> klass) {
        logger.debug("Registering type [" + type + "] with class [" +
                     klass + "]");
        try {
            keyTypeInstances.put(type.toLowerCase(), klass.newInstance());
        } catch (IllegalAccessException e) {
            logger.error("Error while registering type " + type, e);
        } catch (InstantiationException e) {
            logger.error("Error while registering type " + type, e);
        }
    }

    /**
     * Removes a type registration. See the registerType method for more details
     * @param type the name of the type to unregister
     */
    public static void unregisterType(String type) {
        keyTypeInstances.remove(type.toLowerCase());
    }

    /**
     * Instance generator. Uses a string representation of the key to extract
     * the type and IDInType parameters. Build an instance of the appropriate
     * class corresponding to the type described.
     *
     * @param key a String in the format : type + KEY_SEPARATOR + IDInType
     *
     * @return an ObjectKey sub class instance that corresponds to the given
     * key.
     *
     * @throws ClassNotFoundException if no class could be found for the type
     * passed in parameter.
     */
    public static ObjectKey getInstance (String key)
        throws ClassNotFoundException {
        ObjectKey resultKey = null;
        int separatorPos = key.indexOf(KEY_SEPARATOR);
        if (separatorPos > 0) {
            String type = key.substring(0, separatorPos).toLowerCase();
            String idStr = key.substring(separatorPos + KEY_SEPARATOR.length());
            if (!keyTypeInstances.containsKey(type)) {
                throw new ClassNotFoundException("No class defined for type [" +
                                                 type + "]");
            }
            try {
                ObjectKeyInterface objectKeyInterface = (ObjectKeyInterface) keyTypeInstances.get(type);
                resultKey = objectKeyInterface.getChildInstance(idStr, key);
            } catch (SecurityException se) {
                logger.error("Error while creating instance of object key " +
                             key, se);
            } catch (IllegalArgumentException iae2) {
                logger.error("Error while creating instance of object key " +
                             key, iae2);
            }
        }
        return resultKey;
    }

    /* --- ACCESSOR METHODS ------------------------------------------------- */

    public String getKey(){
        return key;
    }

    public String getType(){
        return this.type;
    }

    public String getIDInType() {
        return this.IDInType;
    }

    /**
     * @deprecated {@link #getIDInType()} should be used instead.
     */
    public int getIdInType() {
        return -1;
    }
    /**
     * Redefinition of the equality comparison, since ObjectKey objects are
     * very likely to be used as keys in Maps, or other ordered sets,
     * maps or lists.
     * @param obj the Object to compare this object to.
     * @return true if the object is an instance of the ObjectKey class and
     * the value of the key is the same, false otherwise.
     */
    public boolean equals(Object obj) {
        if (this == obj) return true;

        if (obj != null && this.getClass() == obj.getClass()) {
            final ObjectKey destObj = (ObjectKey) obj;
            return this.getKey().equals(destObj.getKey());
        }
        return false;
    }

    /**
     * Implementation of the comparable interface so that object keys may be
     * used in sets that need a comparable interface.
     * @return a negative integer, zero, or a positive integer as this
     * object's key string is less than, equal to, or greater than the
     * specified object key string
     * @throws ClassCastException thrown if the parameter is not of the
     * ObjectKey type.
     */
    public int compareTo(ObjectKey destObjectKey) throws ClassCastException {
        return this.key.compareTo(destObjectKey.getKey());
    }

    /**
     * Hash code implementation to make object compatible with hash table
     * usage.
     * @return an unique hash code for a unique key.
     */
    public int hashCode() {
        return key.hashCode();
    }

    /**
     * Converts the object to a string only to be used for debug display,
     * not serialization !
     * @return a String containing the object key.
     */
    public String toString() {
        return key;
    }

    /**
     * Optimized method to generate ObjectKey strings without the overhead
     * of creating an ObjectKey object.
     * @param type the objectKey type
     * @param IDInType the identifier in the type for the objectKey
     * @return a String that represents the key
     */
    public static String toObjectKeyString(String type,
                             String IDInType) {
        return new StringBuilder(50).append(type).append(KEY_SEPARATOR).append(IDInType).toString();
    }

}
