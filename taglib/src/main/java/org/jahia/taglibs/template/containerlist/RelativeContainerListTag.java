/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
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
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.taglibs.template.containerlist;

import javax.servlet.jsp.JspException;

import org.jahia.data.JahiaData;
import org.jahia.data.containers.JahiaContainerList;
import org.jahia.exceptions.JahiaException;


/**
 * Class RelativeContainerListTag : extends class ContainerListTag
 *
 * @author  Jerome Tamiotti
 *
 *
 * @jsp:tag name="relativeContainerList" body-content="tagdependent"
 * description="initializes Jahia in order to display a container list referenced using absolute addressing and declares it if required.
 * <p><attriInfo>This class extends the ContainerListTag class, it therefore inherits all its attributes. The reader should refer
 * to containerList tag's documentation for more details.
 * <p>The term �relative� is a little bit misleading, it does not mean that we are retrieving a special kind of container list,
 *  but rather that we access a container list on another page. With the containerList tag,
 * only the current page container lists may be retrieved, but relativeContainerListTag allows
 * us to retrieve a container list on another page, using relative addressing of a container list.
 * So we pass an the 'levelNb' attribute that specifies how many page levels up from the current page is located the container we are seeking.
 * <p>Example:
 * <p>&lt;content:relativeContainerList name=�addressList� levelNb=�1�&gt; <br>
 * &nbsp;&nbsp;&nbsp; &lt;!-- tags to display container list here --&gt;<br>
 * &lt;/content:relativeContainerList&gt;</p>
 * <p>&nbsp;</p>
 * </attriInfo>"
 *
 */
@SuppressWarnings("serial")
public class RelativeContainerListTag extends ContainerListTag {

    private int levelNb = 0;

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

    /**
     * @jsp:attribute name="size" required="false" rtexprvalue="true"
     * description="the number of elements in the list. Returns the size of the container list.
     * <p><attriInfo>Inherited from containerList tag. This now returns only the size that has been loaded in memory, as opposed to the full -i.e. real- size of the list
     * set in the database. To get the real full size of data set, use @see JahiaContainerList#getFullSize(). The reason for this
     * is due to the introduction of scrollable container lists which load only the set for the view.
     * </attriInfo>"
     */

    /**
     * @jsp:attribute name="levelNb" required="false" rtexprvalue="true"
     * description="the offset from the current page, specifying the number of
     * levels to up down in the tree.
     * <p><attriInfo>The level is therefore dependent of the current page, as opposed to the absoluteContainerList tag. For example,
     * if we have the following path  page1 -> page2 -> page3 (current page) then the levels correspond to:
     * <br>level 3 = Exception returned (doesn't exist)
     * <br>level 2 = page1 (root page ID)
     * <br>level 1 = page2
     * <br>level 0 = page3 (current page)
     * <p>Note Jahia generates an exception if the levelNb points further up the tree than the root page.
     * </attriInfo>"
     */

    public void setLevelNb(int nb) {
        this.levelNb = nb;
    }

    // reads the container list from a container set
    protected JahiaContainerList getContainerList( JahiaData jData, String listName ) throws JahiaException {
        if(listName ==null || "".equals(listName)) return null;
        JahiaContainerList containerList = jData.containers().getRelativeContainerList(listName, this.levelNb, getId());
        if ( containerList != null ){
            containerList.setMaxSize(getMaxSize());
        }
        return containerList;
    }

    public int doEndTag() throws JspException {
        // let's reinitialize the tag variables to allow tag object reuse in
        // pooling.
        super.doEndTag();

        levelNb = 0;
        return EVAL_PAGE;
    }

}
