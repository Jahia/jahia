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
//

package org.jahia.engines.sitemap;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.jahia.data.JahiaData;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.engines.EngineToolBox;
import org.jahia.engines.JahiaEngine;
import org.jahia.engines.validation.EngineValidationHelper;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ParamBean;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.templates.JahiaTemplateManagerService;

/** todo this engine should be modified if site map defined with tag lib !! */

/**
 * <p>Title: Jahia site map</p> <p>Description: Manage a tree view of a Site Map for a Jahia
 * Site.</p> <p>Copyright: MAP (Jahia Solutions S�rl 2002)</p> <p>Company: Jahia Solutions
 * S�rl</p>
 *
 * @author Mikhael Janson
 * @author MAP
 * @version 3.0
 */
public class SiteMap_Engine implements JahiaEngine {

    public static final String ENGINE_NAME = "sitemap";

    // Session variable that can be initialize in templates :
    // For example :
    //  jParams.getSession().setAttribute(org.jahia.engines.sitemap.SiteMap_Engine.DEFAULT_LEVEL, new Integer(2));
    public final static String DEFAULT_LEVEL = "org.jahia.engines.sitemap.SiteMap_Engine.defaultLevel";

    private static final String SITEMAP_JSP_NAME = "sitemap.jsp";

    private static final org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger (SiteMap_Engine.class);

    /**
     * Constructor
     */
    public SiteMap_Engine () {
    }


    /**
     * Authorises engine render
     *
     * @param jParams The ProcessingContext object.
     *
     * @return always true.
     */
    public boolean authoriseRender (final ProcessingContext jParams) {
        return true;
    }

    /**
     * Renders link to pop-up window
     *
     * @param jParams The ProcessingContext object.
     * @param theObj  A String object
     *
     * @return The URL to this engine.
     *
     * @throws JahiaException
     */
    public String renderLink (final ProcessingContext jParams, final Object theObj) throws JahiaException {
        String theUrl = jParams.composeEngineUrl (ENGINE_NAME, EMPTY_STRING);
        if (theObj != null)
            theUrl += theObj;
        return jParams.encodeURL (theUrl);
    }

    /**
     * Specifies if the engine needs the JahiaData object
     *
     * @param jParams The ProcessingContext object
     *
     * @return always true.
     */
    public boolean needsJahiaData (final ProcessingContext jParams) {
        return true;
    }

    /**
     * Handles the engine actions
     *
     * @param jParams a ProcessingContext object
     * @param jData   a JahiaData object
     *
     * @throws JahiaException
     */
    public EngineValidationHelper handleActions (final ProcessingContext jParams, final JahiaData jData)
            throws JahiaException {
        final Map<String, Object> engineMap = new HashMap<String, Object>();
        engineMap.put (ENGINE_OUTPUT_FILE_PARAM, getJspPath(jParams));
        engineMap.put (RENDER_TYPE_PARAM, new Integer (JahiaEngine.RENDERTYPE_FORWARD));
        engineMap.put (ENGINE_NAME_PARAM, ENGINE_NAME);
        engineMap.put (ENGINE_URL_PARAM, jParams.composeEngineUrl (ENGINE_NAME, EMPTY_STRING));

        String actualLanguageOnly = jParams.getParameter ("actualLanguageOnly");
        if (actualLanguageOnly == null) {
            actualLanguageOnly = "off";
        }
        engineMap.put("actualLanguageOnly", actualLanguageOnly);

        final HttpSession theSession = ((ParamBean) jParams).getRequest().getSession();
        theSession.removeAttribute("selectedPageOperation");
        theSession.removeAttribute("Select_Page_Entry");
        theSession.removeAttribute("jahia_session_engineMap");

        EngineToolBox.getInstance().displayScreen(jParams, engineMap);

        return null;
    }
    
    private String getJspPath(ProcessingContext ctx) {
        String path = SITEMAP_JSP_NAME;

        JahiaTemplateManagerService templateMgr = ServicesRegistry
                .getInstance().getJahiaTemplateManagerService();

        JahiaTemplatesPackage templatePackage = templateMgr
                .getTemplatePackage(ctx.getSite().getTemplatePackageName());

        if (templatePackage.getSitemapPageName() != null) {
            path = templateMgr.resolveResourcePath(templatePackage
                    .getSitemapPageName(), templatePackage.getName());
        } else {
            String jspSiteMapFileName = ctx.getPage().getPageTemplate()
                    .getSourcePath();
            path = jspSiteMapFileName.substring(0,
                    jspSiteMapFileName.lastIndexOf("/") + 1)
                    + path;
        }

        return path;
    }

    /**
     * Retrieve the engine name.
     *
     * @return the engine name.
     */
    public final String getName () {
        return ENGINE_NAME;
    }
}
