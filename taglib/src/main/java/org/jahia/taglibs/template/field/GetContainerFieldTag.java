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

import org.jahia.data.beans.ContainerBean;
import org.jahia.data.beans.FieldBean;
import org.jahia.taglibs.ValueJahiaTag;

/**
 * Retrieves the specified field from a container.
 * 
 * @author Xavier Lawrence
 */
@SuppressWarnings("serial")
public class GetContainerFieldTag extends ValueJahiaTag {

    private ContainerBean containerBean;
    private String fieldName;

    public void setContainerBean(ContainerBean containerBean) {
        this.containerBean = containerBean;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public int doStartTag() {
        final FieldBean field = containerBean.getField(fieldName);
        if (field != null) {
            if (getVar() != null) {
                pageContext.setAttribute(getVar(), field);
            }
            if (getValueID() != null) {
                pageContext.setAttribute(getValueID(), field);
            }
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
        containerBean = null;
        fieldName = null;
    }
    
}
