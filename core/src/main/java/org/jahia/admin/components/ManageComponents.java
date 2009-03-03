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

package org.jahia.admin.components;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.jahia.bin.Jahia;
import org.jahia.bin.JahiaAdministration;
import org.jahia.data.JahiaData;
import org.jahia.data.applications.ApplicationBean;
import org.jahia.data.webapps.JahiaWebAppsPackage;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.utils.i18n.JahiaResourceBundle;
import org.jahia.security.license.License;
import org.jahia.services.acl.JahiaBaseACL;
import org.jahia.services.applications.ApplicationsManagerService;
import org.jahia.services.applications.ServletContextManager;
import org.jahia.services.shares.AppsShareService;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.sites.JahiaSitesService;
import org.jahia.services.usermanager.JahiaGroupManagerService;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.webapps_deployer.JahiaWebAppsDeployerService;
import org.jahia.utils.JahiaTools;
import org.jahia.admin.AbstractAdministrationModule;


/**
 * desc:  This class is used by the administration to manage
 * all the components you've added to your Jahia portal. You can add a
 * component, edit, change the visibility of the component and edit
 * its options. You can also view non-installed components.
 *
 * Copyright:    Copyright (c) 2002
 * Company:      Jahia Ltd
 *
 * @author Khue N'Guyen
 * @author Alexandre Kraft
 * @version 1.0
 */
public class ManageComponents extends AbstractAdministrationModule {

    /** logging */
    private static org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger (ManageComponents.class);

    private static final String CLASS_NAME = JahiaAdministration.CLASS_NAME;
    private static final String JSP_PATH = JahiaAdministration.JSP_PATH;

    private JahiaSite site;
    private JahiaUser user;
    private ServicesRegistry sReg;

    private License coreLicense;

    /**
     * Default constructor.
     *
     * @param   request       Servlet request.
     * @param   response      Servlet response.
     */
    public void service(HttpServletRequest request,
                             HttpServletResponse response)
            throws Exception {

        JahiaData jData = (JahiaData)request.getAttribute ("org.jahia.data.JahiaData");
        ProcessingContext jParams = null;
        if (jData != null) {
            jParams = jData.getProcessingContext ();
        }
        coreLicense = Jahia.getCoreLicense ();
        if (coreLicense == null) {
            // set request attributes...
            String dspMsg = JahiaResourceBundle.getJahiaInternalResource ("org.jahia.admin.JahiaDisplayMessage.invalidLicense.label",
                    jParams.getLocale());
            request.setAttribute ("jahiaDisplayMessage", dspMsg);
            // redirect...
            JahiaAdministration.doRedirect (request, response, request.getSession(), JSP_PATH + "menu.jsp");
            return;
        }

        userRequestDispatcher (request, response, request.getSession());
    } // end constructor



    //-------------------------------------------------------------------------
    /**
     * This method is used like a dispatcher for user requests.
     *
     * @param   request       Servlet request.
     * @param   response      Servlet response.
     * @param   session       Servlet session for the current user.
     */
    private void userRequestDispatcher (HttpServletRequest request,
                                        HttpServletResponse response,
                                        HttpSession session)
            throws Exception {
        String operation = request.getParameter ("sub");

        sReg = ServicesRegistry.getInstance ();

        // check if the user has really admin access to this site...
        user = (JahiaUser)session.getAttribute (ProcessingContext.SESSION_USER);
        site = (JahiaSite)session.getAttribute (ProcessingContext.SESSION_SITE);
        JahiaData jData = (JahiaData)request.getAttribute ("org.jahia.data.JahiaData");
        ProcessingContext jParams = null;
        if (jData != null) {
            jParams = jData.getProcessingContext ();
        }

        if (site != null && user != null && sReg != null) {

            // set the new site id to administrate...
            request.setAttribute ("site", site);

            if (operation.equals ("display")) {
                displayComponentList (request, response, session);
            } else if (operation.equals ("displaynewlist")) {
                displayNewComponentList (request, response, session);
            } else if (operation.equals ("details")) {
                displayNewComponentDetail (request, response, session);
            } else if (operation.equals ("visibility")) {
                confirmComponentVisibilityChange (request, response, session);
            } else if (operation.equals ("savevisibility")) {
                saveComponentVisibility (request, response, session);
            } else if (operation.equals ("edit")) {
                editComponent (request, response, session);
            } else if (operation.equals ("add")) {
                addComponent (request, response, session);
            } else if (operation.equals ("share")) {
                shareComponent (request, response, session);
            } else if (operation.equals ("options")) {
                editComponentOption (request, response, session);
            }

        } else {
            String dspMsg = JahiaResourceBundle.getJahiaInternalResource ("org.jahia.admin.JahiaDisplayMessage.requestProcessingError.label",
                    jParams.getLocale());
            request.setAttribute ("jahiaDisplayMessage", dspMsg);
            JahiaAdministration.doRedirect (request,
                    response,
                    session, JSP_PATH + "menu.jsp");
        }
    } // userRequestDispatcher



    //-------------------------------------------------------------------------
    /**
     * Display the list of components.
     *
     * @param   request         Servlet request.
     * @param   response        Servlet response.
     * @param   session         HttpSession object.
     */
    private void displayComponentList(HttpServletRequest request, HttpServletResponse response, HttpSession session)
            throws IOException, ServletException {
        JahiaData jData = (JahiaData) request.getAttribute("org.jahia.data.JahiaData");
        ProcessingContext jParams = null;
        if (jData != null) {
            jParams = jData.getProcessingContext();
        }
        try {

            ApplicationsManagerService appPersServ = sReg.getApplicationsManagerService();

            if (appPersServ == null) {
                throw new JahiaException("Unavailable Services", "Unavailable Services", JahiaException.SERVICE_ERROR,
                                         JahiaException.ERROR_SEVERITY);
            }

            // all apps
            List<ApplicationBean> appList = appPersServ.getApplications();

            // list of authorized apps
            List<ApplicationBean> authAppList = new ArrayList<ApplicationBean>();
            boolean showAllComponents = false;
            if(request.getAttribute("showAllComponents")!=null)  {
                showAllComponents = true;
            }
            int size = appList.size();
            ApplicationBean app = null;
            for (int i = 0; i < size; i++) {
                app = appList.get(i);
                if (showAllComponents ||
                    app.getACL().getPermission(null, null, jParams.getUser(), JahiaBaseACL.WRITE_RIGHTS,
                                               false, jParams.getSiteID()))
                {
                    authAppList.add(app);
                }
            }

            request.setAttribute("appsList", authAppList.iterator());

            JahiaAdministration.doRedirect(request, response, session, JSP_PATH + "manage_components.jsp");

        } catch (JahiaException je) {
            String dspMsg = JahiaResourceBundle.getJahiaInternalResource("org.jahia.admin.JahiaDisplayMessage.requestProcessingError.label",
                                                                 jParams.getLocale());
            request.setAttribute("jahiaDisplayMessage", dspMsg);
            JahiaAdministration.doRedirect(request, response, session, JSP_PATH + "menu.jsp");
        }


    } // end displayComponentList



    //-------------------------------------------------------------------------
    /**
     * Display the list of new components.
     *
     * @param   request         Servlet request.
     * @param   response        Servlet response.
     * @param   session         HttpSession object.
     */
    private void displayNewComponentList (HttpServletRequest request,
                                          HttpServletResponse response,
                                          HttpSession session)
            throws IOException, ServletException {

        JahiaData jData = (JahiaData)request.getAttribute ("org.jahia.data.JahiaData");
        ProcessingContext jParams = null;
        if (jData != null) {
            jParams = jData.getProcessingContext ();
        }
        try {
            JahiaWebAppsDeployerService appDepServ = sReg.getJahiaWebAppsDeployerService ();

            if (appDepServ == null) {
                throw new JahiaException ("Unavailable Services",
                        "Unavailable Services",
                        JahiaException.SERVICE_ERROR,
                        JahiaException.ERROR_SEVERITY);
            }

// get the list of new web apps
//            Iterator newWebAppsKeys = appDepServ.getWebAppsPackageKeys (site.getSiteKey ());

            Iterator<JahiaWebAppsPackage> enumeration = appDepServ.getWebAppsPackages (site.getSiteKey ());
            List<JahiaWebAppsPackage> vec = new ArrayList<JahiaWebAppsPackage>();
            JahiaWebAppsPackage aPackage = null;

            while (enumeration.hasNext ()) {
                aPackage = (JahiaWebAppsPackage)enumeration.next ();
                if (aPackage != null) {
                    vec.add (aPackage);
                } else {
//System.out.println("displayNewComponentList packages is null");
                }
            }

            request.setAttribute ("packagesList", vec.iterator());

            JahiaAdministration.doRedirect (request,
                    response,
                    session,
                    JSP_PATH + "new_components.jsp");
        } catch (JahiaException je) {
            String dspMsg = JahiaResourceBundle.getJahiaInternalResource ("org.jahia.admin.JahiaDisplayMessage.requestProcessingError.label",
                    jParams.getLocale());
            request.setAttribute ("jahiaDisplayMessage", dspMsg);
            JahiaAdministration.doRedirect (request,
                    response,
                    session,
                    JSP_PATH + "menu.jsp");
        }

    }


    //-------------------------------------------------------------------------
    /**
     * Display informations about a new component
     *
     * @param   request         Servlet request.
     * @param   response        Servlet response.
     * @param   session         HttpSession object.
     */
    private void displayNewComponentDetail (HttpServletRequest request,
                                            HttpServletResponse response,
                                            HttpSession session)
            throws IOException, ServletException {

        JahiaData jData = (JahiaData)request.getAttribute ("org.jahia.data.JahiaData");
        ProcessingContext jParams = null;
        if (jData != null) {
            jParams = jData.getProcessingContext ();
        }

        try {
            JahiaWebAppsDeployerService appDepServ = sReg.getJahiaWebAppsDeployerService ();

            if (appDepServ == null) {
                throw new JahiaException ("Unavailable Services",
                        "Unavailable Services",
                        JahiaException.SERVICE_ERROR,
                        JahiaException.ERROR_SEVERITY);
            }

            // get the new component
            String packageName = request.getParameter ("package_name");
            JahiaWebAppsPackage aPackage = (JahiaWebAppsPackage)appDepServ
                    .getWebAppsPackage (site.getSiteKey ()
                    + "_"
                    + packageName);

            String subAction = request.getParameter ("subaction");

            if (subAction == null) {
                request.setAttribute ("canDeploy", Boolean.valueOf(appDepServ.canDeploy ()));
                request.setAttribute ("aPackage", aPackage);
                JahiaAdministration.doRedirect (request,
                        response,
                        session,
                        JSP_PATH + "new_component_detail.jsp");
            } else if (subAction.equals ("deploy")) {

                try {
                    if (appDepServ.deploy (
                            aPackage.getContextRoot (),
                            aPackage.getFilePath ())) {

                        displayNewComponentList (request, response, session);

                    } else {
                        String dspMsg = JahiaResourceBundle.getJahiaInternalResource ("org.jahia.admin.JahiaDisplayMessage.deployingPackageError.label",
                                jParams.getLocale());
                        session.setAttribute (CLASS_NAME + "jahiaDisplayMessage", dspMsg);
                        request.setAttribute ("aPackage", aPackage);
                        JahiaAdministration.doRedirect (request,
                                response,
                                session,
                                JSP_PATH + "new_component_detail.jsp");
                    }
                } catch (JahiaException je) {
                    request.setAttribute ("aPackage", aPackage);
                    String dspMsg = JahiaResourceBundle.getJahiaInternalResource ("org.jahia.admin.JahiaDisplayMessage.deployingPackageError.label",
                            jParams.getLocale());
                    session.setAttribute (CLASS_NAME + "jahiaDisplayMessage", dspMsg);
                    JahiaAdministration.doRedirect (request,
                            response,
                            session,
                            JSP_PATH + "new_component_detail.jsp");
                }
            } else if (subAction.equals ("delete")) {

                try {
                    if (appDepServ.deletePackage (aPackage.getFilePath ())) {
                        displayNewComponentList (request, response, session);
                    } else {
                        request.setAttribute ("aPackage", aPackage);
                        String dspMsg = JahiaResourceBundle.getJahiaInternalResource ("org.jahia.admin.JahiaDisplayMessage.deletingPackageError.label",
                                jParams.getLocale());
                        session.setAttribute (CLASS_NAME + "jahiaDisplayMessage", dspMsg);
                        JahiaAdministration.doRedirect (request,
                                response,
                                session,
                                JSP_PATH + "new_component_detail.jsp");
                    }
                } catch (IOException ioe) {
                    String dspMsg = JahiaResourceBundle.getJahiaInternalResource ("org.jahia.admin.JahiaDisplayMessage.deletingPackageError.label",
                            jParams.getLocale());
                    session.setAttribute (CLASS_NAME + "jahiaDisplayMessage", dspMsg);
                    JahiaAdministration.doRedirect (request,
                            response,
                            session,
                            JSP_PATH + "new_component_detail.jsp");
                }
            }

        } catch (JahiaException je) {
            String dspMsg = JahiaResourceBundle.getJahiaInternalResource ("org.jahia.admin.JahiaDisplayMessage.requestProcessingError.label",
                    jParams.getLocale());
            request.setAttribute ("jahiaDisplayMessage", dspMsg);
            JahiaAdministration.doRedirect (request,
                    response,
                    session,
                    JSP_PATH + "menu.jsp");
        }

    }


    //-------------------------------------------------------------------------
    /**
     * Display confirmation form for visibility change.
     *
     * @param   request         Servlet request.
     * @param   response        Servlet response.
     * @param   session         HttpSession object.
     */
    private void confirmComponentVisibilityChange (HttpServletRequest request,
                                                   HttpServletResponse response,
                                                   HttpSession session)
            throws IOException, ServletException {

        JahiaData jData = (JahiaData)request.getAttribute ("org.jahia.data.JahiaData");
        ProcessingContext jParams = null;
        if (jData != null) {
            jParams = jData.getProcessingContext ();
        }
        try {

            ApplicationsManagerService appManServ = sReg.getApplicationsManagerService ();

            if (appManServ == null) {
                throw new JahiaException ("Unavailable Services",
                        "Unavailable Services",
                        JahiaException.SERVICE_ERROR,
                        JahiaException.ERROR_SEVERITY);
            }

// get form values...
            String[] ids = request.getParameterValues ("visible_status");

            List<ApplicationBean> webApps = appManServ.getApplications ();
            if (webApps == null) {
                webApps = new ArrayList<ApplicationBean>();
            }
            Iterator<ApplicationBean> enumeration = webApps.iterator();

            if (enumeration != null) {
                ApplicationBean app = null;
                List<ApplicationBean> apps = new ArrayList<ApplicationBean>();	// List of apps to change the visibility

                while (enumeration.hasNext ()) {
                    app = enumeration.next ();
                    if ((app.getVisibleStatus () == 1)
                            && !(JahiaTools.inValues (String.valueOf (app.getID ()), ids))) {
                        apps.add (app);
                    } else if ((app.getVisibleStatus () == 0)
                            && (JahiaTools.inValues (String.valueOf (app.getID ()), ids))) {
                        apps.add (app);
                    }
                }

                if (apps.size () > 0) {
                    request.setAttribute ("appsList", apps.iterator());
                    JahiaAdministration.doRedirect (request,
                            response,
                            session,
                            JSP_PATH + "comps_confirmvisibilitychange.jsp");
                } else {
                    request.setAttribute ("appsList", webApps.iterator());
                    JahiaAdministration.doRedirect (request,
                            response,
                            session,
                            JSP_PATH + "manage_components.jsp");
                }
            } else {
                request.setAttribute ("appsList", enumeration);
                JahiaAdministration.doRedirect (request,
                        response,
                        session,
                        JSP_PATH + "manage_components.jsp");
            }

        } catch (JahiaException je) {
            String dspMsg = JahiaResourceBundle.getJahiaInternalResource ("org.jahia.admin.JahiaDisplayMessage.requestProcessingError.label",
                    jParams.getLocale());
            request.setAttribute ("jahiaDisplayMessage", dspMsg);
            JahiaAdministration.doRedirect (request,
                    response,
                    session,
                    JSP_PATH + "menu.jsp");
        }

    } // end displayComponentList


    //-------------------------------------------------------------------------
    /**
     * Save components visibility.
     *
     * @param   request         Servlet request.
     * @param   response        Servlet response.
     * @param   session         HttpSession object.
     */
    private void saveComponentVisibility (HttpServletRequest request,
                                          HttpServletResponse response,
                                          HttpSession session)
            throws IOException, ServletException {

        JahiaData jData = (JahiaData)request.getAttribute ("org.jahia.data.JahiaData");
        ProcessingContext jParams = null;
        if (jData != null) {
            jParams = jData.getProcessingContext ();
        }
        try {

            ApplicationsManagerService appManServ = sReg.getApplicationsManagerService ();

            if (appManServ == null) {
                throw new JahiaException ("Unavailable Services",
                        "Unavailable Services",
                        JahiaException.SERVICE_ERROR,
                        JahiaException.ERROR_SEVERITY);
            }

// get form values...
            String[] ids = request.getParameterValues ("visible_status");

// save change
            int id = 0;
            int visStatus = 0;
            ApplicationBean app = null;
            for (int i = 0; i < ids.length; i++) {
                id = Integer.parseInt (ids[i]);
                app = appManServ.getApplication (id);
                if (app != null) {
                    visStatus = app.getVisibleStatus ();
                    if (visStatus == 0) {
                        app.setVisible (1);
                    } else {
                        app.setVisible (0);
                    }

                    appManServ.saveDefinition (app);
                }
            }

            List<ApplicationBean> webApps = appManServ.getApplications ();
            if (webApps == null) {
                webApps = new ArrayList<ApplicationBean>();
            }
            Iterator<ApplicationBean> enumeration = webApps.iterator();
            if (enumeration != null) {
                request.setAttribute ("appsList", enumeration);
            }

            JahiaAdministration.doRedirect (request,
                    response,
                    session,
                    JSP_PATH + "manage_components.jsp");

        } catch (JahiaException je) {
            String dspMsg = JahiaResourceBundle.getJahiaInternalResource ("org.jahia.admin.JahiaDisplayMessage.requestProcessingError.label",
                    jParams.getLocale());
            request.setAttribute ("jahiaDisplayMessage", dspMsg);
            JahiaAdministration.doRedirect (request,
                    response,
                    session,
                    JSP_PATH + "menu.jsp");
        }

    }


    //-------------------------------------------------------------------------
    /**
     * Display the edit form for a single component.
     *
     * @param   request         Servlet request.
     * @param   response        Servlet response.
     * @param   session         HttpSession object.
     */
    private void editComponent (HttpServletRequest request,
                                HttpServletResponse response,
                                HttpSession session)
            throws IOException, ServletException {

        JahiaData jData = (JahiaData)request.getAttribute ("org.jahia.data.JahiaData");
        ProcessingContext jParams = null;
        if (jData != null) {
            jParams = jData.getProcessingContext ();
        }
        try {

            JahiaSitesService sitesServ = sReg.getJahiaSitesService ();
            ApplicationsManagerService appManServ = sReg.getApplicationsManagerService ();
            AppsShareService appShareServ = sReg.getAppsShareService ();
            JahiaWebAppsDeployerService appDepServ = sReg.getJahiaWebAppsDeployerService ();
            ApplicationsManagerService appPersServ =
                    sReg.getApplicationsManagerService ();

            if (appManServ == null
                    || sitesServ == null
                    || appShareServ == null
                    || appDepServ == null
                    || appPersServ == null) {
                throw new JahiaException ("Unavailable Services",
                        "Unavailable Services",
                        JahiaException.SERVICE_ERROR,
                        JahiaException.ERROR_SEVERITY);
            }

// get paramater
            String strVal = request.getParameter ("appid");
            if (strVal == null) {
                String dspMsg = JahiaResourceBundle.getJahiaInternalResource ("org.jahia.admin.errMsg.applicaionNotFound.label",
                        jParams.getLocale());
                request.setAttribute ("errMsg", dspMsg);
                displayComponentList (request, response, session);
                return;
            }
            int id = Integer.parseInt (strVal);
            ApplicationBean app = appManServ.getApplication (id);

            if (app == null) {
                String dspMsg = JahiaResourceBundle.getJahiaInternalResource ("org.jahia.admin.errMsg.applicaionNotFound.label",
                        jParams.getLocale());
                request.setAttribute ("errMsg", dspMsg);
                displayComponentList (request, response, session);
                return;
            }

            String subAction = request.getParameter ("subaction");
            if (subAction == null || (subAction.length () <= 0)) {

                // get the list of authorized sites for this application
                Iterator<Integer> authSitesID = null;

                authSitesID = appShareServ.getSites (app);
                List<JahiaSite> authSites = new ArrayList<JahiaSite>();
                Integer siteID = null;
                while (authSitesID.hasNext ()) {
                    siteID = authSitesID.next ();
                    JahiaSite aSite = sitesServ.getSite (siteID.intValue ());
                    authSites.add (aSite);
                }

                request.setAttribute ("authSites", authSites.iterator());
                request.setAttribute ("nbShare", new Integer (authSites.size ()));

                request.setAttribute ("appItem", app);
                JahiaAdministration.doRedirect (request,
                        response,
                        session,
                        JSP_PATH + "component_edit.jsp");
            } else if (subAction.equals ("delete")) {
                String undeploy = request.getParameter ("undeploy");

                if(app.getType().equalsIgnoreCase("portlet")) {
                    /** todo add portlet removal code here */
                }
                // undeploy only apps not in the same context as Jahia !!!!
                if (undeploy != null
                        && (!app.getContext ().equalsIgnoreCase (request.getContextPath ()))) {
                    appDepServ.undeploy (app);
                }
// delete group associated with this application
                appManServ.deleteApplicationGroups (app);

                // delete the app definition
                appPersServ.removeApplication (app.getID ());

                ServletContextManager.getInstance().removeContextFromCache(app.getContext());

                displayComponentList (request, response, session);

            } else if (subAction.equals ("confirmdelete")) {
                request.setAttribute ("currAction", "confirmdelete");
                request.setAttribute ("appItem", app);
                JahiaAdministration.doRedirect (request,
                        response,
                        session,
                        JSP_PATH + "component_edit.jsp");
            } else if (subAction.equals ("save")) {

                String appName = request.getParameter ("appName");

                String visible_status = request.getParameter ("visible_status");
                if (appName != null && (appName.trim ().length () > 0)) {
                    app.setName (appName);
                }
                if (visible_status != null) {
                    app.setVisible (1);
                } else {
                    app.setVisible (0);
                }

                String appDescr = request.getParameter ("appDescr");
                app.setdesc (appDescr);

                if (appManServ.saveDefinition (app)) {
                    String dspMsg = JahiaResourceBundle.getJahiaInternalResource ("org.jahia.admin.JahiaDisplayMessage.applicationUpdated.label",
                            jParams.getLocale());
                    session.setAttribute (CLASS_NAME + "jahiaDisplayMessage", dspMsg);
                } else {
                    String dspMsg = JahiaResourceBundle.getJahiaInternalResource ("org.jahia.admin.JahiaDisplayMessage.applicationNotUpdated.label",
                            jParams.getLocale());
                    session.setAttribute (CLASS_NAME + "jahiaDisplayMessage", dspMsg);
                }

                app = appManServ.getApplication (id);
                request.setAttribute ("appItem", app);
                JahiaAdministration.doRedirect (request,
                        response,
                        session,
                        JSP_PATH + "component_edit.jsp");
            }

        } catch (JahiaException je) {
            String dspMsg = JahiaResourceBundle.getJahiaInternalResource ("org.jahia.admin.JahiaDisplayMessage.requestProcessingError.label",
                    jParams.getLocale());
            request.setAttribute ("jahiaDisplayMessage", dspMsg);
            JahiaAdministration.doRedirect (request,
                    response,
                    session,
                    JSP_PATH + "menu.jsp");
        }

    }


    //-------------------------------------------------------------------------
    /**
     * Handle Edit Components options
     *
     * @param   request         Servlet request.
     * @param   response        Servlet response.
     * @param   session         HttpSession object.
     */
    private void editComponentOption (HttpServletRequest request,
                                      HttpServletResponse response,
                                      HttpSession session)
            throws IOException, ServletException {

        JahiaData jData = (JahiaData)request.getAttribute ("org.jahia.data.JahiaData");
        ProcessingContext jParams = null;
        if (jData != null) {
            jParams = jData.getProcessingContext ();
        }
        try {

            JahiaSitesService sitesServ = sReg.getJahiaSitesService ();

            if (sitesServ == null) {
                throw new JahiaException ("Unavailable Services",
                        "Unavailable Services",
                        JahiaException.SERVICE_ERROR,
                        JahiaException.ERROR_SEVERITY);
            }

            int autoDeploy = 0;

            if (site.getWebAppsAutoDeployMode ()) {
                autoDeploy = 1;
            }

            request.setAttribute ("autoDeploy", new Integer (autoDeploy));

            // get paramater
            String subAction = request.getParameter ("subaction");
            if (subAction != null && subAction.equals ("save")) {

                String strVal = request.getParameter ("autoDeploy");
                int intVal = 0;

                if (strVal != null) {
                    intVal = 1;
                }

                if (intVal != autoDeploy) {

                    try {
                        site.setWebAppsAutoDeployMode (intVal == 1);
                        sitesServ.updateSite (site);
                        session.setAttribute (ProcessingContext.SESSION_SITE, site);
                        String dspMsg = JahiaResourceBundle.getJahiaInternalResource ("org.jahia.admin.JahiaDisplayMessage.changeUpdated.label",
                                jParams.getLocale());
                        session.setAttribute (CLASS_NAME + "jahiaDisplayMessage", dspMsg);

                    } catch (JahiaException je) {
                        String dspMsg = JahiaResourceBundle.getJahiaInternalResource ("org.jahia.admin.JahiaDisplayMessage.changeNotUpdated.label",
                                jParams.getLocale());
                        session.setAttribute (CLASS_NAME + "jahiaDisplayMessage", dspMsg);
                    }

                    request.setAttribute ("autoDeploy", new Integer (intVal));
                }
            }

            JahiaAdministration.doRedirect (request,
                    response,
                    session,
                    JSP_PATH + "component_option.jsp");
        } catch (JahiaException je) {
            String dspMsg = JahiaResourceBundle.getJahiaInternalResource ("org.jahia.admin.JahiaDisplayMessage.requestProcessingError.label",
                    jParams.getLocale());
            request.setAttribute ("jahiaDisplayMessage", dspMsg);
            JahiaAdministration.doRedirect (request,
                    response,
                    session,
                    JSP_PATH + "menu.jsp");
        }


    }


    //-------------------------------------------------------------------------
    /**
     * Handle all the process of manually add a new component.
     *
     * @param   request         Servlet request.
     * @param   response        Servlet response.
     * @param   session         HttpSession object.
     */
    private void addComponent (HttpServletRequest request,
                               HttpServletResponse response,
                               HttpSession session)

            throws IOException, ServletException {

        JahiaData jData = (JahiaData)request.getAttribute ("org.jahia.data.JahiaData");
        ProcessingContext jParams = null;
        if (jData != null) {
            jParams = jData.getProcessingContext ();
        }
        try {

            ApplicationsManagerService appManServ = sReg.getApplicationsManagerService ();
            JahiaWebAppsDeployerService appDepServ = sReg.getJahiaWebAppsDeployerService ();

            if (appManServ == null
                    || appDepServ == null) {
                throw new JahiaException ("Unavailable Services",
                        "Unavailable Services",
                        JahiaException.SERVICE_ERROR,
                        JahiaException.ERROR_SEVERITY);
            }

            request.setAttribute ("warningMsg", "");

            String subAction = request.getParameter ("subaction");
            String appPath = request.getParameter ("appPath");
            String appName = request.getParameter ("appName");
            String appDescr = request.getParameter ("appDescr");

            if (subAction == null || (subAction.length () <= 0)) {
                request.setAttribute ("appPath", "");
            } else if (subAction.equals ("scanDir")) {
                appPath = request.getParameter ("appPath");
                request.setAttribute ("appPath", appPath);
                JahiaWebAppsPackage pack = appDepServ.loadWebAppInfo (appPath);
                if (pack != null) {

// check if this application has been already registered in Jahia
                    ApplicationBean app = appManServ.getApplication ("/" + pack.getContextRoot ());
                    if (app != null) {
                        String dspMsg = JahiaResourceBundle.getJahiaInternalResource ("org.jahia.admin.warningMsg.componentAlreadyRegistered.label",
                                jParams.getLocale());
                        request.setAttribute ("warningMsg", dspMsg);
                        request.setAttribute ("appName", "");
                        request.setAttribute ("appDescr", "");
                    } else {
                        if (appName == null) {
                            appName = pack.getContextRoot ();
                        }
                        if (appDescr == null) {
                            appDescr = "";
                        }
                        request.setAttribute ("appName", appName);
                        request.setAttribute ("appDescr", appDescr);
                        request.setAttribute ("aPackage", pack);
                    }
                } else {
                    request.setAttribute ("appPath", appPath);
                    String dspMsg = JahiaResourceBundle.getJahiaInternalResource ("org.jahia.admin.warningMsg.noComponentInfoFound.label",
                            jParams.getLocale());
                    request.setAttribute ("warningMsg", dspMsg);
                }

            } else if (subAction.equals ("deploy")) {
                request.setAttribute ("appPath", appPath);
                request.setAttribute ("appName", appName);
                request.setAttribute ("appDescr", appDescr);

                JahiaWebAppsPackage pack = appDepServ.loadWebAppInfo (appPath);

                if (pack != null) {
                    if (!pack.isDirectory ()) {
                        if (appDepServ.deploy (pack.getContextRoot (), appPath)) {
// update with value from form
                            ApplicationBean app = appManServ.getApplication ("/" + pack.getContextRoot ());
                            if (app != null) {
                                if (appName != null && appName.trim ().length () > 0) {
                                    app.setName (appName);
                                }
                                app.setdesc (appDescr);
                                appManServ.saveDefinition (app);
                            }
                            String dspMsg = JahiaResourceBundle.getJahiaInternalResource ("org.jahia.admin.warningMsg.componentRegistered.label",
                                    jParams.getLocale());
                            request.setAttribute ("warningMsg", dspMsg);
                        } else {
                            String dspMsg = JahiaResourceBundle.getJahiaInternalResource ("org.jahia.admin.warningMsg.componentNotRegistered.label",
                                    jParams.getLocale());
                            request.setAttribute ("warningMsg", dspMsg);
                            request.setAttribute ("aPackage", pack);
                        }
                    } else {
                        // Try to deploy if its'a portlet
                        appDepServ.deploy (pack.getContextRoot (), appPath);
                        appDepServ.registerWebApps (
                                pack.getContextRoot (),
                                appPath,
                                pack.getWebApps ());

// update with value from form
                        ApplicationBean app = appManServ.getApplication ("/" + pack.getContextRoot ());
                        if (app != null) {
                            if (appName != null && appName.trim ().length () > 0) {
                                app.setName (appName);
                            }
                            app.setdesc (appDescr);
                            appManServ.saveDefinition (app);
                        }

                        String dspMsg = JahiaResourceBundle.getJahiaInternalResource ("org.jahia.admin.warningMsg.componentRegistered.label",
                                jParams.getLocale());
                        request.setAttribute ("warningMsg", dspMsg);
                    }

                } else {
                    String dspMsg = JahiaResourceBundle.getJahiaInternalResource ("org.jahia.admin.warningMsg.noComponentInfoFound.label",
                            jParams.getLocale());
                    request.setAttribute ("warningMsg", dspMsg);
                }
            }
        } catch (JahiaException je) {
            String dspMsg = JahiaResourceBundle.getJahiaInternalResource ("org.jahia.admin.JahiaDisplayMessage.requestProcessingError.label",
                    jParams.getLocale())+"<br>"+je.getJahiaErrorMsg();
            request.setAttribute ("warningMsg", dspMsg);
        }
        JahiaAdministration.doRedirect (request, response, session, JSP_PATH + "component_add.jsp");
    } // end addComponent


    //-------------------------------------------------------------------------
    /**
     * Component sharing between sites.
     *
     * @param   request         Servlet request.
     * @param   response        Servlet response.
     * @param   session         HttpSession object.
     */
    private void shareComponent (HttpServletRequest request,
                                 HttpServletResponse response,
                                 HttpSession session)
            throws IOException, ServletException {

        JahiaData jData = (JahiaData)request.getAttribute ("org.jahia.data.JahiaData");
        ProcessingContext jParams = null;
        if (jData != null) {
            jParams = jData.getProcessingContext ();
        }
        try {
            ApplicationsManagerService appManServ = sReg.getApplicationsManagerService ();
            JahiaSitesService sitesServ = sReg.getJahiaSitesService ();
            AppsShareService appShareServ = sReg.getAppsShareService ();
            ApplicationsManagerService appPersServ =
                    sReg.getApplicationsManagerService ();
            JahiaGroupManagerService grpManServ = sReg.getJahiaGroupManagerService ();

            if (appManServ == null
                    || sitesServ == null
                    || appShareServ == null
                    || appPersServ == null
                    || grpManServ == null) {
                throw new JahiaException ("Unavailable Services",
                        "Unavailable Services",
                        JahiaException.SERVICE_ERROR,
                        JahiaException.ERROR_SEVERITY);
            }


            // get the list of all site
            List<ApplicationBean> apps = appPersServ.getApplications ();
            List<ApplicationBean> ownApps = new ArrayList<ApplicationBean>();				// the site's apps
            if (apps == null) {
                apps = new ArrayList<ApplicationBean>();
            }
            int size = apps.size ();
            ApplicationBean appItem = null;
            for (int i = 0; i < size; i++) {
                appItem = apps.get (i);
                ownApps.add (appItem);
            }

            request.setAttribute ("appsList", ownApps.iterator());

            List<JahiaSite> grantedSites = new ArrayList<JahiaSite>();

// get sites where the user has an admin access...
            try {
                grantedSites = grpManServ.getAdminGrantedSites (user);
            } catch (Exception e) {
                grantedSites.add (site);
            }

            request.setAttribute ("sitesList", grantedSites);

            // get the current application
            int appID = -1;
            String appIDStr = "";
            ApplicationBean app = null;
            if (request.getParameter ("apps") == null) {

                if (ownApps.size () > 0) {

                    app = ownApps.get (0); // get the first app
                    appID = app.getID ();
                }
            } else {
                appIDStr = request.getParameter ("apps");
                appID = Integer.parseInt (appIDStr);
                app = appManServ.getApplication (appID);
            }

            String subAction = null;
            if (request.getParameter ("subaction") != null) {
                subAction = request.getParameter ("subaction");
            }

            if (subAction != null && subAction.equals ("save")) {
                // delete all share for this app
                appShareServ.removeShares (app);
                // add new ones;
                String[] vals = request.getParameterValues ("authSites");
                if (vals != null) {
                    JahiaSite authSite = null;
                    for (int i = 0; i < vals.length; i++) {
                        authSite = sitesServ.getSite (Integer.parseInt (vals[i]));
                        appShareServ.addShare (authSite, app);
                    }
                }

                appPersServ.saveDefinition (app);

            }


            // get the list of authorized sites for this application
            Iterator<Integer> authSitesID = null;

            authSitesID = appShareServ.getSites (app);
            List<JahiaSite> authSites = new ArrayList<JahiaSite>();
            Integer siteID = null;
            while (authSitesID.hasNext ()) {
                siteID = authSitesID.next ();
                JahiaSite aSite = sitesServ.getSite (siteID.intValue ());
                authSites.add (aSite);
            }

            request.setAttribute ("app", app);
            request.setAttribute ("appID", new Integer (appID));
            request.setAttribute ("authSites", authSites.iterator());

            JahiaAdministration.doRedirect (request,
                    response,
                    session,
                    JSP_PATH + "component_share.jsp");

        } catch (JahiaException je) {
            String dspMsg = JahiaResourceBundle.getJahiaInternalResource ("org.jahia.admin.JahiaDisplayMessage.requestProcessingError.label",
                    jParams.getLocale());
            request.setAttribute ("jahiaDisplayMessage", dspMsg);
            JahiaAdministration.doRedirect (request,
                    response,
                    session,
                    JSP_PATH + "menu.jsp");
        }

    }
}
