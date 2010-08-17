<%@page language = "java" %>
<%@page import="org.jahia.bin.*"%>
<%@page import = "java.util.*"%>
<%@taglib uri="http://www.jahia.org/tags/internalLib" prefix="internal" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<utility:setBundle basename="JahiaInternalResources" useUILocale="true"/>
<jsp:useBean id="URL" class="java.lang.String" scope="request"/>   <% // http files path. %>

<%
    String groupName = (String)request.getAttribute("groupName");
    List groupMembership = (List)request.getAttribute("groupMembership");
    int stretcherToOpen   = 0;
%>

<!-- Adiministration page position -->
<div id="topTitle">
	<div id="topTitleLogo">
		<img alt="user1_into" src="<%=URL%>images/icons/admin/user1_into.gif" width="48" height="48" border="0" />
  </div>
  <h1 id="topTitleLabel"><fmt:message key="org.jahia.admin.viewGroupMemberships.label"/></h1>
</div>

<div id="adminMainContent">
  
  <h2>
    <fmt:message key="org.jahia.admin.membershipsForGroup.label"/> <b><%= groupName%></b>
  </h2>

<!-- Create new group -->
    <!-- For future version : <script language="javascript" src="../search_options.js"></script> -->
    <table border="0" style="width:100%" class="evenOddTable">
      <thead>
        <tr>
            <th><fmt:message key="label.username"/></th>
            <th align="right"><fmt:message key="org.jahia.admin.homeSite.label"/>&nbsp;&nbsp;</th>
        </tr>
      </thead>
      <tbody>
            <%
            Iterator it = groupMembership.iterator();
            if (!it.hasNext()) { %>
            <tr>
              <td colspan="2"><br><fmt:message key="org.jahia.admin.noMembershipFromOtherSites.label"/></td>
            </tr>
            <%
            } else {
                String lineClass="oddLine";
                while (it.hasNext()) {
                    String groupMembers = (String)it.next();
                    String memberSite = (String)it.next();
                    if ("oddLine".equals(lineClass)) {
                      lineClass="evenLine";
                    } else {
                      lineClass="oddLine";
                    }
            %>
            <tr class="<%=lineClass%>">
              <td><%= groupMembers%></td>
              <td align="right"><%= memberSite%>&nbsp;&nbsp;</td>
            </tr>
            <%
                }
            }
            %>
      </tbody>
    </table>
<br><br>
  <div id="operationMenu" style="clear:both">
  	<div id="operationMenuLabel">
			<fmt:message key="org.jahia.admin.otherOperations.label"/>&nbsp;:
		</div>
		<ul id="operationList">
      <li class="operationEntry">
      	<a class="operationLink" href='<%=JahiaAdministration.composeActionURL(request,response,"groups","&sub=display")%>'><fmt:message key="label.backToGroupList"/></a>
      </li>
      <li class="operationEntry">
      	<a class="operationLink" href='<%=JahiaAdministration.composeActionURL(request,response,"displaymenu","")%>'><fmt:message key="label.backToMenu"/></a>
      </li>
    </ul>
  </div>

</div>