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
<%@page import   = "java.util.*" %>
<%@page import="org.jahia.bin.*"%>
<%@taglib uri="http://www.jahia.org/tags/internalLib" prefix="internal" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<utility:setBundle basename="JahiaInternalResources"/>
<jsp:useBean id="url"     		class="java.lang.String"        scope="request"/>     <% // http files path. %>
<jsp:useBean id="input"   		class="java.lang.String"        scope="request"/>     <% // inputs size. %>
<jsp:useBean id="values" 		class="java.util.HashMap" 	scope="request"/>     <% // Map containing values. %>
<jsp:useBean id="subAction"		class="java.lang.String"    scope="request"/>     	  <% // the default screen %>

<%
    Boolean isLynx = (Boolean) request.getAttribute("isLynx"); // Linx.
%>

<h3>
  <fmt:message key="org.jahia.admin.search.ManageSearch.whatIndexOptimiz.label"/>
</h3>

<p>
  <fmt:message key="org.jahia.admin.search.ManageSearch.indexOptimCompact.label"/>.
</p>
<p>
  <fmt:message key="org.jahia.admin.search.ManageSearch.noteWhenOptimiz.label"/>.
</p>

<h3>
  <fmt:message key="org.jahia.admin.search.ManageSearch.whenToIndexOptimiz.label"/>
</h3>

<p>
  <fmt:message key="org.jahia.admin.search.ManageSearch.performIndexOptimiz.label"/>.
</p>

<table border="0" cellpadding="1" cellspacing="0" style="width:100%">
<tr>
    <td align="left">
      <div class="buttonList" style="padding-top: 30px; padding-bottom : 10px">
        <div class="button">
          <a href="javascript:submitFormular('<%=subAction%>','back');"><fmt:message key="org.jahia.admin.previousStep.button.label"/></a>
        </div>
      </div>
    </td>
    <td align="right">
      <div class="buttonList" style="padding-top: 30px; padding-bottom : 10px">
        <div class="button">
          <a href="javascript:submitFormular('<%=subAction%>','ok');"><fmt:message key="org.jahia.admin.ok.label"/></a>
        </div>
      </div>
    </td>
</tr>

</table>

  <div id="operationMenu">
  	<div id="operationMenuLabel">
			<fmt:message key="org.jahia.admin.otherOperations.label"/>&nbsp;:
		</div>
		<ul id="operationList">
      <li class="operationEntry">
      	<a class="operationLink" href='<%=JahiaAdministration.composeActionURL(request,response,"displaymenu","")%>'><fmt:message key="org.jahia.admin.backToMenu.label"/></a>
      </li>     		
    </ul>
  </div>
