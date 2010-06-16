/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2010 Jahia Solutions Group SA. All rights reserved.
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

package org.jahia.ajax.gwt.client.widget.edit.contentengine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.layout.RowData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import org.jahia.ajax.gwt.client.data.GWTJahiaLanguage;
import org.jahia.ajax.gwt.client.data.acl.GWTJahiaNodeACE;
import org.jahia.ajax.gwt.client.data.acl.GWTJahiaNodeACL;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.util.acleditor.AclEditor;

/**
 * Represents a dedicated tab for viewing workflow status and history
 * information.
 * 
 * @author Sergiy Shyrkov
 */
public class WorkflowTabItem extends EditEngineTabItem {
    private LayoutContainer container;

    private WorkflowHistoryPanel activePanel;

    private Map<String, WorkflowHistoryPanel> panelsByLanguage;

    /**
     * Initializes an instance of this class.
     * 
     * @param engine reference to the owner
     */
    public WorkflowTabItem(NodeHolder engine) {
        super(Messages.get("label.engineTab.workflow", "Workflow"), engine);
        //setIcon(ContentModelIconProvider.CONTENT_ICONS.workflow());
        panelsByLanguage = new HashMap<String, WorkflowHistoryPanel>(1);
    }

    @Override
    public void create(GWTJahiaLanguage locale) {
        if (engine.getNode() == null) {
            return;
        }

        if (container == null) {
            container = new LayoutContainer(new RowLayout());

//            AclEditor rightsEditor = new AclEditor(new GWTJahiaNodeACL(new ArrayList<GWTJahiaNodeACE>()), null);
//
//            container.add(rightsEditor.renderNewAclPanel(), new RowData(1,0.5));

            add(container);
        }

        WorkflowHistoryPanel next = getPanel(locale.getLanguage());
        if (activePanel != null) {
            if (activePanel == next) {
                // same as current --> do nothing
                return;
            }
            activePanel.removeFromParent();
        }
        container.add(next,new RowData(1,0.5));

        activePanel = next;

        layout();
    }

    private WorkflowHistoryPanel getPanel(String locale) {
        WorkflowHistoryPanel panel = panelsByLanguage.get(locale);
        if (panel == null) {
            panel = new WorkflowHistoryPanel(engine.getNode().getUUID(), locale);
            panel.setVisible(true);
            panelsByLanguage.put(locale, panel);
        }
        return panel;
    }

}
