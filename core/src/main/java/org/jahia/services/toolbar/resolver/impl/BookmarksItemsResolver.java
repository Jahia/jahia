package org.jahia.services.toolbar.resolver.impl;

import org.jahia.services.toolbar.bean.Item;
import org.jahia.services.preferences.JahiaPreferencesProvider;
import org.jahia.services.preferences.JahiaPreference;
import org.jahia.services.preferences.exception.JahiaPreferenceProviderException;
import org.jahia.services.preferences.bookmarks.BookmarksJahiaPreference;
import org.jahia.services.pages.ContentPage;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.content.decorator.JCRJahiaContentNode;
import org.jahia.data.JahiaData;
import org.jahia.registries.ServicesRegistry;

import java.util.List;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Sep 25, 2009
 * Time: 12:07:14 PM
 * To change this template use File | Settings | File Templates.
 */
public class BookmarksItemsResolver   extends DefaultItemsResolver {
    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(BookmarksItemsResolver.class);
    private static JahiaPreferencesProvider bookmarksPreferencesProvider;

    public List<Item> getItems(JahiaData jahiaData) {

        List<Item> items = new ArrayList<Item>();
        // get bookmarks provider
        JahiaPreferencesProvider jahiaPreferencesProvider = getBookmarksJahiaPreferencesProvider();
        List<JahiaPreference> jahiaPreferenceList = jahiaPreferencesProvider.getAllJahiaPreferences(jahiaData.getProcessingContext());
        if (jahiaPreferenceList != null) {
            for (JahiaPreference pref : jahiaPreferenceList) {
                // current bookmark
                BookmarksJahiaPreference bPref = (BookmarksJahiaPreference) pref.getNode();
                try {
                    String pageUUID = bPref.getPageUUID();
                    ContentPage contentPage = getContentPage(pageUUID, jahiaData.getProcessingContext().getUser());
                    int pid = contentPage.getPageID();

                    Item item = createRedirectItem(jahiaData, null, pid);
                    if (item != null) {
                        String minIconStyle = "gwt-toolbar-ItemsGroup-icons-bookmark-min";
                        String maxIconStyle = "gwt-toolbar-ItemsGroup-icons-bookmark-min";
                        item.setMediumIconStyle(maxIconStyle);
                        item.setMinIconStyle(minIconStyle);
                        // add to itemsgroup
                        items.add(item);
                    }
                } catch (Exception e) {
                    logger.error(e, e);
                }
            }
        }
        return items;
    }

    /**
     * Get Bookmark jahia preference provider
     *
     * @return
     */
    private JahiaPreferencesProvider getBookmarksJahiaPreferencesProvider() {
        try {
            if (bookmarksPreferencesProvider == null) {
                bookmarksPreferencesProvider = ServicesRegistry.getInstance().getJahiaPreferencesService().getPreferencesProviderByType("bookmarks");
            }
            return bookmarksPreferencesProvider;
        } catch (JahiaPreferenceProviderException e) {
            logger.error(e, e);
        }
        return null;
    }


    /**
      * Get Content page from uuid
      * @param uuid
      * @param jahiaUser
      * @return
      */
     private static ContentPage getContentPage(String uuid, JahiaUser jahiaUser) {
         try {
             JCRJahiaContentNode nodeWrapper = (JCRJahiaContentNode) ServicesRegistry.getInstance().getJCRStoreService().getSessionFactory().getCurrentUserSession().getNodeByUUID(uuid);
             return (ContentPage) nodeWrapper.getContentObject();
         } catch (Exception e) {
             logger.error(e, e);
             return null;
         }
     }

}
