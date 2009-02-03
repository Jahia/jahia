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

package org.jahia.ajax.webdav;

import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionForm;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.pages.ContentPage;
import org.jahia.services.webdav.JahiaWebdavBaseService;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.params.ProcessingContext;
import org.jahia.data.fields.JahiaField;
import org.jahia.data.files.JahiaFileField;
import org.jahia.data.containers.JahiaContainer;
import org.jahia.engines.filemanager.TableEntry;
import org.jahia.exceptions.JahiaForbiddenAccessException;
import org.jahia.exceptions.JahiaUnauthorizedException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.ServletException;
import java.io.IOException;
import java.util.Map;

/**
 * Modifies the engine Map by selecting/unselecting the selected file
 *
 * @author Xavier Lawrence
 */
public class SelecUnselectFile extends DAVAbstractAction {

    private static final org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger(SelecUnselectFile.class);

    public ActionForward execute(final ActionMapping mapping,
                                 final ActionForm form,
                                 final HttpServletRequest request,
                                 final HttpServletResponse response)
            throws IOException, ServletException {
        try {
            final ProcessingContext jParams = retrieveProcessingContext(request, response);
            final ContentPage currentPage = jParams.getContentPage();
            final JahiaUser currentUser = jParams.getUser();

            if (currentUser == null || currentPage == null ||
                    ! currentPage.checkWriteAccess(currentUser)) {
                logger.warn("Unauthorized attempt to use AJAX Struts Action - SelecUnselecttFile");
                if (isValidUser(currentUser)) {
                    throw new JahiaForbiddenAccessException("Must have 'Write' access");
                } else {
                    throw new JahiaUnauthorizedException("Must be logged in");
                }
            }

            // fileName
            final String key = getXmlNodeValue(request, KEY);
            final HttpSession session = request.getSession();
            final Map engineMap = (Map) request.getSession().getAttribute("jahia_session_engineMap");
            final String currentFileName = engineMap.get("filename").toString();
            final String fieldsEditCallingEngineName = (String) engineMap.get("fieldsEditCallingEngineName");
            final JahiaField theField = (JahiaField) engineMap.get(fieldsEditCallingEngineName + ".theField");
            final JahiaFileField jff = (JahiaFileField) theField.getObject();

            final String responseValue;
            if (key.equals(currentFileName)) {
                // Unselect file
                engineMap.put("filename", "");
                session.removeAttribute("selectedFile");
                engineMap.remove("deniedUsers");
                jff.setRealName("");
                theField.setObject(jff);
                responseValue = "";

            } else {
                // Select file
                engineMap.put("filename", key);
                session.setAttribute("selectedFile", key);
                jff.setRealName(key);
                theField.setObject(jff);
                final JCRNodeWrapper rm = JahiaWebdavBaseService.getInstance().getDAVFileAccess(
                        key, jParams.getUser());
                engineMap.put("deniedUsers", rm.comparePermsWithField(theField,
                        (JahiaContainer) engineMap.get("theContainer")));
                responseValue = TableEntry.javascriptDecode(key);
            }

            engineMap.put(fieldsEditCallingEngineName + ".theField", theField);
            logger.debug("selected File is: " + engineMap.get("filename"));
            session.setAttribute("jahia_session_engineMap", engineMap);

            sendResponse(new String[]{"value"}, new String[] {responseValue}, response);

        } catch (final Exception e) {
            handleException(e, request, response);
        }
        return null;
    }
}
