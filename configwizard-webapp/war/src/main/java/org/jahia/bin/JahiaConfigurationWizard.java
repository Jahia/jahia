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
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.bin;

import static javax.servlet.http.HttpServletResponse.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.struts.util.RequestUtils;
import org.jahia.admin.database.DatabaseConnection;
import org.jahia.admin.database.DatabaseScripts;
import org.jahia.data.constants.JahiaConstants;
import org.jahia.resourcebundle.JahiaResourceBundle;
import org.jahia.services.mail.MailSettings;
import org.jahia.services.mail.MailSettingsValidationResult;
import org.jahia.utils.FileUtils;
import org.jahia.utils.JahiaTools;
import org.jahia.utils.LanguageCodeConverters;
import org.jahia.utils.PathResolver;
import org.jahia.utils.ServletContainerUtils;
import org.jahia.utils.Version;
import org.jahia.utils.WebAppPathResolver;
import org.jahia.utils.properties.PropertiesManager;


/**
 * This servlet handles the main configuration task.
 * At the first launch of Jahia, the configuration wizard starts and provide
 * to you the capability to configure Jahia *as you want*. During the
 * configuration, you can set the administrator profile, change Jahia
 * default settings, activate the mail notification, and specify which
 * database you want to use with your Jahia portal. JahiaConfigurationWizard
 * runs only one time, at the first launch of Jahia. If you want to change
 * settings later, please refer to the JahiaAdministration.
 * <p/>
 * Copyright:    Copyright (c) 2002
 * Company:      Jahia Ltd
 *
 * @author Alexandre Kraft
 * @author Khue N'Guyen
 * @author Xavier Lawrence
 * @version 2.0
 */

public class JahiaConfigurationWizard extends HttpServlet {
    private  org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger(JahiaConfigurationWizard.class);
    /** ... */
    public final String COPYRIGHT =
            "&copy; Copyright 2002-2008  <a href=\"http://www.jahia.org\" target=\"newJahia\">Jahia Ltd</a> -";

    private  ServletContext context;
    private  ServletConfig config;
    private  PathResolver pathResolver;
    private final String INIT_PARAM_WEBINF_PATH = "webinf_path";
    private  PropertiesManager properties;

    private final DatabaseConnection db = new DatabaseConnection();
    private final DatabaseScripts scripts = new DatabaseScripts();
    public static final String SESSION_LOCALE = "org.jahia.services.multilang.currentlocale";

    static Locale selectedLocale = null;
    protected String jahiaEtcFilesPath;
    protected String jahiaVarFilesPath;

    String webinf_path;
    private  Map serverInfos;
    private  final Map values = new HashMap();

    public static String dbScriptsPath;
    private  String old_database;
    private  String createTables;
    /** properties filename */
    private final String PROPERTIES_FILENAME = "jahia.properties";

    /** license filename */
    private final String LICENSE_FILENAME = "license.xml";

    /** skeleton properties filename */
    private final String PROPERTIES_BASIC = "jahia.skeleton";

    // set default paths...
    private  String jahiaPropertiesPath;

    public  final String USERS_GROUPNAME = "users";
    public  final String ADMINISTRATORS_GROUPNAME = "administrators";
    public  final String GUEST_GROUPNAME = "guest";
    private static  String servletPath;
    private  Locale defaultLocale = Locale.ENGLISH;
    private  Locale newSelectedLocale;
    private final String CTX_PARAM_DEFAULT_SERVLET_PATH = "defaultJahiaServletPath";
    private  final String WIZARD_CONTEXT = "/configuration_wizard/";
    private  final String HTTP_FILES = "";
    protected  final String CLASS_NAME = "org.jahia.bin.JahiaConfigurationWizard";

    final String WIZARD_KEY_COOKIE = "jahiaWizardKey";
    private  final String INIT_PARAM_DEFAULT_LOCALE = "default_locale";

    // Total number of steps of the configuration wizard
    private static final String STEPS = "10";
    private  static String jahiafilename;
    private  String jahiaPropertiesFileName;

    private HypersonicManager hsql;
    private static int BUILD_NUMBER=-1;
    /** Jahia server release number */
    private static double RELEASE_NUMBER = 6.0;

    /** Jahia server patch number */
    private static int PATCH_NUMBER = 0;
    /**
     * Default init method, inherited fPlease point you borowser torom HttpServlet. This method get two
     * servlet container environment properties: its *home* filesystem path and
     * its type (tomcat, etc), and get the local context.
     *
     * @param config servlet configuration (inherited).
     * @throws ServletException a servlet exception occured during the process.
     */
    public void init(final ServletConfig config) throws ServletException {

        
        super.init(config);
        this.config = config;
        // get local context and config...
        context = config.getServletContext();

        WebAppPathResolver webPathResolver = new WebAppPathResolver();
        webPathResolver.setServletContext(context);
        pathResolver = webPathResolver;
        webinf_path = this.config.getInitParameter(INIT_PARAM_WEBINF_PATH);
        // get server informations...
        serverInfos = ServletContainerUtils.getServerInformations(config);

        hsql = new HypersonicManager();
        File hsqldir = new File(context.getRealPath("WEB-INF/dbdata"));
        hsqldir.mkdirs();
        hsql.startup(new File(hsqldir, "hsqldbjahia").getPath());

        jahiafilename = this.config.getInitParameter("jahia-filename");


        if(!jahiaExists()){

            // get xml init parameters...
            dbScriptsPath = context.getRealPath(config.getInitParameter("dbScriptsPath"));
            old_database = config.getInitParameter("old_database");
            createTables = config.getInitParameter("create_tables");
            String defaultLanguageCode = context.getInitParameter(INIT_PARAM_DEFAULT_LOCALE);
            if (defaultLanguageCode == null) {
                defaultLanguageCode = Locale.ENGLISH.toString();
            }
            defaultLocale = LanguageCodeConverters.languageCodeToLocale(defaultLanguageCode);
            setPaths();
            fillDefaultValues();
        }
    }


    private boolean jahiaExists(){

        //jahia is installed if the properties file exists 
        boolean exists = (new File((String)values.get("server_home")+"webapps/jahia/WEB-INF/etc/config/jahia.properties")).exists();
        if (exists) {

            logger.info("Jahia is already installed");
            return true;
        } else {
            logger.info("Jahia is not installed");
            return false;
        }
    }
    // end init( ServletConfig )


    /**
     * Default service method, inherited from HttpServlet. Jahia servlet must be
     * inited, because JahiaConfigurationWizard needs to access to some data
     * (like paths, constants, etc).
     *
     * @param request  servlet request (inherited).
     * @param response servlet response (inherited).
     * @throws IOException      an I/O exception occured during the process.
     * @throws ServletException a servlet exception occured during the process.
     */
    public void service(final HttpServletRequest request,
                        final HttpServletResponse response)
            throws IOException, ServletException {
        logger.debug("--[ " + request.getMethod() + " Request Start URI='" +
                request.getRequestURI() + "' query='" +
                request.getQueryString() + "'] --");
        boolean connect = false;

        if (servletPath == null) {
            servletPath = request.getServletPath();
        }

        // get *go* parameter...
        if (request.getParameter("go") != null) {
            if (request.getParameter("go").equals("connect")) {
                connect = true;
            }
        }

        dispatcher(request, response);
    }

    // end service( HttpServletRequest, HttpServletResponse )

    public static String getServletPath() {
        return servletPath;
    }

    /**
     * Static method to generate URLs for the administration. This should be
     * used by all JSPs and java code that is called by the administration.
     *
     * @param request          the current request object, used to generate the context
     *                         path part of the URL
     * @param response         the current response object, used to make a call to
     *                         encodeURL to generate session information in the case that cookies cannot
     *                         be used
     * @param doAction         a String representing the action we are linking to. This
     *                         is then encoded as a ?do=doAction string
     * @param extraQueryParams a string including any other parameters that will
     *                         be directly appended after the doAction string. This is done because this
     *                         way we offer the possibility to do an encodeURL over the whole string.
     *                         Note that this string may be null.
     * @return a String containing an URL with jsessionid generated and in the
     *         form : /contextPath/servletPath/?do=doActionextraQueryParams
     */
    public  String composeActionURL(final HttpServletRequest request,
                                    final HttpServletResponse response,
                                    final String doAction,
                                    final String extraQueryParams) {
        String internalDoAction = "";
        String internalQueryParams = "";

        if (doAction != null) {
            internalDoAction = "/?call=" + doAction;
        }

        if (extraQueryParams != null) {
            internalQueryParams = extraQueryParams;
        }

        return response.encodeURL(request.getContextPath() +
                getServletPath() +
                internalDoAction +
                internalQueryParams);
    }

    /**
     * Forward the servlet request and servlet response objects, using the request
     * dispatcher (from the ServletContext). Note: please be careful, use only
     * context relative path.
     *
     * @param request  servlet request.
     * @param response servlet response.
     * @param path  target, context-relative path.
     * @param jsp      name of the jsp File
     */
    private void doRedirect(final HttpServletRequest request,
                            final HttpServletResponse response,
                            final String path,
                            final String jsp) {
        logger.debug("Dispatching to target=[" + path + jsp + "]");
        try {
            // check null jsp bottom message, and fill in if necessary...
            if (request.getAttribute("copyright") == null) {
                request.setAttribute("copyright", COPYRIGHT);
            }

            // check null jahia display message, and fill in if necessary...
            if (request.getAttribute("focus") == null) {
                request.setAttribute("focus", "-none-");
            }

            // check null configuration step title, and fill in if necessary...
            if (request.getAttribute("title") == null) {
                request.setAttribute("title", "Jahia Configuration Wizard");
            }

            request.setAttribute("jsp", jsp);

            // set input values on request...
            request.setAttribute("values", values);

            // add http files path in request...
            request.setAttribute("url", request.getContextPath() + HTTP_FILES);

            // get browser entity...
            final Integer navigator;
            if (request.getHeader("user-agent") != null) {
                navigator = (request.getHeader("user-agent").indexOf("IE")) != -1 ? new Integer(0) : new Integer(1);
            } else {
                logger.info("Couldn't reader user-agent header ! Why ?");
                navigator = new Integer(0);
            }
            request.setAttribute("navigator", navigator);

            // set browser response content type
            String contentTypeStr = "text/html;charset=";
            final String acceptCharset = request.getHeader("accept-charset");
            boolean acceptsUTF8 = false;
            if (acceptCharset != null) {
                if (acceptCharset.toLowerCase().indexOf("utf-8") != -1) {
                    acceptsUTF8 = true;
                }
            }
            if (acceptsUTF8) {
                contentTypeStr = contentTypeStr + "UTF-8";
            } else {
                contentTypeStr = contentTypeStr + "ISO8859-1";
            }
            request.setAttribute("content-type", contentTypeStr);

            // redirect!
            context.getRequestDispatcher(path + jsp).forward(request, response);

        } catch (IOException ie) {
            logger.error("IOException in doRedirect", ie);
        } catch (ServletException se) {
            log("Servlet Exception in doRedirect",  se);
            logger.error("Servlet Exception in doRedirect root cause");
            se.printStackTrace();
        }
    }
    // end doRedirect( HttpServletRequest, HttpServletResponse, String )


    /**
     * This method is used like a dispatcher for user HTTP requests, like
     * GET and POST (it works also with DELETE, TRACE and PUT) requests.
     *
     * @param request  servlet request.
     * @param response servlet response.
     */
    private void dispatcher (final HttpServletRequest request,
                             final HttpServletResponse response) throws IOException, ServletException {
        // get the parameter *call*...
        final String call = request.getParameter("call") == null ? "welcome" :
                request.getParameter("call");

        if(call.equals("welcome")){

            displayWelcome(request,response);
            logger.info("redirecting the call to displayWelcome");
        } else if(call.equals("welcome_process")){

            try {
                processWelcome(request,response);
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (ServletException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        } else if(call.equals("chooseedition")){

            displayChooseEdition(request,response);

        } else if(call.equals("chooseedition_process")){

            processChooseEdition(request,response);

        } else if(call.equals("licenseagreement")){

            displayLicenseAgreement(request,response);

        } else if(call.equals("licenseagreement_process")){

            processLicenseAgreement(request,response);

        } else if(call.equals("root")){

            displayRoot(request,response);

        } else if(call.equals("root_process")){

            processRoot(request,response);

        } else if(call.equals("server")){

            displayServer(request,response);

        }
        if(call.equals("server_process")){

            processServer(request,response);

        } else if(call.equals("adv_settings")){

            displayAdvSettings(request,response);

        } else if(call.equals("adv_settings_process")){

            processAdvSettings(request,response);

        } else if(call.equals("mail")){

            displayMail(request,response);

        } else if(call.equals("mail_process")){

            processMail(request,response);

        } else if(call.equals("values")){

            displayValues(request,response);

        } else if(call.equals("values_process")){
            logger.info("processing values...");
            processValues(request,response);

        } else if(call.equals("mail")){

            displayMail(request,response);

        } else if(call.equals("mail_process")){

            processMail(request,response);

        } else if(call.equals("testEmail")){

            testEmail(request,response);

        } 

        // call the action handler...
        //actionHandler.call(this, call, request, response);

    }
    // end dispatcher( HttpServletRequest, HttpServletResponse )


    /**
     * This method display the welcome page.
     *
     * @param request  servlet request.
     * @param response servlet response.
     */
    public void displayWelcome(final HttpServletRequest request,
                               final HttpServletResponse response) {
        // get system properties...
        final Properties sys = System.getProperties();

        // set java infos on request...
        request.setAttribute("javaversion", sys.getProperty("java.version"));
        request.setAttribute("javavendor", sys.getProperty("java.vendor"));
        request.setAttribute("os", sys.getProperty("os.name"));
        request.setAttribute("osVersion", sys.getProperty("os.version"));
        request.setAttribute("osArch", sys.getProperty("os.arch"));

        request.setAttribute("step", "1");
        request.setAttribute("steps", STEPS);

        // set server infos on request...
        request.setAttribute("server", serverInfos.get("info"));

        // now let's test for the available resource bundles for the interface
        List<Locale> availableBundleLocales = LanguageCodeConverters
                .getAvailableBundleLocales(
                        JahiaResourceBundle.MESSAGE_DEFAULT_RESOURCE_BUNDLE,
                        defaultLocale);
        request.setAttribute("availableBundleLocales", availableBundleLocales);


        // now let's set the selected locale according to what we know about
        // locales so far...

        String selectedLanguageCode = request.getParameter("newLocale");
        if (selectedLanguageCode == null) {
            selectedLanguageCode = (String) values.get("welcome_newLocale");
        }
        if (selectedLanguageCode != null) {
            selectedLocale = LanguageCodeConverters.languageCodeToLocale(selectedLanguageCode);
        }
        if (selectedLocale == null) {
            if (request.getLocale() != null) {
                selectedLocale = request.getLocale();
            } else {
                selectedLocale = defaultLocale;
            }
        }

        if (!availableBundleLocales.contains(selectedLocale) && selectedLocale.getCountry() != null) {
            Locale langOnlyLocale = new Locale(selectedLocale.getLanguage());
            if (availableBundleLocales.contains(langOnlyLocale)) {
                selectedLocale = langOnlyLocale;
            }
        }
        if (!availableBundleLocales.contains(selectedLocale)) {
            selectedLocale = defaultLocale;
        }
        values.put("welcome_newLocale", selectedLocale.toString());
        request.getSession().setAttribute(SESSION_LOCALE, selectedLocale);

        // forward to the jsp...
        request.setAttribute("method", "welcome_process");
        doRedirect(request, response, WIZARD_CONTEXT, "welcome.jsp");
    }
    // end displayWelcome( HttpServletRequest, HttpServletResponse )

    /**
     * This method display the welcome page.
     *
     * @param request  servlet request.
     * @param response servlet response.
     */
    public void displayChooseEdition(final HttpServletRequest request,
                                     final HttpServletResponse response) {


        request.setAttribute("selectedJahiaEdition", values.get("selectedJahiaEdition"));

        request.setAttribute("step", "2");
        request.setAttribute("steps", STEPS);

        // forward to the jsp...
        request.setAttribute("method", "chooseedition_process");
        doRedirect(request, response, WIZARD_CONTEXT, "chooseedition.jsp");
    }

    /**
     * This method display the welcome page.
     *
     * @param request  servlet request.
     * @param response servlet response.
     */
    public void displayLicenseAgreement(final HttpServletRequest request,
                                        final HttpServletResponse response) {


        request.setAttribute("selectedJahiaEdition", values.get("selectedJahiaEdition"));


        if(request.getParameter("go").equals("next")&&(this.config.getInitParameter("jahia-edition")).equals("community")){
            request.setAttribute("step", "2");
        }
        else if(request.getParameter("go").equals("next")&&!(this.config.getInitParameter("jahia-edition")).equals("community")){
            request.setAttribute("step", "3");
        }
        request.setAttribute("steps", STEPS);

        // forward to the jsp...
        request.setAttribute("method", "licenseagreement_process");
        doRedirect(request, response, WIZARD_CONTEXT, "licenseagreement.jsp");
    }


    /**
     * This method display the root settings page.
     *
     * @param request  servlet request.
     * @param response servlet response.
     */
    public void displayRoot(final HttpServletRequest request,
                            final HttpServletResponse response) {
        // set focus if it's null...
        if (request.getAttribute("focus") == null) {
            request.setAttribute("focus", "pwd");
        }

        // set configuration step title...
        request.setAttribute("title", "SuperUser (administrator) settings");

        if(request.getParameter("go").equals("next")&&(this.config.getInitParameter("jahia-edition")).equals("community")||request.getParameter("go").equals("back")&&(this.config.getInitParameter("jahia-edition")).equals("community")){
            request.setAttribute("step", "3");
        }
        else if(request.getParameter("go").equals("next")&&!(this.config.getInitParameter("jahia-edition")).equals("community")){
            request.setAttribute("step", "4");
        }
        request.setAttribute("steps", STEPS);

        // forward to the jsp...
        request.setAttribute("method", "root_process");
        doRedirect(request, response, WIZARD_CONTEXT, "root.jsp");
    }
    // end displayRoot( HttpServletRequest, HttpServletResponse )

    /**
     * Process the welcome page language selection form. Then displays the root
     * user settings page.
     *
     * @param request  servlet request.
     * @param response servlet response.
     * @throws IOException      an I/O exception occured during the process.
     * @throws ServletException a servlet exception occured during the process.
     */
    public void processWelcome(final HttpServletRequest request,
                               final HttpServletResponse response)
            throws IOException, ServletException {
        // by default, the user makes at least one error :o)
        boolean error = true;
        String msg = null;

        // save form values...
        values.put("welcome_newLocale", request.getParameter("newLocale"));

        // set the chosen language in the session, so the rest of the configuration
        // wizard will have access to it as well as Jahia.
        if (request.getParameter("go").equals("next")) {
            String newLocaleStr = request.getParameter("newLocale");
            if (newLocaleStr != null) {
                Locale newLocale = LanguageCodeConverters.languageCodeToLocale(newLocaleStr);
                if (newLocale != null) {
                    request.getSession().setAttribute(SESSION_LOCALE, newLocale);
                    Locale.setDefault(newLocale);
                    newSelectedLocale = newLocale;
                    values.put("org.jahia.multilang.default_language_code", newSelectedLocale.toString());
                }
            }
            error = false;                                                  // everything is okay, continue...
        }
        // call the appropriate method...
        if (error) {
            request.setAttribute("msg", msg);
            displayWelcome(request, response);
        } else {
            if (request.getParameter("go").equals("next")&&!(this.config.getInitParameter("jahia-edition")).equals("community")) {               // step next requested...
                logger.info("We have a :"+this.config.getInitParameter("jahia-edition")+" license so displaying the edition ");
                displayChooseEdition(request, response);

            } else  if(request.getParameter("go").equals("next")&&(this.config.getInitParameter("jahia-edition")).equals("community")){
                logger.debug("We have a :"+this.config.getInitParameter("jahia-edition")+" license so displaying the licence");
                chooseFreeEdition();
                displayLicenseAgreement(request, response);
            }

            else if  (request.getParameter("go").equals("back")) {        // step back requested...
                displayWelcome(request, response);
            }
        }
    }

    /**
     * Process the choose license selection form. Then displays the license
     * agreement page.
     *
     * @param request  servlet request.
     * @param response servlet response.
     * @throws IOException      an I/O exception occured during the process.
     * @throws ServletException a servlet exception occured during the process.
     */
    public void processChooseEdition(final HttpServletRequest request,
                                     final HttpServletResponse response)
            throws IOException, ServletException {
        // by default, the user makes at least one error :o)
        boolean error = true;
        String msg = null;

        // save form values...
        String selectedEdition = request.getParameter("selectedJahiaEdition");

        values.put("selectedJahiaEdition", selectedEdition);
        if((this.config.getInitParameter("jahia-edition")).equals("community")){

            values.put("selectedJahiaEdition", "free");
        }
        // set the chosen language in the session, so the rest of the configuration
        // wizard will have access to it as well as Jahia.
        if (request.getParameter("go").equals("next")) {
            if ((selectedEdition == null) ||
                    ("".equals(selectedEdition))) {
                error = true;
                msg =  JahiaResourceBundle.getMessageResource("org.jahia.bin.JahiaConfigurationWizard.chooseedition.mustSelectEdition.label", newSelectedLocale);
            } else {
                error = false;                                                  // everything is okay, continue...
            }
        } else {
            error = false;                                                      // default, the user want to go back.
        }

        // call the appropriate method...
        if (error) {
            request.setAttribute("msg", msg);
            displayChooseEdition(request, response);
        } else {
            if (request.getParameter("go").equals("next")) {               // step next requested...
                displayLicenseAgreement(request, response);
            } else if (request.getParameter("go").equals("back")) {        // step back requested...
                displayWelcome(request, response);
            }
        }
    }

    public void chooseFreeEdition(){

        if((this.config.getInitParameter("jahia-edition")).equals("community")){

            values.put("selectedJahiaEdition", "free");
        }
    }

    /**
     * Process the choose license selection form. Then displays the license
     * agreement page.
     *
     * @param request  servlet request.
     * @param response servlet response.
     * @throws IOException      an I/O exception occured during the process.
     * @throws ServletException a servlet exception occured during the process.
     */
    public void processLicenseAgreement(final HttpServletRequest request,
                                        final HttpServletResponse response)
            throws IOException, ServletException {
        // by default, the user makes at least one error :o)
        boolean error = true;
        String msg = null;

        // save form values...
        String acceptLicense = request.getParameter("acceptLicense");
        String acceptRestrictiveSourceLicense = request.getParameter("acceptRestrictiveSourceLicense");
        //values.put("acceptOpenSourceLicense", acceptOpenSourceLicense);
        values.put("acceptLicense", acceptLicense);

        // set the chosen language in the session, so the rest of the configuration
        // wizard will have access to it as well as Jahia.
        if (request.getParameter("go").equals("next")) {
            if ((acceptLicense == null) ) {
                error = true;
                msg =  JahiaResourceBundle.getMessageResource("org.jahia.bin.JahiaConfigurationWizard.licenseagreement.mustAgree.label", newSelectedLocale);
                Integer failedLicenseCounter = (Integer) request.getSession().getAttribute("failedLicenseCounter");
                if (failedLicenseCounter == null) {
                    failedLicenseCounter = new Integer(1);
                }
                failedLicenseCounter = new Integer(failedLicenseCounter.intValue() + 1);
                request.getSession().setAttribute("failedLicenseCounter", failedLicenseCounter);
                if (failedLicenseCounter.intValue() > 10) {
                    msg = "<img src=\"http://www.jahia.org/failedlicense.png\"/>";
                }
            } else {
                error = false;                                                  // everything is okay, continue...
            }
        } else {
            error = false;                                                      // default, the user want to go back.
        }
        // call the appropriate method...
        if (error) {
            request.setAttribute("msg", msg);
            displayLicenseAgreement(request, response);
        } else {
            if (request.getParameter("go").equals("next")) {               // step next requested...
                displayRoot(request, response);
            }
            if (request.getParameter("go").equals("back")&&(this.config.getInitParameter("jahia-edition")).equals("community")) {
                request.setAttribute("step", "2");// step back requested...
                displayWelcome(request,response);
            }
            if(request.getParameter("go").equals("back")&&!(this.config.getInitParameter("jahia-edition")).equals("community")){
                displayChooseEdition(request, response);
            }
        }
    }




    /**
     * Process and check the validity of the root username, password, the
     * confirmation of the password, first name, last name and mail address
     * from the root settings page. If they are valid, display the server
     * settings page. Otherwise, re-display the root settings page to the user.
     *
     * @param request  servlet request.
     * @param response servlet response.
     * @throws IOException      an I/O exception occured during the process.
     * @throws ServletException a servlet exception occured during the process.
     */
    public void processRoot(final HttpServletRequest request,
                            final HttpServletResponse response)
            throws IOException, ServletException {
        // by default, the user makes at least one error :o)
        boolean error = true;
        String msg = null;

// save form values...
        values.put("root_user",  request.getParameter("user").trim());
        values.put("root_pwd",  request.getParameter("pwd").trim());
        values.put("root_confirm",  request.getParameter("confirm").trim());
        values.put("root_firstname",  request.getParameter("firstname").trim());
        values.put("root_lastname",  request.getParameter("lastname").trim());
        values.put("root_mail",  request.getParameter("mail").trim());

// check root settings validity... if the user want to go next
        if (request.getParameter("go").equals("next")) {
            if (((String) values.get("root_user")).length() < 4) {                                            // check username length [minimum 4 chars]
                request.setAttribute("focus", "user");
                msg = JahiaResourceBundle.getMessageResource("org.jahia.bin.JahiaConfigurationWizard.JahiaConfigurationMsg.usernameshort.label",
                        newSelectedLocale);
            } else if (!JahiaTools.isAlphaValid(((String) values.get("root_user")))) {                        // check username is alpha valid
                request.setAttribute("focus", "user");
                msg = JahiaResourceBundle.getMessageResource("org.jahia.bin.JahiaConfigurationWizard.JahiaConfigurationMsg.usernameinvalid.label",
                        newSelectedLocale);
            } else if (((String) values.get("root_pwd")).length() < 8) {                                      // check password length [minimum 8 chars]
                request.setAttribute("focus", "pwd");
                msg = JahiaResourceBundle.getMessageResource("org.jahia.bin.JahiaConfigurationWizard.JahiaConfigurationMsg.passwordshort.label",
                        newSelectedLocale);
            } else if (!JahiaTools.isAlphaValid(((String) values.get("root_pwd")))) {                         // check password is alpha valid
                request.setAttribute("focus", "pwd");
                msg = JahiaResourceBundle.getMessageResource("org.jahia.bin.JahiaConfigurationWizard.JahiaConfigurationMsg.passwordinvalid.label",
                        newSelectedLocale);
            } else if (((String) values.get("root_confirm")).length() == 0) {                                 // check confirmation length [minimum 8 chars]
                request.setAttribute("focus", "confirm");
                msg = JahiaResourceBundle.getMessageResource("org.jahia.bin.JahiaConfigurationWizard.JahiaConfigurationMsg.passwordmustsame.label",
                        newSelectedLocale);
            } else if (!( values.get("root_pwd")).equals(( values.get("root_confirm")))) {     // check password and confirmation must be equals
                request.setAttribute("focus", "pwd");
                msg = JahiaResourceBundle.getMessageResource("org.jahia.bin.JahiaConfigurationWizard.JahiaConfigurationMsg.passwordmustsame.label",
                        newSelectedLocale);
            }
            else {
                error = false;                                                  // everything is okay, continue...

                Cookie cookie = new Cookie(WIZARD_KEY_COOKIE, URLEncoder
                        .encode(new String(Base64.encodeBase64((values
                        .get("root_user")
                        + ":" + values.get("root_pwd"))
                        .getBytes("UTF-8")), "UTF-8"), "UTF-8"));
                cookie.setPath("/");
                response.addCookie(cookie);
            }
        } else {
            error = false;                                                      // default, the user want to go back.
        }

        // call the appropriate method...
        if (error) {
            request.setAttribute("msg", msg);
            displayRoot(request, response);
        } else {
            if (request.getParameter("go").equals("next")) {               // step next requested...
                displayServer(request, response);
            } else if (request.getParameter("go").equals("back")) {
                if ((this.config.getInitParameter("jahia-edition")).equals("community")) {
                    request.setAttribute("step", "2");// step back requested...
                }
                displayLicenseAgreement(request, response);
            }
        }
    }
// end processRoot( HttpServletRequest, HttpServletResponse )


    /**
     * This method display the server settings page.
     *
     * @param request  servlet request.
     * @param response servlet response..
     */
    public void displayServer(final HttpServletRequest request,
                              final HttpServletResponse response){
        // determine the host url... now i've the request :o)
        if (( values.get("server_url")) == null) {
            values.put("server_url", request.getScheme() + "://" + request.getHeader("host"));
        }

        if ((values.get("webapps_deploybaseurl")) == null) {
            URL deployURL = null;
            try {
                deployURL = new java.net.URL(request.getScheme() + "://" + request.getHeader("host") + "/manager");
            } catch (MalformedURLException mue) {
                logger.error("Error"+ mue);
            }
            if (deployURL != null) {
                values.put("webapps_deploybaseurl", deployURL.toString());
            }
        }

        // set focus it it's null...
        if (request.getAttribute("focus") == null) {
            request.setAttribute("focus", "home");
        }

        // set configuration step title...
        request.setAttribute("title", "Server settings");
        if((this.config.getInitParameter("jahia-edition")).equals("community")){
            request.setAttribute("step", "4");
        }
        else{
            request.setAttribute("step", "5");  }
        request.setAttribute("steps", STEPS);

// forward to the jsp...
        request.setAttribute("method", "server_process");
        doRedirect(request, response, WIZARD_CONTEXT, "server.jsp");
    }
// end displayServer( HttpServletRequest, HttpServletResponse )


    /**
     * Process and check the validity of the servlet container home disk path,
     * the host url and the path (filesystem or context-relative) to jahia
     * files, from the server settings page. If they are valid, display the
     * next page, but... the user can choose to display *advanced settings* or
     * to go back. Otherwise, re-display the server settings page to the user.
     *
     * @param request  servlet request.
     * @param response servlet response.
     * @throws IOException      an I/O exception occured during the process.
     * @throws ServletException a servlet exception occured during the process.
     */
    public void processServer(final HttpServletRequest request,
                              final HttpServletResponse response)
            throws IOException, ServletException {
        // by default, the user makes at least one error :o)
        boolean error = true;
        boolean dbError = false;
        String msg = null;
        String dbMsg = null;

// save form values...
        values.put("server_home", request.getParameter("home").trim());
        values.put("server_url", request.getParameter("hosturl").trim());
        values.put("webapps_deploybaseurl", request.getParameter("webappsdeploybaseurl").trim());
        values.put("server_jahiafiles", request.getParameter("jahiafiles").trim());

// check server settings validity...if the user want to go next
        if (request.getParameter("go").equals("next")) {
            if (((String) values.get("server_home")).length() == 0) {                      // check servlet container home path
                request.setAttribute("focus", "home");
                msg = JahiaResourceBundle.getMessageResource("org.jahia.bin.JahiaConfigurationWizard.JahiaConfigurationMsg.servletcontainer_homepath_mustset.lable",
                        newSelectedLocale);
            } else if (((String) values.get("server_url")).length() == 0) {                // check host url
                request.setAttribute("focus", "hosturl");
                msg = JahiaResourceBundle.getMessageResource("org.jahia.bin.JahiaConfigurationWizard.JahiaConfigurationMsg.hosturl_mustset.label",
                        newSelectedLocale);
            } else if (((String) values.get("webapps_deploybaseurl")).length() == 0) {                // check host url
                request.setAttribute("focus", "webappsdeploybaseurl");
                msg = JahiaResourceBundle.getMessageResource("org.jahia.bin.JahiaConfigurationWizard.JahiaConfigurationMsg.appdeploy_baseurl_mustset.label",
                        newSelectedLocale);
            } else if (((String) values.get("server_jahiafiles")).length() == 0) {         // check jahia files path
                request.setAttribute("focus", "jahiafiles");
                msg = JahiaResourceBundle.getMessageResource("org.jahia.bin.JahiaConfigurationWizard.JahiaConfigurationMsg.jahiafiles_path_mustset.label",
                        newSelectedLocale);
            } else {
                error = false;                                                             // everything is okay, continue...
            }

            // check *slashs* validity of the inputs values...
            checkServerSlashs();

// try some operations which can return customs exceptions...
            try {
                /**
                 * Deactivated because :
                 *  - it's specific to Tomcat
                 *  - doesn't work when Jahia is installed in ROOT context.
                 if (serverInfos.get("type").equals(JahiaConstants.SERVER_TOMCAT)) {
                 // try the host url connection...
                 tryHostURL();

                 // try the host url connection...
                 tryWebAppsDeployBaseURL();
                 }
                 */

                // try the path to jahia files (permissions, etc)...
                tryJahiaFilesPath();
            } catch (Exception e) {
                msg = e.getMessage();
                error = true;
            }

            // reset default values if the user has specified invalid database settings...
            if ((values.get("database_custom")) == Boolean.FALSE) {
                fillDefaultValues(false, true);
            }

            // test the database connection... (only if the user want continue by *next*)
            Map test = testDBConnection();
            dbError = ((Boolean) test.get("testDatabaseConnectionError")).booleanValue();
            dbMsg = (String) test.get("testDatabaseConnectionMessage");
            request.setAttribute("msg", dbMsg);

        } else {
            error = false;                                                                 // default, the user don't want to go next.
        }

        // call the appropriate method...
        if (error) {                                       // process generates errors...
            request.setAttribute("msg", msg);
            displayServer(request, response);
        } else if (dbError) {                              // database settings are not correct...
            request.setAttribute("msg", dbMsg);
            values.put("database_requested", Boolean.FALSE);
            displayAdvSettings(request, response);
        } else {
            if (request.getParameter("go").equals("next")) {               // step next requested...
                displayMail(request, response);
            } else if (request.getParameter("go").equals("back")) {        // step back requested...
                displayRoot(request, response);
            } else if (request.getParameter("go").equals("advanced")) {
                if((this.config.getInitParameter("jahia-edition")).equals("community")){
                    request.setAttribute("step", "4");
                    values.put("database_requested", Boolean.TRUE);
                }
                displayAdvSettings(request, response);
            }
        }
    }
// end processServer( HttpServletRequest, HttpServletResponse )


    /**
     * This method display the database (advanced) settings page.
     *
     * @param request  servlet request.
     * @param response servlet response.
     * @throws IOException      an I/O exception occured during the process.
     */
    public void displayAdvSettings(final HttpServletRequest request,
                                   final HttpServletResponse response)
            throws IOException {
        // get script lists...
        List scriptsInfos = scripts.getDatabaseScriptsInfos(scripts.getDatabaseScriptsFileObjects(), pathResolver);
        values.put("database_test", scripts);
        logger.info("displaying advanced settings");
// set java infos on request...
        request.setAttribute("scripts", scriptsInfos.iterator());
        request.setAttribute("jsscripts", scriptsInfos.iterator());

// set focus it it's null...
        if (request.getAttribute("focus") == null) {
            request.setAttribute("focus", "driver");
        }

        // set configuration step title...
        request.setAttribute("title", "Advanced settings");
        request.setAttribute("method", "adv_settings_process");
        logger.debug("here in the displayAdvanced settings"+(this.config.getInitParameter("jahia-edition")));
        if((this.config.getInitParameter("jahia-edition")).equals("community")){
            request.setAttribute("step", "4.1");
            logger.debug("here in the displayAdvanced settings inside the loop");
        }
        else{
            //request.setAttribute("step", "5.1");
        }
        request.setAttribute("steps", STEPS);

// forward to the jsp...
        doRedirect(request, response, WIZARD_CONTEXT, "adv_settings.jsp");
    }

    /**
     * Process and check the validity of the database driver, url, password and
     * username, from the database settings page. If they are valid, display the
     * mail page (if invoqued) or back to the server settings page. If the user
     * press *back* or *cancel*, don't process the verification of the data.
     *
     * @param request  Servlet request.
     * @param response Servlet response.
     */
    public void processAdvSettings(final HttpServletRequest request,
                                   final HttpServletResponse response)
            throws IOException, ServletException {

        if (request.getParameter("go").equals("back")) {
            displayServer(request, response);
            return;
        }

        // by default, the user makes at least one error :o)
        boolean error = true;
        boolean dbError = false;
        String msg = null;
        String dbMsg = null;

// save form values...
        values.put("database_script",  request.getParameter("script").trim());
        values.put("database_driver",  request.getParameter("driver").trim());
        values.put("database_url",  request.getParameter("dburl").trim());
        values.put("database_user",  request.getParameter("user").trim());
        values.put("database_pwd",  request.getParameter("pwd").trim());
        values.put("db_starthsqlserver",  request.getParameter("starthsqlserver").trim());
        values.put("datasource.name",  request.getParameter("datasource").trim());
        values.put("hibernate_dialect",  request.getParameter("hibernate_dialect").trim());
        values.put("storeFilesInDB",  request.getParameter("storeFilesInDB"));
        values.put("storeBigTextInDB",request.getParameter("storeBigTextInDB"));
        values.put("useExistingDb",request.getParameter("useExistingDb"));
        if(request.getParameter("storeFilesInDB")==null){
            values.put("storeFilesInDB","");
        }
        if(request.getParameter("storeBigTextInDB")==null){
            values.put("storeBigTextInDB",  "false");
        }
        if(request.getParameter("useExistingDb")==null){

            values.put("useExistingDb","false")  ;

        }
        logger.debug("we are in the useExistingDb value is  "+values.get("useExistingDb"));
        logger.debug(" storeFilesInDB value is "+values.get("storeFilesInDB")+ " and storeBigTextInDB is "+values.get("storeBigTextInDB"));

        String utf8Encoding = request.getParameter("utf8Encoding");
        if (utf8Encoding == null) {
            utf8Encoding = "false";
        } else {
            if ("true".equals(utf8Encoding)) {
                utf8Encoding = "true";
            }
        }
        values.put("utf8Encoding", utf8Encoding);

// check database settings validity...if the user want to go next (or apply)
        if (request.getParameter("go").equals("next")) {
            if (((String) values.get("database_driver")).length() == 0) {           // check database driver
                request.setAttribute("focus", "driver");
                msg = JahiaResourceBundle.getMessageResource("org.jahia.bin.JahiaConfigurationWizard.JahiaConfigurationMsg.dbdriver_mustset.label",
                        newSelectedLocale);
            } else if (((String) values.get("database_url")).length() == 0) {       // check database url
                request.setAttribute("focus", "dburl");
                msg = JahiaResourceBundle.getMessageResource("org.jahia.bin.JahiaConfigurationWizard.JahiaConfigurationMsg.dburl_mustset.label",
                        newSelectedLocale);
            } else {
                error = false;                                                      // everything is okay, continue...
            }

            // test the database connection...
            Map test = testDBConnection();
            dbError = ((Boolean) test.get("testDatabaseConnectionError")).booleanValue();
            dbMsg = (String) test.get("testDatabaseConnectionMessage");

        } else {
            error = false;                                                          // default, the user don't want to go next.
        }

        // call the appropriate method...
        if (error) {                                       // process generates errors...
            request.setAttribute("msg", msg);
            displayAdvSettings(request, response);
        } else if (dbError) {                              // database settings are not correct...
            request.setAttribute("msg", dbMsg);
            displayAdvSettings(request, response);
        } else {
            // fill with db other settings
            if (request.getParameter("go").equals("next")) {        // step next requested...
                values.put("database_custom", Boolean.TRUE);
                displayMail(request, response);
            }
        }
    }

    /**
     * This method display the mail settings page.
     *
     * @param request  servlet request.
     * @param response servlet response.
     */
    public void displayMail(final HttpServletRequest request,
                            final HttpServletResponse response) {
        // set focus it it's null...
        if (request.getAttribute("focus") == null) {
            request.setAttribute("focus", "host");
        }

        // set configuration step title...
        request.setAttribute("title", "Mail settings");
        logger.debug("here we are in the the display mail");
        if(request.getParameter("go").equals("next")&&(this.config.getInitParameter("jahia-edition")).equals("community")){
            request.setAttribute("step", "5");
            logger.debug("here we are in the the display mail 1");
        }
        else if(request.getParameter("go").equals("next")&&!(this.config.getInitParameter("jahia-edition")).equals("community")){
            request.setAttribute("step", "6");
            logger.debug("here we are in the the display mail 2");
        }
        request.setAttribute("steps", STEPS);

// forward to the jsp...
        request.setAttribute("method", "mail_process");
        doRedirect(request, response, WIZARD_CONTEXT, "mail.jsp");
    }
// end displayMail( HttpServletRequest, HttpServletResponse )


    /**
     * Process and check the validity of the database driver, url, password and
     * username, from the database settings page. If they are valid, display the
     * mail page (if invoqued) or back to the server settings page. If the user
     * press *back* or *cancel*, don't process the verification of the data.
     *
     * @param request  Servlet request.
     * @param response Servlet response.
     */
    public void processMail(final HttpServletRequest request,
                            final HttpServletResponse response)
            throws IOException, ServletException {

        boolean error = false;
        String msg = null;
        String focus = null;

// save form values...
        MailSettings cfg = new MailSettings();
        RequestUtils.populate(cfg, request);

        values.put("mail_server", cfg.getHost());
        values.put("mail_recipient", cfg.getTo());
        values.put("mail_from", cfg.getFrom());
        values.put("mail_parano", cfg.getNotificationLevel());

// check mail settings validity...if the user want to go next
        if (request.getParameter("go").equals("next")) {
            MailSettingsValidationResult result = MailSettings.validateSettings(cfg, true);
            if (!result.isSuccess()) {
                error = true;
                msg = JahiaResourceBundle.getString(ResourceBundle.getBundle(
                        JahiaResourceBundle.MESSAGE_DEFAULT_RESOURCE_BUNDLE,
                        newSelectedLocale), result.getMessageKey(), result
                        .getMessageKey());
                focus = result.getProperty();
            }
        }

        // call the appropriate method...
        if (error) {                                       // process generates errors...
            request.setAttribute("msg", msg);
            request.setAttribute("focus", focus);
            displayMail(request, response);
        } else {
            if (request.getParameter("go").equals("back")&&(this.config.getInitParameter("jahia-edition")).equals("community")) {               // step back requested...
                request.setAttribute("step", "4");
                displayAdvSettings(request, response);
                // step back when database is forced...

            } else if (request.getParameter("go").equals("next")) {        // step next requested...
                displayValues(request, response);
            }
        }
    }
// end processMail( HttpServletRequest, HttpServletResponse )


    /**
     * This method display the values confirmation (before save options) page.
     *
     * @param request  servlet request.
     * @param response servlet response.
     */
    public void displayValues(final HttpServletRequest request,
                              final HttpServletResponse response) {
        // set configuration step title...
        request.setAttribute("title", "Confirm your values");

        request.setAttribute("selectedLocale", newSelectedLocale.toString());

        request.setAttribute("focus", null);
        if(request.getParameter("go").equals("next")&&(this.config.getInitParameter("jahia-edition")).equals("community")||request.getParameter("go").equals("back")&&(this.config.getInitParameter("jahia-edition")).equals("community")){
            request.setAttribute("step", "6");
        }
        else{
            request.setAttribute("step", "7");}
        request.setAttribute("steps", STEPS);

// forward to the jsp...
        request.setAttribute("method", "values_process");
        doRedirect(request, response, WIZARD_CONTEXT, "values.jsp");
    }
// end displayValues( HttpServletRequest, HttpServletResponse )


    /**
     * Save the values from the Map in the database and properties file,
     * since the user has confirmed the values displayed in the page. Launch
     * some
     *  to compose the final jahia properties file, insert
     * database tables, data and remove old templates xml files if necessary.
     *
     * @param request  Servlet request.
     * @param response Servlet response.
     */
    public void processValues(final HttpServletRequest request,
                              final HttpServletResponse response)
            throws IOException, ServletException {

        if (request.getParameter("go").equals("back")&&(this.config.getInitParameter("jahia-edition")).equals("community")) {
            request.setAttribute("step", "5");
            displayMail(request, response);
            return;
        }
        // by default, the user makes at least one error :o)
        boolean error = true;
        String msg = null;

        final String defaultSite = (String)values.get("sitekey");
// try to execute the last operations... any error will stop the process
        try {
            logger.info("we are in the useExistingDb value is  "+values.get("useExistingDb"));
            // overwrite the database if the user has not specified the opposite...
            if (values.get("useExistingDb")==null || "false".equalsIgnoreCase((String)values.get("useExistingDb"))) {



                createDBTables();

                insertDBPopulationDirContent();
                insertDBCustomContent();

// and also the default site property...
                properties.setProperty("defautSite", defaultSite);
            }

            // close the connection with the database connection admin manager...
            db.databaseClose();

            hsql.stop();

            if  (values.get("database_url").equals("jdbc:hsqldb:hsql://localhost/hsqldbjahia")) {
                // default embedded hsql
                FileUtils.copyDirectory(new File(context.getRealPath("WEB-INF/dbdata")), new File(context.getRealPath("WEB-INF/jahia/WEB-INF/var/dbdata")));
            }


            // set new properties...
            setPropertiesObject();

// store jahia.properties file...
            properties.storeProperties(getJahiaPropertiesFileName());

            final StringBuffer script = new StringBuffer().append(dbScriptsPath);
            script.append(File.separator);
            script.append((String) values.get("database_script"));
            Properties dbProperties = new Properties();
            dbProperties.load(new FileInputStream(script.toString()));
            if (values.get("storeFilesInDB") != null) {
                dbProperties.put("storeFilesInDB", values.get("storeFilesInDB"));
            }
            String pathToSpringManagerFile = pathResolver.resolvePath("WEB-INF/jahia/WEB-INF/etc/spring/applicationcontext-manager.xml");
            SpringManagerConfigurator.updateDataSourceConfiguration(pathToSpringManagerFile, dbProperties);
            String pathToSpringHibernateFile = pathResolver.resolvePath("WEB-INF/jahia/WEB-INF/etc/spring/applicationcontext-hibernate.xml");
            SpringHibernateConfigurator.updateDataSourceConfiguration(pathToSpringHibernateFile, dbProperties);
            String pathToQuartzFile = pathResolver.resolvePath("WEB-INF/jahia/WEB-INF/etc/config/quartz.properties");
            QuartzConfigurator.updateDataSourceConfiguration(pathToQuartzFile, dbProperties);
            String pathToJRFile = pathResolver.resolvePath("WEB-INF/jahia/WEB-INF/etc/repository/jackrabbit/repository.xml");
            JackrabbitConfigurator.updateDataSourceConfiguration(pathToJRFile, dbProperties);
// Copy slide files to default site
            final ServletContext context = config.getServletContext();
//final PathResolver pathResolver = new WebAppPathResolver(context);

            final String slideContentDiskPath = "WEB-INF/jahia/WEB-INF/var/content/slide/";
            JahiaTools.copyFolderContent(
                    pathResolver.resolvePath(slideContentDiskPath + "myjahiasite" + File.separator + "store"),
                    pathResolver.resolvePath(slideContentDiskPath + properties.getProperty("defautSite") + File.separator + "store"));
            final File dir = new File(pathResolver.resolvePath(slideContentDiskPath + "myjahiasite"));
            JahiaTools.deleteFile(dir);

// now let's install the selected license file.
            String selectedLicenseFileName = "license-free.xml";
            String selectedJahiaEdition = (String) values.get("selectedJahiaEdition");
            if ("free".equals(selectedJahiaEdition)) {
                selectedLicenseFileName = "license-free.xml";
            } else if ("standard".equals(selectedJahiaEdition)) {
                selectedLicenseFileName = "license-standard.xml";
            } else if ("pro".equals(selectedJahiaEdition)) {
                selectedLicenseFileName = "license-pro.xml";
            }

            String pathToSourceLicenseFile = pathResolver.resolvePath("WEB-INF/jahia/WEB-INF/etc/config/licenses/" + selectedLicenseFileName);
            String pathToDestinationLicenseFile = pathResolver.resolvePath("WEB-INF/jahia/WEB-INF/etc/config/license.xml");
            FileUtils.copyFile(pathToSourceLicenseFile, pathToDestinationLicenseFile);

// okay, everthing is okay... set error to false
            error = false;

// call the appropriate method... We do this before the
// final step of updating the context descriptor, as this
// triggers re-deployment.
            if (error) {
                if(request.getParameter("go").equals("next")&&(this.config.getInitParameter("jahia-edition")).equals("community")){
                    request.setAttribute("step", "6");
                }
                else{// process generates errors...
                    request.setAttribute("step", "7");}
                request.setAttribute("steps", STEPS);
                request.setAttribute("errormsg", msg);
                displayError(request, response);
            } else {
                displayConfigFinished(request, response);          // everything is okay, continue...
            }

            // Update Tomcat specific context descritor that contains
            // database configuration. We do this last as it has for effect
            // of restarting the web application.
            String serverInfo = context.getServerInfo();
            if (serverInfo.indexOf(JahiaConstants.SERVER_TOMCAT) != -1) {

                // we assume in the following code that Tomcat follows the
                // servlet API convention for serverInfo format. If it does
                // not the code will fail.
                int separatorPos = serverInfo.indexOf("/");
                int parenPos = serverInfo.indexOf("(", separatorPos);
                String versionStr;
                if (parenPos != -1) {
                    versionStr = serverInfo.substring(separatorPos + 1, parenPos);

                } else {
                    versionStr = serverInfo.substring(separatorPos + 1);
                }
                versionStr = versionStr.trim();
                final Version currentTomcatVersion = new Version(versionStr);
                final Version tomcatVersion5 = new Version("5.5");
// update the XML context descriptor file configuration
                String jahiaXml = pathResolver.resolvePath(this.config.getInitParameter("jahia-contextfile"));
                String deployToDir = this.config.getInitParameter("dirName");
                new JetspeedDataSourceConfigurator()
                        .updateDataSourceConfiguration(jahiaXml+"/context.xml", jahiaXml,
                                values);
                logger.info("updating to context file in"+jahiaXml);

                //And now the big finale: copy the configured jahia
                File oldDir = new File(pathResolver.resolvePath("WEB-INF/jahia"));
                File newDir = new File((String) values.get("server_home")+"webapps/"+deployToDir);


                logger.info("unzipping jahia GWT folder from "+pathResolver.resolvePath("WEB-INF/jahia/jsp/jahia/gwt.zip")+ " to"+(String)values.get("server_home")+"webapps/"+deployToDir+"jsp/jahia/gwt" );
                FileUtils.unzipFile(pathResolver.resolvePath("WEB-INF/jahia/gwt.zip"),(String) values.get("server_home")+"webapps/"+deployToDir+"/gwt/" );
                logger.info("deleting  gwt zip file");
                FileUtils.deleteFile(pathResolver.resolvePath("WEB-INF/jahia/gwt.zip"));
                logger.info("copying "+ oldDir.getCanonicalFile()+ " to "+newDir+"" );
                FileUtils.copyDirectory(oldDir,newDir);

            }


        } catch (Exception e) {
            e.printStackTrace();
            msg = e.getMessage();
            if (msg.length() == 0) {
                msg = e.getClass().getName();
            }

            displayError(request, response);
        }
    }

    /**
     * This method display some errors during the save process.
     *
     * @param request  servlet request.
     * @param response servlet response.
     */
    public void displayError(final HttpServletRequest request,
                             final HttpServletResponse response) {
        // set configuration step title...
        request.setAttribute("title", "Error");

// forward to the jsp...

        request.setAttribute("method", "values");

        doRedirect(request, response, WIZARD_CONTEXT,  "error_save.jsp");
    }
// end displayError( HttpServletRequest, HttpServletResponse )


    /**
     * This method display the final step (congratulations).
     *
     * @param request  servlet request.
     * @param response servlet response.
     */
    public void displayConfigFinished(final HttpServletRequest request,
                                      final HttpServletResponse response) {
        // set configuration step title...
        request.setAttribute("title", "Congratulations!");
        if(request.getParameter("go").equals("next")&&(this.config.getInitParameter("jahia-edition")).equals("community")){
            request.setAttribute("step", "7");
        }
        else{
            request.setAttribute("step", "8");}
        request.setAttribute("steps", STEPS);
/*
final LicenseManager licenseManager = LicenseManager.getInstance();
String licenseFileName = getServletContext().getRealPath("WEB-INF/etc/config/license.xml");
try {
    licenseManager.load(licenseFileName);
} catch (Exception e) {
    logger.info("Error while trying to read license file to retrieve days left limit.");
}
final LicensePackage jahiaLicensePackage = licenseManager.
        getLicensePackage(LicenseConstants.JAHIA_PRODUCT_NAME);
// we take a component that has an expiration date here. Please modify this
// if licensing policy changes.
License manageSiteLicense = jahiaLicensePackage.getLicense("org.jahia.actions.server.admin.sites.ManageSites");
final Limit daysLeftLimit = manageSiteLicense.getLimit("maxUsageDays");
// the limit might be null if a license has been created without
// this limit.
if (daysLeftLimit != null) {
//            DaysLeftValidator daysLeftValidator = (DaysLeftValidator)
//                    daysLeftLimit.getValidator();
    final int maxDays = Integer.parseInt(daysLeftLimit.getValueStr());
    request.setAttribute("allowedDays", new Integer(maxDays));
    final EngineMessage allowedDaysMsg = new EngineMessage("org.jahia.bin.JahiaConfigurationWizard.congratulations.daysLeftInLicense.label", new Integer(maxDays));
    request.setAttribute("allowedDaysMsg", allowedDaysMsg);
}

String serverType = properties.getProperty("server");
if(serverType != null && serverType.equalsIgnoreCase("Tomcat")){
    request.setAttribute("isTomcat",Boolean.TRUE);
}else{
    request.setAttribute("isTomcat",Boolean.FALSE);
}
*/
// forward to the jsp...
        doRedirect(request, response, WIZARD_CONTEXT, "congratulations.jsp");
    }
// end displayFinalStep( HttpServletRequest, HttpServletResponse )



    /**
     * Check *slashs* validity of the inputs values from the server
     * settings page (servlet container home, host url, path jahia files).
     */
    private void checkServerSlashs() {
        if (!((String) values.get("server_home")).endsWith(File.separator)) {          // server home must ends with file separator
            values.put("server_home",
                    (values.get("server_home")) + File.separator);
        }
        if (((String) values.get("server_url")).endsWith("/")) {                       // host url must ends without "/"
            values.put("server_url",
                    ((String) values.get("server_url")).substring(0,
                            ((String) values.get("server_url")).length() - 1));
        }
        if (((String) values.get("server_jahiafiles")).endsWith(File.separator)) {     // jahia files must ends without file separator
            values.put("server_jahiafiles",
                    ((String) values.get("server_jahiafiles")).substring(0,
                            ((String) values.get("server_jahiafiles")).length() - 1));
        }
    }
// end checkServerSlashs()

    /**
     * Try to create the path to jahia files indicates by the user on the
     * server settings page, and check some error(s) (like permissions, etc).
     *
     * @throws Exception an exception occured during the process.
     */
    private void tryJahiaFilesPath()
            throws Exception {
        String jahiaFiles;

        try {
            // transform context-relative path in filesystem if needed...
            if (((String) values.get("server_jahiafiles")).substring(0, 9).equals("$context/")) {
                jahiaFiles = context.getRealPath(
                        ((String) values.get("server_jahiafiles")).substring(
                                8, ((String) values.get("server_jahiafiles")).length()));
            } else {
                throw new StringIndexOutOfBoundsException();
            }
        } catch (StringIndexOutOfBoundsException sioobe) {
            jahiaFiles = (String) values.get("server_jahiafiles");
        }

        // a filepath must contain at least one file separator...
        if (jahiaFiles.indexOf(File.separator) == -1) {
            throw new Exception("Jahia Files must be a valid filesystem or context-relative path.");
        }

// create the File object...
        final File jf = new File(jahiaFiles);
        File parent;

// check if the location exists... or not.
        if (jf.exists()) {
            if (jf.isFile()) {                                                             // check if it's a file ?!
                throw new Exception("Jahia Files must be a directory, and not a file.");
            }
            if (!jf.canRead()) {                                                           // check if i have permissions to read on it...
                throw new Exception("Can't read in Jahia Files directory. Please verify permissions.");
            }
            if (!jf.canWrite()) {                                                          // check if i have permissions to write on it...
                throw new Exception("Can't write in Jahia Files directory. Please verify permissions.");
            }

        } else {
            // get parent directory...
            parent = jf.getParentFile();

// check if the parent exists... or not.
            if (parent.exists()) {
                if (!parent.canWrite()) {                                                  // check if i have permissions to write on parent...
                    throw new Exception("Jahia can't create your directory (write permissions).");
                }
            } else {
                if (!parent.mkdirs()) {                                                    // the parent don't exists... create it if possible.
                    throw new Exception("Jahia can't create your directory (write permissions).");
                }
            }
        }
    }
// end tryJahiaFilesPath()


    /**
     * Read the database script requested by the user, get the test table line and
     * execute the self-test in the databaseconnection static object of jahia
     * administration.
     *
     * @return Map containing the results of the database test.
     */
    private Map testDBConnection() {
        String line = "";
// get script runtime...
// construct script path...
        File object;
        List sqlStatements;
        final StringBuffer script = new StringBuffer().append(dbScriptsPath);
        script.append(File.separator);
        script.append((String) values.get("database_script"));

// get script runtime...
        try {
            object = new File(script.toString());
            sqlStatements = scripts.getSchemaSQL(object);
            final Iterator sqlIter = sqlStatements.iterator();
// now let's find the jahia_db_test table statement
            while (sqlIter.hasNext()) {
                final String curStatement = (String) sqlIter.next();
                final String lowerCaseLine = curStatement.toLowerCase();
                String tableName;
                int tableNamePos = lowerCaseLine.indexOf("create table");
                if (tableNamePos != -1) {
                    tableName = curStatement.substring("create table".length() +
                            tableNamePos,
                            curStatement.indexOf("(")).trim();
                    if ("jahia_db_test".equalsIgnoreCase(tableName)) {
                        line = curStatement;
                        break;
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Jahia can't read the appropriate database script."+ e);
        }

        // test database...
        return db.databaseTest((String) values.get("database_script"), (String) values.get("database_driver"),
                (String) values.get("database_url"), (String) values.get("database_user"),
                (String) values.get("database_pwd"), line,
                "true".equals(values.get("utf8Encoding")), !"false".equals(createTables));
    }

    /**
     * Insert the database tables described in the database script. Before the
     * insertion, since you're sure that the user want overwrite his database,
     * each table is dropped, table after table.
     *
     * @throws Exception an exception occured during the process.
     */
    private void createDBTables() throws Exception {
        File object;
        List sqlStatements;
        String line;

        logger.info("Creating database tables...");

// construct script path...
        final StringBuffer script = new StringBuffer().append(dbScriptsPath);
        script.append(File.separator);
        script.append((String) values.get("database_script"));

// get script runtime...
        try {
            object = new File(script.toString());
            sqlStatements = scripts.getSchemaSQL(object);
        } catch (Exception e) {
            logger.error("Jahia can't read the appropriate database script."+ e);
            throw e;
        }

// drop each tables (if present) and (re-)create it after...
        final Iterator sqlIter = sqlStatements.iterator();
        while (sqlIter.hasNext()) {
            line = (String) sqlIter.next();
            final String lowerCaseLine = line.toLowerCase();
            final int tableNamePos = lowerCaseLine.indexOf("create table");
            if (tableNamePos != -1) {
                final String tableName = line.substring("create table".length() +
                        tableNamePos,
                        line.indexOf("(")).trim();
                logger.debug("Creating table [" + tableName + "] ...");
                try {
                    db.query("DROP TABLE " + tableName);
                } catch (Throwable t) {
                    // ignore because if this fails it's ok
                    logger.debug("Drop failed on " + tableName + " but that's acceptable...");
                }
            }
            try {
                db.query(line);
            } catch (Exception e) {
                // first let's check if it is a DROP TABLE query, if it is,
                // we will just fail silently.
                String upperCaseLine = line.toUpperCase().trim();
                if (!upperCaseLine.startsWith("DROP") && !upperCaseLine.startsWith("ALTER TABLE")
                        && !upperCaseLine.startsWith("CREATE INDEX") && !upperCaseLine.startsWith("DELETE FROM")) {
                    logger.debug("Error while trying to execute query : " + line + " from script " + script.toString()+ e);
// continue to propagate the exception upwards.
                    throw e;
                } else if(upperCaseLine.startsWith("CREATE INDEX")){
                    logger.debug("Error while trying to execute query : " + line+ e);
                }
            }
        }
    }
// end createDBTables()

    private void insertDBPopulationDirContent() throws Exception {

        File object;
        List sqlStatements;
        String line;

// construct script path...
        StringBuffer popScript = new StringBuffer(dbScriptsPath);
        popScript.append(File.separator);
        popScript.append((String) values.get("database_script"));

// get script runtime...
        try {
            object = new File(popScript.toString());
            sqlStatements = scripts.getPopulationSQL(object);
        } catch (Exception e) {
            logger.error("Jahia can't read the appropriate database script."+ e);
            throw e;
        }

        // drop each tables (if present) and (re-)create it after...
        Iterator sqlIter = sqlStatements.iterator();
        while (sqlIter.hasNext()) {
            line = (String) sqlIter.next();
            try {
                db.query(line);
            } catch (Exception e) {
                logger.error("Error while trying to execute query : " + line);
// continue to propagate the exception upwards.
                throw e;
            }
        }
    }
// end insertDBDefaultContent()

    /**
     * Insert database custom data, like root user and properties.
     *
     * @throws Exception an exception occured during the process.
     */
    private void insertDBCustomContent() throws Exception {


// get two keys...
        final String rootName = (String) values.get("root_user");
        final int siteID0 = 0;
        final String rootKey = rootName + ":" + siteID0;
        final String grpKey0 = ADMINISTRATORS_GROUPNAME + ":" + siteID0;

// query insert root user...
        db.queryPreparedStatement("INSERT INTO jahia_users(id_jahia_users, name_jahia_users, password_jahia_users, key_jahia_users) VALUES(0,?,?,?)",
                new Object[] { rootName, encryptPassword((String) values.get("root_pwd")), rootKey } );

// query insert root first name...
        db.queryPreparedStatement("INSERT INTO jahia_user_prop(id_jahia_users, name_jahia_user_prop, value_jahia_user_prop, provider_jahia_user_prop, userkey_jahia_user_prop) VALUES(0, 'firstname', ?, 'jahia',?)",
                new Object[] { (String) values.get("root_firstname"), rootKey } );

// query insert root last name...
        db.queryPreparedStatement("INSERT INTO jahia_user_prop(id_jahia_users, name_jahia_user_prop, value_jahia_user_prop, provider_jahia_user_prop, userkey_jahia_user_prop) VALUES(0, 'lastname', ?, 'jahia',?)",
                new Object[] { (String) values.get("root_lastname"), rootKey } );

// query insert root e-mail address...
        db.queryPreparedStatement("INSERT INTO jahia_user_prop(id_jahia_users, name_jahia_user_prop, value_jahia_user_prop, provider_jahia_user_prop, userkey_jahia_user_prop) VALUES(0, 'email', ?, 'jahia',?)",
                new Object[] { (String) values.get("root_mail"), rootKey } );

// query insert administrators group...
        db.queryPreparedStatement("INSERT INTO jahia_grps(id_jahia_grps, name_jahia_grps, key_jahia_grps, siteid_jahia_grps) VALUES(?,?,?,null)",
                new Object[] { new Integer(siteID0), ADMINISTRATORS_GROUPNAME, grpKey0 } );

// query insert administrators group access...
        db.queryPreparedStatement("INSERT INTO jahia_grp_access(id_jahia_member, id_jahia_grps, membertype_grp_access) VALUES(?,?,1)",
                new Object[] { rootKey,grpKey0 } );

// create guest user
        db.queryPreparedStatement("INSERT INTO jahia_users(id_jahia_users, name_jahia_users, password_jahia_users, key_jahia_users) VALUES(1,?,?,?)",
                new Object[] {GUEST_GROUPNAME, "*", "guest:0" } );

        db.queryPreparedStatement("INSERT INTO jahia_version(install_number, build, release_number, install_date) VALUES(0, ?,?,?)",
                new Object[] { new Integer("12346789"), "132456" + "." + "123456", new Timestamp(System.currentTimeMillis()) } );


    }
    // end insertDBCustomContent()
    /**
     * Set the properties object, using data containing in the *values* hashmap,
     * using the jahia's propertiesmanager object.
     */
    private void setPropertiesObject() {
        String appsService;
        logger.info("In  setPropertiesObject");
// use appropriate webapps deployer service depending the servlet container type..
        if (((String) serverInfos.get("type")).indexOf("JBoss") != -1) {
            appsService = "org.jahia.services.webapps_deployer.JahiaJBossWebAppsDeployerBaseService";
        } else if (((String) serverInfos.get("type")).indexOf("Tomcat") != -1) {
            appsService = "org.jahia.services.webapps_deployer.JahiaTomcatWebAppsDeployerBaseService";
        } else {
            appsService = "org.jahia.services.webapps_deployer.GenericWebAppsDeployerBaseService";
        }

// find release number...
        final String release = Integer.toString(JahiaTools.getBuildNumber(pathResolver.resolvePath("META-INF/MANIFEST.MF"))) ;//"221123" + "." + "1321322";
// set new values in the PropertiesManager...
        properties.setProperty("release", release);
        properties.setProperty("server", (String) serverInfos.get("type"));
        properties.setProperty("serverHomeDiskPath", (String) values.get("server_home"));
        properties.setProperty("jahiaFilesDiskPath", (String) values.get("server_jahiafiles"));
        properties.setProperty("jahiaEtcDiskPath", values.get("server_jahiafiles") + "/etc/");
        properties.setProperty("jahiaVarDiskPath", values.get("server_jahiafiles") + "/var/");
        properties.setProperty("jahiaFilesBigTextDiskPath", values.get("server_jahiafiles") + "/var/content/bigtext/");
        properties.setProperty("slideContentDiskPath", values.get("server_jahiafiles") + "/var/content/slide/");
        properties.setProperty("tmpContentDiskPath", values.get("server_jahiafiles") + "/var/content/tmp/");
        properties.setProperty("jahiaFilesTemplatesDiskPath", values.get("server_jahiafiles") + "/var/templates/");
        properties.setProperty("jahiaNewTemplatesDiskPath", values.get("server_jahiafiles") + "/var/new_templates/");
        properties.setProperty("jahiaNewWebAppsDiskPath", values.get("server_jahiafiles") + "/var/new_webapps/");
        properties.setProperty("jahiaImportsDiskPath", values.get("server_jahiafiles") + "/var/imports/");
        properties.setProperty("jahiaSharedTemplatesDiskPath", values.get("server_jahiafiles") + "/var/shared_templates/");
        properties.setProperty("jahiaFileRepositoryDiskPath", values.get("server_jahiafiles") + "/var/content/filemanager/");
// properties.setProperty("jahiaHostHttpPath",             (String)values.get("server_url") );
        properties.setProperty("jahiaHostHttpPath", "");
        properties.setProperty("jahiaTemplatesHttpPath", "$webContext"
                + properties.getProperty("jahiaJspDiskPath") + "templates/");
        properties.setProperty("jahiaEnginesHttpPath", "$webContext"
                + properties.getProperty("jahiaJspDiskPath") + "engines/");
        properties.setProperty("jahiaJavaScriptHttpPath", "$webContext"
                + properties.getProperty("jahiaJspDiskPath")
                + "javascript/jahia.js");
        properties.setProperty("mail_server", (String) values.get("mail_server"));
        properties.setProperty("mail_administrator", (String) values.get("mail_recipient"));
        properties.setProperty("mail_from", (String) values.get("mail_from"));
        properties.setProperty("mail_paranoia", (String) values.get("mail_parano"));
        if (properties.getProperty("mail_server").length() > 0) {
            properties.setProperty("mail_service_activated", "true");
        }

        properties.setProperty("db_script", (String) values.get("database_script"));
        if( !((String)values.get("database_script")).equals("hypersonic.script")){
            properties.setProperty("db_starthsqlserver", "false");
        }
        String utf8Encoding = (String) values.get("utf8Encoding");
        properties.setProperty("utf8Encoding", utf8Encoding);
        if ("true".equalsIgnoreCase(utf8Encoding)) {
            properties.setProperty("defaultResponseBodyEncoding", "UTF-8");
        } else {
            properties.setProperty("defaultResponseBodyEncoding", "ISO-8859-1");
        }
        try {
            properties.setProperty("localIp", InetAddress.getLocalHost().getHostAddress());
        } catch (UnknownHostException e) {
        }
        String storeBigTextInDB = (String) values.get("storeBigTextInDB");
        if ((storeBigTextInDB == null) || "false".equalsIgnoreCase(storeBigTextInDB)) {
            properties.setProperty("bigtext.service", "FileJahiaText");
        } else {
            properties.setProperty("bigtext.service", "DBJahiaText");
        }
        if (appsService.length() != 0) {
            properties.setProperty("JahiaWebAppsDeployerService", appsService);
            properties.setProperty("jahiaWebAppsDeployerBaseURL", (String) values.get("webapps_deploybaseurl"));
        }
        properties.setProperty("org.jahia.multilang.default_language_code", (String) values.get("org.jahia.multilang.default_language_code"));
        logger.debug("Finished setPropertiesObject");
    } // setPropertiesObject


    /**
     * Get *skeleton* init parameter and read the skeleton files values to
     * fill in the default hashmap values with the results.
     */
    private void fillDefaultValues() {
        fillDefaultValues(true, true);
    }
// end fillDefaultValues();


    /**
     * Get *skeleton* init parameter and read the skeleton files values to
     * fill in the default hashmap values with the results.
     *
     * @param all      redefine database default values.
     * @param database redefine database default values.
     */
    private void fillDefaultValues(final boolean all, final boolean database) {
        // get init parameter and read jahia skeleton properties...
        logger.debug("here is the jahia.skeleton path"+pathResolver.resolvePath(config.getInitParameter("skeleton"))); ;

        properties = new PropertiesManager(pathResolver.resolvePath(config.getInitParameter("skeleton")));
// root settings ...
        if (all) {
            values.put("root_user", "root");
            values.put("root_pwd", "");
            values.put("root_confirm", "");
            values.put("root_firstname", "Jahia");
            values.put("root_lastname", "Super Administrator");
            values.put("root_mail", "");

// server settings...
            values.put("server_home", serverInfos.get("home"));
            values.put("server_url", null);
            values.put("server_jahiafiles", properties.getProperty("jahiaFilesDiskPath").trim());
            values.put("server_jahiafiles_default", properties.getProperty("jahiaFilesDiskPath").trim());

            values.put("sitekey", "mySite");
            values.put("sitetitle", "My Site");
            values.put("siteservername", "localhost");

            values.put("adminUsername", "siteAdmin");
            values.put("adminFirstName", "Default Virtual Site");
            values.put("adminLastName", "Site Admin");
            values.put("adminEmail", "siteadmin@localhost");
            values.put("adminOrganization", "");
            values.put("adminPassword", "");
            values.put("adminConfirm", "");
        }

        // database settings...
        if (database || all) {
            values.put("database_script", properties.getProperty("db_script").trim());
            if (((String) serverInfos.get("type")).indexOf("JBoss") != -1) {
                values.put("database_script", "jboss.script");
            }
            values.put("utf8Encoding", properties.getProperty("utf8Encoding").trim());
            values.put("database_custom", Boolean.FALSE);
            values.put("database_requested", Boolean.FALSE);
            try {
                // let's try to copy the values from the script that's set as
                // the default.
                List scriptsInfos = scripts.getDatabaseScriptsInfos(scripts.
                        getDatabaseScriptsFileObjects(), pathResolver);
                Iterator scriptInfoEnum = scriptsInfos.iterator();
                while (scriptInfoEnum.hasNext()) {
                    Map curDatabaseHash = (Map) scriptInfoEnum.next();
                    String scriptFileName = (String) curDatabaseHash.get("jahia.database.script");
                    if (scriptFileName.equals(values.get("database_script"))) {
                        logger.debug("Found script " + scriptFileName + ", copying default values");
                        values.put("database_driver", ((String) curDatabaseHash.get("jahia.database.driver")).trim());
                        values.put("database_url", ((String) curDatabaseHash.get("jahia.database.url")).trim());
                        values.put("database_user", ((String) curDatabaseHash.get("jahia.database.user")).trim());
                        values.put("database_pwd", ((String) curDatabaseHash.get("jahia.database.pass")).trim());
                        values.put("database_transactions", ((String) curDatabaseHash.get("jahia.database.transactions")).trim());
                        copyStringSetting(curDatabaseHash, "jahia.database.datasource", values, "datasource.name", "");
                        copyStringSetting(curDatabaseHash, "jahia.database.starthsqlserver", values, "db_starthsqlserver", "false");
                        copyStringSetting(curDatabaseHash, "jahia.database.hibernate.dialect", values, "hibernate_dialect", "");
                    }
                }
            } catch (IOException ioe) {
                logger.error("Error while loading default values from database scripts"+ ioe);
            }
        }

        // mail settings...
        if (all) {
            values.put("mail_server", "");
            values.put("mail_recipient", "");
            values.put("mail_from", "");
            values.put("mail_parano", "");

// default templates set
            values.put("templates", properties.getProperty("default_templates_set").trim());
        }
        
    }

    private void copyStringSetting(final Map sourceMap, final String sourceName, final Map destMap,
                                   final String destName, final String defaultValue) {
        final String settingValue = (String) sourceMap.get(sourceName);
        if (settingValue != null) {
            destMap.put(destName, settingValue.trim());
        } else {
            destMap.put(destName, defaultValue);
        }
    }

    public static String getSteps(){
        return STEPS;
    }


    String getDefaultServletPath(ServletContext ctx) {
        String path = ctx.getInitParameter(CTX_PARAM_DEFAULT_SERVLET_PATH);
        if (null == path) {
            // should we alternatively default it to '/Jahia'?
            throw new RuntimeException("Missing required context-param '"
                    + CTX_PARAM_DEFAULT_SERVLET_PATH + "'"
                    + " in the web.xml. Initialization failed.");
        }
        return path;
    }
    public  String getJahiaPropertiesFileName () {
        return jahiaPropertiesFileName;
    }

    public static Locale getLocale(){
        return selectedLocale;
    }
    public String encryptPassword (String password) {
        if (password == null) {
            return null;
        }

        if (password.length () == 0) {
            return null;
        }

        String result = null;

        try {
            MessageDigest md = MessageDigest.getInstance ("SHA-1");
            if (md != null) {
                md.reset ();
                md.update (password.getBytes ());
                result = new String (Base64.encodeBase64 (md.digest ()));
            }
            md = null;
        } catch (NoSuchAlgorithmException ex) {

            result = null;
        }

        return result;
    }

    public void setPaths(){


        // set default paths...
        jahiaPropertiesPath = context.getRealPath(webinf_path +
                "/etc/config/");
        jahiaPropertiesFileName = jahiaPropertiesPath + File.separator +
                PROPERTIES_FILENAME;



    }

    private void testEmail(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        try {
            String host = getParameter(request, "host");
            String from = getParameter(request, "from");
            String to = getParameter(request, "to");

            Locale locale = (Locale) request.getSession(true).getAttribute(SESSION_LOCALE);
            locale = locale != null ? locale : request.getLocale();
            
            ResourceBundle bundle = ResourceBundle.getBundle(JahiaResourceBundle.MESSAGE_DEFAULT_RESOURCE_BUNDLE, locale);

            response.setContentType("text/html;charset=UTF-8");
            if (!MailSettings.isValidEmailAddress(to, true)) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response
                        .getWriter()
                        .append(
                                JahiaResourceBundle
                                        .getString(bundle, 
                                                "org.jahia.admin.JahiaDisplayMessage.enterValidEmailAdmin.label",
                                                "Please provide a valid administrator e-mail address"));
                return;
            }
            if (!MailSettings.isValidEmailAddress(from, false)) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response
                        .getWriter()
                        .append(
                                JahiaResourceBundle
                                        .getString(bundle,
                                                "org.jahia.admin.JahiaDisplayMessage.enterValidEmailFrom.label",
                                                "Please provide a valid sender e-mail address"));
                return;
            }

            sendEmail(
                    host,
                    from,
                    to,
                    JahiaResourceBundle
                            .getString(
                                    bundle,
                                    "org.jahia.admin.server.ManageServer.testSettings.mailSubject",
                                    "[Jahia] Test message"),
                    JahiaResourceBundle
                            .getString(
                                    bundle,
                                    "org.jahia.admin.server.ManageServer.testSettings.mailText",
                                    "Test message"));

            response.setStatus(HttpServletResponse.SC_OK);

        } catch (IllegalArgumentException iae) {
            response.sendError(SC_BAD_REQUEST, iae.getMessage());
        } catch (Exception e) {
            response.setStatus(SC_INTERNAL_SERVER_ERROR);
            response.getWriter().append(e.getMessage());
            if (e.getCause() != null) {
                response.getWriter().append("\n").append(e.getCause().getMessage());
            }
            logger.warn("Error sending test e-mail message. Cause: "
                    + e.getMessage(), e);
        }
    }
    
    private static void sendEmail(String host, String from, String to,
            String subject, String text) throws AddressException,
            MessagingException {
        MailSettings cfg = new MailSettings(false, host, from, to, "Disabled");
        Properties props = new Properties();
        props.putAll(cfg.getOptions());
        Session session = Session.getInstance(props);
        MimeMessage msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress(from));
        String[] recipients = StringUtils.split(to, ",");
        for (String rcp : recipients) {
            msg.addRecipient(Message.RecipientType.TO, new InternetAddress(rcp
                    .trim()));
        }
        msg.setSubject(subject);
        msg.setText(text);
        int port = cfg.getPort();
        Transport transport = session.getTransport("smtp");
        transport.connect(cfg.getSmtpHost(), port > 0 ? port : -1, cfg
                .getUser(), cfg.getPassword());
        try {
            msg.saveChanges();
            transport.sendMessage(msg, msg.getAllRecipients());
        } finally {
            transport.close();
        }
    }

    private static String getParameter(final HttpServletRequest request,
            final String name) throws IllegalArgumentException {
        final String value = request.getParameter(name);
        if (value == null) {
            throw new IllegalArgumentException("Missing required '" + name
                    + "' parameter in request.");
        }
        return value;
    }
}