/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
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
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
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
