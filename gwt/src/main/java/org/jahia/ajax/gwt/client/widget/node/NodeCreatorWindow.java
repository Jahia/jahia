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
package org.jahia.ajax.gwt.client.widget.node;

import com.extjs.gxt.ui.client.widget.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jahia.ajax.gwt.client.widget.tripanel.BrowserLinker;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.service.definition.ContentDefinitionServiceAsync;
import org.jahia.ajax.gwt.client.service.definition.ContentDefinitionService;
import org.jahia.ajax.gwt.client.widget.definition.PropertiesEditor;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeType;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Nov 25, 2008
 * Time: 7:13:29 PM
 * To change this template use File | Settings | File Templates.
 */
public class NodeCreatorWindow extends Window {

    public NodeCreatorWindow(final BrowserLinker linker, final GWTJahiaNode parent, List<String> names) {
        super() ;
        setHeading("Mount");
        setSize(800, 500);
        setResizable(true);

        ContentDefinitionServiceAsync service = ContentDefinitionService.App.getInstance();
        service.getNodeTypes(names, new AsyncCallback<List<GWTJahiaNodeType>>() {
            public void onFailure(Throwable caught) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            public void onSuccess(List<GWTJahiaNodeType> result) {
                PropertiesEditor pe = new PropertiesEditor(result,false, false);
                add(pe);
                layout();
            }

        });
        show();

    }

}

