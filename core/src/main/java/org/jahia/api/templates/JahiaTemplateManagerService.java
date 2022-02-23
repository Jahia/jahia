/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
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
