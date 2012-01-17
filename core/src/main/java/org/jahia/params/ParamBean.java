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

/*
 * Copyright (c) 2005, Your Corporation. All Rights Reserved.
 */

//
//  ParamBean
//  EV      03.11.2000
//  EV      23.12.2000  SettingsBean now in ParamBean
//  SH      24.01.2001  added getSession accessor
//  SH      24.01.2001  added some debugging code and some comments about comportement under Orion
//	DJ		30.01.2001  added an internal wrapper for FileUpload/HttpServletRequest getParameter.
//  SH      04.02.2001  added some comments on InputStream, Parameters, WebApps problems + javadoc
//  MJ      21.03.2001  replaced basic URL parameters with PathInfo elements
//	NK		17.04.2001	added Multisite features
//	NK		14.05.2001  Jump to requested site's home page when the actual requested page is not of this site
//						instead of page not found Exception.
//	NK		11.07.2001  Added last requested page parameter
//  JB      25.10.2001  Added setOperationMode methode
//  SH      01.02.2002  Added defaultParameterValues Map to reduce URL length
//                      when using default values for engine names, operation
//                      modes, etc...
//  FH      15.08.2003  - javadoc fixes
//                      - removed redundant class casting
//                      - removed unused private attribute
//
// Development notes : for the moment this class does not handle the problematic
// of reading an input stream multiple times. This can cause problems if for
// example Jahia needs to read the InputStream object from the request before
// forwarding it to a web application. Also, under some server implementations,
// such as the Orion server, retrieving an InputStream object before reading the
// parameters will cause an error when trying to call a getParameter method.
// The main issue of all this is that we are very dependant on the implementation
// of these methods right now. A solution that was studied involved writing
// parsers for the content of the request and managing the body internally inside
// Jahia. This is probably the most solid way to do it, but it involves a lot of
// development, especially since there should be a significant performance hit
// if not done fully.
// Basically the solution would be something like this :
// 1. Jahia retrieves the InputStream (or Reader) object
// 2. Jahia retrieves the full content and stores it either in memory or on disk
// depending on some criteria to be defined (size, type of request ?)
// 3. Jahia parses the parameters included in the request body, making sure it
// then uses only the result of that parsing internally
// 4. Jahia's dispatching service can then emulate a full web container behaviour
// without much problems, since it can intercept everything, include the request
// body (which is the part of the emulation currently missing). So the application
// could indeed retrieve an InputStream or a Reader that is passed the body only
// of that application and that comes from memory or disk storage instead of the
// socket.
//
// Advantages :
// - Allows for a FULL implementation of a request object
// - Allows Jahia and web applications to process the request multiple times
// - Improved security because we could only pass the body that concerns the
// web application.
//
// Disadvantages :
// - Serious performance hit because the worst case is : Jahia processes the
// request body, parses it, forwards it to the app, that reprocesses it again !
// The current case is : Jahia forwards it directly to the webapp, not reading
// it most of the time.
// - Loss of security because in some cases an application can receive a GET
// request that actually has the body of a POST request (this is because the
// emulation replaces the URL but not the body currently, mostly for performance
// reasons).
// - More usage of resources, since request bodies should have to be stored
// in memory and/or on disk, probably multiple times.
// The current decision was not to go forward with this implementation since it
// will involve a lot of work and that the benefits are dependant on the applications
// that must run under it. If the applications do not undergo problems in the
// meantime the current solution should be sufficient.
//
/**
 * todo Implement a system to store parameters either in the session or in
 * the URL, transparently, in order to generate short URLs
 *
 * todo Implement static (or search engine) friendly URLs, such as .html ending
 * URLs
 */

package org.jahia.params;

import org.apache.commons.collections.iterators.EnumerationIterator;
import org.slf4j.Logger;
import org.jahia.bin.Jahia;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaSessionExpirationException;
import org.jahia.services.applications.ServletIncludeRequestWrapper;
import org.jahia.services.applications.ServletIncludeResponseWrapper;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.settings.SettingsBean;
import org.jahia.tools.files.FileUpload;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.jstl.core.Config;
import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.*;
import java.util.Map.Entry;

/**
 * This object contains most of the request context, including object such as the request and response objects, sessions, engines, contexts,
 * ... It also contains methods for generating URLs for output generation.
 *
 * @author Eric Vassalli
 * @author Khue NGuyen
 * @author Fulco Houkes
 * @author David Jilli
 * @author Serge Huber
 * @author Mikhael Janson
 * @author Xavier Lawrence
 */
public class ParamBean extends ProcessingContext {

    private static final transient Logger logger = org.slf4j.LoggerFactory.getLogger(ParamBean.class);

    private HttpServletRequest mRealRequest;
    private ServletIncludeRequestWrapper mRequest;
    private HttpServletResponse mRealResponse;
    // The new mResponse format is used for content caching.
    private ServletIncludeResponseWrapper mResponse = null;
    private ServletContext context;

    private FileUpload fupload;

    /**
     * Constructor needed by SerializableParamBean
     */
    ParamBean() {

    }

    /**
     * This constructor is used by AdminParamBean to create a ParamBean with the minimum data requirement needed to work within
     * JahiaAdministration Servlet Do not use this constructor within Jahia Servlet
     *
     * @param request     the HTTP request reference
     * @param response    the HTTP response reference
     * @param aContext    the servlet context reference
     * @param aStartTime  the start time in milliseconds
     * @param aHttpMethod
     * @param aSite
     * @param user
     * @throws JahiaPageNotFoundException when the requested page could not be found
     * @throws JahiaSessionExpirationException
     *                                    when the user session expired
     * @throws JahiaSiteNotFoundException when the specified site could not be found
     * @throws JahiaException             when a general internal exception occured
     */
    ParamBean(final HttpServletRequest request,
              final HttpServletResponse response, final ServletContext aContext,
              final long aStartTime, final int aHttpMethod,
              final JahiaSite aSite, final JahiaUser user) throws JahiaException {

        this.context = aContext;

        copyRequestData(request);

        // default vars
        setEngineName(CORE_ENGINE_NAME);
        setOpMode(NORMAL);

        // main vars
        mRealRequest = request;
        mRealResponse = response;

        // mResponse = new ServletIncludeResponseWrapper(response);

        setStartTime(aStartTime);
        setHttpMethod(aHttpMethod);

        setSite(aSite);
        if (aSite != null) {
            setSiteID(aSite.getID());
            setSiteKey(aSite.getSiteKey());
        }


        if (getRequest() != null) {
            final HttpSession session = getRequest().getSession();

            // last requested page
            Integer lrpID = (Integer) session
                    .getAttribute(SESSION_LAST_REQUESTED_PAGE_ID);
            if (lrpID == null) {
                lrpID = Integer.valueOf(-1);
            }
            setNewPageRequest(lrpID.intValue() != this.getPageID());
            setLastRequestedPageID(lrpID.intValue());

            // Get the current user out of the session. If there is no user
            // present, then assign the guest user to this session.
            setTheUser((JahiaUser) session.getAttribute(SESSION_USER));
            if (getTheUser() == null) {
                setUserGuest();
            }

            setUserAgent(resolveUserAgent());

            // keep the last language
            final Locale lastLocale = (Locale) session
                    .getAttribute(ParamBean.SESSION_LOCALE);
            if (lastLocale != null) {
                changeLanguage(lastLocale);
            }
        } else {
            setTheUser(user);
            if (getTheUser() == null) {
                setUserGuest();
            }
        }


    }

    /**
     * constructor EV 03.11.2000 EV 04.11.2000 now request object in parameters EV 05.11.2000 invalid page passed from critical to error EV
     * 20.11.2000 okay, everything changed... old framework, get a life
     */
    public ParamBean(final HttpServletRequest request,
                     final HttpServletResponse response,
                     final ServletContext aContext,
                     final SettingsBean jSettings,
                     final long aStartTime,
                     final int aHttpMethod) throws JahiaException {
        this(request, response, aContext, jSettings, aStartTime, aHttpMethod, null);
    }

    /**
     * constructor EV 03.11.2000 EV 04.11.2000 now request object in parameters EV 05.11.2000 invalid page passed from critical to error EV
     * 20.11.2000 okay, everything changed... old framework, get a life <p/> Xavier Lawrence: The purpose of the extraParams argument is to
     * offer the possibility to add parameters to the pathInfo that could not be passed in the URL at the time the request was sent. This
     * can happend in Struts Application when you map an URL to a Struts Action.
     */
    public ParamBean(final HttpServletRequest request,
                     final HttpServletResponse response,
                     final ServletContext aContext,
                     final SettingsBean jSettings,
                     final long aStartTime,
                     final int aHttpMethod,
                     final String extraParams)
            throws JahiaException {
        
            Jahia.setThreadParamBean(this);

            this.context = aContext;

            copyRequestData(request);

            // default vars
            setEngineName(CORE_ENGINE_NAME);
            setOpMode(NORMAL);

            // main vars
            mRealRequest = request;
            mRealResponse = response;
            // mResponse = new ServletIncludeResponseWrapper(response);
            setStartTime(aStartTime);
            setHttpMethod(aHttpMethod);

            // This call can take a lot of time the first time a session is
            // created because the servlet container uses random generators
            // for Session ID creation that are classes loaded at run-time.
            // eg. in Tomcat 4 a java.security.random class is loaded and
            // called.
            getRequest().getSession();

            // build a custom parameter map, from PathInfo
            buildCustomParameterMapFromPathInfo(getRequest(), extraParams);

            setEngineNameIfAvailable();

            resolveUser();
            setUserAgent(resolveUserAgent());

            // last engine name
            setLastEngineName((String) getRequest().getSession(false).getAttribute(SESSION_LAST_ENGINE_NAME));
            setEngineHasChanged(getLastEngineName() == null || !getLastEngineName().equals(getEngine()));

    } // end constructor

    // -------------------------------------------------------------------------
    // DJ 08.02.2001
    /**
     * Sets the current user to GUEST, in the params and in the session. Also, comes back in NORMAL mode.
     */
    public void setUserGuest() throws JahiaException {
        HttpSession session = null;
        if (getRequest() != null) {
            session = getRequest().getSession(false);
            if (session == null) {
                throw new JahiaSessionExpirationException();
            }
        }

        super.setUserGuest();

        if (session != null) {
            session.setAttribute(SESSION_USER, getUser());
        }
    }

    /**
     * Sets the current user, in the params and in the session. Also, comes back in NORMAL mode.
     */
    public void setUser(final JahiaUser user) throws JahiaException {
        HttpSession session = null;
        if (getRequest() != null) {
            session = getRequest().getSession(false);
            if (session == null) {
                throw new JahiaSessionExpirationException();
            }
        }

        super.setUser(user);

        if (session != null) {
            session.setAttribute(SESSION_USER, getUser());
        }
    }

    /**
     * accessor methods EV 03.11.2000
     */

    public HttpServletRequest getRequest() {
        return getRequestWrapper();
    }

    public ServletIncludeRequestWrapper getRequestWrapper() {
        if (mRequest == null) {
            HttpServletRequest request = getRealRequest();
            if (request != null)
                mRequest = new ServletIncludeRequestWrapper(request);
        }
        return mRequest;
    }

    public HttpServletRequest getRealRequest() {
        return mRealRequest;
    }

    public ServletIncludeResponseWrapper getResponseWrapper() {
        if (mResponse == null) {
            mResponse = new ServletIncludeResponseWrapper(getRealResponse(),
                    true, settings().getCharacterEncoding());
        }
        return mResponse;
    }

    public HttpServletResponse getResponse() {
        return getResponseWrapper();
    }

    public HttpServletResponse getRealResponse() {
        return mRealResponse;
    }

    public ServletContext getContext() {
        return context;
    }

    public int getHttpMethod() {
        return httpMethod;
    }

    // -------------------------------------------------------------------------
    // @author Khue NGuyen
    /**
     * Return the current mode in which Jahia is running. There is two main mode in which Jahia is running JahiaInterface.CORE_MODE or
     * JahiaInterface.ADMIN_MODE ( we are in administration mode ) Return -1 if not defined This mode is stored in the session as an
     * attribute with the name : ParamBean.SESSION_JAHIA_RUNNING_MODE
     *
     * @return int the Jahia running mode or -1 if not defined.
     * @throws JahiaSessionExpirationException
     *          Throw this exception when the session is null. This happens usually when the session expired.
     * @see org.jahia.bin.JahiaInterface#ADMIN_MODE
     * @see org.jahia.bin.JahiaInterface#CORE_MODE
     */

    public int getJahiaRunningMode() throws JahiaSessionExpirationException {
        final HttpSession session = getRequest().getSession(false);
        if (session == null) {
            throw new JahiaSessionExpirationException();
        }
        final Integer I = (Integer) session
                .getAttribute(SESSION_JAHIA_RUNNING_MODE);
        if (I == null)
            return -1;
        return I.intValue();
    }

    // -------------------------------------------------------------------------
    // @author Khue NGuyen
    /**
     * Return true if the current running mode is JahiaInterface.ADMIN_MODE ( we are in administration mode ).
     *
     * @return boolean
     * @throws JahiaSessionExpirationException
     *          Throw this exception when the session is null. This happens usually when the session expired.
     * @see org.jahia.bin.JahiaInterface#ADMIN_MODE
     * @see org.jahia.bin.JahiaInterface#CORE_MODE
     */

    public final boolean isInAdminMode() throws JahiaSessionExpirationException {
        final HttpSession session = getRequest().getSession(false);
        if (session == null) {
            throw new JahiaSessionExpirationException();
        }
        final Integer i = (Integer) session.getAttribute(SESSION_JAHIA_RUNNING_MODE);
        return i != null && (i.intValue() == Jahia.ADMIN_MODE);
    }

    // -------------------------------------------------------------------------
    /**
     * Return the session ID.
     *
     * @return Return the session ID.
     * @throws JahiaSessionExpirationException
     *          Throw this exception when the session is null. This happens usually when the session expired.
     */
    public String getSessionID() throws JahiaSessionExpirationException {
        final HttpSession session = getRequest().getSession(false);
        if (session == null) {
            throw new JahiaSessionExpirationException();
        }
        return session.getId();
    }

    // -------------------------------------------------------------------------
    /**
     * Return the reference on the current session.
     *
     * @return Return the session reference.
     * @throws JahiaSessionExpirationException
     *          Throw this exception when the session is null. This happens usually when the session expired.
     */

    public HttpSession getSession() throws JahiaSessionExpirationException {
        if (getRequest() == null) {
            return null;
        }
        final HttpSession session = getRequest().getSession(false);
        if (session == null) {
            throw new JahiaSessionExpirationException();
        }
        return session;
    }

    // -------------------------------------------------------------------------
    /**
     * Return the reference on the current session.
     *
     * @return Return the session reference.
     * @throws JahiaSessionExpirationException
     *          Throw this exception when the session is null. This happens usually when the session expired.
     */

    public HttpSession getSession(final boolean create)
            throws JahiaSessionExpirationException {
        HttpSession session = getRequest().getSession(create);
        if (session == null)
            throw new JahiaSessionExpirationException();
        return session;
    }

    // -------------------------------------------------------------------------
    /**
     * Invalidates the current session and replaces it with a new one, avoiding a call to getSession for the Jahia developper.
     */
    public void invalidateSession() throws JahiaSessionExpirationException {
        Object locale = null;
        if (getRequest().getSession(false) != null) {
            locale = getRequest().getSession().getAttribute(SESSION_LOCALE);
            getRequest().getSession().invalidate();
        }
        sessionState = new HttpSessionState(getRequest().getSession(true));
        sessionState.setAttribute(SESSION_LOCALE, locale);
    }

    /**
     * Change the current Locale Reinit the locales list and the entry load request too !
     *
     * @param locale
     */
    public void changeLanguage(final Locale locale) throws JahiaException {
        super.changeLanguage(locale);
        // added by PAP: for Struts and JSTL applications
        this.getSession().setAttribute("org.apache.struts.action.LOCALE", locale);
        Config.set(this.getSession(), Config.FMT_LOCALE, locale);

    }

    /**
     * Jahia's redefinition of the Servlet APIs getLocales code. This method actually builds a list that is a concatenation of multiple
     * sources : <p/> 1. the session locale representing the locales the user has chosen to surf with 2. the list of locales extracted from
     * the users' preferences 3. the list of locales extracted from the browser settings 4. the list of locales that are setup in the site
     * settings <p/> The construction of this list should be configurable, notably to be able from the site settings to say that we want to
     * avoid mixing languages, or that the user never wants to see languages that are not in his settings. <p/> Warning, this method
     * supposes that the current data has been already initialized and is available : - JahiaSite - JahiaUser - Session
     *
     * @return an List of Locale objects that contain the list of locale that are active for the current session, user and site.
     */
    @SuppressWarnings("unchecked")
    public List<Locale> getLocales(final boolean allowMixLanguages)
            throws JahiaException {

        return getLocales(allowMixLanguages,
                getRequest() != null ? new EnumerationIterator(getRequest().getLocales()) : null);
    }

    // -------------------------------------------------------------------------
    // MJ 20.03.2001
    // NK 18.05.2001 Catch malformed pathinfo exception. Stop parsing it.
    /**
     * parse the PathInfo elements and convert them to emultated request parameters. the parameters are stored in a HashMap
     * (customParameters) where they can be looked up by a customized version of this.getParameter().
     * <p/>
     * todo we might want to extend this in order to store parameter info either in the session or in the URL. Session for shorter URLs and
     * URL for bookmarking. This should be configurable in the properties, or maybe even for the page.
     *
     * @param request the HttpRequest object
     */
    private void buildCustomParameterMapFromPathInfo(
            final HttpServletRequest request, final String extraParams) {

        if (logger.isDebugEnabled()) {
            logger.debug("buildCustomParameters: " + extraParams);
        }

        // Parse the PathInfo and build a custom parameter map
        String pathInfo = null;
        try {
            String characterEncoding = request.getCharacterEncoding();
            if (characterEncoding == null)
                characterEncoding = "UTF-8";
            pathInfo = URLDecoder.decode(request.getRequestURI(),
                    characterEncoding);
            String str = request.getContextPath() + request.getServletPath();
            pathInfo = pathInfo.substring(str.length());
        } catch (Exception e) {
            pathInfo = request.getPathInfo();
        }

        buildCustomParameterMapFromPathInfo(pathInfo, extraParams, request.getServletPath());

        // Hollis : In case we have Multipart request
        // Append other parameters parsed from query string
        if (isMultipartRequest(request)) {

            final Map<String, String> queryStringParams = new HashMap<String, String>();

            ServletIncludeRequestWrapper.parseStringParameters(
                    queryStringParams, request.getQueryString(), request.getCharacterEncoding(), true);
            for (Entry<String, String> entry : queryStringParams.entrySet()) {
                getCustomParameters().put(entry.getKey(), new String[]{entry.getValue()});
            }
        }
    }

    /**
     * Extract the user agent from request headers and set our instance var userAgent to only one value.???
     */
    private String resolveUserAgent() {
        String reqUserAgent = "";
        Enumeration<?> userAgentValues = getRequest().getHeaders("user-agent");
        if (userAgentValues.hasMoreElements()) {
            // we only use the first value.
            reqUserAgent = (String) userAgentValues.nextElement();
        }
        return reqUserAgent;
    }

    /**
     * Special wrap around response.encodeURL to deactivate the cache in case jsessionid parameters are generated. This method modifies the
     * internal cacheStatus variable to modify the state.
     *
     * @param inputURL the string for the URL to encode
     * @return the encoded URL string.
     */
    public String encodeURL(final String inputURL) {
        return inputURL != null && mRealResponse != null && !inputURL.contains(";jsessionid=") ? mRealResponse.encodeURL(inputURL) : inputURL;
    }

    /**
     * Returns a String containing the full content generated by Jahia so far. Please note that calling this before all processing if
     * finished will returns incomplete content.
     *
     * @return a String containing the generated content.
     * @throws IOException if there was an error during output of content.
     */
    public String getGeneratedOutput() throws IOException {
        return getResponseWrapper().getStringBuffer();
    }

    /**
     * Returns the location set in a response redirect call.
     *
     * @return a String object that contains the location to which to redirect, or null if no redirect call has been made.
     */
    public String getRedirectLocation() {
        return getResponseWrapper().getRedirectLocation();
    }

    /**
     * Returns a String containing the actual content type used by Jahia so far. Please note that this may change over time if multiple
     * calls to the wrapper response setContentType call are made (not good :( ).
     *
     * @return a String containing the current content type.
     */
    public String getContentType() {
        return getResponseWrapper().getContentType();
    }

    @SuppressWarnings("unchecked")
    private void copyRequestData(final HttpServletRequest request) {

        if (request == null) {
            return;
        }

        if (isMultipartRequest(request)) {
            // multipart is processed only if it's not a portlet request.
            // otherwise it's the task the portlet
            if (!isPortletRequest(request)) {
                final String savePath = settings().getTmpContentDiskPath();
                final File tmp = new File(savePath);
                if (!tmp.exists()) {
                    tmp.mkdirs();
                }
                try {
                    fupload = new FileUpload(request, savePath,
                            Integer.MAX_VALUE);
                    Set<String> names = fupload.getParameterNames();
                    for (String name : names) {
                        getCustomParameters().put(name,
                                fupload.getParameterValues(name));
                    }
                } catch (IOException e) {
                    logger.error("Cannot parse multipart data !", e);
                }
            } else {
                logger
                        .debug("Mulipart request is not processed. It's the task of the portlet");
            }
        }

        // first let's copy all the parameters from the request object
        getCustomParameters().putAll(request.getParameterMap());

        setScheme(request.getScheme());
        setRequestURI(request.getRequestURI());
        setContextPath(request.getContextPath());
        setServletPath(request.getServletPath());
        setPathInfo(request.getPathInfo());
        setQueryString(request.getQueryString());
        setServerName(request.getServerName());
        setServerPort(request.getServerPort());

        setRemoteAddr(request.getRemoteAddr());

        setSessionState(new HttpSessionState(request.getSession()));
    }

    /**
     * Used internally by authorization pipeline. Do not call this method from somewhere else (such as templates)
     *
     * @param user JahiaUser
     */
    public void setTheUser(final JahiaUser user) {
        super.setTheUser(user);
        if (getRequest() != null) {
            final HttpSession session = getRequest().getSession(true);
            if (session != null) {
                session.setAttribute(SESSION_USER, user);
            }
        }
    }

    public Object getAttribute(final String attributeName) {
        return getRequest().getAttribute(attributeName);
    }

    public Object getAttributeSafeNoNullPointer(final String attributeName) {
        if (getRequest() != null) {
            return getRequest().getAttribute(attributeName);
        }
        return null;
    }

    public void setAttribute(final String attributeName,
                             final Object attributeObject) {
        getRequest().setAttribute(attributeName, attributeObject);
    }

    public void removeAttribute(final String attributeName) {
        getRequest().removeAttribute(attributeName);
    }

    @SuppressWarnings("unchecked")
    public Iterator<String> getAttributeNames() {
        return new EnumerationIterator(getRequest().getAttributeNames());
    }

    public FileUpload getFileUpload() {
        return fupload;
    }

    public String toString() {
        return "ParamBean{user: " + theUser + "; siteId: " + siteID
                + "; locale: " + currentLocale + "; session: "
                + mRequest.getSession(false) + "; ...}";
    }

} // end ParamBean
