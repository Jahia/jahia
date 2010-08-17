<%@ include file="/admin/include/header.inc" %>
<%@ page import="org.jahia.bin.JahiaAdministration,
                 java.util.Properties" %>
<%@ page import="org.jahia.services.sites.*" %>
<%@ page import="org.jahia.services.analytics.*" %>

<%
    String jahiaGAprofile = "";
    String operation = "";
    String gaUserAccount = "";
    String gaLogin = "";
    String gaProfile = "";
    String gaPassword = "";
    boolean typeUrlVirtual = false;
    boolean typeUrlReal = false;
    boolean trackingEnabled = false;
    if (request.getParameter("sub") != null) {
        if (request.getParameter("sub").equals("displayEdit")) {
            jahiaGAprofile = request.getParameter("profile");
            operation = "saveEdit&profile=" + jahiaGAprofile;
            gaUserAccount = currentSite.getGoogleAnalytics().getAccount();
            gaLogin = currentSite.getGoogleAnalytics().getLogin();
            gaProfile = currentSite.getGoogleAnalytics().getProfile();
            gaPassword = currentSite.getGoogleAnalytics().getPassword();
            trackingEnabled = currentSite.getGoogleAnalytics().isEnabled();
            typeUrlVirtual = currentSite.getGoogleAnalytics().getTypeUrl().equals("virtual");
            typeUrlReal = currentSite.getGoogleAnalytics().getTypeUrl().equals("real");
        } else {
            operation = "add";
            gaUserAccount = (String) request.getAttribute("gaUserAccount");
            gaLogin = (String) request.getAttribute("gaLogin");
            gaProfile = (String) request.getAttribute("gaProfile");
            gaPassword = (String) request.getAttribute("gaPassword");
            jahiaGAprofile = (String) request.getAttribute("jahiaGAprofile");
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
        : <% if (currentSite != null) { %><fmt:message
                key="org.jahia.admin.site.label"/>&nbsp;<%=currentSite.getServerName() %><%} %></h2>
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
                                &nbsp;&nbsp;<%=request.getAttribute("warningMsg") == null ? "" : request.getAttribute("warningMsg") %>
                                &nbsp;
                            </p>
                            <% } %>

                            <form name="mainForm"
                                  action='<%=JahiaAdministration.composeActionURL(request,response,"analytics","&sub="+operation)%>'
                                  method="post">

                                <table border="0" cellpadding="5">
                                    <tbody>
                                    <tr>
                                        <td width="100%" colspan="2" align="center">
                                        </td>
                                    </tr>


                                    <tr>
                                        <td valign="top">
                                            <label><fmt:message
                                                    key="org.jahia.admin.site.ManageAnalytics.trackedUrls.label"/></label>&nbsp;
                                        </td>
                                        <td valign="top">
                                            <label><fmt:message
                                                    key="org.jahia.admin.site.ManageAnalytics.realUrls.label"/></label>
                                            <input type="radio" name="trackedUrls" value="real"
                                                   <%if(typeUrlReal) { %>checked<% }else{ %>checked<% }%> id="trackedUrls"/>
                                            <label><fmt:message
                                                    key="org.jahia.admin.site.ManageAnalytics.virtualUrls.label"/></label>
                                            <input type="radio" name="trackedUrls" value="virtual"
                                                   <% if(typeUrlVirtual) { %>checked<% }%> id="trackedUrls"/>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td>
                                            <fmt:message
                                                    key="org.jahia.admin.site.ManageAnalytics.gaUserAccount.label"/>&nbsp;
                                            <br><em><fmt:message
                                                key="org.jahia.admin.site.ManageAnalytics.egGAuserAccount.label"/></em>
                                        </td>
                                        <td>
                                            :&nbsp;<input class="input" type="text" name="gaUserAccount"
                                                          value="<%=(gaUserAccount == null?"": gaUserAccount)%>"
                                                          size="<%=inputSize%>" maxlength="50">
                                        </td>
                                    </tr>
                                    <tr>
                                        <td>
                                            <fmt:message key="org.jahia.admin.site.ManageAnalytics.gaProfile.label"/>&nbsp;
                                            <br><em><fmt:message
                                                key="org.jahia.admin.site.ManageAnalytics.egGAprofile.label"/></em>
                                        </td>
                                        <td>
                                            :&nbsp;<input class="input" type="text" name="gaProfile"
                                                          value="<%=(gaProfile == null?"":gaProfile)%>"
                                                          size="<%=inputSize%>" maxlength="50">
                                        </td>
                                    </tr>

                                    <tr>
                                        <td>
                                            <fmt:message key="org.jahia.admin.site.ManageAnalytics.gaLogin.label"/>&nbsp;
                                            <br><em><fmt:message
                                                key="org.jahia.admin.site.ManageAnalytics.egGaLogin.label"/></em>
                                        </td>
                                        <td>
                                            :&nbsp;<input class="input" type="text" name="gaLogin"
                                                          value="<%=(gaLogin == null?"":gaLogin)%>"
                                                          size="<%=inputSize%>" maxlength="50">
                                        </td>
                                    </tr>
                                    <tr>
                                        <td>
                                            <fmt:message key="org.jahia.admin.site.ManageAnalytics.gaPassword.label"/>&nbsp;
                                        </td>
                                        <td>
                                            :&nbsp;<input class="input" type="password" name="gaPassword"
                                                          value="<%=gaPassword%>"
                                                          size="<%=inputSize%>" maxlength="50">
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
                    <a class="ico-back"
                       href='<%=JahiaAdministration.composeActionURL(request,response,"analytics","&sub=display")%>'><fmt:message
                            key="label.backToMenu"/></a>
                  </span>
                </span>
                <span class="dex-PushButton">
                  <span class="first-child">
                    <a class="ico-delete"
                       href='<%=JahiaAdministration.composeActionURL(request,response,"analytics","&sub=delete&profile="+jahiaGAprofile)%>'><fmt:message
                            key="label.delete"/></a>
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
