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
package org.jahia.engines.shared;

import org.jahia.data.containers.JahiaContainer;
import org.jahia.data.fields.FieldsEditHelper;
import org.jahia.data.fields.FieldsEditHelperAbstract;
import org.jahia.data.fields.JahiaField;
import org.jahia.data.files.JahiaFileField;
import org.jahia.engines.JahiaEngine;
import org.jahia.engines.JahiaEngineTools;
import org.jahia.engines.EngineLanguageHelper;
import org.jahia.engines.filemanager.TableEntry;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ParamBean;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.acl.JahiaBaseACL;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.webdav.JahiaWebdavBaseService;
import org.jahia.services.lock.LockPrerequisitesResult;
import org.jahia.services.lock.LockPrerequisites;
import org.jahia.services.lock.LockKey;

import javax.servlet.http.HttpSession;
import javax.transaction.Status;
import javax.jcr.RepositoryException;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Jun 27, 2003
 * Time: 2:45:58 PM
 * To change this template use Options | File Templates.
 */
public class DAVFile_Field implements FieldSubEngine {


    private static DAVFile_Field theObject = null;
    private static final String TREE_MANAGER_JSP = "/engines/shared/embedded_filemanager.jsp";
    public static final String READONLY_JSP = "/engines/shared/readonly_davfile_field.jsp";

    private static final org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger(DAVFile_Field.class);

    /**
     * getInstance
     * NK    21.01.2001
     */
    public static synchronized DAVFile_Field getInstance() {
        if (theObject == null) {
            theObject = new DAVFile_Field();
        }
        return theObject;
    } // end getInstance


    /**
     * handles the field actions
     *
     * @param jParams a ProcessingContext object
     * @return true if everything went okay, false if not
     * @see org.jahia.engines.JahiaEngine
     */
    public boolean handleField(final ProcessingContext jParams, final Integer modeInt, final Map<String, Object> engineMap)
            throws JahiaException {
        int mode = modeInt.intValue();
        final String fieldsEditCallingEngineName = (String) engineMap.get("fieldsEditCallingEngineName");
        final JahiaField theField = (JahiaField) engineMap.get(fieldsEditCallingEngineName + "." + "theField");
        engineMap.put("filemanagerUrl", jParams.composeEngineUrl("filemanager", ""));
        if  (logger.isDebugEnabled()) {
            logger.debug("FieldID: " + theField.getID());
            logger.debug("mode is: " + mode);
        }

        if (mode != JahiaEngine.SAVE_MODE) {
            final String theScreen = jParams.getParameter("screen");
            final String lastScreen = jParams.getParameter("lastscreen");
            if (theScreen == null || lastScreen == null) {
                mode = JahiaEngine.LOAD_MODE;
            }
        }

        switch (mode) {
            case (JahiaEngine.LOAD_MODE)            :
                return composeEngineMap(jParams, engineMap, theField);
            case (JahiaEngine.UPDATE_MODE)          :
                return getFormData(jParams, engineMap, theField);
            case (JahiaEngine.SAVE_MODE)            :
                return saveData(jParams, theField);
        }
        return false;
    } // end handleField


    /**
     * gets POST data from the form and saves it in session
     *
     * @param jParams   a ProcessingContext object
     * @param engineMap the engine hashmap
     * @param theField  the field we are working on
     * @return true if everything went okay, false if not
     */
    private boolean getFormData(final ProcessingContext jParams, final Map<String, Object> engineMap, final JahiaField theField) {
        boolean out = true;

        final String fileID = TableEntry.javascriptDecode(jParams.getParameter("file_id"));
        JahiaFileField fField;

        final HttpSession session = ((ParamBean) jParams).getRequest().getSession();
        final String selectedFile = (String) session.getAttribute("selectedFile");
        final String path;
        if (selectedFile != null && (fileID == null || fileID.length() == 0)) {
            path = selectedFile;
        } else {
            path = fileID;
        }
        if (selectedFile != null) {
            session.removeAttribute("selectedFile");
        }

        final JCRNodeWrapper object = JahiaWebdavBaseService.getInstance().getDAVFileAccess(
                path, jParams.getUser());

        fField = object.getJahiaFileField();
        theField.setValue(fileID);
        theField.setObject(fField);

        final String fieldsEditCallingEngineName = (String) engineMap.get("fieldsEditCallingEngineName");
        final JahiaContainer theContainer = (JahiaContainer) engineMap.get(fieldsEditCallingEngineName + "."
                + "theContainer");
        if (object.isValid()) {
            engineMap.put("deniedUsers", object.comparePermsWithField(theField, theContainer));
            engineMap.put("canAdmin", Boolean.valueOf(object.hasPermission(JCRNodeWrapper.MANAGE)));
            jParams.setParameter("select-file",object.getPath());
        } else {
            jParams.setParameter("select-file", JahiaField.NULL_STRING_MARKER);
            engineMap.remove("deniedUsers");
            engineMap.remove("canAdmin");
        }

        //logger.debug ("activefolderID=" + activeFolderID );
        engineMap.put("fileID", fileID);

//        String fixRights = jParams.getParameter("fixrights");
//        if (fixRights != null && fixRights.equals("on")) {
        final Set<String> uris = new HashSet<String>();
        final String[] users = jParams.getParameterValues("user");
        if (users != null) {
            for (int i = 0; i < users.length; i++) {
                uris.add(users[i]);
            }
        }
        if (!uris.isEmpty()) {
            try {
                object.alignPermsWithField(theField, uris);
                try {
                    object.save();
                } catch (RepositoryException e) {
                    logger.error("error",e);                    
                }
            } finally {
                if (object.getTransactionStatus() == Status.STATUS_ACTIVE) {
                    try {
                        object.refresh(false);
                    } catch (RepositoryException e) {
                        logger.error("error",e);
                    }
                }
            }                
        }
//        }

        //logger.debug ("getFormData for " +  jParams.getEngine() + " fileTitle is " + fileTitle);

        if (jParams.getParameter("ignoreWarnings") == null) {
            if (engineMap.containsKey("deniedUsers") && ! "cancel".equals(jParams.getParameter("screen"))) {
                final Set<String> deniedUsers = (Set<String>) engineMap.get("deniedUsers");
                if (deniedUsers.size() > 0) {
                    composeFileField(jParams, engineMap, theField);
                    final EngineLanguageHelper elh = (EngineLanguageHelper)
                            engineMap.get(JahiaEngine.ENGINE_LANGUAGE_HELPER);
                    jParams.setParameter (EngineLanguageHelper.ENGINE_LANG_PARAM,elh.getPreviousLanguageCode());
                    elh.update(jParams);
                    return false;
                }
            }
        }

        return out;
    } // end getFormData


    /**
     * saves data in datasource
     *
     * @param jParams  a ProcessingContext object
     * @param theField the field we are working on
     * @return true if everything went okay, false if not
     */
    private boolean saveData(final ProcessingContext jParams, final JahiaField theField) throws JahiaException {
        //logger.debug ("started ");
        final JahiaFileField fField = (JahiaFileField) theField.getObject();
        if (fField != null) {
            //fField.setFileFieldTitle(fileTitle);
            //fField.setFileID(fileID);
            //theField.setObject(fField);
            // 0 for parentAclID in saveField, because field already exists
            //  -> field already has an aclID
            //  -> no need to create a new one

            //logger.debug ("fieldID= " + theField.getID());
            return theField.save(jParams);
            //ServicesRegistry.getInstance().getJahiaFieldService().saveField( theField, 0, jParams );
        }

        //engineMap.put( "engineOutputFile", JahiaEngine.CLOSE_JSP );

        return true;
    } // end saveData

    /**
     * composes engine hash map
     *
     * @param jParams   a ProcessingContext object
     * @param engineMap the engine hashmap
     * @param theField  the field we are working on
     * @return true if everything went okay, false if not
     */
    private boolean composeEngineMap(final ProcessingContext jParams, final Map<String, Object> engineMap, final JahiaField theField)
            throws JahiaException {

//        List list = checkRights(jParams, theField);
        if (jParams.getSessionState().getAttribute(jParams.getSiteID() + "-justUploaded") != null) {
            jParams.setParameter("file_id", TableEntry.javascriptEncode((String) jParams.getSessionState().
                    getAttribute(jParams.getSiteID() + "-justUploaded")));
            jParams.getSessionState().removeAttribute(jParams.getSiteID() + "-justUploaded");
            //getFormData(jParams, engineMap, theField);
        }
        jParams.getSessionState().removeAttribute("selectedFile");
        final JahiaFileField fField = composeFileField(jParams, engineMap, theField);
        final String fieldsEditCallingEngineName = (String) engineMap.get("fieldsEditCallingEngineName");

        boolean editable = false;
        final JahiaContainer theContainer = (JahiaContainer) engineMap.get(fieldsEditCallingEngineName + "." + "theContainer");
        if (theContainer == null) {
            // in case of a field , not a field in a container
            editable = true;
        } else {
            final FieldsEditHelper feh = (FieldsEditHelper) engineMap.get(fieldsEditCallingEngineName + "."
                    + FieldsEditHelperAbstract.FIELDS_EDIT_HELPER_CONTEXTID);
            final Map<Integer, Integer> ctnListFieldAcls = feh.getCtnListFieldAcls();
            int fieldId = theField.getID();
            if (theContainer.getListID() != 0 && ctnListFieldAcls != null && ctnListFieldAcls.size() > 0) {
                final JahiaBaseACL acl = JahiaEngineTools.getCtnListFieldACL(ctnListFieldAcls, fieldId);
                if (acl != null) {
                    editable = acl.getPermission(jParams.getUser(), JahiaBaseACL.WRITE_RIGHTS, JahiaEngineTools.isCtnListFieldACLDefined(ctnListFieldAcls, fieldId));
                }
            } else {
                editable = true;
            }
        }

        String output;

        //logger.debug ("composeEngineMap for " +  jParams.getEngine() );
        // set the object
        theField.setObject(fField);
        final String davAction = jParams.getParameter("davAction");

        final JCRNodeWrapper rm = JahiaWebdavBaseService.getInstance().getDAVFileAccess(
                fField.getRealName(), jParams.getUser());
        if (rm.isValid() && davAction == null) {
            engineMap.put("deniedUsers", rm.comparePermsWithField(theField, theContainer));
            engineMap.put("canAdmin", Boolean.valueOf(rm.hasPermission(JCRNodeWrapper.MANAGE)));
            jParams.setParameter("select-file", rm.getPath());
        } else {
            jParams.setParameter("select-file", JahiaField.NULL_STRING_MARKER);
            engineMap.remove("deniedUsers");
            engineMap.remove("canAdmin");
        }

        if  (logger.isDebugEnabled()) {
            logger.debug("select-file: " + jParams.getParameter("select-file"));
        }

        String viewType = jParams.getParameter("viewtype");
        if (viewType != null) {
            jParams.getSessionState().setAttribute("viewtype", viewType);
        }

        jParams.setAttribute("ZimbraInclude", "true");
        engineMap.put("fileManagerEngineUrl", jParams.composeEngineUrl("filemanager"));

        final LockPrerequisitesResult results = LockPrerequisites.getInstance().getLockPrerequisitesResult((LockKey) engineMap.get("LockKey"));
        final String screen = (String) engineMap.get("screen");
        boolean isLocked = false;
        if (results != null) {
            if ("edit".equals(screen)) {
                isLocked = results.getReadOnlyTabs().contains(LockPrerequisites.EDIT) ||
                            results.getReadOnlyTabs().contains(LockPrerequisites.ALL_LEFT);
            } else if ("metadata".equals(screen)) {
                isLocked = results.getReadOnlyTabs().contains(LockPrerequisites.METADATA) ||
                            results.getReadOnlyTabs().contains(LockPrerequisites.ALL_LEFT);
            }
        }
        final boolean readOnly = (results != null && isLocked);
        if (editable && ! readOnly) {
            output = ServicesRegistry.getInstance().getJahiaFetcherService().fetchServlet((ParamBean) jParams, TREE_MANAGER_JSP);
        } else {
            output = ServicesRegistry.getInstance().getJahiaFetcherService().fetchServlet((ParamBean) jParams, READONLY_JSP);
        }

        engineMap.put(fieldsEditCallingEngineName + "." + "fieldForm", output);

        return true;
    } // end composeEngineMap


    /**
     * composes JahiaFileField object
     *
     * @param jParams   a ProcessingContext object
     * @param engineMap the engine hashmap
     * @param theField  the field we are working on
     * @return the page object
     */
    private JahiaFileField composeFileField(final ProcessingContext jParams,
                                            final Map<String, Object> engineMap, final JahiaField theField) {

        JahiaFileField fField = (JahiaFileField) theField.getObject();
        final String movedFrom = (String) jParams.getSessionState().getAttribute(jParams.getSiteID() + "-movedfrom");
        if (movedFrom != null) {
            jParams.getSessionState().removeAttribute(jParams.getSiteID() + "-movedfrom");
            final String movedTo = (String) jParams.getSessionState().getAttribute(jParams.getSiteID() + "-movedto");
            if (fField.getRealName().equals(movedFrom)) {
                jParams.setParameter("file_id", TableEntry.javascriptEncode(movedTo));
                this.getFormData(jParams, engineMap, theField);
                fField = (JahiaFileField) theField.getObject();
            }
        }

        engineMap.put("filename", TableEntry.javascriptEncode(fField.getRealName()));    

        if (fField.getRealName().equals("")) {
            engineMap.put("filenotloaded", fField.getType());
        } else {
            engineMap.remove("filenotloaded");
        }

        return fField;

    } // end composePage
}
