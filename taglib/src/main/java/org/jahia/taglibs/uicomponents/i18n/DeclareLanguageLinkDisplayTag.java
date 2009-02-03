/**
 * 
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
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
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
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
