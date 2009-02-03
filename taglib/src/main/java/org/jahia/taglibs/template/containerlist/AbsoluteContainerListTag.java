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

package org.jahia.taglibs.template.containerlist;

import javax.servlet.jsp.JspException;

import org.apache.commons.lang.StringUtils;
import org.jahia.data.JahiaData;
import org.jahia.data.containers.JahiaContainerList;
import org.jahia.exceptions.JahiaException;

/**
 * Class AbsoluteContainerListTag : extends class ContainerListTag
 *
 * An absolute container list is declared on a page and can be used on any page
 *
 * @author  Jerome Tamiotti
 *
 * @jsp:tag name="absoluteContainerList" body-content="tagdependent"
 * description="initializes Jahia in order to display a container list referenced using absolute addressing and declares it if required.
 * <p><attriInfo>This class extends the ContainerListTag class, it therefore inherits all its attributes. Therefore
 * The reader should refer to containerList tag's documentation for more details.
 * <p>The term �absolute� is a little bit misleading, it does not mean that we are retrieving a special kind of container list,
 *  but rather that we access a container list on another page. With the containerList tag,
 * only the current page container lists may be retrieved, but absoluteContainerList allows
 * us to retrieve a container list on another page, using absolute addressing of a container list.
 * So we pass an the 'pageID' attribute that contains the page ID for which we want to display
 * the current children in the current recursive pass.
 * <p>Example:
 * <p>&lt;content:absoluteContainerList name=�addressList� pageId=�3�&gt; <br>
 * &nbsp;&nbsp;&nbsp; &lt;!-- tags to display container list here --&gt; <br>
 * &lt;/content:absoluteContainerList&gt;</p>
* </attriInfo>"
 */
public class AbsoluteContainerListTag extends ContainerListTag {

    private int pageId = -1;
    private int pageLevel = -1;
    private String pageKey; 

    /**
     * @jsp:attribute name="id" required="false" rtexprvalue="true"
     * description="id attribute for this tag.
     * <p><attriInfo>Inherited from javax.servlet.jsp.tagext.TagSupport</attriInfo>"
     */

    /**
     * @jsp:attribute name="name" required="true" rtexprvalue="true"
     * description="the name of the list.
     * <p><attriInfo>Inherited from containerList tag. This is an identifier that must be unique within the current parent object (page or containerlist) CHECK</i> [To Be Completed] </i>,
     * Usually it is good practice to avoid using spaces in this name, which makes manipulation easier in general.
     *  @see ContainerListTag
     * </attriInfo>"
     */

    /**
     * @jsp:attribute name="size" required="false" rtexprvalue="true"
     * description="the number of elements in the list. Returns the size of the container list.
     * <p><attriInfo>Inherited from containerList tag. This now returns only the size that has been loaded in memory, as opposed to the full -i.e. real- size of the list
     * set in the database. To get the real full size of data set, use @see JahiaContainerList#getFullSize(). The reason for this
     * is due to the introduction of scrollable container lists which load only the set for the view.
     * </attriInfo>"
     */


    /**
     * @jsp:attribute name="pageID" required="false" rtexprvalue="true"
     * description="the Page ID of the page which contains the desired container.
     * <p><attriInfo>
     * </attriInfo>"
     */

    public void setPageId(String pageId) {
        if (StringUtils.isNotEmpty(pageId)) {
            try {
                this.pageId = Integer.parseInt(pageId);
            } catch (NumberFormatException nfe) {
                throw new IllegalArgumentException("The given page id '"
                        + pageId + "'is not a number", nfe);
            }
        }
    }

   /**
    * @jsp:attribute name="pageLevel" required="false" rtexprvalue="true"
    * description="the offset from the root page, specifying the number of
    * levels to go down in the tree.
    * <p><attriInfo>The level is therefore independent of the current page, for this use the relativeContainerList tag. For example,
    * if we have the following path  page1 -> page2 -> page3 (current page) then the levels correspond to:
    * <br>level 0 = Exception returned (doesn't exist)
    * <br>level 1 = page1 (root page ID)
    * <br>level 2 = page2
    * <br>level 3 = page3
    * <br>level 4 = Exception returned (doesn't exist)
    * </attriInfo>"
     */

    public void setPageLevel(String pageLevel) {
        if (StringUtils.isNotEmpty(pageLevel)) {
            try {
                this.pageLevel = Integer.parseInt(pageLevel);
            } catch (NumberFormatException nfe) {
                throw new IllegalArgumentException("The given page level '"
                        + pageId + "'is not a number", nfe);
            }
        }
    }


    // reads the container list from a container set
    protected JahiaContainerList getContainerList( JahiaData jData, String listName ) throws JahiaException {
        JahiaContainerList containerList = null;
        int id = pageId;
        if (id == -1 && pageLevel != -1) {
            id = jData.gui().getLevelID(pageLevel);
        }
        if (id == -1  && pageKey != null) {
            containerList = jData.containers().getAbsoluteContainerList(listName, pageKey);
        } else {
            containerList = jData.containers().getAbsoluteContainerList(listName, id);
        }
        if ( containerList != null ){
            containerList.setMaxSize(this.getMaxSize());
        }
        return containerList;
    }

    public int doEndTag() throws JspException {
        // let's reinitialize the tag variables to allow tag object reuse in
        // pooling.
        super.doEndTag();
        pageId = -1;
        pageLevel = -1;
        pageKey = null;
        return EVAL_PAGE;
    }

    public void setPageKey(String pageKey) {
        if (pageKey != null && pageKey.length() == 0) {
            throw new IllegalArgumentException("Page key cannot be empty");
        }
        this.pageKey = pageKey;
    }
}
