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

package org.jahia.services.render.scripting;

import org.jahia.bin.listeners.JahiaContextLoaderListener;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.services.content.JCRContentUtils;
import org.jahia.services.content.decorator.JCRSiteNode;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.services.render.*;
import org.jahia.services.templates.JahiaTemplateManagerService;
import org.jahia.services.templates.JahiaTemplateManagerService.ModuleDeployedOnSiteEvent;
import org.jahia.services.templates.JahiaTemplateManagerService.TemplatePackageRedeployedEvent;
import org.jahia.settings.SettingsBean;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;

import javax.jcr.RepositoryException;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import java.io.File;
import java.net.MalformedURLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class resolves the template to which we will dispatch to, in the web application file system.
 * <p/>
 * It will try to resolve the template from the following schema :
 * <p/>
 * /templates/[currentTemplateSet]/modules/[nodetypenamespace]/[nodetypename]/[templatetype]/[templatename].[file_extension]
 * /templates/[parentTemplateSet]/modules/[nodetypenamespace]/[nodetypename]/[templatetype]/[templatename].[file_extension]
 * /templates/default/modules/[nodetypenamespace]/[nodetypename]/[templatetype]/[templatename].[file_extension]
 * <p/>
 * And then iterates on the supertype of the resource, until nt:base
 *
 * <p/>
 * [file_extension] is resolved based on the scriptExtensionsOrdering list, which is initialized in Spring.
 *
 * @author Thomas Draier
 */
public class FileSystemScriptResolver implements ScriptResolver, ApplicationListener<ApplicationEvent> {

    private static final String JSP_EXTENSION = "jsp";
    private static final String PHP_EXTENSION = "php";
    private List<String> scriptExtensionsOrdering;

    private static Map<String, Boolean> resourcesCache = new ConcurrentHashMap<String, Boolean>();
    private static Map<String, SortedSet<View>> viewSetCache = new ConcurrentHashMap<String, SortedSet<View>>();
    
    private JahiaTemplateManagerService templateManagerService;

    public List<String> getScriptExtensionsOrdering() {
        return scriptExtensionsOrdering;
    }

    public void setScriptExtensionsOrdering(List<String> scriptExtensionsOrdering) {
        this.scriptExtensionsOrdering = scriptExtensionsOrdering;
    }

    protected View resolveView(Resource resource, ArrayList<String> searchedLocations) throws RepositoryException {
        if (resource.getResourceNodeType() != null) {
            ExtendedNodeType nt = resource.getResourceNodeType();
            List<ExtendedNodeType> nodeTypeList = getNodeTypeList(nt);
            return resolveView(resource, nodeTypeList, searchedLocations);
        }

        ExtendedNodeType nt = resource.getNode().getPrimaryNodeType();
        List<ExtendedNodeType> nodeTypeList = getNodeTypeList(nt);
        for (ExtendedNodeType type : resource.getNode().getMixinNodeTypes()) {
            nodeTypeList.addAll(0,Arrays.asList(type.getSupertypes()));
            nodeTypeList.add(0,type);
        }
        

        View res = resolveView(resource, nodeTypeList, searchedLocations);
        if (res != null) {
            return res;
        }

        List<ExtendedNodeType> mixinNodeTypes = Arrays.asList(resource.getNode().getMixinNodeTypes());
        if (mixinNodeTypes.size() > 0) {
            res = resolveView(resource, mixinNodeTypes, searchedLocations);
            if (res != null) {
                return res;
            }
        }
        return null;
    }

    private List<ExtendedNodeType> getNodeTypeList(ExtendedNodeType nt) throws NoSuchNodeTypeException {
        List<ExtendedNodeType> extendedNodeTypes = Arrays.asList(nt.getSupertypes());
        List<ExtendedNodeType> nodeTypeList = new ArrayList<ExtendedNodeType>();
        nodeTypeList.add(nt);
        nodeTypeList.addAll(extendedNodeTypes);
        ExtendedNodeType base = NodeTypeRegistry.getInstance().getNodeType("nt:base");
        nodeTypeList.remove(base);
        nodeTypeList.add(base);
        return nodeTypeList;
    }

    private View resolveView(Resource resource, List<ExtendedNodeType> nodeTypeList, ArrayList<String> searchedLocations) {
        String template = resource.getResolvedTemplate();
            for (ExtendedNodeType st : nodeTypeList) {
                Set<JahiaTemplatesPackage> sortedPackages = templateManagerService.getAvailableTemplatePackagesForModule(
                        JCRContentUtils.replaceColon(st.getAlias()));
                View res = resolveView(resource, template, st, sortedPackages, searchedLocations);
                if (res != null) {
                    return res;
                }
            }
        return null;
    }

    private View resolveView(Resource resource, String template, ExtendedNodeType st, Set<JahiaTemplatesPackage> sortedPackages, ArrayList<String> searchedLocations) {
        for (JahiaTemplatesPackage aPackage : sortedPackages) {
            String currentTemplatePath = aPackage.getRootFolderPath();
            String templatePath = getTemplatePath(resource.getTemplateType(), template, st, currentTemplatePath, searchedLocations);
            if (templatePath != null) {
                View resolvedTemplate = new FileSystemView(templatePath, template, aPackage, template);
                return resolvedTemplate;
            }
        }
        return null;
    }

    private String getTemplatePath(String templateType, String template, ExtendedNodeType nt, String currentTemplatePath, ArrayList<String> searchedLocations) {
        String n = nt.getAlias();
        if (nt.getPrefix().length() > 0) {
            n = n.substring(nt.getPrefix().length() + 1);
        }

        for (String currentFileExtension : scriptExtensionsOrdering) {
            String templatePath = n + (template.equals("default") ? "" : "." + template) + "." + currentFileExtension;
            String modulePath = currentTemplatePath + "/" + JCRContentUtils.replaceColon(nt.getAlias()) + "/" + templateType + "/" + templatePath;
            try {
                searchedLocations.add(modulePath);
                if (resourcesCache.containsKey(modulePath)) {
                    if (resourcesCache.get(modulePath)) {
                        return modulePath;
                    }
                } else if (JahiaContextLoaderListener.getServletContext().getResource(modulePath) != null) {
                    resourcesCache.put(modulePath, Boolean.TRUE);
                    return modulePath;
                } else {
                    if (templateType.contains("-")) {
                        int lastDash = templateType.lastIndexOf("-");
                        String simplerTemplateType = templateType.substring(0, lastDash);
                        String simplerModulePath = currentTemplatePath + "/" + JCRContentUtils.replaceColon(nt.getAlias()) + "/" + simplerTemplateType + "/" + templatePath;
                        if (JahiaContextLoaderListener.getServletContext().getResource(simplerModulePath) != null) {
                            resourcesCache.put(simplerModulePath, Boolean.TRUE);
                            return simplerModulePath;
                        }
                    }
                    resourcesCache.put(modulePath, Boolean.FALSE);
                }
            } catch (MalformedURLException e) {
            }
        }
        return null;
    }

    public Script resolveScript(Resource resource) throws TemplateNotFoundException {
        try {
            ArrayList<String> searchLocations = new ArrayList<String>();
            View resolvedTemplate = resolveView(resource, searchLocations);
            if (resolvedTemplate == null) {
                throw new TemplateNotFoundException("Unable to find the template for resource " + resource + " by looking in " + searchLocations);
            }

            // @todo remove this hardcoding of script instantiation and make it Spring-configured.
            if (JSP_EXTENSION.equals(resolvedTemplate.getFileExtension()) || PHP_EXTENSION.equals(resolvedTemplate.getFileExtension())) {
                return new RequestDispatcherScript(resolvedTemplate);
            } else {
                return new JSR223Script(resolvedTemplate);
            }
        } catch (RepositoryException e) {
            throw new TemplateNotFoundException(e);
        }
    }

    public boolean hasView(ExtendedNodeType nt, String key, JCRSiteNode site) {
        SortedSet<View> t = null;
        String cacheKey = nt.getName() + (site != null ? site.getSiteKey() : "");
        if (viewSetCache.containsKey(cacheKey)) {
            t = viewSetCache.get(cacheKey);
        } else {
            t = getViewsSet(nt, site);
            viewSetCache.put(cacheKey, t);
        }
        for (View view : t) {
            if (view.getKey().equals(key)) {
                return true;
            }
        }
        return false;
    }

    public SortedSet<View> getViewsSet(ExtendedNodeType nt, JCRSiteNode site) {
        Map<String, View> views = new HashMap<String, View>();

        List<ExtendedNodeType> nodeTypeList = new ArrayList<ExtendedNodeType>(Arrays.asList(nt.getSupertypes()));
        nodeTypeList.add(nt);

        String templateType = "html";

        Collections.reverse(nodeTypeList);

        List<String> installedModules = null;
        if (site != null && site.getPath().startsWith("/sites/")) {
            installedModules = site.getInstalledModules();
            for (int i = 0; i < installedModules.size(); i++) {
                JahiaTemplatesPackage aPackage = templateManagerService.getTemplatePackageByFileName(installedModules.get(i));
                for (JahiaTemplatesPackage depend : aPackage.getDependencies()) {
                    if (!installedModules.contains(depend.getRootFolder())) {
                        installedModules.add(depend.getRootFolder());
                    }
                }
            }
        }

        for (ExtendedNodeType type : nodeTypeList) {
            Set<JahiaTemplatesPackage> packages = templateManagerService.getAvailableTemplatePackagesForModule(JCRContentUtils.replaceColon(type.getName()));
            for (JahiaTemplatesPackage aPackage : packages) {
                if (installedModules == null || installedModules.contains(aPackage.getRootFolder())) {
                    getViewsSet(type, views, templateType, aPackage.getRootFolder(), aPackage);
                }
            }
            getViewsSet(type, views, templateType, "default", null);
        }
        return new TreeSet<View>(views.values());
    }

    private void getViewsSet(ExtendedNodeType nt, Map<String, View> views, String templateType, String currentTemplatePath, JahiaTemplatesPackage tplPackage) {
        String path = currentTemplatePath + "/" + JCRContentUtils.replaceColon(nt.getAlias()) + "/" + templateType;

        File f = new File(SettingsBean.getInstance().getJahiaTemplatesDiskPath() + "/" + path);
        if (f.exists()) {
            File[] files = f.listFiles();
            for (File file : files) {
                if (!file.isDirectory()) {
                    String filename = file.getName();
                    String key = null;
                    try {
                        key = filename.substring(filename.indexOf(".") + 1, filename.lastIndexOf("."));
                    } catch (StringIndexOutOfBoundsException e) {
                        key = "default";
                    }
                    if (!views.containsKey(key)) {
                        views.put(key, new FileSystemView(SettingsBean.getInstance().getTemplatesContext() + path + "/" + file.getName(), key, tplPackage, filename));
                    }
                }
            }
        }
    }

    public void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof TemplatePackageRedeployedEvent || event instanceof ModuleDeployedOnSiteEvent) {
            resourcesCache.clear();
            viewSetCache.clear();
            FileSystemView.clearPropertiesCache();
        }
    }

    public void setTemplateManagerService(JahiaTemplateManagerService templateManagerService) {
        this.templateManagerService = templateManagerService;
    }
}
