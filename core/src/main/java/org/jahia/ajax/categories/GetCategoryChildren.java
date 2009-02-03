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

package org.jahia.ajax.categories;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.jahia.data.fields.JahiaField;
import org.jahia.engines.applications.ManageApplicationCategoriesEngine;
import org.jahia.engines.filemanager.TableEntry;
import org.jahia.engines.shared.Category_Field;
import org.jahia.engines.categories.CategoriesSelect_Engine;
import org.jahia.exceptions.JahiaBadRequestException;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaForbiddenAccessException;
import org.jahia.exceptions.JahiaUnauthorizedException;
import org.jahia.params.ProcessingContext;
import org.jahia.params.SessionState;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.acl.JahiaACLManagerService;
import org.jahia.services.acl.JahiaBaseACL;
import org.jahia.services.categories.Category;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.utils.comparator.NumericStringComparator;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

/**
 * Struts AJAX based Action that collects all the direct child categories of a given category for the administration.
 *
 * @author Xavier Lawrence
 * @author Khaled Tlili
 */
public class GetCategoryChildren extends AbstractCategoryAction {

    private static final org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger(GetCategoryChildren.class);

    private static final JahiaACLManagerService aclService = ServicesRegistry.getInstance().getJahiaACLManagerService();

    public ActionForward execute(final ActionMapping mapping,
                                 final ActionForm form,
                                 final HttpServletRequest request,
                                 final HttpServletResponse response)
            throws IOException, ServletException {
        try {
            final ProcessingContext jParams = retrieveProcessingContext(request, response);
            final JahiaUser currentUser = jParams.getUser();
            final String admin = getParameter(request, "admin", "");
            final String move = getParameter(request, "move", "");
            final String objectKey = getParameter(request, "display", "");
            final String portletCategories = getParameter(request, ManageApplicationCategoriesEngine.PORLET_CATEGORIES, "");
            final boolean isPortletCategoriesContext = portletCategories != null && portletCategories.equalsIgnoreCase(ManageApplicationCategoriesEngine.PORLET_CATEGORIES_TRUE);

            final boolean isAdmincontext = admin.length() > 0;
            if (isAdmincontext) {
                if (aclService.getServerActionPermission("admin.categories.ManageCategories",
                        currentUser, JahiaBaseACL.READ_RIGHTS, jParams.getSiteID()) < 1) {
                    if (isValidUser(currentUser)) {
                        throw new JahiaForbiddenAccessException("Must have 'ManageCategories' access");
                    } else {
                        throw new JahiaUnauthorizedException("Must be logged in");
                    }
                }
            }
            String key = null;
            boolean isSelectEngine = false;
            String contextId = "";
            try {
                key = getXmlNodeValue(request, KEY);
                if (key.startsWith("selectengine|")) {
                    isSelectEngine = true;
                    key = key.substring("selectengine|".length());
                    contextId = key.substring(0,key.indexOf('|'));
                    key = key.substring(key.indexOf('|')+1);
                }
            } catch (Exception ex) {
                logger.debug("No Key specified", ex);
            }
            if (logger.isDebugEnabled()) {
                logger.debug("Getting Category Children for: " + key);
            }

            final Document resp = getNewDocument(request);
            final Element root = resp.createElement("GetCategoryChildrenResp");
            Category startingCategory = null;

            final SessionState session = jParams.getSessionState();
            final Map engineMap = (Map) session.getAttribute("jahia_session_engineMap");
            if (engineMap != null) {
                final String startCategoryKey = (String) engineMap.get("startCategory");
                if (startCategoryKey != null) {
                    startingCategory = Category.getCategory(startCategoryKey, currentUser);
                }
                engineMap.remove("startCategory");
                session.setAttribute("jahia_session_engineMap", engineMap);
            }

            final boolean isMovecontext = move.length() > 0;
            if (key == null || startingCategory != null || "/".equals(key) || key.length() == 0) {
                // Append the root category only and stop
                if (startingCategory == null) {
                    startingCategory = Category.getRootCategory(currentUser);
                }
                if (startingCategory != null) {
                    if (isAdmincontext) {
                        attachCategoryforAdminToResp(startingCategory, resp, root, currentUser, jParams,
                                request, response);

                    } else if (isMovecontext) {
                        attachCategoryToResp(startingCategory, resp, root, jParams,
                                !startingCategory.getACL().getPermission(startingCategory, startingCategory, currentUser, JahiaBaseACL.WRITE_RIGHTS));

                    } else if (isPortletCategoriesContext) {
                        // we are dealing with portlet
                        logger.debug("deal with portlets category");
                        final List selectedCategories = getSelectedPortletsCategories(jParams);
                        final List categoriesIsMultipleSrcList = getSelectedPortletsAllsourcesCategories(jParams);
                        attachCategoryForPortletToResp(startingCategory, resp, root, jParams, selectedCategories,
                                categoriesIsMultipleSrcList);
                    } else {
                        if (objectKey.startsWith("/")) {
                            // we are dealing with a DAV resource
                            final List selectedCategories = (List) request.getSession().getAttribute(
                                    TableEntry.javascriptDecode(objectKey) + Category_Field.SELECTEDCATEGORIES_ENGINEMAPKEY);
                            attachCategoryToResp(startingCategory, resp, root, jParams, selectedCategories);
                        } else {
                            // we are dealing with a JahiaObject
                            final List selectedCategories = getSelectedCategories(jParams);
                            attachCategoryToResp(startingCategory, resp, root, jParams, selectedCategories);
                        }
                    }
                } else {
                    logger.debug("User[" + currentUser + "] has no read access on root category");
                }
                resp.appendChild(root);
                sendResponse(resp, response);
                return null;

            } else {
                startingCategory = Category.getCategory(key, currentUser);
                if (startingCategory == null) {
                    resp.appendChild(root);
                    throw new JahiaBadRequestException("Starting category not found for key '" + key + "'");
                }
            }

            final List children = startingCategory.getChildCategories(currentUser);

            // Let us sort the categories in ascending order comparing their title in the current language
            final TreeSet allCategories = new TreeSet(new NumericStringComparator());
            allCategories.addAll(children);

            final Iterator ite = allCategories.iterator();
            if (isAdmincontext) {
                while (ite.hasNext()) {
                    attachCategoryforAdminToResp((Category) ite.next(), resp, root, currentUser, jParams,
                            request, response);
                }

            } else if (isMovecontext) {
                while (ite.hasNext()) {
                    final Category cat = (Category) ite.next();
                    attachCategoryToResp(cat, resp, root, jParams,
                            !cat.getACL().getPermission(cat, cat, currentUser, JahiaBaseACL.WRITE_RIGHTS));
                }

            } else if (isPortletCategoriesContext) {
                // we are dealing with portlet
                logger.debug("Dealing with a portlet.");
                while (ite.hasNext()) {
                    final List selectedCategories = getSelectedPortletsCategories(jParams);
                    final List categoriesIsMultipleSrcList = getSelectedPortletsAllsourcesCategories(jParams);
                    attachCategoryForPortletToResp((Category) ite.next(), resp, root, jParams, selectedCategories, categoriesIsMultipleSrcList);
                }

            } else if (isSelectEngine) {
                Map datamap = (Map) request.getSession().getAttribute(CategoriesSelect_Engine.ENGINE_NAME + ".categoriesDataMap." + contextId);
                final List selectedCategories = (List) datamap.get("defaultSelectedCategories");

                while (ite.hasNext()) {
                    attachCategoryForSelectEngineToResp((Category) ite.next(), resp, root, jParams, selectedCategories, contextId);
                }
            } else {
                if (objectKey.startsWith("/")) {
                    // we are dealing with a DAV resource
                    while (ite.hasNext()) {
                        final List selectedCategories = (List) request.getSession().getAttribute(
                                TableEntry.javascriptDecode(objectKey) + Category_Field.SELECTEDCATEGORIES_ENGINEMAPKEY);
                        attachCategoryToResp((Category) ite.next(), resp, root, jParams, selectedCategories);
                    }
                } else {
                    // we are dealing with a JahiaObject
                    logger.debug("Dealing with a JahiaObject");
                    while (ite.hasNext()) {
                        attachCategoryToResp((Category) ite.next(), resp, root, jParams,
                                getSelectedCategories(jParams));
                    }

                }
            }

            resp.appendChild(root);
            sendResponse(resp, response);
        } catch (final Exception e) {
            handleException(e, request, response);
        }
        return null;
    }

    protected List getSelectedCategories(final ProcessingContext jParams) throws JahiaException, ClassNotFoundException {
        final SessionState session = jParams.getSessionState();
        final Map engineMap = (Map) session.getAttribute("jahia_session_engineMap");
        if (engineMap != null) {
            final JahiaField theField = (JahiaField) engineMap.get("theField");
            if (theField != null) {
                return (List) engineMap.get(theField.getDefinition().getName() + Category_Field.SELECTEDCATEGORIES_ENGINEMAPKEY);
            }
        }
        return new ArrayList();
    }

    protected List getSelectedPortletsCategories(final ProcessingContext jParams) {
        List result = (List) jParams.getSessionState().getAttribute("org.jahia.engines.applications.ManageApplicationCategoriesEngine.categoriesKeyList");
        if (result == null) {
            return new ArrayList();
        }
        return result;
    }

    protected List getSelectedPortletsAllsourcesCategories(final ProcessingContext jParams) {
        List result = (List) jParams.getSessionState().getAttribute("org.jahia.engines.applications.ManageApplicationCategoriesEngine.allSourcesCategoriesKeyList");
        if (result == null) {
            return new ArrayList();
        }
        return result;
    }
}