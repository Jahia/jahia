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
package org.jahia.ajax.gwt.client.widget.definition;

import com.extjs.gxt.ui.client.Events;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.TabPanel;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import org.jahia.ajax.gwt.client.widget.AsyncTabItem;
import org.jahia.ajax.gwt.client.widget.tripanel.BottomRightComponent;
import org.jahia.ajax.gwt.client.service.definition.ContentDefinitionServiceAsync;
import org.jahia.ajax.gwt.client.service.definition.ContentDefinitionService;


/**
 * Created by IntelliJ IDEA.
 *
 * @author rfelden
 * @version 23 juin 2008 - 16:15:46
 */
public class ContentDefinitionDetails extends BottomRightComponent {

    private ContentPanel m_component;
    private AsyncTabItem info;
    private TabPanel tabs;

    private final ContentDefinitionServiceAsync service = ContentDefinitionService.App.getInstance();

    public ContentDefinitionDetails() {
        super();
        m_component = new ContentPanel(new FitLayout());
        tabs = new TabPanel();
        info = new AsyncTabItem();
        info.setText("Information");
        tabs.add(info);

        tabs.addListener(Events.Select, new Listener<ComponentEvent>() {
            public void handleEvent(ComponentEvent event) {
            }
        });

        m_component.add(tabs);
    }


    public void clear() {
    }

    public void fillData(Object selectedItem) {
        clear();
        if (selectedItem != null) {
        }
    }

    private void fillCurrentTab() {
        TabItem currentTab = tabs.getSelectedItem();
        if (!currentTab.isEnabled()) {
            currentTab = info;
            tabs.setSelection(currentTab);
        }
        if (currentTab == info) {
        }
    }

    public Component getComponent() {
        return m_component;
    }

    // INFO TAB

}
