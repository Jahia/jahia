package org.jahia.services.content.automation;

import org.jahia.data.search.JahiaSearchHit;

public class URLService {
    private static URLService instance;

    private URLService() {
    }

    public static synchronized URLService getInstance() {
        if (instance == null) {
            instance = new URLService();
        }
        return instance;
    }

    public String addURLPath(JahiaSearchHit searchHit, String urlPath) {
        String url = searchHit.getURL() + urlPath;
        searchHit.setURL(url);
        return url;
    }

    public String addURLQueryParameter(JahiaSearchHit searchHit, String parameterName,
            String parameterValue) {
        StringBuffer changedUrl = new StringBuffer(searchHit.getURL());
        appendParams(changedUrl, parameterName + "=" + parameterValue);
        String url = changedUrl.toString();
        searchHit.setURL(url);        
        return url;
    }

    protected void appendParams(final StringBuffer theUrl, String params) {

        if (params != null && (params.length() > 0)) {
            if (theUrl.toString().indexOf("?") == -1) {
                if (params.startsWith("&")) {
                    params = "?" + params.substring(1, params.length());
                } else if (!params.startsWith("?")) {
                    if (!params.startsWith("/")) {
                        params = "?" + params;
                    }
                }
            } else {
                if (!params.startsWith("&")) {
                    if (!params.startsWith("/")) {
                        params = "&" + params;
                    }
                }
            }
            theUrl.append(params);
        }

    }
}
