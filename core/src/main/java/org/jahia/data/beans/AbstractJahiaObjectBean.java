/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2017 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
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
 package org.jahia.data.beans;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


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

    private static Map<String, String> jahiaObjectToBeanClassNames = new ConcurrentHashMap<String, String>();

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

}