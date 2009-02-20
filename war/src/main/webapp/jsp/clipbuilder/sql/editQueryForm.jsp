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

<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ page language="java" import="org.jahia.bin.JahiaAdministration" %>
<%@ page import="org.jahia.data.JahiaData" %>
<%@ page import="org.jahia.registries.ServicesRegistry" %>
<%@ page import="org.jahia.services.acl.JahiaACLManagerService" %>
<%@ page import="org.jahia.services.acl.JahiaBaseACL" %>
<%@ page import="org.jahia.services.usermanager.JahiaUser" %>
<%@ page import="java.util.List" %>
<%
    final JahiaACLManagerService aclService = ServicesRegistry.getInstance().getJahiaACLManagerService();
    final JahiaData jData = (JahiaData) request.getAttribute("org.jahia.data.JahiaData");
    final JahiaUser user = jData.getProcessingContext().getUser();
    final int currentSiteID = jData.getProcessingContext().getSiteID();
%>
<html:html>
<head>
    <link rel="stylesheet"
          href="<%=response.encodeURL(request.getContextPath()+"/html/startup/clipbuilder/web_css.jsp?colorSet=blue")%>"
          type="text/css"/>
    <link rel="stylesheet" href="<%=response.encodeURL(request.getContextPath()+"/jsp/jahia/engines/css/jahia.css")%>"
          type="text/css">
    <link rel="stylesheet"
          href="<%=response.encodeURL(request.getContextPath()+"/html/startup/clipbuilder/style.css")%>"
          type="text/css"/>
    <script language="javascript"
            src="<%=response.encodeURL(request.getContextPath()+"/jsp/jahia/engines/../javascript/jahia.js")%>"></script>
    <script LANGUAGE="JavaScript" TYPE="text/javascript">
        <!--
        function popitup(url)
        {
            newwindow = window.open(url, 'name', 'toolbar=0,resizable=yes,scrollable=yes');
            if (window.focus) {
                newwindow.focus();
            }
        }

        // -->
    </script>
    <script type="text/javascript">
        function updateValue(form, value)
        {
            if (value == '<%=org.jahia.clipbuilder.sql.struts.EditQueryAction.DATABASE_MYSQL.toLowerCase()%>')
            {
                form.databaseUrl.value = 'localhost:3306';
            }
            if (value == '<%=org.jahia.clipbuilder.sql.struts.EditQueryAction.DATABASE_HSQL.toLowerCase()%>')
            {
                form.databaseUrl.value = 'localhost:8886';
            }
            if (value == '<%=org.jahia.clipbuilder.sql.struts.EditQueryAction.DATABASE_ORACLE.toLowerCase()%>')
            {
                form.databaseUrl.value = 'localhost:1521';
            }
            if (value == '<%=org.jahia.clipbuilder.sql.struts.EditQueryAction.DATABASE_POSTGRE.toLowerCase()%>')
            {
                form.databaseUrl.value = 'localhost:5432';
            }

        }
    </script>
    <style type="text/css">
        input.fancyButton {
            text-decoration: none;
            background-image: url( <%=request.getContextPath()%> /jsp/jahia/css/bg_button_up.gif );
            border: 1px solid;
            border-color: #D0D0D0 #555555 #555555 #D0D0D0;
            font-size: 11px;
            cursor: default;
            color: black;
            font-weight: bold;
            padding: 3px 3px 3px 3px;
            height: 24px;
            margin-left: 6px;
            font-family: Verdana, Arial, Helvetica, sans-serif;
        }
    </style>

</head>
<body class="install">
<center><br/><br/><br/>
<table cellpadding="1" cellspacing="0" width="80%" border="0">
<tr>
<td bgcolor="#000000">
<!-- Jahia Image -->
<table class="text" height="63" border="0" cellspacing="0" cellpadding="0">
    <tr>
        <td width="126" height="63">
            <img src="${pageContext.request.contextPath}<utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.header.image"/>"
                 width="126" height="63" alt="">
        </td>
        <td width="100%" height="63">
            <img src="${pageContext.request.contextPath}<utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.headerBg.image"/>"
                 width="100%" height="63" alt="">
        </td>
    </tr>
</table>
<!-- back to menu-->
<table class="text" width="100%">
    <tr>
        <td align="right" class="text">
            <a href='<%=JahiaAdministration.composeActionURL(request,response,"displaymenu","")%>'>
                <utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.backToMenu.label"/>
            </a>
        </td>
    </tr>
</table>
<!-- SQL CLipper -->
<table class="text" width="100%">
<!-- Main table-->
<tr>
<td align="center">
<!-- Main -->
<table class="text" align="center" width="100%">
<html:form action="/buildSqlPortlet?do=clipbuilder">
<!-- Header -->
<table align="center" class="principal" width="100%">
    <!-- Title -->
    <tr>
        <td class="text">
            <b>
                <bean:message key="sql.title"/>
            </b>
            <img src="${pageContext.request.contextPath}<utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.hr.image"/>"
                 width="100%" height="2">
        </td>
    </tr>
</table>
<table class="principal" width="100%">
<!-- Database connection -->
<tr>
    <td class="topmenugreen bold waBG">
        <bean:message key="sql.database.connection"/>
    </td>
</tr>
<!-- Warning messages -->
<tr>
    <td>
        <logic:messagesPresent name="error" message="true">
            <table align="center" bgcolor="#800000" cellpadding="0" cellspacing="1" width="100%">
                <tbody>
                    <tr>
                        <td>
                            <table class="text" align="center" bgcolor="#ffffff" border="0" width="100%">
                                <tbody>
                                    <tr>
                                        <td align="center" bgcolor="#dcdcdc">
                                            <font color="#cc0000">
                                                <b><bean:message key="error.error"/> </b>
                                            </font>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td>
                                            <ul>
                                                <html:messages id="error" message="true">
                                                    <li class="text">
                                                        <bean:write name="error"/>
                                                    </li>
                                                </html:messages>
                                            </ul>
                                        </td>
                                    </tr>
                                </tbody>
                            </table>
                        </td>
                    </tr>
                </tbody>
            </table>
        </logic:messagesPresent>
        <logic:present name="deployed" scope="request">
            <table align="center" bgcolor="#006699" cellpadding="0" cellspacing="1" width="100%">
                <tbody>
                    <tr>
                        <td>
                            <table class="text" align="center" bgcolor="#006699" border="0" width="100%">
                                <tbody>
                                    <tr>
                                        <td align="center" bgcolor="#dcdcdc">
                                            <font color="#006699">
                                                <b><bean:message key="information"/> </b>
                                            </font>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td>
                                            <ul>
                                                <li class="text">
                                                    <bean:message key="clipbuilder.deployement.available"/>
                                                </li>
                                                <li class="text">
                                                    <bean:message key="clipbuilder.deployement.permissions"/>
                                                    <% if (aclService.getServerActionPermission("admin.components.ManageShareComponents", user, JahiaBaseACL.READ_RIGHTS, currentSiteID) > 0) { %>
                                                    <a href='<%=JahiaAdministration.composeActionURL(request,response,"sharecomponents","&sub=display")%>'>
                                                        (<utility:resourceBundle resourceBundle="JahiaInternalResources"
                                                                resourceName="org.jahia.admin.manageComponents.label"/>)
                                                    </a>
                                                    <% } %>
                                                </li>
                                            </ul>
                                        </td>
                                    </tr>
                                </tbody>
                            </table>
                        </td>
                    </tr>
                </tbody>
            </table>
        </logic:present>
    </td>
</tr>
<!--Properties-->
<tr>
    <td>
        <table width="100%">
            <!--database type-->
            <tr>
                <td width="33%" class="text">
                    <bean:message key="sql.query.database"/>
                </td>
                <td class="leftlevel2 waInput">
                    <html:select onchange="updateValue(this.form,this.value);" name="editQueryForm" property="database"
                                 size="1">
                        <html:option value="<%=org.jahia.clipbuilder.sql.struts.EditQueryAction.DATABASE_MYSQL%>">
                            <bean:message key="sql.query.database.mysql"/>
                        </html:option>
                        <html:option value="<%=org.jahia.clipbuilder.sql.struts.EditQueryAction.DATABASE_HSQL%>">
                            <bean:message key="sql.query.database.hsql"/>
                        </html:option>
                        <html:option value="<%=org.jahia.clipbuilder.sql.struts.EditQueryAction.DATABASE_ORACLE%>">
                            <bean:message key="sql.query.database.oracle"/>
                        </html:option>
                        <html:option value="<%=org.jahia.clipbuilder.sql.struts.EditQueryAction.DATABASE_POSTGRE%>">
                            <bean:message key="sql.query.database.postegre"/>
                        </html:option>
                    </html:select>
                </td>
                <td></td>
            </tr>
            <!-- database url -->
            <tr>
                <td width="33%" class="text">
                    <bean:message key="sql.query.databaseUrl"/>
                </td>
                <td class="leftlevel2 waInput">
                    <html:text name="editQueryForm" property="databaseUrl"/>
                </td>
                <td></td>
            </tr>
            <!-- database name-->
            <tr>
                <td width="33%" class="text">
                    <bean:message key="sql.query.databaseName"/>
                </td>
                <td class="leftlevel2 waInput">
                    <html:text name="editQueryForm" property="databaseName"/>
                </td>
                <td></td>
            </tr>
            <!-- database user-->
            <tr>
                <td width="33%" class="text">
                    <bean:message key="sql.query.userName"/>
                </td>
                <td class="leftlevel2 waInput">
                    <html:text name="editQueryForm" property="userName"/>
                </td>
                <td></td>
            </tr>
            <!-- database password-->
            <tr>
                <td width="33%" class="text">
                    <bean:message key="sql.query.userPassword"/>
                </td>
                <td class="leftlevel2 waInput">
                    <html:password name="editQueryForm" property="userPassword"/>
                </td>
                <td width="20%">
                    <html:submit styleClass="fancyButton" property="webClippingAction">
                        <bean:message key="sql.button.saveAsDefault"/>
                    </html:submit>
                </td>
            </tr>
        </table>
    </td>
</tr>
<tr>
    <td class="topmenugreen bold waBG">
        <bean:message key="sql.query.properties"/>
    </td>
</tr>
<tr>
    <td>
        <!-- Portlet properties-->
        <table width="100%">
            <!-- query title-->
            <tr>
                <td width="33%" class="text">
                    <bean:message key="sql.query.title"/>
                </td>
                <td class="leftlevel2 waInput">
                    <html:text name="editQueryForm" property="title"/>
                </td>
                <td></td>
            </tr>
            <!-- query nb result-->
            <tr>
                <td width="33%" class="text">
                    <bean:message key="sql.query.nbResultPerTable"/>
                </td>
                <td class="leftlevel2 waInput">
                    <html:text name="editQueryForm" property="tableSize"/>
                </td>
                <td></td>
            </tr>
            <!-- Query value-->
            <tr>
                <td width="33%" valign="top" class="text">
                    <bean:message key="sql.query.sqlQuery"/>
                </td>
                <td class="leftlevel2 waInput">
                    <html:textarea rows="5" cols="40" name="editQueryForm" property="sqlQuery"/>
                </td>
                <td valign="bottom" width="20%">
                    <html:submit styleClass="fancyButton" property="webClippingAction">
                        <bean:message key="sql.button.executeQuery"/>
                    </html:submit>
                </td>
            </tr>
        </table>
    </td>
</tr>
<tr>
    <td class="topmenugreen bold waBG">
        <bean:message key="sql.portlet.properties"/>
    </td>
</tr>
<tr>
    <td>
        <!-- Portlet properties-->
        <table width="100%">
            <!-- portlet name-->
            <tr>
                <td width="33%" class="text">
                    <bean:message key="sql.portlet.name"/>
                </td>
                <td class="leftlevel2 waInput">
                    <html:text name="editQueryForm" property="portletname"/>
                </td>
                <td></td>
            </tr>
            <!-- portlet value-->
            <tr>
                <td width="33%" class="text">
                    <bean:message key="sql.portlet.description"/>
                </td>
                <td class="leftlevel2 waInput">
                    <html:text name="editQueryForm" property="portletdescription"/>
                </td>
                <td width="20%">
                    <html:submit styleClass="fancyButton" property="webClippingAction">
                        <bean:message key="sql.button.deploy"/>
                    </html:submit>
                </td>
            </tr>
        </table>
    </td>
</tr>
<tr>
    <td class="topmenugreen bold waBG">
        <bean:message key="sql.preview"/>
    </td>
</tr>
<tr>
    <td>
        <table bgcolor="white" align="center" width="100%">
            <tr>
                <td>
                    <%
                        //Display the table data:
                        List queryData = (List) request.getSession().getAttribute("queryData");
                        if (queryData != null && queryData.size() > 0) {
                    %>
                    <jsp:include flush="false" page="/jsp/clipbuilder/sql/tableResult.jsp"/>
                    <%} else { %>
                    <b>No result</b>
                    <%} %>
                </td>
            </tr>
        </table>
    </td>
</tr>
</table>
</html:form>
</table>
<!-- back to menu-->
<table class="text" width="100%">
    <tr>
        <td align="right" class="text">
            <a href='<%=JahiaAdministration.composeActionURL(request,response,"displaymenu","")%>'>
                <utility:resourceBundle resourceBundle="JahiaInternalResources" resourceName="org.jahia.admin.backToMenu.label"/>
            </a>
        </td>
    </tr>
</table>
</td>
</tr>
</table>
</td>
</tr>
</table>
</center>
</body>
</html:html>
