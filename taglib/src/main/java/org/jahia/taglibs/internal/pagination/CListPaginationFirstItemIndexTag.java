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

import org.jahia.data.containers.JahiaContainerList;
import org.jahia.data.containers.JahiaContainerListPagination;
import org.jahia.taglibs.AbstractJahiaTag;
import org.jahia.taglibs.template.containerlist.ContainerListTag;

import javax.servlet.jsp.JspWriter;
import java.io.IOException;


/**
 * Return the index of the first listed item ( container ) in the current displayed page ( container list pagination ).
 *
 * @author  NK
 *
 * @jsp:tag name="cListPaginationFirstItemIndex" body-content="empty"
* description="Return the index of the first listed item ( container ) in the current displayed page ( container list pagination ).
*
 * <p><attriInfo>This tag only needs to be enclosed inside the containerList Tag.
* It is part of the 3 tags (<a href='cListPaginationTotalSize.html' target='tagFrame'>content:cListPaginationTotalSize</a>
* and <a href='cListPaginationLastItemIndex.html' target='tagFrame'>content:cListPaginationLastItemIndex</a>)
* used to give some information about the navigation through the paginated Container List.
 *
 * <p>See <a href='cListPagination.html' target='tagFrame'>content:cListPagination</a> for more details.
*
* <p>
* </attriInfo>"
*/
@SuppressWarnings("serial")
public class CListPaginationFirstItemIndexTag extends AbstractJahiaTag {

    private static org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger(CListPaginationFirstItemIndexTag.class);

    public int doStartTag() {
        //JahiaConsole.println("CListPaginationFirstItemIndexTag: doStartTag", "Started");

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
                    index = cPagination.getFirstItemIndex();
                    if ( cPagination.getSize()>0 )
                    {
                        index+=1;
                    }
                } else if ( cList.size()<=0 ) {
                    index = 0;
                } else {
                    index = 1;
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
