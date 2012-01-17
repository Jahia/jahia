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

//
//
//

package org.jahia.services.applications;

import java.net.URLDecoder;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jahia.params.ParamBean;

/**
 */

public class ServletIncludeRequestWrapper extends HttpServletRequestWrapper {

    private static Logger logger = LoggerFactory
            .getLogger(ServletIncludeRequestWrapper.class);

    private String emulatedContextPath;
    private boolean applicationCacheOn = false;
    private long applicationCacheExpirationDelay = -1;

    /**
     * This is a version of the wrapper for dispatcher's that don't need to emulate URLs but need Jahia Params
     * 
     * @param httpServletRequest
     */
    public ServletIncludeRequestWrapper(HttpServletRequest httpServletRequest) {
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
