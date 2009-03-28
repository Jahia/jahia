/**
 * 
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
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
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
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
import org.jahia.services.search.JahiaSearchService;
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
   
    private JahiaSearchService searchService;
    
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

    public void setSearchService(JahiaSearchService searchService) {
        this.searchService = searchService;
    }    
    
    public void updateAllPageDefinitions(JahiaPageService pageService, JahiaSitesService siteService, JahiaPageTemplateService pageTemplateService) throws JahiaException {
        logger.info("Checking for updated page definitions...");

        Iterator<JahiaSite> sites = siteService.getSites();
        while (sites.hasNext()) {
            JahiaSite site = sites.next();
            if (site.getTemplatePackageName() != null) {
                JahiaTemplatesPackage pkg = templatePackageRegistry.lookup(site
                        .getTemplatePackageName());
                if (pkg != null) {
                    updatePageDefinitions(site, siteService, pageService, pkg, pageTemplateService);
                } else {
                    logger
                            .error("Unable to find a template package with the name '"
                                    + site.getTemplatePackageName()
                                    + "' used by the site '"
                                    + site.getTitle()
                                    + "'. This will case an exception accssing this virtual site.");
                }
            }
        }
        logger.info("Page definitions are up-to-date");
    }

    public void updatePageDefinitions(JahiaSite site,
            JahiaSitesService siteService,
            JahiaPageService pageService,
            JahiaTemplatesPackage templatePackage,
            JahiaPageTemplateService pageTemplateService) throws JahiaException {

        // first create new page definitions or update visibility flag for
        // existing page definitions
        for (JahiaTemplateDef templateDef : templatePackage.getTemplates()) {
            JahiaPageDefinition pageDef = null;
            try {
                pageDef = pageTemplateService.lookupPageTemplateByName(
                        templateDef.getName(), site.getID());
            } catch (JahiaTemplateNotFoundException e) {
                // ignore
            }
            if (pageDef == null) {
                // create new
                pageDef = pageTemplateService.createPageTemplate(site.getID(),
                        templateDef.getName(), null, templateDef.isVisible(),
                        templateDef.getPageType(),
                        templateDef.getDescription(), null, site.getAclID());
                logger.info("Creating new page definition '"
                        + templateDef.getName() + "' for site '"
                        + site.getTitle() + "'");
            } else if (pageDef.isAvailable() != templateDef.isVisible()
                    || (templateDef.getPageType() != null && !templateDef
                            .getPageType().equals(pageDef.getPageType()))) {
                // update existing one
                pageDef.setAvailable(templateDef.isVisible());
                pageDef.setPageType(templateDef.getPageType());
                pageDef.commitChanges(pageTemplateService);
                logger.info("Setting visibility flag to '"
                        + templateDef.isVisible() + "' for page definition '"
                        + templateDef.getName() + "' for site '"
                        + site.getTitle() + "'");
            }
            
            containerDefRegistry.buildContainerDefinitionsForTemplate(pageDef.getPageType(), site.getID(), pageDef.getID(), null);
        }
        
        searchService.initSearchFieldConfiguration(site.getID());
        
        // check for removed definitions
        Iterator<JahiaPageDefinition> pageDefs = pageTemplateService
                .getPageTemplates(site.getID(), false);

        while (pageDefs.hasNext()) {
            JahiaPageDefinition existingDefintion = pageDefs.next();
            if (templatePackage.lookupTemplate(existingDefintion.getName()) == null) {
                // we found a template that was removed

                // check if there were already pages created with this template
                List<Integer> pages = pageService
                        .getPageIDsWithTemplate(existingDefintion.getID());
                if (pages.isEmpty()) {
                    // no pages were created --> delete page definition
                    pageTemplateService.deletePageTemplate(existingDefintion
                            .getID());
                    logger.info("Deleting page definition '"
                            + existingDefintion.getName() + "' for site '"
                            + site.getTitle()
                            + "' as it was removed from the template set");
                } else {
                    // there are already pages with this definition --> warn
                    logger
                            .error("Unable to find template '"
                                    + existingDefintion.getName()
                                    + "' in the template set for site '"
                                    + site.getTitle()
                                    + "'. There were already "
                                    + pages.size()
                                    + " page(s) created with this template. IDs: ["
                                    + StringUtils.join(pages.iterator(), ", ")
                                    + "]. This will cause an exception when one of these pages will be called.");
                    // and at least set the visibility flag to 'false'
                    if (existingDefintion.isAvailable()) {
                        existingDefintion.setAvailable(false);
                        existingDefintion.commitChanges(pageTemplateService);
                    }
                }
            }
        }
        // check for default template
        JahiaPageDefinition defaultPage = null;
        try {
            defaultPage = pageTemplateService.lookupPageTemplateByName(
                    templatePackage.getDefaultPageName(), site.getID());
        } catch (JahiaTemplateNotFoundException e) {
            //ignore it
        }
        if (defaultPage != null
                && defaultPage.getID() != site.getDefaultTemplateID()) {
            site.setDefaultTemplateID(defaultPage.getID());
            siteService.updateSite(site);
            logger.info("Default template for site '" + site.getTitle()
                    + "' is set to '" + templatePackage.getDefaultPageName()
                    + "'");
        }
    }
    
    public void setContainerDefRegistry(JahiaContainerDefinitionsRegistry containerDefRegistry) {
        this.containerDefRegistry = containerDefRegistry;
    }
}