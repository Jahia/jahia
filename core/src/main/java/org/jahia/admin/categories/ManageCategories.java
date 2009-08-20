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
package org.jahia.admin.categories;

import org.jahia.admin.AbstractAdministrationModule;
import org.jahia.bin.Jahia;
import org.jahia.bin.JahiaAdministration;
import org.jahia.data.JahiaData;
import org.jahia.exceptions.JahiaException;
import org.jahia.hibernate.manager.JahiaSiteLanguageListManager;
import org.jahia.hibernate.manager.SpringContextSingleton;
import org.jahia.params.ParamBean;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.security.license.License;
import org.jahia.services.categories.Category;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.tools.files.FileUpload;
import org.jahia.utils.i18n.JahiaResourceBundle;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * <p>Title: Category administration user interface</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Jahia Ltd</p>
 *
 * @author Serge Huber
 * @version 1.0
 */

public class ManageCategories extends AbstractAdministrationModule {

    private static org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger(ManageCategories.class);

    private static final String JSP_PATH = JahiaAdministration.JSP_PATH;
    public static final String CURRENTCATEGORY_SESSIONKEY =
            "currentCategoryKey";
    public static final String CURRENTCATEGORYCHILDS_SESSIONKEY =
            "currentCategoryChilds";
    public static final String CATEGORYTREE_SESSIONKEY = "categoryTree";

    private JahiaSiteLanguageListManager listManager = null;

    /**
     * Default constructor.
     *
     * @param request  Servlet request.
     * @param response Servlet response.
     */
    public void service(HttpServletRequest request,
                            HttpServletResponse response)
            throws Exception {

        JahiaData jData = (JahiaData) request.getAttribute("org.jahia.data.JahiaData");
        ProcessingContext jParams = null;
        if (jData != null) {
            jParams = jData.getProcessingContext();
        }

        License coreLicense = Jahia.getCoreLicense();
        if (coreLicense == null) {
            // set request attributes...
            if (jParams == null) return;
            String dspMsg = JahiaResourceBundle.getJahiaInternalResource(
                    "org.jahia.admin.JahiaDisplayMessage.invalidLicenseKey.label",
                    jParams.getLocale());
            request.setAttribute("jahiaDisplayMessage", dspMsg);
            // redirect...
            JahiaAdministration.doRedirect(request, response, request.getSession(),JSP_PATH + "menu.jsp");
            return;
        }
        listManager = (JahiaSiteLanguageListManager) SpringContextSingleton.getInstance().getContext().getBean(JahiaSiteLanguageListManager.class.getName());
        userRequestDispatcher(request, response, request.getSession());
    } // end constructor

    //-------------------------------------------------------------------------

    /**
     * This method is used like a dispatcher for user requests.
     *
     * @param request  Servlet request.
     * @param response Servlet response.
     * @param session  Servlet session for the current user.
     */
    private void userRequestDispatcher(HttpServletRequest request,
                                       HttpServletResponse response,
                                       HttpSession session)
            throws Exception {
        JahiaData jData = (JahiaData) request.getAttribute(
                "org.jahia.data.JahiaData");
        ProcessingContext jParams = null;
        if (jData != null) {
            jParams = jData.getProcessingContext();
        }
        String operation = request.getParameter("sub");
        logger.debug("Operation: " + operation);
        if (operation == null) {
            operation = "display";
        }

        ServicesRegistry sReg = ServicesRegistry.getInstance();

        // check if the user has really admin access to this site...
        JahiaUser user = (JahiaUser) session.getAttribute(ProcessingContext.SESSION_USER);
        JahiaSite site = (JahiaSite) session.getAttribute(ProcessingContext.SESSION_SITE);

        if (site != null && user != null && sReg != null) {

            // set the new site id to administrate...
            request.setAttribute("site", site);

            if (operation.equals("display")) {
                JahiaAdministration.doRedirect(request,response,session,JSP_PATH + "manage_categories.jsp");
            } else if (operation.equals("commit")) {
                commitChanges(request, response, session);
            } else if (operation.equals("editCategory")) {
                //displayEditCategory(request, response, session, null);
            } else if (operation.equals("addExistingCategory")) {
               // commitChanges(request, response, session);
            } else if (operation.equals("commitEditCategory")) {
               // commitEditCategory(request, response, session);
            } else if (operation.equals("deleteCategory")) {
               // displayDeleteCategory(request, response, session);
            } else if (operation.equals("commitDeleteCategory")) {
               // commitDeleteCategory(request, response, session);
            } else if (operation.equals("moveCategory")) {
               //  displayMoveCategory(request, response, session);
            } else if (operation.equals("commitMoveCategory")) {
                // commitMoveCategory(request, response, session);
            } else if (operation.equals("import")) {
                importCategories(request, response, session);
            }

        } else {
            if (jParams == null) return;
            String dspMsg = JahiaResourceBundle.getJahiaInternalResource(
                    "org.jahia.admin.JahiaDisplayMessage.requestProcessingError.label",
                    jParams.getLocale());
            request.setAttribute("jahiaDisplayMessage", dspMsg);
            JahiaAdministration.doRedirect(request,
                    response,
                    session, JSP_PATH + "menu.jsp");
        }
    } // userRequestDispatcher

    //-------------------------------------------------------------------------
    private void displayCategories(HttpServletRequest request,
                                   HttpServletResponse response,
                                   HttpSession session)
            throws IOException, ServletException {
        JahiaData jData = (JahiaData) request.getAttribute("org.jahia.data.JahiaData");
        ProcessingContext jParams = null;
        if (jData != null) {
            jParams = jData.getProcessingContext();
        }
        try {
            String curCategoryKey = request.getParameter("currentCategoryKey");
            if (curCategoryKey != null) {
                session.setAttribute(CURRENTCATEGORY_SESSIONKEY, curCategoryKey);
            }

            List flatCategoryList = null;

            // For JSP Output
            String currentCategoryKey = (String) session.getAttribute(CURRENTCATEGORY_SESSIONKEY);
            if (currentCategoryKey == null) {
                logger.error("is this code still accessed?");
/*                Category rootCategory = Category.getRootCategory(jParams.getUser());
                if (rootCategory != null) {
                    currentCategoryKey = rootCategory.getObjectKey().getIDInType();
                    session.setAttribute(CURRENTCATEGORY_SESSIONKEY, currentCategoryKey);
                }*/
            }
            // test if we are able to load the current category key
            if (currentCategoryKey != null) {
                Category currentCategory = Category.getCategoryByUUID(currentCategoryKey, jParams.getUser());
                if (currentCategory == null) {
                    // if not we default to root category.
                    logger.warn("Couldn't find category key " + currentCategoryKey + " defaulting back to root category");
                    logger.error("is this code still accessed?");
/*                    currentCategory = Category.getRootCategory(jParams.getUser());
                    if (currentCategory != null) {
                        currentCategoryKey = currentCategory.getObjectKey().getIDInType();
                        session.setAttribute(CURRENTCATEGORY_SESSIONKEY,
                                currentCategoryKey);
                    }*/
                }
                if (currentCategory != null) {
                    session.setAttribute(CURRENTCATEGORYCHILDS_SESSIONKEY,
                            currentCategory.getChildCategories(jParams.getUser()));
                }
            }
            request.setAttribute("flatCategoryList", flatCategoryList);

            JahiaAdministration.doRedirect(request,
                    response,
                    session,
                    JSP_PATH + "manage_categories.jsp");

        } catch (JahiaException je) {
            String dspMsg = JahiaResourceBundle.getJahiaInternalResource(
                    "org.jahia.admin.JahiaDisplayMessage.requestProcessingError.label",
                    jParams.getLocale());
            request.setAttribute("jahiaDisplayMessage", dspMsg);
            JahiaAdministration.doRedirect(request,
                    response,
                    session,
                    JSP_PATH + "menu.jsp");
        }

    }

    //-------------------------------------------------------------------------
    private void commitChanges(HttpServletRequest request,
                               HttpServletResponse response,
                               HttpSession session)
            throws IOException, ServletException {

        JahiaData jData = (JahiaData) request.getAttribute(
                "org.jahia.data.JahiaData");
        ProcessingContext jParams = null;
        if (jData != null) {
            jParams = jData.getProcessingContext();
        }

        request.setAttribute("warningMsg", "");

        String dspMsg = JahiaResourceBundle.getJahiaInternalResource( "org.jahia.admin.JahiaDisplayMessage.changeCommitted.label",jParams.getLocale());
        request.setAttribute("jahiaDisplayMessage", dspMsg);
        displayCategories(request, response, session);


    } // end addComponent


    private void importCategories(HttpServletRequest request, HttpServletResponse response, HttpSession session) {
        try {
            JahiaData jData = (JahiaData) request.getAttribute("org.jahia.data.JahiaData");
            ProcessingContext jParams = null;
            if (jData != null) {
                jParams = jData.getProcessingContext();
            }

            final FileUpload fupload = ((ParamBean) jParams).getFileUpload();

            // get files name
            Set filesName = fupload.getFileNames();

            for (Iterator iterator = filesName.iterator(); iterator.hasNext();) {
                String name = (String) iterator.next();
                final File file = fupload.getFile(name);
                final String contentType = fupload.getFileContentType(name);
                if (contentType.equalsIgnoreCase("text/xml") || contentType.equalsIgnoreCase("application/xml")) {
                    ServicesRegistry.getInstance().getImportExportService().importCategories(jParams, new FileInputStream(file));
                } else {
                    logger.error("Cannot import: File[ContentType="+contentType+"] is not a valid XML document. ");
                    String errors = JahiaResourceBundle.getJahiaInternalResource("org.jahia.admin.site.ManageSites.import.error.xml",
                    jParams.getLocale());
                    request.setAttribute("errors",errors);
                }
            }
            displayCategories(request, response, session);
        } catch (Exception e) {
            logger.error("Error", e);
        }

    }

}