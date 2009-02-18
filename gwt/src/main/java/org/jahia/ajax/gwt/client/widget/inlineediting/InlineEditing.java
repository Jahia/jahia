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

package org.jahia.ajax.gwt.client.widget.inlineediting;

import org.jahia.ajax.gwt.client.data.config.GWTJahiaPageContext;
import org.jahia.ajax.gwt.client.data.GWTJahiaInlineEditingResultBean;
import org.jahia.ajax.gwt.client.service.JahiaService;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Widget;
import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.widget.Info;

/**
 * Created by IntelliJ IDEA.
 * User: loom
 * Date: Dec 18, 2008
 * Time: 4:28:05 PM
 * To change this template use File | Settings | File Templates.
 */
public class InlineEditing extends FocusPanel implements ClickListener {

    private int containerID;
    private int fieldID;

    public InlineEditing(GWTJahiaPageContext page, String containerID, String fieldID, Element element) {
        this.containerID = Integer.parseInt(containerID);
        this.fieldID = Integer.parseInt(fieldID);
        setElement(element);
        sinkEvents(Event.ONCLICK);
        sinkEvents(Event.ONBLUR);
        addClickListener(this);
    }

    public void onClick(Widget sender) {
        Window.alert("Browser event called.");
    }

    @Override
    public void onBrowserEvent(Event event) {
        Window.alert("Browser event called.");
        super.onBrowserEvent(event);    //To change body of overridden methods use File | Settings | File Templates.
        if (event.getTypeInt() == Event.ONCLICK) {
            JahiaService.App.getInstance().isInlineEditingAllowed(containerID, fieldID, new AsyncCallback<Boolean>() {
                public void onSuccess(Boolean result) {
                    if (result.booleanValue()) {
                        DOM.setElementAttribute(getElement(), "contentEditable", "true");
                    }
                    Log.info("Inline editing is allowed.");
                }

                public void onFailure(Throwable throwable) {
                    Log.error("Error modifying content", throwable);
                }
            });
        } else if (event.getTypeInt() == Event.ONBLUR) {
            String content = getElement().getInnerHTML();
            JahiaService.App.getInstance().inlineUpdateField(containerID, fieldID, content, new AsyncCallback<GWTJahiaInlineEditingResultBean>() {
                public void onSuccess(GWTJahiaInlineEditingResultBean result) {
                    if (result.isContentModified()) {
                        Info.display("Content updated", "Content saved.");
                        Log.info("Content successfully modified.");
                    }
                }

                public void onFailure(Throwable throwable) {
                    Log.error("Error modifying content", throwable);
                }
            });

        }
    }
}
