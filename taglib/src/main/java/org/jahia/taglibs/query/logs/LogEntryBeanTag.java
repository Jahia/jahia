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

package org.jahia.taglibs.query.logs;

import org.jahia.content.JahiaObject;
import org.jahia.data.JahiaData;
import org.jahia.data.beans.AbstractJahiaObjectBean;
import org.jahia.data.beans.ContentBean;
import org.jahia.services.audit.display.LogEntryItem;

import javax.servlet.ServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.BodyTagSupport;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: hollis
 * Date: 14 d�c. 2007
 * Time: 17:23:15
 * To change this template use File | Settings | File Templates.
 */
@SuppressWarnings("serial")
public class LogEntryBeanTag extends BodyTagSupport {

    private String beanId;

    public int doStartTag() {
        try {
            if (getId() != null) {
                LogEntryItem logItem = (LogEntryItem)pageContext.getAttribute(getId());
                if ( logItem != null ){
                    ServletRequest request = pageContext.getRequest();
                    JahiaData jData = (JahiaData) request.getAttribute("org.jahia.data.JahiaData");
                    JahiaObject jahiaObject = JahiaObject.getInstance(logItem.getObjectKey());
                    if ( jahiaObject != null ){
                        if (getBeanId()!=null){
                            AbstractJahiaObjectBean contentBean = ContentBean.getInstance(jahiaObject,
                                    jData.getProcessingContext());
                            if(contentBean != null){
                                pageContext.setAttribute(getBeanId(),contentBean);
                            } else {
                                pageContext.removeAttribute(getBeanId(), PageContext.PAGE_SCOPE);
                            }
                        }
                    }
                }
            }
        } catch ( Exception t ){

        }
        return EVAL_BODY_BUFFERED;
    }

    public int doAfterBody() throws JspException {
        try {
            getBodyContent().writeOut(getPreviousOut());
        } catch (IOException ioe) {
            throw new JspTagException();
        }
        return SKIP_BODY;
    }

    public int doEndTag() throws JspException {
        id = null;
        beanId = null;
        return EVAL_PAGE;
    }

    public String getBeanId() {
        return beanId;
    }

    public void setBeanId(String beanId) {
        this.beanId = beanId;
    }

}
