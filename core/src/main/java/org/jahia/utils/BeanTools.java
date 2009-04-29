/**
 * Jahia Enterprise Edition v6
 *
 * Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.
 *
 * Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
 * with Document Management and Portal features.
 *
 * The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED.
 *
 * Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
 * you and Jahia (Jahia Sustainable Enterprise License - JSEL).
 *
 * If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.
 */
 package org.jahia.utils;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Jahia Ltd</p>
 * @author Serge Huber
 * @version 1.0
 */

public class BeanTools {

    private static org.apache.log4j.Logger logger =
        org.apache.log4j.Logger.getLogger(BeanTools.class);

    private BeanTools () {
    }

    public static boolean setBeanProperty (Object beanObject,
                                           String propertyName,
                                           Class propertyType,
                                           Object propertyValue) {
        Class beanClass = beanObject.getClass();
        String beanMethodName = "set" + propertyName.substring(0, 1).toUpperCase() +
                       propertyName.substring(1);
        Class[] beanPropertySetterParamTypes = new Class[1];
        beanPropertySetterParamTypes[0] = propertyType;
        try {
            Method beanPropertySetter = beanClass.getMethod(beanMethodName, beanPropertySetterParamTypes);
            Object[] args = new Object[1];
            args[0] = propertyValue;
            beanPropertySetter.invoke(beanObject, args);
            return true;
        } catch (NoSuchMethodException nsme) {
            logger.error("Couldn't find setter method " + beanMethodName + " on object " + beanObject, nsme);
            return false;
        } catch (IllegalAccessException iae) {
            logger.error("Error accessing setter method " + beanMethodName + " on object " + beanObject, iae);
            return false;
        } catch (InvocationTargetException ite) {
            logger.error("Exception in setter method " + beanMethodName + " on object " + beanObject,
                         ite.getTargetException());
            return false;
        }
    }

    public static boolean setBooleanProperty (Object beanObject,
                                              String propertyName,
                                              String propertyValue) {
        return setBeanProperty(beanObject, propertyName, Boolean.TYPE,
                               Boolean.valueOf(propertyValue));
    }

    public static boolean setIntProperty (Object beanObject,
                                          String propertyName,
                                          String propertyValue) {
        return setBeanProperty(beanObject, propertyName, Integer.TYPE,
                               Integer.valueOf(propertyValue));
    }

    public static boolean setLongProperty (Object beanObject,
                                           String propertyName,
                                           String propertyValue) {
        return setBeanProperty(beanObject, propertyName, Long.TYPE,
                               Long.valueOf(propertyValue));
    }

    public static boolean setStringProperty (Object beanObject,
                                             String propertyName,
                                             String propertyValue) {
        return setBeanProperty(beanObject, propertyName, String.class,
                               propertyValue);
    }

}