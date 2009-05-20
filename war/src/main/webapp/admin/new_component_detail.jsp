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
<%@include file="/admin/include/header.inc"%>
<%@page import   = "java.util.*,org.jahia.data.applications.*,org.jahia.data.webapps.*"%>
<%@page import="org.jahia.bin.*"%>

<%
    String theURL = "";
    JahiaWebAppsPackage aPackage = (JahiaWebAppsPackage)request.getAttribute("aPackage");

    String requestURI = (String)request.getAttribute("requestURI");
    String contextRoot = (String)request.getContextPath();
    Boolean canDeploy = (Boolean)request.getAttribute("canDeploy");
%>

<script type="text/javascript">

        function sendForm(theAction)
        {
            document.mainForm.subaction.value = theAction;
            document.mainForm.submit();
        }

</script>

<div id="topTitle">
	<div id="topTitleLogo">
		<img src="<%=URL%>images/icons/admin/application.gif" width="48" height="48" alt="Applications" />
  </div>
  <h1 id="topTitleLabel"><fmt:message key="org.jahia.admin.manageComponents.label"/></h1>
</div>

<div id="adminMainContent">
	
	<h2><fmt:message key="org.jahia.admin.components.ManageComponents.componentDetails.label"/>&nbsp;:</h2>

        <form name="mainForm" action="<%=requestURI%>?do=sharecomponents&sub=details&package_name=<%=aPackage.getFileName()%>" method="post">
        <table border="0"  cellpadding="0">
        <% if ( !canDeploy.booleanValue() ) { %>
          <div class="text">
                 <fmt:message key="org.jahia.admin.components.ManageComponents.applicationCannotBeDeployed.label"/>
          </div>
        <% } %>
        <tr>
            <td valign="top" align="left" colspan="2">
                <table border="0" cellpadding="0" cellspacing="0" >
                <tr>
                    <td valign="top" align="left" nowrap>
                        <font class="text"><b><% if (!aPackage.isDirectory()) { %><fmt:message key="org.jahia.admin.file.label"/><% } else { %><fmt:message key="org.jahia.admin.directoryName.label"/><% } %>&nbsp;:</b></font>
                    </td>
                    <td valign="top" align="left">
                        <font class="text"><b>&nbsp;&nbsp;&nbsp;<% if ( aPackage.isDirectory() ) {%>/<%} %><%=aPackage.getFileName()%></b></font>
                    </td>
                </tr>
                </table>
            </td>
        </tr>
        <tr>
            <td colspan="2"><br></td>
        </tr>
        <tr>
            <td colspan="2" class="text"align="left"><b><fmt:message key="org.jahia.admin.components.ManageComponents.listWebApplications.label"/>&nbsp;:</b></td>
        </tr>
        <tr>
            <td colspan="2"><br></td>
        </tr>
        <tr>
            <td colspan="2" width="100%">
                <table border="0" cellpadding="0" cellspacing="0" width="100%">

                <%
                    JahiaWebAppDef webApp = null;
                    List webApps = aPackage.getWebApps();
                    int size = webApps.size();
                    int count = 0;

                    if ( size>0 ){

                        for ( int i=0 ; i<size ; i++ ) {

                            webApp = (JahiaWebAppDef)webApps.get(i);
                            if ( webApp != null ){
                                count +=1;

                    %>
                    <tr>
                        <td valign="top" align="right" nowrap>
                            <font class="text"><fmt:message key="org.jahia.admin.components.ManageComponents.webAppName.label"/>&nbsp;:</font>
                        </td>
                        <td width="90%">
                            <font class="text">&nbsp;<b><%=webApp.getName()%></b></font>
                            <br>
                        </td>
                    </tr>
                    <tr>
                        <td valign="top" align="right" nowrap>
                            <font class="text"><fmt:message key="org.jahia.admin.components.ManageComponents.webAppContext.label"/>&nbsp;:</font>
                            <br>
                        </td>
                        <td width="90%">
                            <font class="text">&nbsp;<%=webApp.getContextRoot()%></font>
                            <br>
                        </td>

                    </tr>
                    <tr>
                        <td valign="top" align="right" nowrap>
                            <font class="text"><fmt:message key="org.jahia.admin.description.label"/>&nbsp;:</font>
                        </td>
                        <td >
                            <font class="text">&nbsp;<%=webApp.getdesc()%></font>
                            <br>
                        </td>
                    </tr>
                    <tr>
                        <td colspan="2" height="10"></td>
                    </tr>
                    <tr>
                        <td valign="top" align="right" nowrap >
                            <font class="text"><fmt:message key="org.jahia.admin.components.ManageComponents.listOfServlets.label"/>&nbsp;:</font>
                        </td>
                        <td><br></td>
                    </tr>

                    <tr>
                        <td colspan="2" height="5"></td>
                        <table border="0" cellpadding="0" cellspacing="0">
                        <tr>
                            <td colspan="2" height="5"></td>
                        </tr>

                    <%
                        List servlets = webApp.getServlets();
                        Servlet_Element servlet = null;
                        int nbServlet = 0;

                        if ( servlets != null ){
                            nbServlet = servlets.size();
                        }

                        for ( int j=0 ; j<nbServlet ; j++ ){

                            servlet = (Servlet_Element)servlets.get(j);

                        %>
                    <tr>
                        <td valign="top" align="right" nowrap>
                            <font class="text"><fmt:message key="org.jahia.admin.name.label"/>&nbsp;:</font>
                        </td>
                        <td >
                            <font class="text">&nbsp;<%=servlet.getName()%></font>
                            <br>
                        </td>
                    </tr>
                    <tr>
                        <td valign="top" align="right" nowrap>
                            <font class="text"><fmt:message key="org.jahia.admin.components.ManageComponents.source.label"/>&nbsp;:</font>
                        </td>
                        <td >
                            <font class="text">&nbsp;<%=servlet.getSource()%></font>
                            <br>
                        </td>
                    </tr>
                    <tr>
                        <td valign="top" align="right" nowrap>
                            <font class="text"><fmt:message key="org.jahia.admin.type.label"/>&nbsp;:</font>
                        </td>
                        <td >
                            <font class="text">&nbsp;<%=servlet.getTypeLabel()%></font>
                            <br>
                        </td>
                    </tr>
                    <tr>
                        <td valign="top" align="right" nowrap>
                            <font class="text"><fmt:message key="org.jahia.admin.description.label"/>&nbsp;:</font>
                        </td>
                        <td>
                            <font class="text">&nbsp;<%=servlet.getdesc()%></font>
                            <br>
                        </td>
                    </tr>
                    <tr>
                        <td colspan="2" height="10" class="text"></td>
                    </tr>


                    <% 			}
                    %>
                    </table>
                    </td>
                    </tr>


                    <%		}
                        }
                    }

                    if ( count==0 ){

                    %>
                    <tr>
                        <td valign="top" align="right" nowrap colspan="2">
                            <font class="text"><b><fmt:message key="org.jahia.admin.components.ManageComponents.noWebComponents.label"/></b></font>
                        </td>
                    </tr>
                    <tr>
                        <td colspan="2"><br></td>
                    </tr>
                    <% }

                    %>
                    </table>
            </td>
        </tr>

        </table>
        </form>

  <div style="text-align:center">
                &nbsp;<br>
                <input type="hidden" name="subaction" value="">
                <a href="javascript:sendForm('delete');" onMouseOut="MM_swapImgRestore()" onMouseOver="MM_swapImage('delete','','${pageContext.request.contextPath}<fmt:message key="org.jahia.deleteOn.button"/>',1)"><img name="delete" src="${pageContext.request.contextPath}<fmt:message key="org.jahia.deployOff.button"/>" width="69" height="17" border="0" alt="<fmt:message key="org.jahia.admin.delete.label"/>"></a>
                <% if ( canDeploy.booleanValue() ){%>
                    <a href="javascript:sendForm('deploy');" onMouseOut="MM_swapImgRestore()" onMouseOver="MM_swapImage('deploy','','${pageContext.request.contextPath}<fmt:message key="org.jahia.deployOn.button"/>',1)"><img name="deploy" src="${pageContext.request.contextPath}<fmt:message key="org.jahia.deployOff.button"/>" width="69" height="17" border="0" alt="<fmt:message key="org.jahia.admin.deploy.label"/>"></a>
                <% } else { %>
                    <a href='<%=JahiaAdministration.composeActionURL(request,response,"sharecomponents","&sub=displaynewlist")%>' onMouseOut="MM_swapImgRestore()" onMouseOver="MM_swapImage('cancel','','${pageContext.request.contextPath}<fmt:message key="org.jahia.cancelOn.button"/>',1)"><img name="cancel" src="${pageContext.request.contextPath}<fmt:message key="org.jahia.cancelOff.button"/>" width="69" height="17" border="0" alt="<fmt:message key="org.jahia.admin.cancel.label"/>"></a>
                <% } %>
  </div>

  <div id="operationMenu">
  	<div id="operationMenuLabel">
			<fmt:message key="org.jahia.admin.otherOperations.label"/>&nbsp;:
		</div>
		<ul id="operationList">
      <li class="operationEntry">
        <a class="operationLink" href='<%=JahiaAdministration.composeActionURL(request,response,"sharecomponents","&sub=displaynewlist")%>'><fmt:message key="org.jahia.admin.components.ManageComponents.backToNewComponentsList.label"/></a>
      </li>
      <li class="operationEntry">
        <a class="operationLink" href='<%=JahiaAdministration.composeActionURL(request,response,"sharecomponents","&sub=display")%>'><fmt:message key="org.jahia.admin.components.ManageComponents.backToComponentsList.label"/></a>
      </li>
      <li class="operationEntry">
      	<a class="operationLink" href='<%=JahiaAdministration.composeActionURL(request,response,"displaymenu","")%>'><fmt:message key="org.jahia.admin.backToMenu.label"/></a>
      </li>     		
    </ul>
  </div>

</div>

<%@include file="/admin/include/footer.inc"%>