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
<%@page import="org.jahia.bin.*"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@include file="/admin/include/header.inc"%>

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
      <fmt:message key="org.jahia.admin.site.ManageSites.manageVirtualSites.label"/>
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
				<%@include file="/admin/include/tab_menu.inc"%>
			</td>
		</tr>
        <% } %>
		<tr>
			<td style="vertical-align: top;" align="left" height="100%">
			<% if(!isConfigWizard){ %>
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
					 <fmt:message key="org.jahia.admin.site.ManageSites.createAdministrator.label"/>
				</div>
				
				<%if(!isConfigWizard){%>
    <div  class="object-shared">
        <fmt:message key="org.jahia.step.label"/> 1a / 3
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
            <fmt:message key="org.jahia.admin.username.label"/>&nbsp;:&nbsp;
        </td>
        <td>
            <input  class="input" type="text" name="adminUsername" size="<%=inputSize%>" maxlength="255" value="<%=adminUsername%>">
        </td>
    </tr>
    <tr>
        <td></td>
        <td nowrap>
            <fmt:message key="org.jahia.admin.firstName.label"/>&nbsp;:&nbsp;
        </td>
        <td>
            <input class="input" type="text" name="adminFirstName" size="<%=inputSize%>" maxlength="255" value="<%=adminFirstName%>">
        </td>
    </tr>
    <tr>
        <td></td>
        <td>
            <fmt:message key="org.jahia.admin.lastName.label"/>&nbsp;:&nbsp;
        </td>
        <td>
            <input class="input" type="text" name="adminLastName" size="<%=inputSize%>" maxlength="255" value="<%=adminLastName%>">
        </td>
    </tr>
    <tr>
        <td>&nbsp;</td>
        <td>
            <fmt:message key="org.jahia.admin.eMail.label"/>&nbsp;:&nbsp;
        </td>
        <td>
            <input class="input" type="text" name="adminEmail" size="<%=inputSize%>" maxlength="255" value="<%=adminEmail%>">
        </td>
    </tr>
    <tr>
        <td>&nbsp;</td>
        <td>
            <fmt:message key="org.jahia.admin.organization.label"/>&nbsp;:&nbsp;
        </td>
        <td>              
            <input class="input" type="text" name="adminOrganization" size="<%=inputSize%>" maxlength="255" value="<%=adminOrganization%>">
        </td>
    </tr>
    <tr>
        <td class="asterisk">*</td>
        <td>
            <fmt:message key="org.jahia.admin.password.label"/>&nbsp;:&nbsp;
        </td>
        <td>
            <input class="input" type="password" name="adminPassword" size="<%=inputSize%>" maxlength="255" value="<%=adminPassword%>">
        </td>
    </tr>
    <tr>
        <td class="asterisk">*</td>
        <td>
            <fmt:message key="org.jahia.admin.confirmPassword.label"/>&nbsp;:&nbsp;
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
        		<a class="ico-back" href='<%=JahiaAdministration.composeActionURL(request,response,"sites","&sub=list")%>' onclick="showWorkInProgress(); return true;"><fmt:message key="org.jahia.admin.site.ManageSites.backToPreviousStep.label"/></a>
		<% } else { %>
		        <a class="ico-back" href='<%=JahiaAdministration.composeActionURL(request,response,"sites","&sub=add")%>' onclick="showWorkInProgress(); return true;"><fmt:message key="org.jahia.admin.site.ManageSites.backToPreviousStep.label"/></a>
		<% } %>
      </span>
     </span> 
  	<% if (session.getAttribute(JahiaAdministration.CLASS_NAME + "redirectToJahia") == null) { %>  	
  	<span class="dex-PushButton"> 
	  <span class="first-child">
      	 <a class="ico-ok" href="javascript:document.jahiaAdmin.submit();" onclick="showWorkInProgress(); return true;">          
          <fmt:message key="org.jahia.admin.save.label"/>
        </a>
      </span>
     </span>
    <% } %> 	      
<%  } else { %>
 	<span class="dex-PushButton"> 
	  <span class="first-child">
	  	 <a class="ico-back" href='<%=JahiaAdministration.composeActionURL(request,response,"sites","&sub=list")%>' onclick="showWorkInProgress(); return true;">
           <internal:message key="org.jahia.back.button"/>
        </a>
	  </span>
	</span>
	<span class="dex-PushButton"> 
	  <span class="first-child">
	  	  <a class="ico-next" href="javascript:document.jahiaAdmin.submit();" onclick="showWorkInProgress(); return true;">          
          <internal:message key="org.jahia.nextStep.button"/>          
        </a>
	  </span>
	</span>
  <%}%>
    </div>
</div>

<%@include file="/admin/include/footer.inc"%>