/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2010 Jahia Solutions Group SA. All rights reserved.
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

package org.jahia.ajax.gwt.client.widget.edit.mainarea;

import com.allen_sauer.gwt.log.client.Log;
import com.extjs.gxt.ui.client.dnd.DragSource;
import com.extjs.gxt.ui.client.dnd.DropTarget;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.DNDEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.Header;
import com.extjs.gxt.ui.client.widget.tips.ToolTipConfig;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.HTML;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.widget.edit.EditModeDNDListener;
import org.jahia.ajax.gwt.client.widget.contentengine.EditContentEnginePopupListener;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Aug 19, 2009
 * Time: 12:25:19 PM
 * To change this template use File | Settings | File Templates.
 */
public class SimpleModule extends Module {
    protected boolean hasDragDrop = true;

    public SimpleModule(String id, String path, String template, String scriptInfo, String nodeTypes, String referenceType,
                        MainModule mainModule) {
        super(id, path, template, scriptInfo, nodeTypes, referenceType, mainModule);
    }

    public SimpleModule(String id, final String path, String s, String template, String scriptInfo, String nodeTypes,
                        String referenceType, final MainModule mainModule, boolean header) {
        super(id, path, template, scriptInfo, nodeTypes, referenceType, mainModule);

        if (header) {
            head = new Header();
            add(head);
            setHeaderText(Messages.get("label.content") + " : " + path.substring(path.lastIndexOf('/') + 1));
            head.addStyleName("x-panel-header");
            head.addStyleName("x-panel-header-simplemodule");
            setBorders(false);
        }

        html = new HTML(s);
        add(html);
    }

    public void onParsed() {
        Log.debug("Add drag source for simple module " + path);

        if (hasDragDrop) {
            DragSource source = new SimpleModuleDragSource(this);
            source.addDNDListener(mainModule.getEditLinker().getDndListener());
            DropTarget target = new ModuleDropTarget(this, EditModeDNDListener.SIMPLEMODULE_TYPE);
            target.setAllowSelfAsSource(true);
            target.addDNDListener(mainModule.getEditLinker().getDndListener());
        } else {
            new DropTarget(this) {
                @Override
                protected void onDragEnter(DNDEvent event) {
                    event.getStatus().setStatus(false);
                }
            };

        }

        sinkEvents(Event.ONCLICK + Event.ONDBLCLICK + Event.ONMOUSEOVER + Event.ONMOUSEOUT + Event.ONCONTEXTMENU);

        Listener<ComponentEvent> listener = new Listener<ComponentEvent>() {
            public void handleEvent(ComponentEvent ce) {
                if (selectable) {
                    Log.info("click" + path + " : " + scriptInfo);
                    mainModule.getEditLinker().onModuleSelection(SimpleModule.this);
                }
            }
        };
        addListener(Events.OnClick, listener);
        addListener(Events.OnContextMenu, listener);
        addListener(Events.OnDoubleClick, new EditContentEnginePopupListener(this, mainModule.getEditLinker()));

        Listener<ComponentEvent> hoverListener = new Listener<ComponentEvent>() {
            public void handleEvent(ComponentEvent ce) {
                Hover.getInstance().addHover(SimpleModule.this);
            }
        };
        Listener<ComponentEvent> outListener = new Listener<ComponentEvent>() {
            public void handleEvent(ComponentEvent ce) {
                Hover.getInstance().removeHover(SimpleModule.this);
            }
        };

        addListener(Events.OnMouseOver, hoverListener);
        addListener(Events.OnMouseOut, outListener);
    }

    public void setNode(GWTJahiaNode node) {
        super.setNode(node);
        if (node.isShared()) {
            this.setToolTip(new ToolTipConfig(Messages.get("info_important", "Important"), Messages.get("info_sharednode", "This is a shared node")));
        }
    }
}
