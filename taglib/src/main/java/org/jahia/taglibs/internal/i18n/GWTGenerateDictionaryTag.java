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
package org.jahia.taglibs.internal.i18n;

import org.jahia.taglibs.AbstractJahiaTag;
import org.jahia.data.JahiaData;
import org.jahia.utils.i18n.JahiaResourceBundle;
import org.apache.log4j.Logger;

import javax.servlet.jsp.JspWriter;
import javax.servlet.ServletRequest;
import java.io.IOException;
import java.util.Locale;

/**
 * Create a resource bundle dictionary
 */
@SuppressWarnings("serial")
public class GWTGenerateDictionaryTag extends AbstractJahiaTag {
    private static final transient Logger logger = Logger.getLogger(GWTGenerateDictionaryTag.class);

    public int doStartTag() {
        final JspWriter out = pageContext.getOut();
        // print output
        try {
            ServletRequest request = pageContext.getRequest();
            Locale currentLocale = request.getLocale();
            JahiaData jData = (JahiaData) request.getAttribute("org.jahia.data.JahiaData");
            addMandatoryGwtMessages(jData, currentLocale);
            out.append("<script type='text/javascript'>\n");
            out.append(generateJahiaGwtDictionary());
            out.append("</script>\n");
        } catch (IOException e) {
            logger.error(e, e);
        }
        return EVAL_PAGE;
    }

    /**
     * Add mandatory messages
     *
     * @param jData
     * @param currentLocale
     */
    private void addMandatoryGwtMessages(JahiaData jData, Locale currentLocale) {
        addGwtDictionaryMessage("workInProgressTitle", getAdminMessage("org.jahia.admin.workInProgressTitle", jData, currentLocale));
        addGwtDictionaryMessage("workInProgressProgressText", getAdminMessage("org.jahia.admin.workInProgressProgressText", jData, currentLocale));
    }

    /**
     * Get admin message
     *
     * @param resourceName
     * @param jData
     * @param currentLocale
     * @return
     */
    private String getAdminMessage(String resourceName, JahiaData jData, Locale currentLocale) {
        if (jData != null) {
            return JahiaResourceBundle.getJahiaInternalResource(resourceName, jData.getProcessingContext().getLocale());
        } else {
            // for any reason the jData wasn't loaded correctly
            return JahiaResourceBundle.getJahiaInternalResource(resourceName, currentLocale);
        }
    }

}