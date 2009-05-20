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


