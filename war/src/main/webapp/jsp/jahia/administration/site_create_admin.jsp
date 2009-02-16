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

<%@page import="org.jahia.bin.*"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@include file="/jsp/jahia/administration/include/header.inc"%>

<%
    String warningMsg		    = (String)request.getAttribute("warningMsg");
    String adminUsername		= (String)request.getAttribute("adminUsername");
    String adminPassword	    = (String)request.getAttribute("adminPassword");
    String adminConfirm		    = (String)request.getAttribute("adminConfirm");
    String adminFirstName		= (String)request.getAttribute("adminFirstName");
    String adminLastName        = (String)request.getAttribute("adminLastName");
    String adminEmail           = (String)request.getAttribute("adminEmail");
    String adminOrganization    = (String)request.getAttribute("adminOrganization");
    stretcherToOpen   = 0;
%>

<%if(!isConfigWizard){%>
<div id="topTitle">
<h1>Jahia</h1>
<h2 class="edit">
<%if(!isConfigWizard){%>
      <internal:adminResourceBundle resourceName="org.jahia.admin.site.ManageSites.manageVirtualSites.label"/>
      <%}else{%>
      <internal:message key="org.jahia.createSite.siteFactory"/>
      <%}%>   
</h2>
</div>
<% } %>
<div id="main">
<table style="width: 100%;" class="dex-TabPanel" cellpadding="0"
	cellspacing="0">
	<tbody>
		<%if(!isConfigWizard){%>
        <tr>
			<td style="vertical-align: top;" align="left">
				<%@include file="/jsp/jahia/administration/include/tab_menu.inc"%>
			</td>
		</tr>
        <% } %>
		<tr>
			<td style="vertical-align: top;" align="left" height="100%">
			<% if(!isConfigWizard){ %>
            <div class="dex-TabPanelBottom">
			<div class="tabContent">
                <jsp:include page="/jsp/jahia/administration/include/left_menu.jsp">
                    <jsp:param name="mode" value="server"/>
                </jsp:include>
			           
			<div id="content" class="fit">
             <% } else { %>
             <div class="dex-TabPanelBottom-full">
                 
            <div id="content" class="full">
             <% } %>
			<div class="head">
				<div class="object-title">
					 <internal:adminResourceBundle resourceName="org.jahia.admin.site.ManageSites.createAdministrator.label"/>
				</div>
				
				<%if(!isConfigWizard){%>
    <div  class="object-shared">
        <internal:adminResourceBundle resourceName="org.jahia.step.label"/> 1a / 3
    </div>
   <% } %>

 			</div>
   <c:if test="${not empty warningMsg}">          
  <p class="errorbold">
    <%=warningMsg%>
  </p>
  </c:if>
    <logic:present name="engineMessages">
    <logic:equal name="engineMessages" property="size" value="1">
            <logic:iterate name="engineMessages" property="messages" id="msg">
                <span class="errorbold"><internal:message name="msg"/></span>
            </logic:iterate>
    </logic:equal>
    <logic:notEqual name="engineMessages" property="size" value="1">
            <ul>
                <logic:iterate name="engineMessages" property="messages" id="msg">
                    <li class="errorbold"><internal:message name="msg"/></li>
                </logic:iterate>
            </ul>
    </logic:notEqual>
    </logic:present>
  <form name="jahiaAdmin" action='<%=JahiaAdministration.composeActionURL(request,response,"sites","&sub=processcreateadmin")%>' method="post">
    <table  border="0" cellpadding="5" cellspacing="0" border="0">
    <tr>
        <td class="asterisk">*&nbsp;</td>
        <td>
            <internal:adminResourceBundle resourceName="org.jahia.admin.username.label"/>&nbsp;:&nbsp;
        </td>
        <td>
            <input  class="input" type="text" name="adminUsername" size="<%=inputSize%>" maxlength="255" value="<%=adminUsername%>">
        </td>
    </tr>
    <tr>
        <td></td>
        <td nowrap>
            <internal:adminResourceBundle resourceName="org.jahia.admin.firstName.label"/>&nbsp;:&nbsp;
        </td>
        <td>
            <input class="input" type="text" name="adminFirstName" size="<%=inputSize%>" maxlength="255" value="<%=adminFirstName%>">
        </td>
    </tr>
    <tr>
        <td></td>
        <td>
            <internal:adminResourceBundle resourceName="org.jahia.admin.lastName.label"/>&nbsp;:&nbsp;
        </td>
        <td>
            <input class="input" type="text" name="adminLastName" size="<%=inputSize%>" maxlength="255" value="<%=adminLastName%>">
        </td>
    </tr>
    <tr>
        <td>&nbsp;</td>
        <td>
            <internal:adminResourceBundle resourceName="org.jahia.admin.eMail.label"/>&nbsp;:&nbsp;
        </td>
        <td>
            <input class="input" type="text" name="adminEmail" size="<%=inputSize%>" maxlength="255" value="<%=adminEmail%>">
        </td>
    </tr>
    <tr>
        <td>&nbsp;</td>
        <td>
            <internal:adminResourceBundle resourceName="org.jahia.admin.organization.label"/>&nbsp;:&nbsp;
        </td>
        <td>              
            <input class="input" type="text" name="adminOrganization" size="<%=inputSize%>" maxlength="255" value="<%=adminOrganization%>">
        </td>
    </tr>
    <tr>
        <td class="asterisk">*</td>
        <td>
            <internal:adminResourceBundle resourceName="org.jahia.admin.password.label"/>&nbsp;:&nbsp;
        </td>
        <td>
            <input class="input" type="password" name="adminPassword" size="<%=inputSize%>" maxlength="255" value="<%=adminPassword%>">
        </td>
    </tr>
    <tr>
        <td class="asterisk">*</td>
        <td>
            <internal:adminResourceBundle resourceName="org.jahia.admin.confirmPassword.label"/>&nbsp;:&nbsp;
        </td>
        <td>
            <input class="input" type="password" name="adminConfirm" size="<%=inputSize%>" maxlength="255" value="<%=adminConfirm%>">
        </td>
    </tr>
    </table>
 
    
  
  </form>
</div> 
			
			</td>
		</tr>
	</tbody>
</table>
</div>
<div id="actionBar">
<%if(!isConfigWizard){%>

  	<span class="dex-PushButton"> 
	  <span class="first-child">
      	 <% if (session.getAttribute(JahiaAdministration.CLASS_NAME + "noSites") != null) { %>
        		<a class="ico-back" href='<%=JahiaAdministration.composeActionURL(request,response,"sites","&sub=list")%>'><internal:adminResourceBundle resourceName="org.jahia.admin.site.ManageSites.backToPreviousStep.label"/></a>
		<% } else { %>
		        <a class="ico-back" href='<%=JahiaAdministration.composeActionURL(request,response,"sites","&sub=add")%>'><internal:adminResourceBundle resourceName="org.jahia.admin.site.ManageSites.backToPreviousStep.label"/></a>
		<% } %>
      </span>
     </span> 
  	<% if (session.getAttribute(JahiaAdministration.CLASS_NAME + "redirectToJahia") == null) { %>  	
  	<span class="dex-PushButton"> 
	  <span class="first-child">
      	 <a class="ico-ok" href="javascript:document.jahiaAdmin.submit();">          
          <internal:adminResourceBundle resourceName="org.jahia.admin.save.label"/>
        </a>
      </span>
     </span>
    <% } %> 	      
<%  } else { %>
 	<span class="dex-PushButton"> 
	  <span class="first-child">
	  	 <a class="ico-back" href='<%=JahiaAdministration.composeActionURL(request,response,"sites","&sub=list")%>'>
           <internal:message key="org.jahia.back.button"/>
        </a>
	  </span>
	</span>
	<span class="dex-PushButton"> 
	  <span class="first-child">
	  	  <a class="ico-next" href="javascript:document.jahiaAdmin.submit();">          
          <internal:message key="org.jahia.nextStep.button"/>          
        </a>
	  </span>
	</span>
  <%}%>
    </div>
</div>

<%@include file="/jsp/jahia/administration/include/footer.inc"%>