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
        Integer homepageID = -1;
        Integer siteID = -1;
        if (params.get(HOMEPAGE_ID) != null)
            homepageID = (Integer) params.get(HOMEPAGE_ID);
        if (params.get(SITE_ID) != null)
            siteID = (Integer) params.get(SITE_ID);
        return Jahia.getContextPath()+"/engines/selectpage/select_page.jsp?homepageID="+
                homepageID + "&siteID="+siteID+"&pageID="+pageID+"&selectPageOperation="+operation+"&parentPageID="+parentPageID+
                "&callback="+callback;
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