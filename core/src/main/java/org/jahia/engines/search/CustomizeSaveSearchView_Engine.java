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

package org.jahia.engines.search;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.struts.util.RequestUtils;
import org.jahia.data.JahiaData;
import org.jahia.engines.EngineToolBox;
import org.jahia.engines.JahiaEngine;
import org.jahia.engines.validation.EngineValidationHelper;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ParamBean;
import org.jahia.params.ProcessingContext;
import org.jahia.services.search.JahiaSearchService;
import org.jahia.services.search.savedsearch.JahiaSavedSearchView;
import org.jahia.services.search.savedsearch.JahiaSavedSearchViewSettings;

/**
 * Customize save saved search view engine.
 * 
 * @author Khue Nguyen
 */
public class CustomizeSaveSearchView_Engine implements JahiaEngine {

    private static final String CONTEXT_ID = "contextId";

    /** The engine's name. */
    public static final String ENGINE_NAME = "CustomizeSaveSearchView";

    private static final transient Logger logger = Logger
            .getLogger(CustomizeSaveSearchView_Engine.class);

    // search screen
    private static final String PARAM_NAME_PREFIX = "view_";

    private static final String SAVE_SEARCH_ID = "saveSearchId";

    private static final String SEARCH_MODE = "searchMode";

    private static final String VIEW_CONFIG_NAME = "viewConfigName";

    private JahiaSearchService searchService;

    private EngineToolBox toolBox;

    /**
     * constructor
     */
    public CustomizeSaveSearchView_Engine() {
        toolBox = EngineToolBox.getInstance();
    }

    public JahiaSearchService getSearchService() {
        return searchService;
    }

    public void setSearchService(JahiaSearchService searchService) {
        this.searchService = searchService;
    }

    /**
     * authorises engine render
     */
    public boolean authoriseRender(ProcessingContext jParams) {
        return true; // always allowed to render search
    }

    /**
     * Retrieve the engine name.
     * 
     * @return the engine name.
     */
    public final String getName() {
        return CustomizeSaveSearchView_Engine.ENGINE_NAME;
    }

    private JahiaSavedSearchView getView(ProcessingContext ctx)
            throws JahiaException {
        JahiaSavedSearchView view = null;
        String searchMode = ctx.getParameter(SEARCH_MODE);
        String contextId = ctx.getParameter(CONTEXT_ID);
        String savedSearchId = ctx.getParameter(SAVE_SEARCH_ID);
        String viewConfigName = ctx.getParameter(VIEW_CONFIG_NAME);
        if (StringUtils.isEmpty(searchMode) || StringUtils.isEmpty(contextId)
                || StringUtils.isEmpty(savedSearchId)
                || StringUtils.isEmpty(viewConfigName)) {
            throw new IllegalArgumentException(
                    "Not all required parameters are provided."
                            + " Required are: " + SEARCH_MODE + ", "
                            + SAVE_SEARCH_ID + ", " + CONTEXT_ID + ", "
                            + VIEW_CONFIG_NAME);
        }

        try {
            view = searchService.getSavedSearchView(
                    Integer.valueOf(searchMode),
                    Integer.valueOf(savedSearchId), contextId, viewConfigName,
                    ctx);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(e);
        }

        return view;
    }

    /**
     * handles the engine actions
     * 
     * @param jParams
     *            a ParamBean object
     * @param jData
     *            a JahiaData object (not mandatory)
     */
    public EngineValidationHelper handleActions(ProcessingContext jParams,
            JahiaData jData) throws JahiaException {
        return handleActions(jParams, jData, true);
    }

    /**
     * handles the engine actions
     * 
     * @param jParams
     *            a ParamBean object
     * @param jData
     *            a JahiaData object (not mandatory)
     * @param displayScreen
     * @return
     * @throws JahiaException
     */
    public EngineValidationHelper handleActions(ProcessingContext jParams,
            JahiaData jData, boolean displayScreen) throws JahiaException {

        // init engineMap
        Map<String, Object> engineMap = initEngineMap(jParams, jData);
        processScreen(jParams, jData, engineMap);

        // displays the screen
        if (displayScreen) {
            toolBox.displayScreen(jParams, engineMap);
        }

        return null;
    }

    protected Map<String, Object> initEngineMap(ProcessingContext jParams, JahiaData jData)
            throws JahiaException {

        String theScreen = jParams.getParameter("screen");
        theScreen = StringUtils.isNotEmpty(theScreen) ? theScreen
                : Search_Engine.EXECUTE_SCREEN;

        Map<String, Object> engineMap = null;
        engineMap = (Map<String, Object>) jParams.getSessionState().getAttribute(
                "engineMap");
        if (engineMap == null) {
            engineMap = new HashMap<String, Object>();
        }

        if (!"cancel".equals(theScreen)) {
            JahiaSavedSearchView view = StringUtils.equals(theScreen, "save") ? (JahiaSavedSearchView) engineMap
                    .get("savedSearchView")
                    : null;

            if (view == null) {
                view = getView(jParams);
                engineMap.put("savedSearchView", view);
            }
        }

        engineMap.put("screen", theScreen);
        jParams.setAttribute("engineMap", engineMap);

        engineMap.put("jspSource", ENGINE_NAME);
        engineMap.put(RENDER_TYPE_PARAM, new Integer(
                JahiaEngine.RENDERTYPE_FORWARD));
        engineMap.put(ENGINE_NAME_PARAM,
                CustomizeSaveSearchView_Engine.ENGINE_NAME);
        engineMap.put(ENGINE_URL_PARAM, jParams.composeEngineUrl(
                CustomizeSaveSearchView_Engine.ENGINE_NAME, EMPTY_STRING));
        engineMap.put("jahiaBuild", new Integer(jParams.settings()
                .getBuildNumber()));
        engineMap.put("javascriptUrl", jParams.settings().getJsHttpPath());
        engineMap.put("noApply", "");

        jParams.getSessionState().setAttribute("engineMap", engineMap);

        return engineMap;
    }

    /**
     * specifies if the engine needs the JahiaData object
     */
    public boolean needsJahiaData(ProcessingContext processingContext) {
        return false;
    }

    private void populateData(JahiaSavedSearchViewSettings settings,
            HttpServletRequest request) {
        settings.setSelectedForAll(false);
        try {
            RequestUtils.populate(settings, PARAM_NAME_PREFIX, null, request);
        } catch (ServletException e) {
            Throwable cause = e.getRootCause() != null ? e.getRootCause() : e;
            if (cause instanceof InvocationTargetException
                    && ((InvocationTargetException) cause).getTargetException() != null) {
                cause = ((InvocationTargetException) cause)
                        .getTargetException();
            }
            logger.error("Error parsing request parameters for view settings",
                    cause);
        }
    }

    /**
     * @param jParams
     * @param jData
     * @param engineMap
     * @throws org.jahia.exceptions.JahiaException
     */
    public void processScreen(ProcessingContext jParams, JahiaData jData,
            Map<String, Object> engineMap) throws JahiaException {

        JahiaSavedSearchView view = (JahiaSavedSearchView) engineMap
                .get("savedSearchView");
        if (view == null) {
            return;
        }
        String theScreen = (String) engineMap.get("screen");
        if ("save".equals(theScreen)) {
            JahiaSavedSearchViewSettings viewSettings = view.getSettings();
            // reset fields
            viewSettings.setSelectedForAll(false);
            populateData(viewSettings, ((ParamBean) jParams).getRequest());
            searchService.updateSavedSearchView(view, jParams);

            engineMap.put("jspSource", "close");
            engineMap.remove("savedSearchView");
        } else if ("cancel".equals(theScreen)) {
            engineMap.put("jspSource", "cancel");
            engineMap.remove("savedSearchView");
        }
    }

    /**
     * renders link to pop-up window
     */
    public String renderLink(ProcessingContext jParams, Object theObj)
            throws JahiaException {
        Map<String, Object> params = (Map<String, Object>) theObj;
        String paramsStr = EMPTY_STRING;
        if (params != null) {
            StringBuilder buff = new StringBuilder();
            for (Map.Entry<String, Object> entry : (Set<Map.Entry<String, Object>>) params
                    .entrySet()) {
                buff.append("&").append(entry.getKey()).append("=").append(
                        entry.getValue());
            }
            paramsStr = buff.toString();
        }
        String theUrl = jParams.composeEngineUrl(
                CustomizeSaveSearchView_Engine.ENGINE_NAME, paramsStr);
        return jParams.encodeURL(theUrl);
    }
}
