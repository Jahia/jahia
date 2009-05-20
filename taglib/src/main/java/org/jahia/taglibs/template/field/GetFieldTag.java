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

import org.jahia.taglibs.ValueJahiaTag;
import org.jahia.services.fields.ContentField;
import org.jahia.data.beans.FieldBean;
import org.jahia.params.ProcessingContext;
import org.apache.log4j.Logger;

/**
 * Allow to get any field from its ID.
 */
@SuppressWarnings("serial")
public class GetFieldTag extends ValueJahiaTag {
    private static transient final Logger logger = Logger.getLogger(GetFieldTag.class);

    private int fieldID;

    public void setFieldID(int fieldID) {
        this.fieldID = fieldID;
    }

    public int doStartTag() {
        try {
            final ContentField cf = ContentField.getField(fieldID);
            if (cf == null) return SKIP_BODY;

            final ProcessingContext jParams = getProcessingContext();

            final FieldBean cfBean = new FieldBean(cf.getJahiaField(jParams.getEntryLoadRequest()),
                    jParams);
            if (getVar() != null) {
                pageContext.setAttribute(getVar(), cfBean);
            }
            if (getValueID() != null) {
                pageContext.setAttribute(getValueID(), cfBean);
            }
        } catch (Exception e) {
            logger.error("Error in GetFieldTag", e);
        }
        return SKIP_BODY;
    }

    public int doEndTag() {
        resetState();
        return EVAL_PAGE;
    }
    
    @Override
    protected void resetState() {
        super.resetState();
        fieldID = -1;
    }
}