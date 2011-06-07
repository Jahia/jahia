/**
 * This file is part of the Enterprise Jahia software.
 *
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2011 Jahia Solutions Group SA. All rights reserved.
 *
 * This Enteprise Jahia software must be used in accordance with the terms contained in the
 * Jahia Solutions Group Terms & Conditions as well as the
 * Jahia Sustainable Enterprise License (JSEL). You may not use this software except
 * in compliance with the Jahia Solutions Group Terms & Conditions and the JSEL.
 * See the license for the rights, obligations and limitations governing use
 * of the contents of the software. For questions regarding licensing, support, production usage,
 * please contact our team at sales@jahia.com or go to: http://www.jahia.com/license
 */

package org.jahia.ajax.gwt.client.widget.toolbar.action;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.widget.layout.FillLayout;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.util.security.PermissionsUtils;
import org.jahia.ajax.gwt.client.widget.LinkerSelectionContext;
import org.jahia.ajax.gwt.client.widget.edit.EditLinker;
import org.jahia.ajax.gwt.client.widget.form.FormQuickRemotePublication;

/**
 * User: david
 * Date: Apr 26, 2010
 * Time: 4:54:01 PM
 */
public class NewRemotePublicationActionItem extends BaseActionItem  {
    public void onComponentSelection() {
        GWTJahiaNode parent = linker.getSelectionContext().getSingleSelection();
        if (parent != null) {
            com.extjs.gxt.ui.client.widget.Window w = new com.extjs.gxt.ui.client.widget.Window();
            w.setHeading(Messages.get("label.createRemotePublication", "New Remote Publication"));
            w.setModal(true);
            w.setResizable(false);
            w.setBodyBorder(false);
            w.setLayout(new FillLayout());
            w.setWidth(700);
            w.add(new FormQuickRemotePublication() {
                public void onRemotePublicationCreated() {
                    linker.refresh(EditLinker.REFRESH_ALL);
                }
            });
            w.setScrollMode(Style.Scroll.AUTO);
            w.layout();
            w.show();
        }
    }

    public void handleNewLinkerSelection() {
        LinkerSelectionContext lh = linker.getSelectionContext();
        setEnabled(lh.getSingleSelection() != null && PermissionsUtils.isPermitted("jcr:addChildNodes", lh.getSelectionPermissions()));
    }
}
