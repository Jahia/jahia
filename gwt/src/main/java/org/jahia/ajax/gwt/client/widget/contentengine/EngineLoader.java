/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

package org.jahia.ajax.gwt.client.widget.contentengine;

import com.extjs.gxt.ui.client.GXT;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.RunAsyncCallback;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeProperty;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeType;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.edit.EditLinker;
import org.jahia.ajax.gwt.client.widget.edit.mainarea.Hover;
import org.jahia.ajax.gwt.client.widget.edit.sidepanel.SidePanelTabItem.SidePanelLinker;

import java.util.Map;

/**
 * Engine loader component.
 * User: toto
 * Date: Jul 2, 2010
 * Time: 7:36:04 PM
 */
public class EngineLoader {
    public static final int CREATE = 1;
    public static final int EDIT = 2;

    public static void showEditEngine(final Linker linker, final GWTJahiaNode node) {
        showEngine(EDIT, linker, node, null, null, null, false, false);
    }

    public static void showEditEngine(final Linker linker, final GWTJahiaNode node, boolean forceEngineWindow) {
        showEngine(EDIT, linker, node, null, null, null, false, forceEngineWindow);
    }

    public static void showCreateEngine(final Linker linker, final GWTJahiaNode node, final GWTJahiaNodeType type,
                                        final Map<String, GWTJahiaNodeProperty> props, final String targetName,
                                        final boolean createInParentAndMoveBefore) {
        showEngine(CREATE, linker, node, type, props, targetName, createInParentAndMoveBefore, false);
    }

    private static void showEngine(final int t, final Linker linker, final GWTJahiaNode node,
                                   final GWTJahiaNodeType type, final Map<String, GWTJahiaNodeProperty> props,
                                   final String targetName, final boolean createInParentAndMoveBefore,
                                   final boolean forceEngineWindow) {
        GWT.runAsync(new RunAsyncCallback() {
            public void onFailure(Throwable reason) {

            }

            public void onSuccess() {
                EngineContainer container = createContainer(linker, forceEngineWindow);

                if (t == CREATE) {
                    new CreateContentEngine(linker, node, type, props, targetName, createInParentAndMoveBefore,
                            container);
                } else if (t == EDIT) {
                    final AbstractContentEngine contentEngine = new EditContentEngine(node, linker, container);
                }
                container.showEngine();
                Hover.getInstance().removeAll();
            }
        });


    }

    private static EngineContainer createContainer(Linker linker, boolean forceEngineWindow) {
        EngineContainer container;
        if (!forceEngineWindow && (!(GXT.isIE7 || GXT.isIE6) && (linker instanceof EditLinker || linker instanceof SidePanelLinker))) {
            container = new EnginePanel();
        } else {
            container = new EngineWindow();
        }
        return container;
    }

    public static EngineContainer createContainer(Linker linker) {
        return createContainer(linker, false);
    }
}
