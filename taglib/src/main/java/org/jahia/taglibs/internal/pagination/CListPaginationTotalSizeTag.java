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
import org.jahia.taglibs.AbstractJahiaTag;
import org.jahia.taglibs.template.containerlist.ContainerListTag;

/**
 * Return the total number of containers of the enclosing container list.
 * This is the size returned by containerlist.getQueryContainerList().getFullSize() , not containerlist.getSize()
 *
 * @author  NK
 *
 * @jsp:tag name="cListPaginationTotalSize" body-content="empty"
 * description="Returns the total number of containers of the enclosing container list.
 *
 * <p><attriInfo>This tag only needs to be enclosed inside the containerList Tag.
 * It is part of the 3 tags (<a href='cListPaginationFirstItemIndex.html' target='tagFrame'>content:cListPaginationFirstItemIndex</a>
 * and <a href='cListPaginationFirstItemIndex.html' target='tagFrame'>content:cListPaginationFirstItemIndex</a>)
 * used to give some information about the navigation through the paginated Container List.
 *
 * <p>Note that this is the size returned by containerlist.getQueryContainerList().getFullSize() ,
 * not containerlist.getSize()
 *
 * <p>See <a href='cListPagination.html' target='tagFrame'>content:cListPagination</a> for more details.
 *
 * <p><b>Example :</b>
 * <p>
 *
 &lt;content:containerList name=\"directoryPeopleContainer\"&gt; <br>
 &nbsp;&nbsp;    ... <br>
 &nbsp;&nbsp;    [&lt;content:cListPaginationFirstItemIndex /&gt; - &lt;content:cListPaginationLastItemIndex /&gt;]  <br>
 &nbsp;&nbsp;    of &lt;content:cListPaginationTotalSize /&gt; <br>
 &nbsp;&nbsp;    ... <br>
 &lt;/content:containerList&gt; <br>
 *
 *
 * <p>generates the following HTML:
 *
 * <p>[1 - 4] of 4
 *

 *
 * </attriInfo>"
 */
@SuppressWarnings("serial")
public class CListPaginationTotalSizeTag extends AbstractJahiaTag {

    private static org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger(CListPaginationTotalSizeTag.class);

    public int doStartTag() {
        //JahiaConsole.println("CListpaginationTotalSizeTag: doStartTag", "Started");

        int size = 0;
        // gets the enclosing tag ContainerListTag
        ContainerListTag containerListTag = (ContainerListTag) findAncestorWithClass(this, ContainerListTag.class);
        if (containerListTag != null) {
            JahiaContainerList cList = containerListTag.getContainerList();
            if ( cList != null )
            {
                size = cList.getFullSize();
                //JahiaConsole.println("CListPaginationTotalSizeTag: doStartTag", "Full Size : " + size);
                try {
                    JspWriter out = pageContext.getOut();
                    out.print(size);
                } catch (IOException ioe) {
                    logger.error(ioe.toString(), ioe);
                }
            }
        }
        return SKIP_BODY;
    }

}
