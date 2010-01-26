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
<%@ include file="/admin/include/header.inc" %>
<%@ page import="org.jahia.bin.JahiaAdministration,
                 java.util.Properties" %>
<%
    Properties settings = currentSite.getSettings();
    int profileCnt = 0;
    if(settings.get("profileCnt_"+currentSite.getSiteKey()) != null) {profileCnt = Integer.parseInt((String)settings.get("profileCnt_"+currentSite.getSiteKey()));}
    String jahiaGAprofile = "";
    if((String)request.getAttribute("profile") != null){jahiaGAprofile = (String)request.getAttribute("profile");}
    String operation = "";
    String readonly = "";
    if(request.getParameter("sub") != null)
    {
        if(((String)request.getParameter("sub")).equals("displayEdit")){
        operation = "saveEdit&profile="+jahiaGAprofile;
        }else {
            operation = "add";
            }
    }


%>
<script type="text/javascript">

function sendForm() {
    document.mainForm.submit();
}



//-->
</script>
<div id="topTitle">
    <h1>Jahia</h1>

    <h2 class="edit"><fmt:message key="label.manageAnalytics"/>
        : <% if (currentSite != null) { %><fmt:message key="org.jahia.admin.site.label"/>&nbsp;<%=currentSite.getServerName() %><%} %></h2>
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
    <jsp:param name="mode" value="site"/>
</jsp:include>

<div id="content" class="fit">
<div class="head headtop">
    <div class="object-title">
        <fmt:message key="label.manageAnalytics"/>
    </div>
</div>



<% if (request.getAttribute("warningMsg") != null) { %>
<p class="errorbold">
    &nbsp;&nbsp;<%=request.getAttribute("warningMsg") == null ? "" : request.getAttribute("warningMsg") %>&nbsp;
</p>
<% } %>

 <form name="mainForm" action='<%=JahiaAdministration.composeActionURL(request,response,"analytics","&sub="+operation)%>' method="post">

    <table border="0" cellpadding="5">
        <tbody>
        <tr>
            <td width="100%" colspan="2" align="center">
            </td>
        </tr>


            <tr>
            <td valign="top">
                <label for="trackingEnabled"><fmt:message key="org.jahia.admin.site.ManageAnalytics.enableTracking.label"/></label>&nbsp;
            </td>
            <td valign="top">
                :&nbsp;<input type="checkbox" name="trackingEnabled" <% if (Boolean.valueOf(settings.getProperty(jahiaGAprofile+"_"+currentSite.getSiteKey()+"_trackingEnabled"))) { %>checked<% } %> id="trackingEnabled" />
            </td>
        </tr>
            <tr>
            <td valign="top">
                <label><fmt:message key="org.jahia.admin.site.ManageAnalytics.trackedUrls.label"/></label>&nbsp;
            </td>
            <td valign="top">
                 <label><fmt:message key="org.jahia.admin.site.ManageAnalytics.realUrls.label"/></label><input type="radio" name="trackedUrls" value = "real" <%if(settings.getProperty(jahiaGAprofile+"_"+currentSite.getSiteKey()+"_trackedUrls")!=null){if ((settings.getProperty(jahiaGAprofile+"_"+currentSite.getSiteKey()+"_trackedUrls").equals("real"))) { %>checked<% }}else{ %>checked<% }%> id="trackedUrls" />
                 <label><fmt:message key="org.jahia.admin.site.ManageAnalytics.virtualUrls.label"/></label><input type="radio" name="trackedUrls" value = "virtual"<% if(settings.getProperty(jahiaGAprofile+"_"+currentSite.getSiteKey()+"_trackedUrls")!=null){ if ((settings.getProperty(jahiaGAprofile+"_"+currentSite.getSiteKey()+"_trackedUrls").equals("virtual"))) { %>checked<% }} %> id="trackedUrls" />
            </td>
        </tr>

        <tr>
            <td>
                <fmt:message key="org.jahia.admin.site.ManageAnalytics.jahiaGAprofileName.label"/>&nbsp;
                <br><em><fmt:message key="org.jahia.admin.site.ManageAnalytics.egJahiaGAprofileName.label"/></em>
            </td>
            <td>
                :&nbsp;<input class="input" type="text" name="jahiaGAprofile" value="<%=jahiaGAprofile%>" <%if(!operation.equals("add")){%> readonly="readonly" <%}%> size="<%=inputSize%>" maxlength="50">
            </td>
        </tr>

           <tr>
            <td>
                <fmt:message key="org.jahia.admin.site.ManageAnalytics.gaUserAccount.label"/>&nbsp;
                <br><em><fmt:message key="org.jahia.admin.site.ManageAnalytics.egGAuserAccount.label"/></em>
            </td>
            <td>
                :&nbsp;<input class="input" type="text" name="gaUserAccount" value="<%if((settings.getProperty(jahiaGAprofile+"_"+currentSite.getSiteKey()+"_gaUserAccount") !=null )){%><%=settings.getProperty(jahiaGAprofile+"_"+currentSite.getSiteKey()+"_gaUserAccount")%><%}%>" size="<%=inputSize%>" maxlength="50">
            </td>
        </tr>
        <tr>
            <td>
                <fmt:message key="org.jahia.admin.site.ManageAnalytics.gaProfile.label"/>&nbsp;
                <br><em><fmt:message key="org.jahia.admin.site.ManageAnalytics.egGAprofile.label"/></em>
            </td>
            <td>
                :&nbsp;<input class="input" type="text" name="gaProfile" value="<%if((settings.getProperty(jahiaGAprofile+"_"+currentSite.getSiteKey()+"_gaProfile") !=null )){%><%=settings.getProperty(jahiaGAprofile+"_"+currentSite.getSiteKey()+"_gaProfile")%><%}%>" size="<%=inputSize%>" maxlength="50">
            </td>
        </tr>

        <tr>
            <td>
                <fmt:message key="org.jahia.admin.site.ManageAnalytics.gaLogin.label"/>&nbsp;
                <br><em><fmt:message key="org.jahia.admin.site.ManageAnalytics.egGaLogin.label"/></em>
            </td>
            <td>
                :&nbsp;<input class="input" type="text" name="gaLogin" value="<%if((settings.getProperty(jahiaGAprofile+"_"+currentSite.getSiteKey()+"_gaLogin") !=null )){%><%=settings.getProperty(jahiaGAprofile+"_"+currentSite.getSiteKey()+"_gaLogin")%><%}%>" size="<%=inputSize%>" maxlength="50">
            </td>
        </tr>
        <tr>
            <td>
                <fmt:message key="org.jahia.admin.site.ManageAnalytics.gaPassword.label"/>&nbsp;
            </td>
            <td>
                :&nbsp;<input class="input" type="password" name="gaPassword" value="<%if((settings.getProperty(jahiaGAprofile+"_"+currentSite.getSiteKey()+"_gaPassword") !=null )){%><%=settings.getProperty(jahiaGAprofile+"_"+currentSite.getSiteKey()+"_gaPassword")%><%}%>" size="<%=inputSize%>" maxlength="50">
            </td>
        </tr>

        <%if(request.getAttribute("gaError") != null)
        {
        %>
            <tr>
            <td></td>
            <td>
                <p class='error'>
                          <%=request.getAttribute("gaError")%>
                </p>
            </td>
        </tr>
        <%}
        %>
         <%if(request.getAttribute("jahiaProfileInUse") != null)
        {
        %>
            <tr>
            <td></td>
            <td>
                <p class='error'>
                          <%=request.getAttribute("jahiaProfileInUse")%>
                </p>
            </td>
        </tr>
        <%}
        %>
    </table>
</form>
</div>
</td>
</tr>
</tbody>
</table>
 <div id="actionBar">
                <span class="dex-PushButton">
                  <span class="first-child">
                    <a class="ico-back" href='<%=JahiaAdministration.composeActionURL(request,response,"analytics","&sub=display")%>'><fmt:message key="label.backToMenu"/></a>
                  </span>
                </span>
                <span class="dex-PushButton">
                  <span class="first-child">
                    <a class="ico-delete" href='<%=JahiaAdministration.composeActionURL(request,response,"analytics","&sub=delete&profile="+jahiaGAprofile)%>'><fmt:message key="label.delete"/></a>
                  </span>
                </span>
                <span class="dex-PushButton">
                  <span class="first-child">
                    <a class="ico-ok" href="javascript:sendForm();"><fmt:message key='label.save'/></a>
                  </span>
                </span>
              </div>
</div>
</div>
<%@include file="/admin/include/footer.inc" %>
