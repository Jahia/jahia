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

/**
 * 
 * User: toto
 * Date: Aug 21, 2009
 * Time: 4:12:53 PM
 * 
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
//            Element toDrag = event.getStatus().getData("element");
//            if (toDrag != null) {
//                Element parent = DOM.getParent(row);
//                parent.insertBefore(toDrag, row);
//            }
        if (PermissionsUtils.isPermitted("jcr:addChildNodes", module.getParentModule().getNode()) && !module.getParentModule().getNode().isLocked()) {
            Insert insert = Insert.get();
            insert.setVisible(true);
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
                        if (source.getNodeTypes().contains(type) || source.getInheritedNodeTypes().contains(type)) {
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
        if (PermissionsUtils.isPermitted("jcr:addChildNodes", jahiaNode) && !jahiaNode.isLocked()) {
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
            EditModeDNDListener.SIMPLEMODULE_TYPE.equals(e.getStatus().getData(EditModeDNDListener.SOURCE_MODULE));
            if (childCount >= listLimit && listLimit != -1 &&
                    (e.getStatus().getData(EditModeDNDListener.SOURCE_MODULE) == null ||
                    !parentModule.equals(((Module) e.getStatus().getData(EditModeDNDListener.SOURCE_MODULE)).getParentModule()))) {
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
