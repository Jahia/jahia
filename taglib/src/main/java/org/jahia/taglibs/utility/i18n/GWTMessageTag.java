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
package org.jahia.taglibs.utility.i18n;

import org.jahia.taglibs.AbstractJahiaTag;

/**
 * User: ktlili
 * Date: 9 sept. 2008
 * Time: 17:23:57
 */
@SuppressWarnings("serial")
public class GWTMessageTag extends AbstractJahiaTag {

    private String resourceName;
    private String aliasResourceName;


    public String getResourceName() {
        return resourceName;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }

    public String getAliasResourceName() {
        return aliasResourceName;
    }

    public void setAliasResourceName(String aliasResourceName) {
        this.aliasResourceName = aliasResourceName;
    }

    public int doStartTag() {
        // add message to the jahia_gwt_dictionary
        addGwtDictionaryMessage(aliasResourceName != null ? aliasResourceName : resourceName, getMessage(resourceName));       
        return SKIP_BODY;
    }


    public int doEndTag() {
        resourceName = null;
        aliasResourceName = null;
        return EVAL_PAGE;
    }

}
