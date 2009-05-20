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
//
//

package org.jahia.services.applications;

import org.apache.log4j.Logger;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaSessionExpirationException;
import org.jahia.params.ParamBean;
import org.jahia.registries.ServicesRegistry;
import org.jahia.security.license.LicenseConstants;
import org.jahia.services.usermanager.GenericPrincipal;
import org.jahia.services.usermanager.JahiaGroup;
import org.jahia.services.usermanager.JahiaUser;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpSession;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.security.Principal;
import java.util.*;

/**
 */

public class ServletIncludeRequestWrapper extends HttpServletRequestWrapper {

    private static Logger logger = Logger
            .getLogger(ServletIncludeRequestWrapper.class);

    private String emulatedContextPath;
    private boolean applicationCacheOn = false;
    private long applicationCacheExpirationDelay = -1;

    /**
     * This is a version of the wrapper for dispatcher's that don't need to emulate URLs but need Jahia Params
     * 
     * @param httpServletRequest
     *                Jahia's original request object
     */
    public ServletIncludeRequestWrapper(HttpServletRequest httpServletRequest,
            ParamBean jParams) {
        super(httpServletRequest);
        emulatedContextPath = super.getContextPath();
    }

    public String getContextPath() {
        String contextPath = super.getContextPath();
        if (logger.isDebugEnabled()) {
            logger.debug(" super.getContextPath=[" + contextPath + "]");
            logger.debug("emulatedContextPath = [" + emulatedContextPath + "]");
        }
        return emulatedContextPath;
    }

    private static String decode(String value, String encoding,
            boolean urlParameters) {
        if (value != null
                && (value.indexOf('%') >= 0 || value.indexOf('+') >= 0)) {
            try {
                value = URLDecoder.decode(value, encoding);
            } catch (Exception t) {
                logger.debug("Error during decoding", t);
            }
        }
        return value;
    }

    /**
     * Append request parameters from the specified String to the specified Map. It is presumed that the specified Map is not accessed from
     * any other thread, so no synchronization is performed.
     * <p>
     * <strong>IMPLEMENTATION NOTE</strong>: URL decoding is performed individually on the parsed name and value elements, rather than on
     * the entire query string ahead of time, to properly deal with the case where the name or value includes an encoded "=" or "&"
     * character that would otherwise be interpreted as a delimiter.
     * 
     * @param map
     *                Map that accumulates the resulting parameters
     * @param data
     *                Input string containing request parameters
     * @param encoding
     *                character encoding of the request parameter string
     * @param urlParameters
     *                true if we're parsing parameters on the URL
     * @exception IllegalArgumentException
     *                    if the data is malformed
     */
    public static void parseStringParameters(Map map, String data,
            String encoding, boolean urlParameters) {

        if (data == null || data.length() < 1)
            return;

        // logger.debug( "Parsing string [" + data + "]");

        // Initialize the variables we will require
        StringParser parser = new StringParser(data);
        boolean first = true;
        int nameStart = 0;
        int nameEnd = 0;
        int valueStart = 0;
        int valueEnd = 0;
        String name = null;
        String value = null;

        // Loop through the "name=value" entries in the input data
        while (true) {

            // Extract the name and value components
            if (first)
                first = false;
            else
                parser.advance();
            nameStart = parser.getIndex();
            nameEnd = parser.findChar('=');
            parser.advance();
            valueStart = parser.getIndex();
            valueEnd = parser.findChar('&');
            name = parser.extract(nameStart, nameEnd);
            value = parser.extract(valueStart, valueEnd);

            // A zero-length name means we are done
            if (name.length() < 1)
                break;

            // Decode the name and value if required
            name = decode(name, encoding, urlParameters);
            value = decode(value, encoding, urlParameters);

            map.put(name, value);

        }

    }

    public boolean isApplicationCacheOn() {
        return applicationCacheOn;
    }

    public long getApplicationCacheExpirationDelay() {
        return applicationCacheExpirationDelay;
    }

} // end ServletIncludeRequestWrapper
