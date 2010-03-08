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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jahia.ajax.gwt.client.data.GWTJahiaLanguage;
import org.jahia.ajax.gwt.client.data.seo.GWTJahiaUrlMapping;
import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.service.content.JahiaContentManagementService;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Represents a dedicated tab for configuring URL mapping for content objects
 * and other SEO-related settings.
 * 
 * @author Sergiy Shyrkov
 */
public class SeoTabItem extends EditEngineTabItem {

    private UrlMappingEditor activeEditor;

    private Map<String, UrlMappingEditor> editorsByLanguage;

    /**
     * Initializes an instance of this class.
     * 
     * @param engine reference to the owner
     */
    public SeoTabItem(AbstractContentEngine engine) {
        super(Messages.get("ece_seo", "SEO"), engine);
        editorsByLanguage = new HashMap<String, UrlMappingEditor>(1);
    }

    @Override
    public void create(GWTJahiaLanguage locale) {
        if (engine.getNode() == null) {
            return;
        }

        UrlMappingEditor next = getEditor(locale);
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

        layout();
    }

    private UrlMappingEditor getEditor(GWTJahiaLanguage locale) {
        UrlMappingEditor editor = editorsByLanguage.get(locale.getCountryIsoCode());
        if (editor == null) {
            editor = new UrlMappingEditor(engine.getNode(), locale);
            editor.setVisible(false);
            editorsByLanguage.put(locale.getCountryIsoCode(), editor);
            add(editor);
        }
        return editor;
    }
    
    public void doSave() {
        Set<String> langs = new HashSet<String>(editorsByLanguage.keySet());
        if (langs.isEmpty()) {
            return;
        }
        List<GWTJahiaUrlMapping> mappings = new ArrayList<GWTJahiaUrlMapping>();
        for (UrlMappingEditor editor : editorsByLanguage.values()) {
            mappings.addAll(editor.getMappings());
        }
        
        JahiaContentManagementService.App.getInstance().saveUrlMappings(engine.getNode(), langs, mappings, new AsyncCallback<Object>() {
            public void onFailure(Throwable throwable) {
                com.google.gwt.user.client.Window.alert(Messages.get("saved_prop_failed", "URL mapping save failed\n\n") + throwable.getLocalizedMessage());
                Log.error("failed", throwable);
            }

            public void onSuccess(Object ok) {
                // do nothing
            }
        });
        
    }
}
