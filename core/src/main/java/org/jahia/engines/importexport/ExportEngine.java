/**
 * Jahia Enterprise Edition v6
 *
 * Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.
 *
 * Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
 * with Document Management and Portal features.
 *
 * The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED.
 *
 * Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
 * you and Jahia (Jahia Sustainable Enterprise License - JSEL).
 *
 * If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.engines.importexport;

import org.apache.log4j.Logger;
import org.jahia.content.ContentObject;
import org.jahia.content.ContentObjectKey;
import org.jahia.data.JahiaData;
import org.jahia.engines.EngineToolBox;
import org.jahia.engines.JahiaEngine;
import org.jahia.engines.validation.EngineValidationHelper;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ParamBean;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.importexport.ImportExportService;
import org.jahia.services.lock.LockKey;
import org.jahia.services.lock.LockRegistry;
import org.jahia.services.lock.LockPrerequisites;
import org.jahia.services.sites.SiteLanguageSettings;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.version.EntryLoadRequest;
import org.jahia.bin.JahiaAdministration;
import org.xml.sax.SAXException;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.net.HttpURLConnection;
import java.util.*;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: 13 sept. 2005
 * Time: 12:03:42
 * To change this template use File | Settings | File Templates.
 */
public class ExportEngine implements JahiaEngine {
    private static Logger logger = Logger.getLogger(ExportEngine.class);
    public static final String ENGINE_NAME = "export";

    private EngineToolBox toolBox;

    public ExportEngine() {
        toolBox = EngineToolBox.getInstance();
    }

    public boolean authoriseRender(ProcessingContext processingContext) {
        return true;
    }

    public String getName() {
        return ENGINE_NAME;
    }

    public String renderLink(ProcessingContext processingContext, Object theObj) throws JahiaException {
//        ContentObject object = (ContentObject) theObj;
        return processingContext.composeEngineUrl(ENGINE_NAME);
    }

    public boolean needsJahiaData(ProcessingContext processingContext) {
        return false;
    }

    public EngineValidationHelper handleActions(ProcessingContext processingContext, JahiaData jData) throws JahiaException {
        try {
            Map engineMap = new HashMap();

            export(processingContext);
            engineMap.put(JahiaEngine.RENDER_TYPE_PARAM, new Integer(-1));

            toolBox.displayScreen(processingContext, (Map) engineMap);
        } catch (Exception e) {
            throw new JahiaException("Exception occured", "Exception occured", JahiaException.ENGINE_ERROR, JahiaException.ERROR_SEVERITY, e);
        }
        return null;
    }

    private void export(ProcessingContext processingContext) throws Exception {
        ParamBean jParams = (ParamBean) processingContext;
        HttpServletResponse resp = jParams.getRealResponse();
        ServletOutputStream outputStream = resp.getOutputStream();
        //make sure this file is not cached by the client (or a proxy middleman)
        resp.setHeader("Expires", "Thu, 01 Jan 1970 00:00:00 GMT");
        resp.setHeader("Pragma", "no-cache");
        resp.setHeader("Cache-Control", "no-cache");

        Map params = new HashMap();

        params.put(ImportExportService.VIEW_CONTENT, Boolean.valueOf(!"false".equals(processingContext.getParameter("viewContent"))));
        params.put(ImportExportService.VIEW_VERSION, Boolean.valueOf("true".equals(processingContext.getParameter("viewVersion"))));
        params.put(ImportExportService.VIEW_ACL, Boolean.valueOf(!"false".equals(processingContext.getParameter("viewAcl"))));
        params.put(ImportExportService.VIEW_METADATA, Boolean.valueOf(!"false".equals(processingContext.getParameter("viewMetadata"))));
        params.put(ImportExportService.VIEW_JAHIALINKS, Boolean.valueOf(!"false".equals(processingContext.getParameter("viewLinks"))));
        params.put(ImportExportService.VIEW_WORKFLOW, Boolean.valueOf("true".equals(processingContext.getParameter("viewWorkflow"))));
        boolean enforceLanguage = "true".equals(processingContext.getParameter("enforceLanguage"));


        String key = processingContext.getParameter("key");
        ContentObject object;
        if (key != null) {
            object = ContentObject.getContentObjectInstance(ContentObjectKey.getInstance(key));
        } else {
            object = processingContext.getSite().getHomeContentPage();
        }

        if (object == null) {
            resp.setStatus(HttpURLConnection.HTTP_NOT_FOUND);
            return;
        }

        if (!object.checkReadAccess(processingContext.getUser())) {
            resp.setStatus(HttpURLConnection.HTTP_UNAUTHORIZED);
            return;
        }


        String locker = processingContext.getParameter("lock");
        if (locker != null && object.checkWriteAccess(processingContext.getUser())) {
            LockKey lock = LockKey.composeLockKey(LockKey.IMPORT_ACTION + "_" + object.getObjectKey().getType(), object.getID(), object.getID());
            if (!LockRegistry.getInstance().acquire(lock, processingContext.getUser(), locker, processingContext.getSessionState().getMaxInactiveInterval())) {
                resp.setStatus(HttpURLConnection.HTTP_FORBIDDEN);
                logger.warn("Cannot acquire lock");
                return;
            } else {
                logger.warn("Lock acquired");
            }
        } else {
            String checklocker = processingContext.getParameter("checklock");
            if (checklocker != null) {
                LockKey lock = LockKey.composeLockKey(LockKey.IMPORT_ACTION + "_" + object.getObjectKey().getType(), object.getID(), object.getID());
                if (LockPrerequisites.getInstance().isLockAcquirable(lock, processingContext.getUser(), checklocker, false)) {
                    resp.setStatus(HttpURLConnection.HTTP_OK);
                } else {
                    resp.setStatus(HttpURLConnection.HTTP_FORBIDDEN);
                }
                return;
            }
        }
        String unlock = processingContext.getParameter("unlock");
        if (unlock != null && object.checkWriteAccess(processingContext.getUser())) {
            LockKey lock = LockKey.composeLockKey(LockKey.IMPORT_ACTION + "_" + object.getObjectKey().getType(), object.getID(), object.getID());
            if (LockRegistry.getInstance().isAlreadyAcquired(lock)) {
                LockRegistry.getInstance().release(lock, processingContext.getUser(), unlock);
            }
            return;
        }

        try {
            ImportExportService ie = ServicesRegistry.getInstance().getImportExportService();

            if ("all".equals(processingContext.getParameter("exportformat"))) {
                if (!processingContext.getUser().isRoot()) {
                    resp.setStatus(HttpURLConnection.HTTP_UNAUTHORIZED);
                    return;
                }

                resp.setContentType("application/zip");
                params.put(ImportExportService.INCLUDE_ALL_FILES, Boolean.TRUE);
                params.put(ImportExportService.INCLUDE_TEMPLATES, Boolean.TRUE);
                params.put(ImportExportService.INCLUDE_SITE_INFOS, Boolean.TRUE);
                params.put(ImportExportService.INCLUDE_DEFINITIONS, Boolean.TRUE);
                params.put(ImportExportService.VIEW_WORKFLOW, Boolean.TRUE);
                params.put(ImportExportService.VIEW_PID, Boolean.TRUE);

                ie.exportAll(outputStream, params, processingContext);
                outputStream.close();
                return;
            } else if ("site".equals(processingContext.getParameter("exportformat"))) {
                if (!processingContext.getUser().isRoot()) {
                    resp.setStatus(HttpURLConnection.HTTP_UNAUTHORIZED);
                    return;
                }

                List sites = new ArrayList();
                String[] sitekeys = processingContext.getParameterValues("sitebox");
                if (sitekeys != null) {
                    for (int i = 0; i < sitekeys.length; i++) {
                        String sitekey = sitekeys[i];
                        JahiaSite site = ServicesRegistry.getInstance().getJahiaSitesService().getSiteByKey(sitekey);
                        sites.add(site);
                    }
                }

                if (sites.isEmpty()) {
                    JahiaAdministration.doRedirect(jParams.getRequest(), jParams.getResponse(), jParams.getSession(),
                            JahiaAdministration.JSP_PATH + "no_sites_selected.jsp");
                } else {
                    resp.setContentType("application/zip");
                    params.put(ImportExportService.INCLUDE_ALL_FILES, Boolean.TRUE);
                    params.put(ImportExportService.INCLUDE_TEMPLATES, Boolean.TRUE);
                    params.put(ImportExportService.INCLUDE_SITE_INFOS, Boolean.TRUE);
                    params.put(ImportExportService.INCLUDE_DEFINITIONS, Boolean.TRUE);
                    params.put(ImportExportService.VIEW_WORKFLOW, Boolean.TRUE);
                    params.put(ImportExportService.VIEW_PID, Boolean.TRUE);

                    ie.exportSites(outputStream, params, processingContext, sites);
                    outputStream.close();
                }
                return;
            } else if ("cats".equals(processingContext.getParameter("exportformat"))) {
                resp.setContentType("text/xml");

                ie.exportCategories(outputStream, processingContext);
                outputStream.close();
                return;
            }

            List siteLanguageSettings = processingContext.getSite().getLanguageSettings();
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

            if (enforceLanguage && processingContext.getParameter("lang") != null) {
                if (!processingContext.getParameter("lang").equals(processingContext.getLocale().toString())) {
                    resp.setStatus(HttpURLConnection.HTTP_NOT_FOUND);
                    return;
                }
            }

            if ("versions".equals(processingContext.getParameter("exportformat"))) {
                resp.setContentType("text/xml");

                ie.exportVersions(outputStream, processingContext);
                outputStream.close();
                return;
            }

            if ("diff".equals(processingContext.getParameter("exporttype"))) {
                params.put(ImportExportService.FROM, EntryLoadRequest.CURRENT);
                params.put(ImportExportService.TO, EntryLoadRequest.STAGED);
            } else if ("staging".equals(processingContext.getParameter("exporttype"))) {
                params.put(ImportExportService.TO, EntryLoadRequest.STAGED);
            }
            if ("zipfiles".equals(processingContext.getParameter("exportformat"))) {
                resp.setContentType("application/zip");
                params.put(ImportExportService.INCLUDE_FILES, Boolean.TRUE);
                ie.exportZip(object, languageCodes, outputStream, processingContext, params);
            } else if ("zipallfiles".equals(processingContext.getParameter("exportformat"))) {
                resp.setContentType("application/zip");
                params.put(ImportExportService.INCLUDE_ALL_FILES, Boolean.TRUE);
                ie.exportZip(object, languageCodes, outputStream, processingContext, params);
            } else if ("zipnofiles".equals(processingContext.getParameter("exportformat"))) {
                resp.setContentType("application/zip");
                ie.exportZip(object, languageCodes, outputStream, processingContext, params);
            } else if ("doc".equals(processingContext.getParameter("exportformat"))) {
                resp.setContentType("text/xml");
                params.put(ImportExportService.EXPORT_FORMAT, ImportExportService.DOCUMENT_EXPORTER);
                ie.exportFile(object, processingContext.getLocale().toString(), outputStream, processingContext, params);
            } else {
                resp.setContentType("text/xml");
                ie.exportFile(object, processingContext.getLocale().toString(), outputStream, processingContext, params);
            }
            outputStream.close();
        } catch (Exception e) {
            logger.error("Exception during export", e);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
