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
//  ViewAccess
//
//  01.04.2001  AK  added in jahia.
//  22.04.2001  AK  multisite modifications.
//  17.05.2001  NK  Integrate License Check.
//

package org.jahia.admin.access;

import java.io.IOException;
import java.util.*;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.jahia.bin.Jahia;
import org.jahia.bin.JahiaAdministration;
import org.jahia.data.JahiaData;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.resourcebundle.JahiaResourceBundle;
import org.jahia.services.pages.JahiaPage;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.utils.JahiaTools;
import org.jahia.security.license.License;

/**
 * desc:  This class is used by the administration to view users access
 *
 * Copyright:    Copyright (c) 2002
 * Company:      Jahia Ltd
 *
 * @author Philippe Vollenweider
 * @version 1.0
 */
public class ViewAccess
{
    private static final String     CLASS_NAME        =  JahiaAdministration.CLASS_NAME;
    private static final String     JSP_PATH          =  JahiaAdministration.JSP_PATH;

    private              JahiaSite                  site;
    private              JahiaUser                  user;

    private License coreLicense;



    /**
     * Default constructor.
     * @author  Alexandre Kraft
     *
     * @param   request       Servlet request.
     * @param   response      Servlet response.
     * @param   session       Servlet session for the current user.
     */
    public ViewAccess( HttpServletRequest       request,
                        HttpServletResponse      response,
                        HttpSession              session )
    throws Exception
    {
        // get the current website. get the jahiaserver if it's null...
        site =  (JahiaSite) session.getAttribute( ProcessingContext.SESSION_SITE );
        user     =  (JahiaUser) session.getAttribute( ProcessingContext.SESSION_USER );

        if(site==null) {
            site = ServicesRegistry.getInstance().getJahiaSitesService().getSite( 0 );
            session.setAttribute( ProcessingContext.SESSION_SITE, site );
        }

        JahiaData jData = (JahiaData) request.getAttribute("org.jahia.data.JahiaData");
        ProcessingContext jParams = null;
        if (jData != null) {
            jParams = jData.getProcessingContext();
        }
        coreLicense = Jahia.getCoreLicense();
        if ( coreLicense == null ){
            // set request attributes...
            String dspMsg = JahiaResourceBundle.getAdminResource("org.jahia.admin.JahiaDisplayMessage.invalidLicense.label",
                                               jParams, jParams.getLocale());
            request.setAttribute("jahiaDisplayMessage", dspMsg);
            // redirect...
            JahiaAdministration.doRedirect( request, response, session, JSP_PATH + "menu.jsp" );
            return;
        }

        // continue the execution of user request...
        userRequestDispatcher( request, response, session );
    } // end constructor



    /**
     * Default constructor.
     * @author  Alexandre Kraft
     *
     * @param   request       Servlet request.
     * @param   response      Servlet response.
     * @param   session       Servlet session for the current user.
     */

    /* FIXME : Not used ?
    public ViewAccess()
    throws Exception
    {
        ServicesRegistry sReg =  ServicesRegistry.getInstance();
        if (sReg != null) {
            uMgr =  sReg.getJahiaUserManagerService();
            gMgr =  sReg.getJahiaGroupManagerService();
        }
    } // end constructor
    */



    /**
     * This method is used like a dispatcher for user requests.
     * @author  Philippe Vollenweider
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
        displayAccess( request, response, session, request.getParameter("sub"));
    } // userRequestDispatcher



    /**
     * Display the complete user access list, using JahiaAdministration.doRedirect().
     * @author Philippe Vollenweider
     *
     * @param   request         The HttpServlet request.
     * @param   response        The HttpServlet response.
     * @param   session         The HttpSession object.
     * @param   type            Describe the type for output (tab || excel)
     */
    protected void displayAccess( HttpServletRequest    request,
                                    HttpServletResponse   response,
                                    HttpSession           session,
                                    String                type )
    throws IOException, ServletException
    {
        // Set the MIME type for the information being sent to the browser.
        // In this case, we are going to send excel or plain text
        if (type.equals("excel")){
            response.setContentType( "application/ms-excel" );
            response.setHeader("Content-Disposition","attachment; filename=\"user_access.xls\"");
        } else {
            response.setContentType( "text/plain" );
        }
        // Get a reference to the output stream.
        // Anything written to this stream is sent directly to the browser
        // (The browser sees this as its input).
        ServletOutputStream result = response.getOutputStream();

        try {
            String output = JahiaTools.html2text(drawRightsUserList());
            result.println( output );
        } catch ( Exception t ){
            // oups...
        } finally {
            result.close();
        }

        // reset message...
        session.setAttribute(CLASS_NAME + "jahiaDisplayMessage",  Jahia.COPYRIGHT);
    } // end displayAccess


    private String drawRightsUserList()
    throws JahiaException {

        StringBuffer output = new StringBuffer();

        // find user list
        List theUserList = ServicesRegistry.getInstance().getJahiaSiteUserManagerService().getMembers(site.getID());

        // find home page
        JahiaPage theHomePage = site.getHomePage();

        // draw home page's childs, recursively
        if (theHomePage != null) {

            output.append("path");

            Iterator theUserList0 = theUserList.iterator();
            Iterator theUserList1 = theUserList.iterator();
            while (theUserList0.hasNext()) {
                JahiaUser theUser = (JahiaUser) theUserList0.next();
                output.append("\t").append(theUser.getUsername());
            }

            output.append("\n").append(theHomePage.getTitle());

            // display users rights
            output.append(getPageRights(theUserList1, theHomePage));
            output.append(drawChildPages( theHomePage, theUserList ));
        }
        return output.toString();
    }

    private String getPageRights( Iterator theUserList, JahiaPage thePage )
    throws JahiaException {

        StringBuffer output = new StringBuffer();

        // Return User Rights for a Page
        while (theUserList.hasNext()) {
            JahiaUser theUser = (JahiaUser) theUserList.next();
            output.append("\t");
            if (thePage.checkReadAccess(theUser)) {
                output.append("r ");
            }
            if (thePage.checkWriteAccess(theUser)) {
                output.append("w ");
            }
            if (thePage.checkAdminAccess(theUser)) {
                output.append("a");
            }
        }
        return output.append("\n").toString();
    }

    private String drawChildPages( JahiaPage parentPage, List theUserList)
    throws JahiaException {
        StringBuffer output = new StringBuffer();

        if (parentPage != null) {
            // get list of page childs
            List<JahiaPage> pageChilds = parentPage.getChilds(user);

            // parse list of page childs
            for (JahiaPage pageChild : pageChilds) {
                Iterator theUserList1 = theUserList.iterator();
                    // display page path
                    Iterator thePath = pageChild.getPagePath(ProcessingContext.EDIT, user);
                    while (thePath.hasNext()) {
                        JahiaPage thePage = (JahiaPage) thePath.next();
                        output.append(thePage.getTitle());
                        if (thePath.hasNext()) {
                            output.append(" : ");
                        }
                    }

                    // display users rights
                    output.append(getPageRights(theUserList1, pageChild));
                    output.append(drawChildPages( pageChild, theUserList ));
                }
            }

        // return output string
        return output.toString();
    }


}
