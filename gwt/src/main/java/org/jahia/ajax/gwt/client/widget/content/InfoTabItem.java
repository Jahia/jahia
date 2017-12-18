/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2018 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.jahia.ajax.gwt.client.widget.content;

import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.util.Formatter;
import org.jahia.ajax.gwt.client.util.URL;
import org.jahia.ajax.gwt.client.widget.AsyncTabItem;
import org.jahia.ajax.gwt.client.widget.contentengine.EditEngineTabItem;
import org.jahia.ajax.gwt.client.widget.contentengine.NodeHolder;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 
 * User: toto
 * Date: May 10, 2010
 * Time: 7:39:18 PM
 * 
 */
public class InfoTabItem extends EditEngineTabItem {
    private transient FlowPanel infoPanel;


    public void init(NodeHolder engine, AsyncTabItem tab, String locale) {
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
                    g.setWidget(0, 0, new Image(URL.appendTimestamp(preview)));
                }
                String name = selectedNode.getName();
                if (name != null) {
                    flowPanel.add(new HTML("<b>" + Messages.get("label.name") + ":</b> " + SafeHtmlUtils.htmlEscape(name)));
                }
                String path = selectedNode.getPath();
                if (path != null) {
                    flowPanel.add(new HTML("<b>" + Messages.get("label.path") + ":</b> " + SafeHtmlUtils.htmlEscape(path)));
                }
                String id = selectedNode.getUUID();
                if (id != null) {
                    flowPanel.add(new HTML("<b>" + Messages.get("label.id", "ID") + ":</b> " + id));
                }
                if (selectedNode.isFile() != null &&selectedNode.isFile()) {
                    Long s = selectedNode.getSize();
                    if (s != null) {
                        flowPanel.add(new HTML("<b>" + Messages.get("label.size") + ":</b> " +
                                Formatter.getFormattedSize(s) + " (" + s.toString() + " bytes)"));
                    }
                }
                Date date = selectedNode.get("jcr:lastModified");
                if (date != null) {
                    flowPanel.add(new HTML("<b>" + Messages.get("label.lastModif") + ":</b> " +
                            org.jahia.ajax.gwt.client.util.Formatter.getFormattedDate(date, "d/MM/y")));
                }
                if (selectedNode.isLocked() != null && selectedNode.isLocked() && selectedNode.getLockInfos() != null) {
                    StringBuilder infos = new StringBuilder();
                    if (selectedNode.getLockInfos().containsKey(null) && selectedNode.getLockInfos().size() == 1) {
                        for (String s : selectedNode.getLockInfos().get(null)) {
                            infos.append(Formatter.getLockLabel(s));
                        }
                    } else {
                        for (Map.Entry<String, List<String>> entry : selectedNode.getLockInfos().entrySet()) {
                            if (entry.getKey() != null) {
                                if (infos.length() > 0) {
                                    infos.append("; ");
                                }
                                infos.append(entry.getKey()).append(" : ");
                                int i = 0;
                                for (String s : entry.getValue()) {
                                    if (i > 0) {
                                        infos.append(", ");
                                    }
                                    infos.append(Formatter.getLockLabel(s));
                                    i++;
                                }
                            }
                        }
                    }
                    flowPanel.add(new HTML(
                            "<b>" + Messages.get("info.lock.label") + ":</b> " + infos.toString()));
                }

                flowPanel.add(new HTML("<b>" + Messages.get("nodes.label", "Types") + ":</b> " + selectedNode.getNodeTypes()));
                flowPanel.add(new HTML("<b>" + Messages.get("org.jahia.jcr.edit.tags.tab", "Tags") + ":</b> " + (selectedNode.getTags() != null ? selectedNode.getTags() : "")));
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

    @Override
    public boolean isHandleCreate() {
        return false;
    }
}
