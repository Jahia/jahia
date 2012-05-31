/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
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
        set("toolbar", "Full");
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

    public String getToolbarSet() {
        return cfg.get("toolbar");
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

    public void setToolbarSet(String toolbarSet) {
        cfg.put("toolbar", toolbarSet);
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