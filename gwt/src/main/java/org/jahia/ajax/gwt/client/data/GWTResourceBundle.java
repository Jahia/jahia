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
package org.jahia.ajax.gwt.client.data;

import java.util.*;

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
        if (getEntryMap().isEmpty()) {
            return DEFAULT_LANG_SET;
        } else {
            Set<String> languages = new HashSet<String>();
            Iterator<Map.Entry<String, GWTResourceBundleEntry>> entries = getEntryMap().entrySet().iterator();

            while (entries.hasNext()) {
                languages.addAll(entries.next().getValue().getValues().getProperties().keySet());

            }
            return languages;
        }
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
