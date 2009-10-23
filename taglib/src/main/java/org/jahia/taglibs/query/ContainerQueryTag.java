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
package org.jahia.taglibs.query;

import java.io.IOException;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

import org.jahia.data.containers.JahiaContainerList;
import org.jahia.taglibs.template.containerlist.ContainerListTag;

/**
 * Created by IntelliJ IDEA.
 * User: hollis
 * Date: 22 oct. 2007
 * Time: 12:47:54
 * To change this template use File | Settings | File Templates.
 */
@SuppressWarnings("serial")
public class ContainerQueryTag extends QueryDefinitionTag  {

    private static org.apache.log4j.Logger logger =
        org.apache.log4j.Logger.getLogger(ContainerQueryTag.class);

    private String queryObjectModelBeanName;

    private String targetContainerListName;

    private String targetContainerListID;

    private JahiaContainerList targetContainerList;

    private String queryBeanID;

    public String getQueryBeanID() {
        return queryBeanID;
    }

    public void setQueryBeanID(String queryBeanID) {
        this.queryBeanID = queryBeanID;
    }

    public String getTargetContainerListName() {
        return targetContainerListName;
    }

    public void setTargetContainerListName(String targetContainerListName) {
        this.targetContainerListName = targetContainerListName;
    }

    public String getTargetContainerListID() {
        return targetContainerListID;
    }

    public void setTargetContainerListID(String targetContainerListID) {
        this.targetContainerListID = targetContainerListID;
    }

    public String getQueryObjectModelBeanName() {
        return queryObjectModelBeanName;
    }

    public void setQueryObjectModelBeanName(String queryObjectModelBeanName) {
        this.queryObjectModelBeanName = queryObjectModelBeanName;
    }

    public int doStartTag ()  throws JspException {
        int eval = super.doStartTag();

        // check if we are inside a ContainerListTag
        ContainerListTag containerListTag = (ContainerListTag)findAncestorWithClass(this, ContainerListTag.class);
        if (containerListTag != null){
            this.targetContainerList = containerListTag.getContainerList();
            if ( this.targetContainerList != null ){
                this.targetContainerListID = String.valueOf(this.targetContainerList.getID());
            }
        }

        return eval;
    }

    // Body is evaluated one time, so just writes it on standard output
    public int doAfterBody () {
        int result = super.doAfterBody();

        if ( this.getQueryObjectModelBeanName() != null && getId() != null) {
            pageContext.removeAttribute(getId(),PageContext.REQUEST_SCOPE);
        }
        try {
            bodyContent.writeOut(bodyContent.getEnclosingWriter());
        } catch (IOException ioe) {
            logger.error("Error:", ioe);
        }
        return result;
    }

    public int doEndTag ()
        throws JspException {

        int result = super.doEndTag();

        // let's reinitialize the tag variables to allow tag object reuse in
        // pooling.
        queryBeanID = null;
        targetContainerList = null;
        queryObjectModelBeanName = null;
        targetContainerListName = null;
        targetContainerListID = null;
        
        return result;
    }
}
