/**
 * Jahia Enterprise Edition v6
 *
 * Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.
 *
 * Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
 * with Document Management and Portal features.
 *
 * The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED.
 *
 * Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
 * you and Jahia (Jahia Sustainable Enterprise License - JSEL).
 *
 * If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.services.content.nodetypes.initializers;

import org.jahia.params.ProcessingContext;
import org.jahia.bin.Jahia;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.nodetypes.ValueImpl;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.utils.i18n.ResourceBundleMarker;

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
public class Skins implements ValueInitializer {

    public Value[] getValues(ProcessingContext jParams, ExtendedPropertyDefinition declaringPropertyDefinition, List<String> params) {
        String tplPkgName = jParams.getSite().getTemplatePackageName();
        JahiaTemplatesPackage pkg = ServicesRegistry.getInstance().getJahiaTemplateManagerService().getTemplatePackage(tplPkgName);
        SortedSet skins = new TreeSet();
        for (Iterator iterator = pkg.getLookupPath().iterator(); iterator.hasNext();) {
            String rootFolderPath = (String) iterator.next();
            File f = new File(Jahia.getStaticServletConfig().getServletContext().getRealPath(rootFolderPath+"/skins"));
            if (f.exists()) {
                File[] files = f.listFiles();
                for (File file : files) {
                    if (file.isDirectory()) skins.add(file.getName());
                }
            }
        }
        List<Value> vs = new ArrayList<Value>();
        for (Iterator iterator = skins.iterator(); iterator.hasNext();) {
            String skin = (String) iterator.next();
            vs.add(new ValueImpl(ResourceBundleMarker.drawMarker(pkg.getResourceBundleName(),"skins."+skin,skin), PropertyType.STRING, false));
        }
        return vs.toArray(new Value[vs.size()]);

    }
}
