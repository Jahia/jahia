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