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
package org.jahia.ajax.admin;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.jahia.ajax.AjaxAction;
import org.jahia.params.ProcessingContext;
import org.jahia.utils.i18n.JahiaResourceBundle;
import org.jahia.version.Status;
import org.jahia.version.VersionService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: 17 sept. 2007
 * Time: 11:22:24
 * To change this template use File | Settings | File Templates.
 */
public class PatchStatusAction extends AjaxAction {

    public PatchStatusAction() {
    }

    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
            ProcessingContext jParams = retrieveProcessingContext(request, response, null, false);
            final Locale currentLocale = jParams.getLocale();

            StringBuilder buf = new StringBuilder(256);
            buf.append(XML_HEADER);
            buf.append("<response>\n");

            List l = VersionService.getInstance().getScriptsStatus();
            for (Iterator iterator = l.iterator(); iterator.hasNext();) {
                Status status = (Status) iterator.next();
                buf.append("<script>");

                buf.append("<name>");
                buf.append(status.getScriptName());
                buf.append("</name>\n");

                buf.append(status.getScriptName());
                if (status.getResult() >= 0) {
                    buf.append("<result>");
                    buf.append(status.getResult());
                    buf.append("</result>");
                } else {
                    buf.append("<substatus>");
                    Status lastStatus = VersionService.getInstance().getLastScriptStatus();
                    String subStatus = lastStatus.getSubStatus();
                    String s = JahiaResourceBundle.getJahiaInternalResource(subStatus, currentLocale);
                    if (s == null) {
                        s = subStatus;
                    }
                    if (s == null) {
                        s = " ";
                    }
                    buf.append(s);
                    buf.append("</substatus>\n");
                    buf.append("<completed>");
                    buf.append(lastStatus.getPercentCompleted());
                    buf.append("</completed>\n");
                    buf.append("<remaining>");
                    if (lastStatus.getExecutionTime() > 10) {
                        DecimalFormat df = new DecimalFormat("00");
                        int i = lastStatus.getRemainingTime();
                        buf.append(df.format(i/3600)+":"+df.format((i/60)%60)+":"+df.format(i%60));
                    } else {
                        buf.append("...");
                    }
                    buf.append("</remaining>\n");
                }
                buf.append("</script>\n");
            }
            buf.append("</response>\n");
            sendResponse(buf.toString(), response);

        return null;
    }
}
