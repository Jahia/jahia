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

<%@page import="org.jahia.bin.*" %>
<%@include file="/jsp/jahia/administration/include/header.inc" %>
<%@page import = "org.jahia.services.usermanager.*" %>
<%
String  warningMsg		= (String)request.getAttribute("warningMsg");
String  siteTitle		= (String)request.getAttribute("siteTitle");
String  siteServerName	= (String)request.getAttribute("siteServerName");
String  siteDescr		= (String)request.getAttribute("siteDescr");
String  siteKey         = (String)request.getAttribute("siteKey");
List 	usrProviders 	= (List)request.getAttribute("usrProviders");
stretcherToOpen   = 0;
if ( usrProviders == null ){
usrProviders = new ArrayList();
} %>
<internal:gwtInit modules="org.jahia.ajax.gwt.module.engines.Engines"/>
<div id="topTitle">
  <h1>Jahia</h1>
  <h2 class="edit"><utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.site.ManageSites.manageVirtualSites.label"/></h2>
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
                <jsp:include page="/jsp/jahia/administration/include/left_menu.jsp">
                    <jsp:param name="mode" value="server"/>
                </jsp:include>
              <div id="content" class="fit">
                <div class="head">
                  <div class="object-title">
                    <utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.site.ManageSites.doYouWantToContinue.label"/>
                  </div>
                </div>
                <form method="post" action='<%=JahiaAdministration.composeActionURL(request,response,"sites","&sub=processdelete&siteid=" + request.getParameter("siteid"))%>' name="mainForm">
                  <p class="errorbold">
                    <utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.site.ManageSites.pleaseBeCareful.label"/>
                  </p>
                  <table border="0" cellpadding="10" cellspacing="0" style="width:100%" class="topAlignedTable">
                    <tr>
                      <td>
                        <utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.site.ManageSites.siteTitle.label"/>&nbsp;
                      </td>
                      <td>
                        :&nbsp;<%=siteTitle %>
                      </td>
                    </tr>
                    <tr>
                      <td>
                        <utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.site.ManageSites.siteServerName.label"/>&nbsp;
                      </td>
                      <td>
                        :&nbsp;<%=siteServerName %>
                      </td>
                    </tr>
                    <tr>
                      <td>
                        <utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.site.ManageSites.siteKey.label"/>&nbsp;
                      </td>
                      <td>
                        :&nbsp;<%=siteKey %>
                      </td>
                    </tr>
                    <tr>
                      <td>
                        <utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.site.ManageSites.siteDesc.label"/>&nbsp;
                      </td>
                      <td>
                        &nbsp;
                        <textarea class="input" name="siteDescr" rows="6" cols='45'><%=siteDescr %></textarea>
                      </td>
                    </tr>
                    <tr>
                      <td>
                        <utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.site.ManageSites.purgeOptions.label"/>&nbsp;:
                      </td>
                      <td>
                        <table border="0" cellpadding="0" cellspacing="0">
                        <%-- 
                          <tr>
                            <td>
                              <input name="deleteTemplates" type="checkbox" value="1" checked>&nbsp;<utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.site.ManageSites.deleteSiteTemplates.label"/>
                            </td>
                          </tr>
                          --%>
                          <tr>
                            <td>
                              <input name="deleteFileRepository" type="checkbox" value="1" checked>&nbsp;<utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.site.ManageSites.deleteSiteFileRepository.label"/>
                            </td>
                          </tr>
                        </table>
                      </td>
                    </tr>
                  </table>
                </form>
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
              <a class="ico-back" class="operationLink" href='<%=JahiaAdministration.composeActionURL(request,response,"sites","&sub=list")%>'><utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.site.ManageSites.backToSitesList.label"/></a>
            </span>
          </span>
          <span class="dex-PushButton">
            <span class="first-child">
              <a class="ico-cancel" href='<%=JahiaAdministration.composeActionURL(request,response,"sites","&sub=edit&siteid=" + request.getParameter("siteid"))%>'><utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.cancel.label"/></a>
            </span>
          </span>
          <span class="dex-PushButton">
            <span class="first-child">
              <a class="ico-delete" href="#" onclick="javascript:{ showWorkInProgress(); document.mainForm.submit(); return false; }"><utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.delete.label"/></a>
            </span>
          </span>
        </div>
      </div><%@include file="/jsp/jahia/administration/include/footer.inc" %>
