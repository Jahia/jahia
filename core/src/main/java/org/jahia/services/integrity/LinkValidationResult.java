/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
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
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.services.integrity;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;

/**
 * Represents a validation result for a link item.
 * 
 * @author Sergiy Shyrkov
 */
public class LinkValidationResult {

    private int errorCode;

    private String errorMessage;

    private String url;

    /**
     * Initializes an instance of this class.
     * 
     * @param errorCode
     *            the HTTP response code
     * @param errorMessage
     *            the error message if any
     */
    public LinkValidationResult(int errorCode, String errorMessage) {
        super();
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    /**
     * Initializes an instance of this class.
     * 
     * @param errorCode
     *            the HTTP response code
     * @param errorMessage
     *            the error message if any
     * @param url
     *            the result link URL after all redirects
     */
    public LinkValidationResult(int errorCode, String errorMessage, String url) {
        this(errorCode, errorMessage);
        this.url = url;
    }

    /**
     * Returns the HTTP response code for the link validation result.
     * 
     * @return the HTTP response code for the link validation result
     */
    public int getErrorCode() {
        return errorCode;
    }

    /**
     * Returns the error message, if any, for the link validation result.
     * 
     * @return the error message, if any, for the link validation result
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * The final URL after all redirects. If there are no redirects, it is the
     * same as the original one.
     * 
     * @return final URL after all redirects. If there are no redirects, it is
     *         the same as the original one
     */
    public String getUrl() {
        return url;
    }

    /**
     * Sets the final URL after all redirects.
     * 
     * @param url
     *            the final URL after all redirects
     */
    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this);
    }
}
