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

import org.jahia.content.ContentObject;
import org.jahia.content.JahiaObject;
import org.jahia.content.ObjectKey;
import org.jahia.data.JahiaData;
import org.jahia.params.ProcessingContext;
import org.jahia.taglibs.AbstractJahiaTag;
import org.jahia.ajax.gwt.client.core.JahiaType;
import org.jahia.ajax.gwt.client.util.Constants;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.JspWriter;

/**
 * @author Xavier Lawrence
 */
@SuppressWarnings("serial")
public class DisplayLanguageStateTag extends AbstractJahiaTag {

    private static final transient org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger(DisplayLanguageStateTag.class);

    private String objectKey;

    public void setObjectKey(String objectKey) {
        this.objectKey = objectKey;
    }

    public int doStartTag() throws JspTagException {
        final HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();
        final JahiaData jData = (JahiaData) request.getAttribute("org.jahia.data.JahiaData");
        final ProcessingContext jParams = jData.getProcessingContext();
        final ContentObject obj;
        if (objectKey != null && objectKey.length() > 0) {
            try {
                final ObjectKey theKey = ObjectKey.getInstance(objectKey);
                obj = (ContentObject) JahiaObject.getInstance(theKey);
            } catch (final Exception e) {
                throw new JspTagException(e.getLocalizedMessage(), e);
            }
        } else {
            obj = jParams.getContentPage();
        }


        if (!jData.gui().isNormalMode()) {
            final StringBuffer buff = new StringBuffer();
            String objKey = obj.getObjectKey().getKey() ;
            final JspWriter out = pageContext.getOut();
            final String uid = new StringBuffer(objKey).append(Constants.UID_SEPARATOR).append(System.currentTimeMillis() % Constants.MODULO).append("_").append(Math.random()).toString() ;
            buff.append("<span ").append(JahiaType.JAHIA_TYPE).append("=\"").append(JahiaType.ACTION_MENU)
                .append("\" id=\"").append(uid)
                .append("\" wfkey=\"").append(objKey)
                .append("\" lang=\"").append(languageCode)
                .append("\" extended=\"").append(String.valueOf(languageCode.equals(jParams.getLocale().getLanguage())))
                .append("\" statusonly=\"true\"></span>") ;
            try {
                out.print(buff.toString());
                out.flush();
            } catch (final Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
        return SKIP_BODY;
    }

    public int doEndTag() {
        objectKey = null;
        return EVAL_PAGE;
    }
}
