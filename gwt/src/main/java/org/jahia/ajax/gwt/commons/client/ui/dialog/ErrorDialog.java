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

package org.jahia.ajax.gwt.commons.client.ui.dialog;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.MessageBox;
import org.jahia.ajax.gwt.commons.client.beans.GWTAjaxActionResultError;

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

    private List<GWTAjaxActionResultError> errors;

    public ErrorDialog() {
        super();
    }

    public ErrorDialog(List<GWTAjaxActionResultError> errors) {
        this();
        this.errors = errors;
    }

    public List<GWTAjaxActionResultError> getErrors() {
        return errors;
    }

    public void setErrors(List<GWTAjaxActionResultError> errors) {
        this.errors = errors;
    }

    protected void setMessage() {
        StringBuffer buffer = new StringBuffer();
        Html htmlContent = new Html();
        //htmlContent.setStyleName("x-window-errordlg-content");
        if (this.errors != null){
            buffer.append("<ul class=\"x-window-errordlg-content\">");
            Iterator<GWTAjaxActionResultError> iterator = this.errors.iterator();
            GWTAjaxActionResultError error = null;
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
