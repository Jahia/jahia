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
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
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
