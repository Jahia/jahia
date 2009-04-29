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
package org.jahia.taglibs.template.field;

import org.jahia.taglibs.AbstractJahiaTag;
import org.jahia.services.fields.ContentField;
import org.jahia.data.JahiaData;
import org.jahia.data.beans.FieldBean;
import org.jahia.params.ProcessingContext;
import org.apache.log4j.Logger;

import javax.servlet.ServletRequest;
import javax.servlet.jsp.JspTagException;

/**
 * Allow to get any field from its ID
 */
@SuppressWarnings("serial")
public class GetFieldTag extends AbstractJahiaTag {
    private static transient final Logger logger = Logger.getLogger(GetFieldTag.class);

    private int fieldID;
    private String valueID;

    public void setFieldID(int fieldID) {
        this.fieldID = fieldID;
    }

    public void setValueID(String valueID) {
        this.valueID = valueID;
    }

    public int doStartTag() {
        try {
            final ContentField cf = ContentField.getField(fieldID);
            if (cf == null) return SKIP_BODY;

            final ServletRequest request = pageContext.getRequest();
            final JahiaData jData = (JahiaData) request.getAttribute("org.jahia.data.JahiaData");
            final ProcessingContext jParams = jData.getProcessingContext();

            final FieldBean cfBean = new FieldBean(cf.getJahiaField(jParams.getEntryLoadRequest()),
                    jParams);
            if (valueID != null && valueID.length() > 0) {
                pageContext.setAttribute(valueID, cfBean);
            } else {
                throw new JspTagException("valueID attribute must not have an empty value");
            }

        } catch (Exception e) {
            logger.error("Error in GetFieldTag", e);
        }
        return SKIP_BODY;
    }

    public int doEndTag() {
        fieldID = -1;
        valueID = null;
        return EVAL_PAGE;
    }
}