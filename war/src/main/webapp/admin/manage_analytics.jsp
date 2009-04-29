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
%>
<script type="text/javascript">

    function sendForm() {
        document.mainForm.submit();
    }
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
                            <!--div class="head headtop">
                                <div class="object-title">Google analytics tracking settings</div>
                            </div-->

                             <div id="operationMenu">
                    <span class="dex-PushButton">
                  <span class="first-child">
                    <a class="ico-delete" href='<%=JahiaAdministration.composeActionURL(request,response,"analytics","&sub=delete&profile=all")%>'><fmt:message key="org.jahia.admin.delete.label"/></a>
                  </span>
                </span>

    </div>
                            <div class="head headtop">
                                <div class="object-title">Existing profiles</div>
                            </div>
                            <form name="mainForm"
                                  action='<%=JahiaAdministration.composeActionURL(request,response,"analytics","&sub=commit")%>'
                                  method="post">
                                <table class="evenOddTable" border="0" cellpadding="5" cellspacing="0" width="100%">
                                    <thead>
                                    <tr>
                                        <th width="5%">
                                            &nbsp;
                                        </th>
                                        <th width="35%">
                                             <fmt:message key="org.jahia.admin.site.ManageAnalytics.jahiaGAprofile.label"/>
                                        </th>
                                        <th width="35%">
                                            <fmt:message key="org.jahia.admin.site.ManageAnalytics.gaProfileName.label"/>
                                        </th>
                                        <th width="12%" style="white-space: nowrap">
                                            <fmt:message key="org.jahia.admin.site.ManageAnalytics.gaUserAcc.label"/>
                                        </th>
                                        <th width="12%" style="white-space: nowrap">
                                             <fmt:message key="org.jahia.admin.site.ManageAnalytics.realUrlsTracked.label"/>
                                        </th>
                                        <th width="12%" style="white-space: nowrap">
                                            <fmt:message key="org.jahia.admin.site.ManageAnalytics.virtualUrlsTracked.label"/>
                                        </th>
                                        <th width="12%" style="white-space: nowrap">
                                            <fmt:message key="org.jahia.admin.site.ManageAnalytics.trackingEnabled.label"/>
                                        </th>
                                        <th width="15%" class="lastCol">
                                            <fmt:message key="org.jahia.admin.site.ManageSites.actions.label"/>
                                        </th>
                                    </tr>
                                    </thead>
                                        <%
                                    int profileCnt = 0;
                                    if(settings.getProperty("profileCnt_"+currentSite.getSiteKey()) != null){
                                    profileCnt = Integer.parseInt(settings.getProperty("profileCnt_"+currentSite.getSiteKey()));
                                    }
                                    Set profiles  = settings.keySet();
                                    Iterator it = profiles.iterator();
                                    String myClass = "evenLine";
                                    int cnt = 0;
                                    while(it.hasNext())
                                    {
                                        String prof = (String)it.next();

                                        if(prof.startsWith("jahiaGAprofile"))
                                        {
                                        if(cnt%2 == 0){myClass = "evenLine" ;}else{myClass = "oddLine" ;}
                                                String jahiaGAprofile = settings.getProperty(prof);
                                            %>

                                    <tr class="<%=myClass%>" id="<%=jahiaGAprofile%>">
                                        <td><input type="checkbox"/></td>
                                        <td><%=jahiaGAprofile%>
                                        </td>
                                        <td><%=settings.getProperty(jahiaGAprofile + "_" + currentSite.getSiteKey() + "_gaProfile")%>
                                        </td>
                                        <td><%=settings.getProperty(jahiaGAprofile + "_" + currentSite.getSiteKey() + "_gaUserAccount")%>
                                        </td>
                                        <td><input type="radio" value="real" name="<%=jahiaGAprofile%>TrackedUrls"
                                                   <% if ((settings.getProperty(jahiaGAprofile+"_"+currentSite.getSiteKey()+"_trackedUrls").equals("real"))) { %>checked<% } %>
                                                   id="<%=jahiaGAprofile%>TrackedUrls"/></td>
                                        <td><input type="radio" value="virtual" name="<%=jahiaGAprofile%>TrackedUrls"
                                                   <% if ((settings.getProperty(jahiaGAprofile+"_"+currentSite.getSiteKey()+"_trackedUrls").equals("virtual"))) { %>checked<% } %>
                                                   id="<%=jahiaGAprofile%>TrackedUrls"/></td>
                                        <td><input type="checkbox" name="<%=jahiaGAprofile%>TrackingEnabled"
                                                   <% if (Boolean.valueOf(settings.getProperty(jahiaGAprofile+"_"+currentSite.getSiteKey()+"_trackingEnabled"))) { %>checked<% } %>
                                                   id="<%=jahiaGAprofile%>TrackingEnabled"/></td>
                                        <td class="lastCol">
                                            <a href='<%=JahiaAdministration.composeActionURL(request,response,"analytics","&sub=displayEdit&profile="+jahiaGAprofile )%>'
                                               title="<fmt:message key='org.jahia.admin.edit.label'/>"><img
                                                    src="<%=URL%>images/icons/admin/adromeda/edit.png"
                                                    alt="<fmt:message key='org.jahia.admin.edit.label'/>"
                                                    title="<fmt:message key='org.jahia.admin.edit.label'/>"
                                                    width="16"
                                                    height="16" border="0"/></a>&nbsp;
                                            <a href='<%=JahiaAdministration.composeActionURL(request,response,"analytics","&sub=delete&profile="+jahiaGAprofile )%>'
                                               title="<fmt:message key='org.jahia.admin.delete.label'/>"><img
                                                    src="<%=URL%>images/icons/admin/adromeda/delete.png"
                                                    alt="<fmt:message key='org.jahia.admin.delete.label'/>"
                                                    title="<fmt:message key='org.jahia.admin.delete.label'/>"
                                                    width="16"
                                                    height="16" border="0"/></a>&nbsp;
                                        </td>
                                    </tr>
                                        <%
                                        cnt++;
                                    }
                                }
                                %>
                            </form>
                            <table>
                                <tbody>

                                <div class="head headtop">
                                    <div class="object-title"><fmt:message key="org.jahia.admin.site.ManageAnalytics.jahiaAnalyticsProfile.label"/></div>
                                </div>
                                <tr>
                                    <td width="88%">
                                        <fmt:message key="org.jahia.admin.ManageAnalytics.description.jahiaGAprofile.label"/>
                                        <table class="evenOddTable" border="0" cellpadding="5" cellspacing="0"
                                               width="100%">
                                            <tr class="evenLine">
                                                <td><fmt:message key="org.jahia.admin.site.ManageAnalytics.jahiaGAprofile.label"/></td>
                                                <td>
                                                    <fmt:message key="org.jahia.admin.ManageAnalytics.description.jahiaGAprofileName.label"/>
                                                    </td>
                                            </tr>
                                            <tr class="oddLine">
                                                <td><fmt:message key="org.jahia.admin.site.ManageAnalytics.gaUserAcc.label"/></td>
                                                <td>
                                                    <fmt:message key="org.jahia.admin.site.ManageAnalytics.description.gaUserAcc.label"/>
                                                </td>
                                            </tr>
                                            <tr class="evenLine">
                                                <td><fmt:message key="org.jahia.admin.site.ManageAnalytics.gaProfileName.label"/></td>
                                                <td><fmt:message key="org.jahia.admin.site.ManageAnalytics.description.gaProfileName.label"/>
                                                </td>
                                            </tr>
                                            <tr class="oddLine">
                                                <td><fmt:message key="org.jahia.admin.site.ManageAnalytics.gaCredentials.label"/>
                                                    </td>
                                                <td>
                                                    <fmt:message key="org.jahia.admin.site.ManageAnalytics.description.gaCredentials.label"/>
                                                </td>
                                            </tr>
                                            <tr class="oddLine">
                                                <td><fmt:message key="org.jahia.admin.site.ManageAnalytics.trackedUrls.label"/></td>
                                                <td>
                                                    <fmt:message key="org.jahia.admin.site.ManageAnalytics.description.trackedUrls.label"/>
                                                </td>
                                            </tr>
                                        </table>

                                    <td>
                                        <span class="dex-PushButton">
                                            <span class="first-child">
                                              <a class="ico-add"
                                                 href="<%=JahiaAdministration.composeActionURL(request,response,"analytics","&sub=new")%>"><fmt:message key="org.jahia.admin.add.label"/></a>
                                            </span>
                                        </span>
                                    </td>
                                </tr>
                            </table>


                          <div id="actionBar">
                            <span class="dex-PushButton">
                              <span class="first-child">
                                <a class="ico-back"
                                   href='<%=JahiaAdministration.composeActionURL(request,response,"displaymenu","")%>'><fmt:message key="org.jahia.admin.backToMenu.label"/></a>
                              </span>
                            </span>
                           <span class="dex-PushButton">
                            <span class="first-child">
                              <a class="ico-ok" href="javascript:sendForm();"><fmt:message key="org.jahia.admin.save.label"/></a>
                            </span>
                          </span>

                         </div>
                       </div>
                   </div>
<%@include file="/admin/include/footer.inc" %>
