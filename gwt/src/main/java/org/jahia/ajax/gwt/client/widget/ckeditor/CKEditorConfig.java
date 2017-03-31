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
package org.jahia.ajax.gwt.client.widget.ckeditor;

import java.util.HashMap;
import java.util.Map;

/**
 * Custom configuration options that will be passed when creating CKEditor instance.
 * 
 * @author ktlili
 */
public class CKEditorConfig {
    
    private Map<String, String> cfg = new HashMap<String, String>();

    public CKEditorConfig() {
        super();
        set("width", "98%");
        set("height", "300px");
    }
    
    public CKEditorConfig(Map<String, String> cfg) {
        this();
        if (cfg != null) {
            this.cfg.putAll(cfg);
        }
    }

    public String getHeight() {
        return cfg.get("height");
    }

    public String getWidth() {
        return cfg.get("width");
    }

    public CKEditorConfig set(String key, String value) {
        cfg.put(key, value);
        return this;
    }

    public void setHeight(String height) {
        cfg.put("height", height);
    }

    public void setDefaultToolbar(String toolbarSet) {
        cfg.put("defaultToolbar", toolbarSet);
    }

    public void setWidth(String width) {
        cfg.put("width", width);
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("{");
        boolean first = true;
        for (Map.Entry<String, String> option : cfg.entrySet()) {
            if (first) {
                first = false;
            } else {
                sb.append(", ");
            }
            sb.append(option.getKey()).append(":");

            boolean skipQuotes = option.getValue() != null
                    && (option.getValue().startsWith("[") && option.getValue().endsWith("]") || option
                            .getValue().startsWith("{") && option.getValue().endsWith("}"));
            if (!skipQuotes) {
                sb.append("\'");
            }
            sb.append(option.getValue());
            if (!skipQuotes) {
                sb.append("\'");
            }
        }
        sb.append("}");

        return sb.toString();
    }
}