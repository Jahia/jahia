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
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
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
