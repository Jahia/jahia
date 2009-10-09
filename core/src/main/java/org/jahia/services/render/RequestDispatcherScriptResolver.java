package org.jahia.services.render;

import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.settings.SettingsBean;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Sep 28, 2009
 * Time: 7:24:17 PM
 * To change this template use File | Settings | File Templates.
 */
public class RequestDispatcherScriptResolver implements ScriptResolver {

    public Script resolveScript(Resource resource, RenderContext context) throws TemplateNotFoundException {
        try {
            return new RequestDispatcherScript(resource, context);
        } catch (IOException e) {
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
                                templates.put(key, new Template(path + "/" + file.getName(), key, tplPackage, filename));
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
                        templates.put(key, new Template(path + "/" + file.getName(), key, tplPackage, filename));
                    }
                }
            }
        }
    }


}
