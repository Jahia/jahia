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
package org.jahia.ajax.actionmenus;

import org.jahia.ajax.AjaxAction;
import org.jahia.services.timebasedpublishing.TimeBasedPublishingService;
import org.jahia.services.timebasedpublishing.RetentionRule;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaAdminUser;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.version.EntryLoadRequest;
import org.jahia.params.ProcessingContext;
import org.jahia.params.ParamBean;
import org.jahia.content.ObjectKey;
import org.jahia.utils.i18n.JahiaResourceBundle;
import org.jahia.hibernate.manager.JahiaObjectDelegate;
import org.jahia.hibernate.manager.JahiaObjectManager;
import org.jahia.hibernate.manager.SpringContextSingleton;
import org.jahia.utils.xml.XmlUtils;
import org.jahia.data.fields.JahiaDateFieldUtil;
import org.jahia.exceptions.JahiaBadRequestException;
import org.jahia.exceptions.JahiaRuntimeException;
import org.jahia.exceptions.JahiaUnauthorizedException;
import org.jahia.engines.calendar.CalendarHandler;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionForm;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.ServletException;
import javax.servlet.ServletContext;
import java.util.Map;
import java.util.Date;
import java.util.TimeZone;
import java.util.Calendar;
import java.util.concurrent.ConcurrentHashMap;
import java.io.IOException;
import java.io.InputStream;
import java.io.BufferedInputStream;
import java.io.OutputStream;
import java.text.Format;

/**
 * Retrieves the time based publishing state of a ContentObject
 *
 * @author hollis
 */
public class GetTimeBasedPublishingState extends AjaxAction {

    private static final org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger(GetTimeBasedPublishingState.class);

    protected static final TimeBasedPublishingService tbpService = servicesRegistry.getTimeBasedPublishingService();
    private static final Map imageMap = new ConcurrentHashMap(4);

    public ActionForward execute(final ActionMapping mapping,
                                 final ActionForm form,
                                 final HttpServletRequest request,
                                 final HttpServletResponse response)
            throws IOException, ServletException {
        long start_tbp = System.currentTimeMillis();
        try {
            final JahiaObjectManager jahiaObjectManager =
                    (JahiaObjectManager) SpringContextSingleton.getInstance()
                            .getContext().getBean(JahiaObjectManager.class.getName());

            final ProcessingContext jParams = retrieveProcessingContext(request, response);

            final String key = getParameter(request, KEY);
            ObjectKey currentObjectKey = ObjectKey.getInstance(key);

            if (logger.isDebugEnabled()) {
                logger.debug("Getting Time Based Publishing State for: " + key);
            }
            if (currentObjectKey == null) {
                throw new JahiaBadRequestException("Invalid content object key '" + key + "'");
            }

            JahiaObjectDelegate jahiaObjectDelegate =
                    jahiaObjectManager.getJahiaObjectDelegate(currentObjectKey);
            RetentionRule retRule = tbpService.getRetentionRule(currentObjectKey);
            boolean inherited = true;
            if (retRule == null) {
                JahiaUser adminUser = JahiaAdminUser.getAdminUser(jahiaObjectDelegate.getSiteId());
                currentObjectKey = tbpService.getParentObjectKeyForTimeBasedPublishing(currentObjectKey,
                        adminUser, EntryLoadRequest.STAGED, ParamBean.EDIT,
                        true);
                if (currentObjectKey != null) {
                    jahiaObjectDelegate = jahiaObjectManager
                            .getJahiaObjectDelegate(currentObjectKey);
                    retRule = tbpService.getRetentionRule(currentObjectKey);
                }
            } else {
                inherited = retRule.getInherited();
            }
            final long now = System.currentTimeMillis();

            final boolean displayDialog = "true".equals(getParameter(request, "displayDialog", "false"));

            if (!displayDialog) {
                returnTimeBasedPublishingStateImg(request, response, retRule, jahiaObjectDelegate, now, jParams, inherited);
            } else {
                displayTimeBasedPublishingStateDialog(request, response, retRule, jahiaObjectDelegate, jParams);
            }
            if (logger.isDebugEnabled()) {
                logger.debug("TBPSTATS: elapsed:" + (System.currentTimeMillis() - start_tbp));
            }
        } catch (final Exception e) {
            handleException(e, request, response);
        }
        return null;
    }

    protected void returnTimeBasedPublishingStateImg(
            final HttpServletRequest request,
            final HttpServletResponse response, final RetentionRule retRule,
            final JahiaObjectDelegate jahiaObjectDelegate, final long now,
            final ProcessingContext jParams, boolean inherited)
            throws IOException, ServletException {
        String statusLabel = "";
        if (retRule != null) {
            final boolean isValid = jahiaObjectDelegate.isValid();
            final boolean isExpired = jahiaObjectDelegate.isExpired();
            final boolean willExpire = jahiaObjectDelegate.willExpire(now);
            final boolean willBecomeValid = jahiaObjectDelegate.willBecomeValid(now);
            if (isExpired) {
                if (willBecomeValid) {
                    statusLabel = "expired_but_will_become_valid"; // yellow
                } else {
                    statusLabel = "expired"; // red
                }
            } else if (isValid) {
                if (willExpire) {
                    statusLabel = "valid_but_will_expire"; // orange
                } else {
                    statusLabel = "valid"; // green
                }
            } else {
                if (willBecomeValid) {
                    statusLabel = "not_valid_but_will_become_valid"; // yellow
                } else {
                    // is not valid
                    statusLabel = "unknown";
                }
            }
            if (statusLabel.length() > 0 && inherited) {
                statusLabel = "inherited_" + statusLabel;
            }
        }
        // Don't display any icons for objects that have no rules
        if (!jParams.settings().showTimeBasedPublishingIcons() ||
                "".equals(statusLabel) || "inherited_valid".equals(statusLabel)) {
            //statusLabel = "unknown";
            statusLabel = "org.jahia.pix.image";
        }
        final String imagePath = JahiaResourceBundle.getJahiaInternalResource(statusLabel, jParams.getLocale());

        if (logger.isDebugEnabled()) {
            logger.debug("imagePath: " + imagePath);
        }
        final ServletContext context = super.getServlet().getServletContext();
        response.setContentType(context.getMimeType(imagePath));
        setNoCacheHeaders(response);
        String imageName = "";
        if (imagePath != null) {
            imageName = imagePath.substring(request.getContextPath().length());
        }
        final byte[] buff;
        if (!imageMap.containsKey(imageName)) {
            final InputStream image = new BufferedInputStream(context.getResourceAsStream(imageName));
            buff = new byte[image.available()];
            image.read(buff);
            image.close();
            imageMap.put(imageName, buff);
        } else {
            buff = (byte[]) imageMap.get(imageName);
        }
        final OutputStream out = response.getOutputStream();
        out.write(buff);
        out.flush();
    }

    protected void displayTimeBasedPublishingStateDialog(final HttpServletRequest request,
                                                         final HttpServletResponse response,
                                                         final RetentionRule retRule,
                                                         final JahiaObjectDelegate jahiaObjectDelegate,
                                                         final ProcessingContext jParams) throws Exception {

        final JahiaUser currentUser = (JahiaUser) request.getSession().getAttribute(ParamBean.SESSION_USER);
        final JahiaSite site = (JahiaSite) request.getSession().getAttribute("org.jahia.services.sites.jahiasite");

        if (currentUser == null || site == null) {
            throw new JahiaUnauthorizedException("Unauthorized attempt to use AJAX Struts Action - GetTimeBasedPublishingState");
        }
        final org.w3c.dom.Document doc = getRequestXmlDocument(request);
        if (doc == null) {
            throw new JahiaBadRequestException("Error: Wrong XML request");
        }
        if (jahiaObjectDelegate != null) {

            String statusLabel = JahiaResourceBundle.getJahiaInternalResource("org.jahia.engines.timebasedpublishing.timebBasedPublishingStatus.label",
                    jParams.getLocale(), "Time Based Publishing Status");
            String currentStatusLabel = JahiaResourceBundle.getJahiaInternalResource("org.jahia.engines.timebasedpublishing.currentstatus.label",
                    jParams.getLocale(), "Current status");
            String schedulingTypeLabel = JahiaResourceBundle.getJahiaInternalResource("org.jahia.engines.timebasedpublishing.schedulingType.label",
                    jParams.getLocale(), "Scheduling type");
            String publicationDateLabel = JahiaResourceBundle.getJahiaInternalResource("org.jahia.engines.timebasedpublishing.rangerule.validFrom.label",
                    jParams.getLocale(), "Publication date");
            String expirationDateLabel = JahiaResourceBundle.getJahiaInternalResource("org.jahia.engines.timebasedpublishing.rangerule.validTo.label",
                    jParams.getLocale(), "Publication date");
            String publicationDateValue = "";
            String expirationDateValue = "";
            String statusValueLabel = "";
            String schedulingType = "";
            if (retRule != null) {
                boolean inherited = retRule.getInherited();
                boolean isValid = jahiaObjectDelegate.isValid();
                boolean isExpired = jahiaObjectDelegate.isExpired();
                schedulingType = retRule.getRuleType();
                if (inherited) {
                    JahiaUser adminUser = JahiaAdminUser.getAdminUser(jahiaObjectDelegate.getSiteId());
                    ObjectKey contentObjectKey = tbpService
                            .getParentObjectKeyForTimeBasedPublishing(jahiaObjectDelegate.getObjectKey(), adminUser, EntryLoadRequest.STAGED, ParamBean.EDIT, true);
                    if (contentObjectKey != null &&
                            !contentObjectKey.equals(jahiaObjectDelegate.getObjectKey())) {
                        final RetentionRule effectiveRetRule = tbpService.getRetentionRule(contentObjectKey);
                        schedulingType = effectiveRetRule.getRuleType();
                    }
                }
                final int statusCode;
                if (isExpired) {
                    statusCode = 0;
                } else if (isValid) {
                    statusCode = 2;
                } else {
                    statusCode = 1;
                }
                statusValueLabel = JahiaResourceBundle.getJahiaInternalResource("org.jahia.engines.timebasedpublishing.timebpstatus." + statusCode + ".label", jParams.getLocale(), "");
                if (statusValueLabel == null || "".equals(statusValueLabel)) {
                    if (statusCode == 0) {
                        statusValueLabel = "expired";
                    } else if (statusCode == 2) {
                        statusValueLabel = "available";
                    } else {
                        statusValueLabel = "not available";
                    }
                }
                long clientTimeZoneDiff = 0;
                try {
                    final String clientTimeZoneOffsetStr = getStringValueFromDocument(doc, "timeZoneOffset");
                    long clientTimeZoneOffset = Long.parseLong(clientTimeZoneOffsetStr);
                    clientTimeZoneDiff = clientTimeZoneOffset * 60 * 1000;
                } catch (Exception t) {
                    //
                }
                schedulingType = JahiaResourceBundle.getJahiaInternalResource("org.jahia.engines.timebasedpublishing.schedulingType."
                                + schedulingType, jParams.getLocale(), schedulingType);

                Format formater = JahiaDateFieldUtil.getDateFormat(
                        CalendarHandler.DEFAULT_DATE_FORMAT, jParams.getLocale());
                Long dateLong = jahiaObjectDelegate.getValidFromDate();
                if (dateLong != null && dateLong != 0) {
                    Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                    cal.setTimeInMillis(dateLong - clientTimeZoneDiff);
                    publicationDateValue = formater.format(cal.getTime());
                } else {
                    publicationDateValue = JahiaResourceBundle.getJahiaInternalResource("org.jahia.engines.timebasedpublishing.dateNotAssigned",
                            jParams.getLocale(), "not assigned");
                }

                dateLong = jahiaObjectDelegate.getValidToDate();
                if (dateLong != null && dateLong != 0) {
                    Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                    cal.setTimeInMillis(dateLong - clientTimeZoneDiff);
                    expirationDateValue = formater.format(cal.getTime());
                } else {
                    expirationDateValue = JahiaResourceBundle.getJahiaInternalResource("org.jahia.engines.timebasedpublishing.dateNotAssigned",
                            jParams.getLocale(), "not assigned");
                }

            }
            StringBuffer buff = new StringBuffer(2048);
            buff.append("<table class=\"timeBasedPublishingMainTable\" border=\"0\" cellpadding=\"0\" >");
            buff.append("<tr>").append("<td colspan=\"3\" class=\"timeBasedPublishingTitle\" valign=\"top\" align=\"left\">").append(statusLabel).append("</td>").append("</tr>");
            buff.append("<tr>").append("<td class=\"timeBasedPublishingLabel\" valign=\"top\" align=\"left\" nowrap>").append(schedulingTypeLabel).append("</td><td valign=\"top\" align=\"left\" style=\"width:20px;text-aling:left\">:</td>");
            buff.append("<td class=\"timeBasedPublishingValue\" valign=\"top\" align=\"left\" nowrap>").append(schedulingType).append("</td></tr>");
            buff.append("<tr>").append("<td class=\"timeBasedPublishingLabel\" valign=\"top\" align=\"left\" nowrap>").append(currentStatusLabel).append("</td><td valign=\"top\" align=\"left\" style=\"width:20px;text-aling:left\">:</td>");
            buff.append("<td class=\"timeBasedPublishingValue\" valign=\"top\" align=\"left\" nowrap>").append(statusValueLabel).append("</td></tr>");
            buff.append("<tr>").append("<td class=\"timeBasedPublishingLabel\" valign=\"top\" align=\"left\" nowrap>").append(publicationDateLabel).append("</td><td valign=\"top\" align=\"left\" style=\"width:20px;text-aling:left\">:</td>");
            buff.append("<td class=\"timeBasedPublishingValue\" valign=\"top\" align=\"left\" nowrap>").append(publicationDateValue).append("</td></tr>");
            buff.append("<tr>").append("<td class=\"timeBasedPublishingLabel\" valign=\"top\" align=\"left\" nowrap>").append(expirationDateLabel).append("</td><td valign=\"top\" align=\"left\" style=\"width:20px;text-aling:left\">:</td>");
            buff.append("<td class=\"timeBasedPublishingValue\" valign=\"top\" align=\"left\" nowrap>").append(expirationDateValue).append("</td></tr>");
            buff.append("</table>");

            String eventX = getStringValueFromDocument(doc, "eventX");
            String eventY = getStringValueFromDocument(doc, "eventY");
            String htmlElement = getStringValueFromDocument(doc, "htmlElement");

            final StringBuffer buf = new StringBuffer();
            buf.append(XML_HEADER);
            buf.append("<response>\n");
            buf.append(buildXmlElement("timeBasedPublishingDialog", "<![CDATA[" + XmlUtils.removeNotValidXmlChars(buff.toString()) + "]]>"));
            buf.append(buildXmlElement("eventX", eventX));
            buf.append(buildXmlElement("eventY", eventY));
            buf.append(buildXmlElement("htmlElement", htmlElement));
            buf.append("</response>\n");
            sendResponse(buf.toString(), response);
        } else {
            throw new JahiaRuntimeException("time based publishing status not available");
        }
    }

}
