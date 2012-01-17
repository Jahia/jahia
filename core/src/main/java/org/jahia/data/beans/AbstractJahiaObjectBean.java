/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
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
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

 package org.jahia.data.beans;

import java.util.Map;
import java.util.HashMap;
import org.jahia.content.JahiaObject;
import java.lang.reflect.InvocationTargetException;
import org.jahia.params.ProcessingContext;

/**
 * <p>Title: Abstract class that is the parent class of all bean wrappers
 * for JahiaObject classes.</p>
 * <p>Description: This class is the parent class for all the JavaBean
 * compliant wrappers that are part of the new template tools available since
 * Jahia 4. Using this common abstract class we can instantiate any wrapper
 * by using the corresponding JahiaObject and ProcessingContext objects.</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Jahia Ltd</p>
 * @author Serge Huber
 * @version 1.0
 */

public abstract class AbstractJahiaObjectBean {

    private static org.slf4j.Logger logger =
        org.slf4j.LoggerFactory.getLogger(AbstractJahiaObjectBean.class);

    private static Map<String, String> jahiaObjectToBeanClassNames = new HashMap<String, String>();

    /**
     * Registers a new mapping for a sub class of JahiaObject in the factory
     * hash table. Use this if you want to be able to create instance of the
     * new class by using a JahiaObject. The new sub class MUST
     * implement a static method with the following signature :
     *     public static AbstractJahiaObjectBean getChildInstance(JahiaObject jahiaObject,
     *                                                            ProcessingContext paramBean);
     *
     * @param jahiaObjectClassName the source JahiaObject class name that will
     * be mapped to the beanClassName
     * @param beanClassName the target AbstractJahiaObjectBean class name that
     * will be mapped to.
     */
    public static void registerType(String jahiaObjectClassName, String beanClassName) {
        logger.debug("Registering mapping from JahiaObject class name [" + jahiaObjectClassName + "] to bean class name [" +
                     beanClassName + "]");
        jahiaObjectToBeanClassNames.put ( jahiaObjectClassName, beanClassName);
    }

    /**
     * Removes a JahiaObject mapping registration. See the registerType method
     * for more details
     * @param jahiaObjectClassName the name of the JahiaObject class name
     */
    public static void unregisterType(String jahiaObjectClassName) {
        jahiaObjectToBeanClassNames.remove(jahiaObjectClassName);
    }

    /**
     * Instance generator. Build an instance of the appropriate JavaBean
     * compliant wrapper class corresponding to the JahiaObject instance and
     * the current ProcessingContext that represents the current request.
     *
     * @param jahiaObject the JahiaObject child instance for which to create
     * an AbstractJahiaObjectBean instance.
     * @throws ClassNotFoundException if no class could be found for the type
     * passed in the object key
     */
    public static AbstractJahiaObjectBean getInstance (JahiaObject jahiaObject, ProcessingContext processingContext)
        throws ClassNotFoundException {
        AbstractJahiaObjectBean resultObject = null;
        if (!jahiaObjectToBeanClassNames.containsKey(jahiaObject.getClass().getName())) {
            throw new ClassNotFoundException("No class defined for JahiaObject class name [" +
                                             jahiaObject.getClass().getName() + "]");
        }
        try {
            Class<? extends AbstractJahiaObjectBean> childClass = Class.forName( jahiaObjectToBeanClassNames.
                                             get(jahiaObject.getClass().getName())).asSubclass(AbstractJahiaObjectBean.class);
            Class<?>[] childClassParameters = new Class[2];
            childClassParameters[0] = JahiaObject.class;
            childClassParameters[1] = ProcessingContext.class;
            java.lang.reflect.Method childClassMethod = childClass.
                getMethod("getChildInstance", childClassParameters);
            Object[] args = new Object[2];
            args[0] = jahiaObject;
            args[1] = processingContext;
            resultObject = (AbstractJahiaObjectBean) childClassMethod.invoke(null, args);
        } catch (ClassNotFoundException cnfe) {
            logger.error("Error while creating instance of object " +
                         jahiaObject, cnfe);
        } catch (NoSuchMethodException nsme) {
            logger.error("Error while creating instance of object " +
                         jahiaObject, nsme);
        } catch (SecurityException se) {
            logger.error("Error while creating instance of object " +
                         jahiaObject, se);
        } catch (IllegalAccessException iae) {
            logger.error("Error while creating instance of object " +
                         jahiaObject, iae);
        } catch (IllegalArgumentException iae2) {
            logger.error("Error while creating instance of object " +
                         jahiaObject, iae2);
        } catch (InvocationTargetException ite) {
            logger.error("Error while creating instance of object " +
                         jahiaObject, ite);
            logger.error(
                "Error while creating instance of object " + jahiaObject +
                ", target exception="
                , ite.getTargetException());
        }
        return resultObject;
    }

}