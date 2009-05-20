/**
 * Jahia Enterprise Edition v6
 *
 * Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.
 *
 * Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
 * with Document Management and Portal features.
 *
 * The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED.
 *
 * Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
 * you and Jahia (Jahia Sustainable Enterprise License - JSEL).
 *
 * If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.engines;

import java.util.Locale;

/**
 * Bean class representing a language link. It is used by the engines to display flags
 *
 * @author Xavier Lawrence
 */
public class LangLink {

    private String url;
    private String label;
    private Locale locale;
    private boolean isSelected;

    public LangLink(final String url,
                    final String label,
                    final Locale locale,
                    final boolean isSelected) {
        this.url = url;
        this.label = label;
        this.locale = locale;
        this.isSelected = isSelected;
    }

    public String getUrl() {
        return url;
    }

    public String getLabel() {
        return label;
    }

    public String getLanguageCode() {
        return locale.toString();
    }

    public Locale getLocale() {
        return locale;
    }

    public boolean isSelected() {
        return isSelected;
    }
}
