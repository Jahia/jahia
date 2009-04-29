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

import java.util.HashMap;
import java.util.Map;

import org.jahia.data.JahiaData;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ProcessingContext;
import org.jahia.settings.SettingsBean;

/**
 *
 * <p>Title: Helper object for Jahia Engine </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author Khue Nguyen
 * @version 1.0
 */
public class JahiaEngineViewHelper implements JahiaEngineViewHelperInterface {

	public static final String ENGINE_VIEW_HELPER
            = "jahiaEngineViewHelper";

    protected String engineURL;
    protected JahiaEngineCommonDataInterface engineCommonData;

    public Map objectMaps = new HashMap();

    /**
     *
     * @param request
     * @param engineCommonData
     * @throws JahiaException
     */
    public JahiaEngineViewHelper(){

    }

    /**
     *
     * @param engineCommonData
     * @throws JahiaException
     */
    public JahiaEngineViewHelper(JahiaEngineCommonDataInterface engineCommonData)
    throws JahiaException {
        this.engineCommonData
                = engineCommonData;
    }

    public JahiaEngineCommonDataInterface getEngineCommonData(){
        return this.engineCommonData;
    }

    public void setEngineCommonData(JahiaEngineCommonDataInterface engineCommonData){
        this.engineCommonData = engineCommonData;
    }

    public JahiaData getJahiaData(){
        return this.engineCommonData.getJahiaData();
    }

    public ProcessingContext getParamBean(){
        return this.engineCommonData.getParamBean();
    }

    public SettingsBean getSettings(){
        return this.getParamBean().settings();
    }

    public Map getObjectMaps() {
        return objectMaps;
    }

    public void setObjectMaps(Map objectMaps) {
        this.objectMaps = objectMaps;
    }


    public void setObject(String key, Object object){
        this.objectMaps.put(key,object);
    }

    public Object getObject(String key){
        return this.objectMaps.get(key);                
    }

}

