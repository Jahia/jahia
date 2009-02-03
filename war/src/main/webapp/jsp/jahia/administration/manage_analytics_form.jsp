<%--
Copyright 2002-2008 Jahia Ltd

Licensed under the JAHIA COMMON DEVELOPMENT AND DISTRIBUTION LICENSE (JCDDL),
Version 1.0 (the "License"), or (at your option) any later version; you may
not use this file except in compliance with the License. You should have
received a copy of the License along with this program; if not, you may obtain
a copy of the License at

 http://www.jahia.org/license/

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
--%>
<%@ include file="/jsp/jahia/administration/include/header.inc" %>
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

    <h2 class="edit"><internal:adminResourceBundle resourceName="org.jahia.admin.analytics.ManageAnalytics.label"/>
        : <% if (currentSite != null) { %><internal:adminResourceBundle resourceName="org.jahia.admin.site.label"/>&nbsp;<%=currentSite.getServerName() %><%} %></h2>
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
    <jsp:param name="mode" value="site"/>
</jsp:include>

<div id="content" class="fit">
<div class="head headtop">
    <div class="object-title">
        <internal:adminResourceBundle resourceName="org.jahia.admin.analytics.ManageAnalytics.label"/>
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
                <label for="trackingEnabled"><internal:adminResourceBundle resourceName="org.jahia.admin.site.ManageAnalytics.enableTracking.label"/></label>&nbsp;
            </td>
            <td valign="top">
                :&nbsp;<input type="checkbox" name="trackingEnabled" <% if (Boolean.valueOf(settings.getProperty(jahiaGAprofile+"_"+currentSite.getSiteKey()+"_trackingEnabled"))) { %>checked<% } %> id="trackingEnabled" />
            </td>
        </tr>
            <tr>
            <td valign="top">
                <label><internal:adminResourceBundle resourceName="org.jahia.admin.site.ManageAnalytics.trackedUrls.label"/></label>&nbsp;
            </td>
            <td valign="top">
                 <label><internal:adminResourceBundle resourceName="org.jahia.admin.site.ManageAnalytics.realUrls.label"/></label><input type="radio" name="trackedUrls" value = "real" <%if(settings.getProperty(jahiaGAprofile+"_"+currentSite.getSiteKey()+"_trackedUrls")!=null){if ((settings.getProperty(jahiaGAprofile+"_"+currentSite.getSiteKey()+"_trackedUrls").equals("real"))) { %>checked<% }}else{ %>checked<% }%> id="trackedUrls" />
                 <label><internal:adminResourceBundle resourceName="org.jahia.admin.site.ManageAnalytics.virtualUrls.label"/></label><input type="radio" name="trackedUrls" value = "virtual"<% if(settings.getProperty(jahiaGAprofile+"_"+currentSite.getSiteKey()+"_trackedUrls")!=null){ if ((settings.getProperty(jahiaGAprofile+"_"+currentSite.getSiteKey()+"_trackedUrls").equals("virtual"))) { %>checked<% }} %> id="trackedUrls" />
            </td>
        </tr>

        <tr>
            <td>
                <internal:adminResourceBundle resourceName="org.jahia.admin.site.ManageAnalytics.jahiaGAprofileName.label"/>&nbsp;
                <br><em><internal:adminResourceBundle resourceName="org.jahia.admin.site.ManageAnalytics.egJahiaGAprofileName.label"/></em>
            </td>
            <td>
                :&nbsp;<input class="input" type="text" name="jahiaGAprofile" value="<%=jahiaGAprofile%>" <%if(!operation.equals("add")){%> readonly="readonly" <%}%> size="<%=inputSize%>" maxlength="50">
            </td>
        </tr>

           <tr>
            <td>
                <internal:adminResourceBundle resourceName="org.jahia.admin.site.ManageAnalytics.gaUserAccount.label"/>&nbsp;
                <br><em><internal:adminResourceBundle resourceName="org.jahia.admin.site.ManageAnalytics.egGAuserAccount.label"/></em>
            </td>
            <td>
                :&nbsp;<input class="input" type="text" name="gaUserAccount" value="<%if((settings.getProperty(jahiaGAprofile+"_"+currentSite.getSiteKey()+"_gaUserAccount") !=null )){%><%=settings.getProperty(jahiaGAprofile+"_"+currentSite.getSiteKey()+"_gaUserAccount")%><%}%>" size="<%=inputSize%>" maxlength="50">
            </td>
        </tr>
        <tr>
            <td>
                <internal:adminResourceBundle resourceName="org.jahia.admin.site.ManageAnalytics.gaProfile.label"/>&nbsp;
                <br><em><internal:adminResourceBundle resourceName="org.jahia.admin.site.ManageAnalytics.egGAprofile.label"/></em>
            </td>
            <td>
                :&nbsp;<input class="input" type="text" name="gaProfile" value="<%if((settings.getProperty(jahiaGAprofile+"_"+currentSite.getSiteKey()+"_gaProfile") !=null )){%><%=settings.getProperty(jahiaGAprofile+"_"+currentSite.getSiteKey()+"_gaProfile")%><%}%>" size="<%=inputSize%>" maxlength="50">
            </td>
        </tr>

        <tr>
            <td>
                <internal:adminResourceBundle resourceName="org.jahia.admin.site.ManageAnalytics.gaLogin.label"/>&nbsp;
                <br><em><internal:adminResourceBundle resourceName="org.jahia.admin.site.ManageAnalytics.egGaLogin.label"/></em>
            </td>
            <td>
                :&nbsp;<input class="input" type="text" name="gaLogin" value="<%if((settings.getProperty(jahiaGAprofile+"_"+currentSite.getSiteKey()+"_gaLogin") !=null )){%><%=settings.getProperty(jahiaGAprofile+"_"+currentSite.getSiteKey()+"_gaLogin")%><%}%>" size="<%=inputSize%>" maxlength="50">
            </td>
        </tr>
        <tr>
            <td>
                <internal:adminResourceBundle resourceName="org.jahia.admin.site.ManageAnalytics.gaPassword.label"/>&nbsp;
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
                    <a class="ico-back" href='<%=JahiaAdministration.composeActionURL(request,response,"analytics","&sub=display")%>'><internal:adminResourceBundle resourceName="org.jahia.admin.backToMenu.label"/></a>
                  </span>
                </span>
                <span class="dex-PushButton">
                  <span class="first-child">
                    <a class="ico-delete" href='<%=JahiaAdministration.composeActionURL(request,response,"analytics","&sub=delete&profile="+jahiaGAprofile)%>'><internal:adminResourceBundle resourceName="org.jahia.admin.delete.label"/></a>
                  </span>
                </span>
                <span class="dex-PushButton">
                  <span class="first-child">
                    <a class="ico-ok" href="javascript:sendForm();"><internal:adminResourceBundle resourceName='org.jahia.admin.save.label'/></a>
                  </span>
                </span>
              </div>
</div>
</div>
<%@include file="/jsp/jahia/administration/include/footer.inc" %>
