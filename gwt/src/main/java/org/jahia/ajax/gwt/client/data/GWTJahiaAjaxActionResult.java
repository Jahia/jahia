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
package org.jahia.ajax.gwt.client.data;

import org.jahia.ajax.gwt.client.data.GWTJahiaAjaxActionResultError;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: hollis
 * Date: 20 ao�t 2008
 * Time: 12:26:53
 * To change this template use File | Settings | File Templates.
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
