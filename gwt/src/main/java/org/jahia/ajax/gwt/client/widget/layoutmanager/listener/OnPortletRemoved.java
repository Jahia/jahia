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

package org.jahia.ajax.gwt.client.widget.layoutmanager.listener;

import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.Window;
import com.allen_sauer.gwt.log.client.Log;
import org.jahia.ajax.gwt.client.widget.layoutmanager.JahiaPortalManager;
import org.jahia.ajax.gwt.client.widget.layoutmanager.portlet.JahiaPortlet;
import org.jahia.ajax.gwt.client.service.layoutmanager.LayoutmanagerService;
import org.jahia.ajax.gwt.client.core.JahiaPageEntryPoint;

/**
 * User: ktlili
 * Date: 17 nov. 2008
 * Time: 21:35:05
 */
public class OnPortletRemoved  extends SelectionListener<ComponentEvent> {
    private JahiaPortlet jahiaPortlet;

    public OnPortletRemoved(JahiaPortlet jahiaPortlet) {
        this.jahiaPortlet = jahiaPortlet;
    }

    public void componentSelected(ComponentEvent event) {
        final Listener confirmBoxListener = new Listener<ComponentEvent>() {
            public void handleEvent(ComponentEvent ce) {
                close();
            }
        };

        final MessageBox box = new MessageBox();
        box.setButtons(MessageBox.YESNOCANCEL);
        box.setIcon(MessageBox.QUESTION);
        box.setTitle(jahiaPortlet.getHeading());
        box.addCallback(confirmBoxListener);
        box.setMessage("Do you really want to remove: <br/> "+jahiaPortlet.getHeading() +"?");
        box.show();

    }

    public void close() {
        // make a call ajax
        LayoutmanagerService.App.getInstance().removeLayoutItem(JahiaPageEntryPoint.getJahiaGWTPage(), jahiaPortlet.getPorletConfig(), new AsyncCallback() {
            public void onSuccess(Object o) {
                Log.debug("Portlet removed: "+jahiaPortlet.getPorletConfig().getWindowId());
                JahiaPortalManager.getInstance().removeJahiaPortlet(jahiaPortlet);
            }

            public void onFailure(Throwable t) {
                // handle failure
                Window.alert(t.getLocalizedMessage());
            }
        });
    }

}
