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
    in Jahia's FLOSS exception. You should have received a copy of the text
    describing the FLOSS exception, and it is also available here:
    http://www.jahia.com/license
    
    Commercial and Supported Versions of the program
    Alternatively, commercial and supported versions of the program may be used
    in accordance with the terms contained in a separate written agreement
    between you and Jahia Limited. If you are unsure which license is appropriate
    for your use, please contact the sales department at sales@jahia.com.

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

    <h2 class="edit"><utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.analytics.ManageAnalytics.label"/>
        : <% if (currentSite != null) { %><utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.site.label"/>&nbsp;<%=currentSite.getServerName() %><%} %></h2>
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
                                    <utility:resourceBundle resourceBundle="JahiaInternalResources"
                                            resourceName="org.jahia.admin.analytics.ManageAnalytics.label"/>
                                </div>
                            </div>
                            <!--div class="head headtop">
                                <div class="object-title">Google analytics tracking settings</div>
                            </div-->

                             <div id="operationMenu">
                    <span class="dex-PushButton">
                  <span class="first-child">
                    <a class="ico-delete" href='<%=JahiaAdministration.composeActionURL(request,response,"analytics","&sub=delete&profile=all")%>'><utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.delete.label"/></a>
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
                                             <utility:resourceBundle resourceBundle="JahiaInternalResources"
                                                    resourceName="org.jahia.admin.site.ManageAnalytics.jahiaGAprofile.label"/>
                                        </th>
                                        <th width="35%">
                                            <utility:resourceBundle resourceBundle="JahiaInternalResources"
                                                    resourceName="org.jahia.admin.site.ManageAnalytics.gaProfileName.label"/>
                                        </th>
                                        <th width="12%" style="white-space: nowrap">
                                            <utility:resourceBundle resourceBundle="JahiaInternalResources"
                                                    resourceName="org.jahia.admin.site.ManageAnalytics.gaUserAcc.label"/>
                                        </th>
                                        <th width="12%" style="white-space: nowrap">
                                             <utility:resourceBundle resourceBundle="JahiaInternalResources"
                                                    resourceName="org.jahia.admin.site.ManageAnalytics.realUrlsTracked.label"/>
                                        </th>
                                        <th width="12%" style="white-space: nowrap">
                                            <utility:resourceBundle resourceBundle="JahiaInternalResources"
                                                    resourceName="org.jahia.admin.site.ManageAnalytics.virtualUrlsTracked.label"/>
                                        </th>
                                        <th width="12%" style="white-space: nowrap">
                                            <utility:resourceBundle resourceBundle="JahiaInternalResources"
                                                    resourceName="org.jahia.admin.site.ManageAnalytics.trackingEnabled.label"/>
                                        </th>
                                        <th width="15%" class="lastCol">
                                            <utility:resourceBundle resourceBundle="JahiaInternalResources"
                                                    resourceName="org.jahia.admin.site.ManageSites.actions.label"/>
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
                                               title="<utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName='org.jahia.admin.edit.label'/>"><img
                                                    src="<%=URL%>images/icons/admin/adromeda/edit.png"
                                                    alt="<utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName='org.jahia.admin.edit.label'/>"
                                                    title="<utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName='org.jahia.admin.edit.label'/>"
                                                    width="16"
                                                    height="16" border="0"/></a>&nbsp;
                                            <a href='<%=JahiaAdministration.composeActionURL(request,response,"analytics","&sub=delete&profile="+jahiaGAprofile )%>'
                                               title="<utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName='org.jahia.admin.delete.label'/>"><img
                                                    src="<%=URL%>images/icons/admin/adromeda/delete.png"
                                                    alt="<utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName='org.jahia.admin.delete.label'/>"
                                                    title="<utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName='org.jahia.admin.delete.label'/>"
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
                                    <div class="object-title"><utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.site.ManageAnalytics.jahiaAnalyticsProfile.label"/></div>
                                </div>
                                <tr>
                                    <td width="88%">
                                        <utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.ManageAnalytics.description.jahiaGAprofile.label"/>
                                        <table class="evenOddTable" border="0" cellpadding="5" cellspacing="0"
                                               width="100%">
                                            <tr class="evenLine">
                                                <td><utility:resourceBundle resourceBundle="JahiaInternalResources"
                                                    resourceName="org.jahia.admin.site.ManageAnalytics.jahiaGAprofile.label"/></td>
                                                <td>
                                                    <utility:resourceBundle resourceBundle="JahiaInternalResources"
                                                    resourceName="org.jahia.admin.ManageAnalytics.description.jahiaGAprofileName.label"/>
                                                    </td>
                                            </tr>
                                            <tr class="oddLine">
                                                <td><utility:resourceBundle resourceBundle="JahiaInternalResources"
                                                    resourceName="org.jahia.admin.site.ManageAnalytics.gaUserAcc.label"/></td>
                                                <td>
                                                    <utility:resourceBundle resourceBundle="JahiaInternalResources"
                                                    resourceName="org.jahia.admin.site.ManageAnalytics.description.gaUserAcc.label"/>
                                                </td>
                                            </tr>
                                            <tr class="evenLine">
                                                <td><utility:resourceBundle resourceBundle="JahiaInternalResources"
                                                    resourceName="org.jahia.admin.site.ManageAnalytics.gaProfileName.label"/></td>
                                                <td><utility:resourceBundle resourceBundle="JahiaInternalResources"
                                                    resourceName="org.jahia.admin.site.ManageAnalytics.description.gaProfileName.label"/>
                                                </td>
                                            </tr>
                                            <tr class="oddLine">
                                                <td><utility:resourceBundle resourceBundle="JahiaInternalResources"
                                                    resourceName="org.jahia.admin.site.ManageAnalytics.gaCredentials.label"/>
                                                    </td>
                                                <td>
                                                    <utility:resourceBundle resourceBundle="JahiaInternalResources"
                                                    resourceName="org.jahia.admin.site.ManageAnalytics.description.gaCredentials.label"/>
                                                </td>
                                            </tr>
                                            <tr class="oddLine">
                                                <td><utility:resourceBundle resourceBundle="JahiaInternalResources"
                                                    resourceName="org.jahia.admin.site.ManageAnalytics.trackedUrls.label"/></td>
                                                <td>
                                                    <utility:resourceBundle resourceBundle="JahiaInternalResources"
                                                    resourceName="org.jahia.admin.site.ManageAnalytics.description.trackedUrls.label"/>
                                                </td>
                                            </tr>
                                        </table>

                                    <td>
                                        <span class="dex-PushButton">
                                            <span class="first-child">
                                              <a class="ico-add"
                                                 href="<%=JahiaAdministration.composeActionURL(request,response,"analytics","&sub=new")%>"><utility:resourceBundle resourceBundle="JahiaInternalResources"
                                                      resourceName='org.jahia.admin.add.label'/></a>
                                            </span>
                                        </span>
                                    </td>
                                </tr>
                            </table>


                          <div id="actionBar">
                            <span class="dex-PushButton">
                              <span class="first-child">
                                <a class="ico-back"
                                   href='<%=JahiaAdministration.composeActionURL(request,response,"displaymenu","")%>'><utility:resourceBundle resourceBundle="JahiaInternalResources"
                                        resourceName="org.jahia.admin.backToMenu.label"/></a>
                              </span>
                            </span>
                           <span class="dex-PushButton">
                            <span class="first-child">
                              <a class="ico-ok" href="javascript:sendForm();"><utility:resourceBundle resourceBundle="JahiaInternalResources"
                                      resourceName="org.jahia.admin.save.label"/></a>
                            </span>
                          </span>

                         </div>
                       </div>
                   </div>
<%@include file="/admin/include/footer.inc" %>
