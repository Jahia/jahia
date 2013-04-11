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
package org.jahia.modules.serversettings.flow;

import org.apache.commons.io.FilenameUtils;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.exceptions.JahiaException;
import org.jahia.modules.serversettings.moduleManagement.ModuleFile;
import org.jahia.services.content.JCRTemplate;
import org.jahia.services.content.decorator.JCRSiteNode;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.services.render.RenderContext;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.sites.JahiaSitesService;
import org.jahia.services.templates.JahiaTemplateManagerService;
import org.jahia.services.templates.ModuleVersion;
import org.jahia.settings.SettingsBean;
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
import java.io.IOException;
import java.io.Serializable;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 *
 * @author : rincevent
 * @since : JAHIA 6.1
 *        Created : 08/04/13
 */
public class ModuleManagementFlowHandler implements Serializable {
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
            context.addMessage(new MessageBuilder().error().source("moduleFile").defaultText("File should be a jar or a war file").build());
            return false;
        }
        try {
            final File file = new File(SettingsBean.getInstance().getJahiaModulesDiskPath(),originalFilename);
            moduleFile.getModuleFile().transferTo(file);

            context.addMessage(new MessageBuilder().source("moduleFile").defaultText("Module has been successfully uploaded. Check status in the list").build());
        } catch (IOException e) {
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
        //populate information about sites
        List<String> siteKeys = new ArrayList<String>();
        List<String> directSiteDep = new ArrayList<String>();
        List<String> templateSiteDep = new ArrayList<String>();
        List<String> transitiveSiteDep = new ArrayList<String>();
        try {
            List<JCRSiteNode> sites = sitesService.getSitesNodeList();
            for (JCRSiteNode site : sites) {
                siteKeys.add(site.getSiteKey());
                List<JahiaTemplatesPackage> directDependencies = templateManagerService.getInstalledModulesForSite(
                        site.getSiteKey(), false, true, false);
                for (JahiaTemplatesPackage directDependency : directDependencies) {
                    if(directDependency.getRootFolder().equals(selectedModuleName)) {
                        directSiteDep.add(site.getSiteKey());
                    }
                }
                List<JahiaTemplatesPackage> templateDependencies = templateManagerService.getInstalledModulesForSite(
                        site.getSiteKey(), true, false, false);
                for (JahiaTemplatesPackage templateDependency : templateDependencies) {
                    if(templateDependency.getRootFolder().equals(selectedModuleName)){
                        templateSiteDep.add(site.getSiteKey());
                    }
                }
                List<JahiaTemplatesPackage> transitiveDependencies = templateManagerService.getInstalledModulesForSite(
                        site.getSiteKey(), false, false, true);
                for (JahiaTemplatesPackage transitiveDependency : transitiveDependencies) {
                    if(transitiveDependency.getRootFolder().equals(selectedModuleName)){
                        transitiveSiteDep.add(site.getSiteKey());
                    }
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
        context.getRequestScope().put("sitesTransitive",transitiveSiteDep);
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
    }
}
