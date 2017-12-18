/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2018 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.jahia.ajax.gwt.client.widget.content;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.extjs.gxt.ui.client.widget.*;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNodeUsage;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementServiceAsync;
import org.jahia.ajax.gwt.client.util.icons.StandardIconsProvider;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.LinkerSelectionContext;
import org.jahia.ajax.gwt.client.widget.edit.EditLinker;
import org.jahia.ajax.gwt.client.widget.edit.sidepanel.SidePanelTabItem;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.data.LoadEvent;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.LoadListener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.TextArea;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;

public class DeleteItemWindow extends Window {
	private final int windowHeight = 500;

	private final int windowWidth = 650;
	
	final Grid<GWTJahiaNodeUsage> usagesGrid;
	final FormPanel formPanel;

	public DeleteItemWindow(final Linker linker, final LinkerSelectionContext lh, final boolean permanentlyDelete) {
		super();

		setSize(windowWidth, windowHeight);
		setHeadingHtml("Informations");
		setResizable(false);
		
		/* Information message */
		formPanel = new FormPanel();
		formPanel.setHeadingHtml(Messages.get("label.information"));
		formPanel.setHeaderVisible(false);
		formPanel.setBorders(false);
		formPanel.setId("JahiaGxtDeleteItem");
	    formPanel.setLayout(new RowLayout(Orientation.VERTICAL));
	    formPanel.setSize(windowWidth, windowHeight);
	    
	    final Html textMessage = new Html();
	    formPanel.add(textMessage);	    
	    
		/* Usages grid */
		final List<GWTJahiaNode> selectedNodeList = new ArrayList<GWTJahiaNode>();
		final List<String> selectedPathList = new ArrayList<String>();
		for (GWTJahiaNode node : lh.getMultipleSelection()) {
			selectedNodeList.add(node);
			selectedPathList.add(node.getPath());
		}
		
		// use this content panel to get vertical scrollbar only on the grid
		final ContentPanel cp = new ContentPanel();
		cp.setLayout(new FitLayout());
		cp.setBorders(false);
		cp.setHeaderVisible(false);
		usagesGrid = NodeUsagesGrid.createUsageGrid(selectedNodeList);
		usagesGrid.setSize(windowWidth, 200);
		cp.add(usagesGrid);		
		formPanel.add(cp);

        /* Comments textarea */
        final TextArea textArea = new TextArea();
        textArea.setSize(windowWidth - 30, 100);
	    if (!permanentlyDelete) {
	        formPanel.add(new Html("<br />" + Messages.get("label.comment", "Comment") + ": <br />"));
	    	formPanel.add(textArea);
	    }
	    
		// listener on the grid because the message depends on the number of usages found, and we get this at the very end
		final int nbSelectedNodes = selectedNodeList.size();
	    usagesGrid.getStore().getLoader().addLoadListener(new LoadListener() {
			@Override
            public void loaderLoad(LoadEvent le) {
				List<GWTJahiaNode> data = le.getData();
				int nbRows = data.size();
				String strMessage = getConfirmationMessage(lh, nbSelectedNodes);
				if (nbRows > 0) {
					strMessage += "<br /><br />" + (nbSelectedNodes > 1 ? Messages.get("message.remove.multiple.usage", "Those nodes are still used in:") : Messages.get("message.remove.single.usage", "This node is still used by:"));
				} else {
					// no empty grid if no usages
					formPanel.remove(cp);
                    textArea.setSize(""+(windowWidth - 30), "70%");
                    
                    if (permanentlyDelete) {
                    	setHeight(130);
                    	formPanel.setHeight(130);
                    }
				}
				textMessage.setHtml(strMessage);
				formPanel.layout();
            }
		});

		if (permanentlyDelete) {
			String permanentDeletionMessage = Messages.get("message.remove.warning",
					"<br/><span style=\"font-style:bold;color:red;\">Warning: this will erase the content definitively from the repository<br/>So it will not be displayed anymore anywere</span>");
			formPanel.add(new Html(permanentDeletionMessage));
			setIcon(StandardIconsProvider.STANDARD_ICONS.warning());
		}
		
		/* Buttons */
		Button submit = new Button(Messages.get("label.yes"), new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent event) {
                hide();
                linker.loading(Messages.get("label.executing"));
				final JahiaContentManagementServiceAsync async = JahiaContentManagementService.App.getInstance();

				BaseAsyncCallback<Object> baseAsyncCallback = new BaseAsyncCallback<Object>() {
					public void onApplicationFailure(Throwable throwable) {
                        linker.loaded();
						Log.error(throwable.getMessage(), throwable);
						MessageBox.alert(Messages.get("label.error", "Error"), throwable.getMessage(), null);
					}

					public void onSuccess(Object o) {
                        linker.loaded();
						EditLinker el = null;
						if (linker instanceof SidePanelTabItem.SidePanelLinker) {
							el = ((SidePanelTabItem.SidePanelLinker) linker).getEditLinker();
						} else if (linker instanceof EditLinker) {
							el = (EditLinker) linker;
						}
                        Map<String, Object> data = new HashMap<String, Object>();
                        if (el != null && selectedPathList.contains(el.getSelectionContext().getMainNode().getPath())) {
                            data.put("node", el.getSelectionContext().getMainNode());
                        } else {
                            data.put(Linker.REFRESH_ALL, true);
                        }
                        linker.refresh(data);
                        linker.select(null);
					}
				};
				if (permanentlyDelete) {
					async.deletePaths(selectedPathList, baseAsyncCallback);
				} else {
					async.markForDeletion(selectedPathList, textArea.getValue(), baseAsyncCallback);
				}
			}
		});

		Button cancel = new Button(Messages.get("label.no"), new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent event) {
				hide();
			}
		});
	    		
        ButtonBar buttons = new ButtonBar() ;
        buttons.setAlignment(HorizontalAlignment.CENTER);
		buttons.add(submit);
		buttons.add(cancel);
		
        formPanel.add(buttons);        
        setBottomComponent(buttons);
        
		add(formPanel);
	}

	private boolean isPageDeleted(LinkerSelectionContext lh) {
		if (lh.getMultipleSelection().get(0).getNodeTypes().contains("jnt:page")) {
			return true;
		} else {
			return false;
		}
	}
	
	private String getConfirmationMessage(LinkerSelectionContext lh, int nbSelectedNodes) {
		String message = "";
		if (nbSelectedNodes > 1) {
			message = Messages.getWithArgs("message.remove.multiple.confirm", "Do you really want to remove the {0} selected resources?", new String[] { String.valueOf(nbSelectedNodes) });
		} else {
			if (isPageDeleted(lh)) {
				message = Messages.getWithArgs("message.remove.single.page.confirm", "Do you really want to remove the selected PAGE {0}?", new String[] { lh.getSingleSelection().getName() });
				// icon = "ext-mb-delete-page";
			} else {
				message = Messages.getWithArgs("message.remove.single.confirm", "Do you really want to remove the selected resource {0}?", new String[] { lh.getSingleSelection().getName() });
			}
		}
		return message;
	}
}
