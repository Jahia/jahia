/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
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
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.taglibs.internal.i18n;

import org.jahia.taglibs.AbstractJahiaTag;
import org.jahia.data.JahiaData;
import org.jahia.utils.i18n.JahiaResourceBundle;
import org.jahia.bin.Jahia;
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
        addGwtDictionaryMessage("fm_copyright", Jahia.COPYRIGHT_TXT + " " + Jahia.VERSION + "." + Jahia.getPatchNumber() + " r" + Jahia.getBuildNumber());
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
            return JahiaResourceBundle.getJahiaInternalResource(resourceName, jData.getProcessingContext().getUILocale());
        } else {
            // for any reason the jData wasn't loaded correctly
            return JahiaResourceBundle.getJahiaInternalResource(resourceName, currentLocale);
        }
    }

}