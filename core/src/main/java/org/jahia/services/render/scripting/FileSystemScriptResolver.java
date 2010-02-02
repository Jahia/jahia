package org.jahia.services.render.scripting;

import org.jahia.bin.Jahia;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.services.render.*;
import org.jahia.settings.SettingsBean;

import javax.jcr.RepositoryException;
import java.io.File;
import java.net.MalformedURLException;
import java.util.*;

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
public class FileSystemScriptResolver implements ScriptResolver {

    private static final String JSP_EXTENSION = "jsp";
    private static final String PHP_EXTENSION = "php";
    private List<String> scriptExtensionsOrdering;

    public List<String> getScriptExtensionsOrdering() {
        return scriptExtensionsOrdering;
    }

    public void setScriptExtensionsOrdering(List<String> scriptExtensionsOrdering) {
        this.scriptExtensionsOrdering = scriptExtensionsOrdering;
    }

    protected Template resolveTemplate(Resource resource, final RenderContext context) throws RepositoryException {
        ExtendedNodeType nt = resource.getNode().getPrimaryNodeType();

        List<ExtendedNodeType> nodeTypeList = new ArrayList<ExtendedNodeType>(Arrays.asList(
                nt.getSupertypes()));
        nodeTypeList.add(nt);
        Collections.reverse(nodeTypeList);
        ExtendedNodeType base = NodeTypeRegistry.getInstance().getNodeType("nt:base");
        nodeTypeList.remove(base);
        nodeTypeList.add(base);
        if (resource.getWrappedMixinType() == null) {
            for (String template : resource.getTemplates()) {
                for (ExtendedNodeType st : nodeTypeList) {
                    SortedSet<JahiaTemplatesPackage> sortedPackages = ServicesRegistry.getInstance().getJahiaTemplateManagerService().getSortedAvailableTemplatePackagesForModule(
                            st.getAlias().replace(":", "_"), context);
                    for (JahiaTemplatesPackage aPackage : sortedPackages) {
                        if ("siteLayout".equals(aPackage.getModuleType()) && !aPackage.getName().equals(context.getSite().getTemplatePackageName())) {
                            continue;
                        }
                        String currentTemplatePath = aPackage.getRootFolderPath();
                        String templatePath = getTemplatePath(resource.getTemplateType(), template, st, currentTemplatePath);
                        if (templatePath != null) {
                            JahiaTemplatesPackage module = aPackage;
                            String templateName = template;
                            Template resolvedTemplate = new FileSystemTemplate(templatePath, templateName, module, templateName);
                            return resolvedTemplate;
                        }
                    }
                }
            }
        } else {
            for (String template : resource.getTemplates()) {
                SortedSet<JahiaTemplatesPackage> sortedPackages = ServicesRegistry.getInstance().getJahiaTemplateManagerService().getSortedAvailableTemplatePackagesForModule(
                        resource.getWrappedMixinType().getAlias().replace(":", "_"), context);
                for (JahiaTemplatesPackage aPackage : sortedPackages) {
                    if ("siteLayout".equals(aPackage.getModuleType()) && !aPackage.getName().equals(context.getSite().getTemplatePackageName())) {
                        continue;
                    }
                    String currentTemplatePath = aPackage.getRootFolderPath();
                    String templatePath = getTemplatePath(resource.getTemplateType(), template, resource.getWrappedMixinType(),
                                                   currentTemplatePath);
                    if (templatePath != null) {
                        JahiaTemplatesPackage module = aPackage;
                        Template resolvedTemplate = new FileSystemTemplate(templatePath, template, module, template);
                        return resolvedTemplate;
                    }
                }

            }
        }
        return null;
    }

    private String getTemplatePath(String templateType, String template, ExtendedNodeType nt, String currentTemplatePath) {
        String n = nt.getAlias();
        if (nt.getPrefix().length() > 0) {
            n = n.substring(nt.getPrefix().length() + 1);
        }

        for (String currentFileExtension : scriptExtensionsOrdering) {
            String templatePath = n + (template.equals("default") ? "" : "." + template) + "." + currentFileExtension;
            String modulePath = currentTemplatePath + "/" + nt.getAlias().replace(':', '_') + "/" + templateType + "/" + templatePath;
            try {
                if (Jahia.getStaticServletConfig().getServletContext().getResource(modulePath) != null) {
                    return modulePath;
                }
            } catch (MalformedURLException e) {
            }
        }
        return null;
    }

    public Script resolveScript(Resource resource, RenderContext context) throws TemplateNotFoundException {
        try {
            Template resolvedTemplate = resolveTemplate(resource, context);
            if (resolvedTemplate == null) {
                throw new TemplateNotFoundException("Unable to find the template for resource " + resource);
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

    public boolean hasTemplate(ExtendedNodeType nt, String key) {
        SortedSet<Template> t = getTemplatesSet(nt);
        for (Template template : t) {
            if (template.getKey().equals(key)) {
                return true;
            }
        }
        return false;
    }

    public SortedSet<Template> getAllTemplatesSet() {
        Map<String, Template> templates = new HashMap<String, Template>();

        String templateType = "html";

        List<JahiaTemplatesPackage> packages = ServicesRegistry.getInstance().getJahiaTemplateManagerService().getAvailableTemplatePackages();
        for (JahiaTemplatesPackage aPackage : packages) {
            getAllTemplatesSet(templates, templateType, aPackage.getRootFolder(), aPackage);
        }
        getAllTemplatesSet(templates, templateType, "default", null);

        return new TreeSet<Template>(templates.values());
    }

    private void getAllTemplatesSet(Map<String, Template> templates, String templateType, String currentTemplatePath, JahiaTemplatesPackage tplPackage) {
        String path = currentTemplatePath;
        File d = new File(SettingsBean.getInstance().getJahiaTemplatesDiskPath() + "/" + path);
        File[] dirs = d.listFiles();
        for (File f : dirs) {
            if (f.exists() && !f.isFile()) {
                f = new File(f, templateType);
                if (f.exists() && !f.isFile()) {
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
                            if (!templates.containsKey(key)) {
                                templates.put(key, new FileSystemTemplate(path + "/" + file.getName(), key, tplPackage, filename));
                            }
                        }
                    }
                }
            }
        }

    }


    public SortedSet<Template> getTemplatesSet(ExtendedNodeType nt) {
        Map<String, Template> templates = new HashMap<String, Template>();

        List<ExtendedNodeType> nodeTypeList = new ArrayList<ExtendedNodeType>(Arrays.asList(nt.getSupertypes()));
        nodeTypeList.add(nt);

        String templateType = "html";

        Collections.reverse(nodeTypeList);

        for (ExtendedNodeType type : nodeTypeList) {
            List<JahiaTemplatesPackage> packages = ServicesRegistry.getInstance().getJahiaTemplateManagerService().getAvailableTemplatePackagesForModule(type.getName().replace(":", "_"));
            for (JahiaTemplatesPackage aPackage : packages) {
                getTemplatesSet(type, templates, templateType, aPackage.getRootFolder(), aPackage);
            }
            getTemplatesSet(type, templates, templateType, "default", null);
        }
        return new TreeSet<Template>(templates.values());
    }

    private void getTemplatesSet(ExtendedNodeType nt, Map<String, Template> templates, String templateType, String currentTemplatePath, JahiaTemplatesPackage tplPackage) {
        String path = currentTemplatePath + "/" + nt.getAlias().replace(':', '_') + "/" + templateType;

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
                    if (!templates.containsKey(key)) {
                        templates.put(key, new FileSystemTemplate(SettingsBean.getInstance().getTemplatesContext() + path + "/" + file.getName(), key, tplPackage, filename));
                    }
                }
            }
        }
    }


}
