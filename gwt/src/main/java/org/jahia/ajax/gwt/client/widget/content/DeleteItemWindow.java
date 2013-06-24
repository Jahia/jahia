package org.jahia.ajax.gwt.client.widget.content;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNodeUsage;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementServiceAsync;
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
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.Text;
import com.extjs.gxt.ui.client.widget.Window;
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
		setHeading("Informations");
		setResizable(false);
		
		/* Information message */
		formPanel = new FormPanel();
		formPanel.setHeading(Messages.get("label.information"));
		formPanel.setHeaderVisible(false);
		formPanel.setBorders(false);
		formPanel.setId("JahiaGxtDeleteItem");
	    formPanel.setLayout(new RowLayout(Orientation.VERTICAL));
	    formPanel.setSize(windowWidth, windowHeight);
	    
	    final Text textMessage = new Text();
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
				}
				textMessage.setText(strMessage);  
				formPanel.layout();
            }
		});	
	    
		/* Comments textarea */
		final TextArea textArea = new TextArea();
		textArea.setSize((windowWidth - 30), 100);

		/* Buttons */
		Button submit = new Button(Messages.get("label.yes"), new SelectionListener<ButtonEvent>() {
			public void componentSelected(ButtonEvent event) {
				final JahiaContentManagementServiceAsync async = JahiaContentManagementService.App.getInstance();

				BaseAsyncCallback<Object> baseAsyncCallback = new BaseAsyncCallback<Object>() {
					public void onApplicationFailure(Throwable throwable) {
						Log.error(throwable.getMessage(), throwable);
						MessageBox.alert(Messages.get("label.error", "Error"), throwable.getMessage(), null);
					}

					public void onSuccess(Object o) {
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
						hide();
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
	    
	    formPanel.add(new Text("<br />" + Messages.get("label.comment", "Comment") + ": <br />"));	    
		formPanel.add(textArea);
		
        ButtonBar buttons = new ButtonBar() ;
        buttons.setAlignment(HorizontalAlignment.CENTER);
		buttons.add(submit);
		buttons.add(cancel);
		
        formPanel.add(buttons);        
        setBottomComponent(buttons);
        
		add(formPanel);
	}

	private String getConfirmationMessage(LinkerSelectionContext lh, int nbSelectedNodes) {
		String message = "";
		if (nbSelectedNodes > 1) {
			message = Messages.getWithArgs("message.remove.multiple.confirm", "Do you really want to remove the {0} selected resources?", new String[] { String.valueOf(nbSelectedNodes) });
		} else {
			if (lh.getMultipleSelection().get(0).getNodeTypes().contains("jnt:page")) {
				message = Messages.getWithArgs("message.remove.single.page.confirm", "Do you really want to remove the selected PAGE {0}?", new String[] { lh.getSingleSelection().getName() });
				// icon = "ext-mb-delete-page";
			} else {
				message = Messages.getWithArgs("message.remove.single.confirm", "Do you really want to remove the selected resource {0}?", new String[] { lh.getSingleSelection().getName() });
			}
		}
		return message;
	}
}
