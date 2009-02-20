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

<%@include file="/jsp/jahia/administration/include/header.inc"%>
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
  <h1 id="topTitleLabel"><utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.manageComponents.label"/></h1>
</div>

<div id="adminMainContent">
	
	<h2><utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.components.ManageComponents.componentDetails.label"/>&nbsp;:</h2>

        <form name="mainForm" action="<%=requestURI%>?do=sharecomponents&sub=details&package_name=<%=aPackage.getFileName()%>" method="post">
        <table border="0"  cellpadding="0">
        <% if ( !canDeploy.booleanValue() ) { %>
          <div class="text">
                 <utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.components.ManageComponents.applicationCannotBeDeployed.label"/>
          </div>
        <% } %>
        <tr>
            <td valign="top" align="left" colspan="2">
                <table border="0" cellpadding="0" cellspacing="0" >
                <tr>
                    <td valign="top" align="left" nowrap>
                        <font class="text"><b><% if (!aPackage.isDirectory()) { %><utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.file.label"/><% } else { %><utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.directoryName.label"/><% } %>&nbsp;:</b></font>
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
            <td colspan="2" class="text"align="left"><b><utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.components.ManageComponents.listWebApplications.label"/>&nbsp;:</b></td>
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
                            <font class="text"><utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.components.ManageComponents.webAppName.label"/>&nbsp;:</font>
                        </td>
                        <td width="90%">
                            <font class="text">&nbsp;<b><%=webApp.getName()%></b></font>
                            <br>
                        </td>
                    </tr>
                    <tr>
                        <td valign="top" align="right" nowrap>
                            <font class="text"><utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.components.ManageComponents.webAppContext.label"/>&nbsp;:</font>
                            <br>
                        </td>
                        <td width="90%">
                            <font class="text">&nbsp;<%=webApp.getContextRoot()%></font>
                            <br>
                        </td>

                    </tr>
                    <tr>
                        <td valign="top" align="right" nowrap>
                            <font class="text"><utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.description.label"/>&nbsp;:</font>
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
                            <font class="text"><utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.components.ManageComponents.listOfServlets.label"/>&nbsp;:</font>
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
                            <font class="text"><utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.name.label"/>&nbsp;:</font>
                        </td>
                        <td >
                            <font class="text">&nbsp;<%=servlet.getName()%></font>
                            <br>
                        </td>
                    </tr>
                    <tr>
                        <td valign="top" align="right" nowrap>
                            <font class="text"><utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.components.ManageComponents.source.label"/>&nbsp;:</font>
                        </td>
                        <td >
                            <font class="text">&nbsp;<%=servlet.getSource()%></font>
                            <br>
                        </td>
                    </tr>
                    <tr>
                        <td valign="top" align="right" nowrap>
                            <font class="text"><utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.type.label"/>&nbsp;:</font>
                        </td>
                        <td >
                            <font class="text">&nbsp;<%=servlet.getTypeLabel()%></font>
                            <br>
                        </td>
                    </tr>
                    <tr>
                        <td valign="top" align="right" nowrap>
                            <font class="text"><utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.description.label"/>&nbsp;:</font>
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
                            <font class="text"><b><utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.components.ManageComponents.noWebComponents.label"/></b></font>
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
                <a href="javascript:sendForm('delete');" onMouseOut="MM_swapImgRestore()" onMouseOver="MM_swapImage('delete','','${pageContext.request.contextPath}<utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.deleteOn.button"/>',1)"><img name="delete" src="${pageContext.request.contextPath}<utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.deployOff.button"/>" width="69" height="17" border="0" alt="<utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.delete.label"/>"></a>
                <% if ( canDeploy.booleanValue() ){%>
                    <a href="javascript:sendForm('deploy');" onMouseOut="MM_swapImgRestore()" onMouseOver="MM_swapImage('deploy','','${pageContext.request.contextPath}<utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.deployOn.button"/>',1)"><img name="deploy" src="${pageContext.request.contextPath}<utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.deployOff.button"/>" width="69" height="17" border="0" alt="<utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.deploy.label"/>"></a>
                <% } else { %>
                    <a href='<%=JahiaAdministration.composeActionURL(request,response,"sharecomponents","&sub=displaynewlist")%>' onMouseOut="MM_swapImgRestore()" onMouseOver="MM_swapImage('cancel','','${pageContext.request.contextPath}<utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.cancelOn.button"/>',1)"><img name="cancel" src="${pageContext.request.contextPath}<utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.cancelOff.button"/>" width="69" height="17" border="0" alt="<utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.cancel.label"/>"></a>
                <% } %>
  </div>

  <div id="operationMenu">
  	<div id="operationMenuLabel">
			<utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.otherOperations.label"/>&nbsp;:
		</div>
		<ul id="operationList">
      <li class="operationEntry">
        <a class="operationLink" href='<%=JahiaAdministration.composeActionURL(request,response,"sharecomponents","&sub=displaynewlist")%>'><utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.components.ManageComponents.backToNewComponentsList.label"/></a>
      </li>
      <li class="operationEntry">
        <a class="operationLink" href='<%=JahiaAdministration.composeActionURL(request,response,"sharecomponents","&sub=display")%>'><utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.components.ManageComponents.backToComponentsList.label"/></a>
      </li>
      <li class="operationEntry">
      	<a class="operationLink" href='<%=JahiaAdministration.composeActionURL(request,response,"displaymenu","")%>'><utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.backToMenu.label"/></a>
      </li>     		
    </ul>
  </div>

</div>

<%@include file="/jsp/jahia/administration/include/footer.inc"%>