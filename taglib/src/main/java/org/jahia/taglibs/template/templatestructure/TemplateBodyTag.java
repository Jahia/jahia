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
import org.jahia.analytics.GoogleAnalyticsService;
import org.jahia.data.JahiaData;
import org.jahia.data.beans.JahiaBean;
import org.jahia.exceptions.JahiaException;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.pages.ContentPage;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.filter.analytics.GoogleAnalyticsFilter;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.sites.SitesSettings;
import org.jahia.taglibs.AbstractJahiaTag;
import org.jahia.taglibs.internal.gwt.GWTIncluder;

import javax.servlet.ServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.DynamicAttributes;
import javax.servlet.jsp.tagext.Tag;
import java.util.*;

/**
 * <p>Title: Defines the body part of a jahia template</p>
 * <p>Description: This tag simply delimits what is the body part of a Jahia template.
 * All templates MUST have a body part. It should be used together with the template and templateHead tags. The tag also
 * implements the dynamic attributes interface allowing to specify whatever attribute should be passed to the HTML
 * body tag of the generated page.</p>
 * <p>Copyright: Copyright (c) 1999-2009</p>
 * <p>Company: Jahia Ltd</p>
 *
 * @author Xavier Lawrence
 * @version 1.0
 * @jsp:tag name="templateBody" body-content="JSP" description="Defines the body part of a jahia template."
 * <p/>
 * <p><attriInfo>This tag simply delimits what is the body part of a Jahia template.
 * All templates MUST have a body part. It should be used together with the template and templateHead tags. The tag also
 * implements the dynamic attributes interface allowing to specify whatever attribute should be passed to the HTML
 * body tag of the generated page.
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
public class TemplateBodyTag extends AbstractJahiaTag implements DynamicAttributes {

    private final static transient Logger logger = Logger.getLogger(TemplateBodyTag.class);

    private transient Map<String, Object> attributes = new HashMap<String, Object>();
    private String gwtScript;
    boolean useGwt = false;
    private boolean editDivOpen = false;

    /**
     * Allows the template developer to specify a specific GWT javascript file to use rather than the default one.
     *
     * @param gwtScript The GWT javascript file to use if the default one does not suit needs
     * @jsp:attribute name="gwtScript" required="false" rtexprvalue="true"
     * description="Allows the template developer to specify a specific GWT javascript file to use rather than the default one."
     * <p><attriInfo>
     * </attriInfo>"
     */
    public void setGwtScript(String gwtScript) {
        this.gwtScript = gwtScript;
    }

    public int doStartTag() {
        try {
            RenderContext renderContext = (RenderContext) pageContext.getAttribute("renderContext", PageContext.REQUEST_SCOPE);

            useGwt = renderContext.isEditMode();
            ServletRequest request = pageContext.getRequest();

            // check the gwtForGuest attribute from parent tag
            Tag parent = getParent();
            boolean gwtForGuest = false;
            if (parent instanceof TemplateTag) {
                gwtForGuest = ((TemplateTag) parent).enableGwtForGuest();
            }

            StringBuilder buf = new StringBuilder("<body");
            for (String param : attributes.keySet()) {
                buf.append(" ").append(param).append("=\"").append(attributes.get(param)).append("\"");
            }
            buf.append(">");

            // add google visualizer api if there is at least one active google analytics profile and if its the edit mode
            if (renderContext.isEditMode() && isGoogleAnalyticsActivated(renderContext.getSite())) {
                buf.append(GoogleAnalyticsService.getInstance().renderBaseVisualisationCode());
            }

            pageContext.getOut().println(buf.toString());

            if (renderContext != null) {
                if (renderContext.isEditMode()) {
                    Resource r = (Resource) pageContext.getRequest().getAttribute("currentResource");
                    pageContext.getRequest().setAttribute("jahia.engines.gwtModuleIncluded", Boolean.TRUE);
                    pageContext.getOut().println(GWTIncluder.generateGWTImport(pageContext, "org.jahia.ajax.gwt.module.edit.Edit"));
                    pageContext.getOut().println("<div class=\"jahia-template-gxt editmode-gxt\" id=\"editmode\" jahiatype=\"editmode\" path=\"" + r.getNode().getPath() + "\" locale=\"" + r.getLocale() + "\" template=\"" + r.getResolvedTemplate() + "\">");
                    editDivOpen = true;
                } else {
//                    Resource r = (Resource) pageContext.getRequest().getAttribute("currentResource");
//                    request.setAttribute("templateWrapper", "bodywrapper");
//                    String out = RenderService.getInstance().render(r, renderContext);
//                    pageContext.getOut().print(out);
//                    return SKIP_BODY;
                }
            }

        } catch (Exception e) {
            logger.error("Error while writing to JspWriter", e);
        }
        return EVAL_BODY_INCLUDE;
    }


    public int doEndTag() {
        final StringBuilder buf = new StringBuilder();

        ServletRequest request = pageContext.getRequest();
        try {
            RenderContext renderContext = (RenderContext) pageContext.getAttribute("renderContext", PageContext.REQUEST_SCOPE);
           
            //if (isGoogleAnaylitcsTrackingActivated(renderContext.getSite()) && isLiveMode()) {
                buf.append(GoogleAnalyticsService.getInstance().renderBaseTrackingCode());
                List<JCRNodeWrapper> trackedNodes = (List<JCRNodeWrapper>)renderContext.getRequest().getAttribute(GoogleAnalyticsFilter.GOOGLE_ANALYTICS_TRACKED_NODES);
                buf.append(GoogleAnalyticsService.getInstance().renderNodeTrackingCode(trackedNodes,renderContext.getSite()));


           // }
            if (useGwt) {
                // Generate jahia_gwt_dictionnary
                Map<String, String> dictionaryMap = getJahiaGwtDictionary();
                if (dictionaryMap != null) {
                    Locale currentLocale = request.getLocale();
                    if (renderContext != null) {
                        addMandatoryGwtMessages(renderContext.getUILocale(), currentLocale);
                    } else {
                        // we fall back to JahiaData for the administration interface, where this tag is also used.
                        JahiaData jahiaData = (JahiaData) pageContext.findAttribute("org.jahia.data.JahiaData");
                        if (jahiaData != null) {
                            addMandatoryGwtMessages(jahiaData.getProcessingContext().getUILocale(), currentLocale);
                        } else {
                            addMandatoryGwtMessages(null, currentLocale);
                        }
                    }
                    buf.append("<script type='text/javascript'>\n");
                    buf.append(generateJahiaGwtDictionary());
                    buf.append("</script>\n");
                }
            }
            if (editDivOpen) {
                buf.append("</div>");
            }

            buf.append("</body>");

            pageContext.getOut().println(buf.toString());
        } catch (Exception e) {
            logger.error("Error while writing to JspWriter", e);
        }

        // reset attributes
        gwtScript = null;
        attributes = new HashMap<String, Object>();
        useGwt = false;
        editDivOpen = false;
        return EVAL_PAGE;
    }

    /**
     * check if it's live mode
     * @return
     */
    private boolean isLiveMode() {
        final JahiaBean jBean = (JahiaBean) pageContext.getAttribute("jahia", PageContext.REQUEST_SCOPE);
        return jBean.getRequestInfo().isNormalMode();
    }

    /**
     * Set dynamic attribute
     * @param s
     * @param s1
     * @param o
     * @throws JspException
     */
    public void setDynamicAttribute(String s, String s1, Object o) throws JspException {
        if (attributes == null) {
            attributes = new HashMap<String, Object>();
        }
        attributes.put(s1, o);
    }

    


    /**
     * Check if there is one activated google analytics profile
     * @param jahiaSite
     * @return
     */
    private boolean isGoogleAnaylitcsTrackingActivated(JahiaSite jahiaSite) {
        boolean atLeast1TPon = false;
        // google analytics
        Iterator<?> it = ((jahiaSite.getSettings()).keySet()).iterator();
        // check if at list one profile is enabled
        while (it.hasNext()) {
            String key = (String) it.next();
            if (key.startsWith(SitesSettings.JAHIA_GA_PROFILE)) {
                if (Boolean.valueOf(jahiaSite.getSettings().getProperty(SitesSettings.GA_TRACKING_ENABLED))) {
                    return true;
                }
            }
        }
        return false;
    }


    /**
     * Check if there is a least one google analytics profile
     * @param jahiaSite
     * @return
     */
    private boolean isGoogleAnalyticsActivated(JahiaSite jahiaSite) {
        // google analytics
        Iterator<?> sitePropertiesKey = jahiaSite.getSettings().keySet().iterator();
        // check if at list one profile is enabled
        while (sitePropertiesKey.hasNext()) {
            String key = (String) sitePropertiesKey.next();
            if (key.startsWith(SitesSettings.JAHIA_GA_PROFILE)) {
                logger.debug("AT least one profile in the db !!!!");
                return true;
            }
        }
        return false;
    }
}
