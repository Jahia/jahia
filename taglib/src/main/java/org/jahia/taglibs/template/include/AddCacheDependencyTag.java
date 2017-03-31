/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2017 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.jahia.taglibs.template.include;

import org.apache.commons.lang.StringUtils;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.render.Resource;

import javax.jcr.RepositoryException;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

/**
 * @author rincevent
 * @since JAHIA 6.5
 *        Created : 15 juil. 2010
 */
public class AddCacheDependencyTag extends TagSupport {
    private static final long serialVersionUID = 573385038732883661L;
    protected JCRNodeWrapper node;
    protected String stringDependency;
    protected String flushOnPathMatchingRegexp;
    public void setNode(JCRNodeWrapper node) {
        this.node = node;
    }

    public void setUuid(final String uuid) {
        if (!StringUtils.isEmpty(uuid)) {
            try {
                Resource resource = (Resource) pageContext.getRequest().getAttribute("currentResource");
                setPath(resource.getNode().getSession().getNodeByIdentifier(uuid).getPath());
            } catch (RepositoryException e) {
                this.stringDependency = uuid;
            }
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
        addDependency(resource);
        resource = (Resource) pageContext.getRequest().getAttribute("optionResource");
        if (resource != null) {
            addDependency(resource);
        }
        node = null;
        stringDependency = null;
        flushOnPathMatchingRegexp = null;
        return super.doEndTag();    
    }

    private void addDependency(Resource resource) {
        if (node != null) {
            resource.getDependencies().add(node.getCanonicalPath());
        } else if (stringDependency != null) {
            resource.getDependencies().add(stringDependency);
        } else if(flushOnPathMatchingRegexp != null) {
            resource.getRegexpDependencies().add(flushOnPathMatchingRegexp);
        }
    }
}
