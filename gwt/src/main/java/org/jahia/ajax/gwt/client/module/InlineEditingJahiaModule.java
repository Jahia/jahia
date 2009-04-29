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
package org.jahia.ajax.gwt.client.module;

import org.jahia.ajax.gwt.client.core.JahiaModule;
import org.jahia.ajax.gwt.client.core.JahiaType;
import org.jahia.ajax.gwt.client.widget.inlineediting.InlineEditing;
import org.jahia.ajax.gwt.client.data.config.GWTJahiaPageContext;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.DOM;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: loom
 * Date: Dec 18, 2008
 * Time: 4:25:29 PM
 * To change this template use File | Settings | File Templates.
 */
public class InlineEditingJahiaModule extends JahiaModule {
    public void onModuleLoad(GWTJahiaPageContext page, List<RootPanel> rootPanels) {

        for (RootPanel inlineEditingPane : rootPanels) {
            String containerID = DOM.getElementAttribute(inlineEditingPane.getElement(), "containerID");
            String fieldID = DOM.getElementAttribute(inlineEditingPane.getElement(), "fieldID");
            inlineEditingPane.add(new InlineEditing(page, containerID, fieldID, inlineEditingPane.getElement()));
        }

    }

    public String getJahiaModuleType() {
        return JahiaType.INLINE_EDITING;  //To change body of implemented methods use File | Settings | File Templates.
    }

}
