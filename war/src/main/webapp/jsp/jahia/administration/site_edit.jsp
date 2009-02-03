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

<%@page import="org.jahia.params.ParamBean" %>
<%@page import="org.jahia.registries.ServicesRegistry" %>
<%@page import="org.jahia.services.acl.JahiaBaseACL" %>
<%@include file="/jsp/jahia/administration/include/header.inc" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%
    final ParamBean jParams = (ParamBean) request.getAttribute("org.jahia.params.ParamBean");
    String warningMsg = (String) request.getAttribute("warningMsg");
    String siteTitle = (String) request.getAttribute("siteTitle");
    String siteServerName = (String) request.getAttribute("siteServerName");
    String siteDescr = (String) request.getAttribute("siteDescr");
    String siteKey = (String) request.getAttribute("siteKey");
    Boolean defaultSite = (Boolean) request.getAttribute("defaultSite");


    String gaUserAccountCustom = (String) request.getAttribute("gaUserAccountCustom");
    String gaProfileCustom = (String) request.getAttribute("gaProfileCustom");
    String gaUserAccountDefault = (String) request.getAttribute("gaUserAccountDefault");
    String gaProfileDefault = (String) request.getAttribute("gaProfileDefault");
    String gmailAccount = (String) request.getAttribute("gmailAccount");
    String gmailPassword = (String) request.getAttribute("gmailPassword");
    Boolean trackingEnabled = (Boolean) request.getAttribute("trackingEnabled");


    stretcherToOpen = 0;
//    Boolean versioningEnabled   = (Boolean)request.getAttribute("versioningEnabled");
//    Boolean stagingEnabled   = (Boolean)request.getAttribute("stagingEnabled");
    if (warningMsg != null) {
        if (warningMsg.trim().length() > 0) {
            jahiaDisplayMessage = warningMsg;
        }
    } %>
<script type="text/javascript">
    <!--

    function sendForm(){
        document.jahiaAdmin.versioningEnabled.disabled = false;
        document.jahiaAdmin.stagingEnabled.disabled = false;
        document.jahiaAdmin.submit();
    }

    -->
</script>
<div id="topTitle">
    <h1>Jahia</h1>

    <h2 class="edit"><internal:adminResourceBundle
            resourceName="org.jahia.admin.site.ManageSites.manageVirtualSites.label"/></h2>
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
<%@include file="/jsp/jahia/administration/include/menu_server.inc" %>
<div id="content" class="fit">
<div class="head headtop">
    <div class="object-title"><internal:adminResourceBundle
            resourceName="org.jahia.admin.virtualSitesManagement.label"/>
    </div>
</div>
<div class="content-item">
<form name="jahiaAdmin" action='<%=JahiaAdministration.composeActionURL(request,response,"sites","&sub=processedit")%>'
      method="post">
<input type="hidden" name="siteid" value="${siteID}">
<table border="0" cellpadding="5" cellspacing="0" style="width:100%">
<tr>
    <td>
        <internal:adminResourceBundle resourceName="org.jahia.admin.site.ManageSites.siteKey.label"/>*&nbsp;
    </td>
    <td valign="middle" align="left" class="text">
        :&nbsp;<b><%=siteKey %>
    </b>
    </td>
</tr>
<tr>
    <td>
        <internal:adminResourceBundle resourceName="org.jahia.admin.site.ManageSites.siteTitle.label"/>*&nbsp;
        <br>
        <em><internal:adminResourceBundle resourceName="org.jahia.admin.site.ManageSites.egSiteTitle.label"/></em>
    </td>
    <td>
        :&nbsp;<input class="input" type="text" name="siteTitle" value="<%=siteTitle%>" size="<%=inputSize%>"
                      maxlength="100">
    </td>
</tr>
<tr>
    <td>
        <internal:adminResourceBundle resourceName="org.jahia.admin.site.ManageSites.siteServerName.label"/>*&nbsp;
        <br>
        <em><internal:adminResourceBundle resourceName="org.jahia.admin.site.ManageSites.egSiteServerName.label"/></em>
    </td>
    <td>
        :&nbsp;<input class="input" type="text" name="siteServerName" value="<%=siteServerName%>" size="<%=inputSize%>"
                      maxlength="200">
    </td>
</tr>
</tr>
<tr>
    <td>
        <internal:adminResourceBundle resourceName="org.jahia.admin.site.ManageSites.templateSet.label"/>
    </td>
    <td>
        :&nbsp;<%= request.getAttribute("siteTemplatePackageName") %>
    </td>
</tr>
<tr>
    <td>
        <label for="defaultSite">
            <internal:adminResourceBundle resourceName="org.jahia.admin.site.ManageSites.setAsDefaultSite.label"/>&nbsp;
        </label>
    </td>
    <td>
        <% if (!defaultSite.booleanValue()) { %>
        :&nbsp;<input class="input" type="checkbox" name="defaultSite" id="defaultSite"/><% } else { %>
        :&nbsp;<internal:adminResourceBundle
            resourceName="org.jahia.admin.site.ManageSites.isTheDefaultSite.label"/><input type="hidden"
                                                                                           name="defaultSite"
                                                                                           value="true"/><% } %>
    </td>
</tr>
<% if (ServicesRegistry.getInstance().getJahiaACLManagerService().getServerActionPermission("admin.pwdpolicy.ManagePasswordPolicies", jParams.getUser(), JahiaBaseACL.READ_RIGHTS, jParams.getSiteID()) > 0) { %>
<tr>
    <td>
        <label for="enforcePasswordPolicy">
            <internal:adminResourceBundle resourceName="org.jahia.admin.site.ManageSites.enforcePasswordPolicy.label"/>&nbsp;
        </label>
    </td>
    <td>
        :&nbsp;<input class="input" type="checkbox" name="enforcePasswordPolicy" id="enforcePasswordPolicy" value="true"
            <c:if test="${enforcePasswordPolicy}">
                checked="checked"
            </c:if>/>
    </td>
</tr>
<% } %>
<% /*
                    <!-- temporarily deactivated because this is not yet fully functional in the back-end -->
                    <!--
                    <tr>
                    <td>
                    <internal:adminResourceBundle resourceName="org.jahia.admin.site.ManageSites.enableVersioning.label"/>&nbsp;
                    </td>
                    <td>
                    :&nbsp;
                    <input disabled class="input" type="checkbox" value="yes" name="versioningEnabled" if ( versioningEnabled.booleanValue() ) { out.print("checked=\"true\"") } >
                    </td>
                    </tr>
                    -->
                    */ %>
<tr>
    <td colspan="2" height="5">
        <input type="hidden" value="yes" name="versioningEnabled">
    </td>
</tr>
<% /*
                    <!-- temporarily deactivated because this is not yet fully functional in the back-end -->
                    <!--
                    <tr>
                    <td>
                    <internal:adminResourceBundle resourceName="org.jahia.admin.site.ManageSites.enableStaging.label"/>&nbsp;
                    </td>
                    <td>
                    :&nbsp;
                    <input disabled class="input" type="checkbox" value="yes" name="stagingEnabled" if ( stagingEnabled.booleanValue() ) { out.print("checked=\"true\"") } >
                    </td>
                    </tr>
                    -->
                    */ %>
<tr>
    <td colspan="2" height="5">
        <input type="hidden" value="yes" name="stagingEnabled">
    </td>
</tr>
<tr>
    <td>
        <internal:adminResourceBundle resourceName="org.jahia.admin.site.ManageSites.siteDesc.label"/>&nbsp;
    </td>
    <td>
        &nbsp;
        <textarea class="input" name="siteDescr" rows="6" cols='45'><%=siteDescr %>
        </textarea>
    </td>
</tr>
</table>
</form>
</div>
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
              <a class="ico-back" class="operationLink"
                 href='<%=JahiaAdministration.composeActionURL(request,response,"sites","&sub=list")%>'><internal:adminResourceBundle
                      resourceName="org.jahia.admin.site.ManageSites.backToSitesList.label"/></a>
            </span>
          </span>
          <span class="dex-PushButton">
            <span class="first-child">
              <a class="ico-siteDelete"
                 href='<%=JahiaAdministration.composeActionURL(request,response,"sites","&sub=delete&siteid=" + request.getAttribute("siteID"))%>'><internal:adminResourceBundle
                      resourceName="org.jahia.admin.delete.label"/></a>
            </span>
          </span>
          <span class="dex-PushButton">
            <span class="first-child">
              <a class="ico-ok" href="javascript:sendForm();"><internal:adminResourceBundle
                      resourceName="org.jahia.admin.save.label"/></a>
            </span>
          </span>
</div>
</div>
<%@include file="/jsp/jahia/administration/include/footer.inc" %>