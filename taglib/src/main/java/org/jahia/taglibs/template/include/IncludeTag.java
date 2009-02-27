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

package org.jahia.taglibs.template.include;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.PageContext;

import org.apache.taglibs.standard.tag.common.core.ImportSupport;
import org.jahia.data.beans.JahiaBean;
import org.jahia.data.beans.TemplatePathResolverBean;
import org.jahia.data.beans.TemplatePathResolverFactory;
import org.jahia.params.ProcessingContext;
import org.jahia.services.templates.JahiaTemplateManagerService;
import org.jahia.settings.SettingsBean;
import org.jahia.hibernate.manager.SpringContextSingleton;

/**
 * Tag handler for looking up a resource in the template set hierarchy, starting
 * from the current template set root folder, then the parent template set and
 * so on.
 * 
 * <pre>
 * Consider the following example:
 *    
 *    We have:
 *        1) 'Basic templates' set with the root folder 'basic'
 *        2) 'Custom templates v1' set with the root folder 'custom_v1'. This template set extends the 'Basic templates'
 *        3) Our current virtual site uses the 'Custom templates v1' template set
 *    
 *    In one of our templates we make a call to this tag as follows:
 *        ...
 *            &lt;jahia:include page=&quot;common/header.jsp&quot;&gt;
 *              &lt;jahia:param name=&quot;a&quot; value=&quot;1&quot;/&gt;
 *              &lt;jahia:param name=&quot;b&quot; value=&quot;test&quot;/&gt;
 *            &lt;/jahia:include&gt;
 *        ...
 *    
 *    The result will be as follows, if:
 *        1) the 'header.jsp' is found under custom_v1/common/header.jsp
 *                
 *            &lt;jsp:include page=&quot;/templates/custom_v1/common/header.jsp&quot;&gt;
 *              &lt;jsp:param name=&quot;a&quot; value=&quot;1&quot;/&gt;
 *              &lt;jsp:param name=&quot;b&quot; value=&quot;test&quot;/&gt;
 *            &lt;/jsp:include&gt;
 * 
 *        2) the 'header.jsp' is found under basic/common/header.jsp  (in the parent template set)
 * 
 *            &lt;jsp:include page=&quot;/templates/basic/common/header.jsp&quot;&gt;
 *              &lt;jsp:param name=&quot;a&quot; value=&quot;1&quot;/&gt;
 *              &lt;jsp:param name=&quot;b&quot; value=&quot;test&quot;/&gt;
 *            &lt;/jsp:include&gt;
 * 
 *        3) the 'header.jsp' is not found --&gt; the path remains unchanged
 * 
 *            &lt;jsp:include page=&quot;common/header.jsp&quot;&gt;
 *              &lt;jsp:param name=&quot;a&quot; value=&quot;1&quot;/&gt;
 *              &lt;jsp:param name=&quot;b&quot; value=&quot;test&quot;/&gt;
 *            &lt;/jsp:include&gt;
 * </pre>
 * 
 * @author Sergiy Shyrkov
 * @see JahiaTemplateManagerService
 */
public class IncludeTag extends ImportSupport {

    /**
     * Adds a parameters to the list.
     * 
     * @param name
     *            the name of the parameter
     * @param value
     *            parameter value
     */
    public void addParameter(String name, String value) {
        try {
            super.addParameter(name, URLEncoder.encode(value, pageContext
                    .getResponse().getCharacterEncoding() != null ? pageContext
                    .getResponse().getCharacterEncoding() : SettingsBean.getInstance()
                    .getDefaultResponseBodyEncoding()));
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException(e);
        }

    }

    protected String getResolvedPath(String page) throws JspTagException {
        String lookupPath = page;
        int questionMarkPosition = page.indexOf('?');
        // path includes parameters?
        if (questionMarkPosition != -1) {
            // get path to '?'
            lookupPath = page.substring(0, questionMarkPosition);
        }
        String resolvedPath = getTemplatePathResolver().lookup(lookupPath);
        if (null == resolvedPath) {
            throw new JspTagException("Unable to resolve the specified path '"
                    + lookupPath + "' in the template set hierarchy");
        }
//        resolvedPath = getTemplatePathResolver().skinnify(pageContext.getRequest(), resolvedPath);
        return questionMarkPosition != -1 ? resolvedPath
                + page.substring(questionMarkPosition) : resolvedPath;
    }

    protected TemplatePathResolverBean getTemplatePathResolver()
            throws JspTagException {
        TemplatePathResolverBean resolverBean = null;
        JahiaBean jahiaBean = (JahiaBean) pageContext.getAttribute("jahia",
                PageContext.REQUEST_SCOPE);
        if (jahiaBean != null) {
            resolverBean = jahiaBean.getIncludes().getTemplatePath();
        } else {
            ProcessingContext ctx = (ProcessingContext) pageContext
                    .getAttribute("org.jahia.data.JahiaParams",
                            PageContext.REQUEST_SCOPE);
            if (ctx != null && ctx.getSite() != null) {
                TemplatePathResolverFactory factory = (TemplatePathResolverFactory) SpringContextSingleton.getInstance().getContext().getBean("TemplatePathResolverFactory");
                resolverBean = factory.getTemplatePathResolver(ctx); 
            }
        }

        if (resolverBean == null) {
            throw new JspTagException(
                    "Unable to find any Jahia site related information");
        }

        return resolverBean;
    }

    public void setPage(String page) throws JspTagException {
        this.url = getResolvedPath(page);
    }
}
