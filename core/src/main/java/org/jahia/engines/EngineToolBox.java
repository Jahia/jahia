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
//
// $Id$

//
//  EV  10.01.20001
//

package org.jahia.engines;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import org.jahia.data.containers.JahiaContainer;
import org.jahia.data.fields.JahiaField;
import org.jahia.engines.audit.ManageLogs_Engine;
import org.jahia.engines.shared.FieldSubEngine;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ParamBean;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.audit.JahiaAuditLogManagerService;
import org.jahia.utils.JahiaObjectTool;


public class EngineToolBox {

    private static final EngineToolBox instance = new EngineToolBox ();

    private static final String MSG_INTERNAL_ERROR = "Audit Log Manager internal error";
    private static final String JSP_LOGFORM = "/engines/audit/viewlogs.jsp";
    //private static final String JSP_SENDLOG = "/engines/audit/sendlogs.jsp";
    private static final String JSP_FLUSHCONFIRM = "/engines/audit/flushconfirm.jsp";

    /** Logger instance */
    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger (EngineToolBox.class);


    private EngineToolBox() {
    }


    /**
     * returns a single instance of the object
     */
    public static EngineToolBox getInstance() {
        return instance;
    }


    /**
     * authoriseRender
     */
    public boolean authoriseRender(final ProcessingContext jParams) {
        return jParams.getOperationMode().equals(ProcessingContext.EDIT);
    } // end authoriseRender


    /**
     * dispatches to an engine according to the field type
     *
     *
     * @param theField JahiaField
     * @param theContainer JahiaContainer, can be null
     * @param engineName String
     * @param jParams ProcessingContext
     * @param mode int
     * @param engineMap HashMap
     * @throws JahiaException
     * @return boolean
     */
    public boolean processFieldTypes(final JahiaField theField, final JahiaContainer theContainer, final String engineName,
                                     final ProcessingContext jParams, final int mode, final Map<String, Object> engineMap) throws JahiaException {
        boolean out;

        engineMap.put("fieldsEditCallingEngineName", engineName);
        engineMap.put(engineName + "." + "theContainer", theContainer);
        engineMap.put(engineName + "." + "theField", theField);
        engineMap.put("theField", theField);

        int fieldType = theField.getDefinition().getType();
        if (logger.isDebugEnabled()) {
            logger.debug(" field type is " + fieldType);
        }
        if (fieldType <= 0) {
            fieldType = theField.getType();
        }
        // get the engine className

        final String fieldEngineName = theField.getEngineName();

        try {
            final Class<?> theParams[] = null;
            final Method thisMethod = Class.forName(fieldEngineName).getDeclaredMethod("getInstance", theParams);
            final Object args[] = null;

            final FieldSubEngine engine = (FieldSubEngine) thisMethod.invoke(null, args);

            out = engine.handleField(jParams, Integer.valueOf(mode), engineMap);

            if (logger.isDebugEnabled()) {
                logger.debug("Editing " + fieldEngineName.substring(fieldEngineName.lastIndexOf(".")) + " !");
            }

        } catch (ClassNotFoundException cnfe) {
            throw new JahiaException("EngineToolBox:processFieldTypes", "Class not found!", JahiaException.PAGE_ERROR, JahiaException.CRITICAL_SEVERITY, cnfe);
        } catch (NoSuchMethodException nsme) {
            throw new JahiaException("EngineToolBox:processFieldTypes", "Method not found!", JahiaException.PAGE_ERROR, JahiaException.CRITICAL_SEVERITY, nsme);
        } catch (IllegalAccessException iae) {
            throw new JahiaException("EngineToolBox:processFieldTypes", "Illegal access", JahiaException.PAGE_ERROR, JahiaException.CRITICAL_SEVERITY, iae);
        } catch (InvocationTargetException ite) {
            throw new JahiaException("EngineToolBox:processFieldTypes", "InvocationTarget exception", JahiaException.PAGE_ERROR, JahiaException.CRITICAL_SEVERITY, ite);
        }


        // we flush the corresponding cache entry, since we assume we are
        // updating it if we call this method (although we might only be
        // performing a load operation)
        /** todo is there a way to do this only when we really update a
         * field ? Maybe move this to a field.save() generic method.
         */
        ServicesRegistry.getInstance().getJahiaFieldService().invalidateCacheField(theField.getID());
        return out;
    } // end processFieldTypes

    /**
     * displays the screen requested by the user
     *
     * @param jParams a ProcessingContext object
     */
    public void displayScreen(final ProcessingContext jParams, final Map<String, Object> engineMap) throws JahiaException {
        EngineRenderer.getInstance().render(jParams, engineMap);
    } // end displayScreen


    /**
     * loads log data for the JSP file
     *
     * @param jParams    a ParamBean object (with request and response)
     * @param objectType an <code>int</code> representing the type of the object processed
     * @param engineMap  then engine map, to be forwarded to the JSP file
     */
    public void loadLogData(final ProcessingContext jParams, final int objectType, final Map<String, Object> engineMap) throws JahiaException {

        // set default values
        int flushLogs = 0;
        String output;
        int deletedRows = 0;

        // get parameters
        final String userAgent = jParams.getUserAgent();
        //boolean sendAsFile = (jParams.getRequest ().getParameter ("send") != null);
        if (jParams.getParameter("flush") != null) {
            flushLogs = Integer.parseInt(jParams.getParameter("flush"));
        }
        final int objectID = JahiaObjectTool.getInstance().getObjectID(objectType, engineMap);
        final String objectName = JahiaObjectTool.getInstance().getObjectName(objectType, engineMap);

        // Try to get the Audit Log Manager Service
        final ServicesRegistry registry = ServicesRegistry.getInstance();
        final JahiaAuditLogManagerService mAuditLogManager;
        if (registry != null) {
            mAuditLogManager = registry.getJahiaAuditLogManagerService();
            if (mAuditLogManager == null) {
                throw new JahiaException(MSG_INTERNAL_ERROR, "Properties Engine could not get the Audit Log Manager Service instance.", JahiaException.SERVICE_ERROR, JahiaException.CRITICAL_SEVERITY);
            }
        } else {
            throw new JahiaException(MSG_INTERNAL_ERROR, "Properties Engine could not get the Service Registry instance.", JahiaException.REGISTRY_ERROR, JahiaException.CRITICAL_SEVERITY);
        }

        // logs
        engineMap.put("flushLogs", Integer.valueOf(flushLogs));
        engineMap.put("sendLogsURL", ManageLogs_Engine.getInstance().renderLink(jParams, ""));
        engineMap.put("objectTypeName", JahiaObjectTool.getInstance().getObjectTypeName(objectType));
        engineMap.put ("objectIDObj", Integer.valueOf(objectID));
        engineMap.put ("objectName", objectName);
        engineMap.put ("userAgent", userAgent);

        // flushLogs cases :
        //  - null : just display current log entries
        //  - 1    : user has clicked "Flush" in viewlogs.jsp -> display confirmation request
        //  - 2    : user has confirmed flush in flushconfirm.jsp -> call flushlogs() method
        //                                                           and display clean log window
        switch (flushLogs) {
            case 1:
                output = registry.getJahiaFetcherService().fetchServlet((ParamBean) jParams, JSP_FLUSHCONFIRM);
                break;
            case 2:
                deletedRows = mAuditLogManager.flushLogs(objectType, objectID, jParams);
            default:
                final List<Map<String, Object>> logData = mAuditLogManager.getLog(objectType, objectID, jParams);
                engineMap.put("logData", logData);
                engineMap.put("deletedRows", Integer.valueOf(deletedRows));
                output = registry.getJahiaFetcherService().fetchServlet((ParamBean) jParams, JSP_LOGFORM);
        }
        engineMap.put("logForm", output);

    }

}
