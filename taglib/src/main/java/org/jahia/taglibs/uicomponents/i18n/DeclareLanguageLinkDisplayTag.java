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

import org.jahia.taglibs.AbstractJahiaTag;

import javax.servlet.ServletRequest;
import javax.servlet.jsp.JspTagException;
import java.util.Map;
import java.util.HashMap;

/**
 * @author Xavier Lawrence
 */
@SuppressWarnings("serial")
public class DeclareLanguageLinkDisplayTag extends AbstractJahiaTag {
    private String name;
    private String displayFile;

    public void setName(String name) {
        this.name = name;
    }

    public void setDisplayFile(String displayFile) {
        this.displayFile = displayFile;
    }

    public int doStartTag() throws JspTagException {
        if (name.length() > 0 && displayFile.length() > 0) {
            final ServletRequest request = pageContext.getRequest();
            if (LanguageSwitchTag.DISPLAY_TYPE_BEANS.containsKey(name)) {
                throw new JspTagException("Please use a different value for the 'name' attribute. The value '" +
                        name + "' is a reserved value");
            }

            Map<String, LanguageLinkDisplayBean> customTypes = (Map<String, LanguageLinkDisplayBean>)
                    request.getAttribute(LanguageSwitchTag.CUSTOM_DISPLAY_TYPE_BEANS);

            if (customTypes == null) {
                customTypes = new HashMap<String, LanguageLinkDisplayBean>();
            }

            customTypes.put(name, new LanguageLinkDisplayBean(name, displayFile));
            request.setAttribute(LanguageSwitchTag.CUSTOM_DISPLAY_TYPE_BEANS, customTypes);
        } else {
            throw new JspTagException("Both 'name' and 'displayFile' attributes must have a length > 0");
        }

        return SKIP_BODY;
    }

    public int doEndTag() {
        name = null;
        displayFile = null;
        return EVAL_PAGE;
    }
}
