<%--

    Jahia Enterprise Edition v6

    Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.

    Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
    with Document Management and Portal features.

    The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
    IMPLIED.

    Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
    you and Jahia (Jahia Sustainable Enterprise License - JSEL).

    If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.

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

    <h2 class="edit"><fmt:message key="org.jahia.admin.analytics.ManageAnalytics.label"/>
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
        <fmt:message key="org.jahia.admin.analytics.ManageAnalytics.label"/>
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
                    <a class="ico-back" href='<%=JahiaAdministration.composeActionURL(request,response,"analytics","&sub=display")%>'><fmt:message key="org.jahia.admin.backToMenu.label"/></a>
                  </span>
                </span>
                <span class="dex-PushButton">
                  <span class="first-child">
                    <a class="ico-delete" href='<%=JahiaAdministration.composeActionURL(request,response,"analytics","&sub=delete&profile="+jahiaGAprofile)%>'><fmt:message key="org.jahia.admin.delete.label"/></a>
                  </span>
                </span>
                <span class="dex-PushButton">
                  <span class="first-child">
                    <a class="ico-ok" href="javascript:sendForm();"><fmt:message key='org.jahia.admin.save.label'/></a>
                  </span>
                </span>
              </div>
</div>
</div>
<%@include file="/admin/include/footer.inc" %>
