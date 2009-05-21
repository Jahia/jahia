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
 package org.jahia.urls;

import org.apache.log4j.Logger;
import org.apache.regexp.RE;
import org.apache.regexp.RESyntaxException;
import java.util.Map;
import java.util.HashMap;
import java.io.UnsupportedEncodingException;

import javax.servlet.http.HttpServletResponse;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Jahia Ltd</p>
 * @author Serge Huber
 * @version 1.0
 */

public class URI {

    private static Logger logger = Logger.getLogger (URI.class);

    private static RE uriRegexp;
    static {
        try {
            uriRegexp = new RE(
                "^(([^:/?#]+):)?(//([^/?#]*))?([^?#]*)(\\?([^#]*))?(#(.*))?");
        } catch (RESyntaxException rese) {
            logger.error("Error in regexp syntax", rese);
        }
    }

    static private final Object[][] defaultSchemePortArray = {
        { "http", new Integer(80) },
        { "https", new Integer(443) }
    };

    private boolean uriStartingAtPath = false;
    private boolean relative = false;

    private String scheme = null;
    private String authority = null;
    private String userInfo = null;
    private String hostName = null;
    private int port = -1;
    private String path = null;
    private String queryString = null;
    private String fragmentString = null;

    private Map defaultSchemePorts;

    private static String defaultEncoding = URICodec.getDefaultEncoding();
    private String encoding = defaultEncoding;

    static public String getDefaultEncoding() {
        return defaultEncoding;
    }

    static public void setDefaultEncoding(String encoding) {
        defaultEncoding = encoding;
    }

    public URI() {
        init();
    }

    public URI(String uri) {
        this();
        setURI(uri);
    }

    /**
     * Allows to set/reset the uri. This makes this class mutable, but this
     * method should only be used if you are using this class as a JavaBean.
     * If the URI starts with a "/" character, this will be recorded in the
     * internal state of the URI and when the URI will be converted back to
     * String representation it will start at the path, not as a completely
     * fully qualified URI.
     * @param uri a String containing a fully qualified URI or a relative URI
     * starting with a "/" character
     */
    public void setURI(String uri) {
        if (uri == null) {
            return;
        }
        if (uri.indexOf("/") == 0) {
            uriStartingAtPath = true;
            parseURI(uri);
        } else {
            parseURI(uri);
        }
    }

    /**
     * Sets/resets the URI using an existing Java URL.
     * @param javaURL an existing valid Java URL
     */
    public void setURI(java.net.URL javaURL) {
        if (javaURL == null) {
            return;
        }
        parseURI(javaURL.toString());
    }

    /**
     * Converts the URI to a String form. This is done by remembering whether
     * the URI is a relative or absolute URI, and by outputing all the URI,
     * including the query parameters, in the order they were set.
     * Note that if you need to output URIs that contain the session
     * tracking part, you should use the toString(response) method.
     * @return a String containing the converted URI
     */
    public String toString(String anEncoding) {
        StringBuffer result = new StringBuffer();
        try {
        if (!uriStartingAtPath) {
            if (getScheme() != null) {
                    result.append(URICodec.encode(getScheme(), anEncoding,
                                                  URICodec.ALPHANUM_CHARS));
                result.append(":");
            }
            if (getAuthority() != null) {
                result.append("//");
                    result.append(URICodec.encode(getAuthority(), anEncoding,
                                                  URICodec.
                                                  AUTHORITY_AUTHORIZEDCHARS));
            }
        }
            if (getPath() != null) {
                result.append(URICodec.encode(getPath(), anEncoding,
                                              URICodec.PATH_AUTHORIZEDCHARS));
            }

        // now let's append the paramters if there are some.
        if (getQueryString() != null) {
            result.append("?");
                result.append(URICodec.encode(getQueryString(), anEncoding, URICodec.QUERY_AUTHORIZEDCHARS));
        }

        if (getFragmentString() != null) {
            result.append("#");
                result.append(URICodec.encode(getFragmentString(), anEncoding, URICodec.FRAGMENT_AUTHORIZEDCHARS));
        }
        } catch (UnsupportedEncodingException uee) {
            logger.error("Unsupported encoding: " + anEncoding, uee);
        }
        if (result.toString().length() == 0) {
            return null;
        }
        return result.toString();
    }

    /**
     * Same as the toString() method, except that it feeds the output through
     * the response.encodeURL() method to properly output session tracking
     * part.
     * @param response the HttpServletResponse object to use to encode the
     * URL with the current session ID.
     * @return a String containing the converted and encoded URL.
     */
    public String toString(HttpServletResponse response) {
        if (response == null) {
            return null;
        }
        return response.encodeURL(toString());
    }

    public String toString(HttpServletResponse response, String anEncoding) {
        if (response == null) {
            return null;
        }
        return response.encodeURL(toString(anEncoding));
    }

    public String toString() {
        return toString(encoding);
    }

    /**
     * Basically allows to decide if we want to generate a fully qualified
     * URI using the toString() methods, or a relative URI. A relative URI
     * means that the scheme :// hostname : port / will be omitted
     * @param uriStartingAtPath set to true if we want to generate this
     * URI as a relative URI.
     */
    public void setURIStartingAtPath(boolean uriStartingAtPathFlag) {
        this.uriStartingAtPath = uriStartingAtPathFlag;
    }

    /**
     * @return true if the URI will be generated as a relative URI, false
     * otherwise.
     */
    public boolean isURIStartingAtPath() {
        return uriStartingAtPath;
    }

    public String getScheme() {
        return scheme;
    }

    public void setScheme(String aScheme) {
        this.scheme = aScheme;
    }

    public String getAuthority() {
        return authority;
    }

    public void setAuthority(String anAuthority) {
        this.authority = anAuthority;
        if (anAuthority != null) {
            String curSubString = anAuthority;
            int userInfoSepPos = anAuthority.indexOf("@");
            if (userInfoSepPos > 0) {
                userInfo = anAuthority.substring(0, userInfoSepPos);
                curSubString = anAuthority.substring(userInfoSepPos+1);
            }
            int portSepPos = curSubString.indexOf(":");
            if (portSepPos > 0) {
                hostName = curSubString.substring(0, portSepPos);
                port = Integer.parseInt(curSubString.substring(portSepPos+1));
            } else {
                hostName = curSubString;
                port = getSchemeDefaultPort(scheme);
            }
        }
    }

    public String getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(String aUserInfo) {
        this.userInfo = aUserInfo;
        setAuthority(aUserInfo, hostName, port);
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String aHostName) {
        this.hostName = aHostName;
        setAuthority(userInfo, aHostName, port);
    }

    public int getPort() {
        return port;
    }

    public void setPort(int aPort) {
        this.port = aPort;
        setAuthority(userInfo, hostName, aPort);
    }

    public String getPath() {
        return path;
    }

    public void setPath(String aPath) {
        this.path = aPath;
    }

    public String getQueryString() {
        return queryString;
    }

    public void setQueryString(String aQueryString) {
        this.queryString = aQueryString;
    }

    public String getFragmentString() {
        return fragmentString;
    }

    public void setFragmentString(String aFragmentString) {
        this.fragmentString = aFragmentString;
    }

    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String anEncoding) {
        this.encoding = anEncoding;
    }

    private void parseURI(String uri) {
        synchronized (uriRegexp) {
            if (uriRegexp.match(uri)) {
                // this was taken from
                scheme = uriRegexp.getParen(2);
                setAuthority(uriRegexp.getParen(4));
                path = uriRegexp.getParen(5);
                queryString = uriRegexp.getParen(7);
                fragmentString = uriRegexp.getParen(9);

                if (authority == null) {
                    if (path.startsWith("/")) {
                        uriStartingAtPath = true;
                    } else {
                        uriStartingAtPath = false;
                        relative = true;
                    }
                } else {
                    uriStartingAtPath = false;
                }
            }
        }
    }

    private void setAuthority(String aUserInfo, String aHostName, int aPort) {
        StringBuffer result = new StringBuffer();
        if (aUserInfo != null) {
            result.append(aUserInfo);
            result.append("@");
        }
        if (aHostName != null) {
            result.append(aHostName);
        }
        if (aPort != getSchemeDefaultPort(scheme) && aPort > 0) {
            result.append(":");
            result.append(aPort);
        }
        if (result.toString().length() > 0) {
            authority = result.toString();
        } else {
            authority = null;
        }
    }

    private int getSchemeDefaultPort(String aScheme) {
        if (aScheme == null) {
            return -1;
        }
        Integer defaultPortInt = (Integer) defaultSchemePorts.get(aScheme);
        if (defaultPortInt != null) {
            return defaultPortInt.intValue();
        } else {
            return -1;
        }
    }

    private void init() {
        defaultSchemePorts = new HashMap();
        for (int i = 0; i < defaultSchemePortArray.length; i++) {
            defaultSchemePorts.put(defaultSchemePortArray[i][0], defaultSchemePortArray[i][1]);
        }
    }
    public boolean isRelative() {
        return relative;
    }
    public void setRelative(boolean relativeFlag) {
        this.relative = relativeFlag;
    }



}