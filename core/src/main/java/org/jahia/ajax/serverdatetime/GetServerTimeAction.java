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
package org.jahia.ajax.serverdatetime;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.jahia.ajax.AjaxAction;
import org.jahia.data.fields.JahiaDateFieldUtil;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.Format;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;


/**
 * Gets the Server current time and returns it to the client.
 *
 * @author Xavier Lawrence
 */
public class GetServerTimeAction extends AjaxAction {

    private static final String SERVER_TIME = "servertime";

    private static final org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger(GetServerTimeAction.class);

    /**
     * Returns the server time
     *
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @return ActionForward  (null)
     * @throws IOException
     * @throws ServletException
     */
    public ActionForward execute(final ActionMapping mapping,
                                 final ActionForm form,
                                 final HttpServletRequest request,
                                 final HttpServletResponse response)
            throws IOException, ServletException {

        logger.debug("GetServerTimeAction - execute");

        try {
            final String dateformat = getParameter(request, "dateformat");
            final Format df = JahiaDateFieldUtil.getDateFormat(dateformat,
                    Locale.getDefault());
            final String timeZoneOffSetVal = getParameter(request, "timeZoneOffSet");
            long timeZoneOffSet;
            try {
                timeZoneOffSet = Long.parseLong(timeZoneOffSetVal);
            } catch (Exception t) {
                timeZoneOffSet = 0;
            }

            final Date now = new Date();
            final int hostTimeZone = TimeZone.getDefault().getOffset(now.getTime());
            timeZoneOffSet = hostTimeZone - timeZoneOffSet;

            // Build and send the response message...
            sendResponse(new String[]{SERVER_TIME},
                    new String[]{df.format(new Date(now.getTime() + timeZoneOffSet))},
                    response);

        } catch (Exception e) {
            handleException(e, request, response);
        }
        return null;
    }
}
