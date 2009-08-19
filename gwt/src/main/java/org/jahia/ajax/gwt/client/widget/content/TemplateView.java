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
package org.jahia.ajax.gwt.client.widget.content;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.dnd.DropTarget;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.Window;
import com.google.gwt.core.client.GWT;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.util.content.actions.ManagerConfiguration;
import org.jahia.ajax.gwt.client.util.templates.TemplatesDOMUtil;
import org.jahia.ajax.gwt.client.widget.tripanel.TopRightComponent;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;

import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 *
 * @author rfelden
 * @version 20 juin 2008 - 09:53:08
 */
public class TemplateView extends TopRightComponent {

    private LayoutContainer m_component;
    private HTML html;

    public TemplateView(final ManagerConfiguration config) {
        m_component = new LayoutContainer(new FitLayout());
        m_component.setBorders(false);

    }

    public void setContent(final Object root) {
        clearTable();
        if (root != null) {
            JahiaContentManagementService.App.getInstance().getRenderedContent(((GWTJahiaNode) root).getPath(), null, false, new AsyncCallback<String>() {
                public void onSuccess(String result) {
                    html = new HTML(result);
                    m_component.add(html);
                    m_component.layout();
                    Map<String, List<RootPanel>> jahiaTypedRootPanels = TemplatesDOMUtil.getAllJahiaTypedRootPanels(html.getElement());
                    
//                    for (String jahiaType : jahiaTypedRootPanels.keySet()) {
//                        try {
//                            if ("placeHolder".equals(jahiaType)) {
//                                List<RootPanel> rootPanelsForType = jahiaTypedRootPanels.get(jahiaType);
//                                for (RootPanel rootPanel : rootPanelsForType) {
//                                    final Button w = new Button("drop here");
//                                    rootPanel.add(w);
//
//                                    DropTarget target = new DropTarget(w);
//                                    target.addDNDListener(getLinker().getDndListener());
//                                }
//                            }
//                        } catch (Exception e) {
//                            GWT.log("Unable to load jahia module [" + jahiaType + "].", e);
//                        }
//                    }

                }

                public void onFailure(Throwable caught) {
                    Log.error("", caught);
                    Window.alert("-->"+caught.getMessage());
                }
            });
        }
    }

    public void setProcessedContent(Object content) {
        clearTable();
    }

    public void clearTable() {
        m_component.removeAll();
    }

    public Object getSelection() {
        return null;
    }

    public void refresh() {
        //m_table.getView().refresh(true);
        setContent(getLinker().getTreeSelection());
    }

    public Component getComponent() {
        return m_component;
    }

}