/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2011 Jahia Solutions Group SA. All rights reserved.
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
import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.user.client.Window;
import org.jahia.ajax.gwt.client.core.BaseAsyncCallback;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeProperty;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodePropertyType;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodePropertyValue;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeType;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.service.definition.JahiaContentDefinitionService;
import org.jahia.ajax.gwt.client.util.content.CopyPasteEngine;
import org.jahia.ajax.gwt.client.util.security.PermissionsUtils;
import org.jahia.ajax.gwt.client.widget.LinkerSelectionContext;
import org.jahia.ajax.gwt.client.util.content.actions.ContentActions;
import org.jahia.ajax.gwt.client.widget.edit.ContentTypeWindow;
import org.jahia.ajax.gwt.client.widget.edit.EditLinker;
import org.jahia.ajax.gwt.client.widget.contentengine.EngineLoader;
import org.jahia.ajax.gwt.client.widget.edit.mainarea.Module;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
* User: toto
* Date: Sep 25, 2009
* Time: 6:57:42 PM
* 
*/
public class PasteReferenceActionItem extends BaseActionItem  {
    protected transient List<String> allowedRefs;

    public void onComponentSelection() {
        if (CopyPasteEngine.getInstance().getCopiedPaths().size() == 1) {
        JahiaContentDefinitionService.App.getInstance().getNodeTypes(allowedRefs, new BaseAsyncCallback<List<GWTJahiaNodeType>>() {
            public void onApplicationFailure(Throwable caught) {
                Window.alert("Cannot retrieve node type "+allowedRefs+". Cause: " + caught.getLocalizedMessage());
                Log.error("Cannot retrieve node type "+allowedRefs+". Cause: " + caught.getLocalizedMessage(), caught);
            }

            public void onSuccess(List<GWTJahiaNodeType> result) {
                GWTJahiaNode copiedNode = CopyPasteEngine.getInstance().getCopiedPaths().get(0);
                Map<String, GWTJahiaNodeProperty> props = new HashMap<String, GWTJahiaNodeProperty>(2);
                props.put("jcr:title", new GWTJahiaNodeProperty("jcr:title", new GWTJahiaNodePropertyValue(copiedNode.getDisplayName(), GWTJahiaNodePropertyType.STRING)));
                props.put("j:node", new GWTJahiaNodeProperty("j:node", new GWTJahiaNodePropertyValue(copiedNode, GWTJahiaNodePropertyType.WEAKREFERENCE)));
                if (result.size() == 1) {
                    EngineLoader.showCreateEngine(linker, linker.getSelectionContext().getSingleSelection(), result.get(0), props, copiedNode.getName(), false);
                } else {
                    Map<GWTJahiaNodeType, List<GWTJahiaNodeType>> m = new HashMap<GWTJahiaNodeType, List<GWTJahiaNodeType>>();
                    m.put(null, result);
                    new ContentTypeWindow(linker, linker.getSelectionContext().getSingleSelection(), m, props, copiedNode.getName(), false).show();
                }
            }
        });
        } else {
            ContentActions.pasteReference(linker);
        }
    }

    public void handleNewLinkerSelection() {
        LinkerSelectionContext lh = linker.getSelectionContext();
        boolean b = lh.getSingleSelection() != null && PermissionsUtils.isPermitted("jcr:addChildNodes", lh.getSelectionPermissions()) && lh.isPasteAllowed();

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
                if (checkNodeType(CopyPasteEngine.getInstance().getCopiedPaths(), types[1])) {
                    allowedRefs.add(types[0]);
                }
            }
            if (this.allowedRefs.size() == 0) {
                b = false;
            }
        } else {
            b = false;
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
