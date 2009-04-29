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
package org.jahia.ajax.gwt.client.widget.process;

import org.jahia.ajax.gwt.client.data.GWTJahiaProcessJob;
import org.jahia.ajax.gwt.client.widget.ReportGrid;
import org.jahia.ajax.gwt.client.widget.tripanel.BottomRightComponent;

import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;

/**
 * User: jahia
 * Date: 24 juil. 2008
 * Time: 13:17:03
 */
public class ProcessJobMessagesTab extends BottomRightComponent {
    private ContentPanel m_component;


    public ProcessJobMessagesTab() {
        super();
    }

    /**
     * Create main UI
     */
    public void createUI() {
        // create main panel
        m_component = new ContentPanel(new FitLayout());
        m_component.setHeaderVisible(false);
    }

    /**
     * Clear messages about current ProcessJob
     */
    public void clear() {
        m_component.setHeading("&nbsp;");
    }

    /**
     * Display info about current process job
     *
     * @param selectedItem
     */
    public void fillData(Object selectedItem) {
        GWTJahiaProcessJob element = (GWTJahiaProcessJob) selectedItem;
        m_component.setHeading(element.getJobName());
        m_component.removeAll();

        ReportGrid grid = new ReportGrid(element.getActions(), element.getTitleForObjectKey(), true, element.getLogs(), true);
        m_component.add(grid);
        m_component.layout();
    }

    /**
     * Get main component
     *
     * @return
     */
    public Component getComponent() {
        return m_component;
    }
}
