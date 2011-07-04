package org.jahia.services.seo.urlrewrite;

import org.apache.commons.lang.StringUtils;
import org.jahia.settings.SettingsBean;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

/**
 * Simple filter that removes the ;jsessionid string
 */
public class SessionidRemovalResponseWrapper extends HttpServletResponseWrapper {


    public SessionidRemovalResponseWrapper(HttpServletResponse response) {
        super(response);
    }

    @Override
    public String encodeURL(String url) {
        return clean(super.encodeURL(url));
    }

    @Override
    public String encodeRedirectURL(String url) {
        return clean(super.encodeRedirectURL(url));
    }

    @Override
    public String encodeUrl(String url) {
        return clean(super.encodeUrl(url));
    }

    @Override
    public String encodeRedirectUrl(String url) {
        return clean(super.encodeRedirectUrl(url));
    }

    private String clean(String url) {
        String s = ";" + SettingsBean.getInstance().getJsessionIdParameterName();
        if (url.contains(s)) {
            return StringUtils.substringBefore(url, s);
        }
        return url;
    }
}
