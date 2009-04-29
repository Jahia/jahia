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
package org.jahia.taglibs.uicomponents.i18n;

/**
 * @author Xavier Lawrence
 */
public class LanguageLinkDisplayBean {
    private String name;
    private String displayFile;

    public LanguageLinkDisplayBean() {
    }

    public LanguageLinkDisplayBean(final String name, final String displayFile) {
        this.name = name;
        this.displayFile = displayFile;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDisplayFile() {
        return displayFile;
    }

    public void setDisplayFile(String displayFile) {
        this.displayFile = displayFile;
    }
}
