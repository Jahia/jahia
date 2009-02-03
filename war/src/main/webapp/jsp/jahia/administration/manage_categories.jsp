<%--

    
    This file is part of Jahia: An integrated WCM, DMS and Portal Solution
    Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
    
    This program is free software; you can redistribute it and/or
    modify it under the terms of the GNU General Public License
    as published by the Free Software Foundation; either version 2
    of the License, or (at your option) any later version.
    
    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
    GNU General Public License for more details.
    
    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
    
    As a special exception to the terms and conditions of version 2.0 of
    the GPL (or any later version), you may redistribute this Program in connection
    with Free/Libre and Open Source Software ("FLOSS") applications as described
    in Jahia's FLOSS exception. You should have recieved a copy of the text
    describing the FLOSS exception, and it is also available here:
    http://www.jahia.com/license
    
    Commercial and Supported Versions of the program
    Alternatively, commercial and supported versions of the program may be used
    in accordance with the terms contained in a separate written agreement
    between you and Jahia Limited. If you are unsure which license is appropriate
    for your use, please contact the sales department at sales@jahia.com.

--%>

<%@ page import="org.jahia.resourcebundle.JahiaResourceBundle" %>
<%@ page import="org.jahia.services.categories.Category" %>
<%@ page import="org.jahia.services.usermanager.JahiaUser" %>
<%@ page import="org.jahia.bin.JahiaAdministration" %>
<%@ taglib uri="http://www.jahia.org/tags/templateLib" prefix="template" %>
<%@ taglib prefix="internal" uri="http://www.jahia.org/tags/internalLib" %>
<%@ include file="/jsp/jahia/administration/include/header.inc" %>
<% stretcherToOpen = 0; %>
<internal:gwtImport module="org.jahia.ajax.gwt.engines.categories.CategoriesManager"/>
<div id="topTitle">
    <h1>Jahia</h1>

    <h2 class="edit"><internal:adminResourceBundle resourceName="org.jahia.admin.manageCategories.label"/></h2>
</div>
<div id="main">
    <table style="width: 100%;" class="dex-TabPanel" cellpadding="0" cellspacing="0">
        <tbody>
        <tr>
            <td style="vertical-align: top;" align="left">
                <%@include file="/jsp/jahia/administration/include/tab_menu.inc" %>
            </td>
        </tr>
        <tr>
            <td style="vertical-align: top;" align="left" height="100%">
                <div class="dex-TabPanelBottom">
                    <div class="tabContent">
                        <%@include file="/jsp/jahia/administration/include/menu_server.inc" %>
                        <div id="content" class="fit">
                            <div class="head headtop">
                                <div class="object-title">
                                    <internal:adminResourceBundle
                                            resourceName="org.jahia.admin.manageCategories.label"/>
                                </div>
                            </div>
                            <div class="content-item-noborder">
                                <internal:categoryManager/>
                            </div>
                        </div>
                    </div>
            </td>
        </tr>
        </tbody>
    </table>
</div>
<div id="actionBar">
          <span class="dex-PushButton">
            <span class="first-child">
              <a class="ico-back"
                 href='<%=JahiaAdministration.composeActionURL(request,response,"displaymenu","")%>'><internal:adminResourceBundle
                      resourceName="org.jahia.admin.backToMenu.label"/></a>
            </span>
          </span>
</div>
</div>
<%@include file="/jsp/jahia/administration/include/footer.inc" %>
