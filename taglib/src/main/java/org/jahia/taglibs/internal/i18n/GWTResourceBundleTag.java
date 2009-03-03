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

package org.jahia.taglibs.internal.i18n;

import org.jahia.params.ProcessingContext;
import org.jahia.utils.i18n.JahiaResourceBundle;
import org.jahia.data.JahiaData;
import org.jahia.taglibs.utility.i18n.GWTMessageTag;
import org.apache.log4j.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.MissingResourceException;

/**
 * User: ktlili
 * Date: 1 oct. 2008
 * Time: 17:13:28
 */
public class GWTResourceBundleTag extends GWTMessageTag {
    private static final transient Logger logger = Logger.getLogger(GWTResourceBundleTag.class);

    @Override
    protected String getMessage(String titleKey) {
        HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();
        JahiaData jData = (JahiaData) request.getAttribute("org.jahia.data.JahiaData");

        Locale currentLocale = request.getLocale();
        HttpSession session = pageContext.getSession();
        if (session != null) {
            if (session.getAttribute(ProcessingContext.SESSION_LOCALE) != null) {
                currentLocale = (Locale) session.getAttribute(ProcessingContext.SESSION_LOCALE);
            }
        }

        String resValue = null;

        try {

            if (jData != null) {
                resValue = JahiaResourceBundle.getJahiaInternalResource(titleKey, jData.getProcessingContext().getLocale());
            } else {
                // for any reason the jData wasn't loaded correctly
                resValue = JahiaResourceBundle.getJahiaInternalResource(titleKey, currentLocale);
            }
        } catch (MissingResourceException mre) {
            logger.error(mre.toString(), mre);
        }
        if (resValue == null) {
            resValue = titleKey ;
        }
        return resValue;
    }
}