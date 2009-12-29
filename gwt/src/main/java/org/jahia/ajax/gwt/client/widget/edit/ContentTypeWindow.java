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
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 *
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.ajax.gwt.client.widget.edit;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.event.*;
import com.extjs.gxt.ui.client.widget.*;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.button.ButtonBar;
import com.extjs.gxt.ui.client.widget.layout.FillLayout;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeType;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementServiceAsync;
import org.jahia.ajax.gwt.client.util.icons.ContentModelIconProvider;
import org.jahia.ajax.gwt.client.widget.Linker;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 *
 * @author : rincevent
 * @since : JAHIA 6.1
 *        Created : 12 nov. 2009
 */
public class ContentTypeWindow extends Window {
    private String baseType;
    private GWTJahiaNode parentNode;
    private final Linker linker;
    private ButtonBar buttonBar;
    private Button ok;
    private Button cancel;
    private ContentTypeTree contentTypeTree;


    public ContentTypeWindow(Linker linker, GWTJahiaNode parent, GWTJahiaNodeType nodeType, boolean displaySchmurtz) {
        this.linker = linker;
        if (nodeType != null) {
            this.baseType = nodeType.getName();
        } else {
            baseType = null;
        }
        if (parent != null) {
            this.parentNode = parent;
        } else {
            this.parentNode = linker.getMainNode();
        }
        setLayout(new FillLayout());
        setBodyBorder(false);
        setSize(950, 750);
        setClosable(true);
        setResizable(true);
        setModal(true);
        setMaximizable(true);
        setIcon(ContentModelIconProvider.CONTENT_ICONS.engineLogoJahia());
        contentTypeTree = new ContentTypeTree(linker, nodeType, baseType, parentNode,
                                                                    displaySchmurtz,true,695,500,25, true,this);
        add(contentTypeTree);
        contentTypeTree.layout(true);
        layout();
        initFooter();
        setFooter(true);
        ok.setEnabled(true);
    }

    /**
     * init buttons
     */
    private void initFooter() {
        LayoutContainer buttonsPanel = new LayoutContainer();
        buttonsPanel.setBorders(false);
        final Window window = this;
        buttonBar = new ButtonBar();
        buttonBar.setAlignment(Style.HorizontalAlignment.CENTER);

        ok = new Button(Messages.getResource("fm_ok"));
        ok.setHeight(EditContentEngine.BUTTON_HEIGHT);
        ok.setEnabled(false);
        ok.setIcon(ContentModelIconProvider.CONTENT_ICONS.engineButtonOK());
        ok.addSelectionListener(new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent buttonEvent) {
                final ContentTypeModelData contentTypeModelData = contentTypeTree.getTreeGrid().getSelectionModel().getSelectedItem();
                if (contentTypeModelData != null) {
                    final GWTJahiaNodeType gwtJahiaNodeType = contentTypeModelData.getGwtJahiaNodeType();
                    final GWTJahiaNode gwtJahiaNode = contentTypeModelData.getGwtJahiaNode();
                    if (gwtJahiaNodeType != null) {
                        new EditContentEngine(linker, parentNode, gwtJahiaNodeType, null, false).show();
                        window.hide();
                    } else if (gwtJahiaNode != null) {
                        final JahiaContentManagementServiceAsync instance = JahiaContentManagementService.App.getInstance();
                        instance.getNode(gwtJahiaNode.getPath() + "/j:target", new AsyncCallback<GWTJahiaNode>() {
                            public void onFailure(Throwable caught) {
                                MessageBox.alert("Alert",
                                                 "Unable to copy schmurtz to destination. Cause: " + caught.getLocalizedMessage(),
                                                 null);
                            }

                            public void onSuccess(GWTJahiaNode result) {
                                List<GWTJahiaNode> nodes = new ArrayList<GWTJahiaNode>(1);
                                result.setName(gwtJahiaNode.getName());
                                nodes.add(result);
                                instance.paste(nodes, parentNode.getPath(), false, new AsyncCallback() {
                                    public void onFailure(Throwable caught) {
                                        //To change body of implemented methods use File | Settings | File Templates.
                                    }

                                    public void onSuccess(Object result) {
                                        hide();
                                        linker.refreshMainComponent();
                                        if (baseType.equals("jnt:page")) {
                                            linker.refreshLeftPanel();
                                        }
                                    }
                                });
                            }
                        });
                    }
                }
            }
        });


        buttonBar.add(ok);

        cancel = new Button(Messages.getResource("fm_cancel"));
        cancel.setHeight(EditContentEngine.BUTTON_HEIGHT);
        cancel.setIcon(ContentModelIconProvider.CONTENT_ICONS.engineButtonCancel());
        cancel.addSelectionListener(new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent buttonEvent) {
                window.hide();
            }
        });
        buttonBar.add(cancel);
        buttonsPanel.add(buttonBar);

        // copyrigths
        Text copyright = new Text(Messages.getResource("fm_copyright"));
        ButtonBar container = new ButtonBar();
        container.setAlignment(Style.HorizontalAlignment.CENTER);
        container.add(copyright);
        buttonsPanel.add(container);
        setBottomComponent(buttonsPanel);


    }


}
