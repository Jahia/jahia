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
package org.jahia.ajax.gwt.client.module;

import org.jahia.ajax.gwt.client.core.JahiaModule;
import org.jahia.ajax.gwt.client.core.JahiaType;
import org.jahia.ajax.gwt.client.data.config.GWTJahiaPageContext;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.widget.content.wizard.AddContentWizardWindow;
import org.jahia.ajax.gwt.client.widget.content.ContentPicker;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import com.extjs.gxt.ui.client.GXT;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.Text;
import com.extjs.gxt.ui.client.widget.HorizontalPanel;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.layout.CenterLayout;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.allen_sauer.gwt.log.client.Log;

import java.util.List;

/**
 * TODO Comment me
 *
 * @author toto
 */
public class ContentPlaceholderJahiaModule extends JahiaModule {
    public String getJahiaModuleType() {
        return JahiaType.CONTENT_PLACEHOLDER;
    }

    public void onModuleLoad(GWTJahiaPageContext page, List<RootPanel> rootPanels) {
        GXT.init();
        try {
            for (RootPanel actionPane : rootPanels) {
                final String path = DOM.getElementAttribute(actionPane.getElement(), "path");
                final LayoutContainer container = new LayoutContainer();
                container.setLayoutOnChange(true);
                container.setBorders(true);
                container.setSize("100%", "50");
                container.setLayout(new CenterLayout());
                HorizontalPanel h = new HorizontalPanel();
                h.add(new Text(path));


                final Button selectButton = new Button("Select");
                selectButton.addSelectionListener(new SelectionListener<ButtonEvent>() {
                    public void componentSelected(ButtonEvent buttonEvent) {
                        final com.extjs.gxt.ui.client.widget.Window w = new com.extjs.gxt.ui.client.widget.Window();
                        w.setLayout(new FitLayout());
                        final ContentPicker contentPicker = new ContentPicker("/content", "", "", null,null, "complete", false, "");
                        w.setModal(true);
                        w.setSize(800, 600);
                        w.setResizable(true);
                        w.setMaximizable(true);
                        ButtonBar bar = new ButtonBar();
                        Button ok = new Button("OK", new SelectionListener<ButtonEvent>() {
                            public void componentSelected(ButtonEvent event) {
                                List<GWTJahiaNode> selection = (List<GWTJahiaNode>) contentPicker.getLinker().getTableSelection();
                                if (selection != null && selection.size() > 0) {
                                    Window.alert("selection : "+selection.get(0));
                                    JahiaContentManagementService.App.getInstance().pasteReference(selection.get(0),path.substring(0,path.lastIndexOf("/")), path.substring(path.lastIndexOf("/")+1), new AsyncCallback() {
                                        public void onSuccess(Object o) {
                                            Window.alert("Success");
                                        }

                                        public void onFailure(Throwable throwable) {
                                            Log.error("Cannot get node", throwable);
                                        }
                                    });

                                }
                                w.hide();
                            }
                        });
                        bar.add(ok);
                        w.setTopComponent(bar);
                        w.add(contentPicker);
                        w.show();


                    }
                });
                h.add(selectButton);

                final Button createButton = new Button("Create");
                createButton.addSelectionListener(new SelectionListener<ButtonEvent>() {
                    public void componentSelected(ButtonEvent buttonEvent) {
                        JahiaContentManagementService.App.getInstance().getRoot(path.substring(0,path.lastIndexOf("/")), null,null,null,null, new AsyncCallback<List<GWTJahiaNode>>() {
                            public void onSuccess(List<GWTJahiaNode> gwtJahiaNodes) {
                                new AddContentWizardWindow(null, gwtJahiaNodes.iterator().next(), null).show();
                            }

                            public void onFailure(Throwable throwable) {
                                Log.error("Cannot get node", throwable);
                            }
                        });

                    }
                });
                h.add(createButton);
                container.add(h);

                actionPane.add(container);
            }
        } catch (Exception e) {
            Log.error("Error in ContentPlaceholderJahiaModule: " + e.getMessage(), e);
        }
    }

}
