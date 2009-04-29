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
package org.jahia.ajax.gwt.module.pagepicker.client;

import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.DOM;
import com.google.gwt.core.client.EntryPoint;
import com.allen_sauer.gwt.log.client.Log;
import org.jahia.ajax.gwt.client.util.JahiaGWT;
import org.jahia.ajax.gwt.client.widget.pagepicker.PagePicker;
import org.jahia.ajax.gwt.client.widget.pagepicker.PagePickerViewport;

/**
 * Created by IntelliJ IDEA.
 * User: rfelden
 * Date: 2 sept. 2008
 * Time: 17:44:14
 * To change this template use File | Settings | File Templates.
 */
public class PagePickerEntryPoint  implements EntryPoint {

    private static final String PAGE_PICKER_ID = "gwtpagepicker" ;

    public void onModuleLoad() {
        JahiaGWT.init();
        Log.debug("onload");
        RootPanel panel = RootPanel.get(PAGE_PICKER_ID) ;
        if (panel != null) {
            String operation = DOM.getElementAttribute(panel.getElement(), "selectPageOperation");
            String parentPath = DOM.getElementAttribute(panel.getElement(), "parentPath");
            String pagePath = DOM.getElementAttribute(panel.getElement(), "pagePath");
            String homepageIdStr = DOM.getElementAttribute(panel.getElement(), "homePageID");
            String siteIdStr = DOM.getElementAttribute(panel.getElement(), "siteID");
            String callback = DOM.getElementAttribute(panel.getElement(), "callback");

            int homepageId = -1;
            if (homepageIdStr != null && !"".equals(homepageIdStr)) {
                homepageId = Integer.parseInt(homepageIdStr);
            }
            int siteId = -1;
            if (siteIdStr != null && !"".equals(siteIdStr)) {
                siteId = Integer.parseInt(siteIdStr);
            }

            if (callback == null || callback.length() == 0) {
                panel.add(new PagePicker(operation, pagePath, parentPath , homepageId, siteId, callback));
            } else {
                panel.add(new PagePickerViewport(operation, pagePath, parentPath , homepageId, siteId, callback));
            }

        }
    }

}
