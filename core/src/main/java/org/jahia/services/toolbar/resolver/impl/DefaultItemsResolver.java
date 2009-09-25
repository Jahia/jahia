package org.jahia.services.toolbar.resolver.impl;

import org.jahia.ajax.gwt.client.widget.toolbar.ActionItemFactory;
import org.jahia.data.JahiaData;
import org.jahia.exceptions.JahiaException;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.pages.JahiaPage;
import org.jahia.services.toolbar.bean.Item;
import org.jahia.services.toolbar.bean.Property;
import org.jahia.services.toolbar.resolver.ItemsResolver;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Sep 25, 2009
 * Time: 12:22:08 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class DefaultItemsResolver implements ItemsResolver {
    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(DefaultItemsResolver.class);

    /**
     * create a redirect toolitem. If itemTitle is null, then the pageTitle will be the itemTitle.
     *
     * @param jahiaData
     * @param pid
     * @return
     * @throws org.jahia.exceptions.JahiaException
     */
    protected Item createRedirectItem(JahiaData jahiaData, String itemTitle, Integer pid) {
        try {
            JahiaPage jahiaPage = ServicesRegistry.getInstance().getJahiaPageService().lookupPage(pid, jahiaData.getProcessingContext());

            return createRedirectItem(jahiaData, itemTitle, jahiaPage);
        } catch (JahiaException e) {
            logger.debug("Page with id[" + pid + "] has been deleted");
            return null;
        }
    }

    /**
     * create a redirect toolitem
     *
     * @param jahiaData
     * @return
     * @throws JahiaException
     */
    protected Item createRedirectItem(JahiaData jahiaData, String itemTitle, JahiaPage jahiaPage) throws JahiaException {
        if (jahiaPage != null) {
            String url = jahiaData.getProcessingContext().composePageUrl(jahiaPage);
            if (url == null) {
                return null;
            }
            String title = itemTitle;
            if (title == null) {
                title = jahiaPage.getTitle();
                if (title == null || title.length() == 0) {
                    title = "[pid=" + jahiaPage.getID() + "]";
                }
            }

            // create the toolitem
            Item item = new Item();
            item.setTitle(title);
            item.setType(ActionItemFactory.REDIRECT_WINDOW);
            item.setDisplayTitle(true);

            // add url property
            Property property = new Property();
            property.setName("url");
            property.setValue(url);
            item.addProperty(property);
            return item;

        }
        return null;
    }



}
