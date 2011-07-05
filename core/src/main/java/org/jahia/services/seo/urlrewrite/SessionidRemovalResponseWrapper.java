package org.jahia.services.seo.urlrewrite;

import org.apache.commons.lang.StringUtils;
import org.apache.oro.text.regex.PatternMatcher;
import org.jahia.settings.SettingsBean;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.util.regex.Pattern;

/**
 * Simple filter that removes the ;jsessionid string
 */
public class SessionidRemovalResponseWrapper extends HttpServletResponseWrapper {
    private HttpServletRequest request;

    private Pattern cleanPattern = Pattern.compile(";"+SettingsBean.getInstance().getJsessionIdParameterName()+"=[^\\?#]*");

    public SessionidRemovalResponseWrapper(HttpServletRequest request, HttpServletResponse response) {
        super(response);
        this.request = request;
    }

    @Override
    public String encodeURL(String url) {
        return clean(super.encodeURL(url));
    }

    @Override
    public String encodeUrl(String url) {
        return encodeURL(url);
    }

    /**
     * Remove all jsession parameter if set in the settings, or replace it by a macro marker if we are in a rendered page
     * macro in that case)
     * @param url
     * @return
     */
    private String clean(String url) {
        if (SettingsBean.getInstance().isDisableJsessionIdParameter() || isInRender()) {
            // Remove the jsessionid= part
            String s = ";" + SettingsBean.getInstance().getJsessionIdParameterName();
            if (url.contains(s)) {
                url = cleanPattern.matcher(url).replaceFirst("");
            }

            // If we are in jahia page, and jsession ids are not disabled, replace by a macro
            if (isInRender() && !SettingsBean.getInstance().isDisableJsessionIdParameter() && !url.contains("##sessionid##")) {
                url += "##sessionid##";
            }
        }
        return url;
    }

    private boolean isInRender() {
        return request.getAttribute("currentNode") != null;
    }

}
