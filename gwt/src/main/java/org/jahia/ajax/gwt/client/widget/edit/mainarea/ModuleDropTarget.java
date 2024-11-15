/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
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
package org.jahia.ajax.gwt.client.widget.edit.mainarea;

import com.extjs.gxt.ui.client.core.El;
import com.extjs.gxt.ui.client.dnd.DND;
import com.extjs.gxt.ui.client.dnd.DropTarget;
import com.extjs.gxt.ui.client.dnd.Insert;
import com.extjs.gxt.ui.client.event.DNDEvent;
import com.extjs.gxt.ui.client.util.Rectangle;
import com.google.gwt.user.client.Element;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeType;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.util.security.PermissionsUtils;
import org.jahia.ajax.gwt.client.widget.edit.EditModeDNDListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * GWT Drop target for modules
 *
 * User: toto
 * Date: Aug 21, 2009
 * Time: 4:12:53 PM
 */
public class ModuleDropTarget extends DropTarget {

    private Module module;
    protected String targetType;

    public ModuleDropTarget(Module target, String targetType) {
        super(target.getContainer());
        this.module = target;
        setOperation(DND.Operation.COPY);
        this.targetType = targetType;
    }

    public Module getModule() {
        return module;
    }

    @Override
    protected void onDragMove(DNDEvent event) {
        super.onDragMove(event);
        event.setCancelled(false);
    }

    @Override
    protected void showFeedback(DNDEvent event) {
        showInsert(event, this.getComponent().getElement(), true);
    }

    private void showInsert(DNDEvent event, Element row, boolean before) {
        if (PermissionsUtils.isPermitted("jcr:addChildNodes", module.getParentModule().getNode()) && (!module.getParentModule().getNode().isLocked() || module.getParentModule().getNode().isLockAllowsAdd())) {
            Insert insert = Insert.get();
            insert.setVisible(true);
            // Set insert relative to main content
            MainModule.getInstance().getInnerElement().appendChild(insert.getElement());
            Rectangle rect = El.fly(row).getBounds();
            int y = !before ? (rect.y + rect.height - 4) : rect.y - 2;
            insert.el().setBounds(rect.x, y, rect.width, 20);
        }
    }

    /**
     * Checks if the event source node (or source node type) is of certain nodetypes
     * @param e
     * @param nodetypes
     * @return
     */
    private boolean checkNodeType(DNDEvent e, String nodetypes) {
        boolean allowed = true;

        if (nodetypes != null && nodetypes.length() > 0) {
            List<GWTJahiaNode> sources = e.getStatus().getData(EditModeDNDListener.SOURCE_NODES);
            if (sources != null) {
                String[] allowedTypes = nodetypes.split(" |,");
                for (GWTJahiaNode source : sources) {
                    boolean nodeAllowed = false;
                    for (String type : allowedTypes) {
                        if (source.isNodeType(type)
                                || (source.isReference() && source.getReferencedNode() != null && source.getReferencedNode().isNodeType(type))) {
                            nodeAllowed = true;
                            break;
                        }
                    }
                    allowed &= nodeAllowed;
                }
            }
            GWTJahiaNodeType type = e.getStatus().getData(EditModeDNDListener.SOURCE_NODETYPE);
            if (type != null) {
                String[] allowedTypes = nodetypes.split(" ");
                boolean typeAllowed = false;
                for (String t : allowedTypes) {
                    if (!type.isMixin() && (t.equals(type.getName()) || type.getSuperTypes().contains(t))) {
                        typeAllowed = true;
                        break;
                    }
                }
                allowed &= typeAllowed;
            }
        }
        return allowed;
    }

    @Override
    protected void onDragEnter(DNDEvent e) {
        Module parentModule = module.getParentModule();
        final GWTJahiaNode jahiaNode = parentModule.getNode();
        if (PermissionsUtils.isPermitted("jcr:addChildNodes", jahiaNode) &&  (!jahiaNode.isLocked() || jahiaNode.isLockAllowsAdd())) {
            String nodetypes = parentModule.getNodeTypes();
            if (targetType.equals(EditModeDNDListener.PLACEHOLDER_TYPE) &&
                    (module.getNodeTypes() != null) && (module.getNodeTypes().length() > 0)) {
                nodetypes = module.getNodeTypes();
            }
            int listLimit = parentModule.getListLimit();
            int childCount = parentModule.getChildCount();
            if (EditModeDNDListener.EMPTYAREA_TYPE.equals(targetType)) {
                nodetypes = module.getNodeTypes();
                listLimit = module.getListLimit();
                childCount = module.getChildCount();
            }

            List<GWTJahiaNode> sources = e.getStatus().getData(EditModeDNDListener.SOURCE_NODES);
            int totalCount = childCount + (sources == null ? 1 : sources.size());
            if (e.getStatus().getData(EditModeDNDListener.SOURCE_MODULES) != null) {
                Set<Module> modules = e.getStatus().getData(EditModeDNDListener.SOURCE_MODULES);
                for (Module moduleToCopy : modules) {
                    if (parentModule.equals(moduleToCopy.getParentModule())) {
                        totalCount --;
                    }
                }
            }
            if (totalCount > listLimit && listLimit != -1) {
                e.getStatus().setStatus(false);
                e.setCancelled(false);
                return;
            }

            // first let's check if we can directly instantiate such a node type, in the case of droppping a definition of the same type
            boolean allowed = !EditModeDNDListener.CONTENT_SOURCE_TYPE.equals(e.getStatus().getData(EditModeDNDListener.SOURCE_TYPE)) &&  checkNodeType(e, nodetypes);

            if (allowed) {
                e.getStatus().setData(EditModeDNDListener.TARGET_TYPE, targetType);
                e.getStatus().setData(EditModeDNDListener.TARGET_REFERENCE_TYPE, null);
                e.getStatus().setData(EditModeDNDListener.TARGET_PATH, module.getPath());
                e.getStatus().setData(EditModeDNDListener.TARGET_NODE, module.getNode() != null ? module.getNode() : jahiaNode);
            } else {
                String refTypes = parentModule.getReferenceTypes();
                if (targetType.equals(EditModeDNDListener.EMPTYAREA_TYPE)) {
                    refTypes = module.getReferenceTypes();
                } else if (targetType.equals(EditModeDNDListener.PLACEHOLDER_TYPE)) {
                    if (module.getReferenceTypes() != null) {
                        refTypes = module.getReferenceTypes();
                    }
                }
                if (refTypes.length() > 0 && e.getStatus().getData(EditModeDNDListener.SOURCE_NODES) != null) {
                    String[] refs = refTypes.split(" ");
                    List<String> allowedRefs = new ArrayList<String>();
                    for (String ref : refs) {
                        String[] types = ref.split("\\[|\\]");
                        if (checkNodeType(e, types[1])) {
                            allowedRefs.add(types[0]);
                        }
                    }
                    if (allowedRefs.size() > 0) {
                        allowed = true;
                        e.getStatus().setData(EditModeDNDListener.TARGET_TYPE, targetType);
                        e.getStatus().setData(EditModeDNDListener.TARGET_REFERENCE_TYPE, allowedRefs);
                        e.getStatus().setData(EditModeDNDListener.TARGET_PATH, module.getPath());
                        e.getStatus().setData(EditModeDNDListener.TARGET_NODE, module.getNode() != null ? module.getNode() : jahiaNode);
                    }
                }
            }
            e.getStatus().setStatus(allowed);
            e.setCancelled(false);
        } else {
            e.getStatus().setStatus(false);
        }
    }

}
