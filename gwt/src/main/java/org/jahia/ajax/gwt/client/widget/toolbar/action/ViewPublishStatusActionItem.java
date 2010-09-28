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

package org.jahia.ajax.gwt.client.widget.toolbar.action;

import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.util.Rectangle;
import org.jahia.ajax.gwt.client.data.publication.GWTJahiaPublicationInfo;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.widget.Linker;
import org.jahia.ajax.gwt.client.widget.edit.mainarea.Module;
import org.jahia.ajax.gwt.client.widget.publication.PublicationManagerEngine;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Sep 25, 2009
 * Time: 6:59:01 PM
 * To change this template use File | Settings | File Templates.
 */
public class ViewPublishStatusActionItem extends ViewStatusActionItem {

    @Override
    public void viewStatus(List<Module> moduleList, Rectangle rect, final Linker linker) {
        Listener<ComponentEvent> removeListener = new Listener<ComponentEvent>() {
            public void handleEvent(ComponentEvent ce) {
                removeAll();
                if (button != null) {
                    button.toggle(false);
                }
            }
        };

        String lastUnpublished = null;
        boolean allPublished = true;
        for (Module module : moduleList) {
            if (module.getNode() != null) {
                GWTJahiaPublicationInfo info = module.getNode().getPublicationInfo();
                if (info.getStatus() != GWTJahiaPublicationInfo.PUBLISHED) {
                    allPublished = false;
                    if (lastUnpublished != null && module.getNode().getPath().startsWith(lastUnpublished)) {
                        continue;
                    }

                    final String label = PublicationManagerEngine.statusToLabel.get(info.getStatus());
                    String status = Messages.get("label.publication." + label, label);

                    if (info.getStatus() == GWTJahiaPublicationInfo.NOT_PUBLISHED || info.getStatus() == GWTJahiaPublicationInfo.UNPUBLISHED) {
                        lastUnpublished = module.getNode().getPath();
                        if (info.getStatus() == GWTJahiaPublicationInfo.UNPUBLISHED) {
                            addInfoLayer(module, status, "black", "black", null, rect, removeListener, false,
                                    "0.7");
                        } else {
                            addInfoLayer(module, status, "black", "black", null, rect, removeListener, false,
                                    "0.7");
                        }
                    } else if (info.getStatus() == GWTJahiaPublicationInfo.LOCKED) {
                        addInfoLayer(module, status, "red", "red", null, rect, removeListener, true,
                                "0.7");
                    } else if (info.getStatus() == GWTJahiaPublicationInfo.MODIFIED) {
                        addInfoLayer(module, status, "red", "red", null, rect, removeListener, true,
                                "0.7");
                    } else if (info.getStatus() == GWTJahiaPublicationInfo.LIVE_MODIFIED) {
                        addInfoLayer(module, status, "blue", "blue", null, rect, removeListener, true,
                                "0.7");
                    } else if (info.getStatus() == GWTJahiaPublicationInfo.CONFLICT) {
                        addInfoLayer(module, status, "red", "red", null, rect, removeListener, true,
                                "0.7");
                    } else if (info.getStatus() == GWTJahiaPublicationInfo.MANDATORY_LANGUAGE_UNPUBLISHABLE) {
                        addInfoLayer(module, status, "red", "red", null, rect, removeListener, true,
                                "0.7");
                    } else if (info.getStatus() == GWTJahiaPublicationInfo.MANDATORY_LANGUAGE_VALID) {
                        addInfoLayer(module, status, "red", "red", null, rect, removeListener, true,
                                "0.7");
                    }
                }
            }
        }

        if (allPublished) {
            addInfoLayer(moduleList.iterator().next(), "Everything published", "black", "white",
                    null, rect,removeListener, false,
                    "0.7");
        }

    }

}
