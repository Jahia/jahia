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

package org.jahia.ajax.gwt.client.widget.definition;

import com.extjs.gxt.ui.client.Events;
import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.DataList;
import com.extjs.gxt.ui.client.widget.DataListItem;
import com.extjs.gxt.ui.client.widget.button.ToolButton;
import com.extjs.gxt.ui.client.widget.layout.AccordionLayout;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.allen_sauer.gwt.log.client.Log;
import org.jahia.ajax.gwt.client.service.definition.ContentDefinitionService;
import org.jahia.ajax.gwt.client.service.definition.ContentDefinitionServiceAsync;
import org.jahia.ajax.gwt.client.widget.tripanel.BrowserLinker;
import org.jahia.ajax.gwt.client.widget.tripanel.LeftComponent;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeType;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 *
 * @author rfelden
 * @version 19 juin 2008 - 15:57:08
 */
public class LeftPanel extends LeftComponent {

    private ContentPanel m_component ;

    private ContentPanel availableTypesPanel;
    private ContentPanel selectorsPanel;

    private DataList typesList;

    private ContentDefinitionServiceAsync service = ContentDefinitionService.App.getInstance() ;

    public LeftPanel() {
        m_component = new ContentPanel(new AccordionLayout()) ;

        m_component.setScrollMode(Style.Scroll.AUTO);

        availableTypesPanel = new ContentPanel(new FitLayout()) ;
        availableTypesPanel.setScrollMode(Style.Scroll.AUTO);
        availableTypesPanel.setHeading("Types") ;

        typesList = new DataList() ;
        typesList.setFlatStyle(true);
        typesList.setScrollMode(Style.Scroll.AUTO);
        typesList.setSelectionMode(Style.SelectionMode.SINGLE);
        availableTypesPanel.getHeader().addTool(new ToolButton("x-tool-refresh", new SelectionListener<ComponentEvent>() {
            public void componentSelected(ComponentEvent event) {
                retrieveTypes() ;
            }
        }));

        availableTypesPanel.add(typesList) ;

        typesList.addListener(Events.SelectionChange, new Listener<ComponentEvent>() {
            public void handleEvent(ComponentEvent event) {
                Log.debug("Selection changed :" + typesList.getSelectedItem());
                if (typesList.getSelectedItems().size() == 1) {
                    getLinker().getTopRightObject().setContent(typesList.getSelectedItem().getData("nodetype"));
                }
            }
        });

        selectorsPanel = new ContentPanel(new FitLayout()) ;
        selectorsPanel.setScrollMode(Style.Scroll.NONE);
        selectorsPanel.setHeading("Selectors") ;


        m_component.add(availableTypesPanel) ;
        m_component.add(selectorsPanel) ;

        availableTypesPanel.addListener(Events.Expand, new ChangeAccordionListener<ComponentEvent>()) ;
        selectorsPanel.addListener(Events.Expand, new ChangeAccordionListener<ComponentEvent>()) ;
    }
    
    private class ChangeAccordionListener<T extends ComponentEvent> implements Listener<T> {
        public void handleEvent(T t) {
            getLinker().onTreeItemSelected();
        }
    }

    public void initWithLinker(BrowserLinker linker) {
        super.initWithLinker(linker);
        retrieveTypes();
    }
    
    public void openAndSelectItem(Object item) {
        if (item != null) {
        }
    }

    public void refresh() {
    }

    private void globalRefresh() {
        refresh() ;
    }

    private void retrieveTypes() {
        typesList.removeAll() ;
        service.getNodeTypes(new AsyncCallback<List<GWTJahiaNodeType>>() {
            public void onFailure(Throwable throwable) {
                // ...
            }
            public void onSuccess(List<GWTJahiaNodeType> gwtJahiaNodeTypes) {
                for (GWTJahiaNodeType query: gwtJahiaNodeTypes) {
                    addNodeType(query, false);
                }
                if (selectorsPanel.isExpanded()) {
                    getLinker().onTreeItemSelected();
                }
            }
        });
    }

    public Object getSelectedItem() {
        if (availableTypesPanel.isExpanded()) {
            if (typesList.getSelectedItems().size() == 1) {
                return typesList.getSelectedItem().getData("nodetype");
            } else {
                return null ;
            }
        } else {
            return null ;
        }
    }

    public Component getComponent() {
        return m_component ;
    }

    public void addNodeType(GWTJahiaNodeType type, boolean expandSearchPanel) {
        DataListItem typeItem = new DataListItem() ;
        typeItem.setData("nodetype", type);
        typeItem.setText(type.getName());
        typesList.add(typeItem);
        if (expandSearchPanel) {
            availableTypesPanel.setExpanded(true);
        }
    }

}
