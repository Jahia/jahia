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
import org.jahia.taglibs.AbstractJahiaTag;

/**
 * @author Xavier Lawrence
 */
@SuppressWarnings("serial")
public class GetContainerFieldTag extends AbstractJahiaTag {

    private String valueID;
    private ContainerBean containerBean;
    private String fieldName;

    public void setContainerBean(ContainerBean containerBean) {
        this.containerBean = containerBean;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public void setValueID(String valueID) {
        this.valueID = valueID;
    }

    public int doStartTag() {
        final FieldBean field = containerBean.getField(fieldName);
        if (field != null && valueID != null) {
            pageContext.setAttribute(valueID, field);
        }
        return SKIP_BODY;
    }

    public int doEndTag() {
        valueID = null;
        containerBean = null;
        fieldName = null;
        return EVAL_PAGE;
    }
}
