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
//  EngineParams
//  EV      03.12.2000
//
//  getParameter( key )
//
//

package org.jahia.engines;

import java.util.HashMap;
import java.util.Map;


public class EngineParams {

    public static final String PARAM_TOKEN = "���";
    public static final String VALUE_TOKEN = "***";

    /**
     * associates String
     */
    final private Map<String, String> theParams = new HashMap<String, String>();


    /**
     * constructor EV    03.12.2000
     */
    public EngineParams (String paramStr) {
        decomposeParams (paramStr);
    } // end constructor


    /**
     * decomposeParams EV    03.12.2000
     */
    private void decomposeParams (String tempStr) {
        if (tempStr != null) {
            String paramStr = tempStr;
            int startpos = paramStr.indexOf (VALUE_TOKEN);
            int endpos;
            while (startpos != -1) {
                endpos = paramStr.indexOf (PARAM_TOKEN);
                if (endpos == -1) {
                    endpos = paramStr.length ();
                }
                String keyStr = paramStr.substring (0, startpos);
                String valStr = paramStr.substring (startpos + VALUE_TOKEN.length (), endpos);
                if ((! keyStr.equals("")) && (! valStr.equals(""))) {
                    theParams.put (keyStr, valStr);
                }
                paramStr = paramStr.substring (endpos, paramStr.length ());
                startpos = paramStr.indexOf (VALUE_TOKEN);
            }
        }
    } // end decomposeParams


    /**
     * getParameter EV    03.12.2000
     */
    public String getParameter (String keyStr) {
        return theParams.get (keyStr);
    } // end getParams


}
