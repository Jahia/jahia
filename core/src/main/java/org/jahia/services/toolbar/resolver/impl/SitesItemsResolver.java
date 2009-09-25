package org.jahia.services.toolbar.resolver.impl;

import org.jahia.services.toolbar.resolver.ItemsResolver;
import org.jahia.services.toolbar.bean.Item;
import org.jahia.services.toolbar.bean.Selected;
import org.jahia.services.usermanager.JahiaGroupManagerService;
import org.jahia.services.sites.JahiaSite;
import org.jahia.data.JahiaData;
import org.jahia.registries.ServicesRegistry;
import org.jahia.ajax.gwt.client.data.toolbar.GWTJahiaToolbarItem;
import org.jahia.exceptions.JahiaException;

import java.util.List;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Sep 25, 2009
 * Time: 12:12:47 PM
 * To change this template use File | Settings | File Templates.
 */
public class SitesItemsResolver extends DefaultItemsResolver {
    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(SitesItemsResolver.class);

    public List<Item> getItems(JahiaData jahiaData) {
        List<Item> items = new ArrayList<Item>();

        try {
            JahiaGroupManagerService jahiaGroupManagerService = ServicesRegistry.getInstance().getJahiaGroupManagerService();
            List<JahiaSite> sitesList = jahiaGroupManagerService.getAdminGrantedSites(jahiaData.getProcessingContext().getUser());
            if (sitesList != null && sitesList.size() > 1) {
                for (JahiaSite site : sitesList) {
                    if (site.getHomePageID() > -1) {
                        Item item = createRedirectItem(jahiaData, site.getTitle(), site.getHomePage());
                        // add to itemsgroup
                        if (item != null) {
                            String minIconStyle = "gwt-toolbar-ItemsGroup-icons-site-min";
                            String maxIconStyle = "gwt-toolbar-ItemsGroup-icons-site-min";
                            item.setMediumIconStyle(maxIconStyle);
                            item.setMinIconStyle(minIconStyle);
                            if (jahiaData.getProcessingContext().getSiteID() == site.getID()) {
                                Selected s = new Selected();
                                s.setValue(true);
                                item.setSelected(s);
                            }
                            // add to group lis
                            items.add(item);
                        }
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
