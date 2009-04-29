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

import org.jahia.ajax.gwt.client.widget.tripanel.BottomBar;
import org.jahia.ajax.gwt.client.widget.tripanel.MyStatusBar;
import com.extjs.gxt.ui.client.widget.Component;

/**
 * Created by IntelliJ IDEA.
 *
 * @author rfelden
 * @version 23 juin 2008 - 17:35:03
 */
public class FileStatusBar extends BottomBar {

    private MyStatusBar m_component ;

    public FileStatusBar() {
        m_component = new MyStatusBar() ;
        m_component.setHeight("19px");
    }

    public Component getComponent() {
        return m_component ;
    }

    public void clear() {
        m_component.clear();
    }

    public void setIconStyle(String style) {
        m_component.setIconStyle(style);
    }

    public void setMessage(String info) {
        m_component.setIconStyle(info) ;
    }

    public void showBusy() {
        m_component.showBusy();
    }

    public void showBusy(String message) {
        m_component.showBusy(message);
    }

}
