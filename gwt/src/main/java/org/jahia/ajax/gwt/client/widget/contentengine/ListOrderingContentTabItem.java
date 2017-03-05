/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2017 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.ajax.gwt.client.widget.contentengine;

import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.FieldSet;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import org.jahia.ajax.gwt.client.data.acl.GWTJahiaNodeACL;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeProperty;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeType;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.widget.AsyncTabItem;
import org.jahia.ajax.gwt.client.widget.definition.PropertiesEditor;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Edit engine tab for performing actions on the list of items:
 * manual and automatic ordering, deletion etc.
 * User: ktlili
 * Date: Apr 27, 2010
 * Time: 10:33:26 AM
 */
public class ListOrderingContentTabItem extends ContentTabItem {
    private transient CheckBox useManualRanking;
    private transient ManualListOrderingEditor manualListOrderingEditor = null;
    private static final String JMIX_ORDERED_LIST = "jmix:orderedList";

    public ListOrderingContentTabItem() {
        setHandleCreate(false);
    }

    @Override
    public void attachPropertiesEditor(NodeHolder engine, AsyncTabItem tab) {
        tab.setLayout(new RowLayout());
        tab.add(propertiesEditor);
        if (!engine.isMultipleSelection()) {
            attachManualListOrderingEditor(engine, tab, propertiesEditor);
        }
    }

    /**
     * Create manual list ordering editor
     *
     * @param engine
     * @param tab
     * @param propertiesEditor  @return
     */
    private void attachManualListOrderingEditor(NodeHolder engine, AsyncTabItem tab, final PropertiesEditor propertiesEditor) {
        manualListOrderingEditor = new ManualListOrderingEditor(engine.getNode());
        useManualRanking = new CheckBox();

        // create a field set for the manual ranking
        final FieldSet fieldSet = new FieldSet();
        fieldSet.setCollapsible(true);
        fieldSet.setHeadingHtml(Messages.get("label.manualRanking", "Manual ranking"));
        useManualRanking.setBoxLabel(Messages.get("label.useManualRanking", "Use manual ranking"));

        useManualRanking.addListener(Events.Change, new Listener<ComponentEvent>() {
            public void handleEvent(ComponentEvent componentEvent) {
                if (useManualRanking.getValue()) {
                    propertiesEditor.getRemovedTypes().add(JMIX_ORDERED_LIST);
                    propertiesEditor.getAddedTypes().remove(JMIX_ORDERED_LIST);
                } else {
                    propertiesEditor.getRemovedTypes().remove(JMIX_ORDERED_LIST);
                    propertiesEditor.getAddedTypes().add(JMIX_ORDERED_LIST);
                }

                // update form components
                for (FieldSet component : propertiesEditor.getOrderingListFieldSet()) {
                    if (useManualRanking.getValue()) {
                        component.setData("addedField", null);
                        component.setEnabled(false);
                        component.collapse();
                    } else {
                        component.setData("addedField", "true");
                        component.setEnabled(true);
                        component.expand();
                    }
                }
                manualListOrderingEditor.setEnabled(useManualRanking.getValue());
                if (useManualRanking.getValue()) {
                    manualListOrderingEditor.expand();
                } else {
                    manualListOrderingEditor.collapse();
                }
            }
        });


        // update form components
        boolean isManual = !propertiesEditor.getNodeTypes().contains(new GWTJahiaNodeType(JMIX_ORDERED_LIST));
        for (FieldSet component : propertiesEditor.getOrderingListFieldSet()) {
            component.setEnabled(!isManual);
            if (isManual) {
                component.collapse();
            }
        }
        useManualRanking.setValue(isManual);
        manualListOrderingEditor.setEnabled(isManual);
        if (!isManual) {
            manualListOrderingEditor.collapse();
        }

        fieldSet.add(useManualRanking);
        fieldSet.add(manualListOrderingEditor);
        tab.add(fieldSet);
    }

    @Override
    public void setProcessed(boolean processed) {
        if (!processed && langPropertiesEditorMap != null) {
            manualListOrderingEditor = null;
        }
        super.setProcessed(processed);
    }

    @Override
    public void doSave(GWTJahiaNode node, List<GWTJahiaNodeProperty> changedProperties, Map<String, List<GWTJahiaNodeProperty>> changedI18NProperties, Set<String> addedTypes, Set<String> removedTypes, List<GWTJahiaNode> chidren, GWTJahiaNodeACL acl) {
        if (propertiesEditor != null) {
            if (manualListOrderingEditor != null && useManualRanking.getValue()) {
                for (GWTJahiaNode child : manualListOrderingEditor.getOrderedNodes()) {
                    node.add(child);
                }
                for (GWTJahiaNode child : manualListOrderingEditor.getRemovedNodes()) {
                    node.add(child);
                    node.remove(child);
                }
                node.set(GWTJahiaNode.INCLUDE_CHILDREN, Boolean.TRUE);
            }

            addedTypes.addAll(propertiesEditor.getAddedTypes());
            addedTypes.addAll(propertiesEditor.getExternalMixin());
            changedProperties.addAll(propertiesEditor.getProperties(false, true, node != null));
            removedTypes.addAll(propertiesEditor.getRemovedTypes());
        }

    }

    @Override
    public boolean isOrderableTab() {
        return true;
    }
}
