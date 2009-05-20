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
package org.jahia.ajax.gwt.client.widget.layoutmanager.picker;

import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.layout.FlowLayout;
import com.extjs.gxt.ui.client.widget.layout.AccordionLayout;
import com.extjs.gxt.ui.client.Style;

/**
 * Created by IntelliJ IDEA.
 * User: jahia
 * Date: 23 janv. 2009
 * Time: 17:13:04
 * To change this template use File | Settings | File Templates.
 */
public class JahiaPortletLeftComponent extends LayoutContainer {
    private JahiaFolderPortletTree sharedPortletFolderView = new JahiaFolderPortletTree();
    private JahiaFolderPortletTree sitePortletFolderView = new JahiaFolderPortletTree();
    private JahiaFolderPortletTree myPortletFolderView = new JahiaFolderPortletTree();


    public JahiaPortletLeftComponent() {
        setLayout(new FlowLayout(10));

        ContentPanel panel = new ContentPanel();
        panel.setHeading("AccordionLayout");
        panel.setBodyBorder(false);

        panel.setLayout(new AccordionLayout());
        panel.setIconStyle("icon-accordion");

        ContentPanel cp = new ContentPanel();
        cp.setHeading("Shared");
        cp.setScrollMode(Style.Scroll.AUTO);
        cp.add(sharedPortletFolderView);
        panel.add(cp);
        

        cp = new ContentPanel();
        cp.setHeading("Site");
        cp.add(sitePortletFolderView);
        panel.add(cp);

        cp = new ContentPanel();
        cp.setHeading("Private");
        cp.add(myPortletFolderView);
        panel.add(cp);

        panel.setSize(200, 325);
        add(panel);
    }

}
