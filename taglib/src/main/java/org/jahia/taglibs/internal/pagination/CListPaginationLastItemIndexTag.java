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

import java.io.IOException;

import javax.servlet.jsp.JspWriter;

import org.jahia.data.containers.JahiaContainerList;
import org.jahia.data.containers.JahiaContainerListPagination;
import org.jahia.taglibs.AbstractJahiaTag;
import org.jahia.taglibs.template.containerlist.ContainerListTag;


/**
 * Return the index of the first listed item ( container ) in the current displayed page ( container list pagination ).
 *
 * @author NK
 *
 * @jsp:tag name="cListPaginationLastItemIndex" body-content="empty"
* description="Return the index of the first listed item ( container ) in the current displayed page ( container list pagination ).
 *
 * <p><attriInfo>This tag only needs to be enclosed inside the containerList Tag.
* It is part of the 3 tags (<a href='cListPaginationTotalSize.html' target='tagFrame'>content:cListPaginationTotalSize</a>
* and <a href='cListPaginationFirstItemIndex.html' target='tagFrame'>content:cListPaginationFirstItemIndex</a>)
* used to give some information about the navigation through the paginated Container List.
 *
 * <p>See <a href='cListPagination.html' target='tagFrame'>content:cListPagination</a> for more details.
 *
* </attriInfo>"
 */
@SuppressWarnings("serial")
public class CListPaginationLastItemIndexTag extends AbstractJahiaTag {

    private static org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger(CListPaginationLastItemIndexTag.class);

    public int doStartTag() {
        //JahiaConsole.println("CListPaginationLastItemIndexTag: doStartTag", "Started");

        int index = 0;
        // gets the enclosing tag ContainerListTag
        ContainerListTag containerListTag = (ContainerListTag) findAncestorWithClass(this, ContainerListTag.class);
        if (containerListTag != null) {
            JahiaContainerList cList = containerListTag.getContainerList();
            if ( cList != null )
            {
                JahiaContainerListPagination cPagination = cList.getCtnListPagination();
                if ( cPagination != null && cPagination.isValid() )
                {
                    index = cPagination.getLastItemIndex()+1;
                } else if ( cList.size()==0 ) {
                    index = 0;
                } else {
                    index = cList.size();
                }
                try {
                    JspWriter out = pageContext.getOut();
                    out.print(index);
                } catch (IOException ioe) {
                    logger.error(ioe.toString(), ioe);
                }
            }
        }
        return SKIP_BODY;
    }

}
