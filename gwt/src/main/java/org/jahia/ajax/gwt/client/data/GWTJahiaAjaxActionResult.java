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
package org.jahia.ajax.gwt.client.data;

import org.jahia.ajax.gwt.client.data.GWTJahiaAjaxActionResultError;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * User: hollis
 * Date: 20 août 2008
 * Time: 12:26:53
 *
 */
public class GWTJahiaAjaxActionResult implements Serializable {

    private String value;
    private List<GWTJahiaAjaxActionResultError>errors = new ArrayList<GWTJahiaAjaxActionResultError>();

    public GWTJahiaAjaxActionResult() {
    }

    public GWTJahiaAjaxActionResult(String value) {
        this.value = value;
    }

    public GWTJahiaAjaxActionResult(String value, List<GWTJahiaAjaxActionResultError> errors) {
        this.value = value;
        this.errors = errors;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public List<GWTJahiaAjaxActionResultError> getErrors() {
        return errors;
    }

    public void setErrors(List<GWTJahiaAjaxActionResultError> errors) {
        this.errors = errors;
    }

    public void addError(String errorMsg){
        GWTJahiaAjaxActionResultError error = new GWTJahiaAjaxActionResultError(errorMsg);
        if (this.errors==null){
            this.errors = new ArrayList<GWTJahiaAjaxActionResultError>();
        }
        this.errors.add(error);
    }

}
