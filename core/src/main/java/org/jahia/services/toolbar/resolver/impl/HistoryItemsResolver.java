package org.jahia.services.toolbar.resolver.impl;

import org.jahia.services.toolbar.resolver.ItemsResolver;
import org.jahia.services.toolbar.bean.Item;
import org.jahia.services.pages.JahiaPage;
import org.jahia.data.JahiaData;
import org.jahia.data.beans.history.HistoryBean;
import org.jahia.operations.valves.HistoryValve;
import org.jahia.ajax.gwt.client.data.toolbar.GWTJahiaToolbarItem;
import org.jahia.ajax.gwt.client.data.GWTJahiaProperty;
import org.jahia.ajax.gwt.client.widget.toolbar.ActionItemFactory;
import org.jahia.registries.ServicesRegistry;
import org.jahia.exceptions.JahiaException;

import java.util.List;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Sep 25, 2009
 * Time: 12:07:32 PM
 * To change this template use File | Settings | File Templates.
 */
public class HistoryItemsResolver extends DefaultItemsResolver {
    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(HistoryItemsResolver.class);

    private int historyPathLength;

    public int getHistoryPathLength() {
        return historyPathLength;
    }

    public void setHistoryPathLength(int historyPathLength) {
        this.historyPathLength = historyPathLength;
    }

    public List<Item> getItems(JahiaData jahiaData) {
        List<Item> gwtToolbarItemsList = new ArrayList<Item>();
        // create the list item
        List<HistoryBean> historyBeanList = (List<HistoryBean>) jahiaData.getProcessingContext().getSessionState().getAttribute(HistoryValve.ORG_JAHIA_TOOLBAR_HISTORY);
        if (historyBeanList != null) {
            if (historyPathLength < 0) {
                historyPathLength = historyBeanList.size();
            }
            int iteratorsize = historyPathLength < historyBeanList.size() ? historyPathLength : historyBeanList.size();
            for (int i = 0; i < iteratorsize; i++) {
                HistoryBean cHistoryBean = historyBeanList.get(i);
                int pid = cHistoryBean.getPid();
                try {
                    Item item = createRedirectItem(jahiaData, null, pid);
                    if (item != null) {
                        String minIconStyle = "gwt-toolbar-ItemsGroup-icons-page-min";
                        String maxIconStyle = "gwt-toolbar-ItemsGroup-icons-page-normal";
                        item.setMediumIconStyle(maxIconStyle);
                        item.setMinIconStyle(minIconStyle);
                        // add to itemsgroup
                        gwtToolbarItemsList.add(item);
                    }
                } catch (Exception e) {
                    logger.error(e, e);
                }
            }
        }
        return gwtToolbarItemsList;
    }



}
