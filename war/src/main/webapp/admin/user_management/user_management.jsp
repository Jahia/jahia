<%@page language = "java" %>
<%@page import = "java.util.*"%>
<%@page import="org.jahia.bin.*"%>
<%@page import="org.jahia.params.ProcessingContext"%>
<%@page import = "org.jahia.data.JahiaData"%>
<%@taglib uri="http://www.jahia.org/tags/internalLib" prefix="internal" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<utility:setBundle basename="JahiaInternalResources" useUILocale="true"/>
<jsp:useBean id="URL" class="java.lang.String" scope="request"/>   <% // http files path. %>
<jsp:useBean id="userMessage" class="java.lang.String" scope="session"/>

<%
    String userSearch = (String)request.getAttribute("userSearch");
    String currentSite = (String)request.getAttribute("currentSite");
    JahiaData jData = (JahiaData)request.getAttribute("org.jahia.data.JahiaData");
    int stretcherToOpen   = 0;
%>

<!-- For future version : <script language="javascript" src="../search_options.js"></script> -->
<script type="text/javascript" src="<%=URL%>../javascript/selectbox.js"></script>
<script type="text/javascript">

function submitForm(action)
{
    document.mainForm.action = '<%=JahiaAdministration.composeActionURL(request,response,"users","&sub=")%>' + action;
    document.mainForm.method = "post";
    document.mainForm.submit();
}

function handleKey(e)
{
    if (e.altKey && e.ctrlKey) {
        submitForm('remove');
    } else if (e.altKey) {
        submitForm('membership');
    } else if (e.ctrlKey) {
        submitForm('copy');
    } else {
        submitForm('edit');
    }
}

function handleKeyCode(code)
{
    if (code == 46) {
        submitForm('remove');
    } else if (code == 45) {
        submitForm('create');
    } else if (code == 13) {
        submitForm('edit');
    }
}

function setFocus()
{
    document.mainForm.searchString.focus();
}

</script>

<div id="topTitle">
<h1>Jahia</h1>
<h2 class="edit"><fmt:message key="label.manageUsers"/></h2>
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
                <fmt:message key="label.manageUsers"/>
                </div>
            </div>
            <div class="content-body">
            <!-- User operations -->
            <div id="operationMenu">
                <span class="dex-PushButton"> 
                    <span class="first-child">                  
                    <a class="ico-user-add" href="javascript:submitForm('create');"><fmt:message key="org.jahia.admin.users.ManageUsers.createNewUser.label"/></a>
                    </span> 
                </span>
                <span class="dex-PushButton"> 
                    <span class="first-child">                  
                    <a class="ico-user-view" href="javascript:submitForm('edit');"><fmt:message key="org.jahia.admin.users.ManageUsers.editViewProp.label"/></a>
                    </span> 
                </span>
                <span class="dex-PushButton">
                    <span class="first-child">                  
                    <a class="ico-user-delete" href="javascript:submitForm('remove');"><fmt:message key="org.jahia.admin.users.ManageUsers.removeSelectedUser.label"/></a>
                    </span> 
                </span>
                <span class="dex-PushButton">
                    <span class="first-child">
                    <a class="ico-user-view" href="javascript:submitForm('batchCreate');"><fmt:message key="org.jahia.admin.users.ManageUsers.batchCreateUsers.label"/></a>
                    </span>
                </span>
            </div>
            </div>
                <div class="head">
                    <div class="object-title">
                    <fmt:message key="org.jahia.admin.users.ManageUsers.userList.label"/>
                    </div>
                </div>
             <%
            if ( userMessage.length()>0 ){
            %>        
              <p class="${not isError ? 'blueColor' : 'errorbold'}">
                <%=userMessage%>
              </p>
            <% } %>

<form name="mainForm" action="" method="post">
<!-- User management -->
    <table border="0" style="width:100%">
        <tr>
            <td>

            <jsp:include page="<%=userSearch%>" flush="true"/>

            </td>
        </tr>
        
    </table>
<!-- -->



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
      	 <a class="ico-back" href='<%=JahiaAdministration.composeActionURL(request,response,"displaymenu","")%>'><fmt:message key="label.backToMenu"/></a>
      </span>
     </span> 	      
  </div>

</div>
<script language="javascript">
    setFocus();
</script>

