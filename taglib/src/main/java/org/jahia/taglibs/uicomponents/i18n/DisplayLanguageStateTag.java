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
