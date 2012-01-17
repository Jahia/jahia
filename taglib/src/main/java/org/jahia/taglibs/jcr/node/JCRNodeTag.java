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
import org.apache.commons.lang.*;
import org.apache.taglibs.standard.tag.common.core.Util;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.render.Resource;
import org.jahia.taglibs.jcr.AbstractJCRTag;

import javax.jcr.ItemNotFoundException;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

/**
 * Tag exposing a JCR node.
 * <p/>
 * User: romain
 * Date: 27 mai 2009
 * Time: 14:06:08
 */
public class JCRNodeTag extends AbstractJCRTag {

    private static final long serialVersionUID = 5546424686123575512L;

    private final static Logger logger = org.slf4j.LoggerFactory.getLogger(JCRNodeTag.class);

    private String var;
    private String path;
    private int scope = PageContext.PAGE_SCOPE;
    private String uuid;

    public void setVar(String var) {
        this.var = var;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setScope(String scope) {
        this.scope = Util.getScope(scope);
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public int doStartTag() throws JspException {
        Resource currentResource = getCurrentResource();
        try {
            JCRNodeWrapper node;
            if (uuid != null) {
                node = getJCRSession().getNodeByUUID(uuid);
            } else {
                if (path.startsWith("/")) {
                    node = getJCRSession().getNode(path);
                } else {
                    node = currentResource.getNode();
                    if (!StringUtils.isEmpty(path)) {
                        node = node.getNode(path);
                    }
                }
            }
            pageContext.setAttribute(var, node, scope);
        } catch (PathNotFoundException e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Item not found '" + path + "'", e);
            }
        } catch (ItemNotFoundException e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Item not found '" + path + "'", e);
            }
        } catch (RepositoryException e) {
            logger.error("Could not retrieve JCR node using path '" + path + "'", e);
        }
        return EVAL_BODY_INCLUDE;
    }

    public int doEndTag() throws JspException {
        resetState();
        return EVAL_PAGE;
    }
    
    @Override
    protected void resetState() {
        path = null;
        scope = PageContext.PAGE_SCOPE;
        var = null;
        uuid = null;
        super.resetState();
    }
}
