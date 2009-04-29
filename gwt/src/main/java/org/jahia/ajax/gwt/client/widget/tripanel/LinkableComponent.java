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
package org.jahia.ajax.gwt.client.widget.tripanel;

import org.jahia.ajax.gwt.client.widget.tripanel.BrowserLinker;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.menu.Menu;

/**
 * This class represents a part of the universal tree browser. Its purpose is to define
 * linkage mechanism in order to have interaction between components.
 *
 * @author rfelden
 * @version 19 juin 2008 - 14:37:33
 */
public abstract class LinkableComponent {

    /**
     * This is the link target, it deals with communication between components.
     */
    private BrowserLinker m_linker ;

    /**
     * Set the linker, should be used by the linker itself in order lay a callback in each component.
     * @param linker the linker
     */
    public void initWithLinker(BrowserLinker linker) {
        m_linker = linker ;
    }

    /**
     * Get the linker shared by every component.
     * @return the linker
     */
    public BrowserLinker getLinker() {
        return m_linker ;
    }

    /**
     * Get the UI component used by the subclass since it is not directly a subclass of a widget
     * (multiple inheritance is not supported in Java, damn).
     * @return the ui component
     */
    public abstract Component getComponent() ;

    /**
     * Initialize the context m�enu if defined into subclass. If not, do nothing.
     */
    public void initContextMenu() {}

    public void setContextMenu(Menu menu) {}

}
