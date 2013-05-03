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

package org.jahia.services.render.scripting.bundle;

import org.apache.commons.lang.StringUtils;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.services.channels.Channel;
import org.jahia.services.channels.ChannelService;
import org.jahia.services.content.JCRContentUtils;
import org.jahia.services.content.decorator.JCRSiteNode;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.services.render.*;
import org.jahia.services.render.scripting.Script;
import org.jahia.services.render.scripting.ScriptFactory;
import org.jahia.services.render.scripting.ScriptResolver;
import org.jahia.services.templates.JahiaTemplateManagerService;
import org.jahia.services.templates.JahiaTemplateManagerService.ModuleDependenciesEvent;
import org.jahia.services.templates.JahiaTemplateManagerService.ModuleDeployedOnSiteEvent;
import org.jahia.services.templates.JahiaTemplateManagerService.TemplatePackageRedeployedEvent;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;

import javax.jcr.RepositoryException;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class BundleScriptResolver implements ScriptResolver, ApplicationListener<ApplicationEvent> {
    
    private static Logger logger = LoggerFactory.getLogger(BundleScriptResolver.class);
    
    private static Map<String, SortedSet<View>> viewSetCache = new ConcurrentHashMap<String, SortedSet<View>>(512);
    
    private Map<String, Set<ViewResourceInfo>> availableScripts = new HashMap<String, Set<ViewResourceInfo>>(64);
    private Map<String, ScriptFactory> scriptFactoryMap;
    private JahiaTemplateManagerService templateManagerService;
    private Comparator<ViewResourceInfo> scriptExtensionComparator;
    private List<String> scriptExtensionsOrdering;
    
    public void setScriptFactoryMap(Map<String, ScriptFactory> scriptFactoryMap) {
        this.scriptFactoryMap = scriptFactoryMap;
    }

    public List<String> getScriptExtensionsOrdering() {
        return scriptExtensionsOrdering;
    }

    public void setScriptExtensionsOrdering(final List<String> extensionsOrdering) {
        this.scriptExtensionsOrdering = extensionsOrdering;
        scriptExtensionComparator = new Comparator<ViewResourceInfo>() {
            public int compare(ViewResourceInfo o1, ViewResourceInfo o2) {
                int i = scriptExtensionsOrdering.indexOf(o1.extension) - scriptExtensionsOrdering.indexOf(o2.extension);
                return i != 0 ? i : 1;
            }
        };
    }

    /**
     * Callback for registering new resource views for a bundle.
     * @param bundle the bundle to register views for
     * @param scripts the URLs of the views to register
     */
    public void addBundleScripts(Bundle bundle, List<URL> scripts) {
        // TODO consider versions of modules/bundles
        for (URL script : scripts) {
            String path = script.getPath();
            if (path.split("/").length == 4) {
                ViewResourceInfo scriptResource = new ViewResourceInfo(path);
                Set<ViewResourceInfo> existingBundleScripts = availableScripts.get(bundle.getSymbolicName());
                if (existingBundleScripts == null) {
                    existingBundleScripts = new HashSet<ViewResourceInfo>();
                    availableScripts.put(bundle.getSymbolicName(), existingBundleScripts);
                    existingBundleScripts.add(scriptResource);
                } else if (!existingBundleScripts.contains(scriptResource)) {
                    existingBundleScripts.add(scriptResource);
                }
            }
        }
        clearCaches();
    }

    /**
     * Callback for unregistering resource views for a bundle.
     * @param bundle the bundle to unregister views for
     * @param scripts the URLs of the views to unregister
     */
    public void removeBundleScripts(Bundle bundle, List<URL> scripts) {
        Set<ViewResourceInfo> existingBundleScripts = availableScripts.get(bundle.getSymbolicName());
        if (existingBundleScripts == null) {
            return;
        }
        for (URL script : scripts) {
            existingBundleScripts.remove(new ViewResourceInfo(script.getPath()));
        }
        clearCaches();
    }

    @Override
    public Script resolveScript(Resource resource, RenderContext renderContext) throws TemplateNotFoundException {
        try {
            View resolvedView = resolveView(resource, renderContext);
            if (resolvedView == null) {
                throw new TemplateNotFoundException("Unable to find the view for resource " + resource);
            }

            if (scriptFactoryMap.containsKey(resolvedView.getFileExtension())) {
                return scriptFactoryMap.get(resolvedView.getFileExtension()).createScript(resolvedView);
            }
            throw new TemplateNotFoundException("Unable to script factory map extension handler for the resolved view "
                    + resolvedView.getInfo());
        } catch (RepositoryException e) {
            throw new TemplateNotFoundException(e);
        }
    }

    /*
     * @todo copied from FileSystemScriptResolver, we should refactor this into an abstract parent class
     */
    private View resolveView(Resource resource, RenderContext renderContext) throws RepositoryException {
        ExtendedNodeType nt = resource.getNode().getPrimaryNodeType();
        List<ExtendedNodeType> nodeTypeList = getNodeTypeList(nt);
        for (ExtendedNodeType type : resource.getNode().getMixinNodeTypes()) {
            nodeTypeList.addAll(0, type.getSupertypeSet());
            nodeTypeList.add(0, type);
        }

        if (resource.getResourceNodeType() != null) {
            nodeTypeList.addAll(0, getNodeTypeList(resource.getResourceNodeType()));
        }

        View res = resolveView(resource, nodeTypeList, renderContext);
        
        return res;
    }

    /*
     * @todo copied from FileSystemScriptResolver, we should refactor this into an abstract parent class
     */
    private View resolveView(Resource resource, List<ExtendedNodeType> nodeTypeList, RenderContext renderContext) {
        String template = resource.getResolvedTemplate();
        try {
            JCRSiteNode site = renderContext.getSite();

            Template t = (Template) renderContext.getRequest().getAttribute("previousTemplate");
            if (t != null && t.getNode() != null) {
                site = site.getSession().getNodeByIdentifier(t.getNode()).getResolveSite();
            }

            List<String> templateTypeMappings = null;
            if (renderContext != null) {
                Channel channel = renderContext.getChannel();
                if (!channel.getFallBack().equals("root")) {
                    templateTypeMappings = new LinkedList<String>();
                    while (!channel.getFallBack().equals("root")) {
                        if (channel.getCapability("template-type-mapping") != null) {
                            templateTypeMappings.add(resource.getTemplateType() + "-" + channel.getCapability("template-type-mapping"));
                        }
                        channel = ChannelService.getInstance().getChannel(channel.getFallBack());
                    }
                    templateTypeMappings.add(resource.getTemplateType());
                }
            }
            Set<View> s = getViewsSet(nodeTypeList, site,
                    templateTypeMappings != null ? templateTypeMappings : Arrays.asList(resource.getTemplateType()));
            for (View view : s) {
                if (view.getKey().equals(template)) {
                    return view;
                }
            }
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    @Override
    /*
     * @todo copied from FileSystemScriptResolver, we should refactor this into an abstract parent class
     */
    public boolean hasView(ExtendedNodeType nt, String key, JCRSiteNode site, String templateType) {
        for (View view : getViewsSet(nt, site, templateType)) {
            if (view.getKey().equals(key)) {
                return true;
            }
        }
        return false;
    }

    @Override
    /*
     * @todo copied from FileSystemScriptResolver, we should refactor this into an abstract parent class
     */
    public SortedSet<View> getViewsSet(ExtendedNodeType nt, JCRSiteNode site, String templateType) {
        SortedSet<View> t = null;
        String cacheKey = nt.getName() + "_" + (site != null ? site.getSiteKey() : "") + "_" + templateType;
        if (viewSetCache.containsKey(cacheKey)) {
            t = viewSetCache.get(cacheKey);
        } else {
            try {
                t = getViewsSet(getNodeTypeList(nt), site, Arrays.asList(templateType));
                viewSetCache.put(cacheKey, t);
            } catch (NoSuchNodeTypeException e) {
                logger.error(e.getMessage(), e);
            }
        }

        return t;
    }

    /**
     * @param nt
     * @return
     * @throws NoSuchNodeTypeException
     * @todo copied from FileSystemScriptResolver, we should refactor this into an abstract parent class
     */
    private List<ExtendedNodeType> getNodeTypeList(ExtendedNodeType nt) throws NoSuchNodeTypeException {
        List<ExtendedNodeType> nodeTypeList = new LinkedList<ExtendedNodeType>();
        nodeTypeList.add(nt);
        nodeTypeList.addAll(nt.getSupertypeSet());
        ExtendedNodeType base = NodeTypeRegistry.getInstance().getNodeType("nt:base");
        if (nodeTypeList.remove(base)) {
            nodeTypeList.add(base);
        }
        return nodeTypeList;
    }

    /*
     * @todo copied from FileSystemScriptResolver, we should refactor this into an abstract parent class
     */
    private SortedSet<View> getViewsSet(List<ExtendedNodeType> nodeTypeList, JCRSiteNode site,
            List<String> templateTypes) {
        Map<String, View> views = new HashMap<String, View>();

        Set<String> installedModules = getInstalledModules(site);

        for (ExtendedNodeType type : nodeTypeList) {
            boolean defaultModuleProcessed = false;
            Set<JahiaTemplatesPackage> packages = templateManagerService
                    .getModulesWithViewsForComponent(JCRContentUtils.replaceColon(type.getName()));
            for (JahiaTemplatesPackage aPackage : packages) {
                String packageName = aPackage.getRootFolder();
                if (installedModules == null || installedModules.contains(packageName)) {
                    if (aPackage.isDefault()) {
                        defaultModuleProcessed = true;
                    }
                    for (String templateType : templateTypes) {
                        getViewsSet(type, views, templateType, packageName, aPackage);
                    }
                }
            }
            if (!defaultModuleProcessed) {
                JahiaTemplatesPackage defaultModule = templateManagerService.getTemplatePackageByFileName("default");
                if (defaultModule != null) {
                    for (String templateType : templateTypes) {
                        getViewsSet(type, views, templateType, "default", defaultModule);
                    }
                }
            }
        }
        return new TreeSet<View>(views.values());
    }

    private Set<String> getInstalledModules(JCRSiteNode site) {
        if (site == null) {
            return null;
        }
        Set<String> installedModules = null;
        String sitePath = site.getPath();
        if (sitePath.startsWith("/sites/")) {
            installedModules = new LinkedHashSet<String>(site.getInstalledModules());
            List<String> keys = new ArrayList<String>(installedModules);
            for (int i = 0; i < keys.size(); i++) {
                String key = keys.get(i);
                JahiaTemplatesPackage aPackage = templateManagerService.getTemplatePackageByFileName(key);
                if (aPackage != null) {
                    for (JahiaTemplatesPackage depend : aPackage.getDependencies()) {
                        if (!installedModules.contains(depend.getRootFolder())) {
                            installedModules.add(depend.getRootFolder());
                            keys.add(depend.getRootFolder());
                        }
                    }
                } else {
                    logger.warn("Couldn't find module '" + key + "' installed in site '" + sitePath + "'");
                }
            }
        } else if (sitePath.startsWith("/modules/")) {
            JahiaTemplatesPackage aPackage = templateManagerService.getTemplatePackageByFileName(site.getName());
            if (aPackage != null) {
                installedModules = new LinkedHashSet<String>();
                installedModules.add(aPackage.getRootFolder());
                for (JahiaTemplatesPackage depend : aPackage.getDependencies()) {
                    if (!installedModules.contains(depend.getRootFolder())) {
                        installedModules.add(depend.getRootFolder());
                    }
                }
            }
        }

        return installedModules;
    }

    /*
     * @todo copied from FileSystemScriptResolver, we should refactor this into an abstract parent class
     */
    private void getViewsSet(ExtendedNodeType nt, Map<String, View> views, String templateType, String module,
            JahiaTemplatesPackage tplPackage) {
        StringBuilder pathBuilder = new StringBuilder(64);
        pathBuilder.append("/").append(JCRContentUtils.replaceColon(nt.getAlias())).append("/").append(templateType)
                .append("/");
        String path = pathBuilder.toString();

        // append node type name (without namespace prefix) + "."
        pathBuilder.append(nt.getName().contains(":") ? StringUtils.substringAfter(nt.getName(), ":") : nt.getName())
                .append(".");

        // find scripts in the module bundle, matching that path prefix
        Set<ViewResourceInfo> sortedScripts = findBundleScripts(module, pathBuilder.toString());
        for (ViewResourceInfo res : sortedScripts) {
            if (!views.containsKey(res.viewKey)) {
                BundleView view = new BundleView(path + res.filename, res.viewKey, tplPackage, res.filename);
                views.put(res.viewKey, view);
                scriptFactoryMap.get(res.extension).initView(view);
            }
        }
    }

    /**
     * Returns view scripts for the specified module bundle which match the specified path.
     * 
     * @param module
     *            the module bundle to perform lookup in
     * @param pathPrefix
     *            the resource path prefix to match
     * @return a set of matching view scripts ordered by the extension (script type)
     */
    private Set<ViewResourceInfo> findBundleScripts(String module, String pathPrefix) {
        Set<ViewResourceInfo> allBundleScripts = availableScripts.get(module);
        if (allBundleScripts == null || allBundleScripts.isEmpty()) {
            return Collections.emptySet();
        }
        if (allBundleScripts.size() == 1) {
            return allBundleScripts;
        }
        SortedSet<ViewResourceInfo> sortedScripts = new TreeSet<ViewResourceInfo>(scriptExtensionComparator);
        for (ViewResourceInfo res : allBundleScripts) {
            if (!res.path.startsWith(pathPrefix)) {
                continue;
            }
            sortedScripts.add(res);
        }
        return sortedScripts;
    }

    public void setTemplateManagerService(JahiaTemplateManagerService templateManagerService) {
        this.templateManagerService = templateManagerService;
    }

    public void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof TemplatePackageRedeployedEvent || event instanceof ModuleDeployedOnSiteEvent
                || event instanceof ModuleDependenciesEvent) {
            clearCaches();
        }
    }

    public static void clearCaches() {
        viewSetCache.clear();
        BaseView.clearPropertiesCache();
    }
}
