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
            logger.info("Unable to lookup class " + className + " for the job scheduler.");
            // fallback to NoOpJob
            loadModuleClass = NoOpJob.class;
        }

        return loadModuleClass;
    }
}
