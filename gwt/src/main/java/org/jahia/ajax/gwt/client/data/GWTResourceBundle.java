/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
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
