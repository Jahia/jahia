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

package org.jahia.ajax.usersession;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.jahia.ajax.AjaxAction;
import org.jahia.content.ContentObject;
import org.jahia.content.ContentObjectKey;
import org.jahia.exceptions.JahiaBadRequestException;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaUnauthorizedException;
import org.jahia.params.ParamBean;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.usermanager.JahiaUser;

/**
 * a class to handle ajax request for clipboard and communicate with
 * {@link org.jahia.engines.importexport.ClipboardEngine ClipboardEngine} <br/>
 * @author joe Pillot
 * @version $Id: $
 */

public class clipboardAction extends AjaxAction {
    private static final transient Logger logger = Logger.getLogger(clipboardAction.class);

    public ActionForward execute(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request, final HttpServletResponse response) throws IOException, ServletException {

       final HttpSession mysession = request.getSession(false);
        final JahiaUser currentUser = (JahiaUser) mysession.getAttribute(ParamBean.SESSION_USER);
        final JahiaSite site = (JahiaSite) mysession.getAttribute(ParamBean.SESSION_SITE);
        //final String sessionID = mysession.getId();

        if (currentUser == null) {
            logger.debug("Unauthorized attempt to use AJAX Struts Action - User settings");
            throw new JahiaUnauthorizedException("Must be logged in");
        }
        if (site == null) {
            throw new JahiaBadRequestException("Virtual site data cannot be found");
        }
        String clipkey=(String)mysession.getAttribute("clipboard_key");
        if(clipkey!=null){
            try {
                final ContentObject source = ContentObject.getContentObjectInstance(ContentObjectKey.getInstance(clipkey));
                // validate the object
                if (source == null || source.isMarkedForDelete()) {
                    mysession.removeAttribute("clipboard_key");
                    clipkey = null;
                }
            } catch (ClassNotFoundException ex) {
                logger.warn("Wrong object key in the clipboard found", ex);
                mysession.removeAttribute("clipboard_key");
                clipkey = null;
            } catch (JahiaException jex) {
                logger.warn("Unable to validate the object in the clipboard", jex);
            }
            logger.debug("clipkey="+clipkey);
        } else {
            logger.debug("clipkey=is null");
            clipkey="null";
        }

        sendResponse(new StringBuffer(64).append(XML_HEADER).append(
                "<response>").append(clipkey).append("</response>").toString(),
                response);

        return null;
    }
}
