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
package org.jahia.ajax.gwt.client.widget.pagepicker;

import org.jahia.ajax.gwt.client.widget.tripanel.MyStatusBar;
import org.jahia.ajax.gwt.client.widget.tripanel.BottomBar;
import com.extjs.gxt.ui.client.widget.Component;

/**
 * Created by IntelliJ IDEA.
 * User: rfelden
 * Date: 3 sept. 2008
 * Time: 14:34:52
 * To change this template use File | Settings | File Templates.
 */
public class PageStatusBar extends BottomBar {
    
    private MyStatusBar m_component ;

    public PageStatusBar() {
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
