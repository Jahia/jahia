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
