/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.jahia.ajax.gwt.client.service;

import com.google.gwt.user.client.rpc.IsSerializable;

import java.io.Serializable;

public class GWTConstraintViolationException extends GWTJahiaServiceException implements Serializable, IsSerializable {
    private String path;
    private String constraintMessage;
    private String locale;
    private String propertyName;
    private String propertyLabel;

    public GWTConstraintViolationException() {
    }

    GWTConstraintViolationException(String path, String constraintMessage, String locale, String propertyName, String propertyLabel) {
        this.path = path;
        this.constraintMessage = constraintMessage;
        this.locale = locale;
        this.propertyName = propertyName;
        this.propertyLabel = propertyLabel;
    }

    public String getUuid() {
        return path;
    }

    public String getConstraintMessage() {
        return constraintMessage;
    }

    public String getLocale() {
        return locale;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public String getPropertyLabel() {
        return propertyLabel;
    }
}
