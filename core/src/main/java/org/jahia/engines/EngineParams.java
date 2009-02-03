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
    final private Map theParams = new HashMap();


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
        return (String) theParams.get (keyStr);
    } // end getParams


}
