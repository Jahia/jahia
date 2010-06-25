package org.jahia.services.render.filter;

import org.apache.log4j.Logger;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.TemplateNotFoundException;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Set contribution template for contentLists set as editable
 * User: toto
 * Date: Nov 26, 2009
 * Time: 3:28:13 PM
 */
public class UserAgentFilter extends AbstractFilter {
    private static Logger logger = Logger.getLogger(UserAgentFilter.class);

    // Views may come from user-agent, query parameters or even form parameters.
    private Map<String, String> userAgentMatchingRules;
    private Map<Pattern, String> userAgentMatchingPatterns;

    public String prepare(RenderContext context, Resource resource, RenderChain chain) throws Exception {
        String userAgent = context.getRequest().getHeader("user-agent");

        if (userAgent != null && !resource.getTemplateType().contains("-")) {
            for (Map.Entry<Pattern,String> entry : userAgentMatchingPatterns.entrySet()) {
                Pattern curPattern = entry.getKey();
                Matcher m = curPattern.matcher(userAgent);
                if (m.matches()) {
                    String baseType = resource.getTemplateType();
                    resource.setTemplateType(baseType+"-"+entry.getValue());
                    // todo : opimize a little bit ..
                    try {
                        service.resolveScript(resource, context);
                    } catch (TemplateNotFoundException e) {
                        logger.debug("Template not found for "+entry.getValue());
                        resource.setTemplateType(baseType);
                    }
                }
            }
        }
        return null;
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