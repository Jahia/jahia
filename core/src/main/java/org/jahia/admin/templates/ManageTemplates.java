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

// $Id$
//
//  ManageTemplates
//
//  01.04.2001  AK  added in jahia.
//  17.05.2001  NK  Integrate License Check.
//

package org.jahia.admin.templates;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.jahia.api.Constants;
import org.jahia.bin.Jahia;
import org.jahia.bin.JahiaAdministration;
import org.jahia.data.JahiaData;
import org.jahia.data.events.JahiaEvent;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.utils.i18n.JahiaResourceBundle;
import org.jahia.security.license.License;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.services.pages.ContentPage;
import org.jahia.services.pages.JahiaPage;
import org.jahia.services.pages.JahiaPageDefinition;
import org.jahia.services.pages.JahiaPageService;
import org.jahia.services.pages.JahiaPageTemplateService;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.sites.JahiaSitesService;
import org.jahia.services.templates_deployer.JahiaTemplatesDeployerService;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.utils.JahiaTools;
import org.jahia.utils.properties.PropertiesManager;
import org.jahia.admin.AbstractAdministrationModule;

/**
 * desc:  This class is used by the administration to manage
 * all the templates you've on your Jahia portal. You can add a
 * template, edit, change the visibility of the template and edit
 * its options. You can also view non-installed templates.
 *
 * Copyright:    Copyright (c) 2002
 * Company:      Jahia Ltd
 *
 * @author Khue N'Guyen
 * @author Alexandre Kraft
 * @version 1.0
 */
public class ManageTemplates extends AbstractAdministrationModule {

    private static org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger(ManageTemplates.class);

    private static final String     CLASS_NAME          =  JahiaAdministration.CLASS_NAME;
    private static final String     JSP_PATH            =  JahiaAdministration.JSP_PATH;

    private static ServletContext   context;

    private JahiaSite site;
    private JahiaUser user;
    private ServicesRegistry sReg;

    private License coreLicense;


    /**
     * Default constructor.
     * @author  Alexandre Kraft
     *
     * @param   request       Servlet request.
     * @param   response      Servlet response.
     */
    public void service( HttpServletRequest       request,
                         HttpServletResponse      response )
    throws Exception
    {
        JahiaData jData = (JahiaData) request.getAttribute("org.jahia.data.JahiaData");
        ProcessingContext jParams = null;
        if (jData != null) {
            jParams = jData.getProcessingContext();
        }

        coreLicense = Jahia.getCoreLicense();
        if ( coreLicense == null ){
            // set request attributes...
            String dspMsg = JahiaResourceBundle.getJahiaInternalResource("org.jahia.admin.JahiaDisplayMessage.invalidLicenseKey.label",
                                               jParams.getLocale());
            request.setAttribute("jahiaDisplayMessage", dspMsg);
            // redirect...
            JahiaAdministration.doRedirect( request, response, request.getSession(), JSP_PATH + "menu.jsp" );
            return;
        }

        userRequestDispatcher( request, response, request.getSession() );
    } // end constructor



    /**
     * This method is used like a dispatcher for user requests.
     * @author  Alexandre Kraft
     *
     * @param   request       Servlet request.
     * @param   response      Servlet response.
     * @param   session       Servlet session for the current user.
     */
    private void userRequestDispatcher( HttpServletRequest    request,
                                        HttpServletResponse   response,
                                        HttpSession           session )
    throws Exception
    {
        String operation =  request.getParameter("sub");

        sReg = ServicesRegistry.getInstance();

        // check if the user has really admin access to this site...
        user     =  (JahiaUser) session.getAttribute( ProcessingContext.SESSION_USER );
        site =  (JahiaSite) session.getAttribute( ProcessingContext.SESSION_SITE );
        JahiaData jData = (JahiaData) request.getAttribute("org.jahia.data.JahiaData");
        ProcessingContext jParams = null;
        if (jData != null) {
            jParams = jData.getProcessingContext();
        }

        if ( site != null && user != null && sReg != null ){

            // set the new site id to administrate...
            request.setAttribute( "site", site );

            if(operation.equals("display")) {
                displayTemplateList( request, response, session );
            } else if(operation.equals("displaynewlist")) {
                displayNewTemplateList( request, response, session );
            } else if(operation.equals("details")) {
                displayNewTemplateDetail( request, response, session );
            } else if(operation.equals("edit")) {
                editTemplate( request, response, session );
            } else if(operation.equals("options")) {
                editTemplateOption( request, response, session );
            } else if(operation.equals("add")) {
                addTemplate( request, response, session );
            } else if(operation.equals("swap")) {
                processSwap( request, response, session );
            }
        } else {
          String dspMsg = JahiaResourceBundle.getJahiaInternalResource("org.jahia.admin.JahiaDisplayMessage.requestProcessingError.label",
                                             jParams.getLocale());
          request.setAttribute("jahiaDisplayMessage", dspMsg);
            JahiaAdministration.doRedirect( request, response, session, JSP_PATH + "menu.jsp" );
        }

    } // userRequestDispatcher



    //-------------------------------------------------------------------------
    /**
     * Display a list of the templates.
     *
     * @author  NK
     * @param   request         Servlet request.
     * @param   response        Servlet response.
     * @param   session         HttpSession object.
     */
    private void displayTemplateList( HttpServletRequest   request,
                                      HttpServletResponse  response,
                                      HttpSession          session )
    throws Exception

    {

      JahiaData jData = (JahiaData) request.getAttribute("org.jahia.data.JahiaData");
      ProcessingContext jParams = null;
      if (jData != null) {
          jParams = jData.getProcessingContext();
      }
        try {

            JahiaPageTemplateService pageTempServ =
            sReg.getJahiaPageTemplateService();

            if ( pageTempServ == null ){
                throw new JahiaException(	"Unavailable Services",
                                            "Unavailable Services",
                                            JahiaException.SERVICE_ERROR,
                                            JahiaException.ERROR_SEVERITY );
            }

            Iterator enumeration = pageTempServ.getPageTemplates (site.getID(), false);
            // check for acls
            JahiaPageDefinition def = null;
            List v = new ArrayList();
            while (enumeration.hasNext()){
                def = (JahiaPageDefinition)enumeration.next();
                if ( def.getACL() == null ){
                    ServicesRegistry.getInstance().getJahiaPageTemplateService().createPageTemplateAcl(def);
                }
                v.add(def);
            }
            enumeration = v.iterator();

            request.setAttribute("templList", enumeration);

            JahiaAdministration.doRedirect( request,
                                            response,
                                            session,
                                            JSP_PATH + "manage_templates.jsp" );

        } catch ( JahiaException je ){
          String dspMsg = JahiaResourceBundle.getJahiaInternalResource("org.jahia.admin.JahiaDisplayMessage.requestProcessingError.label",
                                             jParams.getLocale());
          request.setAttribute("jahiaDisplayMessage", dspMsg);
            JahiaAdministration.doRedirect( request,
                                            response,
                                            session,
                                            JSP_PATH + "menu.jsp" );
        }


    } // end displayTemplateList


    //-------------------------------------------------------------------------
    /**
     * Display the edit form for a single template.
     *
     * @author  NK
     * @param   request         Servlet request.
     * @param   response        Servlet response.
     * @param   session         HttpSession object.
     */
    private void editTemplate( 	HttpServletRequest   request,
                                HttpServletResponse  response,
                                HttpSession          session )
    throws Exception
    {

      JahiaData jData = (JahiaData) request.getAttribute("org.jahia.data.JahiaData");
      ProcessingContext jParams = null;
      if (jData != null) {
          jParams = jData.getProcessingContext();
      }
        try {

            JahiaPageTemplateService pageTempServ =
            sReg.getJahiaPageTemplateService();

            JahiaTemplatesDeployerService tempDepServ =
            sReg.getJahiaTemplatesDeployerService();

            JahiaSitesService sitesServ =
            sReg.getJahiaSitesService();

            JahiaPageService pageServ =
            sReg.getJahiaPageService();

            if ( pageTempServ == null
                || tempDepServ == null
                || sitesServ == null
                || pageServ == null ){
                throw new JahiaException(	"Unavailable Services",
                                            "Unavailable Services",
                                            JahiaException.SERVICE_ERROR,
                                            JahiaException.ERROR_SEVERITY );
            }

            // get paramater
            String strVal = request.getParameter("templid");
            if ( strVal != null ){
                int id = Integer.parseInt(strVal);
                JahiaPageDefinition templ = pageTempServ.lookupPageTemplate (id);

                // this is here so it doesn't overwrite the value set after critical pages check...
                request.setAttribute("canDelete",  Boolean.TRUE );

                if ( templ != null ){

                    String  subAction     =  (String) request.getParameter("subaction");

                    // Override subAction if we are performing a template swap before
                    // template deletion
                    Integer processedSwap =  (Integer)request.getAttribute("processedSwap");
                    if ( processedSwap != null && processedSwap.intValue() == 1 ) {
                        subAction         =  "confirmdelete";
                    }

                    if ( subAction == null ){
                        subAction = "";
                    }

                    if ( subAction.equals("delete") ) {
                        String undeploy = (String)request.getParameter("undeploy");

                        if ( undeploy != null ){
                            tempDepServ.undeploy(templ);
                        }

                        pageTempServ.deletePageTemplate(templ.getID());
                        displayTemplateList(request,response,session);
                        return;

                    } else if ( subAction.equals("confirmdelete") ) {

                        List   criticalPages =  new ArrayList();
                        int    mTemplID      =  templ.getID();
                        List pageIDs       =  pageServ.getPageIDsWithTemplate(mTemplID);
                        for (int i=0; i<pageIDs.size(); i++) {

                            int  pageID      = ((Integer)pageIDs.get(i)).intValue();

                            try {
                                ContentPage contentPage = ContentPage.getPage(pageID,false);
                                criticalPages.add(contentPage);
                            } catch (Exception t) {
                                logger.debug("Exception when loading pages using template[" + mTemplID + "]",t);
                            }
                        }
                        if (!criticalPages.isEmpty()) {
                            request.setAttribute("processedSwap", null);
                            displaySwap (request, response, session, criticalPages, templ);
                            return;
                        }

                        request.setAttribute("currAction",  "confirmdelete");

                    } else if ( subAction.equals("swap") ) {

                        // process requested template swapping for a set of pages...
                        processSwap (request, response, session);

                    } else if ( subAction.equals("save") ) {

                        String templName = (String)request.getParameter("templName");
                        String visible_status = (String)request.getParameter("visible_status");

                        if (templName != null && (templName.trim().length()>0) ){
                            templ.setName(templName);
                        }
                        if ( visible_status != null ){
                            templ.setAvailable(true);
                            String isDefault = (String)request.getParameter("isDefault");
                            if ( isDefault != null ){
                                site.setDefaultTemplateID(templ.getID());
                            } else {
                                site.setDefaultTemplateID(-1);
                            }
                        } else {
                            templ.setAvailable(false);
                        }

                        templ.commitChanges();

                        sitesServ.updateSite(site);

                        templ = pageTempServ.lookupPageTemplate (id);
                        String dspMsg = JahiaResourceBundle.getJahiaInternalResource("org.jahia.admin.JahiaDisplayMessage.templateUpdated.label",
                                                           jParams.getLocale());
                        session.setAttribute(CLASS_NAME + "jahiaDisplayMessage", dspMsg);
                    }

                } else {
                  String errMsg = JahiaResourceBundle.getJahiaInternalResource("org.jahia.admin.errMsg.noTemplateDefinition.label",
                                                     jParams.getLocale());
                  request.setAttribute("errMsg", errMsg);
                }

                int pageid = site.getHomePageID();
                JahiaPage page = pageServ.lookupPage(pageid);

                if ( page != null && (page.getPageTemplateID() == templ.getID()) ){
                    request.setAttribute("canDelete",  Boolean.FALSE );
                }

                request.setAttribute("templ",  templ);

            }

            request.setAttribute("templatesContext",getTemplatesContext(session));

            JahiaAdministration.doRedirect( request,
                                            response,
                                            session,
                                            JSP_PATH + "template_edit.jsp" );

        } catch ( JahiaException je ){
          String dspMsg = JahiaResourceBundle.getJahiaInternalResource("org.jahia.admin.JahiaDisplayMessage.requestProcessingError.label",
                                             jParams.getLocale());
          request.setAttribute("jahiaDisplayMessage", dspMsg);
            JahiaAdministration.doRedirect( request,
                                            response,
                                            session,
                                            JSP_PATH + "menu.jsp" );
        }

    }

    //-------------------------------------------------------------------------
    /**
     * Handle Edit Templates options
     *
     * @author  NK
     * @param   request         Servlet request.
     * @param   response        Servlet response.
     * @param   session         HttpSession object.
     */
    private void editTemplateOption( 	HttpServletRequest   request,
                                        HttpServletResponse  response,
                                        HttpSession          session )
    throws IOException, ServletException
    {

      JahiaData jData = (JahiaData) request.getAttribute("org.jahia.data.JahiaData");
      ProcessingContext jParams = null;
      if (jData != null) {
          jParams = jData.getProcessingContext();
      }
        try {

            JahiaSitesService sitesServ =
            sReg.getJahiaSitesService();


            if ( sitesServ == null ){
                throw new JahiaException(	"Unavailable Services",
                                            "Unavailable Services",
                                            JahiaException.SERVICE_ERROR,
                                            JahiaException.ERROR_SEVERITY );
            }

            int autoDeploy = 0;

            if ( site.getTemplatesAutoDeployMode() ){
                autoDeploy = 1;
            }

            request.setAttribute("autoDeploy",  new Integer(autoDeploy));

            // get paramater
            String subAction =(String)request.getParameter("subaction");
            if ( subAction != null && subAction.equals("save") ) {

                String strVal = (String)request.getParameter("autoDeploy");
                int intVal = 0;

                if ( strVal != null ){
                    intVal = 1;
                }

                if ( intVal != autoDeploy ){
                    try {
                        site.setTemplatesAutoDeployMode(intVal==1);
                        sitesServ.updateSite(site);

                        session.setAttribute( ProcessingContext.SESSION_SITE , site );

                        String dspMsg = JahiaResourceBundle.getJahiaInternalResource("org.jahia.admin.JahiaDisplayMessage.changeUpdated.label",
                                                           jParams.getLocale());
                        session.setAttribute(CLASS_NAME + "jahiaDisplayMessage", dspMsg);

                    } catch ( JahiaException je ) {
                      String dspMsg = JahiaResourceBundle.getJahiaInternalResource("org.jahia.admin.JahiaDisplayMessage.changeNotUpdated.label",
                                                         jParams.getLocale());
                      session.setAttribute(CLASS_NAME + "jahiaDisplayMessage", dspMsg);
                    }

                    request.setAttribute("autoDeploy",  new Integer(intVal));
                }
            }

            JahiaAdministration.doRedirect( request,
                                            response,
                                            session,
                                            JSP_PATH + "template_option.jsp" );
        } catch ( JahiaException je ){
          String dspMsg = JahiaResourceBundle.getJahiaInternalResource("org.jahia.admin.JahiaDisplayMessage.requestProcessingError.label",
                                             jParams.getLocale());
          request.setAttribute("jahiaDisplayMessage", dspMsg);
            JahiaAdministration.doRedirect( request,
                                            response,
                                            session,
                                            JSP_PATH + "menu.jsp" );
        }


    } // end editTemplateOption


    //-------------------------------------------------------------------------
    /**
     * Display the list of new templates.
     *
     * @author  NK
     * @param   request         Servlet request.
     * @param   response        Servlet response.
     * @param   session         HttpSession object.
     */
    private void displayNewTemplateList( HttpServletRequest   request,
                                       HttpServletResponse  response,
                                       HttpSession          session )
    throws IOException, ServletException
    {

      JahiaData jData = (JahiaData) request.getAttribute("org.jahia.data.JahiaData");
      ProcessingContext jParams = null;
      if (jData != null) {
          jParams = jData.getProcessingContext();
      }
        try {

            JahiaTemplatesDeployerService tempDepServ =
            sReg.getJahiaTemplatesDeployerService();


            if ( tempDepServ == null ){
                throw new JahiaException(	"Unavailable Services",
                                            "Unavailable Services",
                                            JahiaException.SERVICE_ERROR,
                                            JahiaException.ERROR_SEVERITY );
            }

            // get the list of new web apps
            Iterator enumeration = tempDepServ.getTemplatesPackages(site.getSiteKey());
            List vec = new ArrayList();

            JahiaTemplatesPackage aPackage = null;

            while (enumeration.hasNext()){

                aPackage = (JahiaTemplatesPackage)enumeration.next();

                if ( aPackage != null ){
                    vec.add(aPackage);
                } else {
                    //System.out.println("displayNewTemplateList packages is null");
                }
            }

            request.setAttribute("packagesList", vec.iterator());
            JahiaAdministration.doRedirect( request,
                                            response,
                                            session,
                                            JSP_PATH + "new_templates.jsp" );
        } catch ( JahiaException je ){
          String dspMsg = JahiaResourceBundle.getJahiaInternalResource("org.jahia.admin.JahiaDisplayMessage.requestProcessingError.label",
                                             jParams.getLocale());
          request.setAttribute("jahiaDisplayMessage", dspMsg);
            JahiaAdministration.doRedirect( request,
                                            response,
                                            session,
                                            JSP_PATH + "menu.jsp" );
        }

    }



    //-------------------------------------------------------------------------
    /**
     * Display informations about a new template
     *
     * @author  NK
     * @param   request         Servlet request.
     * @param   response        Servlet response.
     * @param   session         HttpSession object.
     */
    private void displayNewTemplateDetail( HttpServletRequest   request,
                                            HttpServletResponse  response,
                                            HttpSession          session )
    throws IOException, ServletException
    {

      JahiaData jData = (JahiaData) request.getAttribute("org.jahia.data.JahiaData");
      ProcessingContext jParams = null;
      if (jData != null) {
          jParams = jData.getProcessingContext();
      }
        try {

            JahiaTemplatesDeployerService tempDepServ =
            sReg.getJahiaTemplatesDeployerService();

            JahiaPageTemplateService pageTempServ =
            sReg.getJahiaPageTemplateService();


            if ( tempDepServ == null || pageTempServ == null ){
                throw new JahiaException(	"Unavailable Services",
                                            "Unavailable Services",
                                            JahiaException.SERVICE_ERROR,
                                            JahiaException.ERROR_SEVERITY );
            }

            // get the new template package
            String packageName = (String)request.getParameter("package_name");
            JahiaTemplatesPackage aPackage =
            (JahiaTemplatesPackage)tempDepServ.getTemplatesPackage(site.getSiteKey()
                                                                    + "_"
                                                                    + packageName);

            // check for license limitation
            int nbTemplates = pageTempServ.getNbPageTemplates(site.getID());

            boolean canDeploy = ( ( Jahia.getTemplateLimit() == -1 )
                                    || !(nbTemplates + aPackage.getTemplates().size() > Jahia.getTemplateLimit()) );

            request.setAttribute("templateLimit", new Integer(Jahia.getTemplateLimit()) );
            request.setAttribute("canDeploy", Boolean.valueOf(canDeploy) );


            String subAction =(String)request.getParameter("subaction");
            if ( subAction == null ){
                request.setAttribute("aPackage", aPackage);
            } else if ( subAction.equals("deploy") && canDeploy){
                try {
                    if (tempDepServ.deploy( site,
                                            aPackage.getRootFolder(),
                                            aPackage.getFilePath(),true)){

                        // Register package in Jahia
                        tempDepServ.registerTemplates(site, aPackage);

                        // delete the package
                        tempDepServ.deletePackage(site,aPackage.getFilePath());

                        displayNewTemplateList(request,response,session);
                        return;
                    } else {
                      String dspMsg = JahiaResourceBundle.getJahiaInternalResource("org.jahia.admin.JahiaDisplayMessage.deployingPackageError.label",
                                                         jParams.getLocale());
                      session.setAttribute(CLASS_NAME + "jahiaDisplayMessage", dspMsg);
                        request.setAttribute("aPackage", aPackage);
                    }
                } catch ( JahiaException je ){
                    request.setAttribute("aPackage", aPackage);
                    String dspMsg = JahiaResourceBundle.getJahiaInternalResource("org.jahia.admin.JahiaDisplayMessage.deployingPackageError.label",
                                                       jParams.getLocale());
                    session.setAttribute(CLASS_NAME + "jahiaDisplayMessage", dspMsg);
                }
            } else if ( subAction.equals("delete") ){

                try {
                    if (tempDepServ.deletePackage(site,aPackage.getFilePath())){
                        displayNewTemplateList(request,response,session);
                        return;
                    } else {
                        request.setAttribute("aPackage", aPackage);
                        String dspMsg = JahiaResourceBundle.getJahiaInternalResource("org.jahia.admin.JahiaDisplayMessage.deletingPackageError.label",
                                                           jParams.getLocale());
                        session.setAttribute(CLASS_NAME + "jahiaDisplayMessage", dspMsg);
                    }
                } catch ( IOException ioe ){
                  String dspMsg = JahiaResourceBundle.getJahiaInternalResource("org.jahia.admin.JahiaDisplayMessage.deletingPackageError.label",
                                                     jParams.getLocale());
                  session.setAttribute(CLASS_NAME + "jahiaDisplayMessage", dspMsg);
                }
            }

             JahiaAdministration.doRedirect( request,
                                             response,
                                             session,
                                             JSP_PATH + "new_template_detail.jsp" );
        } catch ( JahiaException je ){
          String dspMsg = JahiaResourceBundle.getJahiaInternalResource("org.jahia.admin.JahiaDisplayMessage.requestProcessingError.label",
                                             jParams.getLocale());
          request.setAttribute("jahiaDisplayMessage", dspMsg);
            JahiaAdministration.doRedirect( request,
                                            response,
                                            session,
                                            JSP_PATH + "menu.jsp" );
        }

    }


    //-------------------------------------------------------------------------
    /**
     * Handle all the process of manually adding a new template
     *
     * @author  NK
     * @param   request         Servlet request.
     * @param   response        Servlet response.
     * @param   session         HttpSession object.
     */
    private void addTemplate( 	HttpServletRequest   request,
                                HttpServletResponse  response,
                                HttpSession          session )

    throws IOException, ServletException
    {


      JahiaData jData = (JahiaData) request.getAttribute("org.jahia.data.JahiaData");
      ProcessingContext jParams = null;
      if (jData != null) {
          jParams = jData.getProcessingContext();
      }
        try {

            JahiaTemplatesDeployerService tempDepServ =
            sReg.getJahiaTemplatesDeployerService();

            JahiaPageTemplateService pageTempServ =
            sReg.getJahiaPageTemplateService();

            if ( tempDepServ == null || pageTempServ == null ){
                throw new JahiaException(	"Unavailable Services",
                                            "Unavailable Services",
                                            JahiaException.SERVICE_ERROR,
                                            JahiaException.ERROR_SEVERITY );
            }

            // check for license limitation
            int nbTemplates = pageTempServ.getNbPageTemplates(site.getID());

            boolean canAddNew = ( (Jahia.getTemplateLimit() == -1)
                                    || (nbTemplates < Jahia.getTemplateLimit()) );

            request.setAttribute("templateLimit", new Integer(Jahia.getTemplateLimit()) );
            request.setAttribute("canAddNew", Boolean.valueOf(canAddNew) );


            PropertiesManager properties =  new PropertiesManager( Jahia.getJahiaPropertiesFileName() );

            request.setAttribute("warningMsg","");

            JahiaSite site = (JahiaSite)request.getAttribute("site");

            String subAction 	= (String)request.getParameter("subaction");
            String templName 	= (String)request.getParameter("templName");
            String rootFolder 	= (String)request.getParameter("rootFolder");
            if ( rootFolder == null ){
                rootFolder = File.separator;
            }

            String fileName 	= (String)request.getParameter("fileName");
            String isAvailable 	= (String)request.getParameter("isAvailable");
            String pageType 	= (String)request.getParameter("pageType");

            ExtendedNodeType t = NodeTypeRegistry.getInstance().getNodeType(Constants.JAHIANT_PAGE);
            ExtendedNodeType[] types = t.getSubtypes();
            Map m = new HashMap();
            for (int i = 0; i < types.length; i++) {
                m.put(types[i].getName(), types[i].getLabel(jParams.getLocale()));
            }

            request.setAttribute("types", types);
            request.setAttribute("templName",JahiaTools.replaceNullString(templName,""));
            request.setAttribute("rootFolder",JahiaTools.replaceNullString(rootFolder,""));
            request.setAttribute("fileName",JahiaTools.replaceNullString(fileName,""));

            if ( isAvailable != null ){
                request.setAttribute("isAvailable",new Integer(1));
            } else {
                request.setAttribute("isAvailable",new Integer(0));
            }

            if ( subAction != null && subAction.equals("save") && canAddNew ){
                if ( templName == null || (templName.length()<=0)
                     || fileName == null || (fileName.length()<=0) ) {
                  String dspMsg = JahiaResourceBundle.getJahiaInternalResource("org.jahia.admin.warningMsg.completeRequestInfo.label",
                                                     jParams.getLocale());
                  request.setAttribute("warningMsg", dspMsg);
                } else {

                    // save definition in db
                    String path = ManageTemplates.context.getRealPath(properties.getProperty("jahiaTemplatesDiskPath") );

                    while ( rootFolder.startsWith("/")
                            || rootFolder.startsWith("\\")
                            || rootFolder.startsWith(".") ){
                        rootFolder = rootFolder.substring(1,rootFolder.length());
                    }

                    while ( rootFolder.endsWith("/")
                            || rootFolder.endsWith("\\") ){
                        rootFolder = rootFolder.substring(0,rootFolder.length()-1);
                    }

                    // check if the file really exist
                    StringBuffer tempFullPath = new StringBuffer (1024);
                    tempFullPath.append(path);
                    tempFullPath.append(File.separator);
                    tempFullPath.append(site.getSiteKey());
                    tempFullPath.append(File.separator);
                    tempFullPath.append(rootFolder);
                    tempFullPath.append(File.separator) ;
                    tempFullPath.append(fileName) ;

                    if ( !JahiaTools.checkFileNameCaseSensitive(tempFullPath.toString()) ){
                      String warningMsg = JahiaResourceBundle.getJahiaInternalResource("org.jahia.admin.warningMsg.noFile.label",
                                                         jParams.getLocale());
                      warningMsg += " " + fileName + " ";
                      warningMsg += JahiaResourceBundle.getJahiaInternalResource("org.jahia.admin.warningMsg.inFolder.label",
                                                         jParams.getLocale());
                      warningMsg += " " + rootFolder;
                      request.setAttribute("warningMsg", warningMsg);
                    } else if ( rootFolder.startsWith(".") ){
                      String warningMsg = JahiaResourceBundle.getJahiaInternalResource("org.jahia.admin.warningMsg.invalidTemplateFolder.label",
                                                         jParams.getLocale());
                        request.setAttribute("warningMsg",warningMsg);
                    } else {
                        StringBuffer buff = new StringBuffer (1024);
                        buff.append(properties.getProperty("jahiaTemplatesDiskPath"));
                        buff.append(site.getSiteKey());
                        buff.append("/") ;
                        if ( !rootFolder.equals("") ){
                            buff.append(rootFolder);
                            buff.append("/") ;
                        }
                        buff.append(fileName) ;
                        String sourcePath = JahiaTools.replacePattern(buff.toString(),"\\","/");

                        JahiaPageDefinition def = pageTempServ.getPageTemplateBySourcePath(site.getID(),sourcePath);
                        if ( def == null || !def.getSourcePath().equals(sourcePath) ){

                            pageTempServ.createPageTemplate (

                                        site.getID(),
                                        templName,
                                        sourcePath,
                                        (isAvailable != null),
                                        pageType,
                                        null,
                                        "",      	// no image
                                        site.getAclID() );

                            String warningMsg = JahiaResourceBundle.getJahiaInternalResource("org.jahia.admin.warningMsg.template.label",
                                jParams.getLocale());
                            warningMsg += " " + fileName + " ";
                            warningMsg = JahiaResourceBundle.getJahiaInternalResource("org.jahia.admin.warningMsg.templateAdded.label",
                                jParams.getLocale());
                            request.setAttribute("warningMsg",warningMsg);
                        } else {
                            request.setAttribute("warningMsg",
                            "This template is already registered in Jahia");
                        }
                    }
                }

            }

            JahiaAdministration.doRedirect(request,response,session,JSP_PATH + "template_add.jsp");

        } catch ( NoSuchNodeTypeException e ) {
            logger.error("Node type not found",e);
        } catch ( JahiaException je ) {
          String dspMsg = JahiaResourceBundle.getJahiaInternalResource("org.jahia.admin.JahiaDisplayMessage.requestProcessingError.label",
                                             jParams.getLocale());
          request.setAttribute("jahiaDisplayMessage", dspMsg);
            JahiaAdministration.doRedirect( request,
                                            response,
                                            session,
                                            JSP_PATH + "menu.jsp" );
        }


    } // end addTemplate





    protected String getTemplatesContext(HttpSession session){

        String templatesContext = null;

        // try to get from session
        templatesContext = (String)session.getAttribute(CLASS_NAME + "jahiaTemplatesDiskPath");
        if ( templatesContext == null ){

            PropertiesManager props =  new PropertiesManager( Jahia.getJahiaPropertiesFileName() );

            if ( props!= null ){

                templatesContext = (String)props.getProperty("jahiaTemplatesDiskPath");

                if ( templatesContext != null ){
                    session.setAttribute(CLASS_NAME + "jahiaTemplatesDiskPath", templatesContext);
                }
            }
        }

        return templatesContext;
    }


    /**
     * Display a custom version of the pages settings view, using doRedirect().
     * This form prompts the user to choose an alternative template for any pages
     * currently referenced.
     *
     * @author  Alexandre Kraft
     * @author  Mikha�l Janson
     *
     * @param   request       Servlet request.
     * @param   response      Servlet response.
     * @param   session       Servlet session for the current user.
     * @param   criticalPages an <code>List</code> of <code>JahiaPage</code> objects referencing the template we want to delete.
     * @param   templ          a reference to the template we want to delete.
     */
    private void displaySwap( HttpServletRequest    request,
                              HttpServletResponse   response,
                              HttpSession           session,
                              List                  criticalPages,
                              JahiaPageDefinition   templ )
    throws IOException, ServletException
    {
        try {

            // get all pages id...
            boolean canDeleteTemplate = true;
            int currentVersion = ServicesRegistry.getInstance()
                    .getJahiaVersionService().getCurrentVersionID();

            // get all pages infos...
            List        allPagesInfosList      = new ArrayList();
            List        allTemplatesList       = new ArrayList();
            for (int i=0; i<criticalPages.size(); i++) {
                ContentPage contentPage = (ContentPage) criticalPages.get(i);
                if ( canDeleteTemplate
                     && (contentPage.hasActiveEntries() ||
                     contentPage.hasArchiveEntryState(currentVersion) ) ){
                    canDeleteTemplate = false;
                }
                allPagesInfosList.add (contentPage);
            }

            // get homepage id for the current site...
            Integer homePageID = new Integer( site.getHomePageID() );

            // retrieve previous form values...
            Integer basePageID           = (Integer) request.getAttribute(CLASS_NAME + "basePageID");
            Integer baseTemplateID       = (Integer) request.getAttribute(CLASS_NAME + "baseTemplateID");

            // get only visible templates
            Iterator completeTemplatesEnumeration =  ServicesRegistry.getInstance().getJahiaPageTemplateService().getPageTemplates (site.getID(), true);

            // exclude the template we want to delete...
            while (completeTemplatesEnumeration.hasNext()) {
                JahiaPageDefinition template = (JahiaPageDefinition) completeTemplatesEnumeration.next();
                if(template.getID() != templ.getID()) {
                    allTemplatesList.add (template);
                }
            }
            Iterator allTemplatesEnumeration = allTemplatesList.iterator();

            // set default values...
            if(basePageID == null)     {  basePageID     =  new Integer(0);  }
            if(baseTemplateID == null) {  baseTemplateID =  new Integer(0);  }
            if(homePageID == null)     {  homePageID     =  new Integer(0);  }

            // set all pages infos into an Iterator and redirect...
            request.setAttribute("canDeleteTemplate",         Boolean.valueOf(canDeleteTemplate));
            request.setAttribute("homePageID",                homePageID);
            request.setAttribute("basePageID",                basePageID);
            request.setAttribute("baseTemplateID",            baseTemplateID);
            request.setAttribute("allTemplatesEnumeration",   allTemplatesEnumeration);
            request.setAttribute("allPagesInfosEnumeration",  allPagesInfosList.iterator());
            request.setAttribute("totalCriticalPages",        new Integer(criticalPages.size()));
            request.setAttribute("templ",                     templ);

            JahiaAdministration.doRedirect( request, response, session, JSP_PATH + "template_cleanpages.jsp" );

            // reset message...
            session.setAttribute(CLASS_NAME + "jahiaDisplayMessage",  Jahia.COPYRIGHT);
        } catch (JahiaException je) {
        }
    } // end displaySwap


    /**
     * Process requested template swapping to remove references
     * to a given template from a page before deleteing the template.
     *
     * @author  Mikha�l Janson
     *
     * @param   request       Servlet request.
     * @param   response      Servlet response.
     * @param   session       Servlet session for the current user.
     */
    protected void processSwap( HttpServletRequest   request,
                                HttpServletResponse  response,
                                HttpSession          session )
    throws Exception
    {
        int       idPage            =  0;
        int       idTemplate        =  0;
        String    idPageString      =  "";
        String    idTemplateString  =  (String) request.getParameter("templateid").trim();
        String[]  pageIDs           =  request.getParameterValues( "pageids" );
        List      errors            =  new ArrayList();
        JahiaData jData = (JahiaData) request.getAttribute("org.jahia.data.JahiaData");
        ProcessingContext jParams = null;
        if (jData != null) {
            jParams = jData.getProcessingContext();
        }

        boolean   valid             = true;


        // check form validity...
        if ((pageIDs == null) || (pageIDs.length < 1)) {
            valid = false;
            errors.add("Please choose at least one page.");
        }

        if (idTemplateString.equals("0")) {
            valid = false;
            errors.add("Please choose a template.");
        }

        if (!valid) {
            request.setAttribute("manageTemplatesErrors", errors);
            idTemplate = 0;

        } else {

            try {

                idTemplate  = Integer.parseInt(idTemplateString);

            } catch (NumberFormatException nfe) {
                // FIXME - Mik : TEMPORARY...
                String dspMsg = JahiaResourceBundle.getJahiaInternalResource("org.jahia.admin.JahiaDisplayMessage.invalidTemplateReference.label",
                                                   jParams.getLocale());
                session.setAttribute(CLASS_NAME + "jahiaDisplayMessage", dspMsg);
            }

        }

        // get form values...
        if (pageIDs != null) {

            String titlePage = "";

            for (int i = 0; i < pageIDs.length; i++) {
                idPageString  = pageIDs[i];

                try {
                    idPage    = Integer.parseInt(idPageString);

                    if (!swapTemplate(idPage, idTemplate,jParams)) {

                        titlePage = sReg.getJahiaPageService().lookupPage(idPage).getTitle();
                        errors.add("Couldn't change templates for page "+ titlePage);
                    }
                } catch (Exception e) {
                    //
                }
            }
        }
        request.setAttribute("processedSwap", new Integer(1));
        editTemplate (request, response, session);


    } // end processSwap



    /**
     * Perform a template swap on a page
     *
     * @author  Mikhael Janson
     *
     * @param   idPage        the ID of the page we want to update
     * @param   idTemplate    the ID of the template we want to assign this page
     */
    private boolean swapTemplate( int idPage, int idTemplate, ProcessingContext jParams )
    {
        boolean   out  = false;
        JahiaPage page = null;
        try {

            JahiaPageService pageServ = ServicesRegistry.getInstance()
                                                        .getJahiaPageService();

            if ( idPage != 0 && idTemplate != 0 ){
                page =  pageServ.lookupPage(idPage);

                if ( page != null ){
                    // set new settings for this JahiaPage...
                    page.setPageTemplateID (idTemplate);
                    page.commitChanges (true, jParams.getUser());
                    ContentPage contentPage = ContentPage.getPage(page.getID());
                    JahiaEvent objectCreatedEvent = new JahiaEvent(this, jParams, contentPage);
                    ServicesRegistry.getInstance ().getJahiaEventService ()
                        .fireContentObjectUpdated(objectCreatedEvent);

                    // index page
                    /* handled by previous event
                    ServicesRegistry.getInstance().getJahiaSearchService()
                            .indexPage(page.getID(), jParams.getUser());
                    */

                    // FIXME - mik : CHECK IF THE PAGE HAS CHANGED TEMPLATES, AND THE TEMPLATE IS VALID...
                    out = true;
                }

            }

        } catch ( JahiaException je ){
            //
        }

        return out;

    } // end swapTemplate


} // end ManageTemplates
