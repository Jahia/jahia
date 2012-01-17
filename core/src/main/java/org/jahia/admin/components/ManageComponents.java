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

package org.jahia.admin.components;

import static org.jahia.bin.JahiaAdministration.JSP_PATH;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.jahia.admin.AbstractAdministrationModule;
import org.jahia.bin.JahiaAdministration;
import org.jahia.data.JahiaData;
import org.jahia.params.ParamBean;
import org.jahia.params.ProcessingContext;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.settings.SettingsBean;
import org.jahia.tools.files.FileUpload;
import org.jahia.configuration.deployers.ServerDeploymentInterface;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.ServletRequestUtils;


/**
 * desc:  This class is used by the administration to manage
 * all the components you've added to your Jahia portal. You can add a
 * component, edit, change the visibility of the component and edit
 * its options. You can also view non-installed components.
 * <p/>
 * Copyright:    Copyright (c) 2002
 * Company:      Jahia Ltd
 *
 * @author Khue N'Guyen
 * @author Alexandre Kraft
 * @version 1.0
 */
public class ManageComponents extends AbstractAdministrationModule {

    private static Logger logger = org.slf4j.LoggerFactory.getLogger(ManageComponents.class);

    private JahiaSite site;
    private JahiaUser user;

    /**
     * Default constructor.
     *
     * @param request  Servlet request.
     * @param response Servlet response.
     */
    public void service(HttpServletRequest request,
                        HttpServletResponse response)
            throws Exception {

        userRequestDispatcher(request, response, request.getSession());
    } // end constructor


    //-------------------------------------------------------------------------
    /**
     * This method is used like a dispatcher for user requests.
     *
     * @param request  Servlet request.
     * @param response Servlet response.
     * @param session  Servlet session for the current user.
     */
    private void userRequestDispatcher(HttpServletRequest request,
                                       HttpServletResponse response,
                                       HttpSession session)
            throws Exception {
        response.setContentType("text/html");
        String operation = request.getParameter("sub");

        // check if the user has really admin access to this site...
        user = (JahiaUser) session.getAttribute(ProcessingContext.SESSION_USER);
        site = (JahiaSite) session.getAttribute(ProcessingContext.SESSION_SITE);

        if (site != null && user != null) {
            // set the new site id to administer...
            request.setAttribute("site", site);
            request.setAttribute("appserverDeployerUrl", SettingsBean.getInstance().getJahiaWebAppsDeployerBaseURL());
            request.setAttribute("autoDeploySupported", Boolean.valueOf(SettingsBean.getInstance().getServerDeployer().isAutoDeploySupported()));

            if (operation.equals("display")) {
                displayComponentList(request, response, session);
            } else if (operation.equals("prepareDeployPortlet")) {
                prepareDeployPortlet(request, response, session);
            } else if (operation.equals("getPreparedWar")) {
                getPreparedWar(request, response, session);
            }

        } else {
            String dspMsg = getMessage("message.generalError");
            request.setAttribute("jahiaDisplayMessage", dspMsg);
            JahiaAdministration.doRedirect(request,
                    response,
                    session, JSP_PATH + "menu.jsp");
        }
    }


    private void getPreparedWar(HttpServletRequest request, HttpServletResponse response, HttpSession session) throws IOException {
        File war = null;
        String fileName = null;
        String warName = null;
        try {
            fileName = ServletRequestUtils.getRequiredStringParameter(request, "file");
            warName = ServletRequestUtils.getStringParameter(request, "war", fileName);
            war = new File(System.getProperty("java.io.tmpdir"), fileName);
        } catch (ServletRequestBindingException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Required parameter war is not found in the request");
            return;
        }
        InputStream is = null;
        try {
            response.setContentType("application/zip");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + warName + "\"");

            is = new BufferedInputStream(new FileInputStream(war));
            IOUtils.copy(is, response.getOutputStream());
        } finally {
            IOUtils.closeQuietly(is);
            FileUtils.deleteQuietly(war);
        }
    }

    /**
     * Display the list of components.
     *
     * @param request  Servlet request.
     * @param response Servlet response.
     * @param session  HttpSession object.
     */
    private void displayComponentList(HttpServletRequest request, HttpServletResponse response, HttpSession session)
            throws IOException, ServletException {
        // handle by a gwt module
        JahiaAdministration.doRedirect(request, response, session, JSP_PATH + "manage_components.jsp");
    }


    private void prepareDeployPortlet(HttpServletRequest request, HttpServletResponse response, HttpSession session)
            throws IOException, ServletException {
        JahiaData jData = (JahiaData) request.getAttribute("org.jahia.data.JahiaData");
        ProcessingContext jParams = null;
        if (jData != null) {
            jParams = jData.getProcessingContext();
        }
        String deploy = jParams.getParameter("doDeploy");
        boolean doDeploy = false;
        if (deploy != null && Boolean.parseBoolean(deploy)) {
            doDeploy = true;
        }

        String prepare = jParams.getParameter("doPrepare");
        boolean doPrepare = false;
        if (prepare != null && Boolean.parseBoolean(prepare)) {
            doPrepare = true;
        }

        if (!doDeploy && !doPrepare) {
            String dspMsg = getMessage("org.jahia.admin.components.ManageComponents.deploy.help");
            response.getWriter().print(dspMsg);
            return;
        }

        FileUpload fileUpload = ((ParamBean) jParams).getFileUpload();
        if (fileUpload != null) {
            Set<String> filesName = fileUpload.getFileNames();
            Iterator<String> iterator = filesName.iterator();
            if (iterator.hasNext()) {
                String n = iterator.next();
                String fileName = fileUpload.getFileSystemName(n);
                File f = fileUpload.getFile(n);
                File generatedFile = null;

                try {
                    generatedFile = f;
                    if (doPrepare) {
                        generatedFile = processUploadedFile(f);
                    }

                    if (generatedFile != null) {
                        // deploy it
                        if (doDeploy) {
                            deployPortlet(generatedFile, fileName);
                        }

                        if (doPrepare && !doDeploy) {
                            String url = JahiaAdministration.composeActionURL(request,response,"sharecomponents","&sub=getPreparedWar&war=" + URLEncoder.encode(fileName, "UTF-8") + "&file=" + generatedFile.getName());
                            String dspMsg = getMessage("message.portletReady");
                            response.getWriter().append(dspMsg).append("<br/><br/>").append(getMessage("label.download")).append(":&nbsp;<a href='").append(url).append("'>").append(fileName).append("</a>");
                        } else {
                            String dspMsg = getMessage("message.portletDeployed");
                            response.getWriter().print(dspMsg);
                        }


                    }
                } catch (Exception e) {
                    String dspMsg = getMessage("message.generalError");
                    response.getWriter().print(dspMsg);
                    logger.error(e.getMessage(), e);
                } finally {
                    FileUtils.deleteQuietly(f);
                    if (!doPrepare || doDeploy) {
                        FileUtils.deleteQuietly(generatedFile);
                    }
                }
            }

        }
    }

    /**
     * Deploy portlet
     *
     * @param file
     * @param filename
     * @throws IOException
     */
    private void deployPortlet(File file, String filename) throws IOException {
        ServerDeploymentInterface deployer = SettingsBean.getInstance().getServerDeployer();
        if (deployer.isAutoDeploySupported()) {
            File target = new File(new File(deployer.getTargetServerDirectory(), deployer.getDeploymentBaseDir()), filename);
            try {
                FileUtils.copyFile(file, target);
            } finally {
                FileUtils.deleteQuietly(file);
            }
            logger.info("Moved " + filename + " to " + target);
        } else {
            logger.info("Server " + SettingsBean.getInstance().getServer() + " "
                    + SettingsBean.getInstance().getServerVersion()
                    + " does not support auto deployment of WAR files. Skipping WAr deployment.");
        }
    }

    /**
     * Prepare uploaded file
     *
     * @param file
     * @return
     * @throws Exception
     */
    private File processUploadedFile(File file) throws Exception {
        return new AssemblerTask(new File(System.getProperty("java.io.tmpdir")), file).execute();
    }

}
