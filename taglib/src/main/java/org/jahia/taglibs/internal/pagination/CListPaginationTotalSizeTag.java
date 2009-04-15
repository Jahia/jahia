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
