/**
 * 
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 * 
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL (or any later version), you may redistribute this Program in connection
 * with Free/Libre and Open Source Software ("FLOSS") applications as described
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
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