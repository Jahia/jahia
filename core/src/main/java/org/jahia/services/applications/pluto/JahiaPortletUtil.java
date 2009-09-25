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
package org.jahia.services.applications.pluto;

import org.apache.pluto.container.PortletWindow;
import org.jahia.data.applications.EntryPointInstance;
import org.jahia.params.ParamBean;
import org.jahia.params.ProcessingContext;
import org.jahia.services.content.JCRSessionFactory;

import javax.jcr.*;
import javax.jcr.nodetype.PropertyDefinition;
import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: jahia
 * Date: 26 mai 2009
 * Time: 16:58:51
 */
public class JahiaPortletUtil {
    public static final String JAHIA_SHARED_MAP = "jahiaSharedMap";
    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(JahiaPortletUtil.class);

    /**
     * Copysome jahia attributes into attributes if the portlet request
     *
     * @param jParams
     * @param window
     * @param portalRequest
     */
    public static void copyJahiaAttributes(EntryPointInstance entryPointInstance, ParamBean jParams, PortletWindow window, HttpServletRequest portalRequest, boolean canModifySharedMap) {
        // todo we should only add these if we are dispatching in the same context as Jahia.
        copyAttribute("org.jahia.data.JahiaData", jParams, portalRequest, window);
        copyAttribute("currentRequest", jParams, portalRequest, window);
        copyAttribute("currentSite", jParams, portalRequest, window);
        copyAttribute("currentPage", jParams, portalRequest, window);
        copyAttribute("currentUser", jParams, portalRequest, window);
        copyAttribute("currentJahia", jParams, portalRequest, window);
        copyAttribute("jahia", jParams, portalRequest, window);
        copyAttribute("fieldId", jParams, portalRequest, window);
        portalRequest.setAttribute("Pluto_" + window.getId().getStringId() + "_EntryPointInstance", entryPointInstance);

        // copy  node properties
        copyNodeProperties(entryPointInstance, jParams, window, portalRequest);

        // copy shared map
        copySharedMapFromJahiaToPortlet(jParams, portalRequest, window, canModifySharedMap);

    }

    /**
     * Copy jahia request attibute in portlet request attribute
     *
     * @param attributeName
     * @param processingContext
     * @param portalRequest
     * @param window
     */
    public static void copyAttribute(String attributeName, ProcessingContext processingContext, HttpServletRequest portalRequest, PortletWindow window) {
        copyAttribute(attributeName, processingContext, portalRequest, window, null, false);
    }

    /**
     * Copy jahia session or request attribute into the portalRequest.
     *
     * @param attributeName
     * @param processingContext
     * @param portalRequest
     * @param window
     * @param fromSession       true means that the attribute is in  Jahia Session else it's taked from the request
     */
    public static void copyAttribute(String attributeName, ProcessingContext processingContext, HttpServletRequest portalRequest, PortletWindow window, Object defaultValue, boolean fromSession) {
        Object objectToCopy;
        if (fromSession) {
            // get from session
            objectToCopy = processingContext.getSessionState().getAttribute(attributeName);
            if (objectToCopy == null) {
                objectToCopy = defaultValue;
                processingContext.getSessionState().setAttribute(attributeName, objectToCopy);
            }
        } else {
            // get from request
            objectToCopy = processingContext.getAttribute(attributeName);
            if (objectToCopy == null) {
                objectToCopy = defaultValue;
                processingContext.setAttribute(attributeName, objectToCopy);
            }
        }

        // add in the request attribute
        portalRequest.setAttribute(getRealAttributeName(window, attributeName), objectToCopy);

    }

    /**
     * Copy node properties into request attribute
     *
     * @param entryPointInstance
     * @param jParams
     * @param window
     * @param portalRequest
     */
    public static void copyNodeProperties(EntryPointInstance entryPointInstance, ParamBean jParams, PortletWindow window, HttpServletRequest portalRequest) {
        // porlet properties
        try {
            Node node = JCRSessionFactory.getInstance().getThreadSession(jParams.getUser()).getNodeByUUID(entryPointInstance.getID());
            if (node != null) {
                PropertyIterator propertyIterator = node.getProperties();
                if (propertyIterator != null) {
                    while (propertyIterator.hasNext()) {
                        Property property = propertyIterator.nextProperty();
                        PropertyDefinition def = property.getDefinition();
                        String propName = def.getName();
                        // create the corresponding GWT bean
                        if (!def.isMultiple()) {
                            portalRequest.setAttribute(getRealAttributeName(window, propName), convertValue(property.getValue()));
                        } else {
                            portalRequest.setAttribute(getRealAttributeName(window, propName), convertValues(property.getValues()));
                        }
                    }
                }
            }

        } catch (RepositoryException e) {
            logger.error(e, e);
        }
    }

    /**
     * convert Values[] jcr object to Object[]
     *
     * @param val
     * @return
     * @throws RepositoryException
     */
    public static Object convertValues(Value val[]) throws RepositoryException {
        if (val == null) {
            return null;
        }
        Object[] o = new Object[val.length];
        for (int i = 0; i < val.length; i++) {
            o[i] = convertValue(val[i]);
        }
        return o;
    }

    /**
     * Convert Value jcr object to Object
     *
     * @param val
     * @return
     * @throws RepositoryException
     */
    public static Object convertValue(Value val) throws RepositoryException {
        Object theValue;
        if (val == null) {
            return null;
        }
        switch (val.getType()) {
            case PropertyType.BINARY:
                theValue = val.getString();
                break;
            case PropertyType.BOOLEAN:
                theValue = Boolean.valueOf(val.getBoolean());
                break;
            case PropertyType.DATE:
                theValue = val.getDate();
                break;
            case PropertyType.DOUBLE:
                theValue = Double.valueOf(val.getDouble());
                break;
            case PropertyType.LONG:
                theValue = Long.valueOf(val.getLong());
                break;
            case PropertyType.NAME:
                theValue = val.getString();
                break;
            case PropertyType.PATH:
                theValue = val.getString();
                break;
            case PropertyType.REFERENCE:
                theValue = val.getString();
                break;
            case PropertyType.STRING:
                theValue = val.getString();
                break;
            case PropertyType.UNDEFINED:
                theValue = val.getString();
                break;
            default:
                theValue = val.getString();
        }
        return theValue;
    }

    /**
     * Handle shared map
     *
     * @param processingContext
     * @param portalRequest
     * @param window
     */
    public static void copySharedMapFromJahiaToPortlet(ProcessingContext processingContext, HttpServletRequest portalRequest, PortletWindow window, boolean canModify) {
        Map sharedMap = (Map) processingContext.getSessionState().getAttribute(JAHIA_SHARED_MAP);
        if (sharedMap == null) {
            sharedMap = new HashMap();
            processingContext.getSessionState().setAttribute(JAHIA_SHARED_MAP, sharedMap);
        }

        // add in the request attribute
        if (canModify) {
            portalRequest.setAttribute(getRealAttributeName(window, JAHIA_SHARED_MAP), sharedMap);
        } else {
            portalRequest.setAttribute(getRealAttributeName(window, JAHIA_SHARED_MAP), Collections.unmodifiableMap(sharedMap));
        }
    }


    /**
     * Save shared map from portler request to jahia Session
     *
     * @param jParams
     * @param portalRequest
     */
    public static void copySharedMapFromPortletToJahia(ProcessingContext jParams, HttpServletRequest portalRequest, PortletWindow window) {
        Map sharedMap = (Map) portalRequest.getAttribute(getRealAttributeName(window, JAHIA_SHARED_MAP));
        jParams.getSessionState().setAttribute(JAHIA_SHARED_MAP, sharedMap);
    }

    /**
     * Get real attribute name
     *
     * @param window
     * @param attributeName
     * @return
     */
    public static String getRealAttributeName(PortletWindow window, String attributeName) {
        return "Pluto_" + window.getId().getStringId() + "_" + attributeName;
    }

    /**
     * Remove from request useless attributes
     *
     * @param portalRequest
     * @return
     */
    public static Map<String, Object> filterJahiaAttributes(HttpServletRequest portalRequest) {
        HashMap<String, Object> map = new HashMap<String, Object>();
        Enumeration<String> enume = portalRequest.getAttributeNames();
        while (enume.hasMoreElements()) {
            String key = enume.nextElement();
            if (isSpringAttribute(key)) {
                Object value = portalRequest.getAttribute(key);
                map.put(key, value);
                portalRequest.removeAttribute(key);
            }
        }
        return map;
    }

    /**
     * true if it's a spring attr.
     *
     * @param key
     * @return
     */
    private static boolean isSpringAttribute(String key) {
        if (key != null && key.indexOf("org.springframework") == 0) {
            return true;
        }
        return false;
    }

    /**
     * Set jahia attributes
     * @param portalRequest
     * @param jahiaAttributes
     */
    public static void setJahiaAttributes(HttpServletRequest portalRequest, Map<String, Object> jahiaAttributes) {
        Set<String> keys = jahiaAttributes.keySet();
        Iterator<String> keysIte = keys.iterator();
        while (keysIte.hasNext()) {
            String key = keysIte.next();
            Object value = portalRequest.getAttribute(key);
            portalRequest.setAttribute(key, value);
        }
    }
}
