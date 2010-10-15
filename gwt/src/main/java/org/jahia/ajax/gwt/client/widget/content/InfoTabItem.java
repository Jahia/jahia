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

package org.jahia.ajax.gwt.client.widget.content;

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.util.Formatter;
import org.jahia.ajax.gwt.client.widget.contentengine.EditEngineTabItem;
import org.jahia.ajax.gwt.client.widget.contentengine.NodeHolder;

import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: May 10, 2010
 * Time: 7:39:18 PM
 * To change this template use File | Settings | File Templates.
 */
public class InfoTabItem extends EditEngineTabItem {
    private transient FlowPanel infoPanel;


    public void init(String locale) {
        if (!tab.isProcessed()) {
            infoPanel = new FlowPanel();
            infoPanel.addStyleName("infoPane");
            tab.add(infoPanel);

            Grid g = new Grid(1, 2);
            g.setCellSpacing(10);
            FlowPanel flowPanel = new FlowPanel();

            if (!engine.isMultipleSelection()) {
                final GWTJahiaNode selectedNode = engine.getNode();


                String preview = selectedNode.getPreview();
                if (preview != null) {
                    g.setWidget(0, 0, new Image(preview));
                }
                String name = selectedNode.getName();
                if (name != null) {
                    flowPanel.add(new HTML("<b>" + Messages.get("label.name") + ":</b> " + name));
                }
                String path = selectedNode.getPath();
                if (path != null) {
                    flowPanel.add(new HTML("<b>" + Messages.get("label.path") + ":</b> " + path));
                }
                String id = selectedNode.getUUID();
                if (id != null) {
                    flowPanel.add(new HTML("<b>" + Messages.get("label.id", "ID") + ":</b> " + id));
                }
                if (selectedNode.isFile()) {
                    Long s = selectedNode.getSize();
                    if (s != null) {
                        flowPanel.add(new HTML("<b>" + Messages.get("label.size") + ":</b> " +
                                Formatter.getFormattedSize(s.longValue()) + " (" + s.toString() + " bytes)"));
                    }
                }
                Date date = selectedNode.get("jcr:lastModified");
                if (date != null) {
                    flowPanel.add(new HTML("<b>" + Messages.get("label.lastModif") + ":</b> " +
                            org.jahia.ajax.gwt.client.util.Formatter.getFormattedDate(date, "d/MM/y")));
                }
                if (selectedNode.isLocked() && selectedNode.getLockOwner() != null) {
                    flowPanel.add(new HTML(
                            "<b>" + Messages.get("info.lock.label") + ":</b> " + selectedNode.getLockOwner()));
                }

                flowPanel.add(new HTML("<b>" + Messages.get("nodes.label", "Types") + ":</b> " + selectedNode.getNodeTypes()));
                flowPanel.add(new HTML("<b>" + Messages.get("org.jahia.jcr.edit.tags.tab", "Tags") + ":</b> " + selectedNode.getTags() != null ? selectedNode.getTags() : ""));
            } else {
                int numberFiles = 0;
                int numberFolders = 0;
                long size = 0;

                for (GWTJahiaNode selectedNode : engine.getNodes()) {
                    if (selectedNode.isFile()) {
                        numberFiles++;
                        size += selectedNode.getSize();
                    } else {
                        numberFolders++;
                    }
                }
                flowPanel.add(new HTML("<b>" + Messages.get("info.nbFiles.label") + " :</b> " + numberFiles));
                flowPanel.add(new HTML("<b>" + Messages.get("info.nbFolders.label") + " :</b> " + numberFolders));
                flowPanel.add(new HTML("<b>" + Messages.get("info.totalSize.label") + " :</b> " +
                        org.jahia.ajax.gwt.client.util.Formatter.getFormattedSize(size)));
            }
            g.setWidget(0, 1, flowPanel);
            infoPanel.add(g);
            tab.setProcessed(true);
        }
    }

    public boolean isHandleMultipleSelection() {
        return true;
    }
}
