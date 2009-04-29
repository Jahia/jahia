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
package org.jahia.ajax.gwt.client.widget.definition;

import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.toolbar.FillToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.TextToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import org.jahia.ajax.gwt.client.widget.tripanel.TopBar;

/**
 * Created by IntelliJ IDEA.
 *
 * @author rfelden
 * @version 7 juil. 2008 - 14:17:12
 */
public class ContentDefinitionToolbar extends TopBar {

    private ToolBar m_component ;

    private TextToolItem copy ;
    private TextToolItem cut ;
    private TextToolItem paste ;

    public ContentDefinitionToolbar(final FormView manager) {
        m_component = new ToolBar() ;
        m_component.setHeight(28);
        copy = new TextToolItem() ;
        cut = new TextToolItem() ;
        paste = new TextToolItem() ;

        copy.setIconStyle("copy");
        copy.setText("copy");
        m_component.add(copy) ;
        cut.setIconStyle("cut");
        cut.setText("cut");
        m_component.add(cut) ;
        paste.setIconStyle("paste");
        paste.setText("paste");
        m_component.add(paste) ;

        m_component.add(new FillToolItem());
        m_component.setHeight(21);
    }

    public void handleNewSelection(Object leftTreeSelection, Object topTableSelectionEl) {
    }

    public Component getComponent() {
        return m_component ;
    }
}
