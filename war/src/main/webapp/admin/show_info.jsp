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

<%@include file="/admin/include/header.inc" %>
<%@page import = "java.util.*" %>
<%@page import = "org.jahia.security.license.*" %>
<%@page import="org.jahia.bin.*" %>
<%@page import="org.jahia.resourcebundle.*" %>
<%@page import="org.jahia.params.*" %>
<%@page import="java.text.SimpleDateFormat" %>
<%@ page import="org.jahia.engines.calendar.CalendarHandler" %>
<%
ProcessingContext jParams = null;
if (jData != null) {
jParams = jData.params();
}
String  nbCurrentSites     = (String) request.getAttribute("nbCurrentSites");
String  nbMaxSites         = (String) request.getAttribute("nbMaxSites");
String  nbCurrentUsers     = (String) request.getAttribute("nbCurrentUsers");
String  nbMaxUsers         = (String) request.getAttribute("nbMaxUsers");
String  nbCurrentTemplates = (String) request.getAttribute("nbCurrentTemplates");
String  nbMaxTemplates     = (String) request.getAttribute("nbMaxTemplates");
String  nbCurrentPages     = (String) request.getAttribute("nbCurrentPages");
String  nbMaxPages         = (String) request.getAttribute("nbMaxPages");
String  release            = (String) request.getAttribute("release");
String  build              = (String) request.getAttribute("build");
String  jahiaEdition       = (String) request.getAttribute("jahiaEdition");
List licenses         = (List) request.getAttribute("licenses");
Date expirationDate = null;
SimpleDateFormat dateFormatter = new SimpleDateFormat(CalendarHandler.DEFAULT_DATE_FORMAT);
int maxDays = -1;
if (request.getAttribute("expirationDate") != null) {
expirationDate = (Date) request.getAttribute("expirationDate");
maxDays = ((Integer) request.getAttribute("allowedDays")).intValue();
}
String jahiaEditionTitle = JahiaResourceBundle.getJahiaInternalResource("org.jahia.admin.info.LicenceInfo.jahiaEdition." + jahiaEdition + ".label", jData.getProcessingContext().getLocale());
stretcherToOpen   = 0; %>
<div id="topTitle">
  <h1>Jahia</h1>
  <h2 class="edit"><fmt:message key="org.jahia.admin.aboutJahia.label"/></h2>
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
                  <fmt:message key="org.jahia.admin.info.LicenceInfo.jahiaRelease.label"/><%=release %>, <%= jahiaEditionTitle %>, <fmt:message key="org.jahia.admin.build.label"/><%=build %>
                </div>
              </div>
              <div class="content-body">
                <span style="padding: 5px;display:block;border-bottom: 1px solid #B7CBD8;">
                  <fmt:message key="org.jahia.admin.info.LicenceInfo.licenceIsValid.label"/>
                </span>
                <table class="evenOddTable" border="0" cellpadding="5" cellspacing="0" width="100%">
                  <thead>
                    <tr>
                      <th width="50%">
                        <fmt:message key="org.jahia.admin.info.LicenceInfo.resourceType.label"/>
                      </th>
                      <th width="25%">
                        <fmt:message key="org.jahia.admin.info.LicenceInfo.current.label"/>
                      </th>
                      <th class="lastCol" width="25%">
                        <fmt:message key="org.jahia.admin.info.LicenceInfo.maximun.label"/>
                      </th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr class="evenLine">
                      <td width="50%">
                        <fmt:message key="org.jahia.admin.info.LicenceInfo.numberOfSites.label"/>
                      </td>
                      <td width="25%">
                        <b><%=nbCurrentSites %></b>
                      </td>
                      <td width="25%" class="lastCol">
                        <b><%=nbMaxSites %></b>
                      </td>
                    </tr>
                    <tr class="oddLine">
                      <td width="50%">
                        <fmt:message key="org.jahia.admin.info.LicenceInfo.numberOfUsers.label"/>
                      </td>
                      <td width="25%">
                        <b><%=nbCurrentUsers %></b>
                      </td>
                      <td width="25%" class="lastCol">
                        <b><%=nbMaxUsers %></b>
                      </td>
                    </tr>
                    <tr class="evenLine">
                      <td width="50%">
                        <fmt:message key="org.jahia.admin.info.LicenceInfo.numberOfTemplates.label"/>
                      </td>
                      <td width="25%">
                        <b><%=nbCurrentTemplates %></b>
                      </td>
                      <td width="25%" class="lastCol">
                        <b><%=nbMaxTemplates %></b>
                      </td>
                    </tr>
                    <tr class="oddLine">
                      <td width="50%">
                        <fmt:message key="org.jahia.admin.info.LicenceInfo.numberOfPages.label"/>
                      </td>
                      <td width="25%">
                        <b><%=nbCurrentPages %></b>
                      </td>
                      <td width="25%" class="lastCol">
                        <b><%=nbMaxPages %></b>
                      </td>
                    </tr>
                    <% if (expirationDate != null) { %>
                    <tr class="evenLine">
                      <td width="50%">
                        <fmt:message key="org.jahia.admin.info.LicenceInfo.expirationDate.label"/>
                      </td>
                      <td width="25%">
                        <b><%=dateFormatter.format(new Date()) %></b>
                      </td>
                      <td width="25%" class="lastCol">
                        <b><%=dateFormatter.format(expirationDate) %></b>
                      </td>
                    </tr>
                    <% } %>
                    <% Iterator licenseIter = licenses.iterator();
                    String lineClass = "oddLine";
                    while (licenseIter.hasNext()) {
                    License curLicense = (License) licenseIter.next();
                    boolean activated = curLicense.checkLimits();
                    String componentTitle = JahiaResourceBundle.getJahiaInternalResource(curLicense.getComponentName() + ".label",
                                                                                         jData.getProcessingContext().getLocale());
                    if (componentTitle == null) {
                    componentTitle = curLicense.getComponentName();
                    } %>
                    <tr class="<%=lineClass%>">
                      <td width="50%">
                        <b><%=componentTitle %></b>
                      </td>
                      <td width="25%">
                        <% if (activated) { %>
                        <fmt:message key="org.jahia.admin.info.LicenceInfo.licenseActivated.label"/><% } else { %>
                        <fmt:message key="org.jahia.admin.info.LicenceInfo.licenseDeactivated.label"/><% } %>
                      </td>
                      <td width="25%" class="lastCol">
                        &nbsp;
                      </td>
                    </tr>
                    <%
                    if ("oddLine".equals(lineClass)) {
                    lineClass = "evenLine";
                    } else {
                    lineClass = "oddLine";
                    }
                    } %>
                  </tbody>
                </table>
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
              <a class="ico-back" href='<%=JahiaAdministration.composeActionURL(request,response,"displaymenu","")%>'><fmt:message key="org.jahia.admin.backToMenu.label"/></a>
            </span>
          </span>
        </div>
      </div><%@include file="/admin/include/footer.inc" %>
