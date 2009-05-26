package org.jahia.services.applications.pluto;

import org.jahia.params.ProcessingContext;
import org.apache.pluto.PortletWindow;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;

/**
 * Created by IntelliJ IDEA.
 * User: jahia
 * Date: 26 mai 2009
 * Time: 16:58:51
 */
public class JahiaPortletUtil {
    public static final String JAHIA_SHARED_MAP = "jahiaSharedMap";

    /**
     * Handle shared map
     *
     * @param processingContext
     * @param portalRequest
     * @param window
     */
    public static void copySharedMapFromJahiaToPortlet(ProcessingContext processingContext, HttpServletRequest portalRequest, PortletWindow window, boolean canModifie) {
        Map sharedMap = (Map) processingContext.getSessionState().getAttribute(JAHIA_SHARED_MAP);
        if (sharedMap == null) {
            sharedMap = new HashMap();
            processingContext.getSessionState().setAttribute(JAHIA_SHARED_MAP, sharedMap);
        }

        // add in the request attribute
        if (canModifie) {
            portalRequest.setAttribute(getRealAttributeName(window,JAHIA_SHARED_MAP), sharedMap);
        } else {
            portalRequest.setAttribute(getRealAttributeName(window,JAHIA_SHARED_MAP), Collections.unmodifiableMap(sharedMap));
        }
    }


    /**
     * Save shared map from portler request to jahia Session
     *
     * @param jParams
     * @param portalRequest
     */
    public static void copySharedMapFromPortletToJahia(ProcessingContext jParams, HttpServletRequest portalRequest, PortletWindow window) {
        Map sharedMap = (Map) portalRequest.getAttribute(getRealAttributeName(window,JAHIA_SHARED_MAP));
        jParams.getSessionState().setAttribute(JAHIA_SHARED_MAP, sharedMap);
    }

    /**
     * Get real attribute name
     * @param window
     * @param attributeName
     * @return
     */
    public static String getRealAttributeName(PortletWindow window,String attributeName) {
        return "Pluto_" + window.getId().getStringId() + "_" + attributeName;
    }

}
