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
package org.jahia.views.engines;

import javax.servlet.http.HttpServletRequest;

import org.jahia.data.JahiaData;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ProcessingContext;
import org.jahia.settings.SettingsBean;

/**
 *
 * <p>Title: Bean containing common datas for Jahia Engine</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author Khue Nguyen
 * @version 1.0
 */
public class JahiaEngineCommonData implements JahiaEngineCommonDataInterface {

	public static final String JAHIA_ENGINE_COMMON_DATA = "jahiaEngineCommonData";

    protected JahiaData jData;
    protected String engineTitle = "No Title";
    protected String enginesJspContextPath;
    protected String imagesContextPath;
    protected String javaScriptPath;
    protected String httpJsContextPath;
    protected String engineURL;

    /**
     *
     */
    public JahiaEngineCommonData(){
    }

    /**
     * @param processingContext
     * @throws JahiaException
     */
    public JahiaEngineCommonData(ProcessingContext processingContext) throws JahiaException {
        init(processingContext);
    }

    public JahiaEngineCommonData(HttpServletRequest request) throws JahiaException {
        init(request);
    }

    private void init(ProcessingContext processingContext) throws JahiaException {

        JahiaData jData = (JahiaData)processingContext.getAttribute(JahiaData.JAHIA_DATA);
        if ( jData == null ){
            throw new JahiaException("Requested JahiaData is null, when trying to init JahiaEngineCommonData",
                                     "Requested JahiaData is null, when trying to init JahiaEngineCommonData",
                                     JahiaException.DATA_ERROR, JahiaException.ENGINE_ERROR);
        }

        init(jData);
    }

    private void init(HttpServletRequest request) throws JahiaException {

        JahiaData jData = (JahiaData)request.getAttribute(JahiaData.JAHIA_DATA);
        if ( jData == null ){
            throw new JahiaException("Requested JahiaData is null, when trying to init JahiaEngineCommonData",
                                     "Requested JahiaData is null, when trying to init JahiaEngineCommonData",
                                     JahiaException.DATA_ERROR, JahiaException.ENGINE_ERROR);
        }

        init(jData);
    }

    private void init(JahiaData jData) throws JahiaException {
        this.jData = jData;

        this.javaScriptPath = jData.getProcessingContext().settings().getJsHttpPath();

        this.imagesContextPath = jData.getProcessingContext().settings().getEnginesContext()
                               + "engines/images/";

        this.httpJsContextPath = jData.getProcessingContext().getContextPath()
                               + jData.getProcessingContext().settings().getJavascriptContext();

        this.enginesJspContextPath = jData.getProcessingContext().getContextPath()
                                   + jData.getProcessingContext().settings().getEnginesContext();
        jData.getProcessingContext().setAttribute ("URL", this.enginesJspContextPath);
    }

    public JahiaData getJahiaData(){
        return this.jData;
    }

    public ProcessingContext getParamBean(){
        return this.jData.getProcessingContext();
    }

    public SettingsBean getSettings(){
        return this.jData.getProcessingContext().settings();
    }

    public String getEngineTitle(){
        return this.engineTitle;
    }

    public void setEngineTitle(String title){
        this.engineTitle = title;
    }

    public String getEngineURL(){
        return this.engineURL;
    }

    public void setEngineURL(String engineURL){
        this.engineURL = engineURL;
    }

    public String getEnginesJspContextPath(){
        return this.enginesJspContextPath;
    }

    public String getImagesContextPath(){
        return this.imagesContextPath;
    }

    public String getJavaScriptPath(){
        return this.javaScriptPath;
    }

    public String getHttpJsContextPath(){
        return this.httpJsContextPath;
    }
}


