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
package org.jahia.taglibs.jcr.node;

import org.jahia.services.content.JCRStoreService;
import org.jahia.services.content.JCRNodeReadOnlyDecorator;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.data.JahiaData;
import org.apache.log4j.Logger;

import javax.servlet.jsp.tagext.TagSupport;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.ServletRequest;
import javax.jcr.RepositoryException;

/**
 * Tag exposing a JCR node.
 * <p/>
 * User: romain
 * Date: 27 mai 2009
 * Time: 14:06:08
 */
public class JCRNodeTag extends TagSupport {

    private final static Logger logger = Logger.getLogger(JCRNodeTag.class);

    private String name;
    private String path;
    private int scope = PageContext.PAGE_SCOPE;

    private JCRNodeReadOnlyDecorator node;

    public void setName(String name) {
        this.name = name;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setScope(int scope) {
        this.scope = scope;
    }

    public JCRNodeReadOnlyDecorator getFile() {
        return node;
    }

    public int doStartTag() throws JspException {
        ServletRequest request = pageContext.getRequest();
        JahiaData jData = (JahiaData) request.getAttribute("org.jahia.data.JahiaData");
        if (jData != null) {
            JahiaUser user = jData.getProcessingContext().getUser();
            try {
                JCRNodeWrapper n = JCRStoreService.getInstance().checkExistence(path, user);
                if (n != null) {
                    node = new JCRNodeReadOnlyDecorator(JCRStoreService.getInstance().getThreadSession(user).getNode(path));
                    pageContext.setAttribute(name, node, scope);
                } else {
                    logger.error("The path '" + path + "' does not exist");
                }
            } catch (RepositoryException e) {
                logger.error("Could not retrieve JCR node using path '" + path + "'", e);
            }
        } else {
            logger.error("JahiaData is null");
        }
        return EVAL_BODY_INCLUDE;
    }

    public int doEndTag() throws JspException {
        path = null;
        node = null;
        scope = PageContext.PAGE_SCOPE;
        return EVAL_PAGE;
    }

}
