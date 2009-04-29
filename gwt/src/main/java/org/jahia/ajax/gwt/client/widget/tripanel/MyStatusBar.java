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

import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.StatusBar;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.allen_sauer.gwt.log.client.Log;


/**
* Redefinition of class StatusBar to have it extend BoxComponent instead of Component
* (avoid a class cast exception when adding it to a BorderLayout).
*/
public class MyStatusBar extends LayoutContainer {
    
    private StatusBar statusBar ;
    
    public MyStatusBar() {
        super(new FitLayout()) ;
        statusBar = new StatusBar() ;
        add(statusBar) ;
    }

    /**
    * Displays the busy icon.
    */
    public void showBusy() {
        statusBar.showBusy();
    }

    /**
    * Displays the busy icon with the given message.
    *
    * @param message the message
    */
    public void showBusy(String message) {
        try{
            statusBar.showBusy(message);
        } catch (Exception e) {
            Log.debug(e.toString()) ;
        }
    }

    /**
    * Clears the status content.
    */
    public void clear() {
        statusBar.clear();
    }

    /**
    * Sets the status message.
    *
    * @param text the message
    */
    public void setMessage(String text) {
        statusBar.setMessage(text);
    }

    /**
    * Sets the icon style.
    *
    * @param iconStyle the icon style
    */
    public void setIconStyle(String iconStyle) {
        statusBar.setIconStyle(iconStyle);
    }

}