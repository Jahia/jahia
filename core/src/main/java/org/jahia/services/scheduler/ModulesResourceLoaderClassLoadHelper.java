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
package org.jahia.services.scheduler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jahia.osgi.BundleUtils;
import org.osgi.framework.Bundle;
import org.quartz.jobs.NoOpJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.quartz.ResourceLoaderClassLoadHelper;

/**
 * Implementation of the {@link ResourceLoaderClassLoadHelper} utility class for loading of job and trigger related classes from Jahia
 * modules.
 * 
 * @author Thomas Draier
 * @author Sergiy Shyrkov
 */
public class ModulesResourceLoaderClassLoadHelper extends ResourceLoaderClassLoadHelper {
    
    private static final Logger logger = LoggerFactory.getLogger(ModulesResourceLoaderClassLoadHelper.class);

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
        Class<?> loadModuleClass = null;
        try {
            loadModuleClass = BundleUtils.loadModuleClass(className, Bundle.ACTIVE);
        } catch (ClassNotFoundException e) {
            logger.error("Unable to lookup class " + className + " for the job scheduler.", e);
            // fallback to NoOpJob
            loadModuleClass = NoOpJob.class;
        }
        
        return loadModuleClass;
    }
}
