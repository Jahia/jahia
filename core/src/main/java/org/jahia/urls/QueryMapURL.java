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
 package org.jahia.urls;

import java.util.Map;
import java.util.StringTokenizer;
import org.jahia.utils.*;

/**
 * <p>Title: Basic class to generate URL in Jahia.</p>
 * <p>Description: This class is used to offer tools to manipulate an URL in
 * a fashion much more flexible than Java's URL class. We can here operate
 * on individual query string parameters. You may also sub-class this class
 * in order to build other URL types such as Jahia Engine URLs, etc...</p>
 * <p>This class is really the first shot at something like this in Jahia so
 * it might change in the future to accomodate more needs. But the basic idea
 * is to sub-class this class rather than put everything including the kitchen
 * sink in here.</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Jahia Ltd</p>
 * @author Serge Huber
 * @version 1.0
 */

public class QueryMapURL extends URI {

    protected InsertionSortedMap queryParameters = new InsertionSortedMap();

    /**
     * Empty constructor. This allows this class to be JavaBean compliant.
     * If you use this constructor, don't forget to use one of the set*URL
     * methods below of the object will not be initialized properly.
     */
    public QueryMapURL() {
    }

    /**
     * Constructs an URL based on a string form of an URL. If the relative URL
     * starts with a "/" character, this will be recorded in the internal state
     * of the URL and when the URL will be converted back to String
     * representation it will start at the path, not as a completely fully
     * qualified URL.
     *
     * @param url a String containing a fully qualified URL
     * @throws java.net.MalformedURLException if the URL was malformed or not
     * fully qualified
     */
    public QueryMapURL(String url)
        throws java.net.MalformedURLException {
        setURI(url);
        parseQueryParameters(super.getQueryString());
    }

    /**
     * Constructs an URL based on an existing Java URL.
     * @param javaURL an existing valid Java URL
     */
    public QueryMapURL(java.net.URL javaURL) {
        setURI(javaURL);
        parseQueryParameters(super.getQueryString());
    }


    /**
     * Sets/resets a query parameter on this URL. Previous to calling this
     * method, this object must be fully initialized using one of the
     * constructors or one of the setters. The query parameters are pair
     * of name=value strings that will be at the end of the URL after a
     * "?" character and seperated by "&" characters. This method may be called
     * more than once to set/reset the value of a query parameter. Note that
     * the query parameters will be outputted in the order they have been
     * set.
     * @param name a String containing the name of the query parameter
     * @param value a String containing the value of the query parameter
     */
    public void setQueryParameter(String name, String value) {
        queryParameters.put(name, value);
    }

    /**
     * Retrieves a query parameter on this URL. Previous to calling this
     * method, this object must be fully initialized using one of the
     * constructors or one of the setters. The query parameters are pair
     * of name=value strings that will be at the end of the URL after a
     * "?" character and seperated by "&" characters.
     *
     * @param name a String containing the name of the query parameter that
     * we want to retrieve
     *
     * @return the string value of the parameter if it exists, null otherwise
     */
    public String getQueryParameter(String name) {
        return (String) queryParameters.get(name);
    }

    /**
     * Removes a query parameter and it's value
     * @param name removes an existing query parameter and it's value. If the
     * parameter didn't exist in the URL previously, this method does nothing.
     */
    public void removeQueryParameter(String name) {
        queryParameters.remove(name);
    }

    /**
     * @param name a String containing the name of the parameter to look for
     * @return true if the query contains a parameter by the specified name
     */
    public boolean containsQueryParameter(String name) {
        return queryParameters.containsKey(name);
    }

    private void parseQueryParameters(String queryString) {
        if (queryString == null) {
            return;
        }
        StringTokenizer tokenizer = new StringTokenizer(queryString, "&");
        while (tokenizer.hasMoreTokens()) {
            String curToken = tokenizer.nextToken();
            int equalPos = curToken.indexOf("=");
            if (equalPos == -1) {
                queryParameters.put(curToken, null);
            } else {
                String curName = curToken.substring(0, equalPos);
                String curValue = curToken.substring(equalPos+1);
                queryParameters.put(curName, curValue);
            }
        }
    }

    public String getQueryString () {
        if (queryParameters.size() == 0) {
            return null;
        }
        StringBuffer result = new StringBuffer();
        java.util.Iterator parameterIter = queryParameters.entrySet().iterator();
        while (parameterIter.hasNext()) {
            Map.Entry curEntry = (Map.Entry) parameterIter.next();
            result.append(curEntry.getKey());
            result.append("=");
            result.append(curEntry.getValue());
            if (parameterIter.hasNext()) {
                result.append("&");
            }
        }
        return result.toString();
    }

    public void setQueryString(String queryString) {
        super.setQueryString(queryString);
        parseQueryParameters(queryString);
    }

}