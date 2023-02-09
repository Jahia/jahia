/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
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

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>Title: Root class for all Jahia objects that use object keys and that
 * can be instantiated through those keys.</p>
 * <p>Description: Objects should derive from this class if they may be
 * instantiated or referenced through ObjectKey instances. </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Jahia Ltd</p>
 * @author Serge Huber
 * @version 1.0
 */

public abstract class JahiaObject implements Serializable {

    private static final long serialVersionUID = -2624062108412420112L;

    private static org.slf4j.Logger logger =
        org.slf4j.LoggerFactory.getLogger(JahiaObject.class);

    private static Map<String, String> keyTypeClassNames = new ConcurrentHashMap<String, String>();

    protected ObjectKey objectKey;
    private static Map<String, Method> methodsMap = new ConcurrentHashMap<String, Method>(32);

    /**
     * Default constructor that sets the object's key.
     * @param objectKey the object key that uniquely identifies this instance
     * of JahiaObject.
     */
    public JahiaObject(ObjectKey objectKey) {
        this.objectKey = objectKey;
    }

    /**
     * No arg constructor used for serialization compliance.
     */
    protected JahiaObject() {
    }

    /**
     * Registers a new type for a sub class of JahiaObject in the factory
     * hash table. Use this if you want to be able to create instance of the
     * new class by using a complete content object key. The new sub class MUST
     * implement a static method with the following signature :
     *     public static JahiaObject getChildInstance(String IDInType);
     *
     * @param type the name of the type, usually defined as a constant in the
     * equivalent ObjectKey sub class (such as ContentPageKey)
     * @param className the name of the class. Can be obtained with a called
     * similar to JahiaObject.class.getName() (replacing the JahiaObject
     * with the subclass of course.
     */
    public static void registerType(String type, String className) {
        logger.debug("Registering type [" + type + "] with class name [" +
                     className + "]");
        keyTypeClassNames.put ( type, className);
    }

    /**
     * Removes a type registration. See the registerType method for more details
     * @param type the name of the type to unregister
     */
    public static void unregisterType(String type) {
        keyTypeClassNames.remove(type);
    }

    /**
     * Instance generator. Build an instance of the appropriate
     * class corresponding to the ObjectKey passed described.
     *
     * @param objectKey an ObjectKey instance for the object we want to retrieve
     * an instance of.
     * @returns a JahiaObject sub class instance that corresponds to the given
     * object key.
     *
     * @throws ClassNotFoundException if no class could be found for the type
     * passed in the object key
     */
    public static JahiaObject getInstance (ObjectKey objectKey)
        throws ClassNotFoundException {
        return (JahiaObject)getInstanceAsObject(objectKey, false);
    }

    /**
     * Instance generator. Build an instance of the appropriate
     * class corresponding to the ObjectKey passed described.
     *
     * @param objectKey an ObjectKey instance for the object we want to retrieve
     * an instance of.
     * @param forceLoadFromDB true, if object should not be returned from cache
     * @returns a JahiaObject sub class instance that corresponds to the given
     * object key.
     *
     * @throws ClassNotFoundException if no class could be found for the type
     * passed in the object key
     */
    public static JahiaObject getInstance (ObjectKey objectKey, boolean forceLoadFromDB)
        throws ClassNotFoundException {
        return (JahiaObject)getInstanceAsObject(objectKey, forceLoadFromDB);
    }

    /**
     * Instance generator. Build an instance of the appropriate
     * class corresponding to the ObjectKey passed described.
     *
     * @param objectKey an ObjectKey instance for the object we want to retrieve
     * an instance of.
     * @returns a JahiaObject sub class instance that corresponds to the given
     * object key.
     *
     * @throws ClassNotFoundException if no class could be found for the type
     * passed in the object key
     */
    public static Object getInstanceAsObject (ObjectKey objectKey)
        throws ClassNotFoundException {
        return getInstanceAsObject(objectKey, false);
    }

    /**
     * Instance generator. Build an instance of the appropriate
     * class corresponding to the ObjectKey passed described.
     *
     * @param objectKey an ObjectKey instance for the object we want to retrieve
     * an instance of.
     * @param forceLoadFromDB true, if object should not be returned from cache
     * @returns a JahiaObject sub class instance that corresponds to the given
     * object key.
     *
     * @throws ClassNotFoundException if no class could be found for the type
     * passed in the object key
     */
    public static Object getInstanceAsObject (ObjectKey objectKey, boolean forceLoadFromDB)
        throws ClassNotFoundException {
        Object resultObject = null;
        if (objectKey == null) {
            return null;
        }
        String type = objectKey.getType();
        String idStr = objectKey.getIDInType();
        if (!keyTypeClassNames.containsKey(type)) {
            throw new ClassNotFoundException("No class defined for type [" +
                                             type + "]");
        }
        try {
            String className = (String) keyTypeClassNames.get(type);
            Method childClassMethod;
            Object[] args = new Object[forceLoadFromDB ? 2 : 1];
            args[0] = idStr;
            if (forceLoadFromDB) {
                args[1] = Boolean.valueOf(forceLoadFromDB);
            }
            if (!methodsMap.containsKey(className + forceLoadFromDB)) {
                Class<? extends JahiaObject> childClass = Class.forName(className).asSubclass(JahiaObject.class);
                Class<?>[] childClassParameters = new Class[forceLoadFromDB ? 2 : 1];
                childClassParameters[0] = String.class;
                if (forceLoadFromDB) {
                    childClassParameters[1] = boolean.class;
                }
                childClassMethod = childClass.getMethod("getChildInstance", childClassParameters);
                methodsMap.put(className + forceLoadFromDB, childClassMethod);
            } else {
                childClassMethod = methodsMap.get(className + forceLoadFromDB);
            }
            resultObject = childClassMethod.invoke(null, args);
        } catch (ClassNotFoundException cnfe) {
            logger.error("Error while creating instance of object " +
                         objectKey, cnfe);
        } catch (NoSuchMethodException nsme) {
            logger.error("Error while creating instance of object " +
                         objectKey, nsme);
        } catch (SecurityException se) {
            logger.error("Error while creating instance of object " +
                         objectKey, se);
        } catch (IllegalAccessException iae) {
            logger.error("Error while creating instance of object " +
                         objectKey, iae);
        } catch (IllegalArgumentException iae2) {
            logger.error("Error while creating instance of object " +
                         objectKey, iae2);
        } catch (InvocationTargetException ite) {
            logger.error("Error while creating instance of object " +
                         objectKey, ite);
            logger.error(
                "Error while creating instance of object " + objectKey +
                ", target exception="
                , ite.getTargetException());
        }
        return resultObject;
    }

    /**
     * Returns the object key that fully identifies the content object within
     * a Jahia system.
     * @return an ObjectKey class that uniquely identifies the object.
     */
    public ObjectKey getObjectKey() {
        return objectKey;
    }
}
