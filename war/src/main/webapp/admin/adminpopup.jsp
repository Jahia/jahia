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
<%@page contentType="text/html; charset=UTF-8"
%><!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">
<%@page language = "java" %>
<%@page import = "java.util.*"%>
<%@page import = "org.jahia.bin.Jahia"%>
<%@page import="org.jahia.bin.*"%>
<%@taglib uri="http://www.jahia.org/tags/internalLib" prefix="internal" %>
<jsp:useBean id="title" class="java.lang.String" scope="request"/>
<jsp:useBean id="URL" class="java.lang.String" scope="request"/>   <% // http files path. %>

<%
    String contenttypestr = (String) request.getAttribute("content-type");
    response.setContentType(contenttypestr);

    String jspSource = (String)request.getAttribute("jspSource");
    String suicide = (String)request.getAttribute("suicide");
    final String contextPath = request.getContextPath();
    String copyright=Jahia.COPYRIGHT;
%>



<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <title><fmt:message key="org.jahia.admin.jahiaAdministration.label"/></title>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />

    <link rel="stylesheet" href="<%=contextPath%>/css/andromeda.css" type="text/css" />
    <script type="text/javascript" src="<%=contextPath%>/javascript/jahia.js"></script>
    <script type="text/javascript" src="<%=request.getContextPath()%>/javascript/selectbox.js"></script>
<script type="text/javascript">

function submitForm(call, action, subAction)
{
    document.mainForm.action = '<%=JahiaAdministration.composeActionURL(request,response,"","")%>' + call + "&sub=" + action + "&subaction=" + subAction;
    document.mainForm.method = "post";
    document.mainForm.submit();
    //var href = window.opener.location.href;
    //window.opener.location.href = href;
}

function submitParent() {

    // if there are operations to be performed before the form is submitted,
    // such as element selection, etc, we perform it now.
    if (window.opener.beforeSubmit != null) {
        window.opener.beforeSubmit();
    }

    window.opener.document.mainForm.actionType.value='update';
    window.opener.document.mainForm.submit();
}
</script>
</head>

<body class="jahiaAdministration" onload="<% if (suicide != null) { %>javascript:submitParent();closePopupWindow()<% } %>">
<div id="main">
  <table style="width: 100%;" class="dex-TabPanel" cellpadding="0" cellspacing="0">
    <tbody>
      <tr>
        <td style="vertical-align: top;" align="left">
        <div id="content" class="full">
<form name="mainForm" method="post" action="">

    <!-- include page start -->

    <jsp:include page="<%=jspSource%>" flush="true"/>

    <!-- include page ends -->
</form>
</div>
</td>
      </tr>
    </tbody>
  </table>
</div>
<div id="copyright">
		<%=copyright%> Jahia <%=Jahia.VERSION%>.<%=Jahia.getPatchNumber()%> r<%=Jahia.getBuildNumber()%>
	</div>



</body>
</html>
