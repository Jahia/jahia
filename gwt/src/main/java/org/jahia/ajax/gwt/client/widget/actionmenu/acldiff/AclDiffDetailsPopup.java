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
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
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
            item.addSelectionListener(new SelectionListener<ComponentEvent>() {
                public void componentSelected(ComponentEvent componentEvent) {
                    EngineOpener.openEngine(details.getUrl());
                }
            }) ;
        } else {
            item.setEnabled(false);
        }
        ButtonBar bar = new ButtonBar() ;
        bar.add(item) ;
        setButtonBar(bar);
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
