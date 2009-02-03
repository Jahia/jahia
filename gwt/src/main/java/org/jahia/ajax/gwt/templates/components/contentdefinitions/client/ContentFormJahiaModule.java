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

//package org.jahia.ajax.gwt.templates.components.contentdefinitions.client;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import org.jahia.ajax.gwt.config.client.beans.GWTJahiaPageContext;
//import org.jahia.ajax.gwt.templates.commons.client.module.JahiaModule;
//import org.jahia.ajax.gwt.templates.commons.client.util.TemplatesDOMUtil;
//import org.jahia.ajax.gwt.templates.components.contentdefinitions.client.ui.ContentFormDisplay;
//
//import com.google.gwt.user.client.DOM;
//import com.google.gwt.user.client.ui.RootPanel;
//
///**
// * Created by IntelliJ IDEA.
// * User: toto
// * Date: Aug 25, 2008
// * Time: 4:33:34 PM
// * To change this template use File | Settings | File Templates.
// */
//public class ContentFormJahiaModule extends JahiaModule {
//
//    public String getJahiaModuleType() {
//       return "contentform";
//   }
//
//    public void onModuleLoad(GWTJahiaPageContext page, List<RootPanel> rootPanels) {
//
//        for (final RootPanel propPane : rootPanels) {
//            final String path = DOM.getElementAttribute(propPane.getElement(), "type");
//            if (path != null) {
//                propPane.add(new ContentFormDisplay(page, path));
//            }
//        }
//    }
//
//    public List<RootPanel> getRootPanels() {
//        List<String> actionSlotsIds = TemplatesDOMUtil.getElementsIdsByJahiaType(RootPanel.getBodyElement(), "contentform") ;
//        List<RootPanel> panels = new ArrayList<RootPanel>() ;
//        for (String actionSlotsId : actionSlotsIds) {
//            panels.add(RootPanel.get(actionSlotsId));
//        }
//        return panels ;
//    }
//
//}
