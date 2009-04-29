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
package org.jahia.ajax.versioning;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.jahia.ajax.AjaxAction;
import org.jahia.exceptions.JahiaBadRequestException;
import org.jahia.exceptions.JahiaUnauthorizedException;
import org.jahia.params.ParamBean;
import org.jahia.params.ProcessingContext;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.utils.xml.XmlUtils;

/**
 * Retrieves the time based publishing state of a ContentObject
 *
 * @author hollis
 */
public class ContainerVersionCompare extends AjaxAction {

    private static final org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger(ContainerVersionCompare.class);

    public ContainerVersionCompare(){
        super();
    }

    public ActionForward execute(final ActionMapping mapping,
                                 final ActionForm form,
                                 final HttpServletRequest request,
                                 final HttpServletResponse response)
            throws IOException, ServletException {
        try {

            final ProcessingContext jParams = retrieveProcessingContext(request, response);
            handleContainerVersionCompare(mapping, form, request, response, jParams);

        } catch (final Exception e) {
            handleException(e, request, response);
        }
        return null;
    }

    protected void handleContainerVersionCompare(final ActionMapping mapping,
                                                 final ActionForm form,
                                                 final HttpServletRequest request,
                                                 final HttpServletResponse response,
                                                 final ProcessingContext jParams)
    throws IOException, ServletException {

        try {
            final JahiaUser currentUser = (JahiaUser) request.getSession().getAttribute(ParamBean.SESSION_USER);
            final JahiaSite site = (JahiaSite) request.getSession().getAttribute("org.jahia.services.sites.jahiasite");

            if (currentUser == null || site == null) {
                ContainerVersionCompare.logger.debug("Unauthorized attempt to use AJAX Struts Action - ContainerVersionCompare");
                throw new JahiaUnauthorizedException("Must be logged in");
            }
            final org.w3c.dom.Document doc = getRequestXmlDocument(request);
            if (doc == null) {
                throw new JahiaBadRequestException("Wrong XML request");
            }
            StringBuffer buff = new StringBuffer(128);
            buff.append("<table class=\"containerVersionCompareMainTable\" border=\"0\" cellpadding=\"0\" >");
            buff.append("<tr>").append("<td class=\"dialogTitle\" valign=\"top\" align=\"left\">").append("Compare version").append("</td>").append("</tr>");
            buff.append("</table>");
            String htmlElement = getStringValueFromDocument(doc, "htmlElement");

            final StringBuffer buf = new StringBuffer();
            buf.append(XML_HEADER);
            buf.append("<response>\n");
            buf.append(buildXmlElement("containerVersionCompareDialog","<![CDATA[" + XmlUtils.removeNotValidXmlChars(buff.toString()) + "]]>") );
            buf.append(buildXmlElement("htmlElement", htmlElement));
            buf.append("</response>\n");
            sendResponse(buf.toString(), response);
        } catch (final Exception e) {
            handleException(e, request, response);
        }
    }

}
