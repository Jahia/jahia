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
package org.jahia.services.templates;

import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jahia.data.templates.JahiaTemplateDef;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaTemplateNotFoundException;
import org.jahia.registries.JahiaContainerDefinitionsRegistry;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.pages.JahiaPageDefinition;
import org.jahia.services.pages.JahiaPageService;
import org.jahia.services.pages.JahiaPageTemplateService;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.sites.JahiaSitesService;

/**
 * Helper class for creating and updating page definitions.
 * 
 * @author Sergiy Shyrkov
 */
public class PageDefinitionHelper {

    private static Logger logger = Logger.getLogger(PageDefinitionHelper.class);

    private TemplatePackageRegistry templatePackageRegistry;
   
    private JahiaContainerDefinitionsRegistry containerDefRegistry;

    public void setDefaultTemplate(JahiaSite site,
            JahiaTemplatesPackage templatePackage) throws JahiaException {
        JahiaPageTemplateService pageTemplateService = ServicesRegistry
                .getInstance().getJahiaPageTemplateService();

        site.setDefaultTemplateID(pageTemplateService.lookupPageTemplateByName(
                templatePackage.getDefaultPageTemplate().getName(),
                site.getID()).getID());

        ServicesRegistry.getInstance().getJahiaSitesService().updateSite(site);
    }

    public void setTemplatePackageRegistry(
            TemplatePackageRegistry tmplPackageRegistry) {
        templatePackageRegistry = tmplPackageRegistry;
    }

    public void setContainerDefRegistry(JahiaContainerDefinitionsRegistry containerDefRegistry) {
        this.containerDefRegistry = containerDefRegistry;
    }
}