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
package org.jahia.taglibs.internal.pagination;

import org.jahia.taglibs.AbstractJahiaTag;
import org.jahia.utils.JahiaConsole;

/**
 * @author Xavier Lawrence
 */
@SuppressWarnings("serial")
public class CListPaginationGetCurrentPageTag extends AbstractJahiaTag {

    private static final org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger(CListPaginationGetCurrentPageTag.class);
    private String valueId;

    public void setValueId(String valueId) {
        this.valueId = valueId;
    }

    public int doStartTag() {
        final CListPaginationTag containerListPaginationTag = (CListPaginationTag) findAncestorWithClass(this,
                CListPaginationTag.class);
        if (containerListPaginationTag == null) {
            JahiaConsole.println("CListPaginationGetCurrentPageTag: doStartTag", "No container list pagination tag found !!");
            return SKIP_BODY;
        }

        final int value = containerListPaginationTag.getCurrentPageIndex();
        if (valueId != null && valueId.length() > 0) {
            pageContext.setAttribute(valueId, value);
        } else {
            try {
                pageContext.getOut().print(value);
            } catch (Exception e) {
                logger.error(e, e);
            }
        }
        return SKIP_BODY;
    }

    public int doEndTag() {
        valueId = null;
        return EVAL_PAGE;
    }
}
