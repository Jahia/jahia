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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.TimeZone;

/**
 * Gets the TimeZone difference between the client and the server.
 *
 * @author Xavier Lawrence
 */
public class GetClientServerTimeDiffAction extends AjaxAction {

    private static final String SERVER_CLIENT_TIME_DIFF = "serverClientTimeDiff";

    private static final org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger(GetServerTimeAction.class);

    /**
     * Returns the server client different number of minutes + secondes in millis
     *
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @return ActionForward  (null)
     * @throws java.io.IOException
     * @throws javax.servlet.ServletException
     */
    public ActionForward execute(final ActionMapping mapping,
                                 final ActionForm form,
                                 final HttpServletRequest request,
                                 final HttpServletResponse response)
            throws IOException, ServletException {

        logger.debug("GetServerTimeAction - execute");

        try {
            final String clientTimeVal = getParameter(request, "clientTime");
            long clientTime;
            try {
                clientTime = Long.parseLong(clientTimeVal);
            } catch (Exception t) {
                clientTime = 0;
            }

            final String timeZoneOffSetVal = getParameter(request, "timeZoneOffSet");
            long timeZoneOffSet;
            try {
                timeZoneOffSet = Long.parseLong(timeZoneOffSetVal);
            } catch (Exception t) {
                timeZoneOffSet = 0;
            }

            final Date now = new Date();
            final int hostTimeZone = TimeZone.getDefault().getOffset(now.getTime());

            // client Time in UTC
            clientTime = clientTime - timeZoneOffSet;

            // servet Time in UTC
            final long serverTime = now.getTime() - hostTimeZone;
            final long diffTime = serverTime - clientTime;

            // Build and send the response message...
            sendResponse(new String[]{SERVER_CLIENT_TIME_DIFF},
                    new String[]{String.valueOf(diffTime)},
                    response);

        } catch (Exception e) {
            handleException(e, request, response);
        }
        return null;
    }
}
