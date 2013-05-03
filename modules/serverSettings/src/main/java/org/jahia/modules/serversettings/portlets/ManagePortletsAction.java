/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2013 Jahia Solutions Group SA. All rights reserved.
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

package org.jahia.modules.serversettings.portlets;

import org.apache.commons.io.FileUtils;
import org.jahia.bin.Action;
import org.jahia.bin.ActionResult;
import org.jahia.configuration.deployers.ServerDeploymentInterface;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.URLResolver;
import org.jahia.settings.SettingsBean;
import org.jahia.tools.files.FileUpload;
import org.jahia.utils.i18n.Messages;
import org.jahia.utils.i18n.ResourceBundles;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Action to manage portlets
 */
public class ManagePortletsAction extends Action {

    private static Logger logger = org.slf4j.LoggerFactory.getLogger(ManagePortletsAction.class);

    @Override
    public ActionResult doExecute(HttpServletRequest req, RenderContext renderContext, Resource resource, JCRSessionWrapper session, Map<String, List<String>> parameters, URLResolver urlResolver) throws Exception {
        boolean doDeploy = false;
        if (parameters.containsKey("doDeploy")) {
            doDeploy = Boolean.parseBoolean(parameters.get("doDeploy").get(0));
        }
        boolean doPrepare = false;
        if (parameters.containsKey("doPrepare")) {
            doPrepare = Boolean.parseBoolean(parameters.get("doPrepare").get(0));
        }
        if (!doDeploy && !doPrepare) {
            String dspMsg = Messages.get("resources.JahiaServerSettings", "serverSettings.portlets.deploy.help", session.getLocale());
            return getDisplayMessageResult(dspMsg);
        }
        FileUpload fileUpload = (FileUpload) req.getAttribute(FileUpload.FILEUPLOAD_ATTRIBUTE);
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
                        if (doDeploy) {
                            deployPortlet(generatedFile, fileName);
                        }

                        if (doPrepare && !doDeploy) {
                            String url = req.getContextPath() + "/cms/preparedportlets?war=" + URLEncoder.encode(fileName, "UTF-8") + "&file=" + generatedFile.getName();
                            String dspMsg = Messages.get("resources.JahiaServerSettings", "serverSettings.portletReady", session.getLocale());
                            dspMsg += "<br/><br/>";
                            dspMsg += Messages.get(ResourceBundles.JAHIA_INTERNAL_RESOURCES, "label.download", session.getLocale());
                            dspMsg += ":&nbsp;<a href='" + url + "'>" + fileName + "</a>";
                            return getDisplayMessageResult(dspMsg);
                        } else {
                            String dspMsg = Messages.get("resources.JahiaServerSettings", "serverSettings.portletDeployed", session.getLocale());
                            return getDisplayMessageResult(dspMsg);
                        }


                    }
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                    String dspMsg = Messages.get(ResourceBundles.JAHIA_INTERNAL_RESOURCES, "message.generalError", session.getLocale());
                    return getDisplayMessageResult(dspMsg);
                } finally {
                    FileUtils.deleteQuietly(f);
                    if (!doPrepare || doDeploy) {
                        FileUtils.deleteQuietly(generatedFile);
                    }
                }
            }
        }
        return null;
    }

    private ActionResult getDisplayMessageResult(String dspMsg) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("dspMsg", dspMsg);
        return new ActionResult(HttpServletResponse.SC_OK, null, jsonObject);
    }

    /**
     * Deploy portlet
     *
     * @param file
     * @param filename
     * @throws java.io.IOException
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
