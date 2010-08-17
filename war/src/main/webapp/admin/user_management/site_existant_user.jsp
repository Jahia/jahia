<%@page import="org.jahia.bin.*"%>
<%@include file="/admin/include/header.inc"%>

<%
    Iterator allSites      = (Iterator)request.getAttribute("allSites");
    Iterator allSitesJS    = (Iterator)request.getAttribute("allSitesJS");
    Iterator usersList = (Iterator)request.getAttribute("usersList");
    Integer     selectedSite  = (Integer)request.getAttribute("selectedSite");
    stretcherToOpen   = 0;
%>

<div id="topTitle">
<h1>Jahia</h1>
<h2 class="edit"><fmt:message key="org.jahia.admin.users.ManageUsers.registerExistingUser.label"/></h2>
</div>
<div id="main">


<script type="text/javascript">
    function changeSite() {
    <%  String baseURL = JahiaAdministration.composeActionURL(request,response,"users","&sub=register");
    %>
        if(document.jahiaAdmin.selectSite.options[0].selected) location.href = "<%=baseURL%>&site=0";
    <%
       int countJS = 1;
       while(allSitesJS.hasNext()) {
           JahiaSite siteJS  = (JahiaSite) allSitesJS.next();
           if(siteJS.getID() != 0) { %>
        if(document.jahiaAdmin.selectSite.options[<%=countJS%>].selected) location.href = "<%=baseURL%>&site=<%=siteJS.getID()%>";
    <%
           }
           countJS++;
       }
    %>
    }
</script>

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
        <fmt:message key="org.jahia.admin.users.ManageUsers.registerExistingUser.label"/>
    </div>
</div>
<div class="content-item">
  <form name="jahiaAdmin" action='<%=JahiaAdministration.composeActionURL(request,response,"users","&sub=processRegister&site=" + selectedSite.intValue())%>' method="post">
    <table border="0" cellpadding="5" cellspacing="0" class="topAlignedTable">
      <tr>
        <% if(allSites != null) { %>
          <td>

            <fmt:message key="org.jahia.admin.users.ManageUsers.virtualSiteWhereUser.label"/>&nbsp;:
          </td>
          <td>
            <select name="selectSite" onChange="changeSite();">
                <option value="0"<% if(selectedSite.intValue()==0) { %> selected<%}%>> ---------&nbsp;&nbsp;<fmt:message key="org.jahia.admin.site.ManageSites.pleaseChooseASite.label"/>&nbsp;&nbsp;---------&nbsp;</option>
                <% while(allSites.hasNext()) {
                      JahiaSite site = (JahiaSite) allSites.next(); %>
                <option value="<%=site.getID()%>"<%if(selectedSite.intValue()==site.getID()){%> selected<%}%>><%=site.getTitle()%> </option>
                <% } %>
            </select>
          </td>
        <% } else { %>
          <td>
            <fmt:message key="org.jahia.admin.users.ManageUsers.singleVirtualSite.label"/>
          </td>
        <% } %>
     </tr>
     <tr>
        <% if(selectedSite.intValue()>0) { %>
          <td>
            <fmt:message key="org.jahia.admin.users.ManageUsers.selectTheUser.label"/>&nbsp;:
          </td>
          <td>
            <select name="userSelected" size="10" style="width: 200px">
                <% if (!usersList.hasNext()) { %>
                <option value="" disabled="disabled">------------------&nbsp;&nbsp;<fmt:message key="org.jahia.admin.users.ManageUsers.noUserFound.label"/>&nbsp;&nbsp;------------------</option>
                <% } %>
                <% while(usersList.hasNext()) {
                      Map userHash = (Map) usersList.next(); %>
                <option value='<%=userHash.get("key")%>'><%=userHash.get("username")%>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</option>
                <% } %>
            </select>
          </td>
        <% } %>
      </tr>
    </table>
   
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
      	 <a class="ico-back" href='<%=JahiaAdministration.composeActionURL(request,response,"displaymenu","")%>' onclick="showWorkInProgress(); return true;"><fmt:message key="label.backToMenu"/></a>
      </span>
     </span>
      <% if(selectedSite.intValue()>0) { %>
     <span class="dex-PushButton"> 
      <span class="first-child">
          <a class="ico-ok" href="javascript:document.jahiaAdmin.submit();" onclick="showWorkInProgress(); return true;">
           <fmt:message key="label.save"/>
         </a>
      </span>
     </span> 
    <% } %>
     	      
  </div>

</div>