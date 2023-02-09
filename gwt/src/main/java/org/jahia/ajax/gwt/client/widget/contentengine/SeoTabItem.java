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

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jahia.ajax.gwt.client.data.acl.GWTJahiaNodeACL;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeProperty;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.data.seo.GWTJahiaUrlMapping;
import org.jahia.ajax.gwt.client.util.security.PermissionsUtils;
import org.jahia.ajax.gwt.client.widget.AsyncTabItem;

/**
 * Represents a dedicated tab for configuring URL mapping for content objects
 * and other SEO-related settings.
 *
 * @author Sergiy Shyrkov
 */
public class SeoTabItem extends EditEngineTabItem {

    /** The serialVersionUID. */
    private static final long serialVersionUID = -3131345831935771996L;

    private transient UrlMappingEditor activeEditor;

    private transient Map<String, UrlMappingEditor> editorsByLanguage = new HashMap<String, UrlMappingEditor>(1);

    public SeoTabItem() {
        setHandleCreate(false);
    }

    @Override
    public void init(NodeHolder engine, AsyncTabItem tab, String locale) {
        if (engine.getNode() == null) {
            return;
        }

        UrlMappingEditor next = getEditor(engine, tab, locale);
        if (activeEditor != null) {
            if (activeEditor == next) {
                // same as current --> do nothing
                return;
            }
            activeEditor.setVisible(false);
        }
        next.setVisible(true);
        next.layout();
        activeEditor = next;

        tab.layout();
    }

    @Override
    public void setProcessed(boolean processed) {
        if (!processed && editorsByLanguage != null) {
            editorsByLanguage.clear();
            activeEditor = null;
        }
        super.setProcessed(processed);
    }

    private UrlMappingEditor getEditor(NodeHolder engine, AsyncTabItem tab, String locale) {
        UrlMappingEditor editor = editorsByLanguage.get(locale);
        if (editor == null) {
            boolean editable = (!engine.isExistingNode() || (PermissionsUtils.isPermitted("jcr:modifyProperties", engine.getNode()) && !engine.getNode().isLocked()));
            editor = new UrlMappingEditor(engine.getNode(), locale, editable);
            editor.setVisible(false);
            editorsByLanguage.put(locale, editor);
            tab.add(editor);
        }
        return editor;
    }

    public void doSave(GWTJahiaNode node, List<GWTJahiaNodeProperty> changedProperties, Map<String, List<GWTJahiaNodeProperty>> changedI18NProperties, Set<String> addedTypes, Set<String> removedTypes, List<GWTJahiaNode> chidren, GWTJahiaNodeACL acl) {
        Set<String> langs = new HashSet<String>(editorsByLanguage.keySet());
        if (langs.isEmpty()) {
            return;
        }
        Map<String, List<GWTJahiaUrlMapping>> mappings = new HashMap<String, List<GWTJahiaUrlMapping>>();
        for (Map.Entry<String, UrlMappingEditor> editor : editorsByLanguage.entrySet()) {
            mappings.put(editor.getKey(), editor.getValue().getMappings());
        }
        if (!node.getNodeTypes().contains("jmix:vanityUrlMapped")) {
            node.getNodeTypes().add("jmix:vanityUrlMapped");
        }
        node.set("vanityMappings", mappings);

    }
}
