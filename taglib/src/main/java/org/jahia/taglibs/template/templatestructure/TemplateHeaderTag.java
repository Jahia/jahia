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
package org.jahia.taglibs.template.templatestructure;

import org.apache.log4j.Logger;
import org.jahia.data.JahiaData;
import org.jahia.exceptions.JahiaException;
import org.jahia.services.pages.JahiaPage;
import org.jahia.services.pages.PageProperty;
import org.jahia.taglibs.AbstractJahiaTag;
import org.jahia.ajax.gwt.utils.GWTInitializer;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.Tag;
import java.io.IOException;

/**
 * <p>Title: Defines the head part of a jahia template</p>
 * <p>Description: This tag simply delimits what is the head part of a Jahia template.
 * It generates links to mandatory Jahia resources such as javascript or CSS files. All templates MUST have a head part.
 * It should be used together with the template and templateBody tags.</p>
 * <p>Copyright: Copyright (c) 1999-2009</p>
 * <p>Company: Jahia Ltd</p>
 *
 * @author Xavier Lawrence
 * @version 1.0
 * @jsp:tag name="templateHead" body-content="JSP" description="Defines the head part of a jahia template."
 * <p/>
 * <p><attriInfo>This tag simply delimits what is the head part of a Jahia template.
 * It generates links to mandatory Jahia resources such as javascript or CSS files. All templates MUST have a head part.
 * It should be used together with the template and templateBody tags.
 * <p/>
 * <p><b>Example :</b>
 * <p/>
 * <%@ include file="common/declarations.jspf" %>
 * &nbsp;&nbsp;&nbsp;&nbsp; &lt;template:template&gt;
 * &nbsp;&nbsp;&nbsp;&nbsp;    &lt;template:templateHead&gt;
 * &nbsp;&nbsp;&nbsp;&nbsp;       &lt;%@ include file="common/template-head.jspf" %&gt;
 * &nbsp;&nbsp;&nbsp;&nbsp;       &lt;utility:applicationResources/&gt;
 * &nbsp;&nbsp;&nbsp;&nbsp;   &lt;/template:templateHead&gt;
 * &nbsp;&nbsp;&nbsp;&nbsp;   &lt;template:templateBody&gt;
 * &nbsp;&nbsp;&nbsp;&nbsp;       &lt;div id="header"&gt;
 * &nbsp;&nbsp;&nbsp;&nbsp;           &lt;template:include page="common/header.jsp"/&gt;
 * &nbsp;&nbsp;&nbsp;&nbsp;       &lt;/div&gt;
 * &nbsp;&nbsp;&nbsp;&nbsp;       &lt;div id="pagecontent"&gt;
 * &nbsp;&nbsp;&nbsp;&nbsp;           &lt;div class="content3cols"&gt;
 * &nbsp;&nbsp;&nbsp;&nbsp;               &lt;div id="columnA"&gt;
 * &nbsp;&nbsp;&nbsp;&nbsp;                   &lt;template:include page="common/loginForm.jsp"/&gt;
 * &nbsp;&nbsp;&nbsp;&nbsp;
 * &nbsp;&nbsp;&nbsp;&nbsp;                   &lt;template:include page="common/box/box.jsp"&gt;
 * &nbsp;&nbsp;&nbsp;&nbsp;                       &lt;template:param name="name" value="columnA_box"/&gt;
 * &nbsp;&nbsp;&nbsp;&nbsp;                   &lt;/template:include&gt;
 * &nbsp;&nbsp;&nbsp;&nbsp;               &lt;/div&gt;
 * <p/>
 * &nbsp;&nbsp;&nbsp;&nbsp;               &lt;div id="columnC"&gt;
 * &nbsp;&nbsp;&nbsp;&nbsp;                   &lt;template:include page="common/searchForm.jsp"/&gt;
 * <p/>
 * &nbsp;&nbsp;&nbsp;&nbsp;                   &lt;!-- in HomePage we display site main properties --&gt;
 * &nbsp;&nbsp;&nbsp;&nbsp;                   &lt;div class="properties"&gt;
 * &nbsp;&nbsp;&nbsp;&nbsp;                       &lt;utility:displaySiteProperties/&gt;
 * &nbsp;&nbsp;&nbsp;&nbsp;                   &lt;/div&gt;
 * &nbsp;&nbsp;&nbsp;&nbsp;                   &lt;template:include page="common/box/box.jsp"&gt;
 * &nbsp;&nbsp;&nbsp;&nbsp;                       &lt;template:param name="name" value="columnC_box"/&gt;
 * &nbsp;&nbsp;&nbsp;&nbsp;                   &lt;/template:include&gt;
 * &nbsp;&nbsp;&nbsp;&nbsp;               &lt;/div&gt;
 * <p/>
 * &nbsp;&nbsp;&nbsp;&nbsp;               &lt;div id="columnB"&gt;
 * &nbsp;&nbsp;&nbsp;&nbsp;                   &lt;!--news--&gt;
 * &nbsp;&nbsp;&nbsp;&nbsp;                   &lt;template:include page="common/news/newsDisplay.jsp"/&gt;
 * <p/>
 * &nbsp;&nbsp;&nbsp;&nbsp;                   &lt;div&gt;
 * &nbsp;&nbsp;&nbsp;&nbsp;                       &lt;a class="bottomanchor" href="#pagetop"&gt;&lt;utility:resourceBundle
 * &nbsp;&nbsp;&nbsp;&nbsp;                               resourceName='pageTop' defaultValue="Page Top"/&gt;&lt;/a&gt;
 * &nbsp;&nbsp;&nbsp;&nbsp;                   &lt;/div&gt;
 * &nbsp;&nbsp;&nbsp;&nbsp;               &lt;/div&gt;
 * <p/>
 * &nbsp;&nbsp;&nbsp;&nbsp;               &lt;br class="clear"/&gt;
 * &nbsp;&nbsp;&nbsp;&nbsp;           &lt;/div&gt;
 * &nbsp;&nbsp;&nbsp;&nbsp;           &lt;!-- end of content3cols section --&gt;
 * &nbsp;&nbsp;&nbsp;&nbsp;       &lt;/div&gt;
 * &nbsp;&nbsp;&nbsp;&nbsp;       &lt;!-- end of pagecontent section--&gt;
 * <p/>
 * &nbsp;&nbsp;&nbsp;&nbsp;       &lt;div id="footer"&gt;
 * &nbsp;&nbsp;&nbsp;&nbsp;           &lt;template:include page="common/footer.jsp"/&gt;
 * &nbsp;&nbsp;&nbsp;&nbsp;       &lt;/div&gt;
 * &nbsp;&nbsp;&nbsp;&nbsp;   &lt;/template:templateBody&gt;
 * &nbsp;&nbsp;&nbsp;&nbsp; &lt;/template:template&gt;
 * </attriInfo>
 */
@SuppressWarnings("serial")
public class TemplateHeaderTag extends AbstractJahiaTag {

    private final static Logger logger = Logger.getLogger(TemplateHeaderTag.class);

    private boolean gwtForGuest;
    
    private String title;

    public int doStartTag() throws JspException {
        // retrieve parameters
        ServletRequest request = pageContext.getRequest();
        JahiaData jData = getJahiaData();
        JahiaPage page = jData.page();
        String pageTitle = title;
        if (pageTitle == null && page != null) {
            pageTitle = page.getTitle();
            if (pageTitle == null) {
                PageProperty prop = null;
                try {
                    prop = page.getPageLocalProperty(PageProperty.PAGE_URL_KEY_PROPNAME);
                } catch (JahiaException e) {
                    logger.error("Cannot access current page properties", e);
                }
                if (prop != null) {
                    pageTitle = prop.getValue();
                } else {
                    pageTitle = String.valueOf(page.getID());
                }
            }
        }

        // write output to StringBuffer
        StringBuilder buf = new StringBuilder("<head>\n");
        buf.append("<meta http-equiv=\"X-UA-Compatible\" content=\"IE=EmulateIE7\" />\n");  // should be fixed in GWT 1.6
        // check the gwtForGuest attribute from parent tag
        Tag parent = getParent();
        gwtForGuest = false;
        if (parent instanceof TemplateTag) {
            gwtForGuest = ((TemplateTag) parent).enableGwtForGuest();
        }

        if (isLogged() || gwtForGuest) {
            buf.append(GWTInitializer.getInitString(pageContext)).append("\n");
        }

        buf.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"");
        buf.append(((HttpServletRequest) request).getContextPath());
        buf.append("/css/languageSwitchingLinks.css\"/>");
        final String cacheInfo = request.getParameter("cacheinfo");
        boolean displayCacheInfo = cacheInfo!=null?Boolean.valueOf(cacheInfo):false;
        if(displayCacheInfo) {
            buf.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"");
            buf.append(((HttpServletRequest) request).getContextPath());
            buf.append("/css/cacheDebugInfo.css\"/>");
        }
        buf.append(DefaultIncludeProvider.getJSToolsImportCss((HttpServletRequest) pageContext.getRequest()));
        if (pageTitle != null) {
        	buf.append("\t<title>").append(pageTitle).append("</title>\n");
        }

        // write StringBuffer to JspWriter
        try {
            pageContext.getOut().println(buf.toString());
        } catch (IOException e) {
            throw new JspTagException(e);
        }
        return EVAL_BODY_INCLUDE;
    }

    public int doEndTag() throws JspException {
        try {
            if (isLogged() || gwtForGuest) {
                pageContext.getOut().append(DefaultIncludeProvider.getJSToolsImportJavaScript(getJahiaData()));
            }
            pageContext.getOut().println("</head>");
        } catch (IOException e) {
            throw new JspTagException(e);
        }
        return EVAL_PAGE;
    }

	public void setTitle(String title) {
    	this.title = title;
    }

	@Override
    protected void resetState() {
		title = null;
	    super.resetState();
    }
	
}
