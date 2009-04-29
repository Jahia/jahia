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
package org.jahia.ajax.gwt.client.widget.dialog;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.MessageBox;
import org.jahia.ajax.gwt.client.data.GWTJahiaAjaxActionResultError;

import java.util.Iterator;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: hollis
 * Date: 21 ao�t 2008
 * Time: 09:51:16
 * To change this template use File | Settings | File Templates.
 */
public class ErrorDialog extends MessageBox {

    private List<GWTJahiaAjaxActionResultError> errors;

    public ErrorDialog() {
        super();
    }

    public ErrorDialog(List<GWTJahiaAjaxActionResultError> errors) {
        this();
        this.errors = errors;
    }

    public List<GWTJahiaAjaxActionResultError> getErrors() {
        return errors;
    }

    public void setErrors(List<GWTJahiaAjaxActionResultError> errors) {
        this.errors = errors;
    }

    protected void setMessage() {
        StringBuffer buffer = new StringBuffer();
        Html htmlContent = new Html();
        //htmlContent.setStyleName("x-window-errordlg-content");
        if (this.errors != null){
            buffer.append("<ul class=\"x-window-errordlg-content\">");
            Iterator<GWTJahiaAjaxActionResultError> iterator = this.errors.iterator();
            GWTJahiaAjaxActionResultError error = null;
            while(iterator.hasNext()){
                error = iterator.next();
                buffer.append("<li>").append(error.getErrorMsg()).append("</li>");
            }
            buffer.append("</ul>");
        }
        htmlContent.setHtml(buffer.toString());
        this.setMessage(htmlContent.getHtml());
    }

    public void show(){
        setMessage();
        //this.getDialog().setButtons(Dialog.OK);
        this.getDialog().setAutoHeight(true);
        this.getDialog().setWidth(450);
        this.getDialog().setScrollMode(Style.Scroll.NONE);
        //this.getDialog().addStyleName("x-window-errordlg");
        this.setIcon(MessageBox.ERROR);
        this.setTitle("Error");
        setModal(true);
        super.show();
    }
}
