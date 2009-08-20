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
package org.jahia.ajax.gwt.client.widget.edit;

import java.util.ArrayList;
import java.util.List;

import org.jahia.ajax.gwt.client.data.node.GWTJahiaGetPropertiesResult;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementServiceAsync;
import org.jahia.ajax.gwt.client.widget.AsyncTabItem;
import org.jahia.ajax.gwt.client.widget.definition.PropertiesEditor;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Info;
import com.extjs.gxt.ui.client.widget.TabPanel;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.toolbar.FillToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Content editing widget.
 * 
 * @author Sergiy Shyrkov
 * 
 */
public class EditContentEngine extends Window {
	
	private static JahiaContentManagementServiceAsync contentService = JahiaContentManagementService.App.getInstance();

	private String contentPath;
	
	private ContentPanel contentPanel; 

	private TabPanel tabs;

	private AsyncTabItem contentTab;

	private AsyncTabItem metadataTab;

	private AsyncTabItem rightsTab;

	private AsyncTabItem versionsTab;

	private AsyncTabItem workflowTab;

	/**
	 * Initializes an instance of this class.
	 * 
	 * @param contentObjectPath
	 *            the path of the content object to be edited
	 */
	public EditContentEngine(String contentObjectPath) {
		contentPath = contentObjectPath;
		initWindowProperties();
		initTabs();
	}

	/**
	 * Creates and initializes all window tabs.
	 */
	private void initTabs() {
		contentPanel = new ContentPanel(new FitLayout());
		contentPanel.setBodyBorder(false);
		contentPanel.setBorders(true);

		tabs = new TabPanel();
		tabs.setBodyBorder(false);
		tabs.setBorders(false);

		createContentTab();
		tabs.add(contentTab);

		metadataTab = new AsyncTabItem(Messages.get("ece_metadata", "Metadata"));
		metadataTab.setLayout(new FitLayout());
		tabs.add(metadataTab);

		rightsTab = new AsyncTabItem(Messages.get("ece_rights", "Rights"));
		rightsTab.setLayout(new FitLayout());
		tabs.add(rightsTab);
		
		workflowTab = new AsyncTabItem(Messages.get("ece_workflow", "Workflow"));
		workflowTab.setLayout(new FitLayout());
		tabs.add(workflowTab);

		versionsTab = new AsyncTabItem(Messages.get("ece_versions", "Versions"));
		versionsTab.setLayout(new FitLayout());
		tabs.add(versionsTab);

		add(tabs);
	}

	private AsyncTabItem createContentTab() {
		contentTab = new AsyncTabItem(Messages.get("ece_content", "Content"));
		contentTab.setLayout(new FitLayout());
		
		contentService.getProperties(contentPath, new AsyncCallback<GWTJahiaGetPropertiesResult>() {
            public void onFailure(Throwable throwable) {
                Log.debug("Cannot get properties", throwable);
            }

            public void onSuccess(GWTJahiaGetPropertiesResult result) {
            	GWTJahiaNode selectedNode = result.getNode();
                final List<GWTJahiaNode> elements = new ArrayList<GWTJahiaNode>();
                elements.add(selectedNode);

                List<String> list = new ArrayList<String>();
                list.add("jcr:content");
                list.add("j:thumbnail");
                final PropertiesEditor propertiesEditor = new PropertiesEditor(result.getNodeTypes(), result.getProperties(), false, true, list, null);

                ToolBar toolBar = (ToolBar) propertiesEditor.getTopComponent();
                Button item = new Button(Messages.getResource("fm_save"));
                item.setIconStyle("gwt-icons-save");
                item.setEnabled(selectedNode.isWriteable() && !selectedNode.isLocked());
                item.addSelectionListener(new SelectionListener<ButtonEvent>() {
                    public void componentSelected(ButtonEvent event) {
                        JahiaContentManagementService.App.getInstance().saveProperties(elements, propertiesEditor.getProperties(), new AsyncCallback<Object>() {
                            public void onFailure(Throwable throwable) {
                                com.google.gwt.user.client.Window.alert("Properties save failed\n\n" + throwable.getLocalizedMessage());
                                Log.error("failed", throwable);
                            }

                            public void onSuccess(Object o) {
                                Info.display("", "Properties saved");
                                //getLinker().refreshTable();
                            }
                        });
                    }
                });
                toolBar.add(new FillToolItem());
                toolBar.add(item);
                item = new Button(Messages.getResource("fm_restore"));
                item.setIconStyle("gwt-icons-restore");
                item.setEnabled(selectedNode.isWriteable() && !selectedNode.isLocked());

                item.addSelectionListener(new SelectionListener<ButtonEvent>() {
                    public void componentSelected(ButtonEvent event) {
                        propertiesEditor.resetForm();
                    }
                });
                toolBar.add(item);
                toolBar.setVisible(true);
                contentTab.add(propertiesEditor);

                contentTab.setProcessed(true);
            }
        });
		
	    return contentTab;
    }

	/**
	 * Initializes basic window properties: size, state and title.
	 */
	private void initWindowProperties() {
		setSize(800, 600);
		setClosable(true);
		setResizable(true);
		setModal(true);
		setMaximizable(true);
		setHeading(contentPath);
	}

}
