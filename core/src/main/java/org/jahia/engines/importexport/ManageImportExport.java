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
package org.jahia.engines.importexport;

import java.io.File;
import java.io.FileInputStream;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.Status;

import org.jahia.content.ContentObject;
import org.jahia.content.StructuralRelationship;
import org.jahia.engines.JahiaEngine;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaSessionExpirationException;
import org.jahia.params.ParamBean;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.EnginesRegistry;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.importexport.ImportExportService;
import org.jahia.services.importexport.ImportJob;
import org.jahia.services.sites.SiteLanguageSettings;
import org.jahia.services.version.EntryLoadRequest;
import org.jahia.tools.files.FileUpload;

/**
 * Created by IntelliJ IDEA.
 * Date: Nov 20, 2003
 * Copyright Codeva 2003
 *
 * @author Thomas Draier
 * @version $Id$
 */
public class ManageImportExport {
    private static final org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger(ManageImportExport.class);

    private static ManageImportExport instance = null;
    private static final String JSP_IMPORT_FILE = "/engines/importexport/import.jsp";
    private static final String JSP_CLOSE_FILE = "/engines/importexport/close.jsp";
    private static final String JSP_EXPORT_FILE = "/engines/importexport/export.jsp";
    //private static final String JSP_XML_FILE = "/engines/importexport/xml.jsp";

    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMddHHmmss");

    /**
     * @return a single instance of the object
     */
    public static synchronized ManageImportExport getInstance() {
        if (instance == null) {
            instance = new ManageImportExport();
        }
        return instance;
    }

    public boolean handleActions(ProcessingContext jParams, int mode, Map engineMap, ContentObject object)
            throws JahiaException, JahiaSessionExpirationException {
        switch (mode) {
            case (JahiaEngine.LOAD_MODE) :
                return load(jParams, engineMap, object);
            case (JahiaEngine.SAVE_MODE) :
                return save(jParams, engineMap, object);
        }
        return false;
    }

    public boolean load(ProcessingContext jParams, Map engineMap, ContentObject object)
            throws JahiaException, JahiaSessionExpirationException {

        String theScreen = (String) engineMap.get("screen");

        engineMap.put("fieldsEditCallingEngineName", "importexport_engine");

        if (ImportJob.IMPORT_TYPE.equals(theScreen)) {
            engineMap.put("importexport_engine.fieldForm", ServicesRegistry.getInstance().
                    getJahiaFetcherService().fetchServlet((ParamBean) jParams, JSP_IMPORT_FILE));
        } else {
//            String engineUrl = (String) engineMap.get(JahiaEngine.ENGINE_URL_PARAM);
//            int endIndex = engineUrl.indexOf('?');
//            if (endIndex == -1) {
//                engineUrl += "/export.zip?lastscreen=export";
//            } else {
//                engineUrl = engineUrl.substring(0,endIndex) + "/export.zip" + engineUrl.substring(endIndex) + "&lastscreen=importexport";
//            }
//            engineMap.put(JahiaEngine.ENGINE_URL_PARAM, engineUrl);

            engineMap.put("exportUrl", EnginesRegistry.getInstance().getEngineByBeanName("exportEngine").renderLink(jParams, object));
            engineMap.put("objectKey", object.getObjectKey().toString());
            // EXPORT
            engineMap.put("hasActiveEntries", Boolean.valueOf(object.hasActiveEntries()));
            engineMap.put("importexport_engine.fieldForm", ServicesRegistry.getInstance().
                    getJahiaFetcherService().fetchServlet((ParamBean) jParams, JSP_EXPORT_FILE));
        }

        return true;
    }

    public boolean save(ProcessingContext jParams, Map engineMap, ContentObject object)
            throws JahiaException, JahiaSessionExpirationException {

        ImportExportService ie = ServicesRegistry.getInstance().getImportExportService();
        engineMap.put("fieldsEditCallingEngineName", "importexport_engine");

        if (ProcessingContext.isMultipartRequest(((ParamBean) jParams).getRequest())) {
            final boolean importedOne = importFile(object, false, jParams);

            if (!importedOne) {
                return true;
            }
            jParams.getSessionState().setAttribute("FireContainerUpdated", "true");
            logger.debug("InvalidateEsiInvalidateEsiInvalidateEsiInvalidateEsiInvalidateEsiInvalidateEsi");

            String theScreen = (String)engineMap.get("screen");
            if ( !"apply".equals(theScreen) ){
                engineMap.put("importexport_engine.fieldForm", ServicesRegistry.getInstance().
                        getJahiaFetcherService().fetchServlet(((ParamBean) jParams), JSP_CLOSE_FILE));
            } else {
                return load(jParams, engineMap, object);
            }
            return true;
        } else {

            List siteLanguageSettings = jParams.getSite().getLanguageSettings();
            Set languageCodes = new HashSet();
            if (siteLanguageSettings != null) {
                for (int i = 0; i < siteLanguageSettings.size(); i++) {
                    SiteLanguageSettings curSetting = (SiteLanguageSettings)
                            siteLanguageSettings.get(i);
                    if (curSetting.isActivated()) {
                        languageCodes.add(curSetting.getCode());
                    }
                }
            }

            Map params = new HashMap();
            params.put(ImportExportService.LINK, StructuralRelationship.ACTIVATION_PICKER_LINK);

            // EXPORT
            if ("diff".equals(jParams.getParameter("exporttype"))) {
                params.put(ImportExportService.FROM, EntryLoadRequest.CURRENT);
                params.put(ImportExportService.TO, EntryLoadRequest.STAGED);
            } else if ("staging".equals(jParams.getParameter("exporttype"))) {
                params.put(ImportExportService.TO, EntryLoadRequest.STAGED);
            }
            HttpServletResponse resp = ((ParamBean) jParams).getResponse();
            try {
                ServletOutputStream outputStream = resp.getOutputStream();
                if ("zipfiles".equals(jParams.getParameter("exportformat"))) {
                    resp.setContentType("application/zip");
                    params.put(ImportExportService.INCLUDE_FILES, Boolean.TRUE);
                    ie.exportZip(object, languageCodes, outputStream, jParams, params);
                } else if ("zipnofiles".equals(jParams.getParameter("exportformat"))) {
                    resp.setContentType("application/zip");
                    ie.exportZip(object, languageCodes, outputStream, jParams, params);
                } else {
                    resp.setContentType("text/xml");
                    ie.exportFile(object, jParams.getLocale().toString(), outputStream, jParams, params);
                }
                outputStream.close();
                engineMap.put(JahiaEngine.RENDER_TYPE_PARAM, new Integer(-1));
            } catch (Exception e) {
                logger.error("Export error", e);
            }
        }

        return false;
    }

    private boolean importFile(ContentObject target, boolean diff, ProcessingContext jParams) {
        if (ProcessingContext.isMultipartRequest(((ParamBean) jParams).getRequest())) {

            final String dkey = target.getObjectKey().toString();

            try {
                final FileUpload fupload = ((ParamBean) jParams).getFileUpload();

                // get files name
                Set filesName = fupload.getFileNames();

                boolean result = false;
                for (Iterator iterator = filesName.iterator(); iterator.hasNext();) {
                    String name = (String) iterator.next();
                    String systemName = fupload.getFileSystemName(name);
                    if (systemName == null) {
                        continue;
                    }
                    result = true;

                    final File file = fupload.getFile(name);
                    String contentType = URLConnection.getFileNameMap().getContentTypeFor(systemName);
                    String ext = systemName.substring(systemName.lastIndexOf("."));
                    String dateOfExport = DATE_FORMAT.format(new Date());

                    List l = ServicesRegistry.getInstance().getJCRStoreService().getImportDropBoxes(null,jParams.getUser());
                    JCRNodeWrapper importFolder = (JCRNodeWrapper) l.iterator().next();

                    String uploadname;
                    if ("text/xml".equals(contentType) || "application/xml".equals(contentType)) {
                        String s = jParams.getCurrentLocale().toString();
                        s = s.replace("_","-");
                        uploadname = "importInto_" + dkey + "_" + s + "_" + dateOfExport + ext;
                    } else {
                        uploadname = "importInto_" + dkey + "_" + dateOfExport + ext;
                    }
                    
                    try {
                        importFolder.uploadFile(uploadname, new FileInputStream(file),
                                contentType);
                        importFolder.save();
                    } finally {
                        if (importFolder.getTransactionStatus() == Status.STATUS_ACTIVE) {
                            importFolder.refresh(false);
                        }
                    }                    
                }
                return result;
            } catch (Exception e) {
                logger.error("Error", e);
            }
        }
        return false;
    }
}
