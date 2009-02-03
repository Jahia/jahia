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

package org.jahia.services.templates;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.net.MalformedURLException;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipFile;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jahia.bin.Jahia;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.exceptions.JahiaTemplateServiceException;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.JahiaService;
import org.jahia.services.cache.Cache;
import org.jahia.services.pages.JahiaPageTemplateBaseService;
import org.jahia.services.pages.JahiaPageTemplateService;
import org.jahia.services.pages.JahiaPageService;
import org.jahia.services.search.JahiaSearchBaseService;
import org.jahia.services.search.JahiaSearchService;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.sites.JahiaSitesService;
import org.jahia.utils.zip.JahiaArchiveFileHandler;

/**
 * Template and template set deployment and management service.
 *
 * @author Sergiy Shyrkov
 */
public class JahiaTemplateManagerService extends JahiaService {

    private static Logger logger = Logger
            .getLogger(JahiaTemplateManagerService.class);

    public static final String MULTIPLE_PACKAGE_HANDLER = "multiple-package-handler.info";

    private TemplatePackageDeployer templatePackageDeployer;

    private TemplatePackageRegistry templatePackageRegistry;

    private PageDefinitionHelper pageDefinitionHelper;

    private JahiaPageService pageService;

    private JahiaSitesService siteService;

    private JahiaPageTemplateService tplService;
    
    private JahiaSearchService searchService;    

    public void setSiteService(JahiaSitesService siteService) {
        this.siteService = siteService;
    }
    
    public void setSearchService(JahiaSearchService searchService) {
        this.searchService = searchService;
    }    

    public void setTplService(JahiaPageTemplateService tplService) {
        this.tplService = tplService;
    }

    public void setPageService(JahiaPageService pageService) {
        this.pageService = pageService;
    }

    public JahiaTemplatesPackage associateTemplatePackageWithSite(
            String templatePackageName, JahiaSite site) throws JahiaException {
        JahiaTemplatesPackage templatePackage = getTemplatePackage(templatePackageName);
        if (templatePackage == null) {
            throw new JahiaTemplateServiceException(
                    "Template package with the name '" + templatePackageName
                            + "' cannot be found in the registry");
        }

        // store package name in the site's properties
        site.setTemplatePackageName(templatePackage.getName());

        // first create or update page definitions for the site
        pageDefinitionHelper.updatePageDefinitions(site, siteService, pageService, templatePackage, tplService);

        // set default template
        pageDefinitionHelper.setDefaultTemplate(site, templatePackage);

        return templatePackage;
    }

    private boolean containsMultipleTemplatePackages(File templateJar) {
        boolean multiplePackages = false;
        try {
            ZipFile zipHandler = new ZipFile(templateJar);
            // do we have templates.xml inside?
            multiplePackages = zipHandler.getEntry(MULTIPLE_PACKAGE_HANDLER) != null;
        } catch (Exception ex) {
            // ignore
        }
        return multiplePackages;
    }

    /**
     * Deploys multiple template sets from the specified JAR file that is
     * located in the templates folder.
     *
     * @param templateJar the file of the template package JAR file
     * @return the name of the deployed package
     * @throws JahiaException in case of an error
     */
    private String deployMultipleTemplatePackagesForSite(JahiaSite site,
                                                         File templateJar) throws JahiaException {

        String packageName = null;

        // check first, if we deal with multiple template packages
        try {
            JahiaArchiveFileHandler zipHandler = new JahiaArchiveFileHandler(
                    templateJar.getPath());
            // retrieve package info
            String[] multiplePackageInfo = StringUtils.split(zipHandler
                    .extractContent(MULTIPLE_PACKAGE_HANDLER), ",");

            String parentPackageName = null;
            JahiaTemplatesPackage deployedPackage = null;
            // iterate over template set JARs
            for (String pkgJar : multiplePackageInfo) {
                if (zipHandler.getEntry(pkgJar) != null) {
                    File templatesFile = zipHandler.extractFile(pkgJar);
                    // deploy it
                    String deployedPackageName = templatePackageDeployer
                            .deployTemplatePackage(templatesFile, true, site
                                    .getSiteKey());
                    try {
                        templatesFile.delete();
                    } catch (Exception e) {
                        // ignore
                    }
                    deployedPackage = templatePackageRegistry
                            .lookup(deployedPackageName);
                    // check if the parent package name was changed during
                    // deployment
                    if (parentPackageName != null
                            && !deployedPackage.getExtends().equals(
                            parentPackageName)) {
                        // set proper parent package name
                        deployedPackage.setExtends(parentPackageName);
                        // and update deployment descriptor
                        TemplateDeploymentDescriptorHelper.serialize(
                                deployedPackage, new File(settingsBean
                                .getJahiaTemplatesDiskPath(),
                                deployedPackage.getRootFolder()));
                    }
                    parentPackageName = deployedPackageName;
                } else {
                    throw new JahiaTemplateServiceException(
                            "Required JAR file '"
                                    + pkgJar
                                    + "' is not found in the templates.jar archive.");
                }
            }

            if (deployedPackage != null) {
                packageName = deployedPackage.getName();
                templatePackageRegistry.resolveInheritance(deployedPackage);
            }
        } catch (IOException e) {
            throw new JahiaTemplateServiceException(
                    "Unable to deploy multiple package set from", e);
        }

        return packageName;
    }

    /**
     * Deploys the template set (or multiple template sets) from the specified
     * JAR file that is located in the templates folder.
     *
     * @param templateJar the file of the template package JAR file
     * @throws JahiaException in case of an error
     */
    public void deployTemplatePackageForSite(JahiaSite site, File templateJar)
            throws JahiaException {
        String pkgName = null;

        // check first, if we deal with multiple template packages
        if (containsMultipleTemplatePackages(templateJar)) {
            pkgName = deployMultipleTemplatePackagesForSite(site, templateJar);
        } else {
            pkgName = templatePackageDeployer.deployTemplatePackage(
                    templateJar, true, site.getSiteKey());
        }
        associateTemplatePackageWithSite(pkgName, site);
    }

    /**
     * Returns a list of all available template packages.
     *
     * @return a list of all available template packages
     */
    public List<JahiaTemplatesPackage> getAvailableTemplatePackages() {
        return templatePackageRegistry.getAvailablePackages();
    }

    /**
     * Returns the number of available template packages in the registry.
     *
     * @return the number of available template packages in the registry
     */
    public int getAvailableTemplatePackagesCount() {
        return templatePackageRegistry.getAvailablePackagesCount();
    }

    /**
     * Returns the default template package (the name is configured in the
     * <code>jahia.properties</code> file).
     *
     * @return the default template package (the name is configured in the
     *         <code>jahia.properties</code> file)
     */
    public JahiaTemplatesPackage getDefaultTemplatePackage() {
        String defSetName = settingsBean.lookupString("default_templates_set");
        return defSetName != null
                && templatePackageRegistry.lookup(defSetName) != null ? templatePackageRegistry
                .lookup(defSetName)
                : (JahiaTemplatesPackage) templatePackageRegistry
                        .getAvailablePackages().get(0);
    }

    public String getTemplateDisplayName(String templateName, int siteId) {
        String displayName = templateName;
        JahiaSite site = null;
        try {
            site = siteService.getSite(siteId);
        } catch (JahiaException e) {
            logger.error("Unable to lookup the site for the ID=" + siteId, e);
        }
        if (site != null) {
            JahiaTemplatesPackage pkg = getTemplatePackage(site
                    .getTemplatePackageName());
            if (pkg != null && pkg.getTemplateMap().containsKey(templateName)) {
                displayName = pkg.lookupTemplate(templateName).getDisplayName();
            }
        }
        if (null == displayName) {
            logger
                    .warn("Unable to lookup display name for the template named '"
                            + templateName + "' in the site with ID=" + siteId);
        }

        return displayName;
    }

    public String getTemplateDescription(String templateName, int siteId) {
        String description = null;
        JahiaSite site = null;
        try {
            site = siteService.getSite(siteId);
        } catch (JahiaException e) {
            logger.error("Unable to lookup the site for the ID=" + siteId, e);
        }
        if (site != null) {
            JahiaTemplatesPackage pkg = getTemplatePackage(site
                    .getTemplatePackageName());
            if (pkg != null && pkg.getTemplateMap().containsKey(templateName)) {
                description = pkg.lookupTemplate(templateName).getDescription();
            }
        }
        return description;
    }

    /**
     * Returns the requested template package or <code>null</code> if the
     * package can not be found in the registry.
     *
     * @param siteId the ID of the site
     * @return the requested template package or <code>null</code> if the
     *         package can not be found in the registry
     */
    public JahiaTemplatesPackage getTemplatePackage(int siteId) {
        JahiaTemplatesPackage pkg = null;
        JahiaSite site = null;
        try {
            site = siteService.getSite(siteId);
        } catch (JahiaException e) {
            logger.error("Unablke to find site by ID=" + siteId, e);
        }
        if (site != null) {
            String pkgName = site.getTemplatePackageName();
            if (pkgName != null) {
                pkg = templatePackageRegistry.lookup(pkgName);
            }
        }

        return pkg;
    }

    /**
     * Returns the requested template package for the specified site or
     * <code>null</code> if the package with the specified name is not
     * registered in the repository.
     *
     * @param packageName the template package name to search for
     * @return the requested template package or <code>null</code> if the
     *         package with the specified name is not registered in the
     *         repository
     */
    public JahiaTemplatesPackage getTemplatePackage(String packageName) {
        return templatePackageRegistry.lookup(packageName);
    }

    /**
     * Returns the requested template package for the specified site or
     * <code>null</code> if the package with the specified fileName is not
     * registered in the repository.
     *
     * @param fileName the template package fileName to search for
     * @return the requested template package or <code>null</code> if the
     *         package with the specified name is not registered in the
     *         repository
     */
    public JahiaTemplatesPackage getTemplatePackageByFileName(String fileName) {
        return templatePackageRegistry.lookupByFileName(fileName);
    }

    public String getTemplateSourcePath(String templateName, int siteId) {
        String sourcePath = null;
        JahiaTemplatesPackage pkg = getTemplatePackage(siteId);
        if (pkg != null && pkg.getTemplateMap().containsKey(templateName)) {
            sourcePath = pkg.lookupTemplate(templateName).getFilePath();
        }
        if (null == sourcePath) {
            logger
                    .warn("Unable to lookup the source path for the template named '"
                            + templateName + "' in the site with ID=" + siteId);
        }

        return sourcePath;
    }

    /**
     * Returns the resource path (related to the Web context), resolved using
     * template package inheritance or <code>null</code>, in case the
     * specified resource can not found. The resource is first searched in the
     * current template package folder, then (if not found) in the parent
     * package folder and so on.
     *
     * @param resource the resource name or path related to the template package root
     *                 folder
     * @param siteId   current site ID
     * @return the resource path (related to the Web context), resolved using
     *         template package inheritance or <code>null</code>, in case the
     *         specified resource can not found
     */
    public String resolveResourcePath(String resource, int siteId) {
        return resolveResourcePath(resource, getTemplatePackage(siteId));
    }

    /**
     * Returns the resource path (related to the Web context), resolved using
     * template package inheritance or <code>null</code>, in case the
     * specified resource can not found. The resource is first searched in the
     * current template package folder, then (if not found) in the parent
     * package folder and so on.
     *
     * @param resource the resource name or path related to the template package root
     *                 folder
     * @param pkg      current template package
     * @return the resource path (related to the Web context), resolved using
     *         template package inheritance or <code>null</code>, in case the
     *         specified resource can not found
     */
    private String resolveResourcePath(String resource,
                                       JahiaTemplatesPackage pkg) {
        String path = null;
        if (pkg != null) {
            for (Iterator iterator = pkg.getLookupPath().iterator(); iterator
                    .hasNext();) {
                String rootFolderPath = (String) iterator.next();

                try {
                    StringBuffer buff = new StringBuffer(64);
                    buff.append(rootFolderPath);
                    if (!resource.startsWith("/")) {
                        buff.append("/");
                    }
                    buff.append(resource);
                    String testPath = buff.toString();
                    if (Jahia.getStaticServletConfig().getServletContext()
                            .getResource(testPath) != null) {
                        path = testPath;
                        break;
                    }
                } catch (MalformedURLException e) {
                    logger.warn("Unable to resolve resource path for '"
                            + resource + "' and template set package '" + pkg.getName()
                            + "'", e);
                }
            }
        }

        return path;
    }

    /**
     * Returns the resource path (related to the Web context), resolved using
     * template package inheritance or <code>null</code>, in case the
     * specified resource can not found. The resource is first searched in the
     * current template package folder, then (if not found) in the parent
     * package folder and so on.
     *
     * @param resource            the resource name or path related to the template package root
     *                            folder
     * @param templatePackageName template set name of the current site
     * @return the resource path (related to the Web context), resolved using
     *         template package inheritance or <code>null</code>, in case the
     *         specified resource can not found
     */
    public String resolveResourcePath(String resource,
                                      String templatePackageName) {
        JahiaTemplatesPackage currentPkg = getTemplatePackage(templatePackageName);
        if (currentPkg == null) {
            currentPkg = getTemplatePackageByFileName(templatePackageName);
        }
        return resolveResourcePath(resource, currentPkg);
    }

    /**
     * Returns the overridden resource path (related to the Web context),
     * resolved using template package inheritance or <code>null</code>, in
     * case the specified resource can not found. I.e. it returns the resource
     * that is overridden by the specified resource in any of the parent template
     * sets.
     *
     * @param jspServletPath      the resource name or path related to the template package root
     *                            folder
     * @param templatePackageName template set name of the current site
     * @return the overridden resource path (related to the Web context),
     *         resolved using template package inheritance or <code>null</code>,
     *         in case the specified resource can not found
     * @see org.jahia.taglibs.include.ExecuteSuperTag
     */
    public String resolveOverriddenResourcePathByServletPath(String jspServletPath,
                                                             String templatePackageName) {

        if (null == jspServletPath || null == templatePackageName) {
            throw new IllegalArgumentException(
                    "Either servletPath, templatePackageName or both are null");
        }

        String path = null;

        JahiaTemplatesPackage currentPkg = getTemplatePackage(templatePackageName);
        if (currentPkg == null) {
            currentPkg = getTemplatePackageByFileName(templatePackageName);
        }
        if (currentPkg != null) {
            JahiaTemplatesPackage foundPackage = null;
            String resource = null;
            for (String pkgName : currentPkg.getHierarchy()) {
                JahiaTemplatesPackage pkg = getTemplatePackage(pkgName);
                String rootFolder = pkg.getRootFolderPath();
                if (jspServletPath.startsWith(rootFolder)) {
                    // found the template package
                    foundPackage = pkg;
                    resource = jspServletPath.substring(rootFolder.length());
                    break;
                }
            }
            if (foundPackage != null && foundPackage.getExtends() != null) {
                path = resolveResourcePath(resource, foundPackage.getExtends());
            }
        }

        return path;
    }

    public void serializeTemplateDeploymentDescriptor(
            JahiaTemplatesPackage pkg, Writer writer)
            throws JahiaTemplateServiceException {
        TemplateDeploymentDescriptorHelper.serialize(pkg, writer);
    }

    public void setTemplatePackageDeployer(TemplatePackageDeployer deployer) {
        templatePackageDeployer = deployer;
    }

    public void setTemplatePackageRegistry(TemplatePackageRegistry registry) {
        templatePackageRegistry = registry;
    }

    public void start() throws JahiaInitializationException {
        logger.info("Starting JahiaTemplateManagerService ...");

        // scan the directory for templates
        templatePackageDeployer.scanDeployedTemplatePackages();

        // deploy shared templates if not deployed yet
        templatePackageDeployer.scanSharedTemplatePackages();

        // build the hierarchy, resolving inheritance
        templatePackageRegistry.buildHierarchy();

        // TODO validate template sets: count, package dependencies etc.
        templatePackageRegistry.validate();

        ((JahiaPageTemplateBaseService)tplService).setTemplateManagerService(this);
        
        ((JahiaSearchBaseService)searchService).initSearchFieldConfiguration();        

        // update all page definitions
        try {
            pageDefinitionHelper.updateAllPageDefinitions(pageService, siteService, tplService);
        } catch (JahiaException ex) {
            logger.error("Error updating all page definitions", ex);
        }

        // start template deployment descriptor watcher
        templatePackageRegistry.startWatchdog();

        logger.info("JahiaTemplateManagerService started successfully."
                + " Total number of found template packages: "
                + templatePackageRegistry.getAvailablePackagesCount());
    }

    public void stop() throws JahiaException {
        logger.info("Stopping JahiaTemplateManagerService ...");

        // stop template deployment descriptor watcher
        templatePackageRegistry.stopWatchdog();

        templatePackageRegistry.reset();
        Cache templateCache = ServicesRegistry.getInstance().getCacheService()
                .getCache(JahiaPageTemplateBaseService.PAGE_TEMPLATE_CACHE);
        if (templateCache != null) {
            templateCache.flush();
        }

        logger.info("... JahiaTemplateManagerService stopped successfully");
    }

    public void setPageDefinitionHelper(
            PageDefinitionHelper pageDefinitionHelper) {
        this.pageDefinitionHelper = pageDefinitionHelper;
    }
    
    public String getCurrentResourceBundleName(ProcessingContext ctx) {
        return getTemplatePackage(ctx.getSite().getTemplatePackageName())
                .getResourceBundleName();
    } 
    
}