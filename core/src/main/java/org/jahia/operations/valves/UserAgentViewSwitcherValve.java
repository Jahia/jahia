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

package org.jahia.operations.valves;

import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import org.jahia.params.ProcessingContext;
import org.jahia.pipelines.PipelineException;
import org.jahia.pipelines.valves.Valve;
import org.jahia.pipelines.valves.ValveContext;

/**
 * Created by IntelliJ IDEA.
 * User: loom
 * Date: Nov 28, 2008
 * Time: 3:59:45 PM
 * To change this template use File | Settings | File Templates.
 */
public class UserAgentViewSwitcherValve implements Valve {

    public static final String VIEW_SWITCHING_VALVE_KEY_REQUEST_ATTRIBUTE_NAME = "org.jahia.operations.valve.ViewSwitchingValveAdditionalKey";

    // Views may come from user-agent, query parameters or even form parameters.
    private Map<String, String> userAgentMatchingRules;
    private Map<Pattern, String> userAgentMatchingPatterns;

    public void invoke(Object context, ValveContext valveContext) throws PipelineException {
        //To change body of implemented methods use File | Settings | File Templates.
        if (context instanceof ProcessingContext) {
            ProcessingContext processingContext = (ProcessingContext) context;
            String userAgent = processingContext.getUserAgent();
            for (Map.Entry<Pattern,String> entry : userAgentMatchingPatterns.entrySet()) {
                Pattern curPattern = entry.getKey();
                Matcher m = curPattern.matcher(userAgent);
                if (m.matches()) {
                    processingContext.setAttribute(VIEW_SWITCHING_VALVE_KEY_REQUEST_ATTRIBUTE_NAME, entry.getValue());
                }
            }
        }
        valveContext.invokeNext(context);
    }

    public void initialize() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public Map<String,String> getUserAgentMatchingRules() {
        return userAgentMatchingRules;
    }

    public void setUserAgentMatchingRules(Map<String, String> userAgentMatchingRules) {
        this.userAgentMatchingRules = userAgentMatchingRules;
        userAgentMatchingPatterns = new HashMap<Pattern, String>();
        for (Map.Entry<String,String> entry : userAgentMatchingRules.entrySet()) {
            Pattern curPattern = Pattern.compile(entry.getKey());
            userAgentMatchingPatterns.put(curPattern, entry.getValue());
        }
    }
}
