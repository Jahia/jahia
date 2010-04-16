package org.jahia.services.uicomponents.resolver.toolbar.impl;

import org.jahia.exceptions.JahiaException;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.decorator.JCRSiteNode;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.uicomponents.bean.toolbar.Item;
import org.jahia.services.usermanager.JahiaGroupManagerService;
import org.jahia.services.usermanager.JahiaUser;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Sep 25, 2009
 * Time: 12:12:47 PM
 * To change this template use File | Settings | File Templates.
 */
public class SitesItemsResolver extends DefaultItemsResolver {
    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(SitesItemsResolver.class);

    public List<Item> getItems(JCRSiteNode currentsite, JahiaUser user, Locale locale) {
        List<Item> items = new ArrayList<Item>();

        try {
            JahiaGroupManagerService jahiaGroupManagerService = ServicesRegistry.getInstance().getJahiaGroupManagerService();
            List<JahiaSite> sitesList = jahiaGroupManagerService.getAdminGrantedSites(user);
            if (sitesList != null && sitesList.size() > 1) {
                for (JahiaSite site : sitesList) {
                    if (site.getHomePageID() > -1) {
                        Item item = null;
                    }
                }
            }
        } catch (JahiaException e) {
            logger.error("JahiaException: Error while creating change site link", e);
        } catch (Exception e) {
            logger.error("Error while creating change site link", e);
        }
        return items;
    }
}
