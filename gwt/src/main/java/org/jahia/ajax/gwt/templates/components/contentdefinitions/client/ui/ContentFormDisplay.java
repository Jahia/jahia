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

//package org.jahia.ajax.gwt.templates.components.contentdefinitions.client.ui;
//
//import com.google.gwt.user.client.ui.FlexTable;
//import com.google.gwt.user.client.ui.Label;
//import com.google.gwt.user.client.rpc.AsyncCallback;
//import com.google.gwt.user.client.Window;
//import org.jahia.ajax.gwt.config.client.beans.GWTJahiaPageContext;
//import org.jahia.ajax.gwt.definitions.client.ContentDefinitionServiceAsync;
//import org.jahia.ajax.gwt.definitions.client.ContentDefinitionService;
//import org.jahia.ajax.gwt.definitions.client.model.GWTJahiaNodeType;
//
///**
// * Created by IntelliJ IDEA.
// * User: toto
// * Date: Aug 25, 2008
// * Time: 5:02:56 PM
// * To change this template use File | Settings | File Templates.
// */
//public class ContentFormDisplay  extends FlexTable {
//
//    private ContentDefinitionServiceAsync cdefService = ContentDefinitionService.App.getInstance();
////    private FileManagerServiceAsync fmService = FileManagerService.App.getInstance();
//
//    public ContentFormDisplay(final GWTJahiaPageContext page,
//                             final String type) {
//        super() ;
//
//        // add the action menu icon
//        setWidget(0, 0, new Label("toto")) ;
//
//        cdefService.getNodeType(type, new AsyncCallback<GWTJahiaNodeType>() {
//            public void onFailure(Throwable throwable) {
//                Window.alert("Failed to retrieve props");
//                throwable.printStackTrace();
//            }
//
//            public void onSuccess(GWTJahiaNodeType gwtNodeType) {
////                setWidget(0,0,new PropertiesEditor(null, result, null, null));
//                setWidget(0, 0, new Label("tata")) ;
//            }
//        });
//
////        fmService.getProperties(path, new AsyncCallback<Map<String, GWTJahiaNodeProperty>>() {
////            public void onFailure(Throwable throwable) {
////                Window.alert("Failed to retrieve props");
////            }
////
////            public void onSuccess(Map<String, GWTJahiaNodeProperty> result) {
////                String name = path.substring(path.lastIndexOf('/')+1);
////                GWTJahiaFSElement el = new GWTJahiaFSElement(name,path, new Date());
////                setWidget(0,0,new PropertiesEditor(el, result));
////            }
////        }
////
////        );
//    }
//
//
//
//}
