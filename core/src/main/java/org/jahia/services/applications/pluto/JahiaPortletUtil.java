/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

package org.jahia.services.applications.pluto;

import org.apache.commons.collections.EnumerationUtils;
import org.apache.pluto.container.PortletWindow;
import org.jahia.data.applications.EntryPointInstance;
import org.jahia.services.content.JCRSessionFactory;

import javax.jcr.*;
import javax.jcr.nodetype.PropertyDefinition;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.*;

/**
 * User: jahia
 * Date: 26 mai 2009
 * Time: 16:58:51
 */
public class JahiaPortletUtil {
    public static final String JAHIA_SHARED_MAP = "jahiaSharedMap";
    private static org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(JahiaPortletUtil.class);
    public static final String PLUTO_PREFIX = "__";
    public static final String PLUTO_ACTION = "ac";
    public static final String PLUTO_RESOURCE = "rs";

    /**
     * Return an path without the pluto part
     * @param pathInfo
     * @return
     */
    public static String removePlutoPart(final String pathInfo) {
        if (isPortletRequest(pathInfo)) {
            return pathInfo.substring(0,pathInfo.indexOf('/'+PLUTO_PREFIX, 1));
        }
        return pathInfo;
    }

    /**
     * Return true is the request is a portlet request
     * @param pathInfo
     * @return
     */
    public static boolean isPortletRequest(final String pathInfo) {
        if (pathInfo != null) {
            StringTokenizer st = new StringTokenizer(pathInfo, "/", false);
            while (st.hasMoreTokens()) {
                String token = st.nextToken();
                // remder/resource url
                if (token.startsWith(PLUTO_PREFIX + PLUTO_RESOURCE)) {
                    return true;
                }
                // actionUrl
                else if (token.startsWith(PLUTO_PREFIX + PLUTO_ACTION)) {
                    return true;
                }
            }
        }
        return false;

    }


    /**
     * Copy some jahia attributes into attributes if the portlet request
     *
     * @param httpServletRequest the original request to copy from
     * @param window
     * @param portalRequest
     */
    public static void copyJahiaAttributes(EntryPointInstance entryPointInstance, HttpServletRequest httpServletRequest, PortletWindow window, HttpServletRequest portalRequest, boolean canModifySharedMap, String workspaceName) {
        // todo we should only add these if we are dispatching in the same context as Jahia.
        copyAttribute("renderContext", httpServletRequest, portalRequest, window);
        copyAttribute("script", httpServletRequest, portalRequest, window);
        copyAttribute("scriptInfo", httpServletRequest, portalRequest, window);
        copyAttribute("currentNode", httpServletRequest, portalRequest, window);
        copyAttribute("workspace", httpServletRequest, portalRequest, window);
        copyAttribute("currentResource", httpServletRequest, portalRequest, window);
        copyAttribute("url", httpServletRequest, portalRequest, window);

        portalRequest.setAttribute("Pluto_" + window.getId().getStringId() + "_EntryPointInstance", entryPointInstance);

        // copy  node properties
        copyNodeProperties(entryPointInstance, window, portalRequest, workspaceName);

        // copy shared map
        copySharedMapFromJahiaToPortlet(httpServletRequest, portalRequest, window, canModifySharedMap);

    }

    /**
     * Copy jahia request attibute in portlet request attribute
     *
     * @param attributeName
     * @param httpServletRequest the original request to copy from.
     * @param portalRequest
     * @param window
     */
    public static void copyAttribute(String attributeName, HttpServletRequest httpServletRequest, HttpServletRequest portalRequest, PortletWindow window) {
        copyAttribute(attributeName, httpServletRequest, portalRequest, window, null, false);
    }

    /**
     * Copy jahia session or request attribute into the portalRequest.
     *
     * @param attributeName
     * @param httpServletRequest the original request to copy from.
     * @param portalRequest
     * @param window
     * @param fromSession       true means that the attribute is in  Jahia Session else it's taked from the request
     */
    public static void copyAttribute(String attributeName, HttpServletRequest httpServletRequest, HttpServletRequest portalRequest, PortletWindow window, Object defaultValue, boolean fromSession) {
        Object objectToCopy;
        if (fromSession) {
            // get from session
            objectToCopy = httpServletRequest.getSession().getAttribute(attributeName);
            if (objectToCopy == null) {
                objectToCopy = defaultValue;
                httpServletRequest.getSession().setAttribute(attributeName, objectToCopy);
            }
        } else {
            // get from request
            objectToCopy = httpServletRequest.getAttribute(attributeName);
            if (objectToCopy == null) {
                objectToCopy = defaultValue;
                httpServletRequest.setAttribute(attributeName, objectToCopy);
            }
        }

        // add in the request attribute
        portalRequest.setAttribute(getRealAttributeName(window, attributeName), objectToCopy);

    }

    /**
     * Copy node properties into request attribute
     *
     * @param entryPointInstance
     * @param window
     * @param portalRequest
     */
    public static void copyNodeProperties(EntryPointInstance entryPointInstance, PortletWindow window, HttpServletRequest portalRequest, String workspaceName) {
        // porlet properties
        try {
            Node node = JCRSessionFactory.getInstance().getCurrentUserSession(workspaceName).getNodeByUUID(entryPointInstance.getID());
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
            logger.error(e.getMessage(), e);
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
     * @param originalRequest the original Jahia request to copy from.
     * @param portalRequest
     * @param window
     */
    public static void copySharedMapFromJahiaToPortlet(HttpServletRequest originalRequest, HttpServletRequest portalRequest, PortletWindow window, boolean canModify) {
        Map sharedMap = (Map) originalRequest.getSession().getAttribute(JAHIA_SHARED_MAP);
        if (sharedMap == null) {
            sharedMap = new HashMap();
            originalRequest.getSession().setAttribute(JAHIA_SHARED_MAP, sharedMap);
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
     * @param portalRequest
     */
    public static void copySharedMapFromPortletToJahia(HttpSession session, HttpServletRequest portalRequest, PortletWindow window) {
        Map sharedMap = (Map) portalRequest.getAttribute(getRealAttributeName(window, JAHIA_SHARED_MAP));
        session.setAttribute(JAHIA_SHARED_MAP, sharedMap);
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
        @SuppressWarnings("unchecked")
        List<String> names = EnumerationUtils.toList(portalRequest.getAttributeNames());
        for (String key : names) {
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
     *
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
