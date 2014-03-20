/**
 * ==========================================================================================
 * =                        DIGITAL FACTORY v7.0 - Community Distribution                   =
 * ==========================================================================================
 *
 *     Rooted in Open Source CMS, Jahia's Digital Industrialization paradigm is about
 *     streamlining Enterprise digital projects across channels to truly control
 *     time-to-market and TCO, project after project.
 *     Putting an end to "the Tunnel effect", the Jahia Studio enables IT and
 *     marketing teams to collaboratively and iteratively build cutting-edge
 *     online business solutions.
 *     These, in turn, are securely and easily deployed as modules and apps,
 *     reusable across any digital projects, thanks to the Jahia Private App Store Software.
 *     Each solution provided by Jahia stems from this overarching vision:
 *     Digital Factory, Workspace Factory, Portal Factory and eCommerce Factory.
 *     Founded in 2002 and headquartered in Geneva, Switzerland,
 *     Jahia Solutions Group has its North American headquarters in Washington DC,
 *     with offices in Chicago, Toronto and throughout Europe.
 *     Jahia counts hundreds of global brands and governmental organizations
 *     among its loyal customers, in more than 20 countries across the globe.
 *
 *     For more information, please visit http://www.jahia.com
 *
 * JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION
 * ============================================
 *
 *     Copyright (C) 2002-2014 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==========================================================
 *
 *     IF YOU DECIDE TO CHOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     "This program is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU General Public License
 *     as published by the Free Software Foundation; either version 2
 *     of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 *     As a special exception to the terms and conditions of version 2.0 of
 *     the GPL (or any later version), you may redistribute this Program in connection
 *     with Free/Libre and Open Source Software ("FLOSS") applications as described
 *     in Jahia's FLOSS exception. You should have received a copy of the text
 *     describing the FLOSS exception, and it is also available here:
 *     http://www.jahia.com/license"
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ==========================================================
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
package org.jahia.ajax.gwt.client.widget.toolbar.action;

import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeProperty;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodePropertyType;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodePropertyValue;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.util.content.CopyPasteEngine;
import org.jahia.ajax.gwt.client.util.content.actions.ContentActions;
import org.jahia.ajax.gwt.client.util.security.PermissionsUtils;
import org.jahia.ajax.gwt.client.widget.LinkerSelectionContext;
import org.jahia.ajax.gwt.client.widget.edit.ContentTypeWindow;
import org.jahia.ajax.gwt.client.widget.edit.EditLinker;
import org.jahia.ajax.gwt.client.widget.edit.mainarea.Module;

import java.util.*;

/**
 * 
* User: toto
* Date: Sep 25, 2009
* Time: 6:57:42 PM
* 
*/
public class PasteReferenceActionItem extends BaseActionItem  {
    protected transient List<String> allowedRefs;

    public void onComponentSelection() {
        if (CopyPasteEngine.getInstance().getCopiedNodes().size() == 1) {
            GWTJahiaNode copiedNode = CopyPasteEngine.getInstance().getCopiedNodes().get(0);
            Map<String, GWTJahiaNodeProperty> props = new HashMap<String, GWTJahiaNodeProperty>(2);
            props.put("jcr:title", new GWTJahiaNodeProperty("jcr:title", new GWTJahiaNodePropertyValue(copiedNode.getDisplayName(), GWTJahiaNodePropertyType.STRING)));
            props.put("j:node", new GWTJahiaNodeProperty("j:node", new GWTJahiaNodePropertyValue(copiedNode, GWTJahiaNodePropertyType.WEAKREFERENCE)));
            ContentTypeWindow.createContent(linker, copiedNode.getName(), allowedRefs, props, linker.getSelectionContext().getSingleSelection(), true, false);
        } else {
            ContentActions.pasteReference(linker);
        }
    }

    public void handleNewLinkerSelection() {
        boolean b = false;
        if (!CopyPasteEngine.getInstance().getCopiedNodes().isEmpty()) {
            LinkerSelectionContext lh = linker.getSelectionContext();
            b = lh.getSingleSelection() != null
                    && !lh.isLocked()
                    && hasPermission(lh.getSelectionPermissions())
                    && PermissionsUtils.isPermitted("jcr:addChildNodes", lh.getSelectionPermissions())
                    && lh.isPasteAllowed();
            String refTypes = null;
            if (linker instanceof EditLinker && b) {
                final Module module = ((EditLinker) linker).getSelectedModule();
                refTypes = module.getReferenceTypes();
            } else if (lh.getSingleSelection() != null) {
                refTypes = lh.getSingleSelection().get("referenceTypes");
            }
            if (refTypes != null && refTypes.length() > 0) {
                String[] refs = refTypes.split(" ");
                allowedRefs = new ArrayList<String>();
                for (String ref : refs) {
                    String[] types = ref.split("\\[|\\]");
                    if (types[1] != null && Arrays.asList(types[1].split(" |,")).contains("jnt:contentReference")) {
                        allowedRefs.add("jnt:contentReference");
                        break;
                    } else if (checkNodeType(CopyPasteEngine.getInstance().getCopiedNodes(), types[1])) {
                        allowedRefs.add(types[0]);
                    }
                }
                if (this.allowedRefs.size() == 0) {
                    b = false;
                }
            } else {
                b = false;
            }
        }
        setEnabled(b);
    }

    private boolean checkNodeType(List<GWTJahiaNode> sources, String nodetypes) {
        boolean allowed = true;

        if (nodetypes != null && nodetypes.length() > 0) {
            if (sources != null) {
                String[] allowedTypes = nodetypes.split(" |,");
                for (GWTJahiaNode source : sources) {
                    boolean nodeAllowed = false;
                    for (String type : allowedTypes) {
                        if (source.getNodeTypes().contains(type) || source.getInheritedNodeTypes().contains(type)) {
                            nodeAllowed = true;
                            break;
                        }
                    }
                    allowed &= nodeAllowed;
                }
            }
        } else {
            allowed = false;
        }
        return allowed;
    }

}
