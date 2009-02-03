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
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
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
