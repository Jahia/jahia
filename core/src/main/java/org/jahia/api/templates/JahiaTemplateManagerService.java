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
package org.jahia.api.templates;

import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.exceptions.JahiaException;
import org.jahia.services.templates.TemplatePackageRegistry;
import org.osgi.framework.Bundle;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Template and template set deployment and management service.
 */
public interface JahiaTemplateManagerService {

    TemplatePackageRegistry getTemplatePackageRegistry();

    List<String> getNonManageableModules();

    /**
     * get List of installed modules for a site.
     *
     * @param siteKey                       key of the site
     * @param includeTemplateSet            if true (default is false) include dependencies of the template set
     * @param includeDirectDependencies     if true (default is false) include dependencies of dependencies
     * @param includeTransitiveDependencies if true (default is false) include all dependencies
     * @return list of template packages
     * @throws JahiaException
     */
    List<JahiaTemplatesPackage> getInstalledModulesForSite(String siteKey, boolean includeTemplateSet, boolean includeDirectDependencies, boolean includeTransitiveDependencies) throws JahiaException;

    /**
     * Returns a list of all available template packages.
     *
     * @return a list of all available template packages
     */
    List<JahiaTemplatesPackage> getAvailableTemplatePackages();

    List<JahiaTemplatesPackage> getNonSystemTemplateSetPackages();

    List<JahiaTemplatesPackage> getNonSystemModulePackages();

    /**
     * Returns the number of available template packages in the registry.
     *
     * @return the number of available template packages in the registry
     */
    int getAvailableTemplatePackagesCount();

    /**
     * Returns the requested template package for the specified site or
     * <code>null</code> if the package with the specified Id is not
     * registered in the repository.
     *
     * @param moduleId the template package Id to search for
     * @return the requested template package or <code>null</code> if the
     * package with the specified Id is not registered in the
     * repository
     */
    JahiaTemplatesPackage getTemplatePackageById(String moduleId);

    /**
     * Returns a set of all available template packages having templates for a module.
     *
     * @return a set of all available template packages
     */
    Set<JahiaTemplatesPackage> getModulesWithViewsForComponent(String componentName);

    /**
     * Returns the requested template package for the specified site or
     * <code>null</code> if the package with the specified name is not
     * registered in the repository.
     *
     * @param packageName the template package name to search for
     * @return the requested template package or <code>null</code> if the
     * package with the specified name is not registered in the
     * repository
     */
    JahiaTemplatesPackage getTemplatePackage(String packageName);

    /**
     * Returns the lookup map for template packages by the JCR node name.
     *
     * @return the lookup map for template packages by the JCR node name
     */
    @SuppressWarnings("unchecked")
    Map<String, JahiaTemplatesPackage> getTemplatePackageByNodeName();

    JahiaTemplatesPackage getAnyDeployedTemplatePackage(String templatePackage);

    /**
     * Returns a set of existing template sets that are available for site creation.
     *
     * @return a set of existing template sets that are available for site creation
     */
    Set<String> getTemplateSetNames();

    Map<Bundle, JahiaTemplatesPackage> getRegisteredBundles();
}
