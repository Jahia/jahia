/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *     Copyright (C) 2002-2014 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ======================================================================================
 *
 *     IF YOU DECIDE TO CHOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     "This program is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU General Public License
 *     as published by the Free Software Foundation; either version 2
 *     of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 *     As a special exception to the terms and conditions of version 2.0 of
 *     the GPL (or any later version), you may redistribute this Program in connection
 *     with Free/Libre and Open Source Software ("FLOSS") applications as described
 *     in Jahia's FLOSS exception. You should have received a copy of the text
 *     describing the FLOSS exception, also available here:
 *     http://www.jahia.com/license"
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ======================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 *
 *
 * ==========================================================================================
 * =                                   ABOUT JAHIA                                          =
 * ==========================================================================================
 *
 *     Rooted in Open Source CMS, Jahia’s Digital Industrialization paradigm is about
 *     streamlining Enterprise digital projects across channels to truly control
 *     time-to-market and TCO, project after project.
 *     Putting an end to “the Tunnel effect”, the Jahia Studio enables IT and
 *     marketing teams to collaboratively and iteratively build cutting-edge
 *     online business solutions.
 *     These, in turn, are securely and easily deployed as modules and apps,
 *     reusable across any digital projects, thanks to the Jahia Private App Store Software.
 *     Each solution provided by Jahia stems from this overarching vision:
 *     Digital Factory, Workspace Factory, Portal Factory and eCommerce Factory.
 *     Founded in 2002 and headquartered in Geneva, Switzerland,
 *     Jahia Solutions Group has its North American headquarters in Washington DC,
 *     with offices in Chicago, Toronto and throughout Europe.
 *     Jahia counts hundreds of global brands and governmental organizations
 *     among its loyal customers, in more than 20 countries across the globe.
 *
 *     For more information, please visit http://www.jahia.com
 */
package org.jahia.modules.macros.initializers;

import org.apache.commons.lang.StringUtils;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.services.content.nodetypes.initializers.ChoiceListValue;
import org.jahia.services.content.nodetypes.initializers.ModuleChoiceListInitializer;
import org.slf4j.Logger;

import javax.jcr.RepositoryException;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: Damien GAILLARD
 * Date: 11/13/13
 * Time: 4:50 PM
 */
public class MacrosChoiceListInitializer implements ModuleChoiceListInitializer {
    private transient static Logger logger = org.slf4j.LoggerFactory.getLogger(MacrosChoiceListInitializer.class);
    private String key;

    private String[] macroLookupPath;
    private List ignoreMacros;

    @Override
    public void setKey(String key) {
        this.key = key;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public List<ChoiceListValue> getChoiceListValues(ExtendedPropertyDefinition epd, String param, List<ChoiceListValue> values, Locale locale, Map<String, Object> context) {
        List<ChoiceListValue> macrosNames = new ArrayList<ChoiceListValue>();
        JCRNodeWrapper node = null;

        if (context.containsKey("contextNode") && context.get("contextNode") != null) {
            node = (JCRNodeWrapper) context.get("contextNode");
        } else if (context.containsKey("contextParent") && context.get("contextParent") != null) {
            node = (JCRNodeWrapper) context.get("contextParent");
        }

        if (node != null) {
            try {
                Set<JahiaTemplatesPackage> packages = new LinkedHashSet<JahiaTemplatesPackage>();

                packages.add(ServicesRegistry.getInstance().getJahiaTemplateManagerService().getTemplatePackageById("macros"));

                for (String s : node.getResolveSite().getInstalledModules()) {
                    JahiaTemplatesPackage pack = ServicesRegistry.getInstance().getJahiaTemplateManagerService().getTemplatePackageById(s);
                    if (pack != null) {
                        packages.add(pack);
                        final Collection<JahiaTemplatesPackage> dependencies = pack.getDependencies();
                        if (dependencies != null && !dependencies.isEmpty()) {
                            packages.addAll(dependencies);
                        }
                    }
                }

                for (JahiaTemplatesPackage aPackage : packages) {
                    for (String path : macroLookupPath) {
                        org.springframework.core.io.Resource[] resources = aPackage.getResources(path);
                        for (org.springframework.core.io.Resource resource : resources) {
                            String macroName = StringUtils.substringBefore(resource.getFilename(), ".");
                            if (ignoreMacros != null && !ignoreMacros.contains(macroName)) {
                                macroName = "##" + macroName + "##";
                                macrosNames.add(new ChoiceListValue(macroName, macroName));
                            }
                        }
                    }
                }
            } catch (RepositoryException e) {
                logger.error("Cannot resolve site", e);
            }
        }
        return macrosNames;
    }

    public void setMacroLookupPath(String macroLookupPath) {
        this.macroLookupPath = macroLookupPath.split(",");
    }

    public void setIgnoreMacros(List ignoreMacros) {
        this.ignoreMacros = ignoreMacros;
    }
}
