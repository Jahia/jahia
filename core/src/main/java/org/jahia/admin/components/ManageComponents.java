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
package org.jahia.admin.components;

import org.apache.commons.io.FileUtils;
import org.jahia.admin.AbstractAdministrationModule;
import org.jahia.bin.Jahia;
import org.jahia.bin.JahiaAdministration;
import org.jahia.data.JahiaData;
import org.jahia.params.ParamBean;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.security.license.License;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRStoreService;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.settings.SettingsBean;
import org.jahia.tools.files.FileUpload;
import org.jahia.utils.i18n.JahiaResourceBundle;

import javax.jcr.RepositoryException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Set;


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

    /**
     * logging
     */
    private static org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger(ManageComponents.class);

    private static final String CLASS_NAME = JahiaAdministration.CLASS_NAME;
    private static final String JSP_PATH = JahiaAdministration.JSP_PATH;

    private JahiaSite site;
    private JahiaUser user;
    private ServicesRegistry sReg;
    private JCRStoreService jcr;

    private License coreLicense;

    /**
     * Default constructor.
     *
     * @param request  Servlet request.
     * @param response Servlet response.
     */
    public void service(HttpServletRequest request,
                        HttpServletResponse response)
            throws Exception {

        JahiaData jData = (JahiaData) request.getAttribute("org.jahia.data.JahiaData");
        ProcessingContext jParams = null;
        if (jData != null) {
            jParams = jData.getProcessingContext();
        }
        coreLicense = Jahia.getCoreLicense();
        if (coreLicense == null) {
            // set request attributes...
            String dspMsg = JahiaResourceBundle.getJahiaInternalResource("org.jahia.admin.JahiaDisplayMessage.invalidLicense.label",
                    jParams.getLocale());
            request.setAttribute("jahiaDisplayMessage", dspMsg);
            // redirect...
            JahiaAdministration.doRedirect(request, response, request.getSession(), JSP_PATH + "menu.jsp");
            return;
        }

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

        sReg = ServicesRegistry.getInstance();
        jcr = sReg.getJCRStoreService();

        // check if the user has really admin access to this site...
        user = (JahiaUser) session.getAttribute(ProcessingContext.SESSION_USER);
        site = (JahiaSite) session.getAttribute(ProcessingContext.SESSION_SITE);

        JahiaData jData = (JahiaData) request.getAttribute("org.jahia.data.JahiaData");
        ProcessingContext jParams = null;
        if (jData != null) {
            jParams = jData.getProcessingContext();
        }

        if (site != null && user != null && sReg != null) {
            // set the new site id to administrate...
            request.setAttribute("site", site);
            request.setAttribute("appserverDeployerUrl", SettingsBean.getInstance().getJahiaWebAppsDeployerBaseURL());
            String serverType = SettingsBean.getInstance().getServer();
            if (serverType != null && serverType.equalsIgnoreCase("Tomcat")) {
                request.setAttribute("isTomcat", Boolean.TRUE);
            } else {
                request.setAttribute("isTomcat", Boolean.FALSE);
            }


            if (operation.equals("display")) {
                displayComponentList(request, response, session);
            } else if (operation.equals("prepareDeployPortlet")) {
                prepareDeployPortlet(request, response, session);
            }

        } else {
            String dspMsg = JahiaResourceBundle.getJahiaInternalResource("org.jahia.admin.JahiaDisplayMessage.requestProcessingError.label",
                    jParams.getLocale());
            request.setAttribute("jahiaDisplayMessage", dspMsg);
            JahiaAdministration.doRedirect(request,
                    response,
                    session, JSP_PATH + "menu.jsp");
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
            String dspMsg = JahiaResourceBundle.getJahiaInternalResource("org.jahia.admin.components.ManageComponents.deploy.help", jParams.getLocale());
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

                try {
                    File generatedFile = f;
                    if (doPrepare) {
                        generatedFile = processUploadedFile(f);
                    }

                    if (generatedFile != null) {
                        // save it in the JCR
                        String url = writeToDisk(user, generatedFile, SettingsBean.getInstance().getJahiaPreparePortletJCRPath(), fileName);

                        // deploy it
                        if (doDeploy) {
                            deployPortlet(generatedFile, fileName);
                        }


                        if (doPrepare && !doDeploy) {
                            String dspMsg = JahiaResourceBundle.getJahiaInternalResource("message.portletReady", jParams.getLocale());
                            String html = dspMsg + "<br/>" + JahiaResourceBundle.getJahiaInternalResource("label.download", jParams.getLocale()) + "<a href='" + url + "'> " + fileName + "</a>";
                            response.getWriter().print(html);
                        } else {
                            String dspMsg = JahiaResourceBundle.getJahiaInternalResource("org.jahia.admin.components.ManageComponents.portletDeployed.label", jParams.getLocale());
                            response.getWriter().print(dspMsg);
                        }


                    }
                } catch (Exception e) {
                    String dspMsg = JahiaResourceBundle.getJahiaInternalResource("org.jahia.admin.JahiaDisplayMessage.requestProcessingError.label", jParams.getLocale());
                    response.getWriter().print(dspMsg);
                    logger.error(e, e);
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
        // ugly: To do: rewrite deploy service with cargo.
        // deploy: the easy way is to copy in tomcat/webapp.
        String serverType = SettingsBean.getInstance().getServer();
        if (serverType != null && serverType.equalsIgnoreCase("Tomcat")) {
            String newName = SettingsBean.getInstance().getJahiaWebAppsDiskPath() + filename;
            FileUtils.copyFile(file, new File(newName));
            logger.info("Copy " + filename + " to " + SettingsBean.getInstance().getJahiaWebAppsDiskPath() + ". Waiting for tomcat. app deployment.");
        } else {
            logger.debug("Server: " + serverType);
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
        AssemblerTask task = new AssemblerTask(getTempDir(), file);
        return task.execute();
    }

    /**
     * Write to disk
     *
     * @param user
     * @param item
     * @param location
     * @param filename
     * @return
     * @throws IOException
     */
    private String writeToDisk(JahiaUser user, File item, String location, String filename) throws IOException {
        if (logger.isDebugEnabled()) {
            logger.debug("item : " + item);
            logger.debug("destination : " + location);
            logger.debug("filename : " + filename);
        }
        if (item == null || location == null || filename == null) {
            return null;
        }


        JCRNodeWrapper locationFolder = jcr.getFileNode(location, user);

        locationFolder.getUrl();

        if (!locationFolder.isWriteable()) {
            logger.debug("destination is not writable for user " + user.getName());
            return null;
        }
        JCRNodeWrapper result;
        try {
            InputStream is = new FileInputStream(item);
            result = locationFolder.uploadFile(filename, is, "multipart/alternative");
            is.close();
            locationFolder.save();
        } catch (RepositoryException e) {
            logger.error("exception ", e);
            return null;
        }
        return result.getUrl();
    }

    /**
     * Get temp dir
     *
     * @return
     * @throws IOException
     */
    private File getTempDir() throws IOException {
        final File tempFile = File.createTempFile("DoesNotMatter", "generated-portlets");
        tempFile.delete();
        final File tempDir = tempFile.getParentFile();
        return tempDir;
    }
}
