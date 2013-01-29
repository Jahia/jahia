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

import org.apache.commons.lang.StringUtils;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.services.channels.Channel;
import org.jahia.services.channels.ChannelService;
import org.jahia.services.content.JCRContentUtils;
import org.jahia.services.content.decorator.JCRSiteNode;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.services.render.*;
import org.jahia.services.templates.JahiaTemplateManagerService;
import org.jahia.services.templates.JahiaTemplateManagerService.ModuleDependenciesEvent;
import org.jahia.services.templates.JahiaTemplateManagerService.ModuleDeployedOnSiteEvent;
import org.jahia.services.templates.JahiaTemplateManagerService.TemplatePackageRedeployedEvent;
import org.jahia.services.templates.ModuleVersion;
import org.jahia.settings.SettingsBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;

import javax.jcr.RepositoryException;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import java.io.File;
import java.io.FilenameFilter;
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

    private static Logger logger = LoggerFactory.getLogger(FileSystemScriptResolver.class);

    Comparator<File> scriptExtensionComparator;
    
    private List<String> scriptExtensionsOrdering;

    private Map<String,ScriptFactory> scriptFactoryMap;

    private static Map<String, SortedSet<View>> viewSetCache = new ConcurrentHashMap<String, SortedSet<View>>();
    
    private JahiaTemplateManagerService templateManagerService;

    public List<String> getScriptExtensionsOrdering() {
        return scriptExtensionsOrdering;
    }

    public void setScriptExtensionsOrdering(final List<String> scriptExtensionsOrdering) {
        this.scriptExtensionsOrdering = scriptExtensionsOrdering;
        scriptExtensionComparator = new Comparator<File>() {
            public int compare(File o1, File o2) {
                int i = scriptExtensionsOrdering.indexOf(StringUtils.substringAfterLast(o1.getName(), ".")) - scriptExtensionsOrdering.indexOf(StringUtils.substringAfterLast(o2.getName(), "."));
                return i != 0 ? i : 1;
            }
        };
    }

    public Map<String, ScriptFactory> getScriptFactoryMap() {
        return scriptFactoryMap;
    }

    public void setScriptFactoryMap(Map<String, ScriptFactory> scriptFactoryMap) {
        this.scriptFactoryMap = scriptFactoryMap;
    }

    protected View resolveView(Resource resource, RenderContext renderContext) throws RepositoryException {
        ExtendedNodeType nt = resource.getNode().getPrimaryNodeType();
        List<ExtendedNodeType> nodeTypeList = getNodeTypeList(nt);
        for (ExtendedNodeType type : resource.getNode().getMixinNodeTypes()) {
            nodeTypeList.addAll(0,Arrays.asList(type.getSupertypes()));
            nodeTypeList.add(0,type);
        }

        if (resource.getResourceNodeType() != null) {
            ExtendedNodeType rnt = resource.getResourceNodeType();
            nodeTypeList.addAll(0,getNodeTypeList(rnt));
        }


        View res = resolveView(resource, nodeTypeList, renderContext);
        if (res != null) {
            return res;
        }

        return null;
    }

    private List<ExtendedNodeType> getNodeTypeList(ExtendedNodeType nt) throws NoSuchNodeTypeException {
        List<ExtendedNodeType> extendedNodeTypes = Arrays.asList(nt.getSupertypes());
        List<ExtendedNodeType> nodeTypeList = new ArrayList<ExtendedNodeType>();
        nodeTypeList.add(nt);
        nodeTypeList.addAll(extendedNodeTypes);
        ExtendedNodeType base = NodeTypeRegistry.getInstance().getNodeType("nt:base");
        if (nodeTypeList.remove(base)){
            nodeTypeList.add(base);
        }
        return nodeTypeList;
    }

    private View resolveView(Resource resource, List<ExtendedNodeType> nodeTypeList, RenderContext renderContext) {
        String template = resource.getResolvedTemplate();
        try {
            JCRSiteNode site = renderContext.getSite();

            Template t = (Template) renderContext.getRequest().getAttribute("previousTemplate");
            if (t != null && t.getNode() != null) {
                site = site.getSession().getNodeByIdentifier(t.getNode()).getResolveSite();
            }

            List<String> templateTypeMappings = new ArrayList<String>();
            SortedSet<View> s = new TreeSet<View>();
            if (renderContext != null) {
                Channel channel = renderContext.getChannel();
                while (!channel.getFallBack().equals("root")) {
                    if (channel.getCapability("template-type-mapping") != null) {
                        templateTypeMappings.add(resource.getTemplateType() + "-" + channel.getCapability("template-type-mapping"));
                    }
                    channel = ChannelService.getInstance().getChannel(channel.getFallBack());
                }
            }
            templateTypeMappings.add(resource.getTemplateType());
            s = getViewsSet(nodeTypeList, site, templateTypeMappings);
            for (View view : s) {
                if (view.getKey().equals(template)) {
                    return view;
                }
            }
        } catch (RepositoryException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Script resolveScript(Resource resource) throws TemplateNotFoundException {
        return resolveScript(resource, null);
    }

    public Script resolveScript(Resource resource, RenderContext renderContext) throws TemplateNotFoundException {
        try {
            ArrayList<String> searchLocations = new ArrayList<String>();
            View resolvedView = resolveView(resource, renderContext);
            if (resolvedView == null) {
                throw new TemplateNotFoundException("Unable to find the template for resource " + resource + " by looking in " + searchLocations);
            }

            if (scriptFactoryMap.containsKey(resolvedView.getFileExtension())) {
                return scriptFactoryMap.get(resolvedView.getFileExtension()).createScript(resolvedView);
            }
            throw new TemplateNotFoundException("Unable to script factory map extension handler for the resolved view " + resolvedView.getInfo());
        } catch (RepositoryException e) {
            throw new TemplateNotFoundException(e);
        }
    }

    public boolean hasView(ExtendedNodeType nt, String key, JCRSiteNode site) {
        return hasView(nt,key,site,"html");
    }

    public boolean hasView(ExtendedNodeType nt, String key, JCRSiteNode site, String templateType) {
        SortedSet<View> t;
        //String cacheKey = nt.getName() + (site != null ? site.getSiteKey() : "");
        //viewSetCache.clear();
//         if (viewSetCache.containsKey(cacheKey)) {
//            t = viewSetCache.get(cacheKey);
//        } else {
            t = getViewsSet(nt, site, templateType);
//            viewSetCache.put(cacheKey, t);
//        }
        for (View view : t) {
            if (view.getKey().equals(key)) {
                return true;
            }
        }
        return false;
    }

    public SortedSet<View> getViewsSet(ExtendedNodeType nt, JCRSiteNode site) {
        return getViewsSet(nt,site,"html");
    }

    public SortedSet<View> getViewsSet(ExtendedNodeType nt, JCRSiteNode site, String templateType) {
        try {
            return getViewsSet(getNodeTypeList(nt), site, Arrays.asList(templateType));
        } catch (NoSuchNodeTypeException e) {
            e.printStackTrace();
        }
        return null;
    }

    private SortedSet<View> getViewsSet(List<ExtendedNodeType> nodeTypeList, JCRSiteNode site,  List<String> templateTypes) {
        Map<String, View> views = new HashMap<String, View>();

        List<String> installedModules = null;
        if (site != null && site.getPath().startsWith("/sites/")) {
            installedModules = site.getInstalledModules();
            for (int i = 0; i < installedModules.size(); i++) {
                String installedModule = installedModules.get(i);
                JahiaTemplatesPackage aPackage = templateManagerService.getTemplatePackageByFileName(installedModule);
                if (aPackage != null) {
                    for (JahiaTemplatesPackage depend : aPackage.getDependencies()) {
                        if (!installedModules.contains(depend.getRootFolder())) {
                            installedModules.add(depend.getRootFolder());
                        }
                    }
                } else {
                    logger.error("Couldn't find module directory for module '" + installedModule + "' installed in site '"+site.getPath()+"'");
                }
            }
        } else if (site != null && site.getPath().startsWith("/modules/")) {
            JahiaTemplatesPackage aPackage = templateManagerService.getTemplatePackageByFileName(site.getName());
            if (aPackage != null) {
                installedModules = new ArrayList<String>();
                installedModules.add(aPackage.getRootFolder());
                for (JahiaTemplatesPackage depend : aPackage.getDependencies()) {
                    if (!installedModules.contains(depend.getRootFolder())) {
                        installedModules.add(depend.getRootFolder());
                    }
                }
            }
        }


        for (ExtendedNodeType type : nodeTypeList) {
            Set<JahiaTemplatesPackage> packages = templateManagerService.getModulesWithViewsForComponent(JCRContentUtils.replaceColon(type.getName()));
            for (JahiaTemplatesPackage aPackage : packages) {
                String packageName = aPackage.getRootFolder();
                if (installedModules == null) {
                    for (String templateType : templateTypes) {
                        getViewsSet(type, views, templateType, packageName, aPackage, aPackage.getVersion());
                    }
                } else if (installedModules.contains(packageName)) {
                    for (String templateType : templateTypes) {
                        getViewsSet(type, views, templateType, packageName ,aPackage, aPackage.getVersion());
                    }
                }
            }
            for (String templateType : templateTypes) {
                JahiaTemplatesPackage defaultModule = templateManagerService.getTemplatePackageByFileName("default");
                if (defaultModule != null) {
                    getViewsSet(type, views, templateType, "default", defaultModule, defaultModule.getVersion());
                }
            }
        }
        return new TreeSet<View>(views.values());
    }

    private void getViewsSet(ExtendedNodeType nt, Map<String, View> views, String templateType, String currentTemplatePath, JahiaTemplatesPackage tplPackage, ModuleVersion version) {
        /*String path = currentTemplatePath + "/" + version.toString() + "/" + JCRContentUtils.replaceColon(nt.getAlias()) + "/" + templateType;
        File f = new File(SettingsBean.getInstance().getJahiaTemplatesDiskPath() + "/" + path);
        if (f.exists()) {
            SortedSet<File> files = new TreeSet<File>(scriptExtensionComparator);
            files.addAll(Arrays.asList(f.listFiles(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return scriptExtensionsOrdering.contains(StringUtils.substringAfterLast(name, "."));
                }
            })));
            for (File file : files) {
                String ext = StringUtils.substringAfterLast(file.getName(), ".");
                if (scriptFactoryMap.containsKey(ext)) {
                    String filename = file.getName();
                    String pref = nt.getName().contains(":") ? StringUtils.substringAfter(nt.getName(),":") : nt.getName();
                    if (filename.startsWith(pref+".")) {
                        String key = null;
                        try {
                            key = filename.substring(filename.indexOf(".") + 1, filename.lastIndexOf("."));
                        } catch (StringIndexOutOfBoundsException e) {
                            key = "default";
                        }
                        if (!views.containsKey(key)) {
                            FileSystemView view = new FileSystemView(SettingsBean.getInstance().getTemplatesContext() + path + "/" + file.getName(), key, tplPackage, version, filename);
                            views.put(key, view);
                            scriptFactoryMap.get(ext).initView(view);
                        }
                    }
                }
            }
        }*/
    }

    public void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof TemplatePackageRedeployedEvent || event instanceof ModuleDeployedOnSiteEvent || event instanceof ModuleDependenciesEvent) {
            viewSetCache.clear();
            FileSystemView.clearPropertiesCache();
        }
    }

    public void setTemplateManagerService(JahiaTemplateManagerService templateManagerService) {
        this.templateManagerService = templateManagerService;
    }
}
