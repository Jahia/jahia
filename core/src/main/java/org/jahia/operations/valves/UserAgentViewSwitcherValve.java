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
package org.jahia.operations.valves;

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
