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

package org.jahia.ajax.gwt.client.widget.edit.contentengine;

import org.jahia.ajax.gwt.client.data.GWTJahiaLanguage;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeProperty;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodePropertyType;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodePropertyValue;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.widget.definition.TagsEditor;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: david
 * Date: Jun 30, 2010
 * Time: 10:04:47 AM
 * To change this template use File | Settings | File Templates.
 */
public class TagsTabItem extends EditEngineTabItem {
    private TagsEditor tagsEditor;

    public TagsTabItem(NodeHolder engine) {
        super(Messages.get("label.engineTab.tags", "Tags"), engine);
    }

    public void create(GWTJahiaLanguage locale) {
        if (!engine.isExistingNode() || (engine.getNode() != null)) {
            setProcessed(true);
            tagsEditor = new TagsEditor(engine.getNode());
            add(tagsEditor);

        }
        layout();
    }

    public TagsEditor getTagsEditor() {
        return tagsEditor;
    }
    
    public void updateProperties(List<GWTJahiaNodeProperty> list, List<String> mixin) {
        if (tagsEditor == null) {
            return;
        }

        GWTJahiaNodeProperty tags = null;

        for (GWTJahiaNodeProperty property : list) {
            if (property.getName().equals("j:tags")) {
                tags = property;
            }
        }
        List<GWTJahiaNode> gwtJahiaNodes = tagsEditor.getTagStore().getAllItems();
        List<GWTJahiaNodePropertyValue> values = new ArrayList<GWTJahiaNodePropertyValue>(gwtJahiaNodes.size());
        for (GWTJahiaNode gwtJahiaNode : gwtJahiaNodes) {
            values.add(new GWTJahiaNodePropertyValue(gwtJahiaNode, GWTJahiaNodePropertyType.WEAKREFERENCE));
        }

        GWTJahiaNodeProperty gwtJahiaNodeProperty = new GWTJahiaNodeProperty();
        gwtJahiaNodeProperty.setMultiple(true);
        gwtJahiaNodeProperty.setValues(values);
        gwtJahiaNodeProperty.setName("j:tags");
        if (tags != null) {
            if (values.isEmpty()) {
                mixin.remove("jmix:tagged");
                list.remove(tags);
            } else {
                list.add(gwtJahiaNodeProperty);
            }
        } else {
            if (!values.isEmpty()) {
                mixin.add("jmix:tagged");
                list.add(gwtJahiaNodeProperty);
            }
        }
    }

}
