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
package org.jahia.data.viewhelper.sitemap;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.pages.ContentPage;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.sites.JahiaSitesService;
import org.jahia.services.usermanager.JahiaUser;

/**
 * @author pap
 */
public class AllSitesDisplayFilter extends LimitedTemplatesFilter {

    private static Logger logger = Logger
            .getLogger(AllSitesDisplayFilter.class);

    public List<ContentPage> getStartPages(ContentPage startPage, JahiaUser user,
            int pageInfosFlag, List<Locale> locales, ProcessingContext params) {
        List<ContentPage> startPages = new ArrayList<ContentPage>();
        JahiaSitesService siteService = ServicesRegistry.getInstance()
                .getJahiaSitesService();

        try {
            for (Iterator<JahiaSite> siteEnum = siteService.getSites(); siteEnum
                    .hasNext();) {
                JahiaSite site = siteEnum.next();

                ContentPage contentPage = site.getHomeContentPage();

                if (contentPage != null) {
                    startPages.add(contentPage);
                }
            }
        } catch (JahiaException e) {
            logger.error("Exception obtaining site homepages", e);
        }

        return startPages;
    }
}
