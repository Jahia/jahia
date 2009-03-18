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

<%@page import="org.jahia.bin.JahiaAdministration"%>
<%@page import="org.jahia.params.ParamBean"%>
<%@page import="org.jahia.registries.ServicesRegistry"%>
<%@page import="org.jahia.services.acl.JahiaBaseACL"%>
<%@include file="/admin/include/header.inc"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%

    /**
     * @version $Id$
     */

    final ParamBean jParams   = (ParamBean) request.getAttribute("org.jahia.params.ParamBean" );
    String warningMsg		= (String)request.getAttribute("warningMsg");
    JahiaSite newJahiaSite	= (JahiaSite)request.getAttribute("newJahiaSite");
    Boolean defaultSite     = (Boolean)request.getAttribute("defaultSite");
    Boolean newAdminOnly    = (Boolean)request.getAttribute("newAdminOnly");
    Integer nbSites		    = (Integer)request.getAttribute("nbSites");
    Integer siteLimit	    = (Integer)request.getAttribute("siteLimit");
    stretcherToOpen   = 0;
%>

<%if(!isConfigWizard){%>
<div id="topTitle">
    <h1>Jahia</h1>
    <h2 class="edit">
    	
    	<fmt:message key="org.jahia.admin.site.ManageSites.manageVirtualSites.label"/></h2>
</div>
<% } %>
<div id="main">
<table style="width: 100%;" class="dex-TabPanel" cellpadding="0"
       cellspacing="0">
<tbody>
        <%if(!isConfigWizard){%>
<tr>
    <td style="vertical-align: top;" align="left">
        <%@include file="/admin/include/tab_menu.inc"%>
    </td>
</tr>
        <% } %>
<tr>
<td style="vertical-align: top;" align="left" height="100%">
            <%if(!isConfigWizard){%>
<div class="dex-TabPanelBottom">
<div class="tabContent">
<jsp:include page="/admin/include/left_menu.jsp">
    <jsp:param name="mode" value="server"/>
</jsp:include>

<div id="content" class="fit">
            <% } else { %>
             <div class="dex-TabPanelBottom-full">
                      
            <div id="content" class="full">
            <% } %>
<div class="head">
    <div class="object-title">
        <fmt:message key="org.jahia.admin.site.ManageSites.siteProperties.label"/>
    </div>
    <%if(!isConfigWizard){%>
    <div  class="object-shared">
        <fmt:message key="org.jahia.step.label"/> 1 / 3
    </div>
   <% } %>
</div>
<% if ( ( newJahiaSite != null ) && ( (siteLimit.intValue()==-1) || (nbSites.intValue()<siteLimit.intValue()) ) ) { %>

<form name="jahiaAdmin" action='<%=JahiaAdministration.composeActionURL(request,response,"sites","&sub=processadd")%>' method="post">
    <c:if test="${not empty warningMsg}">
        <p class="errorbold">
            <%=warningMsg%>
        </p>
    </c:if>

    <table border="0" cellpadding="5" cellspacing="0" class="topAlignedTable">
        <tr>
            <td>
                <fmt:message key="org.jahia.admin.site.ManageSites.siteTitle.label"/>*&nbsp;
                <br><em><fmt:message key="org.jahia.admin.site.ManageSites.egSiteTitle.label"/></em>
            </td>
            <td>
                :&nbsp;<input class="input" type="text" name="siteTitle" value="<%=newJahiaSite.getTitle()%>" size="<%=inputSize%>" maxlength="100">
            </td>
        </tr>
        <tr>
            <td>
                <fmt:message key="org.jahia.admin.site.ManageSites.siteServerName.label"/>*&nbsp;
                <br><em><fmt:message key="org.jahia.admin.site.ManageSites.egSiteServerName.label"/></em>
            </td>
            <td>
                :&nbsp;<input class="input" type="text" name="siteServerName" value="<%=newJahiaSite.getServerName()%>" size="<%=inputSize%>" maxlength="200">
            </td>
        </tr>
        <tr>
            <td>
                <fmt:message key="org.jahia.admin.site.ManageSites.siteKey.label"/>*&nbsp;
                <br><em><fmt:message key="org.jahia.admin.site.ManageSites.egSiteKey.label"/></em>
            </td>
            <td>
                :&nbsp;<input class="input" type="text" name="siteKey" value="<%=newJahiaSite.getSiteKey()%>" size="<%=inputSize%>" maxlength="50">
                <br><em>&nbsp;&nbsp;<fmt:message key="org.jahia.admin.site.ManageSites.siteKeyAppears.label"/></em>

            </td>
        </tr>
        <% if (ServicesRegistry.getInstance().getJahiaACLManagerService().getServerActionPermission("admin.pwdpolicy.ManagePasswordPolicies", jParams.getUser(), JahiaBaseACL.READ_RIGHTS, jParams.getSiteID()) > 0) { %>
        <tr>
            <td>
                <label for="enforcePasswordPolicy">
                    <fmt:message key="org.jahia.admin.site.ManageSites.enforcePasswordPolicy.label"/>&nbsp;
                </label>
            </td>
            <td>
                :&nbsp;<input class="input" type="checkbox" name="enforcePasswordPolicy" value="true" <c:if test="${enforcePasswordPolicy}">checked="checked"</c:if> id="enforcePasswordPolicy"/>
            </td>
        </tr>
        <% } %>
        <tr >
            <td>
                <fmt:message key="org.jahia.admin.site.ManageSites.siteDesc.label"/>&nbsp;
            </td>
            <td>
                &nbsp;<textarea class="input" name="siteDescr" rows="6" cols='45'><%=newJahiaSite.getDescr()%></textarea>
            </td>
        </tr>
        <tr>
            <td valign="top">
                <label for="setAsDefaultVirtualSite"><fmt:message key="org.jahia.admin.site.ManageSites.setAsDefaultVirtualSite.label"/></label>&nbsp;
            </td>
            <td valign="top">
                <input type="checkbox" name="defaultSite" <% if (defaultSite.booleanValue()) { %>checked<% } %> id="setAsDefaultVirtualSite" />
            </td>
        </tr>
    </table>
    <div class="head headtop">
        <div class="object-title">
            <fmt:message key="org.jahia.admin.site.ManageSites.administratorAccount.label"/>
        </div>
    </div>
    <table border="0">
        <tr>
            <td><input type="radio" name="siteAdmin" value="2" checked="checked" id="noSiteAdmin"/></td>
            <td><label for="noSiteAdmin"><fmt:message key="org.jahia.admin.site.ManageSites.noSiteAdmin.label"/></label></td>
        </tr>
        <tr>
            <td><input type="radio" name="siteAdmin" value="0" id="createNewSiteAdmin"/></td>
            <td><label for="createNewSiteAdmin"><fmt:message key="org.jahia.admin.site.ManageSites.createNewUser.label"/></label></td>
        </tr>
        <% if ( !newAdminOnly.booleanValue() ){
        %>
        <tr>
            <td><input type="radio" name="siteAdmin" value="1" id="useExistingSiteAdmin"/></td>
            <td><label for="useExistingSiteAdmin"><fmt:message key="org.jahia.admin.site.ManageSites.selectAdministrator.label"/></label></td>
        </tr>
        <% } %>
    </table>
</form>
<% } else { %>
<p class="errorbold">
    <fmt:message key="org.jahia.admin.licenseLimitation.label"/>
</p>
<p class="error">
    <fmt:message key="org.jahia.admin.site.ManageSites.numberSiteForLicense.label"/>
    <%=siteLimit.intValue()%>
</p>
<% } %>


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
      	 <a class="ico-back" class="operationLink" href='<%=JahiaAdministration.composeActionURL(request,response,"sites","&sub=list")%>' onclick="showWorkInProgress(); return true;"><fmt:message key="org.jahia.admin.site.ManageSites.backToSitesList.label"/></a>
      </span>
     </span>
  	 <span class="dex-PushButton">
      <span class="first-child">
         <a class="ico-next" href="javascript:document.jahiaAdmin.submit();" onclick="showWorkInProgress(); return true;">
             <fmt:message key="org.jahia.admin.nextStep.button.label"/>
         </a>
      </span>
     </span>
</div>

</div>

<%@include file="/admin/include/footer.inc"%>