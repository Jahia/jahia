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
<%@page import   = "java.util.*" %>
<%@page import="org.jahia.bin.*"%>
<%@taglib uri="http://www.jahia.org/tags/internalLib" prefix="internal" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<utility:setBundle basename="JahiaInternalResources"/>
<jsp:useBean id="url"     		class="java.lang.String"        scope="request"/>     <% // http files path. %>
<jsp:useBean id="input"   		class="java.lang.String"        scope="request"/>     <% // inputs size. %>
<jsp:useBean id="values" 		class="java.util.HashMap" 	scope="request"/>     <% // Map containing values. %>
<jsp:useBean id="subAction"		class="java.lang.String"    	scope="request"/>     <% // the default screen %>

<%
    Boolean isLynx = (Boolean) request.getAttribute("isLynx"); // Linx.
    Boolean result = (Boolean) request.getAttribute("result"); // Indexation fail or not.
%>

  <%
      if ( result.booleanValue() ) {
  %>
  <h3>
    <fmt:message key="org.jahia.admin.search.ManageSearch.indexOptimizSuccess.label"/>.
  </h3>
  <%
      } else {
  %>
  <h3>
    <fmt:message key="org.jahia.admin.search.ManageSearch.errorOptimizIndex.label"/>
  </h3>
  <% } %>
  <div class="buttonList" style="text-align: right; padding-top: 30px; padding-bottom : 10px">
    <div class="button">
      <a href="javascript:document.formular.submit();"><fmt:message key="org.jahia.admin.ok.label"/></a>
    </div>
  </div>
        
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
