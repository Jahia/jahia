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
package org.jahia.modules.serversettings.flow;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.exceptions.JahiaException;
import org.jahia.modules.serversettings.moduleManagement.ModuleFile;
import org.jahia.osgi.BundleUtils;
import org.jahia.osgi.FrameworkService;
import org.jahia.services.content.decorator.JCRSiteNode;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.services.render.RenderContext;
import org.jahia.services.sites.JahiaSitesService;
import org.jahia.services.templates.JahiaTemplateManagerService;
import org.jahia.services.templates.ModuleVersion;
import org.jahia.utils.i18n.Messages;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.binding.message.MessageBuilder;
import org.springframework.binding.message.MessageContext;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.webflow.execution.RequestContext;

import javax.jcr.nodetype.NodeTypeIterator;
import javax.jcr.RepositoryException;
import java.io.File;
import java.io.Serializable;
import java.net.URL;
import java.util.*;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

/**
 * @author rincevent
 */
public class ModuleManagementFlowHandler implements Serializable {
    private static final long serialVersionUID = -4195379181264451784L;

    private static Logger logger = LoggerFactory.getLogger(ModuleManagementFlowHandler.class);

    @Autowired
    private transient JahiaTemplateManagerService templateManagerService;

    @Autowired
    private transient JahiaSitesService sitesService;

    private String moduleName;

    public boolean isInModule(RenderContext renderContext) {
        try {
            if (renderContext.getMainResource().getNode().isNodeType("jnt:module")) {
                moduleName = renderContext.getMainResource().getNode().getName();
                return true;
            }
        } catch (RepositoryException e) {
        }
        return false;
    }

    public ModuleFile initModuleFile() {
        return new ModuleFile();
    }

    public boolean uploadModule(ModuleFile moduleFile, MessageContext context) {
        String originalFilename = moduleFile.getModuleFile().getOriginalFilename();
        if (!FilenameUtils.isExtension(originalFilename, Arrays.<String>asList("war","jar","WAR","JAR"))) {
            context.addMessage(new MessageBuilder().error().source("moduleFile")
                    .defaultText(getMessage("serverSettings.manageModules.install.wrongFormat")).build());
            return false;
        }
        try {
            final File file = File.createTempFile("module-", "."+StringUtils.substringAfterLast(originalFilename,"."));
            moduleFile.getModuleFile().transferTo(file);
            Manifest manifest = new JarFile(file).getManifest();
            String symbolicName = manifest.getMainAttributes().getValue("Bundle-SymbolicName");
            if (symbolicName == null) {
                symbolicName = manifest.getMainAttributes().getValue("root-folder");
            }
            String version = manifest.getMainAttributes().getValue("Implementation-Version");
            Bundle bundle = templateManagerService.findBundle(symbolicName, version);

            String location = file.toURI().toString();
            if (file.getName().toLowerCase().endsWith(".war")) {
                location = "jahiawar:"+location;
            }

            if (bundle != null) {
                bundle.update(new URL(location).openStream());
            } else {
                bundle = FrameworkService.getBundleContext().installBundle(location, new URL(location).openStream());
            }
            List<String> deps = BundleUtils.getModule(bundle).getDepends();
            List<String> missingDeps = new ArrayList<String>();
            for (String dep : deps) {
                if (templateManagerService.getTemplatePackageByFileName(dep) == null && templateManagerService.getTemplatePackage(dep) == null) {
                    missingDeps.add(dep);
                }
            }
            if (!missingDeps.isEmpty()) {
                context.addMessage(new MessageBuilder().source("moduleFile")
                        .defaultText(getMessage("serverSettings.manageModules.install.missingDependencies"))
                        .arg(StringUtils.join(missingDeps, ","))
                        .error()
                        .build());
            } else {
                Set<ModuleVersion> allVersions = templateManagerService.getTemplatePackageRegistry().getAvailableVersionsForModule(bundle.getSymbolicName());
                if (allVersions.contains(new ModuleVersion(version)) && allVersions.size() == 1) {
                    bundle.start();
                    context.addMessage(new MessageBuilder().source("moduleFile")
                            .defaultText(getMessage("serverSettings.manageModules.install.uploadedAndStarted"))
                            .build());
                } else {
                    context.addMessage(new MessageBuilder().source("moduleFile")
                            .defaultText(getMessage("serverSettings.manageModules.install.uploaded"))
                            .build());
                }
            }
        } catch (Exception e) {
            context.addMessage(new MessageBuilder().source("moduleFile")
                    .defaultText(getMessage("serverSettings.manageModules.install.failed"))
                    .arg(e.getMessage())
                    .error()
                    .build());
            logger.error(e.getMessage(), e);
        }
        return true;
    }

    public void loadModuleInformation(RequestContext context) {
        String selectedModuleName = moduleName != null ? moduleName : (String) context.getFlowScope().get("selectedModule");
        Map<ModuleVersion, JahiaTemplatesPackage> selectedModule = templateManagerService.getTemplatePackageRegistry().getAllModuleVersions().get(
                selectedModuleName);
        if(selectedModule.size()>1) {
            boolean foundActiveVersion = false;
            for (Map.Entry<ModuleVersion, JahiaTemplatesPackage> entry : selectedModule.entrySet()) {
                JahiaTemplatesPackage value = entry.getValue();
                if (value.isActiveVersion()) {
                    foundActiveVersion = true;
                    populateActiveVersion(context, value);
                }
            }
            if(!foundActiveVersion) {
                // there is no active version take information from most recent installed version
                LinkedList<ModuleVersion> sortedVersions = new LinkedList<ModuleVersion>(selectedModule.keySet());
                Collections.sort(sortedVersions);
                populateActiveVersion(context, selectedModule.get(sortedVersions.getFirst()));
            }
        }
        else {
            populateActiveVersion(context, selectedModule.values().iterator().next());
        }
        context.getRequestScope().put("otherVersions",selectedModule);

        populateSitesInformation(context);

        // Get list of definitions
        NodeTypeIterator nodeTypes = NodeTypeRegistry.getInstance().getNodeTypes(selectedModuleName);
        Map<String,Boolean> booleanMap = new TreeMap<String, Boolean>();
        while (nodeTypes.hasNext()) {
            ExtendedNodeType nodeType = (ExtendedNodeType) nodeTypes.next();
            booleanMap.put(nodeType.getLabel(LocaleContextHolder.getLocale()),nodeType.isNodeType(
                    "jmix:droppableContent"));
        }
        context.getRequestScope().put("nodeTypes", booleanMap);
    }

    public void populateSitesInformation(RequestContext context) {
        //populate information about sites
        List<String> siteKeys = new ArrayList<String>();
        Map<String,List<String>> directSiteDep = new HashMap<String,List<String>>();
        Map<String,List<String>> templateSiteDep = new HashMap<String,List<String>>();
        Map<String,List<String>> transitiveSiteDep = new HashMap<String,List<String>>();
        try {
            List<JCRSiteNode> sites = sitesService.getSitesNodeList();
            for (JCRSiteNode site : sites) {
                siteKeys.add(site.getSiteKey());
                List<JahiaTemplatesPackage> directDependencies = templateManagerService.getInstalledModulesForSite(
                        site.getSiteKey(), false, true, false);
                for (JahiaTemplatesPackage directDependency : directDependencies) {
                    if(!directSiteDep.containsKey(directDependency.getRootFolder())) {
                        directSiteDep.put(directDependency.getRootFolder(), new ArrayList<String>());
                    }
                    directSiteDep.get(directDependency.getRootFolder()).add(site.getSiteKey());
                }
                if (site.getTemplatePackage() != null) {
                    if(!templateSiteDep.containsKey(site.getTemplatePackage().getRootFolder())) {
                        templateSiteDep.put(site.getTemplatePackage().getRootFolder(), new ArrayList<String>());
                    }
                    templateSiteDep.get(site.getTemplatePackage().getRootFolder()).add(site.getSiteKey());
                }
                List<JahiaTemplatesPackage> transitiveDependencies = templateManagerService.getInstalledModulesForSite(
                        site.getSiteKey(), true, false, true);
                for (JahiaTemplatesPackage transitiveDependency : transitiveDependencies) {
                    if(!transitiveSiteDep.containsKey(transitiveDependency.getRootFolder())) {
                        transitiveSiteDep.put(transitiveDependency.getRootFolder(), new ArrayList<String>());
                    }
                    transitiveSiteDep.get(transitiveDependency.getRootFolder()).add(site.getSiteKey());
                }
            }
        } catch (JahiaException e) {
            logger.error(e.getMessage(), e);
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
        context.getRequestScope().put("sites",siteKeys);
        context.getRequestScope().put("sitesDirect",directSiteDep);
        context.getRequestScope().put("sitesTemplates",templateSiteDep);
        context.getRequestScope().put("sitesTransitive", transitiveSiteDep);
    }

    private void populateActiveVersion(RequestContext context, JahiaTemplatesPackage value) {
        context.getRequestScope().put("activeVersion", value);
        Map<String,String> bundleInfo = new HashMap<String, String>();
        Dictionary<String,String> dictionary = value.getBundle().getHeaders();
        Enumeration<String> keys = dictionary.keys();
        while (keys.hasMoreElements()) {
            String s = keys.nextElement();
            bundleInfo.put(s,dictionary.get(s));
        }
        context.getRequestScope().put("bundleInfo", bundleInfo);
        context.getRequestScope().put("activeVersionDate",new Date(value.getBundle().getLastModified()));

        context.getRequestScope().put("dependantModules", templateManagerService.getTemplatePackageRegistry().getDependantModules(value));
    }

    private String getMessage(String key) {
        return Messages.get("resources.JahiaServerSettings", key, LocaleContextHolder.getLocale());
    }

    /**
     * Logs the specified exception details.
     *
     * @param e
     *            the occurred exception to be logged
     */
    public void logError(Exception e) {
        logger.error(e.getMessage(), e);
    }
}
