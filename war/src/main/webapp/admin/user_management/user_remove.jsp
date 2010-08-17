<%@page language = "java" %>
<%@page import = "org.jahia.utils.JahiaTools"%>
<%@page import = "org.jahia.data.JahiaData"%>
<%@page import = "java.util.*"%>
<%@page import="org.jahia.bin.*"%>
<%@taglib uri="http://www.jahia.org/tags/internalLib" prefix="internal" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<utility:setBundle basename="JahiaInternalResources" useUILocale="true"/>
<jsp:useBean id="userErrorMessage" class="java.lang.String" scope="session"/>
<%
    JahiaData jData = (JahiaData)request.getAttribute("org.jahia.data.JahiaData");    
    int stretcherToOpen   = 1;
%>
<!-- Adiministration page position -->
<div id="topTitle">
<h1>Jahia</h1>
<h2 class="edit"><fmt:message key="org.jahia.admin.users.ManageUsers.removeUser.label"/></h2>
</div>
<div id="main">
<table style="width: 100%;" class="dex-TabPanel" cellpadding="0"
    cellspacing="0">
    <tbody>
        <tr>
            <td style="vertical-align: top;" align="left">
                <%@include file="/admin/include/tab_menu.inc"%>
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
<div class="head">
    <div class="object-title">
        <fmt:message key="org.jahia.admin.users.ManageUsers.removeSelectedUser.label"/>
    </div>
</div>
<div class="content-item"><!-- Remove user -->
<form name="mainForm" action='<%=JahiaAdministration.composeActionURL(request,response,"users","&sub=processRemove")%>' method="post">
    <c:if test="${not requestScope.userReadOnly}">
        <p>
          <b><fmt:message key="label.username"/>:</b>&nbsp;&nbsp;&nbsp;${selectedUser.username}
          <input type="hidden" name="username" value="${selectedUser.username}">
        </p>
        <p>
          <fmt:message key="org.jahia.admin.users.ManageUsers.definitivelyRemove.label"/>
        </p>
        <p>
          <fmt:message key="org.jahia.admin.users.ManageUsers.definitivelyRemove.files.label"/>&nbsp;
          (<a href="<c:url value='/cms/export/default/users/${selectedUser.username}.zip?cleanup=simple'/>" target="_blank"><fmt:message key="label.export"/></a>)
        </p>
        <p>
          <fmt:message key="org.jahia.admin.users.ManageUsers.areYouSure.label"/>
        </p>
    </c:if>        
    <c:if test="${requestScope.userReadOnly}">
        <p>
          <b><fmt:message key="label.username"/>:</b>&nbsp;&nbsp;&nbsp;${selectedUser.username}
        </p>
        <p>
          <fmt:message key="org.jahia.admin.users.ManageUsers.cannotRemove.label"/>
        </p>
    </c:if>        
</form>
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
         <a class="ico-back" href='<%=JahiaAdministration.composeActionURL(request,response,"displaymenu","")%>'><fmt:message key="label.backToMenu"/></a>
      </span>
     </span>
     <span class="dex-PushButton"> 
      <span class="first-child">
         <a class="ico-cancel" href='<%=JahiaAdministration.composeActionURL(request,response,"users","&sub=display")%>'><fmt:message key="label.cancel"/></a>
      </span>
     </span>
     <c:if test="${not requestScope.userReadOnly}">     
     <span class="dex-PushButton"> 
      <span class="first-child">
         <a class="ico-ok" href="javascript:document.mainForm.submit();" ><fmt:message key="label.ok"/></a>
      </span>
     </span>      
     </c:if>
  </div>

</div>