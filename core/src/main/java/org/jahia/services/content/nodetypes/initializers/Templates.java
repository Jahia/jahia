/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.services.content.nodetypes.initializers;

import org.jahia.params.ProcessingContext;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.content.nodetypes.ValueImpl;
import org.jahia.settings.SettingsBean;

import javax.jcr.Value;
import javax.jcr.PropertyType;
import java.util.*;
import java.io.File;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Oct 10, 2008
 * Time: 11:09:54 AM
 * To change this template use File | Settings | File Templates.
 */
public class Templates implements ValueInitializer {

    public Value[] getValues(ProcessingContext jParams, ExtendedPropertyDefinition declaringPropertyDefinition, List<String> params, Map context) {
        ExtendedNodeType nt = (ExtendedNodeType) context.get("currentDefinition");
        if (nt == null) {
            return new Value[0];
        }
        SortedSet<Template> templates = getTemplatesSet(nt);

        List<Value> vs = new ArrayList<Value>();
        for (Template template : templates) {
            vs.add(new ValueImpl(template.getKey(), PropertyType.STRING, false));
        }
        return vs.toArray(new Value[vs.size()]);
    }


    // todo: move these methods elsewhere
    public static SortedSet<Template> getTemplatesSet(ExtendedNodeType nt) {
        Map<String,Template> templates = new HashMap<String,Template>();

        List<ExtendedNodeType> nodeTypeList = new ArrayList<ExtendedNodeType>(Arrays.asList(nt.getSupertypes()));
        nodeTypeList.add(nt);

        String templateType = "html";

        Collections.reverse(nodeTypeList);

        for (ExtendedNodeType type : nodeTypeList) {
            List<JahiaTemplatesPackage> packages = ServicesRegistry.getInstance().getJahiaTemplateManagerService().getAvailableTemplatePackagesForModule(type.getName().replace(":","_"));
            for (JahiaTemplatesPackage aPackage : packages) {
                getTemplatesSet(type, templates, templateType, aPackage.getRootFolder(), aPackage);
            }
            getTemplatesSet(type, templates, templateType, "default", null);
        }
        return new TreeSet<Template>(templates.values());
    }

    private static void getTemplatesSet(ExtendedNodeType nt, Map<String,Template> templates, String templateType, String currentTemplatePath, JahiaTemplatesPackage tplPackage) {
        String path = currentTemplatePath + "/" + nt.getAlias().replace(':','_') + "/" + templateType;

        File f = new File(SettingsBean.getInstance().getJahiaTemplatesDiskPath()+ "/"+ path);
        if (f.exists()) {
            File[] files = f.listFiles();
            for (File file : files) {
                if (!file.isDirectory()) {
                    String filename = file.getName();
                    String key = filename.substring(0, filename.lastIndexOf("."));
                    if (!templates.containsKey(key)) {
                        templates.put(key, new Template(path+"/"+file.getName(), key, tplPackage, filename));
                    }
                }
            }
        }
    }

    public static class Template implements Comparable<Template> {
        private String path;
        private String key;
        private JahiaTemplatesPackage ownerPackage;
        private String displayName;

        private Template(String path, String key, JahiaTemplatesPackage ownerPackage, String displayName) {
            this.path = path;
            this.key = key;
            this.ownerPackage = ownerPackage;
            this.displayName = displayName;
        }

        public String getPath() {
            return path;
        }

        public String getKey() {
            return key;
        }

        public JahiaTemplatesPackage getOwnerPackage() {
            return ownerPackage;
        }

        public String getDisplayName() {
            return displayName;
        }

        public int compareTo(Template template) {
            if (ownerPackage == null) {
                if (template.ownerPackage != null ) {
                    return 1;
                } else {
                    return key.compareTo(template.key);
                }
            } else {
                if (template.ownerPackage == null ) {
                    return -1;
                } else if (!ownerPackage.equals(template.ownerPackage)) {
                    return ownerPackage.getName().compareTo(template.ownerPackage.getName());
                } else {
                    return key.compareTo(template.key);
                }
            }
        }
    }

}