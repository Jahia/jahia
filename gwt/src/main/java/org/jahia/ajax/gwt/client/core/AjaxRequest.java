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
package org.jahia.ajax.gwt.client.core;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.RequestBuilder.Method;
import com.google.gwt.user.client.Window;

/**
 * Utility class for performing Ajax calls to the server.
 * 
 * @author Sergiy Shyrkov
 */
public class AjaxRequest {

    public static class AjaxRequestCallback implements RequestCallback {
        
        private JavaScriptObject onFailure;
        private JavaScriptObject onSuccess;

        /**
         * Initializes an instance of this class.
         */
        private AjaxRequestCallback(JavaScriptObject onSuccess, JavaScriptObject onFailure) {
            super();
            this.onSuccess = onSuccess;
            this.onFailure = onFailure;
        }

      public native void callOnFailure(JavaScriptObject callback, String message, String stackTrace)  /*-{
          if (typeof callback == 'function') {
              callback(message, stackTrace);
          }
      }-*/;
      
      public native void callOnSuccess(JavaScriptObject callback, String text, int statusCode, String statusText)  /*-{
          if (typeof callback == 'function') {
              callback(text, statusCode, statusText);
          }
      }-*/;

      public void onError(Request request, Throwable exception){
          callOnFailure(onFailure, exception.getMessage(), String.valueOf(exception.getStackTrace()));
      }
      
      public void onResponseReceived(Request request, Response response) {
          callOnSuccess(onSuccess, response.getText(), response.getStatusCode(), response.getStatusText());
      }
    }
    
    private static native String getMethod(JavaScriptObject options)  /*-{
        return (typeof options != 'undefined') && (typeof options.method != 'undefined') && (options.method.toLowerCase() == 'get') ? 'get' : 'post'; 
    }-*/;

    private static native JavaScriptObject getOnFailure(JavaScriptObject options)  /*-{
        return (typeof options != 'undefined') && (typeof options.onFailure != 'undefined') ? options.onFailure : null; 
    }-*/;
    
    private static native JavaScriptObject getOnSuccess(JavaScriptObject options)  /*-{
        return (typeof options != 'undefined') && (typeof options.onSuccess != 'undefined') ? options.onSuccess : null; 
    }-*/;

    private static native String getParameters(JavaScriptObject options)  /*-{
        var parameters = options.parameters || '';
        var queryString = '';
        if (typeof parameters == 'string') {
            queryString = parameters;
        } else {
            for (var item in parameters) {
                queryString += (queryString.length > 0 ? '&' : '') + item + '=' + encodeURIComponent(parameters[item]);
            }
        }
        
        return queryString; 
    }-*/;

    public static void perfom(String url, JavaScriptObject options) {
        try {
            Method method = getMethod(options).equals("post") ? RequestBuilder.POST
                    : RequestBuilder.GET;
            String parameters = getParameters(options);
            if (RequestBuilder.GET == method && parameters.length() > 0) {
                url += url.contains("?") ? '&' : '?' + parameters;
            }
            RequestBuilder request = new RequestBuilder(method, url);
            String data = null;
            if (RequestBuilder.POST == method) {
                request.setHeader("Content-Type",
                        "application/x-www-form-urlencoded");
                data = parameters.length() > 0 ? parameters : null;
            }

            request.sendRequest(data, new AjaxRequestCallback(
                    getOnSuccess(options), getOnFailure(options)));
        } catch (RequestException e) {
            Window.alert("Failed to send the request: " + e.getLocalizedMessage());
        }
    }

}
