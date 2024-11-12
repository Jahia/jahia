/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
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

import com.extjs.gxt.ui.client.dnd.DND;
import com.extjs.gxt.ui.client.event.DNDEvent;
import com.google.gwt.user.client.DOM;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.util.security.PermissionsUtils;
import org.jahia.ajax.gwt.client.widget.edit.EditModeDNDListener;
import org.jahia.ajax.gwt.client.widget.edit.EditModeDragSource;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * GWT Drag source for simple modules
 *
 * @see SimpleModule
 *
 * User: toto
 * Date: Aug 21, 2009
 * Time: 4:16:42 PM
 */
public class SimpleModuleDragSource extends EditModeDragSource {

    private Module module;

    public SimpleModuleDragSource(Module target) {
        super(target.getContainer());
        this.module = target;
    }

    public Module getModule() {
        return module;
    }

    @Override
    protected void onDragEnd(DNDEvent e) {
        if (e.getStatus().getData("operationCalled") == null) {
            DOM.setStyleAttribute(module.getHtml().getElement(), "display", "block");
        }
        super.onDragEnd(e);
    }

    @Override
    protected void onDragCancelled(DNDEvent dndEvent) {
        DOM.setStyleAttribute(module.getHtml().getElement(), "display", "block");
        super.onDragCancelled(dndEvent);
    }

    @Override
    protected void onDragStart(DNDEvent e) {

        super.onDragStart(e);

        List<GWTJahiaNode> l = new ArrayList<GWTJahiaNode>();
        MainModule mainModule = getModule().getMainModule();
        Set<Module> moduleSet = new HashSet<Module>();
        if (e.isControlKey() || mainModule.getSelections().containsKey(module)) {
            moduleSet.addAll(mainModule.getSelections().keySet());
        }
        moduleSet.add(module);
        for (Module m : moduleSet) {
            if (m.isDraggable()) {
                if (PermissionsUtils.isPermitted("jcr:removeNode", m.getNode()) && !m.getNode().isLocked()) {
                    e.setCancelled(false);
                    e.setData(this);
                    e.setOperation(DND.Operation.COPY);
                    if (getStatusText() == null) {
                        e.getStatus().update(DOM.clone(m.getHtml().getElement(), true));

                        e.getStatus().setData("element", m.getHtml().getElement());
                        DOM.setStyleAttribute(m.getHtml().getElement(), "display", "none");

                    }
                } else {
                    e.setCancelled(true);
                }
                Selection selection = mainModule.getSelections().get(m);
                if (selection != null) {
                    selection.hide();
                }
                l.add(m.getNode());
            }
        }
        if (!l.isEmpty()) {
            e.getStatus().setData(EditModeDNDListener.SOURCE_TYPE, EditModeDNDListener.SIMPLEMODULE_TYPE);
            e.getStatus().setData(EditModeDNDListener.SOURCE_MODULES, moduleSet);
            e.getStatus().setData(EditModeDNDListener.SOURCE_NODES, l);
        }
    }

}
