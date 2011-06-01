<%@page import="org.jahia.bin.*"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
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

<div id="topTitle">
<h1>Jahia</h1>
<h2 class="edit">
      <fmt:message key="org.jahia.admin.site.ManageSites.manageVirtualSites.label"/>
</h2>
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
					 <fmt:message key="org.jahia.admin.site.ManageSites.createAdministrator.label"/>
				</div>
				
    <div  class="object-shared">
        <fmt:message key="label.step"/> 1a / 3
    </div>

 			</div>
   <c:if test="${not empty warningMsg}">          
  <p class="errorbold">
    <%=warningMsg%>
  </p>
  </c:if>
    <c:if test="${not empty engineMessages && engineMessages.size > 0}">
    <c:if test="${engineMessages.size == 1}">
            <c:forEach items="${engineMessages.messages}" var="msg">
                <span class="errorbold"><internal:message name="msg"/></span>
            </c:forEach>
    </c:if>
    <c:if test="${engineMessages.size != 1}">
            <ul>
                <c:forEach items="${engineMessages.messages}" var="msg">
                    <li class="errorbold"><internal:message name="msg"/></li>
                </c:forEach>
            </ul>
    </c:if>
    </c:if>
  <form name="jahiaAdmin" action='<%=JahiaAdministration.composeActionURL(request,response,"sites","&sub=processcreateadmin")%>' method="post">
    <table  border="0" cellpadding="5" cellspacing="0" border="0">
    <tr>
        <td class="asterisk">*&nbsp;</td>
        <td>
            <fmt:message key="label.username"/>&nbsp;:&nbsp;
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
            <fmt:message key="label.email"/>&nbsp;:&nbsp;
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
            <fmt:message key="label.password"/>&nbsp;:&nbsp;
        </td>
        <td>
            <input class="input" type="password" name="adminPassword" size="<%=inputSize%>" maxlength="255" value="<%=adminPassword%>">
        </td>
    </tr>
    <tr>
        <td class="asterisk">*</td>
        <td>
            <fmt:message key="label.comfirmPassword"/>&nbsp;:&nbsp;
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
      	 <a class="ico-next" href="javascript:document.jahiaAdmin.submit();" onclick="showWorkInProgress(); return true;">          
          <fmt:message key="label.nextStep"/>
        </a>
      </span>
     </span>
    <% } %> 	      
    </div>
</div>

<%@include file="/admin/include/footer.inc"%>