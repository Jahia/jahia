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
package org.jahia.params.valves;

import org.apache.log4j.Logger;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.AdvPreviewSettings;
import org.jahia.params.ParamBean;
import org.jahia.params.ProcessingContext;
import org.jahia.pipelines.PipelineException;
import org.jahia.pipelines.valves.Valve;
import org.jahia.pipelines.valves.ValveContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.usermanager.JahiaUser;

/**
 *
 */
public class UserAliasingEngineAuthValveImpl implements Valve {
    private static final Logger logger = Logger.getLogger(UserAliasingEngineAuthValveImpl.class);
    public static final String VALVE_RESULT = "login_valve_result";
    public static final String OK = "ok";
    public static final String USE_COOKIE = "useCookie";

    public void initialize() {
    }

    public void invoke(Object context, ValveContext valveContext) throws PipelineException {
        try {
            final ProcessingContext jParams = (ProcessingContext) context;

            String paramOpMode = jParams.getParameter(ProcessingContext.OPERATION_MODE_PARAMETER);
            String userAliasingMode = jParams.getParameter(ProcessingContext.USERALIASING_MODE_PARAMETER);

            // switch mode detected
            AdvPreviewSettings advPreviewSettings = (AdvPreviewSettings)jParams.getSessionState()
                    .getAttribute(ProcessingContext.SESSION_ADV_PREVIEW_SETTINGS);
            //JahiaUser mainUser = (JahiaUser)jParams.getSessionState().getAttribute(ProcessingContext.SESSION_ALIASING_ROOT_USER);
            //JahiaUser aliasedUser = (JahiaUser)jParams.getSessionState().getAttribute(ProcessingContext.SESSION_ALIASED_USER);
            JahiaUser mainUser = null;
            JahiaUser aliasedUser = null;
            if (advPreviewSettings != null && advPreviewSettings.isEnabled()){
                mainUser = advPreviewSettings.getMainUser();
                aliasedUser = advPreviewSettings.getAliasedUser();
            }

            if (mainUser != null && aliasedUser != null && !mainUser.getUserKey().equals(aliasedUser.getUserKey())){

                if (ProcessingContext.USERALIASING_MODE_OFF.equals(userAliasingMode)
                    && ProcessingContext.EDIT.equals(paramOpMode)){

                    logger.info("Switch mode detected, switch to original user "  + mainUser.getUserKey());

                    // restore main User
                    ParamBean paramBean = null;
                    if (jParams instanceof ParamBean) {
                        paramBean = (ParamBean) jParams;
                        //paramBean.invalidateSession();
                        paramBean.switchUserSession(aliasedUser,mainUser);
                    }
                    jParams.setAttribute(VALVE_RESULT, OK);
                    jParams.setUser(mainUser);
                    ServicesRegistry.getInstance().getLockService().purgeLockForContext(mainUser.getUserKey());

                    //jParams.getSessionState().setAttribute(ProcessingContext.SESSION_ALIASED_USER,aliasedUser);
                    jParams.getSessionState().removeAttribute(ProcessingContext.SESSION_ALIASED_USER);

                } else if ( ProcessingContext.USERALIASING_MODE_ON.equals(userAliasingMode)
                        && ProcessingContext.PREVIEW.equals(paramOpMode) ){

                    JahiaUser previousAliasedUser = (JahiaUser)jParams.getSessionState()
                            .getAttribute(ProcessingContext.SESSION_ALIASED_USER);
                    if (previousAliasedUser == null ||
                            !previousAliasedUser.getUserKey().equals(aliasedUser.getUserKey())){
                        // restore main User
                        ParamBean paramBean = null;
                        if (jParams instanceof ParamBean) {
                            paramBean = (ParamBean) jParams;
                            //paramBean.invalidateSession();
                            paramBean.switchUserSession(mainUser,aliasedUser);
                        }
                        jParams.setAttribute(VALVE_RESULT, OK);
                        //jParams.setUser(aliasedUser);
                        jParams.setUser(mainUser);
                        ServicesRegistry.getInstance().getLockService().purgeLockForContext(aliasedUser.getUserKey());

                        jParams.getSessionState().setAttribute(ProcessingContext.SESSION_ALIASED_USER,aliasedUser);
                    } else {
                        valveContext.invokeNext(context);
                    }
                } else {
                    valveContext.invokeNext(context);
                }
            } else {
                valveContext.invokeNext(context);
            }
        } catch (JahiaException e) {
            throw new PipelineException(e);
        }
    }
}