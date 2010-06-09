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
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.filter.analytics.GoogleAnalyticsFilter;
import org.jahia.taglibs.AbstractJahiaTag;
import org.jahia.taglibs.internal.gwt.GWTIncluder;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.DynamicAttributes;
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
 */
@SuppressWarnings("serial")
public class TemplateBodyTag extends AbstractJahiaTag implements DynamicAttributes {

    private final static transient Logger logger = Logger.getLogger(TemplateBodyTag.class);

    private transient Map<String, Object> attributes = new HashMap<String, Object>();
    private boolean editDivOpen = false;

    public int doStartTag() {
        try {
            RenderContext renderContext = (RenderContext) pageContext.getAttribute("renderContext", PageContext.REQUEST_SCOPE);

            StringBuilder buf = new StringBuilder("<body");
            for (String param : attributes.keySet()) {
                buf.append(" ").append(param).append("=\"").append(attributes.get(param)).append("\"");
            }
            if (renderContext.isEditMode()) {
                buf.append(" style=\"overflow:hidden\"");
            }
            buf.append(">");

            // add google visualizer api if there is at least one active google analytics profile and if its the edit mode
            if (renderContext != null && renderContext.isEditMode() && renderContext.getSite() != null && renderContext.getSite().hasActivatedGoogleAnalyticsProfile()) {
                buf.append(ServicesRegistry.getInstance().getGoogleAnalyticsService().renderBaseVisualisationCode());
            }

            pageContext.getOut().println(buf.toString());

            if (renderContext != null) {
                if (renderContext.isEditMode()) {                    
                    Resource r = (Resource) pageContext.getRequest().getAttribute("wrappedResource");
                    if (r == null) {
                        r = (Resource) pageContext.getRequest().getAttribute("currentResource");
                    }

                    pageContext.getRequest().setAttribute("jahia.engines.gwtModuleIncluded", Boolean.TRUE);
                    pageContext.getOut().println(getGwtDictionaryInclude());
                    pageContext.getOut().println(GWTIncluder.generateGWTImport(pageContext, "org.jahia.ajax.gwt.module.edit.Edit"));

                    pageContext.getOut().println("<div class=\"jahia-template-gxt editmode-gxt\" id=\"editmode\" jahiatype=\"editmode\" config=\""+renderContext.getEditModeConfigName() +"\" path=\"" + r.getNode().getPath() + "\" locale=\"" + r.getLocale() + "\" template=\"" + r.getResolvedTemplate() + "\">");
                    editDivOpen = true;
                }
            }

        } catch (Exception e) {
            logger.error("Error while writing to JspWriter", e);
        }
        return EVAL_BODY_INCLUDE;
    }


    @SuppressWarnings("unchecked")
    public int doEndTag() {
        final StringBuilder buf = new StringBuilder();

        try {
            RenderContext renderContext = (RenderContext) pageContext.getAttribute("renderContext", PageContext.REQUEST_SCOPE);

            if (renderContext.getSite() != null && renderContext.getSite().hasGoogleAnalyticsProfile() /*&& renderContext.isLiveMode()*/) {
                buf.append(ServicesRegistry.getInstance().getGoogleAnalyticsService().renderBaseTrackingCode(renderContext.getRequest().getProtocol()));
                List<JCRNodeWrapper> trackedNodes = (List<JCRNodeWrapper>) renderContext.getRequest().getAttribute(GoogleAnalyticsFilter.GOOGLE_ANALYTICS_TRACKED_NODES);
                buf.append(ServicesRegistry.getInstance().getGoogleAnalyticsService().renderNodeTrackingCode(trackedNodes, renderContext.getSite()));

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
        resetState();
        return EVAL_PAGE;
    }
    
    @Override
    protected void resetState() {
        attributes = new HashMap<String, Object>();
        editDivOpen = false;
        super.resetState();
    }

    /**
     * Set dynamic attribute
     *
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
}
