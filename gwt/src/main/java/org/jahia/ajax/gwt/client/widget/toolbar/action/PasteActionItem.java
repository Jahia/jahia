/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 * Copyright (C) 2002-2015 Jahia Solutions Group SA. All rights reserved.
 *
 * THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 * 1/GPL OR 2/JSEL
 *
 * 1/ GPL
 * ======================================================================================
 *
 * IF YOU DECIDE TO CHOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 * "This program is free software; you can redistribute it and/or
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
 * describing the FLOSS exception, also available here:
 * http://www.jahia.com/license"
 *
 * 2/ JSEL - Commercial and Supported Versions of the program
 * ======================================================================================
 *
 * IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 * Alternatively, commercial and supported versions of the program - also known as
 * Enterprise Distributions - must be used in accordance with the terms and conditions
 * contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 *
 *
 * ==========================================================================================
 * =                                   ABOUT JAHIA                                          =
 * ==========================================================================================
 *
 * Rooted in Open Source CMS, Jahia’s Digital Industrialization paradigm is about
 * streamlining Enterprise digital projects across channels to truly control
 * time-to-market and TCO, project after project.
 * Putting an end to “the Tunnel effect”, the Jahia Studio enables IT and
 * marketing teams to collaboratively and iteratively build cutting-edge
 * online business solutions.
 * These, in turn, are securely and easily deployed as modules and apps,
 * reusable across any digital projects, thanks to the Jahia Private App Store Software.
 * Each solution provided by Jahia stems from this overarching vision:
 * Digital Factory, Workspace Factory, Portal Factory and eCommerce Factory.
 * Founded in 2002 and headquartered in Geneva, Switzerland,
 * Jahia Solutions Group has its North American headquarters in Washington DC,
 * with offices in Chicago, Toronto and throughout Europe.
 * Jahia counts hundreds of global brands and governmental organizations
 * among its loyal customers, in more than 20 countries across the globe.
 *
 * For more information, please visit http://www.jahia.com
 */
package org.jahia.ajax.gwt.client.widget.toolbar.action;

import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.util.content.CopyPasteEngine;
import org.jahia.ajax.gwt.client.util.security.PermissionsUtils;
import org.jahia.ajax.gwt.client.widget.LinkerSelectionContext;
import org.jahia.ajax.gwt.client.widget.content.ManagerLinker;
import org.jahia.ajax.gwt.client.widget.edit.EditLinker;

import java.util.List;

/**
 *
 * User: toto
 * Date: Sep 25, 2009
 * Time: 6:57:20 PM
 */
public class PasteActionItem extends NodeTypeAwareBaseActionItem {
    private boolean pasteInMainNode;
    private List<String> childNodeTypesToSkip;

    public boolean isPasteInMainNode() {
        return pasteInMainNode;
    }

    public void setPasteInMainNode(boolean pasteInMainNode) {
        this.pasteInMainNode = pasteInMainNode;
    }

    public void onComponentSelection() {
        GWTJahiaNode m = linker.getSelectionContext().getSingleSelection();
        if (pasteInMainNode) {
            m = linker.getSelectionContext().getMainNode();
        }
        if (m != null) {
            final String pastingMessage = Messages.get("statusbar.pasting.label");
            final String messageStyleName = "x-mask-loading";
            if (linker instanceof ManagerLinker) {
                ManagerLinker managerLinker = (ManagerLinker) linker;
                managerLinker.getLeftComponent().mask(pastingMessage, messageStyleName);
                managerLinker.getTopRightComponent().mask(pastingMessage, messageStyleName);
            } else {
                linker.loading(pastingMessage);
            }
            final CopyPasteEngine copyPasteEngine = CopyPasteEngine.getInstance();
            copyPasteEngine.paste(m, linker, childNodeTypesToSkip);
        }
    }

    public void handleNewLinkerSelection() {
        LinkerSelectionContext lh = linker.getSelectionContext();
        boolean b = false;
        final CopyPasteEngine copyPasteEngine = CopyPasteEngine.getInstance();
        if (!copyPasteEngine.getCopiedNodes().isEmpty()) {
            if (pasteInMainNode) {
                b = lh.getMainNode() != null && hasPermission(lh.getMainNode()) && PermissionsUtils.isPermitted("jcr:addChildNodes", lh.getMainNode());
            } else {
                b = lh.getSingleSelection() != null && hasPermission(lh.getSelectionPermissions()) &&
                        PermissionsUtils.isPermitted("jcr:addChildNodes", lh.getSelectionPermissions()) && lh.isPasteAllowed();
            }

            if (b && linker instanceof EditLinker) {
                final List<String> nodeTypeNames = linker.getSelectionContext().getSingleSelection().getNodeTypes();
                final StringBuilder nodeTypes = new StringBuilder(nodeTypeNames.size() * 15);
                boolean isFirst = true;
                for (String nodeType : nodeTypeNames) {
                    if (isFirst) {
                        nodeTypes.append(nodeType);
                        isFirst = false;
                    } else {
                        nodeTypes.append(",").append(nodeType);
                    }
                }
                b = copyPasteEngine.checkNodeType(nodeTypes.toString());
            }
        }
        boolean isCutWithNodeTypesSkipped = childNodeTypesToSkip != null && copyPasteEngine.isCut();
        setEnabled(b && !isCutWithNodeTypesSkipped && super.isNodeTypeAllowed(copyPasteEngine.getCopiedNodes()));
    }

    public void setChildNodeTypesToSkip(List childNodeTypesToSkip) {
        this.childNodeTypesToSkip = childNodeTypesToSkip;
    }
}
