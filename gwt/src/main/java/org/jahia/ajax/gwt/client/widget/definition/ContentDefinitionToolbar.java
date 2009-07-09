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
package org.jahia.ajax.gwt.client.widget.definition;

import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.toolbar.FillToolItem;
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

    private Button copy ;
    private Button cut ;
    private Button paste ;

    public ContentDefinitionToolbar(final FormView manager) {
        m_component = new ToolBar() ;
        m_component.setHeight(28);
        copy = new Button() ;
        cut = new Button() ;
        paste = new Button() ;

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
