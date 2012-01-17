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

package org.jahia.taglibs.template.include;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.render.Resource;

import javax.jcr.RepositoryException;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

/**
 * 
 *
 * @author : rincevent
 * @since JAHIA 6.5
 *        Created : 15 juil. 2010
 */
public class AddCacheDependencyTag extends TagSupport {
    private transient static Logger logger = org.slf4j.LoggerFactory.getLogger(AddCacheDependencyTag.class);
    protected JCRNodeWrapper node;
    protected String stringDependency;
    protected String flushOnPathMatchingRegexp;
    public void setNode(JCRNodeWrapper node) {
        this.node = node;
    }

    public void setUuid(final String uuid) {
        try {
            Resource resource = (Resource) pageContext.getRequest().getAttribute("currentResource");
            setPath(resource.getNode().getSession().getNodeByIdentifier(uuid).getPath());
        } catch (RepositoryException e) {
            this.stringDependency = uuid;
        }
    }

    public void setPath(String path) {
        if(path.endsWith("/")) {
            this.stringDependency = StringUtils.substringBeforeLast(path,"/");
        } else {
            this.stringDependency = path;
        }
    }

    public void setFlushOnPathMatchingRegexp(String flushOnPathMatchingRegexp) {
        this.flushOnPathMatchingRegexp = flushOnPathMatchingRegexp;
    }

    /**
     * Default processing of the end tag returning EVAL_PAGE.
     *
     * @return EVAL_PAGE
     * @throws javax.servlet.jsp.JspException if an error occurs while processing this tag
     * @see javax.servlet.jsp.tagext.Tag#doEndTag()
     */
    @Override
    public int doEndTag() throws JspException {
        Resource resource = (Resource) pageContext.getRequest().getAttribute("currentResource");
        if (node != null) {
            resource.getDependencies().add(node.getCanonicalPath());
        } else if (stringDependency != null) {
            resource.getDependencies().add(stringDependency);
        } else if(flushOnPathMatchingRegexp != null) {
            resource.getRegexpDependencies().add(flushOnPathMatchingRegexp);
        }
        node = null;
        return super.doEndTag();    //To change body of overridden methods use File | Settings | File Templates.
    }
}
