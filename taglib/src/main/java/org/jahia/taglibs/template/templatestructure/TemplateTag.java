/**
 * Jahia Enterprise Edition v6
 *
 * Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.
 *
 * Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
 * with Document Management and Portal features.
 *
 * The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED.
 *
 * Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
 * you and Jahia (Jahia Sustainable Enterprise License - JSEL).
 *
 * If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.taglibs.template.templatestructure;

import org.jahia.taglibs.AbstractJahiaTag;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;

/**
 * <p>Title: Defines a jahia template</p>
 * <p>Description: This tag simply defines that the JSP file containing it should be considered as a
 * Jahia template. It generates the XML doctype and starting &lt;html&gt; tag of the XHTML page that will be
 * sent to the browser. It should be used together with the templateHead and templateBody tags, since all templates
 * MUST have a head and a body.</p>
 * <p>Copyright: Copyright (c) 1999-2009</p>
 * <p>Company: Jahia Ltd</p>
 *
 * @author Xavier Lawrence
 * @version 1.0
 * @jsp:tag name="template" body-content="JSP" description="Defines a jahia template."
 * <p/>
 * <p><attriInfo>This tag simply defines that the JSP file containing it should be considered as a
 * Jahia template. It generates the XML doctype and starting &lt;html&gt; tag of the XHTML page that will be
 * sent to the browser. It should be used together with the templateHead and templateBody tags, since all templates
 * MUST have a head and a body.
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
public class TemplateTag extends AbstractJahiaTag {

    private boolean gwtForGuest = false;
    private String doctype = DefaultIncludeProvider.XHTML_TRANSITIONAL;

    /**
     * Enables GWT for the guest (anonymous) user if the attribute value is 'true'. By default
     * GWT is disabled for the guest user in order to not generate javascript code for non-loggued user
     *
     * @param gwt Set to true if you want to enable GWT also for the guest user
     * @jsp:attribute name="gwt" required="false" rtexprvalue="true" type="boolean"
     * description="Enables GWT for the guest (anonymous) user if the attribute value is 'true'"
     * <p><attriInfo>
     * </attriInfo>"
     */
    public void setGwtForGuest(boolean gwt) {
        this.gwtForGuest = gwt;
    }

    public boolean enableGwtForGuest() {
        return gwtForGuest;
    }

    /**
     * Defines what DOCTYPE to use for the current pages created from this template. By default, pages are rendered
     * using the XHTML TRANSITIONAL doctype. Legal values for this attribute are "xhtml-strict", "xhtml-transitional",
     * "html-strict" or "html-transitional"
     *
     * @param doctype The value to use in order to change the doctype of the rendered pages
     * @jsp:attribute name="doctype" required="false" rtexprvalue="true"
     * description="Sets The value to use in order to change the doctype of the rendered pages"
     * <p><attriInfo>
     * </attriInfo>"
     */
    public void setDoctype(String doctype) {
        this.doctype = doctype;
    }

    /**
     * Defines what resource bundle file to use if the default template one is not suitable for this template.
     *
     * @param resourceBundle The name of the resource bundle to use for this template
     * @jsp:attribute name="resourceBundle" required="false" rtexprvalue="true"
     * description="Sets The name of the resource bundle to use for this template"
     * <p><attriInfo>
     * </attriInfo>"
     */
    public void setResourceBundle(String resourceBundle) {
        super.setResourceBundle(resourceBundle);
        if (resourceBundle != null && resourceBundle.length() > 0) {
            pageContext.getRequest().setAttribute(PARENT_BUNDLE_REQUEST_ATTRIBUTE, resourceBundle);
        }
    }

    public int doStartTag() throws JspException {
        if (doctype == null) {
            doctype = DefaultIncludeProvider.XHTML_TRANSITIONAL;
        }
        try {
            pageContext.getOut().println(DefaultIncludeProvider.getDocType(doctype));
            pageContext.getOut().println(DefaultIncludeProvider.getHtmlTag(doctype, pageContext.getRequest()));
        } catch (IOException e) {
            throw new JspTagException(e);
        }
        return EVAL_BODY_INCLUDE;
    }

    public int doEndTag() throws JspException {
        try {
            pageContext.getOut().println("\n</html>");
        } catch (IOException e) {
            throw new JspTagException(e);
        }
        gwtForGuest = false;
        doctype = DefaultIncludeProvider.XHTML_TRANSITIONAL;
        super.resetState();
        return SKIP_PAGE;
    }
}
