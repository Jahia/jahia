/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2013 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.services.scheduler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jahia.osgi.BundleUtils;
import org.springframework.scheduling.quartz.ResourceLoaderClassLoadHelper;

/**
 * Implementation of the {@link ResourceLoaderClassLoadHelper} utility class for loading of job and trigger related classes from Jahia
 * modules.
 * 
 * @author Thomas Draier
 * @author Sergiy Shyrkov
 */
public class ModulesResourceLoaderClassLoadHelper extends ResourceLoaderClassLoadHelper {

    private Map<String, Boolean> coreClassesChecks = new ConcurrentHashMap<String, Boolean>();

    @Override
    public Class<?> loadClass(String className) throws ClassNotFoundException {
        Boolean result = coreClassesChecks.get(className);

        if (result == null) {
            // we have not checked yet if this is a core class
            try {
                Class<?> clazz = super.loadClass(className);
                coreClassesChecks.put(className, Boolean.TRUE);
                return clazz;
            } catch (ClassNotFoundException e) {
                // not present in core -> will look it up in modules
                coreClassesChecks.put(className, Boolean.FALSE);
            }
        } else if (result == true) {
            // this class comes from main class loader -> load it
            return super.loadClass(className);
        }

        // lookup class in module class loaders
        return BundleUtils.loadModuleClass(className);
    }
}
