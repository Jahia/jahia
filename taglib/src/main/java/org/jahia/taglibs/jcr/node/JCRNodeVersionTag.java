/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2016 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.taglibs.jcr.node;

import org.apache.taglibs.standard.tag.common.core.Util;
import org.jahia.services.content.JCRNodeWrapper;

import javax.jcr.RepositoryException;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.TagSupport;

/**
 * 
 * User: toto
 * Date: Dec 3, 2009
 * Time: 7:11:50 PM
 * 
 */
public class JCRNodeVersionTag extends TagSupport {

    private static final long serialVersionUID = -7198867928256587130L;
    private JCRNodeWrapper node;
    private String versionName;
    private String var;
    private int scope = PageContext.PAGE_SCOPE;

    public void setNode(JCRNodeWrapper node) {
        this.node = node;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public void setVar(String var) {
        this.var = var;
    }

    public void setScope(String scope) {
        this.scope = Util.getScope(scope);
    }


    @Override
    public int doStartTag() throws JspException {
        try {
            JCRNodeWrapper version = (JCRNodeWrapper) node.getVersionHistory().getVersion(versionName);
            pageContext.setAttribute(var, version, scope);            
        } catch (RepositoryException e) {
            throw new JspException(e);
        }
        return super.doStartTag();
    }

    @Override
    public int doEndTag() throws JspException {
        node = null;
        versionName = null;
        var = null;
        scope = PageContext.PAGE_SCOPE;
        return super.doEndTag();
    }
}
