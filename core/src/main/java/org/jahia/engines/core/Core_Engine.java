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
//  AK   14.12.2000
//  AK   19.12.2000  use the EngineRenderer class
//  AK   04.01.2001  change EngineRenderer call

package org.jahia.engines.core;

import org.jahia.api.Constants;
import org.jahia.bin.Render;
import org.jahia.bin.Edit;
import org.jahia.data.JahiaData;
import org.jahia.engines.JahiaEngine;
import org.jahia.engines.validation.EngineValidationHelper;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaForbiddenAccessException;
import org.jahia.exceptions.JahiaPageNotFoundException;
import org.jahia.params.ParamBean;
import org.jahia.params.ProcessingContext;

import java.io.IOException;


public class Core_Engine implements JahiaEngine {

    private static final org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger(Core_Engine.class);

    /**
     * The engine's name
     */
    public static final String ENGINE_NAME = "core";

    public Core_Engine() {
    }

    /**
     * authoriseRender AK    14.12.2000
     */
    public boolean authoriseRender(ProcessingContext jParams) {
        return (jParams.getOperationMode() == ProcessingContext.EDIT);
    }


    /**
     * needsJahiaData AK    14.12.2000
     */
    public boolean needsJahiaData(ProcessingContext jParams) {
        return true;
    }


    /**
     * renderLink AK    14.12.2000
     */
    public String renderLink(ProcessingContext jParams, Object theObj)
            throws JahiaException {
        return jParams.composeEngineUrl(ENGINE_NAME, new StringBuffer().append(
                EMPTY_STRING + "/op/").append(jParams.getOperationMode())
                .append("/pid/").append(jParams.getPageID()).toString());
    }


    /**
     * handleActions AK    14.12.2000 AK    04.01.2001  use processCore()...
     */
    public EngineValidationHelper handleActions(ProcessingContext jParams, JahiaData jData)
            throws JahiaException {
        if (logger.isDebugEnabled()) {
            logger.debug("Generating content for " + jParams.getRemoteAddr()
                    + "...");
        }
        processCore(jData);
        return null;
    }

    /**
     * Retrieve the engine name.
     *
     * @return the engine name.
     */
    public final String getName() {
        return ENGINE_NAME;
    }


    /**
     * Does the actual dispatching to the template, displaying all the objects that are
     * accessible through the JahiaData facade object.
     *
     * @param jData the JahiaData object containing all the context data for the current
     *              request.
     * @throws JahiaException can mean a lot of things, from errors in communicating with the
     *                        database, to errors in the templates, etc...
     */
    private void processCore(JahiaData jData)
            throws JahiaException {
        ParamBean processingContext = (ParamBean) jData.getProcessingContext();

        try {
            String base;
            if (ParamBean.NORMAL.equals(processingContext.getOpMode())) {
                base = processingContext.getRequest().getContextPath()+ Render.getRenderServletPath() + "/"+ Constants.LIVE_WORKSPACE +"/"+processingContext.getLocale();
            } else if (ParamBean.PREVIEW.equals(processingContext.getOpMode())) {
                base = processingContext.getRequest().getContextPath()+ Render.getRenderServletPath() + "/"+ Constants.EDIT_WORKSPACE +"/"+processingContext.getLocale();
            } else {
                base = processingContext.getRequest().getContextPath()+ Edit.getEditServletPath()+ "/"+ Constants.EDIT_WORKSPACE +"/"+processingContext.getLocale();
            }
            String jcrPath = "/content/sites/" + processingContext.getSiteKey() + "/home";
            processingContext.getRealResponse().sendRedirect(base + jcrPath + ".html");
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

}
