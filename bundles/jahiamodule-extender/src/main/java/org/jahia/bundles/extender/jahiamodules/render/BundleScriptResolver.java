package org.jahia.bundles.extender.jahiamodules.render;

import org.apache.commons.io.FilenameUtils;
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
import org.jahia.services.templates.ModuleVersion;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import java.net.URL;
import java.util.*;

public class BundleScriptResolver implements ScriptResolver {

    private Map<Bundle, Set<URL>> availableScripts = new HashMap<Bundle, Set<URL>>();
    private Map<String, ScriptFactory> scriptFactoryMap;
    private JahiaTemplateManagerService templateManagerService;
    private static Logger logger = LoggerFactory.getLogger(BundleScriptResolver.class);
    Comparator<URL> scriptExtensionComparator;
    private List<String> scriptExtensionsOrdering;


    public Map<String, ScriptFactory> getScriptFactoryMap() {
        return scriptFactoryMap;
    }

    public void setScriptFactoryMap(Map<String, ScriptFactory> scriptFactoryMap) {
        this.scriptFactoryMap = scriptFactoryMap;
    }

    public JahiaTemplateManagerService getTemplateManagerService() {
        return templateManagerService;
    }

    public void setTemplateManagerService(JahiaTemplateManagerService templateManagerService) {
        this.templateManagerService = templateManagerService;
    }

    public List<String> getScriptExtensionsOrdering() {
        return scriptExtensionsOrdering;
    }

    public void setScriptExtensionsOrdering(final List<String> scriptExtensionsOrdering) {
        this.scriptExtensionsOrdering = scriptExtensionsOrdering;
        scriptExtensionComparator = new Comparator<URL>() {
            public int compare(URL o1, URL o2) {
                int i = scriptExtensionsOrdering.indexOf(StringUtils.substringAfterLast(o1.getFile(), ".")) - scriptExtensionsOrdering.indexOf(StringUtils.substringAfterLast(o2.getFile(), "."));
                return i != 0 ? i : 1;
            }
        };
    }

    public Map<Bundle, Set<URL>> getAvailableScripts() {
        return availableScripts;
    }

    public void addBundleScript(Bundle bundle, URL script) {
        Set<URL> existingBundleScripts = availableScripts.get(bundle);
        if (existingBundleScripts == null) {
            existingBundleScripts = new HashSet<URL>();
        }
        if (!existingBundleScripts.contains(script)) {
            existingBundleScripts.add(script);
        }
        availableScripts.put(bundle, existingBundleScripts);
    }

    public void removeBundleScript(Bundle bundle, URL script) {
        Set<URL> existingBundleScripts = availableScripts.get(bundle);
        if (existingBundleScripts == null) {
            return;
        }
        existingBundleScripts.remove(script);
    }

    public Bundle findScript(String pathScript) {
        for (Map.Entry<Bundle, Set<URL>> availableBundleScripts : availableScripts.entrySet()) {
            if (availableBundleScripts.getValue().contains(pathScript)) {
                return availableBundleScripts.getKey();
            }
        }
        return null;
    }

    /**
     * Returns a map of bundles that contains scripts that start with the path specified as a parameter.
     *
     * @param partialScriptPath
     * @return
     */
    public Map<Bundle, Set<URL>> findBundleScripts(String partialScriptPath) {
        Map<Bundle, Set<URL>> result = new HashMap<Bundle, Set<URL>>();
        for (Map.Entry<Bundle, Set<URL>> availableBundleScripts : availableScripts.entrySet()) {
            for (URL scriptPath : availableBundleScripts.getValue()) {
                if (scriptPath.getPath().contains(partialScriptPath)) {
                    Set<URL> bundleScripts = result.get(availableBundleScripts.getKey());
                    if (bundleScripts == null) {
                        bundleScripts = new HashSet<URL>();
                    }
                    if (!bundleScripts.contains(availableBundleScripts.getValue())) {
                        bundleScripts.add(scriptPath);
                    }
                    result.put(availableBundleScripts.getKey(), bundleScripts);
                }
            }
        }
        return result;
    }

    @Override
    @Deprecated
    public Script resolveScript(Resource resource) throws TemplateNotFoundException {
        return resolveScript(resource, null);
    }

    @Override
    @Deprecated
    public boolean hasView(ExtendedNodeType nt, String viewName, JCRSiteNode site) {
        return hasView(nt, viewName, site, "html");
    }

    @Override
    @Deprecated
    public SortedSet<View> getViewsSet(ExtendedNodeType nt, JCRSiteNode site) {
        return getViewsSet(nt, site, "html");
    }

    @Override
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

    /*
     * @todo copied from FileSystemScriptResolver, we should refactor this into an abstract parent class
     */
    protected View resolveView(Resource resource, RenderContext renderContext) throws RepositoryException {
        ExtendedNodeType nt = resource.getNode().getPrimaryNodeType();
        List<ExtendedNodeType> nodeTypeList = getNodeTypeList(nt);
        for (ExtendedNodeType type : resource.getNode().getMixinNodeTypes()) {
            nodeTypeList.addAll(0, Arrays.asList(type.getSupertypes()));
            nodeTypeList.add(0, type);
        }

        if (resource.getResourceNodeType() != null) {
            ExtendedNodeType rnt = resource.getResourceNodeType();
            nodeTypeList.addAll(0, getNodeTypeList(rnt));
        }


        View res = resolveView(resource, nodeTypeList, renderContext);
        if (res != null) {
            return res;
        }

        return null;
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

    @Override
    /*
     * @todo copied from FileSystemScriptResolver, we should refactor this into an abstract parent class
     */
    public boolean hasView(ExtendedNodeType nt, String key, JCRSiteNode site, String templateType) {
        SortedSet<View> t;
        String cacheKey = nt.getName() + (site != null ? site.getSiteKey() : "");
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

    @Override
    /*
     * @todo copied from FileSystemScriptResolver, we should refactor this into an abstract parent class
     */
    public SortedSet<View> getViewsSet(ExtendedNodeType nt, JCRSiteNode site, String templateType) {
        try {
            return getViewsSet(getNodeTypeList(nt), site, Arrays.asList(templateType));
        } catch (NoSuchNodeTypeException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * @param nt
     * @return
     * @throws NoSuchNodeTypeException
     * @todo copied from FileSystemScriptResolver, we should refactor this into an abstract parent class
     */
    private List<ExtendedNodeType> getNodeTypeList(ExtendedNodeType nt) throws NoSuchNodeTypeException {
        List<ExtendedNodeType> extendedNodeTypes = Arrays.asList(nt.getSupertypes());
        List<ExtendedNodeType> nodeTypeList = new ArrayList<ExtendedNodeType>();
        nodeTypeList.add(nt);
        nodeTypeList.addAll(extendedNodeTypes);
        ExtendedNodeType base = NodeTypeRegistry.getInstance().getNodeType("nt:base");
        if (nodeTypeList.remove(base)) {
            nodeTypeList.add(base);
        }
        return nodeTypeList;
    }

    /*
     * @todo copied from FileSystemScriptResolver, we should refactor this into an abstract parent class
     */
    private SortedSet<View> getViewsSet(List<ExtendedNodeType> nodeTypeList, JCRSiteNode site, List<String> templateTypes) {
        Map<String, View> views = new HashMap<String, View>();

        Map<String, String> installedModules = null;
        if (site != null && site.getPath().startsWith("/sites/")) {
            installedModules = site.getInstalledModulesWithVersions();
            List<String> keys = new ArrayList<String>(installedModules.keySet());
            for (int i = 0; i < keys.size(); i++) {
                String key = keys.get(i);
                JahiaTemplatesPackage aPackage = templateManagerService.getTemplatePackageByFileName(key);
                if (aPackage != null) {
                    for (JahiaTemplatesPackage depend : aPackage.getDependencies()) {
                        if (!installedModules.containsKey(depend.getRootFolder())) {
                            installedModules.put(depend.getRootFolder(), null);
                            keys.add(depend.getRootFolder());
                        }
                    }
                } else {
                    logger.error("Couldn't find module package for module '" + installedModules.get(i) + "' installed in site '" + site.getPath() + "'");
                }
            }
        } else if (site != null && site.getPath().startsWith("/modules/")) {
            JahiaTemplatesPackage aPackage = templateManagerService.getTemplatePackageByFileName(site.getName());
            if (aPackage != null) {
                installedModules = new HashMap<String, String>();
                installedModules.put(aPackage.getRootFolder(), null);
                for (JahiaTemplatesPackage depend : aPackage.getDependencies()) {
                    if (!installedModules.containsKey(depend.getRootFolder())) {
                        installedModules.put(depend.getRootFolder(), null);
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
                } else if (installedModules.containsKey(packageName)) {
                    for (String templateType : templateTypes) {
                        getViewsSet(type, views, templateType, packageName, aPackage, aPackage.getVersion());
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

    /*
     * @todo copied from FileSystemScriptResolver, we should refactor this into an abstract parent class
     */
    private void getViewsSet(ExtendedNodeType nt, Map<String, View> views, String templateType, String currentTemplatePath, JahiaTemplatesPackage tplPackage, ModuleVersion version) {
        String path = JCRContentUtils.replaceColon(nt.getAlias()) + "/" + templateType;
        Map<Bundle, Set<URL>> bundleScripts = findBundleScripts("/" + path + "/");
        for (Bundle bundle : bundleScripts.keySet()) {
            if (bundle.getSymbolicName().equals(currentTemplatePath)) {
                Set<URL> scriptsInBundle = bundleScripts.get(bundle);
                SortedSet<URL> sortedScriptsInBundle = new TreeSet<URL>(scriptExtensionComparator);
                for (URL scriptInBundle : scriptsInBundle) {
                    if (scriptExtensionsOrdering.contains(StringUtils.substringAfterLast(scriptInBundle.getFile(), "."))) {
                        sortedScriptsInBundle.add(scriptInBundle);
                    }
                }
                for (URL scriptInBundle : sortedScriptsInBundle) {
                    String ext = StringUtils.substringAfterLast(scriptInBundle.getFile(), ".");
                    if (scriptFactoryMap.containsKey(ext)) {
                        String filename = FilenameUtils.getName(scriptInBundle.getFile());
                        String pref = nt.getName().contains(":") ? StringUtils.substringAfter(nt.getName(), ":") : nt.getName();
                        if (filename.startsWith(pref + ".")) {
                            String key = null;
                            try {
                                key = filename.substring(filename.indexOf(".") + 1, filename.lastIndexOf("."));
                            } catch (StringIndexOutOfBoundsException e) {
                                key = "default";
                            }
                            if (!views.containsKey(key)) {
                                BundleView view = new BundleView(bundle, "/" + path + "/" + FilenameUtils.getName(scriptInBundle.getFile()), key, tplPackage, version, filename);
                                views.put(key, view);
                                scriptFactoryMap.get(ext).initView(view);
                            }
                        }
                    }
                }
            }
        }
    }

}
