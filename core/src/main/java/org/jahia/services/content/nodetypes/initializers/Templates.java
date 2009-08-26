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
import org.jahia.data.beans.TemplatePathResolverFactory;
import org.jahia.data.beans.TemplatePathResolverBean;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.content.nodetypes.ValueImpl;
import org.jahia.hibernate.manager.SpringContextSingleton;
import org.jahia.bin.Jahia;

import javax.jcr.Value;
import javax.jcr.PropertyType;
import java.util.*;
import java.io.File;
import java.net.MalformedURLException;

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
        String tplPkgName = jParams.getSite().getTemplatePackageName();
        JahiaTemplatesPackage pkg = ServicesRegistry.getInstance().getJahiaTemplateManagerService().getTemplatePackage(tplPkgName);
        SortedSet<String> templates = getTemplatesSet(pkg, nt);

        List<Value> vs = new ArrayList<Value>();
        for (Iterator iterator = templates.iterator(); iterator.hasNext();) {
            String skin = (String) iterator.next();
            vs.add(new ValueImpl(skin, PropertyType.STRING, false));
        }
        return vs.toArray(new Value[vs.size()]);
    }

    public static SortedSet<String> getTemplatesSet(JahiaTemplatesPackage pkg, ExtendedNodeType nt) {
        SortedSet<String> templates = new TreeSet<String>();


        List<ExtendedNodeType> nodeTypeList = new ArrayList<ExtendedNodeType>(Arrays.asList(nt.getSupertypes()));
        nodeTypeList.add(nt);
        Collections.reverse(nodeTypeList);
        for (ExtendedNodeType t : nodeTypeList) {

            if (pkg != null) {
                for (String rootFolderPath : pkg.getLookupPath()) {
                    StringBuffer buff = new StringBuffer(64);
                    buff.append(rootFolderPath);
                    buff.append("/");
                    buff.append("modules/" + t.getAlias().replace(':','/') + "/html");
                    String testPath = buff.toString();
                    File f = new File(Jahia.getStaticServletConfig().getServletContext().getRealPath(testPath));
                    if (f.exists()) {
                        File[] files = f.listFiles();
                        for (File file : files) {
                            if (!file.isDirectory()) {
                                String filename = file.getName();
                                templates.add(filename.substring(0, filename.lastIndexOf(".")));
                            }
                        }
                    }

                }
            }
        }
        return templates;
    }

}