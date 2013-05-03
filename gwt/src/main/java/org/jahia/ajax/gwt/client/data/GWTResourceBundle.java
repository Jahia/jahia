/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2013 Jahia Solutions Group SA. All rights reserved.
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

package org.jahia.ajax.gwt.client.data;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import com.extjs.gxt.ui.client.data.BaseModelData;

/**
 * Data object that represents a resource bundle in multiple languages.
 * 
 * @author Sergiy Shyrkov
 */
public class GWTResourceBundle extends BaseModelData {

    public static final String DEFAULT_LANG = "<default>";

    private static final Set<String> DEFAULT_LANG_SET = new HashSet<String>(
            Arrays.asList(DEFAULT_LANG));

    private static final long serialVersionUID = -354218306140608645L;

    public GWTResourceBundle() {
        this(null);
    }

    public GWTResourceBundle(String name) {
        super();
        if (name != null) {
            setName(name);
        }
        set("entries", new TreeMap<String, GWTResourceBundleEntry>());
    }

    @SuppressWarnings("unchecked")
    public List<GWTJahiaValueDisplayBean> getAvailableLanguages() {
        return (List<GWTJahiaValueDisplayBean>) get("availableLanguages");
    }

    public Collection<GWTResourceBundleEntry> getEntries() {
        return getEntryMap().values();
    }

    public Map<String, GWTResourceBundleEntry> getEntryMap() {
        return get("entries");
    }

    public Set<String> getLanguages() {
        return getEntryMap().isEmpty() ? DEFAULT_LANG_SET : getEntryMap().entrySet().iterator()
                .next().getValue().getValues().getProperties().keySet();
    }

    public String getName() {
        return get("name");
    }

    public void setAvailableLanguages(List<GWTJahiaValueDisplayBean> langs) {
        set("availableLanguages", langs);
    }

    public void setName(String name) {
        set("name", name);
    }

    public void setValue(String key, String language, String value) {
        GWTResourceBundleEntry e = getEntryMap().get(key);
        if (e == null) {
            e = new GWTResourceBundleEntry(key);
            getEntryMap().put(key, e);
        }
        e.setValue(language, value);
    }

}
