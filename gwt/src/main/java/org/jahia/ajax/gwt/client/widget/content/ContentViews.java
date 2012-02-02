/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

package org.jahia.ajax.gwt.client.widget.content;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.EventType;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import org.jahia.ajax.gwt.client.widget.LinkerSelectionContext;
import org.jahia.ajax.gwt.client.widget.tripanel.TopRightComponent;
import org.jahia.ajax.gwt.client.data.toolbar.GWTManagerConfiguration;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;

import java.util.List;

/**
 * User: rfelden
 * Date: 16 sept. 2008 - 09:46:42
 */
public class ContentViews extends TopRightComponent {

    private TableView tableView;
    private ThumbView thumbView;
    private ThumbView detailedThumbView;

    private ContentPanel m_component;
    private AbstractView current;

    private GWTManagerConfiguration configuration;

    public ContentViews(GWTManagerConfiguration config) {
        configuration = config;
        tableView = new TableView(config);
        thumbView = new ThumbView(config, false);
        detailedThumbView = new ThumbView(config, true);
        m_component = new ContentPanel(new FitLayout());
//        m_component.setHeaderVisible(false);
        m_component.setBorders(false);
//        m_component.setBodyBorder(false);

        // set default view
        if ("list".equals(config.getDefaultView())) {
            current = tableView;
        } else if ("thumbs".equals(config.getDefaultView())) {
            current = thumbView;
        } else if ("detailed".equals(config.getDefaultView())) {
            current = detailedThumbView;
        } else {
            current = tableView;
        }
        m_component.setBottomComponent(getToolBar());
        getToolBar().disable();
        m_component.add(current.getComponent());
        m_component.addListener(Events.ContextMenu, new Listener<ComponentEvent>() {
            public void handleEvent(ComponentEvent be) {
                getLinker().getSelectionContext().refresh(LinkerSelectionContext.BOTH);
            }
        });

    }

    public void setBottomComponent (Component bottomComponent) {
        m_component.setBottomComponent(bottomComponent);
    }

    public void switchToListView() {
        switchToView(tableView);
    }

    public void switchToThumbView() {
        switchToView(thumbView);
    }

    public void switchToDetailedThumbView() {
        switchToView(detailedThumbView);
    }

    public void switchToView(AbstractView newView) {
        if (current != newView) {
            List<GWTJahiaNode> hiddenSelection = current.getHiddenSelection();
            List<GWTJahiaNode> visibleSelection = current.getVisibleSelection();
            clearTable();
            m_component.removeAll();
            current = newView;
            m_component.add(current.getComponent());
            m_component.layout();
            refresh();

            newView.setHiddenSelection(hiddenSelection);
            newView.setVisibleSelection(visibleSelection);
//            getLinker().handleNewSelection();
        }
    }

    public void setSelectionMode(Style.SelectionMode mode) {
        tableView.getSelectionModel().setSelectionMode(mode);
        thumbView.getSelectionModel().setSelectionMode(mode);
        detailedThumbView.getSelectionModel().setSelectionMode(mode);
    }

    public void addSelectionListener(EventType eventType, Listener listener) {
        tableView.getSelectionModel().addListener(eventType, listener);
        thumbView.getSelectionModel().addListener(eventType, listener);
        detailedThumbView.getSelectionModel().addListener(eventType, listener);
    }

    public void initWithLinker(ManagerLinker linker) {
        super.initWithLinker(linker);
        tableView.initWithLinker(linker);
        thumbView.initWithLinker(linker);
        detailedThumbView.initWithLinker(linker);
    }

    public void setContent(Object root) {
        if (current != null) {
            current.setContent(root);
        }
    }

    public void setProcessedContent(Object content, AbstractView.ContentSource source) {
        if (current != null) {
            current.setProcessedContent(content, source);
        }
    }

    public void clearTable() {
        if (current != null) {
            current.clearTable();
        }
    }

    public List<GWTJahiaNode> getSelection() {
        if (current != null) {
            return current.getSelection();
        } else {
            return null;
        }
    }

    public List<GWTJahiaNode> getHiddenSelection() {
        if (current != null) {
            return current.getHiddenSelection();
        } else {
            return null;
        }
    }


    public void selectNodes(List<GWTJahiaNode> nodes) {
        if (current != null) {
            current.selectNodes(nodes);
        }
    }

    public void refresh() {
        if (current != null) {
            current.refresh();
        }
    }

    public Component getComponent() {
        return m_component;
    }

    @Override
    public void clearSelection() {
        super.clearSelection();
        tableView.clearSelection();
        thumbView.clearSelection();
    }

    public AbstractView getCurrentView() {
        return current;
    }
}
