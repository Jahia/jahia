/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
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
