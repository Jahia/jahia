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
package org.jahia.ajax.gwt.client.widget.menu;

import com.extjs.gxt.ui.client.util.WidgetHelper;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.google.gwt.user.client.Element;

/**
 * Created by IntelliJ IDEA.
 * User: hollis
 * Date: 27 ao�t 2008
 * Time: 11:44:37
 * To change this template use File | Settings | File Templates.
 */
public class VersionMenu extends Menu {

    private VersionMenuItem item;

    public VersionMenu(VersionMenuItem menuItem) {
        super();
        item = menuItem;
        add(item);
    }

    @Override
    protected void doAttachChildren() {
      super.doAttachChildren();
      WidgetHelper.doAttach(item.getComponent());
    }

    @Override
    protected void doDetachChildren() {
      super.doDetachChildren();
      WidgetHelper.doDetach(item.getComponent());
    }

    public void show(Element elem, String pos) {
        item.getComponent().show();
    }

    public void show() {
        item.getComponent().show();
    }

}
