<%@ include file="/admin/include/header.inc" %>
<%@ page import="org.jahia.bin.JahiaAdministration" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="org.jahia.services.analytics.GoogleAnalyticsProfile" %>

<script type="text/javascript">

    function sendForm() {
        document.mainForm.submit();
    }
</script>
<div id="topTitle">
    <h1>Jahia</h1>

    <h2 class="edit"><fmt:message key="label.manageAnalytics"/>
        : <% if (currentSite != null) { %><fmt:message
                key="org.jahia.admin.site.label"/>&nbsp;<%=currentSite.getTitle() %><%} %></h2>
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
                            <form name="mainForm"
                                  action='<%=JahiaAdministration.composeActionURL(request,response,"analytics","&sub=commit")%>'
                                  method="post">
                                <table class="evenOddTable" border="0" cellpadding="5" cellspacing="0" width="100%">
                                    <thead>
                                    <tr>
                                        <th width="35%">
                                            <fmt:message
                                                    key="org.jahia.admin.site.ManageAnalytics.gaProfileName.label"/>
                                        </th>
                                        <th width="12%" style="white-space: nowrap">
                                            <fmt:message key="org.jahia.admin.site.ManageAnalytics.gaUserAcc.label"/>
                                        </th>
                                        <th width="12%" style="white-space: nowrap">
                                            <fmt:message
                                                    key="org.jahia.admin.site.ManageAnalytics.realUrlsTracked.label"/>
                                        </th>
                                        <th width="12%" style="white-space: nowrap">
                                            <fmt:message
                                                    key="org.jahia.admin.site.ManageAnalytics.virtualUrlsTracked.label"/>
                                        </th>
                                        <th width="15%" class="lastCol">
                                            <fmt:message key="label.action"/>
                                        </th>
                                    </tr>
                                    </thead>
                                        <%
                                        GoogleAnalyticsProfile googleAnalyticsProfile = currentSite.getGoogleAnalyticsProfil();
                                        String jahiaGAprofile= "ga";
                                         if (googleAnalyticsProfile !=null && googleAnalyticsProfile.isEnabled()) {
                                    %>
                                    <tr class="evenLine" id="<%=jahiaGAprofile%>">
                                        <td><%=googleAnalyticsProfile.getProfile()%>
                                        </td>
                                        <td><%=googleAnalyticsProfile.getAccount()%>
                                        </td>
                                        <td><input type="radio" value="real" name="<%=jahiaGAprofile%>TrackedUrls"
                                                   <% if (googleAnalyticsProfile.getTypeUrl().equals("real")) { %>checked<% } %>
                                                   id="<%=jahiaGAprofile%>TrackedUrls"/></td>
                                        <td><input type="radio" value="virtual" name="<%=jahiaGAprofile%>TrackedUrls"
                                                   <% if (googleAnalyticsProfile.getTypeUrl().equals("virtual")) { %>checked<% } %>
                                                   id="<%=jahiaGAprofile%>TrackedUrls"/></td>
                                        <td class="lastCol">
                                            <a href='<%=JahiaAdministration.composeActionURL(request,response,"analytics","&sub=displayEdit&profile="+jahiaGAprofile )%>'
                                               title="<fmt:message key='label.edit'/>"><img
                                                    src="<%=URL%>images/icons/admin/adromeda/edit.png"
                                                    alt="<fmt:message key='label.edit'/>"
                                                    title="<fmt:message key='label.edit'/>"
                                                    width="16"
                                                    height="16" border="0"/></a>&nbsp;
                                            <a href='<%=JahiaAdministration.composeActionURL(request,response,"analytics","&sub=delete&profile="+jahiaGAprofile )%>'
                                               title="<fmt:message key='label.delete'/>"><img
                                                    src="<%=URL%>images/icons/admin/adromeda/delete.png"
                                                    alt="<fmt:message key='label.delete'/>"
                                                    title="<fmt:message key='label.delete'/>"
                                                    width="16"
                                                    height="16" border="0"/></a>&nbsp;
                                        </td>
                                    </tr>
                                        <%
                                        }
                                    %>
                            </form>
                            <%
                                if (googleAnalyticsProfile == null || !googleAnalyticsProfile.isEnabled()) {
                            %>
                            <table>
                                <tbody>

                                <div class="head headtop">
                                    <div class="object-title"><fmt:message
                                            key="org.jahia.admin.site.ManageAnalytics.jahiaAnalyticsProfile.label"/>
                                    </div>
                                </div>
                                <tr>
                                    <td width="88%">
                                        <fmt:message key="label.jahiaGAprofile"/>
                                        <table class="evenOddTable" border="0" cellpadding="5" cellspacing="0"
                                               width="100%">
                                            <tr class="oddLine">
                                                <td><fmt:message
                                                        key="org.jahia.admin.site.ManageAnalytics.gaUserAcc.label"/>
                                                </td>
                                                <td>
                                                    <fmt:message
                                                            key="org.jahia.admin.site.ManageAnalytics.description.gaUserAcc.label"/>
                                                </td>
                                            </tr>
                                            <tr class="evenLine">
                                                <td><fmt:message
                                                        key="org.jahia.admin.site.ManageAnalytics.gaProfileName.label"/>
                                                </td>
                                                <td><fmt:message
                                                        key="org.jahia.admin.site.ManageAnalytics.description.gaProfileName.label"/>
                                                </td>
                                            </tr>
                                            <tr class="oddLine">
                                                <td><fmt:message
                                                        key="org.jahia.admin.site.ManageAnalytics.gaCredentials.label"/>
                                                </td>
                                                <td>
                                                    <fmt:message
                                                            key="org.jahia.admin.site.ManageAnalytics.description.gaCredentials.label"/>
                                                </td>
                                            </tr>
                                            <tr class="oddLine">
                                                <td><fmt:message
                                                        key="org.jahia.admin.site.ManageAnalytics.trackedUrls.label"/>
                                                </td>
                                                <td>
                                                    <fmt:message
                                                            key="org.jahia.admin.site.ManageAnalytics.description.trackedUrls.label"/>
                                                </td>
                                            </tr>
                                        </table>

                                    <td>
                                        <span class="dex-PushButton">
                                            <span class="first-child">
                                              <a class="ico-add"
                                                 href="<%=JahiaAdministration.composeActionURL(request,response,"analytics","&sub=new")%>"><fmt:message
                                                      key="label.add"/></a>
                                            </span>
                                        </span>
                                    </td>
                                </tr>
                            </table>
                            <%
                                }
                            %>
                        </div>
                    </div>
                </div>
            </td>
        </tr>
    </table>

    <div id="actionBar">
                            <span class="dex-PushButton">
                              <span class="first-child">
                                <a class="ico-back"
                                   href='<%=JahiaAdministration.composeActionURL(request,response,"displaymenu","")%>'><fmt:message
                                        key="label.backToMenu"/></a>
                              </span>
                            </span>

    </div>
</div>
</div>
<%@include file="/admin/include/footer.inc" %>
