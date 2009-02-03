/**
 * 
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
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
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

/*
 * Copyright (c) 2005, Your Corporation. All Rights Reserved.
 */

//
//  ProcessingContext
//  EV      03.11.2000
//  EV      23.12.2000  SettingsBean now in ProcessingContext
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

import org.apache.commons.lang.math.NumberUtils;
import org.apache.log4j.Logger;
import org.jahia.bin.Jahia;
import org.jahia.bin.JahiaInterface;
import org.jahia.content.ContentPageKey;
import org.jahia.engines.importexport.ExportEngine;
import org.jahia.engines.login.Login_Engine;
import org.jahia.exceptions.*;
import org.jahia.pipelines.Pipeline;
import org.jahia.pipelines.PipelineException;
import org.jahia.registries.ServicesRegistry;
import org.jahia.security.license.LicenseActionChecker;
import org.jahia.services.acl.JahiaBaseACL;
import org.jahia.services.fields.ContentField;
import org.jahia.services.lock.LockKey;
import org.jahia.services.lock.LockService;
import org.jahia.services.pages.ContentPage;
import org.jahia.services.pages.JahiaPage;
import org.jahia.services.pages.PageProperty;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.sites.SiteLanguageMapping;
import org.jahia.services.sites.SiteLanguageSettings;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaUserAliasing;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.jahia.services.version.EntryLoadRequest;
import org.jahia.services.version.StateModificationContext;
import org.jahia.settings.SettingsBean;
import org.jahia.utils.LanguageCodeConverters;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * This object contains most of the request context, including object such as the request and response objects, sessions, engines, contexts,
 * ... It also contains methods for generating URLs for output generation.
 *
 * @author Serge Huber
 * @author Xavier Lawrence
 */
public class ProcessingContext {

    private static final transient Logger logger = Logger.getLogger(ProcessingContext.class);

    public static final String ENGINE_NAME_PARAMETER = "engineName";

    public static final String SITE_KEY_PARAMETER = "site";
    public static final String PAGE_ID_PARAMETER = "pid"; // refers to the same as current page or new requested page
    public static final String CONTAINERLIST_ID_PARAMETER = "clid";
    public static final String CONTAINER_ID_PARAMETER = "cid";
    public static final String FIELD_ID_PARAMETER = "fid";
    public static final String APPUNIQUE_ID_PARAMETER = "appid";

    public static final String OPERATION_MODE_PARAMETER = "op";
    public static final String USERALIASING_MODE_PARAMETER = "useraliasing";
    public static final String CACHE_MODE_PARAMETER = "cache";
    public static final String CONTAINERCACHE_MODE_PARAMETER = "containercache";
    public static final String ENTRY_STATE_PARAMETER = "entrystate";
    public static final String SHOW_REVISION_DIFF_PARAMETER = "showrevdiff";
    public static final String VALIDATE_PARAMETER = "validate";
    public static final String CONTAINER_SCROLL_PREFIX_PARAMETER = "ctnscroll_";
    public static final String LANGUAGE_CODE = "lang";
    public static final String STEAL_LOCK = "stealLock";
    public static final String RELEASE_LOCK = "releaseLock";
    public static final String TEMPLATE_PARAMETER = "template";

    public static final String DEFAULT_SITE_PROPERTY = "defaultSite";

    /**
     * default page id
     */
    public static final String DEFAULT_PAGE_ID = "1";

    /**
     * Engine core name
     */
    public static final String CORE_ENGINE_NAME = "core";
    protected static final String XMLSOURCE_ENGINE_NAME = "xmlsource";

    // http modes
    public static final int GET_METHOD = 1;
    public static final int POST_METHOD = 2;

    // content filtering status
    public static final int FILTER_ENABLED_STATE = 1;
    public static final int FILTER_DISABLED_STATE = 0;
    public static final int FILTER_UNDEFINED_STATE = -1;

    // navigation operations
    public static final String NORMAL = "normal"; // normal navigation
    public static final String EDIT = "edit"; // edit navigation
    public static final String DEBUG = "debug"; // debug navigation
    public static final String PREVIEW = "preview"; // preview staging navigation
    public static final String COMPARE = "compare"; // show difference between staging and active

    // cache modes
    public static final String CACHE_ON = "on";

    // session names
    public static final String SESSION_USER = "org.jahia.usermanager.jahiauser";
    public static final String SESSION_ALIASED_USER = "org.jahia.usermanager.aliaseduser";
    public static final String SESSION_ALIASING_ROOT_USER = "org.jahia.usermanager.aliasingrootuser";
    public static final String SESSION_ADV_PREVIEW_SETTINGS = "org.jahia.advpreview.settings";
    public static final String SESSION_ADV_COMPARE_MODE_SETTINGS = "org.jahia.advcomparemode.settings";
    public static final String SESSION_SITE = "org.jahia.services.sites.jahiasite";
    public static final String SESSION_DEFAULT_SITE = "org.jahia.services.sites.jahiadefaultsite";
    public static final String SESSION_LAST_REQUESTED_PAGE_ID = "org.jahia.params.lastrequestedpageid";
    public static final String SESSION_LAST_DISPLAYED_PAGE_ID = "org.jahia.params.lastdisplayedpageid";
    public static final String SESSION_LAST_ENGINE_NAME = "org.jahia.engines.lastenginename";
    public static final String SESSION_JAHIA_RUNNING_MODE = "org.jahia.bin.jahiarunningmode";
    public static final String SESSION_JAHIA_ENGINEMAP = "jahia_session_engineMap";
    public static final String SESSION_LOCALE = "org.jahia.services.multilang.currentlocale";
    public static final String SESSION_BACKUP = "org.jahia.session.backup";
    public static final String SESSION_LOCALE_ENGINE = "org.jahia.services.multilang.currentlocaleforengine";

    public static final String USERALIASING_MODE_ON = "on";
    public static final String USERALIASING_MODE_OFF = "off";

    protected long startTime = 0;
    protected int httpMethod = 0;
    protected String engineName = "";
    protected int fieldID = 0;
    protected int containerID = 0;
    protected int containerListID = 0;
    protected String opMode = "";
    protected JahiaPage thePage;
    protected ContentPage contentPage;
    protected JahiaUser theUser = null;
    protected String userAgent = "";
    protected Locale currentLocale = null;
    // a list of Locale objects that contains the current user preferences
    protected List<Locale> localeList = null;
    // private int jahiaID = -1; // FIXME_MULTISITE Hollis: jahiaID = siteID redondant info
    // DaDa'S requested a Server key in the Datasourcing context
    // So Sirdrake what is your opinion ? It's your baby so choose
    // and let me know.
    protected String anchor = null;
    protected int siteID = -1;
    protected String siteKey = null;
    protected JahiaSite site;
    protected boolean siteHasChanged = false;

    protected Map<String, Object> customParameters = new HashMap<String, Object>();

    protected int lastRequestedPageID = 0;
    protected boolean newPageRequest = false; // true , if a new page is requested
    protected String lastEngineName = null;
    protected boolean engineHasChanged = false; // true , if the current engine differs with the previous engine

    protected Date cacheExpirationDate = null; // the date at which the current page cache will expire.

    protected EntryLoadRequest entryLoadRequest = new EntryLoadRequest(EntryLoadRequest.CURRENT);

    protected EntryLoadRequest substituteEntryLoadRequest = null;

    protected static Properties defaultParameterValues; // stores the default values for parameters.

    protected boolean useQueryStringParameterUrl = false;

    protected int diffVersionID = 0;

    protected List<String> pageURLKeys = new ArrayList<String>();

    protected int jahiaRunningMode = JahiaInterface.CORE_MODE;
    protected boolean inAdminMode = false;

    protected String responseMimeType = "text/html";

    protected SessionState sessionState = null;

    private String scheme;
    private String requestURI;
    private String contextPath;
    private String servletPath;
    private String pathInfo;
    private String queryString;
    private String serverName;
    private int serverPort = 80;

    private String remoteAddr;
    private String remoteHost;

    private String characterEncoding;

    private String generatedOutput;
    private String redirectLocation;
    private String contentType;

    private URLGenerator urlGenerator = new BasicURLGeneratorImpl();

    private Map<String, Object> attributeMap = new HashMap<String, Object>();

    // normally the ProcessingContext should not add ESI-specific markers, such as "/jesi/user_dklfjsljf/",
    // to the generated urls so we set this to true by default.
    boolean ignoreEsiParams = true;

    private ThreadLocal<Map<String, Integer>> filtersStatus = new ThreadLocal<Map<String, Integer>>();

    private boolean forceAppendSiteKey = false;
    private boolean siteResolvedByKeyOrPageId;
    private boolean siteResolvedByServername;
    private boolean contentPageLoadedWhileTryingToFindSiteByPageID;
    private Boolean canEdit = null;

    static {
        /**
         * todo we might want to put this in a configuration file so the administrator can change it.
         */
        // static constructor for defaultParameterValues;
        setDefaultParameterValues(new Properties());
        getDefaultParameterValues().setProperty(
                ProcessingContext.ENGINE_NAME_PARAMETER,
                ProcessingContext.CORE_ENGINE_NAME);
        getDefaultParameterValues().setProperty(
                ProcessingContext.OPERATION_MODE_PARAMETER,
                ProcessingContext.NORMAL);
        getDefaultParameterValues().setProperty(
                ProcessingContext.SITE_KEY_PARAMETER, "");
        getDefaultParameterValues().setProperty(
                ProcessingContext.CACHE_MODE_PARAMETER,
                ProcessingContext.CACHE_ON);
        getDefaultParameterValues().setProperty(
                ProcessingContext.SHOW_REVISION_DIFF_PARAMETER,
                "0");
        // defaultParameterValues.setProperty(ProcessingContext.PAGE_ID_PARAMETER, ProcessingContext.DEFAULT_PAGE_ID); // doesn't work yet
        // because of logout engine URL mess
    }

    protected static final ServicesRegistry REGISTRY = ServicesRegistry.getInstance();
    private long delayFromNow = -1;

    /**
     * Default constructor
     */
    public ProcessingContext() {
        super();
        Jahia.setThreadParamBean(this);
    }

    /**
     * This constructor is used by AdminProcessingContext to create a ProcessingContext with the minimum data requirement needed to work
     * within JahiaAdministration Servlet Do not use this constructor within Jahia Servlet
     *
     * @param jSettings    the Jahia settings
     * @param aStartTime   the start time in milliseconds
     * @param aSite
     * @param user
     * @param aContentPage
     * @throws JahiaSessionExpirationException
     *                                    when the user session expired
     * @throws JahiaSiteNotFoundException when the specified site could not be found
     * @throws JahiaException             when a general internal exception occured
     *                                    when the user session expired
     * @throws JahiaSiteNotFoundException when the specified site could not be found
     * @throws JahiaException             when a general internal exception occured
     */
    public ProcessingContext(final SettingsBean jSettings,
                             final long aStartTime,
                             final JahiaSite aSite,
                             final JahiaUser user,
                             final ContentPage aContentPage) throws JahiaException {
        this(jSettings, aStartTime, aSite, user, aContentPage, new EntryLoadRequest(EntryLoadRequest.STAGED), NORMAL);
    }

    /**
     * This constructor is used by AdminProcessingContext to create a ProcessingContext with the minimum data requirement needed to work
     * within JahiaAdministration Servlet Do not use this constructor within Jahia Servlet
     *
     * @param jSettings    the Jahia settings
     * @param aStartTime   the start time in milliseconds
     * @param aSite
     * @param user
     * @param aContentPage
     * @param operationMode operation mode
     * @throws JahiaSessionExpirationException
     *                                    when the user session expired
     * @throws JahiaSiteNotFoundException when the specified site could not be found
     * @throws JahiaException             when a general internal exception occured
     */
    public ProcessingContext(final SettingsBean jSettings,
                             final long aStartTime,
                             final JahiaSite aSite,
                             final JahiaUser user,
                             final ContentPage aContentPage, String operationMode) throws JahiaException {
        this(jSettings, aStartTime, aSite, user, aContentPage, new EntryLoadRequest(EntryLoadRequest.STAGED), operationMode);
    }

    public ProcessingContext(final SettingsBean jSettings,
                             final long aStartTime,
                             final JahiaSite aSite,
                             final JahiaUser user,
                             final ContentPage aContentPage,
                             final EntryLoadRequest loadRequest,
                             final String operationMode)
            throws JahiaException {
        Jahia.setThreadParamBean(this);
        // default vars
        setEngineName(CORE_ENGINE_NAME);
        setOpMode(operationMode);

        setStartTime(aStartTime);

        setSite(aSite);
        if (aSite != null) {
            setSiteID(aSite.getID());
            setSiteKey(aSite.getSiteKey());
        }
        entryLoadRequest = loadRequest;

        setSessionState(new BasicSessionState(Long.toString(System
                .currentTimeMillis())));

        setTheUser(user);
        if (getTheUser() == null) {
            setUserGuest();
        }
        if (aContentPage != null) {
            setContentPage(aContentPage);
            setThePage(aContentPage.getPage(getEntryLoadRequest(),
                    getOperationMode(), getUser()));
        }
    }

    /**
     * For use when specific ProcessingContext parameters should be given to the constructor but cannot be put in the original pathInfo
     * (Struts AJAX Actions for example)
     */
    public ProcessingContext(final long aStartTime, final String extraParams)
            throws JahiaException {

        try {
            Jahia.setThreadParamBean(this);

            // default vars
            setEngineName(CORE_ENGINE_NAME);
            setOpMode(NORMAL);

            setStartTime(aStartTime);

            // logger.debug("Looking up session...");

            // build a custom parameter map, from PathInfo
            buildCustomParameterMapFromPathInfo(getPathInfo(), extraParams, "");

            setEngineNameIfAvailable();

            // retrieve site info
            if (findSiteFromWhatWeHave() == false) {
                throw new JahiaSiteNotFoundException(
                        "400 Bad Request : No site specified or site not found",
                        JahiaException.CRITICAL_SEVERITY);
            }

            if (!LicenseActionChecker.isAuthorizedByLicense(
                    "org.jahia.actions.server.admin.sites.ManageSites", 0)) {
                // we don't have the right to add sites, so we can only use the default site, so
                // we force the site resolution to the default site.
                setSite(getDefaultSite());
            }

            setSiteInfoFromSiteFound();

            if (!isContentPageLoadedWhileTryingToFindSiteByPageID()) {
                if (isPageRequestedByID()) {
                    setContentPageToPageWithID();
                } else if (isPageRequestedByKey()) {
                    setContentPageToPageWithURLKey();
                } else {
                    setContentPage(getSite().getHomeContentPage());
                }
            }

            resolveUser();

            if (getSite() != null) {
                setFieldIDIfAvailable();
                setContainerIDIfAvailable();
                setContainerListIDIfAvailable();

                final int pageID = resolvePageID();

                resolveOpMode(getSessionState());

                resolveLocales();

                processLockAction();

                final String verInfo = resolveEntryState();

                resolveDiffVersionID(verInfo);

                checkLocales();

                checkPageAccess(pageID);

            }

            resolveJahiaPage();

            // last engine name
            this.setLastEngineName((String) getSessionState().getAttribute(
                    SESSION_LAST_ENGINE_NAME));
            this
                    .setEngineHasChanged((getLastEngineName() == null || !getLastEngineName()
                            .equals(getEngine())));

            resolveCacheStatus();

            processActivationAction();

            // /////////////////////////////////////////////////////////////////////////////////////
            // FIXME -Fulco-
            //
            // hmmmmm, this catch has no reason to be here! This exception should be catched
            // where the numeric convertion takes place, and not here!!!
            //
            // /////////////////////////////////////////////////////////////////////////////////////

        } catch (NumberFormatException nfe) {
            final String errorMsg = "Error in translating number : "
                    + nfe.getMessage() + " -> BAILING OUT";
            logger.debug(errorMsg, nfe);
            throw new JahiaException("Error in request parameters", errorMsg,
                    JahiaException.PAGE_ERROR, JahiaException.ERROR_SEVERITY,
                    nfe);
        }

    }

    /**
     * constructor EV 03.11.2000 EV 04.11.2000 now request object in parameters EV 05.11.2000 invalid page passed from critical to error EV
     * 20.11.2000 okay, everything changed... old framework, get a life
     */
    public ProcessingContext(final long aStartTime) throws JahiaException {
        this(aStartTime, null);

    } // end constructor

    /**
     * Set the engine name if it was specified in request.
     */
    protected void setEngineNameIfAvailable() {
        if (getParameter(ENGINE_NAME_PARAMETER) == null)
            return;
        this.engineName = getParameter(ENGINE_NAME_PARAMETER);
    }

    protected void setContainerListIDIfAvailable() {
        final String containerListIDStr = getParameter(CONTAINERLIST_ID_PARAMETER);
        if (containerListIDStr != null) {
            try {
                this.setContainerListID(Integer.parseInt(containerListIDStr));
            } catch (NumberFormatException nfe) {
                logger.warn("Invalid container list ID [" + containerListIDStr
                        + "] in request, ignoring...");
                this.setContainerListID(0);
            }
        }
    }

    protected void setFieldIDIfAvailable() {
        final String fieldIDStr = getParameter(FIELD_ID_PARAMETER);
        if (fieldIDStr != null) {
            try {
                this.setFieldID(Integer.parseInt(fieldIDStr));
            } catch (NumberFormatException nfe) {
                logger.warn("Invalid field ID [" + fieldIDStr
                        + "] in request, ignoring...");
                this.setFieldID(0);
            }
        }
    }

    protected void setContainerIDIfAvailable() {
        final String containerIDStr = getParameter(CONTAINER_ID_PARAMETER);
        if (containerIDStr != null) {
            try {
                this.setContainerID(Integer.parseInt(containerIDStr));
            } catch (NumberFormatException nfe) {
                logger.warn("Invalid container ID [" + containerIDStr
                        + "] in request, ignoring...");
                this.setContainerID(0);
            }
        }
    }

    /**
     * Sets the current user to GUEST, in the params and in the session. Also, comes back in NORMAL mode.
     */
    public void setUserGuest() throws JahiaException {
        // get the User Manager service instance.
        final JahiaUserManagerService userMgr = REGISTRY
                .getJahiaUserManagerService();
        setTheUser(userMgr.lookupUser(
                JahiaUserManagerService.GUEST_USERNAME));
        setOperationMode(NORMAL);
    }

    /**
     * Sets the current user, in the params and in the session. Also, comes back in NORMAL mode.
     */
    public void setUser(final JahiaUser user) throws  JahiaException {
        setTheUser(user);
        flushLocaleListCache();
        getEntryLoadRequest().setLocales(getLocales());
        setOperationMode(NORMAL);
    }

    /**
     * accessor methods EV 03.11.2000
     */
    /*
     * public JahiaPrivateSettings settings() { return org.jahia.settings.SettingsBean.getInstance(); }
     */
    public SettingsBean settings() {
        return org.jahia.settings.SettingsBean.getInstance();
    }

    public long getStartTime() {
        return startTime;
    }

    public String getEngine() {
        return getEngineName();
    }

    public String getOperationMode() {
        return getOpMode();
    }

    public ContentPage getContentPage() {
        return contentPage;
    }

    public JahiaPage getPage() {
        return getThePage();
    }

    public int getPageID() {
        if (getContentPage() == null)
            return -1;
        return getContentPage().getID();
    }

    public int getLastRequestedPageID() {
        return lastRequestedPageID;
    }

    public boolean newPageRequest() {
        return isNewPageRequest();
    }

    public String getLastEngineName() {
        return lastEngineName;
    }

    public boolean engineHasChanged() {
        return isEngineHasChanged();
    }

    public JahiaUser getUser() {
        return getTheUser();
    }

    public String getUserAgent() {
        return userAgent;
    }

    public int getFieldID() {
        return fieldID;
    }

    public int getContainerID() {
        return containerID;
    }

    public int getContainerListID() {
        return containerListID;
    }

    public int getSiteID() {
        return siteID;
    }

    public String getSiteKey() {
        return siteKey;
    }

    public int getJahiaID() {
        return getSiteID();
    } // Hollis : For backward compatibility, but ...

    public JahiaSite getSite() {
        return site;
    }

    public EntryLoadRequest getEntryLoadRequest() {
        if (getSubstituteEntryLoadRequest() != null) {
            return getSubstituteEntryLoadRequest();
        } else {
            return entryLoadRequest;
        }
    }

    public void setEntryLoadRequest(final EntryLoadRequest anEntryLoadRequest) {
        this.entryLoadRequest = anEntryLoadRequest;
    }

    public void setSubstituteEntryLoadRequest(
            final EntryLoadRequest anEntryLoadRequest) {
        this.substituteEntryLoadRequest = anEntryLoadRequest;
    }

    public void resetSubstituteEntryLoadRequest() {
        this.setSubstituteEntryLoadRequest(null);
    }

    public int getDiffVersionID() {
        return this.diffVersionID;
    }

    public boolean showRevisionDiff() {
        return (this.getDiffVersionID() != 0);
    }

    /**
     * false if the ProcessingContext adds ESI-specific markers, such as "/jesi/user_dklfjsljf/", to the generated urls. true by default.
     *
     * @return
     */
    public boolean isIgnoreEsiParams() {
        return !(this instanceof ParamBean) && ignoreEsiParams;
    }

    /**
     * set to false if the ProcessingContext should add ESI-specific markers, such as "/jesi/user_dklfjsljf/", to the generated urls. true
     * by default.
     *
     * @param ignoreEsiParamsFlag
     */
    public void setIgnoreEsiParams(boolean ignoreEsiParamsFlag) {
        this.ignoreEsiParams = ignoreEsiParamsFlag;
    }

    // @author Serge Huber shuber@jahia.org

    /**
     * Sets the Operation Mode for this request to the specified value.
     */
    public void setOperationMode(final String newOperationMode)
            throws JahiaException {
        setOpMode(newOperationMode);
        EntryLoadRequest newLoadRequest = new EntryLoadRequest(
                EntryLoadRequest.ACTIVE_WORKFLOW_STATE, 0, getLocales());
        // compute version info
        if (this.getSiteID() != -1) {
            if (((EDIT.equals(getOpMode())) || (PREVIEW.equals(getOpMode())) || (COMPARE.equals(getOpMode())))
                    && (REGISTRY.getJahiaVersionService().isStagingEnabled(this.getSiteID()))) {
                newLoadRequest = new EntryLoadRequest(
                        EntryLoadRequest.STAGING_WORKFLOW_STATE,
                        0,
                        getLocales());
                // this.cacheStatus = ProcessingContext.CACHE_OFF; deactivated because
                // we can't see a reason to have this here.
            }
        }
        if (COMPARE.equals(getOpMode())) {
            newLoadRequest.setWithDeleted(true);
            newLoadRequest.setWithMarkedForDeletion(true);
        } else if (EDIT.equals(getOpMode())) {
            newLoadRequest.setWithDeleted(false);
            newLoadRequest.setWithMarkedForDeletion(org.jahia.settings.SettingsBean.getInstance().isDisplayMarkedForDeletedContentObjects());
        }
        if (this.entryLoadRequest.getWorkflowState() > 0) {
            this.entryLoadRequest = newLoadRequest;
        }
    }

    // -------------------------------------------------------------------------
    // @author NK

    /**
     * Return true if the current user is an admin member of the current site
     *
     * @return boolean
     */
    public boolean userIsAdmin() {
        return getUser().isAdminMember(getSiteID());
    }

    // -------------------------------------------------------------------------
    // @author Khue NGuyen

    /**
     * Return the current mode in which Jahia is running. There is two main mode in which Jahia is running JahiaInterface.CORE_MODE or
     * JahiaInterface.ADMIN_MODE ( we are in administration mode ) Return -1 if not defined This mode is stored in the session as an
     * attribute with the name : ProcessingContext.SESSION_JAHIA_RUNNING_MODE
     *
     * @return int the Jahia running mode or -1 if not defined.
     * @throws JahiaSessionExpirationException
     *          Throw this exception when the session is null. This happens usually when the session expired.
     * @see org.jahia.bin.JahiaInterface#ADMIN_MODE
     * @see org.jahia.bin.JahiaInterface#CORE_MODE
     */

    public int getJahiaRunningMode() throws JahiaSessionExpirationException {
        return jahiaRunningMode;
    }

    public void setJahiaRunningMode(final int theJahiaRunningMode) {
        this.jahiaRunningMode = theJahiaRunningMode;
    }

    // -------------------------------------------------------------------------
    // @author Khue NGuyen

    /**
     * Return true if the current running mode is JahiaInterface.ADMIN_MODE ( we are in administration mode ).
     *
     * @return boolean <p/> Throw this exception when the session is null. This happens usually when the session expired.
     * @see org.jahia.bin.JahiaInterface#ADMIN_MODE
     * @see org.jahia.bin.JahiaInterface#CORE_MODE
     */
    public boolean isInAdminMode() throws JahiaSessionExpirationException {
        return inAdminMode;
    }

    public void setInAdminMode(boolean inAdminModeFlag) {
        this.inAdminMode = inAdminModeFlag;
    }

    /**
     * If true, the /site/sitekey will be added to the page url
     *
     * @return
     */
    public boolean isForceAppendSiteKey() {
        return forceAppendSiteKey;
    }

    public void setForceAppendSiteKey(boolean forceAppendSiteKey) {
        this.forceAppendSiteKey = forceAppendSiteKey;
    }

    // -------------------------------------------------------------------------

    /**
     * Return the session ID.
     *
     * @return Return the session ID.
     */
    public String getSessionID() throws JahiaSessionExpirationException {
        return getSessionState().getID();
    }

    /**
     * Removes all objects that are stored in the session, but does NOT call a session.invalidate. This should work better for login /
     * logout.
     *
     * @throws JahiaSessionExpirationException
     *          if the session cannot be retrieved from the request object.
     */
    public void purgeSession() throws JahiaSessionExpirationException {

        logger.debug("Purging session of all objects...");

        getSessionState().removeAllAtttributes();

        // keep the last language
        getSessionState().setAttribute(ProcessingContext.SESSION_LOCALE,
                this.getCurrentLocale());
    }

    /**
     * The purpose of this method is to quickly test if the localeList is empty, and in that case to insert a "default" locale so that we
     * never return an empty list.
     *
     * @param locales
     */
    protected void testLocaleList(List<Locale> locales,
                                  final boolean isMixLanguageActive) throws JahiaException {
        if (locales == null) {
            locales = new ArrayList<Locale>();
        }
        if ((locales.size() == 1 && (locales.get(0)).toString().equals(
                ContentField.SHARED_LANGUAGE))
                && this.getSite().getLanguageSettings().isEmpty()) {
            // let's add the default locale as english a last resort locale
            // SettingsBean settings = org.jahia.settings.SettingsBean.getInstance();
            final SettingsBean settings = org.jahia.settings.SettingsBean.getInstance();
            if (settings != null) {
                logger.debug("Using jahia.properties default language code : "
                        + settings.getDefaultLanguageCode());
                locales
                        .add(LanguageCodeConverters
                                .languageCodeToLocale(settings
                                .getDefaultLanguageCode()));
            } else {
                logger
                        .debug("Warning : Couldn't find default language settings in jahia.properties, using english as default locale");
                locales.add(Locale.ENGLISH);
            }
        }
        if (locales.isEmpty()) {
            if (currentLocale == null) {
                currentLocale = Locale.getDefault();
            }
            locales.add(currentLocale);
        }
        if (!locales.get(0).toString().equals(ContentField.SHARED_LANGUAGE)) {
            locales.add(0, EntryLoadRequest.SHARED_LANG_LOCALE);
        }
        if (locales.isEmpty()
                || !(locales.get(0)).toString().equals(
                ContentField.SHARED_LANGUAGE)) {
            locales.add(0, EntryLoadRequest.SHARED_LANG_LOCALE);
        }
        if (!isMixLanguageActive) {
            // we must now check the length of the locale list. It should
            // only have two elements. The shared language and the current
            // language.
            if (locales.size() > 2) {
                final List<Locale> newLocaleList = new ArrayList<Locale>();
                newLocaleList.add(locales.get(0));
                newLocaleList.add(locales.get(1));
                locales.clear();
                locales.addAll(newLocaleList);
            }
        }
        // now let's insert the locale mappings in the list so that we
        // make sure we match all the site defined languages.
        setLocaleList(insertLocaleMappings(locales));
    }

    /**
     * Inserts all the mappings just after the languages if we have them for the languages. Basically if we have the following mappings : fr ->
     * fr_CH en -> en_US and in the locale list we have : <p/> fr en en_US the locale list will be completed to become : fr fr_CH en en_US
     *
     * @param source the source array list without the mappings inserted.
     * @return an array list of Locales including the mappings. This is only possible if the site has already been resolved. Otherwise the
     *         locale list is returned unmodified.
     */
    private List<Locale> insertLocaleMappings(final List<Locale> source) {
        final List<Locale> result = new ArrayList<Locale>();
        if (getSite() == null) {
            logger.debug("Site unknown, can't add mappings...");
            return source;
        }
        List<SiteLanguageMapping> languageMappings;
        try {
            languageMappings = getSite().getLanguageMappings();
        } catch (JahiaException je) {
            logger.debug("Error while retrieving site mappings, returning unmodified locale list.", je);
            return source;
        }

        // we quickly build a Map of the mappings in order to be able
        // to do faster lookups.
        final Map<String, SiteLanguageMapping> languageMappingsMap = new HashMap<String, SiteLanguageMapping>();
        for (SiteLanguageMapping curMapping : languageMappings) {
            languageMappingsMap.put(curMapping.getFromLanguageCode(), curMapping);
        }

        // now let's build the new array list by inserting the mappings
        // immediately after the language, or not at all if the next entry is
        // already the mapped target language or if it was previously in the
        // list in a higher priority level.
        final Set<String> previousLocales = new HashSet<String>();
        final ListIterator<Locale> sourceIter = source.listIterator();
        while (sourceIter.hasNext()) {
            final Locale curSourceLocale = (Locale) sourceIter.next();
            // let's copy the locale to the result.
            result.add(curSourceLocale);
            previousLocales.add(curSourceLocale.toString());
            if (curSourceLocale.getCountry().length() == 0) {
                // no country, let's see if we have a mapping for this locale.
                if (languageMappingsMap.containsKey(curSourceLocale.getLanguage())) {
                    final SiteLanguageMapping curMapping = (SiteLanguageMapping) languageMappingsMap
                            .get(curSourceLocale.getLanguage());
                    final Locale targetLocale = LanguageCodeConverters.languageCodeToLocale(curMapping.getToLanguageCode());
                    // yes, let's see if we can insert it by testing the next
                    // element if it exists to see if it matches the target
                    // locale of this mapping.
                    if (sourceIter.hasNext()) {
                        final Locale nextSourceLocale = (Locale) sourceIter
                                .next();
                        if ((!nextSourceLocale.equals(targetLocale))
                                && (!previousLocales.contains(targetLocale
                                .toString()))) {
                            result.add(targetLocale);
                            previousLocales.add(targetLocale.toString());
                        } else {
                            logger
                                    .debug("Not inserting locale "
                                            + targetLocale.toString()
                                            + " since it already exists in locale list...");
                        }
                        // let's restore the position to the current element.
                        sourceIter.previous();
                    } else {
                        // we are at the end of the list, and we never had it
                        // before, let's add it.
                        if (!previousLocales.contains(targetLocale.toString())) {
                            result.add(targetLocale);
                            previousLocales.add(targetLocale.toString());
                        } else {
                            logger
                                    .debug("Not inserting locale "
                                            + targetLocale.toString()
                                            + " since it already exists in locale list...");
                        }
                    }
                }
            }
        }

        return result;
    }

    /**
     * Change the current Locale Reinit the locales list and the entry load request too !
     *
     * @param locale
     */
    public void changeLanguage(final Locale locale) throws JahiaException {
        if (locale == null) {
            return;
        }

        // reset the locales
        setLocaleList(null);
        getSessionState().setAttribute(SESSION_LOCALE, locale);
        setEntryLoadRequest(new EntryLoadRequest(getEntryLoadRequest().getWorkflowState(), getEntryLoadRequest().getVersionID(),
                getLocales(), getEntryLoadRequest().isWithMarkedForDeletion()));
        resetSubstituteEntryLoadRequest();
        setParameter(ProcessingContext.LANGUAGE_CODE, this.getCurrentLocale().toString());
        resolveJahiaPage();
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
    public List<Locale> getLocales() throws JahiaException {
        JahiaSite site = getSite();
        if (site != null) {
            return getLocales(site.isMixLanguagesActive());
        }
        return getLocales(false);
    }

    /**
     * Jahia's redefinition of the Servlet APIs getLocales code. This method actually builds a list that is a concatenation of multiple
     * sources : <p/> 1. the session locale representing the locales the user has chosen to surf with 2. the list of locales extracted from
     * the users' preferences 3. the list of locales extracted from the browser settings 4. the list of locales that are setup in the site
     * settings <p/> The construction of this list should be configurable, notably to be able from the site settings to say that we want to
     * avoid mixing languages, or that the user never wants to see languages that are not in his settings. <p/> Warning, this method
     * supposes that the current data has been already initialized and is available : - JahiaSite - JahiaUser - Session
     *
     * @return a List of Locale objects that contain the list of locale that are active for the current session, user and site.
     */
    public List<Locale> getLocales(final boolean allowMixLanguages)
            throws JahiaException {

        return getLocales(allowMixLanguages, null);
    }

    protected List<Locale> getLocales(final boolean allowMixLanguages,
                                   Iterator<Locale> browserLocales) throws JahiaException {

        // first we test if we have already this list once, as this method
        // is going to be called a lot.
        if (getLocaleList() != null) {
            return getLocaleList();
        }

        List<Locale> newLocaleList = new ArrayList<Locale>();

        List<Locale> siteLanguages;
        try {
            siteLanguages = this.getSite().getLanguageSettingsAsLocales(true);
        } catch (Exception t) {
            logger.debug("Exception while getting language settings as locales",
                    t);
            siteLanguages = new ArrayList<Locale>();
        }

        // STEP 1 : let's retrieve the current session locale
        if (this.getSessionState() != null) {
            setCurrentLocale((Locale) this.getSessionState().getAttribute(
                    SESSION_LOCALE));
            if (getCurrentLocale() != null
                    && siteLanguages.contains(getCurrentLocale())) {
                newLocaleList.add(getCurrentLocale());
            }
        }

        // STEP 3 : retrieve the browser locales
        if (browserLocales != null) {
            while (browserLocales.hasNext()) {
                final Locale curLocale = browserLocales.next();
                if (siteLanguages.contains(curLocale)) {
                    if (!newLocaleList.contains(curLocale)) {
                        newLocaleList.add(curLocale);
                    }
                } else if (curLocale.getCountry().length() != 0) {
                    final Locale langOnlyLocale = new Locale(curLocale
                            .getLanguage());
                    if (siteLanguages.contains(langOnlyLocale)) {
                        if (!newLocaleList.contains(langOnlyLocale)) {
                            newLocaleList.add(langOnlyLocale);
                        }
                    }
                }
            }
        }

        // STEP 4 : retrieve the site settings locales
        try {
            if (getSite() != null) {
            final List<SiteLanguageSettings> siteLanguageSettings = getSite()
                    .getLanguageSettings();
            if (siteLanguageSettings != null) {
                boolean firstSiteActiveLanguage = true;
                for (SiteLanguageSettings curSetting : siteLanguageSettings) {
                    if (curSetting.isActivated()) {
                        final Locale tempLocale = LanguageCodeConverters
                                .languageCodeToLocale(curSetting.getCode());
                        if (!newLocaleList.contains(tempLocale)) {
                            newLocaleList.add(tempLocale);
                        }
                        if (firstSiteActiveLanguage) {
                            ProcessingContext.getDefaultParameterValues()
                                    .setProperty(
                                            ProcessingContext.LANGUAGE_CODE,
                                            curSetting.getCode());
                            firstSiteActiveLanguage = false;
                        }
                    } else if (logger.isDebugEnabled()) {
                        logger.debug("Not adding language "
                                + curSetting.getCode()
                                + " because it is currently deactivated.");
                    }
                }
            }
            }
        } catch (JahiaException je) {
            logger.debug("Cannot retrieve site language settings", je);
        }

        testLocaleList(newLocaleList, allowMixLanguages);
        return getLocaleList();
    }

    // --------------------------------------------------------------------------
    // @author NK

    /**
     * Actually use the client preferred locale if found, else returns the Locale.getDefault() value. <p/> This function provides a full
     * accept-language implementation, but caches the result. Therefore it assumes that the language is NOT changed during a request other
     * by methods provided by the ProcessingContext class. <p/> Warning, this method supposes that the current data has been already
     * initialized and is available : - JahiaSite - JahiaUser - Session
     *
     * @return Locale the current locale in the current request
     */
    public Locale getLocale() {

        // first we check if we had previously determined the value of the
        // locale in order to avoid doing unnecessary processing.
        if (this.getCurrentLocale() != null) {
            // we have a Locale already determined, let's return it...
            return getCurrentLocale();
        }

        // no currently defined, locale, let's start by retrieving the default
        // locale for the system.
        try {
            final List<Locale> locales = getLocales();
            if (locales.size() >= 2) {
                setCurrentLocale(locales.get(1));
            }
        } catch (Exception t) {
            logger.error("Error while retrieving the default system locale", t);
        }
        if (getCurrentLocale() == null) {
            setCurrentLocale(Locale.getDefault());
        }
        return getCurrentLocale();
    }

    // --------------------------------------------------------------------------

    /**
     * Change the page Used ind Logout_Engine.java
     *
     * @param page
     */
    public void changePage(final ContentPage page) throws JahiaException {
        if (page == null)
            return;
        if (getContentPage() == null) {
            setLastRequestedPageID(-1);
            setNewPageRequest(true);
        } else if (getContentPage().getID() != page.getID()) {
            setLastRequestedPageID(getContentPage().getID());
            setNewPageRequest(true);
        }
        setContentPage(page);
        if (getContentPage().getJahiaID() != getSiteID()) {
            setContentPage(getSite().getHomeContentPage());
        }
        resolveJahiaPage();
    }

    /**
     * Change the page
     *
     * @param pid
     */
    public void changePage(final int pid) throws JahiaException {
        if (pid < 1)
            return;
        changePage(ContentPage.getPage(pid));
    }

    public void flushLocaleListCache() {
        setLocaleList(null);
    }

    /**
     * Set instance var contentPage to loaded content page from page id specified in URL.
     *
     * @throws JahiaException
     */
    protected void setContentPageToPageWithID() throws JahiaException {
        int pageID = NumberUtils.toInt(getParameter(PAGE_ID_PARAMETER), 0);
        setContentPage(REGISTRY.getJahiaPageService().lookupContentPage(pageID,
                true));
    }

    /**
     * Retrieve page properties that have value equals to key specified in URL and the name PAGE_URL_KEY_PROPNAME. Multiple result can
     * happen since an URL key is unique in only one site. Load page props that we want and get page associated with this property via the
     * page cache. N.b: site must be resolved before calling this, since we need the siteID.
     *
     * @throws JahiaException
     */
    protected void setContentPageToPageWithURLKey() throws JahiaException {
        final String pageURLKey = (String) pageURLKeys
                .get(pageURLKeys.size() - 1);
        int pageID = 0;
        int currentParentPageID = 0;

        for (String curURLKey : pageURLKeys) {
            List<PageProperty> pageProperties = REGISTRY.getJahiaPageService()
                    .getPagePropertiesByNameValueSiteIDAndParentID(PageProperty.PAGE_URL_KEY_PROPNAME, curURLKey, siteID, currentParentPageID);
            if (pageProperties.size() != 1) {
                logger.debug("Invalid URL key in path '" + curURLKey + "', defaulting to unique URL key resolution");
                currentParentPageID = 0;
                break;
            }
            final PageProperty pageProperty = (PageProperty) pageProperties.get(0);
            currentParentPageID = pageProperty.getPageID();
        }
        if (currentParentPageID != 0) {
            setContentPage(ContentPage.getPage(currentParentPageID));
            return;
        }

        logger.debug("Couldn't resolve page with new mechanism, reverting to default unique page URL key");

        List<PageProperty> pageProperties = REGISTRY.getJahiaPageService()
                .getPagePropertiesByValueAndSiteID(pageURLKey, siteID);
        for (Iterator<PageProperty> it = pageProperties.iterator(); it.hasNext()
                && pageID == 0;) {
            final PageProperty pageProperty = (PageProperty) it.next();
            if (PageProperty.PAGE_URL_KEY_PROPNAME.equals(pageProperty
                    .getName())) {
                pageID = pageProperty.getPageID();
            }
        }
        if (pageID > 0) {
            setContentPage(ContentPage.getPage(pageID));
        }
//        else {
//            setContentPage(getSite().getHomeContentPage());
//        }
    }

    protected void setSiteInfoFromSiteFound() {
        setSiteID(getSite().getID());
        setSiteKey(getSite().getSiteKey());
        final JahiaSite oldSite = (JahiaSite) getSessionState().getAttribute(
                SESSION_SITE);
        if (oldSite == null) {
            setSiteHasChanged(true);
        } else if (oldSite.getID() != getSite().getID()) {
            setSiteHasChanged(true);
            // setUserGuest(this.getSiteID());
        }

        // if (! (settings().isSiteIDInURL())) {
        // final JahiaSite defaultSite = getDefaultSite();
        // if ((defaultSite != null) && (defaultSite.getID() == site.getID())) {
        // // site in URL is the default site, let's remove it from the
        // // URL
        // ParamBean.defaultParameterValues.setProperty(ParamBean.SITE_KEY_PARAMETER, site.getSiteKey());
        // }
        // }

        getSessionState().setAttribute(SESSION_SITE, site);
    }

    // -------------------------------------------------------------------------
    // * @author DJ 30.01.2001 - Original implementation

    /**
     * get the parameter from the request object specified with specifyrequestObj
     */
    public String getParameter(final String sParameter) {

        if (getCustomParameters().get(sParameter) != null) {
            final String[] paramValues = (String[]) getCustomParameters().get(
                    sParameter);
            return paramValues[0];
        } else {
            return null;
        }
    }

    // @author Serge Huber 20.1.02

    /**
     * Insert a new parameter into Jahia's paramBean internal parameters. This is used for internal in-request communication.
     *
     * @param parameterName  name of the parameter. If this is an existing parameter name-value pair, the value specified will replace the old one
     *                       so be careful.
     * @param parameterValue a String value for the parameter name specified.
     */
    public void setParameter(final String parameterName,
                             final String parameterValue) {
        final String[] paramValues = new String[1];
        paramValues[0] = parameterValue;
        getCustomParameters().put(parameterName, paramValues);
    }

    // -------------------------------------------------------------------------
    // MJ 20.03.2001
    // NK 18.05.2001 Catch malformed pathinfo exception. Stop parsing it.

    /**
     * parse the PathInfo elements and convert them to emultated request parameters. the parameters are stored in a HashMap
     * (customParameters) where they can be looked up by a customized version of this.getParameter(). <p/> todo we might want to extend this
     * in order to store parameter info either in the session or in the URL. Session for shorter URLs and URL for bookmarking. This should
     * be configurable in the properties, or maybe even for the page.
     *
     * @param reqPathInfo a String containing the pathInfo
     */
    protected void buildCustomParameterMapFromPathInfo(String reqPathInfo,
                                                       final String extraParams, final String servletPath) {
        // Parse the PathInfo and build a custom parameter map

        if (reqPathInfo != null) {

            if (extraParams != null)
                reqPathInfo += extraParams;

            if (reqPathInfo.endsWith(".html")) {
                // let's remove false static ending.
                int lastSlash = reqPathInfo.lastIndexOf("/");
                if (lastSlash != -1) {
                    final String fakeStaticName = reqPathInfo
                            .substring(lastSlash + 1);
                    reqPathInfo = reqPathInfo.substring(0, lastSlash);
                    if (logger.isDebugEnabled()) {
                        logger.debug("Removed fake static ending. pathInfo=["
                                + reqPathInfo + "] fakeEnding=[" + fakeStaticName
                                + "]");
                    }
                }
            }

            try {
                final StringTokenizer st = new StringTokenizer(reqPathInfo, "/");
                if ("/ajaxaction".equals(servletPath)) {
                    st.nextToken();
                }
                while (st.hasMoreTokens()) {
                    final String token = st.nextToken();
                    if (isPlutoParameter(token)) {
                        // if we encounter a Pluto parameter, we stop the processing here.
                        return;
                    } else if (isReservedKeyword(token)) {
                        String[] paramValues = new String[1];
                        paramValues[0] = st.nextToken();
                        getCustomParameters().put(token, paramValues);
                    } else if (!ExportEngine.ENGINE_NAME.equals(getParameter("engineName")) || st.hasMoreTokens()) {
                        getPageURLKeys().add(token);
                    }
                }
            } catch (NoSuchElementException nee) {
                // stop parsing token
            }
        }

    }

    public static boolean isContainerScroll(String str) {
        return (str.startsWith(CONTAINER_SCROLL_PREFIX_PARAMETER));
    }

    public static boolean isPlutoParameter(String str) {
        return (str.startsWith("__"));
    }

    /**
     * Returns true if str passed is a reserved keyword. Added by Intellogix
     *
     * @param str
     * @return
     */
    public static boolean isReservedKeyword(String str) {
        if (ProcessingContext.ENGINE_NAME_PARAMETER.equals(str)
                || ProcessingContext.SITE_KEY_PARAMETER.equals(str)
                || ProcessingContext.PAGE_ID_PARAMETER.equals(str)
                || ProcessingContext.CONTAINERLIST_ID_PARAMETER.equals(str)
                || ProcessingContext.CONTAINER_ID_PARAMETER.equals(str)
                || ProcessingContext.FIELD_ID_PARAMETER.equals(str)
                || ProcessingContext.APPUNIQUE_ID_PARAMETER.equals(str)
                || ProcessingContext.OPERATION_MODE_PARAMETER.equals(str)
                || ProcessingContext.ENTRY_STATE_PARAMETER.equals(str)
                || ProcessingContext.SHOW_REVISION_DIFF_PARAMETER.equals(str)
                || ProcessingContext.VALIDATE_PARAMETER.equals(str)
                || ProcessingContext.LANGUAGE_CODE.equals(str)
                || ProcessingContext.RELEASE_LOCK.equals(str)
                || ProcessingContext.STEAL_LOCK.equals(str)
                || ProcessingContext.TEMPLATE_PARAMETER.equals(str)
                || ProcessingContext.CACHE_MODE_PARAMETER.equals(str)
                || ProcessingContext.CONTAINERCACHE_MODE_PARAMETER.equals(str))
            return true;
        else if (isContainerScroll(str) == true)
            return true;
        return false;
    }

    // --------------------------------------------------------------------------

    /**
     * Generates engine url's parameters in queryString or Path info. By default, use path info.
     *
     * @param val
     */
    public void setUseQueryStringParameterUrl(final boolean val) {
        this.useQueryStringParameterUrl = val;
    }

    /**
     * Return true if the engine URL's parameters are generated in the querystring. &engineName=code&pid=10 False if using path info format:
     * /engineName/core/pid/10
     */
    public boolean useQueryStringParameterUrl() {
        return this.isUseQueryStringParameterUrl();
    }

    // -------------------------------------------------------------------------
    // @author Khue Nguyen

    /**
     * Return the default site or null if not found or undefined
     *
     * @return JahiaSite the default site
     */
    public static JahiaSite getDefaultSite() {
        return REGISTRY.getJahiaSitesService().getDefaultSite();
    }

    /**
     * Try to find site via ... - site key - host name - page ID - default site - from session
     *
     * @return true if site was found.
     */
    protected boolean findSiteFromWhatWeHave() throws JahiaException {
        if (findSiteByItsKey()) {
            if (getSite().equals(getSiteByHostName())) {
                setSiteResolvedByServername(true);
            }
            return true;
        } if (findSiteByHostName()) {
            return true;
        } else if (isPageRequestedByID() && findSiteByPageID()) {
            return true;
        } else if (findSiteByRequestParam()) {
            return true;
        } else if (findSiteFromSession()) {
            return true;
        } else if (findDefaultSite()) {
            return true;
        }

        return false;
    }

    private boolean findSiteFromSession() {
        if (!CORE_ENGINE_NAME.equals(getEngineName())) {
            site = (JahiaSite) getSessionState().getAttribute(SESSION_SITE);
            return !(site == null || site.getID() <= 0);
        }
        return false;
    }

    /**
     * @return true if default site was found successfully.
     */
    private boolean findDefaultSite() {
        logger.debug("No site found in URL, serverName or via page ID, trying default site...");
        site = getDefaultSite();
        return site != null;
    }

    /**
     * Tells if page id was passed in URL. ( Page requested by ID )
     *
     * @return true if PAGE_ID_PARAMETER is in URL and its a number bigger then 0.
     */
    protected boolean isPageRequestedByID() {
        final String pageIDStr = getParameter(PAGE_ID_PARAMETER);
        return pageIDStr != null && NumberUtils.toInt(pageIDStr, 0) > 0;
    }

    /**
     * @return true if page was requested by key.
     */
    protected boolean isPageRequestedByKey() {
        return !isPageRequestedByID() && !getPageURLKeys().isEmpty();
    }

    /**
     * Find site via the page id. this.site will be set if its found.
     *
     * @return true if site was found using page id
     * @throws JahiaException
     */
    private boolean findSiteByPageID() throws JahiaException {
        final int pageID = NumberUtils
                .toInt(getParameter(PAGE_ID_PARAMETER), 0);
        try {
            setContentPage(REGISTRY.getJahiaPageService().lookupContentPage(
                    pageID, true));
        } catch (JahiaPageNotFoundException ex) {

        }
        setContentPageLoadedWhileTryingToFindSiteByPageID(true);
        
        if (getContentPage() == null)
            return false;

        setSite(REGISTRY.getJahiaSitesService().getSite(
                getContentPage().getJahiaID()));

        if (getSite() == null)
            return false;

        setSiteResolvedByKeyOrPageId(true);

        return true;
    }

    /**
     * Returns site by the host name.
     *
     * @return site by the host name
     * @throws JahiaException in case of an error
     */
    protected JahiaSite getSiteByHostName() throws JahiaException {
        JahiaSite resolvedSite = null;

        if (isValidServerName(getServerName().toLowerCase())) {
            resolvedSite = REGISTRY.getJahiaSitesService().getSite(
                    getServerName());
        }

        return resolvedSite;
    }

    /**
     * Find site by the host name. this.site will be set if its found.
     *
     * @return true if site was found from host name.
     * @throws JahiaException
     */
    private boolean findSiteByHostName() throws JahiaException {
        setSite(getSiteByHostName());
        setSiteResolvedByServername(getSite() != null);
        return (getSite() != null);
    }

    /**
     * @param aServerName
     * @return true if servername supplied is valid.
     */
    private boolean isValidServerName(final String aServerName) {
        if (aServerName == null)
            return false;
        if (aServerName.equals("localhost"))
            return false;
        return !aServerName.equals("127.0.0.1");
    }

    /**
     * Find site by its key. this.site will be set if its found.
     *
     * @return true if site was found from site key specified in url.
     * @throws JahiaException
     */
    private boolean findSiteByItsKey() throws JahiaException {
        if (!isSiteKeyPresent())
            return false;
        setSite(REGISTRY.getJahiaSitesService().getSiteByKey(
                getParameter(SITE_KEY_PARAMETER)));
        if (getSite() == null)
            return false;
        logger.debug("Found site info in parameters...");

        setSiteResolvedByKeyOrPageId(true);

        return true;
    }

    /**
     * Find site by its key which is passed as arequest paramter with name "siteKey" this.site will be set if its found.
     *
     * @return true if site was found from site key specified in url.
     * @throws JahiaException
     */
    private boolean findSiteByRequestParam() throws JahiaException {
        final String paramSiteKey = (String) getCustomParameters().get(
                "siteKey");

        if (paramSiteKey == null || paramSiteKey.length() == 0)
            return false;

        setSite(REGISTRY.getJahiaSitesService().getSiteByKey(paramSiteKey));

        if (getSite() == null)
            return false;

        logger.debug("Found site info in parameters...");

        setSiteResolvedByKeyOrPageId(true);

        return true;
    }

    private boolean isSiteKeyPresent() {
        return getParameter(SITE_KEY_PARAMETER) != null;
    }

    /**
     * Returns an URL only if the parameterValue for the parameterName is different from its defined default value, or if it has no default
     * value.
     *
     * @param parameterName  name of the parameter to add in the URL
     * @param parameterValue value of the parameter to add in the URL
     * @return an empty string if the value is equal to the default value, otherwise it returns a composition as follow : "/" +
     *         parameterName + "/" + parameterValue
     */
    private String condAppendURL(final String parameterName,
                                 final String parameterValue) {
        final String defaultValue = getDefaultParameterValues().getProperty(
                parameterName);
        return parameterValue == null || parameterValue.equals(defaultValue) ? "" : appendParam(
                parameterName, parameterValue);
    }

    private String appendParam(final String parameterName,
                               final String parameterValue) {
        final StringBuffer result = new StringBuffer();
        if (!useQueryStringParameterUrl()) {
            result.append("/");
        }

        result.append(parameterName);
        if (!useQueryStringParameterUrl()) {
            result.append("/");
        } else {
            result.append("=");
        }
        result.append(parameterValue);
        return result.toString();
    }

    // -------------------------------------------------------------------------
    // EV 20 Nov. 2000 : Original implementation
    // FH 22 Jan. 2001 : - Changed += operation on a String to a StringBuffer.
    // - added error check.
    // MJ 29 May. 2001 : get http path from request instead of settings,

    /**
     * Compose an URL by adding default parameters (like the page id, the session id, ...) to the passed in parameter string.
     *
     * @param params String of parameters.
     * @return Return a valid URL by adding default parameters. Return an non-null empty string on any error.
     */
    public String composeUrl(final String params) throws JahiaException {
        return composeUrl(
                getContentPage() != null ? getPageURLKeyPart(getContentPage().getID()) : "",
                getContentPage() != null ? getPageURLPart(getContentPage().getID()) : "", 0, null, null, params);
    }

    /**
     * Compose an URL by adding default parameters (like the page id, the session id, ...) to the passed in parameter string.
     *
     * @param pageUrlKey the url key of the page
     * @param params     String of parameters.
     * @return Return a valid URL by adding default parameters. Return an non-null empty string on any error.
     */
    public String composeUrl(final String pageUrlKey, final String params)
            throws JahiaException {
        return composeUrl(pageUrlKey, "", 0, null, null, params);
    } // end composeUrl

    // -------------------------------------------------------------------------
    // EV 20 Nov. 2000 : Original implementation
    // FH 22 Jan. 2001 : Changed += operation on a String to a StringBuffer.
    // MJ 29 May. 2001 : get http path from request instead of settings,
    // MJ 24 Jul. 2001 : dirty hack to hide catalina bug in HttpServletResponse
    // .encodeURL(String URL)
    // (bug description : reference to response object is
    // sometimes lost after intensive calls to this method)
    // Affected version : Tomcat 4.0 beta 1

    /**
     * @param pageID
     * @return
     * @throws JahiaException
     */
    public String composePageUrl(final int pageID) throws JahiaException {
        return composeUrl(getPageURLKeyPart(pageID), getPageURLPart(pageID),
                pageID, null, getLocale().toString(), null);
    }

    /**
     * Compose page url parameter form.
     *
     * @param pageID      page id.
     * @param pathParams  path parameters
     * @param queryParams query parameters
     * @return String containing the generated URL.
     * @throws JahiaException
     */
    public String composePageUrl(final int pageID, final Map<String, Object> pathParams,
                                 Map<String, Object> queryParams) throws JahiaException {
        // compose pathParams as string
        return composeUrl(pageID, pathParams, queryParams, getLocale()
                .toString());
    }

    public String composePageUrl(final int pageID, final String languageCode)
            throws JahiaException {
        return composeUrl(getPageURLKeyPart(pageID), getPageURLPart(pageID),
                pageID, null, languageCode, null);
    }

    public String composePageUrl(final String pageUrlKey,
                                 final String languageCode) throws JahiaException {
        return composeUrl(pageUrlKey, "", 0, null, languageCode, null);
    }

    public String composePageUrl(final JahiaPage page) throws JahiaException {
        return composeUrl(getPageURLKeyPart(page.getContentPage()),
                getPageURLPart(page.getContentPage(), false), page.getID(),
                null, this.getLocale().toString(), null);
    }

    public String composePageUrl(final JahiaPage page, final String languageCode)
            throws JahiaException {
        return composeUrl(getPageURLKeyPart(page.getContentPage()),
                getPageURLPart(page.getContentPage(), false), page.getID(),
                null, languageCode, null);
    }

    /**
     * Compose page url parameter form.
     *
     * @param pageID       page id.
     * @param pathParams   path parameters
     * @param queryParams  query parameters
     * @param languageCode language code
     * @return String containing the generated URL.
     * @throws JahiaException
     */
    public String composeUrl(final int pageID, final Map<String, Object> pathParams,
                             Map<String, Object> queryParams, String languageCode) throws JahiaException {
        // compose pathParams as string
        final StringBuffer pathParamsAsString = new StringBuffer();
        if (pathParams != null && !pathParams.isEmpty()) {
            Iterator<String> it = pathParams.keySet().iterator();
            boolean isFirst = true;
            while (it.hasNext()) {
                String name = it.next().toString();
                String value = pathParams.get(name).toString();
                if (isFirst) {
                    pathParamsAsString.append("/");
                    isFirst = false;
                }
                pathParamsAsString.append(name).append("/").append(value);
                if (it.hasNext()) {
                    pathParamsAsString.append("/");
                }

            }
            if (logger.isDebugEnabled()) {
                logger.debug("Path params is [" + pathParamsAsString + "]");
            }
        } else if (logger.isDebugEnabled()) {
            logger.debug("Path params is empty");
        }

        // compose queryParams as String
        final StringBuffer queryParamsAsString = new StringBuffer();
        if (queryParams != null && !queryParams.isEmpty()) {
            Iterator<String> it = queryParams.keySet().iterator();
            boolean isFirst = true;
            while (it.hasNext()) {
                String name = it.next().toString();
                String value = queryParams.get(name).toString();
                if (isFirst) {
                    queryParamsAsString.append("?");
                    isFirst = false;
                } else {
                    queryParamsAsString.append("&");
                }
                queryParamsAsString.append(name).append("=").append(value);
            }
            if (logger.isDebugEnabled()) {
                logger.debug("Query params is [" + queryParamsAsString + "]");
            }
        } else if (logger.isDebugEnabled()) {
            logger.debug("Query params is empty");
        }

        return composeUrl(getPageURLKeyPart(pageID), getPageURLPart(pageID),
                pageID, pathParamsAsString.toString(), languageCode,
                queryParamsAsString.toString());
    }

    private String composeUrl(final String pageUrlKey, final String pageUrl,
                              final int pageID, final String pathParams,
                              final String languageCode, final String params)
            throws JahiaException {
        final StringBuffer theUrl = new StringBuffer();
        theUrl.append(getJahiaCoreHttpPath());
        theUrl.append(getEngineURLPart(CORE_ENGINE_NAME));
        ContentPage aContentPage = pageID > 0 ? ContentPage.getPage(pageID)
                : null;
        if (aContentPage == null) {
            theUrl.append(getSiteURLPart(getSite().getSiteKey()));
        } else if (aContentPage != null) { /* && this.isForceAppendSiteKey() && aContentPage.getSiteID() != getSite().getID()) {*/
            // this case can happens when a page from another site is listened in the search result
            // when a search is done on several site
            theUrl.append(this.getSiteURLPart(aContentPage.getSite().getSiteKey()));
        }
        String paramOpMode = getParameter(OPERATION_MODE_PARAMETER);
        String opModeToUse = paramOpMode != null ? paramOpMode : getOpMode();
        theUrl.append(getOpModeURLPart(opModeToUse));
        if (languageCode != null) {
                theUrl.append(condAppendURL(LANGUAGE_CODE, languageCode));
        }
        if (COMPARE.equals(opModeToUse)) {
            theUrl.append(getEntryStateURLPart(getParameter(ENTRY_STATE_PARAMETER)));
            theUrl.append(getShowRevisionDiffURLPart(getDiffVersionID()));
        }
        if (pathParams != null) {
            theUrl.append(pathParams);
        }
        theUrl.append(pageUrlKey);
        theUrl.append(pageUrl);

        appendParams(theUrl, params);
        appendAnchor(theUrl);

        try {
            return encodeURL(theUrl.toString());
        } catch (NullPointerException npe) {
            return theUrl.toString();
        }
    }
    
    // -------------------------------------------------------------------------
    // MJ 27 Feb. 2001 : Overloaded method without params besides engineName
    // MJ 29 May. 2001 : get http path from request instead of settings,

    /**
     * composeEngineUrl MJ 27.02.2001
     */
    public String composeEngineUrl(final String theEngineName)
            throws JahiaException {
        return composeEngineUrl(theEngineName, null, null, 0);
    }

    // -------------------------------------------------------------------------
    // EV 20 Nov. 2000 : Original implementation
    // FH 22 Jan. 2001 : Changed += operation on a String to a StringBuffer.
    // MJ 29 May. 2001 : get http path from request instead of settings,

    /**
     * composeEngineUrl EV 20.11.2000
     */
    public String composeEngineUrl(final String theEngineName,
                                   final String params) throws JahiaException {
        return composeEngineUrl(theEngineName, params, null, 0);
    }

    /**
     * Supplementary version that allows us to add parameters in standard Jahia parameter form.
     *
     * @param theEngineName    the name of the engine for which to generate the URL
     * @param extraJahiaParams additional /name/value parameter to insert in the url
     * @param params           standard URL parameters in the form of a string that starts with ?
     * @return String containing the generated URL.
     * @throws JahiaException
     */
    public String composeEngineUrl(final String theEngineName,
                                   final Properties extraJahiaParams, final String params)
            throws JahiaException {
        return composeEngineUrl(theEngineName, params, extraJahiaParams, 0);
    }

    /**
     * composeEngineUrl NK compose an engine url with the field id information
     */
    public String composeEngineUrl(final String theEngineName,
                                   final String params, final int aFieldID) throws JahiaException {
        return composeEngineUrl(theEngineName, params, null, aFieldID);
    }

    /**
     * composeEngineUrl
     */
    private String composeEngineUrl(final String theEngineName,
                                    final String params, final Properties extraJahiaParams,
                                    final int aFieldID) throws JahiaException {
        final StringBuffer theUrl = new StringBuffer();
        theUrl.append(getJahiaCoreHttpPath());

        if (theEngineName != null) {
            theUrl.append(getEngineURLPart(theEngineName));
        }

        theUrl.append(getSiteURLPart());
        if (params == null || params.indexOf("engine_params=logout") == -1) {
            theUrl.append(getOpModeURLPart(getOpMode()));
        }
        if (getContentPage() != null) {
            theUrl.append(getPageURLKeyPart(getContentPage().getID()));
            if (extraJahiaParams == null
                    || !extraJahiaParams.containsKey(PAGE_ID_PARAMETER)) {
                theUrl.append(getPageURLPart(getContentPage().getID()));
            }
        }

        if (aFieldID > 0) {
            theUrl.append(getFieldURLPart(aFieldID));
        }

        if (extraJahiaParams != null) {
            final Enumeration<?> propertyNames = extraJahiaParams.propertyNames();
            while (propertyNames.hasMoreElements()) {
                final String propertyName = (String)propertyNames.nextElement();
                final String propertyValue = extraJahiaParams
                        .getProperty(propertyName);
                theUrl.append(condAppendURL(propertyName, propertyValue));
            }
        }

        appendParams(theUrl, params);

        appendAnchor(theUrl);

        return encodeURL(theUrl.toString());
    } // end composeEngineUrl

    // @author Khue Nguyen

    /**
     * Supplementary version that allows us to add parameters in standard Jahia parameter form.
     *
     * @param strutsAction the Struts Action part of the URL to generate. the queryString
     * @param params       standard URL parameters in the form of a string that starts with &
     * @return String containing the generated URL.
     * @throws JahiaException
     */
    public String composeStrutsUrl(final String strutsAction,
                                   final String params) throws JahiaException {
        return composeStrutsUrl(strutsAction, new Properties(), params);
    }

    /**
     * Supplementary version that allows us to add parameters in standard Jahia parameter form.
     *
     * @param strutsAction     the Struts Action part of the URL to generate.
     * @param extraJahiaParams additional name=value parameter to insert in the queryString
     * @param params           standard URL parameters in the form of a string that starts with &
     * @return String containing the generated URL.
     * @throws JahiaException
     */
    public String composeStrutsUrl(final String strutsAction,
                                   Properties extraJahiaParams, final String params)
            throws JahiaException {
        if (extraJahiaParams == null) {
            extraJahiaParams = new Properties();
        }

        setUseQueryStringParameterUrl(true);

        final StringBuffer theUrl = new StringBuffer();
        if (!isInAdminMode()) {
            theUrl.append(getJahiaCoreHttpPath());
        } else {
            theUrl.append(getContextPath());
            theUrl.append("/do");
        }
        if (!strutsAction.startsWith("/")) {
            theUrl.append("/");
        }
        theUrl.append(strutsAction);

        String paramSep = "?";
        String urlParams = getSiteAndModeAndPageAsURLParams(paramSep);
        theUrl.append(urlParams);
        if (urlParams.length() > 0)
            paramSep = "&";

        String paramValue;
        final Enumeration<?> propertyNames = extraJahiaParams.propertyNames();
        while (propertyNames.hasMoreElements()) {
            final String propertyName = (String)propertyNames.nextElement();
            final String propertyValue = extraJahiaParams.getProperty(propertyName);
            paramValue = condAppendURL(propertyName, propertyValue);
            if (paramValue != null && paramValue.trim().length() > 0) {
                theUrl.append(paramSep);
                theUrl.append(paramValue);
                paramSep = "&";
            }
        }

        appendParams(theUrl, params);

        appendAnchor(theUrl);

        setUseQueryStringParameterUrl(false);

        return encodeURL(theUrl.toString());
    }

    public String getSiteAndModeAndPageAsURLParams(String paramSepFirst) {
        boolean old = useQueryStringParameterUrl;
        useQueryStringParameterUrl = true;
        StringBuffer theUrl = new StringBuffer();
        String paramValue = getSiteURLPart();
        String paramSep = paramSepFirst;
        if (paramValue != null && paramValue.trim().length() > 0) {
            theUrl.append(paramSep);
            theUrl.append(paramValue);
            paramSep = "&";
        }
        paramValue = getOpModeURLPart(opMode);
        if (paramValue != null && paramValue.trim().length() > 0) {
            theUrl.append(paramSep);
            theUrl.append(paramValue);
            paramSep = "&";
        }
        if (contentPage != null) {
            paramValue = getPageURLPart(contentPage.getID(), true);
            if (paramValue != null && paramValue.trim().length() > 0) {
                theUrl.append(paramSep);
                theUrl.append(paramValue);
            }
        }
        useQueryStringParameterUrl = old;
        return theUrl.toString();
    }

    // @author Khue Nguyen

    /**
     * Supplementary version that allows us to add parameters in standard Jahia parameter form.
     *
     * @param facesPage the face page part of the URL to generate. the queryString
     * @param params    standard URL parameters in the form of a string that starts with &
     * @return String containing the generated URL.
     * @throws JahiaException
     */
    public String composeFacesUrl(final String facesPage, final String params)
            throws JahiaException {
        return composeFacesUrl(facesPage, new Properties(), params);
    }

    /**
     * Supplementary version that allows us to add parameters in standard Jahia parameter form.
     *
     * @param facesPage        the faces page part of the URL to generate.
     * @param extraJahiaParams additional name=value parameter to insert in the queryString
     * @param params           standard URL parameters in the form of a string that starts with &
     * @return String containing the generated URL.
     * @throws JahiaException
     */
    public String composeFacesUrl(final String facesPage,
                                  Properties extraJahiaParams, final String params)
            throws JahiaException {
        if (extraJahiaParams == null) {
            extraJahiaParams = new Properties();
        }

        setUseQueryStringParameterUrl(true);

        final StringBuffer theUrl = new StringBuffer();
        theUrl.append(Jahia.getContextPath());
        if (!facesPage.startsWith("/")) {
            theUrl.append("/");
        }
        theUrl.append(facesPage);
        String paramSep = "?";
        if (getContentPage() != null) {
            final String paramValue = getPageURLKeyPart(getContentPage()
                    .getID());
            if (paramValue != null && paramValue.trim().length() > 0) {
                theUrl.append(paramSep);
                theUrl.append(paramValue);
                paramSep = "&";
            }
        }

        String urlParams = getSiteAndModeAndPageAsURLParams(paramSep);
        theUrl.append(urlParams);
        if (urlParams.length() > 0)
            paramSep = "&";

        String paramValue;
        final Enumeration<?> propertyNames = extraJahiaParams.propertyNames();
        while (propertyNames.hasMoreElements()) {
            final String propertyName = (String)propertyNames.nextElement();
            final String propertyValue = extraJahiaParams
                    .getProperty(propertyName);
            paramValue = condAppendURL(propertyName, propertyValue);
            if (paramValue != null && paramValue.trim().length() > 0) {
                theUrl.append(paramSep);
                theUrl.append(paramValue);
                paramSep = "&";
            }
        }

        appendParams(theUrl, params);

        appendAnchor(theUrl);

        setUseQueryStringParameterUrl(false);

        return encodeURL(theUrl.toString());
    }

    // -------------------------------------------------------------------------
    // EV 20 Nov. 2000 : Original implementation
    // FH 22 Jan. 2001 : Changed += operation on a String to a StringBuffer.
    // MJ 29 May. 2001 : get http path from request instead of settings,

    /**
     * composeOperationUrl EV 21.11.2000
     */
    public String composeOperationUrl(final String operationMode,
                                      final String params) throws JahiaException {
        final StringBuffer theUrl = new StringBuffer();
        theUrl.append(getJahiaCoreHttpPath());

        theUrl.append(getEngineURLPart(CORE_ENGINE_NAME));
        // @Fixme: as operation links are always attached to a page, we don't nee to add the siteKey
        theUrl.append(getSiteURLPart());
        if (operationMode != null) {
            theUrl.append(getOpModeURLPart(operationMode));
        }

        String languageCode = getLocale().toString();

        if (languageCode != null) {
            theUrl.append(condAppendURL(LANGUAGE_CODE, languageCode));
        }

        if (getContentPage() != null) {
            theUrl.append(getPageURLKeyPart(getContentPage().getID()));
            theUrl.append(getPageURLPart(getContentPage().getID()));
        }
        appendParams(theUrl, params);

        return encodeURL(theUrl.toString());
    } // end composeOperationUrl

    /**
     * composeOperationUrl EV 21.11.2000
     *
     * @param revisionDiffID 0 to compare with staging, 1 with active
     * @param operationMode
     * @param params
     * @return
     * @throws JahiaException
     */
    public String composeRevDifferenceUrl(final int revisionDiffID,
                                          final String operationMode, final String params)
            throws JahiaException {
        return composeRevDifferenceUrl(revisionDiffID == 0 ? "a" : "s",
                revisionDiffID, operationMode, params);
    }

    /**
     * Generate a page url in compare mode between entryStateVersion version and revisionDiffVersion version.
     *
     * @param entryStateVersion
     * @param revisionDiffVersion
     * @param operationMode
     * @param params
     * @return
     * @throws JahiaException
     */
    public String composeRevDifferenceUrl(int entryStateVersion,
                                          final int revisionDiffVersion, final String operationMode,
                                          final String params) throws JahiaException {
        String entryStateString = String.valueOf(entryStateVersion);
        if (entryStateVersion == 1){
            entryStateString = "a";
        } else if (entryStateVersion == 2){
            entryStateString = "s";
        }
        return composeRevDifferenceUrl(entryStateString,revisionDiffVersion, operationMode, params);
    }

    protected String composeRevDifferenceUrl(String entryStateVersionOrState,
                                             final int revisionDiffVersion, final String operationMode,
                                             final String params) throws JahiaException {
        final StringBuffer theUrl = new StringBuffer();
        theUrl.append(this.getJahiaCoreHttpPath());

        theUrl.append(getEngineURLPart(CORE_ENGINE_NAME));
        theUrl.append(getSiteURLPart());
        if (operationMode != null) {
            theUrl.append(getOpModeURLPart(ParamBean.COMPARE));
        }
//        theUrl.append(getCacheModeURLPart(ParamBean.CACHE_OFFONCE));

        if (getContentPage() != null) {
            theUrl.append(this.getEntryStateURLPart(entryStateVersionOrState));
            theUrl.append(this.getShowRevisionDiffURLPart(revisionDiffVersion));
            theUrl.append(getPageURLKeyPart(getContentPage().getID()));
            theUrl.append(getPageURLPart(getContentPage().getID()));
        }
        appendParams(theUrl, params);
        return encodeURL(theUrl.toString());
    }


    public String composeLanguageURL(final String code) throws JahiaException {
        return composeLanguageURL(code, getPageID());
    }

    public String composeLanguageURL(final String code, boolean preserveEngine) throws JahiaException {
        return composeLanguageURL(code, getPageID(), preserveEngine);
    }

    /**
     * @return The URL page containing "lang/'code'"
     * @throws JahiaException
     */
    public String composeLanguageURL(final String code, final int pid) throws JahiaException {
        return composeLanguageURL(code, pid, false);
    }

    public String composeLanguageURL(final String code, final int pid, boolean preserveEngine) throws JahiaException {
        final StringBuffer theUrl = new StringBuffer();
        theUrl.append(getJahiaCoreHttpPath());
        theUrl.append(getEngineURLPart(preserveEngine ? getEngineName() : CORE_ENGINE_NAME));
        theUrl.append(getSiteURLPart());
        String paramOpMode = getParameter(OPERATION_MODE_PARAMETER);
        String opModeToUse = paramOpMode != null ? paramOpMode : getOpMode();
        theUrl.append(getOpModeURLPart(opModeToUse));
        theUrl.append(appendParam(LANGUAGE_CODE, code));

        final ContentPage contentPage1 = (pid>0?ContentPage.getPage(pid):getContentPage());
        if (contentPage1 != null) {
            if (COMPARE.equals(opModeToUse)) {
                theUrl.append(getEntryStateURLPart(getParameter(ENTRY_STATE_PARAMETER)));
                theUrl.append(getShowRevisionDiffURLPart(getDiffVersionID()));
            }

            theUrl.append(getPageURLKeyPart(contentPage1.getID()));
            theUrl.append(getPageURLPart(contentPage1.getID()));
        }
        String queryString = this.getQueryString();
        if (queryString != null && queryString.length() > 0) {
            theUrl.append("?").append(queryString);
        }
        return encodeURL(theUrl.toString());
    }

    // #ifdef LOCK
    public String composeStealLockURL(final LockKey lockKey)
            throws JahiaException {
        return composeLockURL(lockKey, ProcessingContext.STEAL_LOCK);
    }

    public String composeReleaseLockURL(final LockKey lockKey)
            throws JahiaException {
        return composeLockURL(lockKey, ProcessingContext.RELEASE_LOCK);
    }

    private String composeLockURL(final LockKey lockKey, String lockAction)
            throws JahiaException {
        final StringBuffer theURL = new StringBuffer();
        theURL.append(getRequestURI());
        theURL.append("/" + lockAction + "/");
        theURL.append(lockKey.toString());
        theURL.append("?");
        theURL.append(getQueryString());
        return encodeURL(theURL.toString());
    }

    // #endif

    // -------------------------------------------------------------------------
    // MJ 29 May. 2001 : get http path from request instead of settings,
    // NK 16 Apr. 2001

    /**
     * compose a Jahia site url
     */
    public String composeSiteUrl(final JahiaSite aSite) {
        final StringBuffer theUrl = new StringBuffer();
        theUrl.append(getJahiaCoreHttpPath());

        theUrl.append(getEngineURLPart(CORE_ENGINE_NAME));
        theUrl.append(getSiteURLPart(aSite.getSiteKey()));
        String paramOpMode = getParameter(OPERATION_MODE_PARAMETER);
        String opModeToUse = paramOpMode != null ? paramOpMode : getOpMode();
        theUrl.append(getOpModeURLPart(opModeToUse));

        if (COMPARE.equals(opModeToUse)) {
            theUrl.append(getEntryStateURLPart(getParameter(ENTRY_STATE_PARAMETER)));
            theUrl.append(getShowRevisionDiffURLPart(getDiffVersionID()));
        }
        theUrl.append(getPageURLKeyPart(aSite.getHomePageID()));
        theUrl.append(getPageURLPart(aSite.getHomePageID()));
        return encodeURL(theUrl.toString());
    }

    // -------------------------------------------------------------------------
    // MJ 29 May. 2001 : get http path from request instead of settings,
    // NK 16 Apr. 2001

    /**
     * compose a Jahia site url with the current site
     */
    public String composeSiteUrl() {
        return composeSiteUrl(getSite());
    }

    // -------------------------------------------------------------------------
    // @author NK

    /**
     * append params to a url
     *
     * @param theUrl the url
     * @param params the params to append to the url
     */
    protected void appendParams(final StringBuffer theUrl, String params) {

        if (params != null && (params.length() > 0)) {
            if (theUrl.toString().indexOf("?") == -1) {
                if (params.startsWith("&")) {
                    params = "?" + params.substring(1, params.length());
                } else if (!params.startsWith("?")) {
                    if (!params.startsWith("/")) {
                        params = "?" + params;
                    }
                }
            } else {
                if (!params.startsWith("&")) {
                    if (!params.startsWith("/")) {
                        params = "&" + params;
                    }
                }
            }
            theUrl.append(params);
        }

    }

    // -------------------------------------------------------------------------
    // @author NK

    /**
     * append an anchor to the url, used with application field
     *
     * @param theUrl the url
     */
    protected void appendAnchor(final StringBuffer theUrl) {

        if (this.anchor != null) {
            theUrl.append("#");
            theUrl.append(anchor);

            // reset the anchorID
            this.anchor = null;
        }
    }

    // -------------------------------------------------------------------------

    /**
     * Set the active anchor, that is the field id
     *
     * @param val the anchor
     */
    public void setAnchor(final String val) {
        this.anchor = val;
    }

    // -------------------------------------------------------------------------

    /**
     */
    public String getAnchor() {
        return this.anchor;
    }

    // -------------------------------------------------------------------------
    // @author NK

    /**
     * Used to get the templates jahiafiles disk path for the current site.
     *
     * @return The templates jahiafiles disk path.
     */
    public String getJahiaFilesTemplatesDiskPath() {

        final StringBuffer buff = new StringBuffer();
        buff.append(settings().getJahiaFilesTemplatesDiskPath());
        buff.append(File.separator);
        buff.append("site_");
        buff.append(getSite().getID());
        // check if the dir exist
        final File f = new File(buff.toString());
        if (!f.isDirectory()) {
            f.mkdirs();
        }
        return f.getAbsolutePath();

    } // end getJahiaFilesTemplatesDiskPath

    /**
     * Returns a String containing the full content generated by Jahia so far. Please note that calling this before all processing if
     * finished will returns incomplete content.
     *
     * @return a String containing the generated content.
     * @throws IOException if there was an error during output of content.
     */
    public String getGeneratedOutput() throws IOException {
        return generatedOutput;
    }

    /**
     * Returns the location set in a response redirect call.
     *
     * @return a String object that contains the location to which to redirect, or null if no redirect call has been made.
     */
    public String getRedirectLocation() {
        return redirectLocation;
    }

    /**
     * Returns a String containing the actual content type used by Jahia so far. Please note that this may change over time if multiple
     * calls to the wrapper response setContentType call are made (not good :( ).
     *
     * @return a String containing the current content type.
     */
    public String getContentType() {
        return contentType;
    }

    // -------------------------------------------------------------------------
    // @author NK

    /**
     * If the form data is non-multipart (simple), it returns true, otherwise returns false.
     *
     * @param req An HttpServletRequest.
     * @return True if the form data is non-multipart (simple).
     */
    public static boolean isMultipartRequest(final HttpServletRequest req) {
        final String contentType = req.getHeader("Content-Type");

        return ((contentType != null) && (contentType
                .indexOf("multipart/form-data") >= 0));
    }

    /**
     * If the request is a portlet request, it returns true, otherwise returns false.
     *
     * @param req An HttpServletRequest.
     * @return True if request is a portlet request.
     */
    public static boolean isPortletRequest(final HttpServletRequest req) {
        // look if there is appid in pathInfo; if true --> is a portletRequest
        boolean isPortletRequest = false;
        String pathInfo = req.getPathInfo();
        if (pathInfo != null) {
            pathInfo = pathInfo.toLowerCase();
            String appid = "/" + APPUNIQUE_ID_PARAMETER.toLowerCase() + "/";
            if (logger.isDebugEnabled()) {
                logger.debug("Path Info: " + pathInfo);
                logger.debug("appid : " + appid);
            }
            isPortletRequest = pathInfo.indexOf(appid) > -1;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Request is a portlet request? " + isPortletRequest);
        }
        return isPortletRequest;

    }

    /**
     * @return List a list of String objects that contain the page URL keys if any were included in the URL. Page keys are a more
     *         readable way of naming a page. Here is an example of an URL containing page keys :
     *         http://localhost:8080/jahia/Jahia/marketing/products/cache/offonce The result of a call to this method for the above URL
     *         would return: { "marketing", "products" }
     */
    public List<String> getPageURLKeys() {
        return (List<String>) pageURLKeys;
    }

    // -------------------------------------------------------------------------
    // FH 21 Jan. 2001 : Original implementation
    // MJ 21 Mar. 2001 : replaced URL params with context PathInfo elements
    protected String getEngineURLPart(final String theEngineName) {
        return condAppendURL(ENGINE_NAME_PARAMETER, theEngineName);
    }

    // -------------------------------------------------------------------------
    // NK 17 Avr. 2001 :
    public String getSiteURLPart(final String val) {
        if (isForceAppendSiteKey() || settings().isSiteIDInURL()
                || (!isSiteResolvedByServername() && (getDefaultSite() != null && !val.equals(getDefaultSite().getSiteKey())))) {
            return condAppendURL(SITE_KEY_PARAMETER, val);
        }
        return "";
    }

    // -------------------------------------------------------------------------
    // NK 17 Avr. 2001 :
    protected String getSiteURLPart() {
        return getSiteURLPart(getSite().getSiteKey());
    }

    // -------------------------------------------------------------------------
    // FH 21 Jan. 2001 : Original implementation
    // MJ 21 Mar. 2001 : replaced URL params with context PathInfo elements
    public String getPageURLPart(final int id) {
        return getPageURLPart(id, false);
    }

    // -------------------------------------------------------------------------
    // FH 21 Jan. 2001 : Original implementation
    // MJ 21 Mar. 2001 : replaced URL params with context PathInfo elements
    public String getPageURLPart(final int id, final boolean forceRender) {
        String pageURLPart = "";
        try {
            if (id > 0) {
                pageURLPart = getPageURLPart(ContentPage.getPage(id), forceRender);
            }
        } catch (JahiaException e) {
            logger.error("Error while setting page URL ID for page " + id,
                    e);
        }
        return pageURLPart;
    }

    protected String getPageURLPart(final ContentPage aContentPage,
                                    final boolean forceRender) {
        boolean mustRender = true;
        try {
            if (aContentPage
                    .getPageLocalProperty(PageProperty.PAGE_URL_KEY_PROPNAME) != null) {
                mustRender = false;
            }
        } catch (JahiaException je) {
            logger.error(
                    "Error while testing existing of page URL key for page "
                            + aContentPage, je);
        }

        return !mustRender && !forceRender ? "" : condAppendURL(
                PAGE_ID_PARAMETER, Integer.toString(aContentPage.getID()));
    }

    public String getPageURLKeyPart(final int id) {
        String pageURLKeysPart = "";
        try {
            if (id > 0) {
                pageURLKeysPart = getPageURLKeyPart(ContentPage.getPage(id));
            }
        } catch (JahiaException e) {
            logger.error("Error while testing existing of page URL key for page " + id,
                    e);
        }
        return pageURLKeysPart;
    }

    protected String getPageURLKeyPart(final ContentPage curContentPage) {
        // first we must find all parent pages and their URL keys.
        final StringBuffer pageURLKeysPartBuf = new StringBuffer();
        final LinkedList<String> pageURLKeyList = new LinkedList<String>();
        ContentPage curParentPage = null;
        boolean triedToSetPageURLKeyOfCurrentPage = false;
        try {
            final Iterator<ContentPage> pagePathEnum = curContentPage.getContentPagePath(this);
            while (pagePathEnum.hasNext()) {
                curParentPage = (ContentPage) pagePathEnum
                        .next();
                if (curParentPage.getID() == curContentPage.getID()) {
                    triedToSetPageURLKeyOfCurrentPage = true;
                }
                final PageProperty urlKeyProp = curParentPage
                        .getPageLocalProperty(PageProperty.PAGE_URL_KEY_PROPNAME);
                if (urlKeyProp != null) {
                    pageURLKeyList.add(urlKeyProp.getValue());
                }
            }
        } catch (JahiaException je) {
            logger.error("Error while testing existing of page URL key for page " + curParentPage,
                    je);
            // at least set the current page URL key, otherwise even the page-ID will not be set
            if (!triedToSetPageURLKeyOfCurrentPage) {
                try {
                    final PageProperty urlKeyProp = curContentPage
                            .getPageLocalProperty(PageProperty.PAGE_URL_KEY_PROPNAME);
                    if (urlKeyProp != null) {
                        pageURLKeyList.add(urlKeyProp.getValue());
                    }
                } catch (JahiaException e) {
                    logger.error("Error while testing existing of page URL key for page " + curContentPage,
                            je);
                }
            }
        }
        if (!pageURLKeyList.isEmpty()) {
            for (String curURLKey : pageURLKeyList) {
                pageURLKeysPartBuf.append("/");
                pageURLKeysPartBuf.append(curURLKey);
            }
        }
        return pageURLKeysPartBuf.toString();
    }

    // -------------------------------------------------------------------------
    // NK
    protected String getContainerListURLPart(final int id) {
        return condAppendURL(CONTAINERLIST_ID_PARAMETER, Integer.toString(id));

    }

    protected String getContainerURLPart(final int id) {
        return condAppendURL(CONTAINER_ID_PARAMETER, Integer.toString(id));

    }

    protected String getFieldURLPart(final int id) {
        return condAppendURL(FIELD_ID_PARAMETER, Integer.toString(id));

    }

    // -------------------------------------------------------------------------
    // FH 21 Jan. 2001 : Original implementation
    // MJ 21 Mar. 2001 : replaced URL params with context PathInfo elements
    protected String getOpModeURLPart(final String mode) {
        return condAppendURL(OPERATION_MODE_PARAMETER, mode);

    }

    // -------------------------------------------------------------------------
    // SH 12 Oct. 2001 : Original implementation
    protected String getCacheModeURLPart(final String mode) {
        return condAppendURL(CACHE_MODE_PARAMETER, mode);
    }

    // --------------------------------------------------------------------------

    /**
     * @param status "s" for staging, "a" for active
     */
    protected String getEntryStateURLPart(final String status) {
        return condAppendURL(ProcessingContext.ENTRY_STATE_PARAMETER, status);
    }

    // --------------------------------------------------------------------------

    /**
     * by default show revision difference with the staging first else with the active version
     */
    protected String getShowRevisionDiffURLPart() {
        return getShowRevisionDiffURLPart(2);
    }

    // --------------------------------------------------------------------------

    /**
     * by default show revision difference. If the versionID == 0 -> show diff with staging ( active if not exists )
     *
     * @param versionID
     */
    protected String getShowRevisionDiffURLPart(final int versionID) {
        final String value;
        if (versionID == 2) {
            value = condAppendURL(
                    ProcessingContext.SHOW_REVISION_DIFF_PARAMETER, "s");
        } else if (versionID == 1) {
            value = condAppendURL(
                    ProcessingContext.SHOW_REVISION_DIFF_PARAMETER, "a");
        } else {
            value = condAppendURL(
                    ProcessingContext.SHOW_REVISION_DIFF_PARAMETER, String
                    .valueOf(versionID));
        }
        return value;
    }

    // -------------------------------------------------------------------------
    // @author MJ

    /**
     * Build an http path containing the server name for the current site, instead of the path from JahiaPrivateSettings. This does NOT end
     * with a "/" character.
     *
     * @return An http path leading to Jahia, built with the server name, and the server port if nonstandard.
     */
    protected String getJahiaCoreHttpPath() {
        // logger.debug(" request.getServername = " + mRequest.getServerName() );
        // logger.debug(" request.getContextPath = " + mRequest.getContextPath() );
        // logger.debug(" request.getServletPath = " + mRequest.getServletPath() );
        // logger.debug(" request.getRequestURI = " + mRequest.getRequestURI() );
        final StringBuffer buffer = new StringBuffer();

        if (Jahia.getContextPath() != null) {
            buffer.append(Jahia.getContextPath());
        } else {
            buffer.append(getContextPath());
        }

        if (Jahia.getServletPath() != null) {
            buffer.append(Jahia.getServletPath());
        } else {
            // should only happen when the Jahia servlet hasn't been called at
            // least once !
            buffer.append(getServletPath());
        }

        return buffer.toString();
    }

    /**
     * Special wrap around response.encodeURL to deactivate the cache in case jsessionid parameters are generated. This method modifies the
     * internal cacheStatus variable to modify the state.
     *
     * @param inputURL the string for the URL to encode
     * @return the encoded URL string.
     */
    public String encodeURL(final String inputURL) {
        if (inputURL.indexOf(";jsessionid=") != -1) {
            return inputURL;
        }
        final String encodedURL = getUrlGenerator().encodeURL(inputURL);
        return encodedURL;
    }

    public String getSiteURL() {
        if (getPage() != null) {
            return getSiteURL(getPageID(), true, true);
        }
        return getSiteURL(-1, true, true);
    }

    public String getSiteURL(final int pageID, final boolean withSessionID,
                             final boolean withOperationMode) {
        // TODO: this does not fully support ESI like ParamBean since no headers are accessible
        // let's test if the URL entered for the site is valid, and generate
        // an URL
        final JahiaSite theSite = getSite();
        return theSite == null ? "" : getSiteURL(theSite, pageID,
                withSessionID, withOperationMode, false);
    }

    /**
     * Generates a complete URL for a site. Uses the site URL serverName to generate the URL *only* it is resolves in a DNS. Otherwise it
     * simply uses the current serverName and generates a URL with a /site/ parameter
     *
     * @param pageID               A site page ID on which the URL should point to.
     * @param withSessionID        a boolean that specifies whether we should call the encodeURL method on the generated URL. Most of the time we will
     *                             just want to set this to true, but in the case of URLs sent by email we do not, otherwise we have a security problem
     *                             since we are sending SESSION IDs to people that should not have them.
     * @param withOperationMode    a boolean that specifies whether we should include the operation mode in the URL or not.
     * @param forceServerNameInURL allow to ensure that the server name is present in the url (useful for mail)
     * @param theSite              the site agaisnt we build the url
     * @return String a full URL to the site using the currently set values in the ProcessingContext.
     */
    public String getSiteURL(final JahiaSite theSite, final int pageID,
                             final boolean withSessionID, final boolean withOperationMode,
                             boolean forceServerNameInURL) {

        return getSiteURL(theSite, pageID, withSessionID, withOperationMode, null, forceServerNameInURL);
    }

    /**
     * Generates a complete URL for a site. Uses the site URL serverName to generate the URL *only* it is resolves in a DNS. Otherwise it
     * simply uses the current serverName and generates a URL with a /site/ parameter
     *
     * @param pageID               A site page ID on which the URL should point to.
     * @param withSessionID        a boolean that specifies whether we should call the encodeURL method on the generated URL. Most of the time we will
     *                             just want to set this to true, but in the case of URLs sent by email we do not, otherwise we have a security problem
     *                             since we are sending SESSION IDs to people that should not have them.
     * @param withOperationMode    a boolean that specifies whether we should include the operation mode in the URL or not.
     * @param languageCode         force inclusion of the language code (if not null) 
     * @param forceServerNameInURL allow to ensure that the server name is present in the url (useful for mail)
     * @param theSite              the site agaisnt we build the url
     * @return String a full URL to the site using the currently set values in the ProcessingContext.
     */
    public String getSiteURL(final JahiaSite theSite, final int pageID,
                             final boolean withSessionID, final boolean withOperationMode, String languageCode,
                             boolean forceServerNameInURL) {

        String operationMode = null;
        if (withOperationMode) {
            final SessionState session = this.getSessionState();
            if (session.getAttribute(OPERATION_MODE_PARAMETER) != null) {
                if (session.getAttribute(OPERATION_MODE_PARAMETER) instanceof String) {
                    String oldOpMode = (String) session
                            .getAttribute(OPERATION_MODE_PARAMETER);
                    operationMode = oldOpMode;
                }
            } else {
                operationMode = this.getOperationMode();
            }
        }
        
        return getSiteURL(theSite, pageID, withSessionID, operationMode, languageCode, forceServerNameInURL);
    }

    /**
     * Generates a complete URL for a site. Uses the site URL serverName to generate the URL *only* it is resolves in a DNS. Otherwise it
     * simply uses the current serverName and generates a URL with a /site/ parameter
     *
     * @param theSite              the site agaisnt we build the url
     * @param pageID               A site page ID on which the URL should point to.
     * @param withSessionID        a boolean that specifies whether we should call the encodeURL method on the generated URL. Most of the time we will
     *                             just want to set this to true, but in the case of URLs sent by email we do not, otherwise we have a security problem
     *                             since we are sending SESSION IDs to people that should not have them.
     * @param operationMode    operation mode to use
     * @param languageCode         force inclusion of the language code (if not null) 
     * @param forceServerNameInURL allow to ensure that the server name is present in the url (useful for mail)
     * @return String a full URL to the site using the currently set values in the ProcessingContext.
     */
    public String getSiteURL(final JahiaSite theSite, final int pageID,
                             final boolean withSessionID, String operationMode, String languageCode,
                             boolean forceServerNameInURL) {

        final String siteServerName = theSite.getServerName();
        String sessionIDStr = null;

        final StringBuilder newSiteURL = new StringBuilder(64);
        if (!settings().isUseRelativeSiteURLs() || forceServerNameInURL) {
            newSiteURL.append(getScheme()).append("://");
        }

        if (!forceServerNameInURL) {
            if (!settings().isUseRelativeSiteURLs()) {
                // let's construct an URL by deconstruct our current URL and
                // using the site name as a server name
                newSiteURL.append(siteServerName);
                if (!siteServerName.equals(getServerName())) {
                    // serverName has changed, we must transfer cookie information
                    // for sessionID if there is some.
                    try {
                        sessionIDStr = ";jsessionid=" + getSessionID();
                    } catch (JahiaSessionExpirationException jsee) {
                        logger.warn("Error retrieving session ID", jsee);
                    }
                }

                int siteURLPortOverride = settings().getSiteURLPortOverride();
                if (siteURLPortOverride > 0) {
                    if (siteURLPortOverride != 80) {
                        newSiteURL.append(":");
                        newSiteURL.append(siteURLPortOverride);
                    }
                } else if (getServerPort() != 80) {
                    newSiteURL.append(":");
                    newSiteURL.append(getServerPort());
                }
            }
            newSiteURL.append(getContextPath());
            newSiteURL.append(Jahia.getServletPath());
        } else {
            // let's construct an URL by deconstruct our current URL and insering
            // the site id key as a parameter
            newSiteURL.append(getServerName());

            int siteURLPortOverride = settings().getSiteURLPortOverride();
            if (siteURLPortOverride > 0) {
                if (siteURLPortOverride != 80) {
                    newSiteURL.append(":");
                    newSiteURL.append(siteURLPortOverride);
                }
            } else if (getServerPort() != 80) {
                newSiteURL.append(":");
                newSiteURL.append(getServerPort());
            }
            newSiteURL.append(getContextPath());
            newSiteURL.append(Jahia.getServletPath());
            newSiteURL.append("/site/");
            newSiteURL.append(theSite.getSiteKey());
        }

        if (operationMode != null) {
            newSiteURL.append(getOpModeURLPart(operationMode));
        }
        
        if (languageCode != null) {
            newSiteURL.append(appendParam(LANGUAGE_CODE, languageCode));
        }

        if (pageID != -1) {
            newSiteURL.append(getPageURLKeyPart(pageID));
            newSiteURL.append(getPageURLPart(pageID));
        }

        if (withSessionID) {
            String serverURL = encodeURL(newSiteURL.toString());
            if (sessionIDStr != null) {
                if (serverURL.indexOf("jsessionid") == -1) {
                    serverURL += sessionIDStr;
                }
            }
            return serverURL;
        } else {
            return newSiteURL.toString();
        }
    }

    /**
     * @return a date which is the expiration date at which the page cache will expire. If null, the page cache will never expire on a basis
     *         of time (but may be flush upon content changes or other events).
     */
    public Date getCacheExpirationDate() {
        if (cacheExpirationDate != null)
            return cacheExpirationDate;
        else if (delayFromNow > -1) {
            return new Date(System.currentTimeMillis() + delayFromNow);
        }
        return null;
    }

    /**
     * Sets the current page's cache expiration date.
     *
     * @param aCacheExpirationDate a date which is the expiration date at which the page cache will expire. If set to null or never set, the page cache
     *                             will never expire on a basis of time (but may be flush upon content changes or other events).
     */
    final public void setCacheExpirationDate(Date aCacheExpirationDate) {
        this.cacheExpirationDate = aCacheExpirationDate;
    }

    /**
     * Sets the current page's cache expiration delay, starting from the current locale time.
     *
     * @param aDelayFromNow an long value specifying the delay in milliseconds from now for the expiration of the current page's cache.
     */
    final public void setCacheExpirationDelay(long aDelayFromNow) {
        this.delayFromNow = aDelayFromNow;
    }

    /**
     * Gets the current page's cache expiration delay, starting from the current locale time.
     */
    final public long getCacheExpirationDelay() {
        return delayFromNow;
    }

    protected void processActivationAction() throws JahiaException {
        // read validate as parameter
        final String validate = getParameter(VALIDATE_PARAMETER);
        if (validate != null) {
            if (validate.equals("on")) {
                if ((getContentPage() != null) && (getTheUser() != null)) {
                    logger.debug("Validating page");
                    final Map<String, Integer> languageStates = getContentPage()
                            .getLanguagesStates(true);
                    final Set<String> languageCodes = languageStates.keySet();
                    REGISTRY.getJahiaVersionService().activateStagedPage(
                            getContentPage().getID(),
                            getTheUser(),
                            this,
                            new StateModificationContext(new ContentPageKey(
                                    getContentPage().getID()), languageCodes,
                                    false));
                } else {
                    logger.debug("Can't validate page, user or page not found");
                }
            }
        }
    }

    protected void resolveCacheStatus() {
    }

    protected void resolveJahiaPage() throws JahiaException {
        if (getContentPage() != null) {
            JahiaUserAliasing userAliasing = new JahiaUserAliasing(getUser());
            setThePage(getContentPage().getPage(getEntryLoadRequest(),
                    getOperationMode(), userAliasing));
        }
    }

    public void checkPageAccess() throws JahiaException {
        if (getContentPage() != null) {
            checkPageAccess(getContentPage().getID());
        }
    }

    private void checkPageAccess(final int pageID) throws JahiaException {
        if (settings().isReadOnlyMode()
                && CORE_ENGINE_NAME.equals(this.getEngine())
                && !NORMAL.equals(getOpMode()))
            throw new JahiaInvalidModeException();

        if (getContentPage() != null) {
            int pageType = getContentPage().getPageType(getEntryLoadRequest());
            if (pageType > -1 && pageType != ContentPage.TYPE_DIRECT) {
                logger.warn("Trying to display non-direct page ID " + pageID);
                throw new JahiaPageNotFoundException(pageID);
            }

            final int deleteVersionID = getContentPage().getDeleteVersionID();
            if (deleteVersionID != -1) {
                // page has been recently deleted, let's check if we can
                // display it or not.
                if (NORMAL.equals(getOpMode())) {
                    // this is the case of try to access a deleted page in NORMAL
                    // mode which is not allowed.
                    throw new JahiaPageNotFoundException(pageID);

                } else if (CORE_ENGINE_NAME.equals(this.getEngine())
                        && EDIT.equals(getOpMode())
                        && (!getContentPage().hasStagingEntries() /* || getContentPage().isMarkedForDelete() */)) {
                    // try to access a deleted page in EDIT mode is not allowed.
                    throw new JahiaPageNotFoundException(pageID);

                } else if ((COMPARE.equals(getOpMode()) || (PREVIEW.equals(getOpMode())))
                        && (!getContentPage().checkWriteAccess(getTheUser()))) {
                    // we can view a deleted page in compare and preview mode
                    // only if we can do edition operations on the page.
                    throw new JahiaPageNotFoundException(pageID);
                }
            }

            if (!org.jahia.settings.SettingsBean.getInstance().isDisplayMarkedForDeletedContentObjects()) {
                if (getContentPage().isMarkedForDelete()) {
                    if (getContentPage().getActiveVersionID() <= 0) {
                        changePage(getSite().getHomePageID());
                    }
                }

            } else {
                if (CORE_ENGINE_NAME.equals(getEngine()) && EDIT.equals(getOpMode()) &&
                        !getEntryLoadRequest().isWithMarkedForDeletion()) {
                    // try to access a marked for delete in EDIT mode is not allowed.
                    setEntryLoadRequest(new EntryLoadRequest(
                            EntryLoadRequest.STAGING_WORKFLOW_STATE, 0,
                            getLocales(), true));
                    // reset substitute entry load request
                    resetSubstituteEntryLoadRequest();
                }
            }

            // if the page is not found, throw the associated exception
            if (NORMAL.equals(getOpMode())
                    && !getContentPage().hasActiveEntries()
                    && deleteVersionID == -1) {

                setOperationMode(EDIT);
                setEntryLoadRequest(new EntryLoadRequest(
                        EntryLoadRequest.STAGING_WORKFLOW_STATE, 0,
                        getLocales(), org.jahia.settings.SettingsBean.getInstance().isDisplayMarkedForDeletedContentObjects()));
                // reset substitute entry load request
                resetSubstituteEntryLoadRequest();
                if (!getEngine().equals(Login_Engine.ENGINE_NAME)) {
                    if (JahiaUserManagerService.isGuest(getUser())) {
                        throw new JahiaUnauthorizedException();
                    } else {
                        throw new JahiaForbiddenAccessException();
                    }
                }
            }
        }

        if ((getContentPage() == null)
                || ((getOpMode().equals(NORMAL) || getOpMode().equals(COMPARE))
                && getEntryLoadRequest().isCurrent() && !getContentPage().hasActiveEntries())
                || ((getOpMode().equals(NORMAL) || getOpMode().equals(COMPARE))
                && getEntryLoadRequest().isCurrent() && getContentPage().getTitle(getEntryLoadRequest()) == null)) {
            throw new JahiaPageNotFoundException(pageID);
        }
        // Ensure if the requested page is a page of the current site
        if (getContentPage().getJahiaID() != getSiteID()) {
            setContentPage(getSite().getHomeContentPage());
        }
        if (contentPage != null && getPage() == null
                && !getEngine().equals(Login_Engine.ENGINE_NAME)) {
            throw new JahiaForbiddenAccessException();
        }
        // last requested page
        Integer lrpID = (Integer) getSessionState().getAttribute(
                SESSION_LAST_REQUESTED_PAGE_ID);
        if (lrpID == null) {
            lrpID = new Integer(-1);
        }
        setNewPageRequest(lrpID.intValue() != getPageID());
        setLastRequestedPageID(lrpID.intValue());
    }

    public void checkLocales() throws JahiaException {
        if (getOpMode().equals(NORMAL) && getContentPage() != null
                && !getContentPage().hasEntries(ContentPage.ACTIVE_PAGE_INFOS,
                getLocale().toString())) {
            final List<Locale> siteLanguages = getSite()
                    .getLanguageSettingsAsLocales(true);
            boolean skip = getSite().isMixLanguagesActive();
            for (int i = 0, siteLanguagesSize = siteLanguages.size(); i < siteLanguagesSize && !skip; i++) {
                final Locale locale = siteLanguages.get(i);
                if (getContentPage().hasEntries(ContentPage.ACTIVE_PAGE_INFOS,
                        locale.toString())) {
                    changeLanguage(locale);
                    skip = true;
                }
            }
        }
    }

    protected void resolveDiffVersionID(String verInfo) {
        // read difference version id
        verInfo = getParameter(ProcessingContext.SHOW_REVISION_DIFF_PARAMETER);
        if (verInfo != null) {
            if ("s".equals(verInfo)) {
                setDiffVersionID(2); // staging
            } else if ("a".equals(verInfo)) {
                setDiffVersionID(1); // active
            } else {
                try {
                    int ver = Integer.parseInt(verInfo);
                    setDiffVersionID(ver);
                } catch (NumberFormatException nfe) {
                    logger.debug("Diff VersionID format exception", nfe);
                }
            }
        }
    }

    protected String resolveEntryState() throws JahiaException {
        // read version info as parameter
        final String verInfo = getParameter(ENTRY_STATE_PARAMETER);
        if (verInfo != null) {
            if ("s".equals(verInfo)) {
                setEntryLoadRequest(new EntryLoadRequest(
                        EntryLoadRequest.STAGING_WORKFLOW_STATE, 0,
                        getLocales(), true));
            } else if ("a".equals(verInfo)) {
                setEntryLoadRequest(new EntryLoadRequest(
                        EntryLoadRequest.ACTIVE_WORKFLOW_STATE, 0, getLocales()));
            } else {
                try {
                    final int ver = Integer.parseInt(verInfo);
                    setEntryLoadRequest(new EntryLoadRequest(
                            EntryLoadRequest.VERSIONED_WORKFLOW_STATE, ver,
                            getLocales()));
                    if (logger.isDebugEnabled()) {
                        logger.debug("Using entry load request specified : "
                                + getEntryLoadRequest());
                    }
                } catch (NumberFormatException nfe) {
                    logger.debug("VersionID format exception", nfe);
                }
            }
        }
        return verInfo;
    }

    protected String processLockAction() {
        // #ifdef LOCK
        // let's unlock the object
        final LockService lockRegistry = REGISTRY.getLockService();
        String lockKeyStr = getParameter(ProcessingContext.STEAL_LOCK);
        LockKey lockKey;
        final JahiaUser user = getUser();
        if (lockKeyStr != null) {
            lockKey = LockKey.composeLockKey(lockKeyStr);
            lockRegistry.steal(lockKey, user, user.getUserKey());
        } else {
            lockKeyStr = getParameter(ProcessingContext.RELEASE_LOCK);
            if (lockKeyStr != null) {
                lockKey = LockKey.composeLockKey(lockKeyStr);
                lockRegistry.release(lockKey, user, user.getUserKey());
            }
        }
        return lockKeyStr;
        // #endif
    }

    protected void resolveLocales() throws JahiaException {
        // let's try to get the current locale if it was in the session.
        String languageCode = getParameter(LANGUAGE_CODE);

        if (languageCode != null
                && !LicenseActionChecker
                .isAuthorizedByLicense(
                        "org.jahia.actions.sites.*.admin.languages.ManageSiteLanguages",
                        0)) {
            // if language management is not authorized by the license then we
            // don't allow language
            // switching.
            languageCode = null;
        }

        if (languageCode != null) {
            final Locale previousLocale = (Locale) getSessionState()
                    .getAttribute(SESSION_LOCALE);
            final Locale newLocale = LanguageCodeConverters
                    .languageCodeToLocale(languageCode);
            if (previousLocale == null || !previousLocale.equals(newLocale)) {
                // if the locale has changed, we must reevaluate the full
                // locale list.
                setLocaleList(null);
            }
            getSessionState().setAttribute(SESSION_LOCALE, newLocale);
        }
        if (getSessionState().getAttribute(SESSION_LOCALE) != null) {
            setCurrentLocale((Locale) getSessionState().getAttribute(
                    SESSION_LOCALE));
        } else {
            // it's not in the session, let's try to determine what it should
            // be...
            setCurrentLocale(null);
            setLocaleList(null);
            getLocale();
            getSessionState().setAttribute(SESSION_LOCALE, getCurrentLocale());
        }

        // CHECK LOCALE INTEGRITY
        setCurrentLocale((Locale) getLocales().get(1)); // 0=shared, 1=resolved locale
        getSessionState().setAttribute(SESSION_LOCALE, getCurrentLocale());

        setEntryLoadRequest(new EntryLoadRequest(
                EntryLoadRequest.ACTIVE_WORKFLOW_STATE, 0, getLocales()));
        // compute version info
        if (getSiteID() != -1) {
            if ((EDIT.equals(getOpMode()) || PREVIEW.equals(getOpMode()) || COMPARE
                    .equals(getOpMode()))
                    && REGISTRY.getJahiaVersionService().isStagingEnabled(
                    this.getSiteID())) {
                setEntryLoadRequest(new EntryLoadRequest(
                        EntryLoadRequest.STAGING_WORKFLOW_STATE, 0,
                        getLocales(), true));
            }
        }
    }

    public boolean canEditCurrentPage() {
        if (getCanEdit() == null) {
            // JahiaBaseACL cpAcl = contentPage.getACL();
            if (getContentPage() == null) {
                return true;
            }
            setCanEdit(Boolean.valueOf((getContentPage().checkWriteAccess(getTheUser(), true) ||
                                        getContentPage().checkAdminAccess(getTheUser(), true) ) &&
                                        REGISTRY.getJahiaACLManagerService().getSiteActionPermission( "engines.actions.editMode", 
                                                getUser(), JahiaBaseACL.READ_RIGHTS, getSiteID()) > 0));
        }

        return getCanEdit().booleanValue();
    }

    protected void resolveOpMode(final SessionState aSessionState)
            throws JahiaInitializationException,
            JahiaSessionExpirationException {
        // Define the requested Operation
        String paramOpMode = getParameter(OPERATION_MODE_PARAMETER);
        if (paramOpMode != null) {
            if (NORMAL.equals(paramOpMode))
                setOpMode(NORMAL);
            else if (COMPARE.equals(paramOpMode))
                setOpMode(canEditCurrentPage() ? COMPARE : NORMAL);
            else if (EDIT.equals(paramOpMode))
                setOpMode(canEditCurrentPage() ? EDIT : NORMAL);
            else if (PREVIEW.equals(paramOpMode)) {
                setOpMode((canEditCurrentPage())
                        ? PREVIEW : NORMAL);
            } else if (DEBUG.equals(paramOpMode))
                setOpMode(contentPage.checkAdminAccess(theUser) ? DEBUG
                        : NORMAL);
        }

        if (NORMAL.equals(getOpMode()) && getContentPage() != null
                && !getContentPage().hasActiveEntries() && canEditCurrentPage()) {
            try {
                final int deleteVersionID = getContentPage()
                        .getDeleteVersionID();
                if (deleteVersionID == -1) {
                    // switch to edit mode because no active version available
                    setOpMode(EDIT);
                }
            } catch (JahiaException je) {
                logger.error("Error while trying to test if page was deleted",
                        je);
            }
        }

        // only store mode in session if we are not in administration
        // servlet.
        if (!isInAdminMode()) {
            getSessionState().setAttribute(OPERATION_MODE_PARAMETER,
                    getOpMode());
        }

        if ((EDIT.equals(getOpMode())) || (PREVIEW.equals(getOpMode()))
                || (COMPARE.equals(getOpMode()))) {
            // we are in an "editing" mode, we expand the session's expiration time
            // we first save the previous inactive interval, that we will restore on logout
            if (getSessionState().getAttribute("previousInactiveInterval") == null) {
                int previousInactiveInterval = getSessionState()
                        .getMaxInactiveInterval();
                getSessionState().setAttribute("previousInactiveInterval",
                        new Integer(previousInactiveInterval));
                getSessionState().setMaxInactiveInterval(
                        settings().getEditModeSessionTimeout());
            }
        }

        if (PREVIEW.equals(getOpMode())){
            try {
                AdvPreviewSettings.setThreadLocalAdvPreviewSettings(getSessionState());
            } catch ( Throwable t ){
                logger.debug("Exception occured when setting Adv Preview Settings",t);
            }
        } else if (COMPARE.equals(getOpMode())){
            try {
                AdvCompareModeSettings.setThreadLocalAdvCompareModeSettings(getSessionState());
            } catch ( Throwable t ){
                logger.debug("Exception occured when setting Adv Preview Settings",t);
            }
        }
    }

    protected int resolvePageID() throws JahiaPageNotFoundException {
        int pageID = -1;
        if (this.getContentPage() == null) {
            // Get the page information, if no page info is specified, then
            // load the default page.
            String pageIDStr = getParameter(PAGE_ID_PARAMETER);
            if (pageIDStr == null
                    && (this.getEngine().equals(CORE_ENGINE_NAME)
                    || this.getEngine().equals("login")
                    || this.getEngine().equals("export") || XMLSOURCE_ENGINE_NAME
                    .equals(this.getEngine()))) {
                pageIDStr = Integer.toString(this.getSite().getHomePageID());
            }

            if (pageIDStr != null) {
                // try to get the page reference.
                try {

                    pageID = Integer.parseInt(pageIDStr);
                } catch (NumberFormatException nfe) {
                    logger.debug("Number format exception");
                    logger.debug("Request URI = " + getRequestURI());
                    throw new JahiaPageNotFoundException(pageIDStr);
                }
            }
        } else {
            pageID = this.getPageID();
        }
        return pageID;
    }

    protected void resolveUser() throws JahiaException {

        final Pipeline authPipeline = Jahia.getAuthPipeline();
        try {
            authPipeline.invoke(this);
        } catch (PipelineException pe) {
            logger.error("Error while authorizing user", pe);
            setTheUser(null);
        }

        if (getTheUser() == null) {
            setUserGuest();
        }

        try {
            AdvPreviewSettings.setThreadLocalAdvPreviewSettings(getSessionState());
        } catch ( Throwable t ){
            throw new JahiaException("Exception occured initializing user aliasing"
                ,"Exception occured initializing user aliasing",JahiaException.APPLICATION_ERROR,
                JahiaException.ERROR_SEVERITY,t);
        }

    }

    /**
     * Used internally by authorization pipeline. Do not call this method from somewhere else (such as templates)
     *
     * @param aUser JahiaUser
     */
    public void setTheUser(final JahiaUser aUser) {
        this.theUser = aUser;
    }

    public String getResponseMimeType() {
        return responseMimeType;
    }

    public void setResponseMimeType(final String aResponseMimeType) {
        this.responseMimeType = aResponseMimeType;
    }

    public String getRequestURI() {
        return requestURI;
    }

    public void setRequestURI(final String aRequestURI) {
        this.requestURI = aRequestURI;
    }

    public String getContextPath() {
        return contextPath;
    }

    public void setContextPath(final String aContextPath) {
        this.contextPath = aContextPath;
    }

    public String getServletPath() {
        return servletPath;
    }

    public void setServletPath(final String aServletPath) {
        this.servletPath = aServletPath;
    }

    public String getPathInfo() {
        return pathInfo;
    }

    public void setPathInfo(final String aPathInfo) {
        this.pathInfo = aPathInfo;
    }

    public String getQueryString() {
        return queryString;
    }

    public void setQueryString(final String aQueryString) {
        this.queryString = aQueryString;
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(final String aServerName) {
        this.serverName = aServerName;
    }

    public int getServerPort() {
        return serverPort;
    }

    public void setServerPort(final int aServerPort) {
        this.serverPort = aServerPort;
    }

    public String getScheme() {
        return scheme;
    }

    public void setScheme(final String aScheme) {
        this.scheme = aScheme;
    }

    public void setStartTime(final long aStartTime) {
        this.startTime = aStartTime;
    }

    public int getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(final int aHttpMethod) {
        this.httpMethod = aHttpMethod;
    }

    public String getEngineName() {
        return engineName;
    }

    public void setEngineName(final String anEngineName) {
        this.engineName = anEngineName;
    }

    public void setFieldID(final int aFieldID) {
        this.fieldID = aFieldID;
    }

    public void setContainerID(final int aContainerID) {
        this.containerID = aContainerID;
    }

    public void setContainerListID(final int aContainerListID) {
        this.containerListID = aContainerListID;
    }

    public String getOpMode() {
        return opMode;
    }

    public void setOpMode(final String anOpMode) {
        this.opMode = anOpMode;
    }

    public JahiaPage getThePage() {
        return thePage;
    }

    public void setThePage(final JahiaPage aPage) {
        this.thePage = aPage;
    }

    public void setContentPage(final ContentPage aContentPage) {
        this.contentPage = aContentPage;
    }

    public JahiaUser getTheUser() {
        return theUser;
    }

    public void setUserAgent(final String anUserAgent) {
        this.userAgent = anUserAgent;
    }

    public void setSiteID(final int aSiteID) {
        this.siteID = aSiteID;
    }

    public void setSiteKey(final String aSiteKey) {
        this.siteKey = aSiteKey;
    }

    public void setSite(final JahiaSite aSite) {
        this.site = aSite;
    }

    public boolean isSiteHasChanged() {
        return siteHasChanged;
    }

    public void setSiteHasChanged(final boolean aSiteHasChanged) {
        this.siteHasChanged = aSiteHasChanged;
    }

    public Map<String, Object> getCustomParameters() {
        return customParameters;
    }

    public void setCustomParameters(final Map<String, Object> customParameterMap) {
        this.customParameters = customParameterMap;
    }

    public void setLastRequestedPageID(final int aLastRequestedPageID) {
        this.lastRequestedPageID = aLastRequestedPageID;
    }

    public boolean isNewPageRequest() {
        return newPageRequest;
    }

    public void setNewPageRequest(final boolean aNewPageRequest) {
        this.newPageRequest = aNewPageRequest;
    }

    public void setLastEngineName(final String aLastEngineName) {
        this.lastEngineName = aLastEngineName;
    }

    public boolean isEngineHasChanged() {
        return engineHasChanged;
    }

    public void setEngineHasChanged(final boolean engineHasChangedFlag) {
        this.engineHasChanged = engineHasChangedFlag;
    }

    public EntryLoadRequest getSubstituteEntryLoadRequest() {
        return substituteEntryLoadRequest;
    }

    public static Properties getDefaultParameterValues() {
        return defaultParameterValues;
    }

    public static void setDefaultParameterValues(
            final Properties defaultParameters) {
        defaultParameterValues = defaultParameters;
    }

    public boolean isUseQueryStringParameterUrl() {
        return useQueryStringParameterUrl;
    }

    public void setDiffVersionID(final int aDiffVersionID) {
        this.diffVersionID = aDiffVersionID;
    }

    public void setGeneratedOutput(final String aGeneratedOutput) {
        this.generatedOutput = aGeneratedOutput;
    }

    public void setRedirectLocation(final String aRedirectLocation) {
        this.redirectLocation = aRedirectLocation;
    }

    public void setContentType(final String aContentType) {
        this.contentType = aContentType;
    }

    public void setPageURLKeys(final List<String> pageURLKeyList) {
        this.pageURLKeys = pageURLKeyList;
    }

    public Locale getCurrentLocale() {
        return currentLocale;
    }

    public void setCurrentLocale(final Locale theCurrentLocale) {
        this.currentLocale = theCurrentLocale;
    }

    public List<Locale> getLocaleList() {
        return localeList;
    }

    public void setLocaleList(final List<Locale> localeList) {
        this.localeList = localeList;
    }

    public URLGenerator getUrlGenerator() {
        return urlGenerator;
    }

    public void setUrlGenerator(final URLGenerator anUrlGenerator) {
        this.urlGenerator = anUrlGenerator;
    }

    public SessionState getSessionState() {
        return sessionState;
    }

    public Map<String, Object> getParameterMap() {
        return customParameters;
    }

    public void setParameterMap(final Map<String, Object> parameterMap) {
        this.customParameters = parameterMap;
    }

    public Map<String, Object> getAttributeMap() {
        return attributeMap;
    }

    public void setAttributeMap(final Map<String, Object> anAttributeMap) {
        this.attributeMap = anAttributeMap;
    }

    public Object getAttribute(final String attributeName) {
        return attributeMap.get(attributeName);
    }

    public Object getAttributeSafeNoNullPointer(final String attributeName) {
        if (attributeMap != null) {
            return attributeMap.get(attributeName);
        }
        return null;
    }

    public void setAttribute(final String attributeName,
                             final Object attributeObject) {
        this.attributeMap.put(attributeName, attributeObject);
    }

    public void removeAttribute(final String attributeName) {
        this.attributeMap.remove(attributeName);
    }

    public Iterator<String> getAttributeNames() {
        return new ArrayList<String>(attributeMap.keySet()).iterator();
    }

    public void setSessionState(final SessionState aSessionState) {
        this.sessionState = aSessionState;
    }

    public Iterator<String> getParameterNames() {
        return customParameters.keySet().iterator();
    }

    public String[] getParameterValues(final String parameterName) {
        return (String[]) customParameters.get(parameterName);
    }

    public String getRemoteAddr() {
        return remoteAddr;
    }

    public void setRemoteAddr(final String aRemoteAddr) {
        this.remoteAddr = aRemoteAddr;
    }

    public String getRemoteHost() {
        return remoteHost;
    }

    public void setRemoteHost(final String aRemoteHost) {
        this.remoteHost = aRemoteHost;
    }

    public String getCharacterEncoding() {
        return characterEncoding;
    }

    public void setCharacterEncoding(final String aCharacterEncoding) {
        this.characterEncoding = aCharacterEncoding;
    }

    /**
     * enable the application of a filter
     *
     * @param filterName
     */
    public void enableFilter(String filterName) {
        if (filterName != null) {
            getFilterStatusMap().put(filterName,
                    new Integer(FILTER_ENABLED_STATE));
        }
    }

    /**
     * disable the application of a filter
     *
     * @param filterName
     */
    public void disableFilter(String filterName) {
        if (filterName != null) {
            getFilterStatusMap().put(filterName,
                    new Integer(FILTER_DISABLED_STATE));
        }
    }

    /**
     * Returns true if the given filter is enabled or not
     *
     * @param filterName
     * @return
     */
    public boolean isFilterEnabled(String filterName) {
        if (filterName == null) {
            return false;
        }
        Integer status = (Integer) getFilterStatusMap().get(filterName);
        return (status != null && status.intValue() == FILTER_ENABLED_STATE);
    }

    /**
     * Returns true if the given filter is really disabled. If the state is not defined, return true.
     *
     * @param filterName
     * @return
     */
    public boolean isFilterDisabled(String filterName) {
        if (filterName == null) {
            return false;
        }
        Integer status = (Integer) getFilterStatusMap().get(filterName);
        return (status != null && status.intValue() == FILTER_DISABLED_STATE);
    }

    /**
     * set to undefined state for the given filter
     *
     * @param filterName
     */
    public void resetFilterStatus(String filterName) {
        if (filterName != null) {
            getFilterStatusMap().remove(filterName);
        }
    }

    private Map<String, Integer> getFilterStatusMap() {
        Map<String, Integer> map = filtersStatus.get();
        if (map == null) {
            map = new HashMap<String, Integer>();
            filtersStatus.set(map);
        }
        return map;
    }

    protected void setData(JahiaSite jSite, JahiaUser jUser) {
        site = jSite;
        siteID = jSite.getID();
        siteKey = jSite.getSiteKey();
        theUser = jUser;
    }

    private Boolean getCanEdit() {
        return canEdit;
    }

    private void setCanEdit(Boolean canEditFlag) {
        this.canEdit = canEditFlag;
    }

    protected boolean isContentPageLoadedWhileTryingToFindSiteByPageID() {
        return contentPageLoadedWhileTryingToFindSiteByPageID;
    }

    protected void setContentPageLoadedWhileTryingToFindSiteByPageID(
            boolean contentPageLoadedWhileTryingToFindSiteByPageIDFlag) {
        this.contentPageLoadedWhileTryingToFindSiteByPageID = contentPageLoadedWhileTryingToFindSiteByPageIDFlag;
    }

    protected boolean isSiteResolvedByKeyOrPageId() {
        return siteResolvedByKeyOrPageId;
    }

    protected void setSiteResolvedByKeyOrPageId(
            boolean siteResolvedByKeyOrPageIdFlag) {
        this.siteResolvedByKeyOrPageId = siteResolvedByKeyOrPageIdFlag;
    }

    protected boolean isSiteResolvedByServername() {
        return siteResolvedByServername;
    }

    protected void setSiteResolvedByServername(boolean siteResolvedByServername) {
        this.siteResolvedByServername = siteResolvedByServername;
    }

    public String getSiteURLForCurrentPageAndCurrentSite(final boolean withSessionID, final boolean withOperationMode,
                             boolean forceServerNameInURL) {

        return getSiteURL(getSite(), getPageID(), withSessionID, withOperationMode, null, forceServerNameInURL);
    }

    public boolean isInEditMode() {
        return getOperationMode().equals(ProcessingContext.EDIT);
    }

    public boolean isInPreviewMode() {
        return getOperationMode().equals(ProcessingContext.PREVIEW);
    }

    /**
     * Compose page URL for current page
     * @return the url of the current page
     * @throws JahiaException
     */
    public String composePageUrl() throws JahiaException {
        return composePageUrl(getPageID());
    }
}
