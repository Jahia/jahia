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

import org.apache.commons.lang.StringUtils;
import org.apache.pluto.driver.url.PortalURLParameter;
import org.apache.pluto.driver.url.PortalURLParser;
import org.apache.pluto.driver.url.PortalURL;
import org.apache.pluto.driver.url.impl.PortalURLParserImpl;
import org.apache.pluto.driver.url.impl.RelativePortalURLImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.portlet.PortletMode;
import javax.portlet.WindowState;
import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * 
 * User: Serge Huber
 * Date: 28 juil. 2008
 * Time: 15:13:28
 * 
 */
public class JahiaPortalURLParserImpl implements PortalURLParser {
    /**
     * Logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(PortalURLParserImpl.class);

    /**
     * The singleton parser instance.     
     */
    private static final PortalURLParser PARSER = new JahiaPortalURLParserImpl();
    private static final PortalURLParser DEFAULT_PARSER = PortalURLParserImpl.getParser();


    // Constants used for Encoding/Decoding ------------------------------------

    private static final String PREFIX = "__";
    private static final String DELIM = "_";
    private static final String PORTLET_ID = "pd";
    private static final String ACTION = "ac";
    private static final String RESOURCE = "rs";
    private static final String RESOURCE_ID = "ri";
    private static final String CACHE_LEVEL = "cl";
    private static final String RENDER_PARAM = "rp";
    private static final String PRIVATE_RENDER_PARAM = "pr";
    private static final String PUBLIC_RENDER_PARAM = "sp";
    private static final String WINDOW_STATE = "ws";
    private static final String PORTLET_MODE = "pm";
    private static final String VALUE_DELIM = "0x0";

    //This is a list of characters that need to be encoded  to be protected
    //The ? is necessary to protect URI's with a query portion that is being passed as a parameter
    private static final String[][] ENCODINGS = new String[][]{
            new String[]{"_", "0x1"},
            new String[]{".", "0x2"},
            new String[]{"/", "0x3"},
            new String[]{"\r", "0x4"},
            new String[]{"\n", "0x5"},
            new String[]{"<", "0x6"},
            new String[]{">", "0x7"},
            new String[]{" ", "0x8"},
            new String[]{"#", "0x9"},
            new String[]{"?", "0xa"},
            new String[]{"\\", "0xb"},
            new String[]{"%", "0xc"},
    };
    public static final String PORTLET_INFO = "portletInfo";

    // Constructor -------------------------------------------------------------

    /**
     * Private constructor that prevents external instantiation.
     */
    private JahiaPortalURLParserImpl() {
        // Do nothing.
    }

    /**
     * Returns the singleton parser instance.
     *
     * @return the singleton parser instance.
     */
    public static PortalURLParser getParser() {
        return PARSER;    
    }

    /**
     * Parse a servlet request to a portal URL.
     *
     * @return the portal URL.
     */
    public PortalURL parse(HttpServletRequest request) {
        final String urlBase = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
        final String contextPath = request.getContextPath();
        final String servletName = request.getServletPath();
        final String pathInfo = request.getPathInfo();
        return parse(urlBase, contextPath, servletName, pathInfo, request.getParameter(PORTLET_INFO));
    }


    /**
     * Parse a servlet request to a portal URL.
     *
     * @return the portal URL.
     */
    private PortalURL parse(String urlBase, String contextPath, String servletName, String pathInfo, String portletInfo) {
        // Construct portal URL using info retrieved from servlet request.
        PortalURL portalURL = new RelativePortalURLImpl(urlBase, contextPath, servletName+pathInfo, this);

        if (portletInfo == null) {
            if (servletName.contains(".jsp") && !servletName.endsWith(".jsp")) {
                int idx = servletName.indexOf(".jsp") + ".jsp".length();
                portletInfo = servletName.substring(idx);
                servletName = servletName.substring(0, idx);
                portalURL = new RelativePortalURLImpl(urlBase, contextPath, servletName, this);
            } else {
                return portalURL;
            }
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Parsing request pathInfo: " + portletInfo);
        }

        StringBuffer renderPath = new StringBuffer();
        StringTokenizer st = new StringTokenizer(portletInfo, "/", false);
        while (st.hasMoreTokens()) {

            String token = st.nextToken();

            // Part of the render path: append to renderPath.
            if (!token.startsWith(PREFIX)) {
//        		renderPath.append(token);
                //Fix for PLUTO-243
                renderPath.append('/').append(token);
            }
//        	 Resource window definition: portalURL.setResourceWindow().
            else if (token.startsWith(PREFIX + RESOURCE)) {
                portalURL.setResourceWindow(decodeControlParameter(token)[0]);
            }
            // Action window definition: portalURL.setActionWindow().
            else if (token.startsWith(PREFIX + ACTION)) {
                portalURL.setActionWindow(decodeControlParameter(token)[0]);
            }
            // Cacheability definition: portalURL.setCacheability().
            else if (token.startsWith(PREFIX + CACHE_LEVEL)) {
                portalURL.setCacheability(decodeControlParameter(token)[0]);
            }
            // ResourceID definition: portalURL.setResourceID().
            else if (token.startsWith(PREFIX + RESOURCE_ID)) {
                portalURL.setResourceID(decodeControlParameter(token)[0]);
            }
            // Window state definition: portalURL.setWindowState().
            else if (token.startsWith(PREFIX + WINDOW_STATE)) {
                String[] decoded = decodeControlParameter(token);
                portalURL.setWindowState(decoded[0], new WindowState(decoded[1]));
            }
            // Portlet mode definition: portalURL.setPortletMode().
            else if (token.startsWith(PREFIX + PORTLET_MODE)) {
                String[] decoded = decodeControlParameter(token);
                portalURL.setPortletMode(decoded[0], new PortletMode(decoded[1]));
            }
            // Portal URL parameter: portalURL.addParameter().
            else if (token.startsWith(PREFIX + RENDER_PARAM)) {
                String value = null;
                if (st.hasMoreTokens()) {
                    value = st.nextToken();
                }
                //set the
                PortalURLParameter param = decodeParameter(token, value);
                portalURL.addParameter(param);


            } else if (token.startsWith(PREFIX + PRIVATE_RENDER_PARAM)) {
                String value = null;
                if (st.hasMoreTokens()) {
                    value = st.nextToken();
                }
                PortalURLParameter param = decodePublicParameter(token, value);
                if (param != null) {
                    //set private (Resource) parameter in portalURL
                    portalURL.getPrivateRenderParameters().put(param.getName(), param.getValues());
                }
            } else if (token.startsWith(PREFIX + PUBLIC_RENDER_PARAM)) {
                String value = null;
                if (st.hasMoreTokens()) {
                    value = st.nextToken();
                }
                PortalURLParameter param = decodePublicParameter(token, value);
                if (param != null) {
                    //set public parameter in portalURL
                    portalURL.addPublicParameterCurrent(param.getName(), param.getValues());
                }
            }
        }
        if (renderPath.length() > 0) {
            portalURL.setRenderPath(renderPath.toString());
        }

        // Return the portal URL.
        return portalURL;
    }


    /**
     * Converts a portal URL to a URL string.
     *
     * @param portalURL the portal URL to convert.
     * @return a URL string representing the portal URL.
     */
    public String toString(PortalURL portalURL) {
        final StringBuffer buffer = new StringBuffer();

        // Append the server URI and the servlet path.
        //buffer.append(portalURL.getServletPath().startsWith("/") ? "" : "/").append(portalURL.getServletPath());
        return buffer.append(StringUtils.substringAfterLast(portalURL.getServletPath(),"/")).append("?").append(PORTLET_INFO).append("=").append(toPortletPathInfo(portalURL).replace('?','&')).toString();
    }

    /**
     * to porlet path info
     *
     * @param portalURL
     * @return
     */
    private String toPortletPathInfo(PortalURL portalURL) {
        final StringBuffer buffer = new StringBuffer();


        // Start the pathInfo with the path to the render URL (page).
        if (portalURL.getRenderPath() != null) {
            buffer.append(portalURL.getRenderPath());
        }
        //Append the resource window definition, if it exists.
        if (portalURL.getResourceWindow() != null) {
            buffer.append("/");
            buffer.append(PREFIX).append(RESOURCE)
                    .append(encodeCharacters(portalURL.getResourceWindow()));
        }
        // Append the action window definition, if it exists.
        if (portalURL.getActionWindow() != null) {
            buffer.append("/");
            buffer.append(PREFIX).append(ACTION)
                    .append(encodeCharacters(portalURL.getActionWindow()));
        }

        if (portalURL.getResourceWindow() != null) {
            if (portalURL.getCacheability() != null) {
                buffer.append("/");
                buffer.append(PREFIX).append(CACHE_LEVEL).append(encodeCharacters(portalURL.getCacheability()));
            }
            if (portalURL.getResourceID() != null) {
                buffer.append("/");
                buffer.append(PREFIX).append(RESOURCE_ID).append(encodeCharacters(portalURL.getResourceID()));
            }
        }

        // Append portlet mode definitions.
        for (Iterator it = portalURL.getPortletModes().entrySet().iterator();
             it.hasNext();) {
            Map.Entry entry = (Map.Entry) it.next();
            buffer.append("/").append(
                    encodeControlParameter(PORTLET_MODE, entry.getKey().toString(),
                            entry.getValue().toString()));
        }

        // Append window state definitions.
        for (Iterator it = portalURL.getWindowStates().entrySet().iterator();
             it.hasNext();) {
            Map.Entry entry = (Map.Entry) it.next();
            buffer.append("/").append(
                    encodeControlParameter(WINDOW_STATE, entry.getKey().toString(),
                            entry.getValue().toString()));
        }

        // Append action and render parameters.
        StringBuffer query = new StringBuffer("?");
        boolean firstParam = true;
        for (Iterator it = portalURL.getParameters().iterator();
             it.hasNext();) {

            PortalURLParameter param = (PortalURLParameter) it.next();

            // Encode action params in the query appended at the end of the URL.
            if (portalURL.getActionWindow() != null
                    && portalURL.getActionWindow().equals(param.getWindowId())
                    || (portalURL.getResourceWindow() != null
                    && portalURL.getResourceWindow().equals(param.getWindowId()))) {
                for (int i = 0; i < param.getValues().length; i++) {
                    // FIX for PLUTO-247
                    if (firstParam) {
                        firstParam = false;
                    } else {
                        query.append("&");
                    }
                    query.append(encodeQueryParam(param.getName())).append("=")
                            .append(encodeQueryParam(param.getValues()[i]));
                }
            }

            // Encode render params as a part of the URL.
            else if (param.getValues() != null
                    && param.getValues().length > 0) {
                String valueString = encodeMultiValues(param.getValues());
                if (valueString.length() > 0) {
                    buffer.append("/").append(
                            encodeControlParameter(RENDER_PARAM, param.getWindowId(),
                                    param.getName()));
                    buffer.append("/").append(valueString);
                }
            }
        }

        encode(buffer);

        if (portalURL.getResourceWindow() != null) {
            Map<String, String[]> privateParamList = portalURL.getPrivateRenderParameters();
            if (privateParamList != null) {
                for (Iterator iter = privateParamList.keySet().iterator(); iter.hasNext();) {
                    String paramname = (String) iter.next();
                    String[] tmp = privateParamList.get(paramname);
                    String valueString = encodeMultiValues(tmp);
                    if (valueString.length() > 0) {
                        buffer.append("/").append(encodePublicParamname(PRIVATE_RENDER_PARAM, paramname));
                        buffer.append("/").append(valueString);
                    }
                }
            }
        }

        Map<String, String[]> publicParamList = portalURL.getPublicParameters();
        if (publicParamList != null) {
            for (Iterator iter = publicParamList.keySet().iterator(); iter.hasNext();) {
                String paramname = (String) iter.next();
                String[] tmp = publicParamList.get(paramname);
                String valueString = encodeMultiValues(tmp);
                if (valueString.length() > 0) {
                    buffer.append("/").append(encodePublicParamname(PUBLIC_RENDER_PARAM, paramname));
                    buffer.append("/").append(valueString);
                }
            }
        }

        // Construct the string representing the portal URL.
        // Fix for PLUTO-247 - check if query string contains parameters
        if (query.length() > 1) {
            return buffer.append(query).toString();
        }

        // Construct the string representing the portal URL.
        return buffer.append(query).toString();
    }

    public static void encode(StringBuffer url) {
        replaceChar(url, "|", "%7C");
        replaceChar(url, "\"", "%22");
    }

    private static void replaceChar(StringBuffer url, String character, String change) {
        boolean contains = url.toString().contains(character);
        while (contains) {
            int index = url.indexOf(character);
            url.deleteCharAt(index);
            url.insert(index, change, 0, 3);
            contains = url.toString().contains(character);
        }
    }

    private String encodeQueryParam(String param) {
        try {
            return URLEncoder.encode(param, "UTF-8");
        }
        catch (UnsupportedEncodingException e) {
            // If this happens, we've got bigger problems.
            throw new RuntimeException(e);
        }
    }

    // Private Encoding/Decoding Methods ---------------------------------------

    /**
     * Encode a control parameter.
     *
     * @param type     the type of the control parameter, which may be:
     *                 portlet mode, window state, or render parameter.
     * @param windowId the portlet window ID.
     * @param name     the name to encode.
     */
    private String encodeControlParameter(String type,
                                          String windowId,
                                          String name) {
        StringBuffer buffer = new StringBuffer();
        buffer.append(PREFIX).append(type)
                .append(encodeCharacters(windowId))
                .append(DELIM).append(name);
        return buffer.toString();
    }

    private String encodePublicParamname(String type, String name) {
        StringBuffer buffer = new StringBuffer();
        buffer.append(PREFIX).append(type)
                .append(DELIM).append(name);
        return buffer.toString();
    }

    /**
     * Encode a string array containing multiple values into a single string.
     * This method is used to encode multiple render parameter values.
     *
     * @param values the string array to encode.
     * @return a single string containing all the values.
     */
    private String encodeMultiValues(String[] values) {
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < values.length; i++) {
            // Do not operate on the array reference
            String currentValue = values[i];
            try {
                if (currentValue != null)
                    currentValue = URLEncoder.encode(values[i], "UTF-8");
            } catch (UnsupportedEncodingException e) {
                LOG.warn(e.getMessage(), e);
            }
            buffer.append(currentValue != null ? currentValue : "");
            if (i + 1 < values.length) {
                buffer.append(VALUE_DELIM);
            }
        }
        return encodeCharacters(buffer.toString());
    }

    /**
     * Encode special characters contained in the string value.
     *
     * @param string the string value to encode.
     * @return the encoded string.
     */
    private String encodeCharacters(String string) {
        for (int i = 0; i < ENCODINGS.length; i++) {
            string = string.replace(ENCODINGS[i][0], ENCODINGS[i][1]);
        }
        return string;
    }


    /**
     * Decode a control parameter.
     *
     * @param control the control parameter to decode.
     * @return values  a pair of decoded values.
     */
    private String[] decodeControlParameter(String control) {
        String[] valuePair = new String[2];
        control = control.substring((PREFIX + PORTLET_ID).length());
        int index = control.indexOf(DELIM);
        if (index >= 0) {
            valuePair[0] = control.substring(0, index);
            valuePair[0] = decodeCharacters(valuePair[0]);
            if (index + 1 <= control.length()) {
                valuePair[1] = control.substring(index + 1);
                valuePair[1] = decodeCharacters(valuePair[1]);
            } else {
                valuePair[1] = "";
            }
        } else {
            valuePair[0] = decodeCharacters(control);
        }
        return valuePair;
    }

    /**
     * Decode a name-value pair into a portal URL parameter.
     *
     * @param name  the parameter name.
     * @param value the parameter value.
     * @return the decoded portal URL parameter.
     */
    private PortalURLParameter decodeParameter(String name, String value) {

        if (LOG.isDebugEnabled()) {
            LOG.debug("Decoding parameter: name=" + name
                    + ", value=" + value);
        }

        // Decode the name into window ID and parameter name.
        String noPrefix = name.substring((PREFIX + PORTLET_ID).length());
        String windowId = noPrefix.substring(0, noPrefix.indexOf(DELIM));
        String paramName = noPrefix.substring(noPrefix.indexOf(DELIM) + 1);

        // Decode special characters in window ID and parameter value.
        windowId = decodeCharacters(windowId);
        if (value != null) {
            value = decodeCharacters(value);
        }

        // Split multiple values into a value array.
        String[] paramValues = value.split(VALUE_DELIM);
        for (int i = 0; i < paramValues.length; i++) {
            try {
                paramValues[i] = URLDecoder.decode(paramValues[i], "UTF-8");
            } catch (UnsupportedEncodingException e) {
                LOG.warn(e.getMessage(), e);
            }
        }
        // Construct portal URL parameter and return.
        return new PortalURLParameter(windowId, paramName, paramValues);
    }

    private PortalURLParameter decodePublicParameter(String name, String value) {

        if (LOG.isDebugEnabled()) {
            LOG.debug("Decoding parameter: name=" + name
                    + ", value=" + value);
        }

//    	// Decode the name into window ID and parameter name.
        String noPrefix = name.substring((PREFIX + PORTLET_ID).length());
        String paramName = noPrefix.substring(noPrefix.indexOf(DELIM) + 1);

        // Decode special characters in parameter value.

        if (value != null) {
            value = decodeCharacters(value);
        }

        // Split multiple values into a value array.
        String[] paramValues = value.split(VALUE_DELIM);

        // Construct portal URL parameter and return.
        return new PortalURLParameter(null, paramName, paramValues);
    }

    /**
     * Decode special characters contained in the string value.
     *
     * @param string the string value to decode.
     * @return the decoded string.
     */
    private String decodeCharacters(String string) {
        for (int i = 0; i < ENCODINGS.length; i++) {
            string = string.replace(ENCODINGS[i][1], ENCODINGS[i][0]);
        }
        return string;
    }
}