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
//

package org.jahia.engines.selectpage;

import java.util.Map;

import org.jahia.bin.Jahia;
import org.jahia.data.JahiaData;
import org.jahia.engines.JahiaEngine;
import org.jahia.engines.validation.EngineValidationHelper;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ProcessingContext;

/**
 * <p>Title: Select page engine</p> <p>Description: Display a Jahia site map that permit to the
 * user to select a DIRECT page. WARNING ! The SESSION_OBJECT object should exist and parameter
 * OPERATION set before all call to this engine !!! </p> <p>Copyright: MAP (Jahia Solutions Sarl
 * 2002)</p> <p>Company: Jahia Solutions Sarl</p>
 *
 * @author MAP
 * @deprected Use jsp directly 
 * @version $Id$
 */
public class SelectPage_Engine implements JahiaEngine {

    public static final String ENGINE_NAME = "selectpage";

    // This parameter should contain the operation that should be execute on this
    // engine. It should be initialize before calling this engine.
    public static final String OPERATION = "selectPageOperation";
    // Select page session parameters
    // Define the source page ID selected in the engine. Set from the HTML form.
    public static final String SOURCE_PAGE_ID = "sourcePageID";
    // This parameter is the container parent page ID for new container creation.
    // Correspond to the 'cpid' parameter. Useful for page edition.
    public static final String PARENT_PAGE_ID = "parentPageID";
    // The current page ID for update if exists.
    // N.B. This parameter is used when the user will change a DIRECT Jahia page
    // to a link type (URL or page link). The DIRECT type cannot be changed to a
    // link type on it's self page.
    // Set to -1 for page creation.
    public static final String PAGE_ID = "pageID";

    // homepage ID used to set the virtual site to tree-display
    public static final String HOMEPAGE_ID = "homepageID";
    public static final String SITE_ID = "siteID";


    // These operations are the same as the Page_Field engine.
    public static final String MOVE_OPERATION = "movePage";
    public static final String LINK_OPERATION = "linkJahiaPage";
    // Display any page for selection.
    public static final String SELECT_ANY_PAGE_OPERATION = "selectAnyPage";

    public static final String LANGUAGE = "lang";

    public SelectPage_Engine() {
    }

    /**
     * Autorize render.
     *
     * @param jParams ;)
     * @return Always true.
     */
    public boolean authoriseRender(ProcessingContext jParams) {
        return true;
    }

    /**
     * Compose this engine URL.
     *
     * @param jParams  The ProcessingContext.
     * @param anObject An object dont on se tape.
     * @return The URL to call this engine.
     * @throws JahiaException
     */
    public String renderLink(final ProcessingContext jParams, final Object anObject)
            throws JahiaException {
        final Map params = (Map) anObject;        
        
        final String operation = (String) params.get(OPERATION);
        final Integer parentPageID = (Integer) params.get(PARENT_PAGE_ID);
        final Integer pageID = (Integer) params.get(PAGE_ID);
        final String callback = (String) params.get("callback");
        final String templates = (String) params.get("templates");
        Integer homepageID = -1;
        Integer siteID = -1;
        if (params.get(HOMEPAGE_ID) != null)
            homepageID = (Integer) params.get(HOMEPAGE_ID);
        if (params.get(SITE_ID) != null)
            siteID = (Integer) params.get(SITE_ID);
        String language = (String) params.get(LANGUAGE);
        language = language != null ? language : ""; 
        StringBuilder link = new StringBuilder(64);
        link.append(Jahia.getContextPath()).append(
                "/engines/selectpage/select_page.jsp?homepageID=").append(
                homepageID).append("&siteID=").append(siteID)
                .append("&pageID=").append(pageID).append(
                        "&selectPageOperation=").append(operation).append(
                        "&parentPageID=").append(parentPageID).append(
                        "&callback=").append(callback).append("&lang=").append(
                        language);
        if (templates != null) {
            link.append("&templates=").append(templates);
        }
        return link.toString();
    }

    /**
     * Set if this engine needs Jahia data.
     * 
     * @param jParams
     *            The ProcessingContext.
     * @return Always false
     */
    public boolean needsJahiaData(ProcessingContext jParams) {
        return false;
    }

    /**
     * Handle the action given by the URL parameter. There are two possible action to this
     * engine. - display : Prepare datas to display the sitemap. - store : Collect datas and put
     * them in the session for the caller.
     *
     * @param jParams The ProcessingContext.
     * @param jData   The JahiaData object dont on se tape.
     * @throws JahiaException
     */
    public EngineValidationHelper handleActions(final ProcessingContext jParams, JahiaData jData)
            throws JahiaException {
        return null;
    }

    /**
     * Retrieve the engine name.
     *
     * @return the engine name.
     */
    public final String getName() {
        return ENGINE_NAME;
    }

}