/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

package org.jahia.taglibs.jcr.node;

import org.slf4j.Logger;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.taglibs.jcr.AbstractJCRTag;
import org.jahia.params.ParamBean;
import org.jahia.params.ProcessingContext;

import javax.servlet.jsp.JspException;
import javax.jcr.RepositoryException;
import javax.jcr.PathNotFoundException;
import java.io.IOException;

/**
 * Generates a download URL for a given JCR node using the provided path.
 * This behaves like the standard a tag (surrounding tag).
 *
 * User: romain
 * Date: 27 mai 2009
 * Time: 16:14:28
 */
public class JCRNodeLinkTag extends AbstractJCRTag {

    private static final long serialVersionUID = -8322805743385450335L;

    private final static Logger logger = org.slf4j.LoggerFactory.getLogger(JCRNodeTag.class);

    private String path =  null;
    private boolean absolute = false;
    private JCRNodeWrapper node = null;

    public void setPath(String path) {
        this.path = path;
    }

    public void setAbsolute(boolean absolute) {
        this.absolute = absolute;
    }

    public int doStartTag() throws JspException {
        try {
            node = getJCRSession().getNode(path);
            if (node.isFile()) {
                StringBuilder link = new StringBuilder("<a href=\"");
                if (absolute) {
                    link.append(node.getAbsoluteUrl(pageContext.getRequest()));
                } else {
                    link.append(node.getUrl());
                }
                link.append("\">");
                pageContext.getOut().print(link.toString());
            } else {
                logger.warn("The path '" + path + "' is not a file");
            }
        } catch (PathNotFoundException e) {
            logger.error("The path '" + path + "' does not exist");
        } catch (RepositoryException e) {
            logger.error("Could not retrieve JCR node using path '" + path + "'", e);
        } catch (IOException e) {
            logger.error(e.toString(), e);
        }
        return EVAL_BODY_INCLUDE;
    }

    public int doEndTag() throws JspException {
        if (node != null && node.isFile()) {
            try {
                pageContext.getOut().print("</a>");
            } catch (IOException e) {
                logger.error(e.toString(), e);
            }
        }
        resetState();
        return EVAL_PAGE;
    }

    @Override
    protected void resetState() {
        absolute = false;
        node = null;
        path = null;
        super.resetState();
    }
}
