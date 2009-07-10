<%--

    This file is part of Jahia: An integrated WCM, DMS and Portal Solution
    Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.

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
    in Jahia's FLOSS exception. You should have received a copy of the text
    describing the FLOSS exception, and it is also available here:
    http://www.jahia.com/license

    Commercial and Supported Versions of the program
    Alternatively, commercial and supported versions of the program may be used
    in accordance with the terms contained in a separate written agreement
    between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
    for your use, please contact the sales department at sales@jahia.com.

--%>
<%@ page import="org.jahia.services.usermanager.JahiaUserManagerProvider" %>
<%@ include file="/admin/include/header.inc" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.List" %>
<%
final List sites = (List) request.getAttribute("sites");
List usrProviders = (List) request.getAttribute("usrProviders");
if (usrProviders == null) {
usrProviders = new ArrayList();
}
stretcherToOpen   = 0; %>
<internal:gwtInit modules="org.jahia.ajax.gwt.module.engines.Engines"/>
<div id="topTitle">
  <h1>Jahia</h1>
  <h2 class="edit"><fmt:message key="org.jahia.admin.site.ManageSites.manageVirtualSites.label"/></h2>
</div>
<div id="main">
  <table style="width: 100%;" class="dex-TabPanel" cellpadding="0" cellspacing="0">
    <tbody>
      <tr>
        <td style="vertical-align: top;" align="left">
          <%@include file="/admin/include/tab_menu.inc" %>
        </td>
      </tr>
      <tr>
        <td style="vertical-align: top;" align="left" height="100%">
          <div class="dex-TabPanelBottom">
            <div class="tabContent">
                <jsp:include page="/admin/include/left_menu.jsp">
                    <jsp:param name="mode" value="server"/>
                </jsp:include>
              <div id="content" class="fit">
                <div class="head">
                  <div class="object-title">
                    <fmt:message key="org.jahia.admin.site.ManageSites.doYouWantToContinue.label"/>
                  </div>
                </div><% if (sites == null || sites.size() == 0) { %>
                <div class="error">
                  <fmt:message key="org.jahia.admin.site.ManageSites.noSiteSpecified.label"/>
                </div><% } else { %>
                <form method="POST" action='<%=JahiaAdministration.composeActionURL(request,response,"sites","")%>' name="mainForm">
                  <input type="hidden" name="sub" value="processmultipledelete"/>
                  <p class="errorbold">
                    <fmt:message key="org.jahia.admin.site.ManageSites.pleaseBeCareful.label"/>
                  </p>
                  <table class="evenOddTable tBorder" border="0" cellpadding="5" cellspacing="0" width="100%">
                    <tr>
                      <th>
                        <fmt:message key="org.jahia.admin.site.ManageSites.siteTitle.label"/>&nbsp;
                      </th>
                      <th>
                        <fmt:message key="org.jahia.admin.site.ManageSites.siteServerName.label"/>&nbsp;
                      </th>
                      <th>
                        <fmt:message key="org.jahia.admin.site.ManageSites.siteKey.label"/>&nbsp;
                      </th>
                      <th>
                        <fmt:message key="org.jahia.admin.site.ManageSites.siteDesc.label"/>&nbsp;
                      </th>
                    </tr><%
                    String lineClass = "oddLine";
                    for (Iterator iterator = sites.iterator(); iterator.hasNext();) {
                    JahiaSite site = (JahiaSite) iterator.next();
                    if ("oddLine".equals(lineClass)) {
                    lineClass = "evenLine";
                    } else {
                    lineClass = "oddLine";
                    } %>
                    <input type="hidden" name="sitebox" value="<%= site.getSiteKey() %>"/>
                    <tr class="<%=lineClass%>">
                      <td>
                        <%=site.getTitle() %>
                      </td>
                      <td>
                        <%=site.getServerName() %>
                      </td>
                      <td>
                        <%=site.getSiteKey() %>
                      </td>
                      <td>
                        <%=site.getDescr() %>
                      </td>
                    </tr>
                    <%
                    } %>
                  </table>
                  <div class="head headtop">
                    <div class="object-title">
                      <fmt:message key="org.jahia.admin.site.ManageSites.purgeOptions.label"/>
                    </div>
                  </div>
                  <table>
                    <tr>
                      <td>
                        <table border="0" cellpadding="0" cellspacing="0">
                        <%-- 
                          <tr>
                            <td>
                              <input name="deleteTemplates" type="checkbox" value="1" checked>&nbsp;<fmt:message key="org.jahia.admin.site.ManageSites.deleteSiteTemplates.label"/>
                            </td>
                          </tr>
                          --%>
                          <tr>
                            <td>
                              <input name="deleteFileRepository" type="checkbox" value="1" checked>&nbsp;<fmt:message key="org.jahia.admin.site.ManageSites.deleteSiteFileRepository.label"/>
                            </td>
                          </tr>
                        </table>
                      </td>
                    </tr>
                  </table>
                </form><% } %>
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
              <a class="ico-cancel" href='<%=JahiaAdministration.composeActionURL(request,response,"sites","&sub=list")%>'><fmt:message key="org.jahia.admin.cancel.label"/></a>
            </span>
          </span>
          <span class="dex-PushButton">
            <span class="first-child">
              <a class="ico-delete" href="#" onclick="javascript:{ showWorkInProgress(); document.mainForm.submit(); return false; }"><fmt:message key="org.jahia.admin.delete.label"/></a>
            </span>
          </span>
        </div>
      </div><%@include file="/admin/include/footer.inc" %>