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

