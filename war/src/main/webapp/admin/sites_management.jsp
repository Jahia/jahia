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
<%@include file="/admin/include/header.inc" %>
<%@page import="org.jahia.bin.JahiaAdministration" %>
<%@ page import="org.jahia.params.ProcessingContext" %>
<%@ page import="org.jahia.services.pages.ContentPage" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="java.text.DateFormat" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.util.Date" %>
<%@page import="org.jahia.registries.ServicesRegistry" %>
<%@page import="org.jahia.services.acl.JahiaBaseACL" %>
<%@ page import="org.jahia.security.license.LicenseActionChecker" %>
<%@ page import="org.jahia.admin.sites.ManageSites" %>
<%@ page import="java.io.File" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%
    Iterator sitesList = (Iterator) request.getAttribute("sitesList");
    String warningMsg = (String) request.getAttribute("warningMsg");
    String sub = (String) request.getParameter("sub");
    JahiaSite newJahiaSite = (JahiaSite) session.getAttribute(JahiaAdministration.CLASS_NAME + "newJahiaSite");
    ProcessingContext jParams = null;
    if (jData != null) {
        jParams = jData.params();
    }
    stretcherToOpen = 0; %>
<% if (sitesList != null && sitesList.hasNext()) { %>
<script type="text/javascript">
    function selectSite(selectedSite) {
        if (selectedSite) {
            if (document.main.sitebox.length) {
                for (var i = 0; i < document.main.sitebox.length; i++) {
                    document.main.sitebox[i].checked = (document.main.sitebox[i].value == selectedSite);
                }
            }
            else {
                document.main.sitebox.checked = true;
            }
        }
    }
    function copyTemplateToVarFolder(selectedTemplate) {
    }

    function sendExportForm(selectedSite) {
        selectSite(selectedSite);
        document.main.action = '<%=jParams.composeSiteUrl() + "/engineName/export/"%>' + (selectedSite ? selectedSite + '_' : '') + '<%= "export_" + new SimpleDateFormat("yyyy-MM-dd-HH-mm").format(new Date()) + ".zip"%>';
        document.main.submit();
    }


    function sendDeleteForm(selectedSite) {
        selectSite(selectedSite);
        document.main.action = '<%=request.getContextPath()+JahiaAdministration.getServletPath()%>';
        document.main.submit();
    }

    function sendForm(){
        document.jahiaAdmin.submit();
    }

</script>
<div id="topTitle">
    <h1>Jahia</h1>

    <h2 class="edit"><fmt:message key="org.jahia.admin.site.ManageSites.virtualSitesListe.label"/></h2>
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
<div class="head headtop">
    <div class="object-title"><fmt:message key="label.virtualSitesManagement"/>
    </div>
</div>
<div class="content-body">
    <div id="operationMenu">
                    <span class="dex-PushButton">
                      <span class="first-child">
                        <a class="ico-siteAdd"
                           href='<%=JahiaAdministration.composeActionURL(request,response,"sites","&sub=add")%>'><fmt:message key="org.jahia.admin.site.ManageSites.addSite.label"/></a>
                      </span>
                    </span>
                    <span class="dex-PushButton">
                      <span class="first-child">
                        <a class="ico-export"
                           href='<%=jParams.composeSiteUrl() + "/engineName/export/jahia.zip?exportformat=all" %>'
                           alt="<fmt:message key="org.jahia.admin.site.ManageSites.exportall.label"/>"><fmt:message key="org.jahia.admin.site.ManageSites.exportall.label"/></a>
                      </span>
                    </span>
                    <span class="dex-PushButton">
                      <span class="first-child">
                        <a class="ico-export" href="javascript:sendExportForm()"><fmt:message key="org.jahia.admin.site.ManageSites.exportsites.label"/></a>
                      </span>
                    </span>
                    <span class="dex-PushButton">
                      <span class="first-child">
                        <a class="ico-siteDelete" class="operationLink"
                           href="javascript:sendDeleteForm()"><fmt:message key="org.jahia.admin.site.ManageSites.deletesites.label"/></a>
                      </span>
                    </span>
    </div>
</div>
<div class="head headtop">
    <div class="object-title"><fmt:message key="org.jahia.admin.site.ManageSites.virtualSitesListe.label"/>
    </div>
</div>
<div  class="content-item-noborder">
    <% if (warningMsg != "" && !sub.equals("prepareimport")) { %>
    <p class="errorbold">
        <%=warningMsg %>
    </p>
    <% } %>
    <table class="evenOddTable" border="0" cellpadding="5" cellspacing="0" width="100%">
        <thead>
        <tr>
            <th width="5%">
                &nbsp;
            </th>
            <th width="35%">
                <fmt:message key="label.name"/>
            </th>
            <th width="12%" style="white-space: nowrap">
                <fmt:message key="org.jahia.admin.site.ManageSites.siteKey.label"/>
            </th>
            <th width="3%">
                &nbsp;
            </th>
            <th width="30%">
                <fmt:message key="org.jahia.admin.site.ManageSites.templateSet.label"/>
            </th>
            <th width="15%" class="lastCol">
                <fmt:message key="label.action"/>
            </th>
        </tr>
        </thead>
        <form name="main">
            <input type="hidden" name="do" value="sites"/><input type="hidden" name="sub" value="multipledelete"/><input
                type="hidden" name="exportformat" value="site"/>
            <tbody>
            <%
                JahiaSite site = null;
                int lineCounter = 0;
                while (sitesList.hasNext()) {
                    site = (JahiaSite) sitesList.next();
                    ContentPage homeContentPage = site.getHomeContentPage();
                    String lineClass = "oddLine";
                    if (lineCounter % 2 == 0) {
                        lineClass = "evenLine";
                    }
                    lineCounter++; %>
            <tr class="<%=lineClass%>">
                <td>
                    <input type="checkbox" name="sitebox" value="<%=site.getSiteKey()%>">
                </td>
                <td>
                    <a href='<%=JahiaAdministration.composeActionURL(request,response,"sites","&sub=edit&siteid=" + site.getID())%>'
                       title="<fmt:message key='label.edit'/>"><%=site.getTitle() %>
                    </a>
                </td>
                <td>
                    <%=site.getSiteKey() %>
                </td>
                <td align="center">
                    <%
                        if (site.isDefault()) {
                    %>
                    <img
                            src="<%=URL%>images/icons/workflow/accept.gif"
                            alt="+"
                            title="<fmt:message key='org.jahia.admin.site.ManageSites.isTheDefaultSite.label'/>" width="10"
                            height="10" border="0"/>
                    <%
                    } else {
                    %>&nbsp;<% } %>

                </td>
                <td>
                    <%=site.getTemplatePackageName() %>
                </td>
                <td class="lastCol">
                    <a href='<%=JahiaAdministration.composeActionURL(request,response,"sites","&sub=edit&siteid=" + site.getID())%>'
                       title="<fmt:message key='label.edit'/>"><img
                            src="<%=URL%>images/icons/admin/adromeda/edit.png"
                            alt="<fmt:message key='label.edit'/>"
                            title="<fmt:message key='label.edit'/>" width="16"
                            height="16" border="0"/></a>&nbsp;<a href="#delete"
                                                                 onclick="sendDeleteForm('<%=site.getSiteKey()%>'); return false;"
                                                                 title="<fmt:message key='label.delete'/>"><img
                        src="<%=URL%>images/icons/admin/adromeda/delete.png"
                        alt="<fmt:message key='label.delete'/>"
                        title="<fmt:message key='label.delete'/>" width="16"
                        height="16" border="0"/></a>&nbsp;<% if (homeContentPage != null) { %>
                    &nbsp;<a href="#export" onclick="sendExportForm('<%=site.getSiteKey()%>'); return false;"
                             title="<fmt:message key='org.jahia.admin.site.ManageSites.export.label'/>"><img
                        src="<%=URL%>images/icons/admin/adromeda/export1.png"
                        alt="<fmt:message key='org.jahia.admin.site.ManageSites.export.label'/>"
                        title="<fmt:message key='org.jahia.admin.site.ManageSites.export.label'/>"
                        width="16" height="16" border="0"/></a><% } %>
                </td>
            </tr>
            <%
                } %>
            </tbody>
        </form>

    </table>

    <!-- prepackaged site -->
    <div class="head headtop">
        <div class="object-title">
            <fmt:message key="org.jahia.admin.site.ManageSites.importprepackaged.label"/>
        </div>
    </div>
    <div  class="content-item">

        <form name="siteImportPrepackaged"
              action='<%=JahiaAdministration.composeActionURL(request,response,"sites","&sub=prepareimport")%>'
              method="post"
              enctype="multipart/form-data">
            <table border="0" cellpadding="5" cellspacing="0" class="topAlignedTable">
                <tr>
                    <td>
                        <fmt:message key="org.jahia.admin.site.ManageSites.importprepackaged.fileselect"/>&nbsp;
                    </td>
                    <td>
                        &nbsp;<select name="importpath">
                        <%
                            File[] files = new File(org.jahia.settings.SettingsBean.getInstance().getJahiaVarDiskPath() + "/prepackagedSites").listFiles();
                            pageContext.setAttribute("files", files);
                        %>
                        <c:forEach var="file" items="${files}">
                            <option value='${file.path}'>${file.name}</option>
                        </c:forEach>
                    </select>
                    </td>
                    <td>
                                            
                	                    <span class="dex-PushButton">
                                          <span class="first-child">
                                            <a class="ico-import"
                                               href="javascript:{showWorkInProgress();document.siteImportPrepackaged.submit();}"><fmt:message key="org.jahia.admin.site.ManageSites.importprepackaged.proceed"/></a>
                                          </span>
                                        </span>

                    </td>
                </tr>
            </table>
        </form>
    </div>


    <!--   import backup -->
    <div class="head headtop">
        <div class="object-title">
            <fmt:message key="org.jahia.admin.site.ManageSites.multipleimport.label"/>
        </div>
    </div>
    <div  class="content-item">
        <% if (warningMsg != "" && sub.equals("prepareimport")) { %>
        <p class="errorbold">
            <%=warningMsg %>
        </p>
        <% } %>
        <form name="siteImport"
              action='<%=JahiaAdministration.composeActionURL(request,response,"sites","&sub=prepareimport")%>'
              method="post"
              enctype="multipart/form-data">
            <table border="0" cellpadding="5" cellspacing="0" class="topAlignedTable">
                <tr>
                    <td>



                        <fmt:message key="org.jahia.admin.site.ManageSites.multipleimport.fileselect"/>&nbsp;
                    </td>
                    <td>
                        :&nbsp;<input type="file" name="import" onclick="setCheckedValue(document.forms['siteImport'].elements['siteImport'], 'siteImport'); setCheckedValue(document.forms['siteImportPrepackaged'].elements['siteImportPrepackaged'], '');setCheckedValue(document.forms['blank'].elements['blank'], '');">
                    </td>

                    <td>
                        <% if ((ServicesRegistry.getInstance().getJahiaACLManagerService().getSiteActionPermission("engines.importexport.ManageImport", jParams.getUser(), JahiaBaseACL.READ_RIGHTS, 0) > 0)
                                && LicenseActionChecker.isAuthorizedByLicense("org.jahia.actions.sites.*.engines.importexport.ManageImport", 0)) { %>
                                            <span class="dex-PushButton">
                                              <span class="first-child">
                                                <a class="ico-import"
                                                   href="javascript:{showWorkInProgress(); document.siteImport.submit();}"><fmt:message key="org.jahia.admin.site.ManageSites.fileImport.label"/></a>
                                              </span>
                                            </span><%
                        } %>
                    </td>

                </tr>
            </table>
        </form>
    </div>

</div>
<% }
else { %>
<%if (!isConfigWizard) { %>
<div id="topTitle">
    <h1>Jahia</h1>

    <h2 class="edit"><fmt:message key="org.jahia.admin.site.ManageSites.virtualSitesListe.label"/></h2>
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
<div class="head headtop">
    <div class="object-title"><fmt:message key="label.virtualSitesManagement"/>
    </div>
</div>

<!-- adding blank site -->
<div class="content-body">
    <div id="operationMenu">
        <table border="0" cellpadding="5" cellspacing="0" class="topAlignedTable">
            <tr>
                <td>
                    <fmt:message key="org.jahia.admin.site.ManageSites.addSite.label"/>&nbsp;
                </td>

                <td>
                    <span class="dex-PushButton">
                                          <span class="first-child">

                                            <a class="ico-siteAdd"
                                               href='<%=JahiaAdministration.composeActionURL(request,response,"sites","&sub=add")%>'><fmt:message key="org.jahia.admin.site.ManageSites.addSite.label"/></a>
                                          </span>
                                        </span>
                </td>
            </tr>
        </table>
    </div>

    <!-- prepackaged site -->
    <div class="head headtop">
        <div class="object-title">
            <fmt:message key="org.jahia.admin.site.ManageSites.importprepackaged.label"/>

            <fmt:message key="label.virtualSitesManagement.default"/>

        </div>
    </div>
    <div  class="content-item">

        <form name="siteImportPrepackaged"
              action='<%=JahiaAdministration.composeActionURL(request,response,"sites","&sub=prepareimport")%>'
              method="post"
              enctype="multipart/form-data">
            <table border="0" cellpadding="5" cellspacing="0" class="topAlignedTable">
                <tr>
                    <td>
                        <fmt:message key="org.jahia.admin.site.ManageSites.importprepackaged.fileselect"/>&nbsp;
                    </td>
                    <td>
                        &nbsp;<select name="importpath">
                        <%
                            File[] files = new File(org.jahia.settings.SettingsBean.getInstance().getJahiaVarDiskPath() + "/prepackagedSites").listFiles();
                            pageContext.setAttribute("files", files);
                        %>
                        <c:forEach var="file" items="${files}">
                            <option value='${file.path}'>${file.name}</option>
                        </c:forEach>
                    </select>
                    </td>
                    <td>
                                            
                	                    <span class="dex-PushButton">
                                          <span class="first-child">
                                            <a class="ico-import"
                                               href="javascript:{showWorkInProgress();document.siteImportPrepackaged.submit();}"><fmt:message key="org.jahia.admin.site.ManageSites.importprepackaged.proceed"/></a>
                                          </span>
                                        </span>

                    </td>
                </tr>
            </table>
        </form>
    </div>

    <!--   import backup -->
    <div class="head headtop">
        <div class="object-title">
            <fmt:message key="org.jahia.admin.site.ManageSites.multipleimport.label"/>
        </div>
    </div>
    <div  class="content-item">
        <% if (warningMsg != "" && sub.equals("prepareimport")) { %>
        <p class="errorbold">
            <%=warningMsg %>
        </p>
        <% } %>
        <form name="siteImport"
              action='<%=JahiaAdministration.composeActionURL(request,response,"sites","&sub=prepareimport")%>'
              method="post"
              enctype="multipart/form-data">
            <table border="0" cellpadding="5" cellspacing="0" class="topAlignedTable">
                <tr>
                    <td>



                        <fmt:message key="org.jahia.admin.site.ManageSites.multipleimport.fileselect"/>&nbsp;
                    </td>
                    <td>
                        :&nbsp;<input type="file" name="import" onclick="setCheckedValue(document.forms['siteImport'].elements['siteImport'], 'siteImport'); setCheckedValue(document.forms['siteImportPrepackaged'].elements['siteImportPrepackaged'], '');setCheckedValue(document.forms['blank'].elements['blank'], '');">
                    </td>

                    <td>
                        <% if ((ServicesRegistry.getInstance().getJahiaACLManagerService().getSiteActionPermission("engines.importexport.ManageImport", jParams.getUser(), JahiaBaseACL.READ_RIGHTS, 0) > 0)
                                && LicenseActionChecker.isAuthorizedByLicense("org.jahia.actions.sites.*.engines.importexport.ManageImport", 0)) { %>
                                            <span class="dex-PushButton">
                                              <span class="first-child">
                                                <a class="ico-import"
                                                   href="javascript:{showWorkInProgress(); document.siteImport.submit();}"><fmt:message key="org.jahia.admin.site.ManageSites.fileImport.label"/></a>
                                              </span>
                                            </span><%
                        } %>
                    </td>

                </tr>
            </table>
        </form>
    </div>


</div>

    <% } %>


    <%if (isConfigWizard) { %>
<div class="dex-TabPanelBottom-full">
    <div id="content" class="full">

        <div class="head headtop">
            <div class="object-title">
                1.&nbsp;<fmt:message key="org.jahia.admin.site.ManageSites.importprepackaged.label"/>&nbsp;<fmt:message key="label.virtualSitesManagement.default"/>
            </div>
        </div>
        <div  class="content-item">

            <form name="siteImportPrepackaged"
                  action='<%=JahiaAdministration.composeActionURL(request,response,"sites","&sub=prepareimport")%>'
                  method="post"
                  enctype="multipart/form-data">
                <table border="0" cellpadding="5" cellspacing="0" class="topAlignedTable">
                    <tr>
                        <td><input type="radio" id="siteImportPrepackaged" name="siteImportPrepackaged" checked="checked" value="siteImportPrepackaged" onclick="setCheckedValue(document.forms['siteImportPrepackaged'].elements['siteImportPrepackaged'], 'siteImportPrepackaged'); setCheckedValue(document.forms['siteImport'].elements['siteImport'], '');setCheckedValue(document.forms['blank'].elements['blank'], '');">
                            <label for="siteImportPrepackaged"><fmt:message key="org.jahia.admin.site.ManageSites.importprepackaged.fileselect"/></label>&nbsp;
                        </td>
                        <td>
                            &nbsp;<select name="importpath">
                            <%
                                File[] files = new File(org.jahia.settings.SettingsBean.getInstance().getJahiaVarDiskPath() + "/prepackagedSites").listFiles();
                                pageContext.setAttribute("files", files);
                            %>
                            <c:forEach var="file" items="${files}">
                                <option value='${file.path}'>${file.name}</option>
                            </c:forEach>
                        </select>
                        </td>
                        <td>
                        </td>
                    </tr>
                </table>
            </form>
        </div>

        <div class="head headtop">
            <div class="object-title">
                2.&nbsp;<fmt:message key="label.virtualSitesManagement.configwizard"/>
            </div>
        </div>

        <div class="content-item">
            <table border="0" cellpadding="5" cellspacing="0" class="topAlignedTable">
                <tr>
                    <td>
                        <form name="blank"  action='<%=JahiaAdministration.composeActionURL(request,response,"sites","&sub=add")%>' method="post" >
                            <input type="radio" id="blank" name="blank" value="blank" onclick="setCheckedValue(document.forms['blank'].elements['blank'], 'blank'); setCheckedValue(document.forms['siteImportPrepackaged'].elements['siteImportPrepackaged'], '');setCheckedValue(document.forms['siteImport'].elements['siteImport'], '');">
                            <label for="blank"><fmt:message key="org.jahia.admin.site.ManageSites.addSite.label"/></label>
                        </form>
                    </td>
                </tr>
            </table>
        </div>


        <!--   import backup -->
        <div class="head headtop">
            <div class="object-title">
                3.&nbsp;<fmt:message key="org.jahia.admin.site.ManageSites.multipleimport.label"/>
            </div>
        </div>
        <div  class="content-item">
            <% if (warningMsg != "" && sub.equals("prepareimport")) { %>
            <p class="errorbold">
                <%=warningMsg %>
            </p>
            <% } %>
            <form name="siteImport"
                  action='<%=JahiaAdministration.composeActionURL(request,response,"sites","&sub=prepareimport")%>'
                  method="post"
                  enctype="multipart/form-data">
                <table border="0" cellpadding="5" cellspacing="0" class="topAlignedTable">
                    <tr>
                        <td>
                            <input type="radio" id="siteImport" name="siteImport" value="siteImport" onclick="setCheckedValue(document.forms['siteImport'].elements['siteImport'], 'siteImport'); setCheckedValue(document.forms['siteImportPrepackaged'].elements['siteImportPrepackaged'], '');setCheckedValue(document.forms['blank'].elements['blank'], '');">
                            <label for="siteImport"><fmt:message key="org.jahia.admin.site.ManageSites.multipleimport.fileselect"/></label>
                        </td>
                        <td>
                            :&nbsp;<input type="file" name="import" onclick="setCheckedValue(document.forms['siteImport'].elements['siteImport'], 'siteImport'); setCheckedValue(document.forms['siteImportPrepackaged'].elements['siteImportPrepackaged'], '');setCheckedValue(document.forms['blank'].elements['blank'], '');">
                        </td>

                    </tr>
                </table>
            </form>
        </div>


        <% } %>









        <% } %>
    </div>
</div>
</td>
</tr>
</tbody>
</table>
</div>
<br class="clear"/>
<div id="actionBar">
    <%
        if (session.getAttribute(JahiaAdministration.CLASS_NAME + "redirectToJahia") == null) { %>
                            <span class="dex-PushButton">
                              <span class="first-child">
                                <a class="ico-back"
                                   href='<%=JahiaAdministration.composeActionURL(request,response,"displaymenu","")%>'><fmt:message key="label.backToMenu"/></a>
                              </span>
                            </span>


    <%
        } %>
    <%if (isConfigWizard) { %>
					<span class="dex-PushButton">
            <span class="first-child">
              <a class="ico-next" href="javascript:submitform();"><internal:message key="label.nextStep"/>
              </a>
            </span>
          </span>

    <%
        } %>
</div>
</div>


<SCRIPT language="JavaScript">

    // return the value of the radio button that is checked
    // return an empty string if none are checked, or
    // there are no radio buttons
    function getCheckedValue(radioObj) {
        if(!radioObj)
            return "";
        var radioLength = radioObj.length;
        if(radioLength == undefined)
            if(radioObj.checked)
                return radioObj.value;
            else
                return "";
        for(var i = 0; i < radioLength; i++) {
            if(radioObj[i].checked) {
                return radioObj[i].value;
            }
        }
        return "";
    }

    // set the radio button with the given value as being checked
    // do nothing if there are no radio buttons
    // if the given value does not exist, all the radio buttons
    // are reset to unchecked
    function setCheckedValue(radioObj, newValue) {
        if(!radioObj)
            return;
        var radioLength = radioObj.length;
        if(radioLength == undefined) {
            //window.alert("setting new checked value for ");
            radioObj.checked = (radioObj.value == newValue.toString());
            return;
        }
        for(var i = 0; i < radioLength; i++) {
            radioObj[i].checked = false;
            if(radioObj[i].value == newValue.toString()) {
                //window.alert("setting new checked value for "+newValue.toString());
                radioObj[i].checked = true;
            }
        }
    }

    function submitform()
    {

        if(getCheckedValue(document.forms['blank'].elements['blank'])=="blank"){
            //window.alert("radio button checked   "+getCheckedValue(document.forms['blank'].elements['blank']));
            document.blank.submit();
        }
        if(getCheckedValue(document.forms['siteImportPrepackaged'].elements['siteImportPrepackaged'])=="siteImportPrepackaged"){
            //window.alert("radio button checked   "+getCheckedValue(document.forms['siteImportPrepackaged'].elements['siteImportPrepackaged']));
            document.siteImportPrepackaged.submit();
        }
        if(getCheckedValue(document.forms['siteImport'].elements['siteImport'])=="siteImport"){
            //window.alert("radio button checked   "+getCheckedValue(document.forms['siteImport'].elements['siteImport']));
            document.siteImport.submit();

        }
    }
</SCRIPT>
<%@include file="/admin/include/footer.inc" %>