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
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
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