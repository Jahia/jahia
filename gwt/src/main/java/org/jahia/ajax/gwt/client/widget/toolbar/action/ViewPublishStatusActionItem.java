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

package org.jahia.ajax.gwt.client.widget.toolbar.action;

import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Listener;
import org.jahia.ajax.gwt.client.data.publication.GWTJahiaPublicationInfo;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.widget.edit.mainarea.Module;

import java.util.List;

/**
 * 
 * User: toto
 * Date: Sep 25, 2009
 * Time: 6:59:01 PM
 * 
 */
public class ViewPublishStatusActionItem extends ViewStatusActionItem {

    @Override
    public void viewStatus(List<Module> moduleList) {
        Listener<ComponentEvent> removeListener = createRemoveListener();

        String lastUnpublished = null;
        boolean allPublished = true;
        for (Module module : moduleList) {
            if (module.getNode() != null) {
                GWTJahiaPublicationInfo info = module.getNode().getAggregatedPublicationInfo();
                if (info.getStatus() != GWTJahiaPublicationInfo.PUBLISHED) {
                    allPublished = false;
                    if (lastUnpublished != null && module.getNode().getPath().startsWith(lastUnpublished)) {
                        continue;
                    }

                    final String label = GWTJahiaPublicationInfo.statusToLabel.get(info.getStatus());
                    String status = Messages.get("label.publication." + label, label);

                    if (info.isLocked()) {
                        infoLayers.addInfoLayer(module, Messages.get("label.publication.locked", "locked"), "orange", "orange", removeListener, true,
                                "0.7");
                    } else if (info.getStatus() == GWTJahiaPublicationInfo.NOT_PUBLISHED || info.getStatus() == GWTJahiaPublicationInfo.UNPUBLISHED) {
                        lastUnpublished = module.getNode().getPath();
                        if (info.getStatus() == GWTJahiaPublicationInfo.UNPUBLISHED) {
                            infoLayers.addInfoLayer(module, status, "black", "black", removeListener, false,
                                    "0.7");
                        } else {
                            infoLayers.addInfoLayer(module, status, "black", "black", removeListener, false,
                                    "0.7");
                        }
                    } else if (info.getStatus() == GWTJahiaPublicationInfo.MODIFIED) {
                        infoLayers.addInfoLayer(module, status, "red", "red", removeListener, true,
                                "0.7");
                    } else if (info.getStatus() == GWTJahiaPublicationInfo.LIVE_MODIFIED) {
                        infoLayers.addInfoLayer(module, status, "blue", "blue", removeListener, true,
                                "0.7");
                    } else if (info.getStatus() == GWTJahiaPublicationInfo.CONFLICT) {
                        infoLayers.addInfoLayer(module, status, "red", "red", removeListener, true,
                                "0.7");
                    } else if (info.getStatus() == GWTJahiaPublicationInfo.MANDATORY_LANGUAGE_UNPUBLISHABLE) {
                        infoLayers.addInfoLayer(module, status, "red", "red", removeListener, true,
                                "0.7");
                    } else if (info.getStatus() == GWTJahiaPublicationInfo.MANDATORY_LANGUAGE_VALID) {
                        infoLayers.addInfoLayer(module, status, "red", "red", removeListener, true,
                                "0.7");
                    }
                }
            }
        }

        if (allPublished) {
            infoLayers.addInfoLayer(moduleList.iterator().next(), "Everything published", "black", "white",
                    removeListener, false,
                    "0.7");
        }

    }

}
