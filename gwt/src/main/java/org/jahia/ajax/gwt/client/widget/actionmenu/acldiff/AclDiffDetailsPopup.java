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
package org.jahia.ajax.gwt.client.widget.actionmenu.acldiff;

import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.data.BaseModelData;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import org.jahia.ajax.gwt.client.data.actionmenu.acldiff.GWTJahiaAclDiffDetails;
import org.jahia.ajax.gwt.client.util.EngineOpener;

import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;

/**
 * User: rfelden
 * Date: 21 janv. 2009 - 15:42:26
 */
public class AclDiffDetailsPopup extends Window {

    public AclDiffDetailsPopup(final GWTJahiaAclDiffDetails details, final String iconStyle) {
        super() ;
        setLayout(new FitLayout());
        setScrollMode(Style.Scroll.AUTO);
        ListStore<UserRights> rightsList = new ListStore<UserRights>() ;
        List<ColumnConfig> columns = new ArrayList<ColumnConfig>(3) ;
        columns.add(new ColumnConfig("principal", "User", 150)) ;
        columns.add(new ColumnConfig("rights", "Rights", 50)) ;
        columns.add(new ColumnConfig("inhRights", "Inherited", 50)) ;
        ColumnModel rightsModel = new ColumnModel(columns) ;
        Grid<UserRights> rights = new Grid<UserRights>(rightsList, rightsModel) ;
        Set<String> principals = new HashSet<String>(details.getRights().keySet()) ;
        principals.addAll(details.getInheritedRights().keySet()) ;
        for (String principal: principals) {
            rightsList.add(new UserRights(principal, details.getRights().get(principal), details.getInheritedRights().get(principal)));
        }
        add(rights);
        Button item = new Button("Open engine") ;
        if (details.getUrl() != null) {
            item.addSelectionListener(new SelectionListener<ButtonEvent>() {
                public void componentSelected(ButtonEvent componentEvent) {
                    EngineOpener.openEngine(details.getUrl());
                }
            }) ;
        } else {
            item.setEnabled(false);
        }
        ButtonBar bar = new ButtonBar() ;
        bar.add(item) ;
        setTopComponent(bar);
        setIconStyle(iconStyle);
        setHeading("ACL");
        setSize(270,230);
    }

    private class UserRights extends BaseModelData {

        public UserRights(String principal, String rights, String inhRights) {
            set("principal", principal) ;
            if (rights != null) {
                set("rights", rights) ;
            } else {
                set("rights", "") ;
            }
            if (inhRights != null) {
                set("inhRights", inhRights) ;
            } else {
                set("inhRights", "") ;
            }
        }

    }

}
