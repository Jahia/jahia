/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.ajax.gwt.client.widget.contentengine;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jahia.ajax.gwt.client.data.acl.GWTJahiaNodeACL;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeProperty;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.widget.AsyncTabItem;

/**
 * Edit engine tab that embeds resource bundle editor.
 *
 * @author Sergiy Shyrkov
 */
public class ResourceBundleEditorTabItem extends EditEngineTabItem {

    private static final long serialVersionUID = -1527005357999548838L;

    private transient ResourceBundleEditor resourceBundleEditor;

    public ResourceBundleEditorTabItem() {
        setHandleCreate(false);
    }

    @Override
    public void doSave(GWTJahiaNode node, List<GWTJahiaNodeProperty> changedProperties,
                       Map<String, List<GWTJahiaNodeProperty>> changedI18NProperties, Set<String> addedTypes,
                       Set<String> removedTypes, List<GWTJahiaNode> chidren, GWTJahiaNodeACL acl) {
        if (resourceBundleEditor != null) {
            node.set(GWTJahiaNode.RESOURCE_BUNDLE, resourceBundleEditor.getResourceBundle());
        }
    }

    @Override
    public void init(final NodeHolder engine, final AsyncTabItem tab, String locale) {
        if (engine.getNode() == null) {
            return;
        }

        resourceBundleEditor = new ResourceBundleEditor(engine);
        tab.add(resourceBundleEditor);
        tab.layout();
    }

    @Override
    public void setProcessed(boolean processed) {
        if (!processed) {
            resourceBundleEditor = null;
        }

        super.setProcessed(processed);
    }

}
