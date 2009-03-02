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

<%@include file="/admin/include/header.inc"%>
<%@page import   = "java.util.*,org.jahia.data.templates.*"%>
<%@page import="org.jahia.bin.*"%>
<%

    String theURL = "";
    String requestURI 	= (String)request.getAttribute("requestURI");
    String contextRoot 	= (String)request.getContextPath();

    String warningMsg	= (String)request.getAttribute("warningMsg");

    String templName 	= (String)request.getAttribute("templName");
    String rootFolder 	= (String)request.getAttribute("rootFolder");
    String fileName 	= (String)request.getAttribute("fileName");
    String pageType 	= (String)request.getAttribute("pageType");
    Integer isAvailable = (Integer)request.getAttribute("isAvailable");

    JahiaSite site = (JahiaSite)request.getAttribute("site");

    Integer templateLimit = (Integer)request.getAttribute("templateLimit");
    Boolean canAddNew = (Boolean)request.getAttribute("canAddNew");

%>
<script type="text/javascript">
    function sendForm(subAction) {
        document.mainForm.subaction.value=subAction;
        document.mainForm.action="<%=requestURI%>?do=templates&sub=add";
        document.mainForm.submit();
    }
</script>

<div id="topTitle">
	<div id="topTitleLogo">
		<img src="<%=URL%>images/icons/admin/briefcase_document.gif" width="48" height="48" />
  </div>
  <h1 id="topTitleLabel"><fmt:message key="org.jahia.admin.manageTemplates.label"/><br><% if ( site!= null ){%><fmt:message key="org.jahia.admin.site.label"/>&nbsp;<%=site.getServerName()%><%}%></h1>
</div>

<div id="adminMainContent">

<% if ( canAddNew.booleanValue() ) { %>

  <h2>
    <fmt:message key="org.jahia.admin.templates.ManageTemplates.manuallyRegister.label"/>&nbsp;:
  </h2>
  
  <p>
    <fmt:message key="org.jahia.admin.templates.ManageTemplates.manuallyRegisterNewTemplate.label"/>&nbsp;:
  </p>
  <p>
    <b>Jahia&nbsp;home/templates/<%=currentSite.getSiteKey()%></b>
  </p>
  
  <h3>
    <fmt:message key="org.jahia.admin.templates.ManageTemplates.newTemplateInfo.label"/>&nbsp;:
  </h3>
  
  <form name="mainForm" action="<%=requestURI%>?do=addtemplate" method="post">
    <table border="0" cellpadding="5" cellspacing="0" style="width : 100%">
  
    <%
        if ( warningMsg.length()>0 ){
    %>
    <tr>
        <td colspan="2" class="errorBold">
          <%=warningMsg%>
        </td>
    </tr>
  
    <% } %>
    <tr>
        <td align="right" style="width:50%" >
            *<fmt:message key="org.jahia.admin.templates.ManageTemplates.templateName.label"/>&nbsp;:
        </td>
        <td>
          <input class="input" type="text" name="templName" value="<%=templName%>" size="<%=inputSize%>">
        </td>
    </tr>
    <tr>
        <td align="right">
            *<fmt:message key="org.jahia.admin.templates.ManageTemplates.templateFolder.label"/>&nbsp;:
        </td>
        <td width="90%">
          <input class="input" type="text" name="rootFolder" value="<%=rootFolder%>" size="<%=inputSize%>">
        </td>
    </tr>
    <tr>
        <td align="right">
            *<fmt:message key="org.jahia.admin.templates.ManageTemplates.fileName.label"/>&nbsp;:
        </td>
        <td>
          <input class="input" type="text" name="fileName" value="<%=fileName%>" size="<%=inputSize%>">
        </td>
    </tr>
    <tr>
        <td align="right">
            *<fmt:message key="org.jahia.admin.templates.ManageTemplates.fileName.label"/>&nbsp;:
        </td>
        <td>
            <select name="pageType">
                <option value=""> ---------&nbsp;&nbsp;<fmt:message key="org.jahia.admin.site.ManageSites.pleaseChooseTemplateSet.label"/>&nbsp;&nbsp;---------&nbsp;</option>
                <c:forEach items="${type}" var="types">
                    <option value="<c:out value='${type.key}'/>" <c:if test="${type.key == pageType}">selected="selected"</c:if>><c:out value="${type.value}"/>
                </c:forEach>
            </select>
        </td>
    </tr>
    <tr>
        <td align="right">
            <fmt:message key="org.jahia.admin.availableToUsers.label"/>&nbsp;:
        </td>
        <td>
          <input type="checkbox" name="isAvailable" value="1" <% if (isAvailable.intValue()==1){%>checked<% } %>>
        </td>
    </tr>
    <tr>
        <td align="right" colspan="2">
            &nbsp;<br>
        </td>
    </tr>
  
    </table>
    
    <div class="buttonList" style="text-align: right; padding-top: 30px; padding-bottom: 20px">
      <div class="button">
        <a href="javascript:sendForm('save');"><fmt:message key="org.jahia.admin.save.label"/></a>
      </div>
    </div>
  
    <input type="hidden" name="subaction" value="">

  </form>
<% } else { %>
  <table border="0" cellpadding="0" width="90%">
  <tr>
      <td colspan="2" class="text" align="left"><b><fmt:message key="org.jahia.admin.licenseLimitation.label"/>&nbsp;:</b><br><br>&nbsp;</td>
  </tr>
  <tr>
      <td valign="top" align="left" colspan="2" class="text">
          <b><fmt:message key="org.jahia.admin.templates.ManageTemplates.numberTemplatePerSite.label"/> <%=templateLimit.intValue()%>&nbsp;.</b>
      </td>
  </tr>

  </table>
  
  <input type="hidden" name="subaction" value="">
<% } %>

  <div id="operationMenu">
  	<div id="operationMenuLabel">
			<fmt:message key="org.jahia.admin.otherOperations.label"/>&nbsp;:
		</div>
		<ul id="operationList">
      <li class="operationEntry">
      	<a class="operationLink" href='<%=JahiaAdministration.composeActionURL(request,response,"templates","&sub=display")%>'><fmt:message key="org.jahia.admin.templates.ManageTemplates.backToTemplatesList.label"/></a>
      </li>     		
      <li class="operationEntry">
      	<a class="operationLink" href='<%=JahiaAdministration.composeActionURL(request,response,"displaymenu","")%>'><fmt:message key="org.jahia.admin.backToMenu.label"/></a>
      </li>     		
    </ul>
  </div>

</div>

<%@include file="/admin/include/footer.inc"%>