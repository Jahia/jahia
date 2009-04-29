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
<%@ page language="java" %>
<%@ page import="org.jahia.views.engines.helloworld.forms.HelloForm" %>

<%@include file="/views/engines/common/taglibs.jsp" %>

<%
	HelloForm form = (HelloForm)request.getAttribute("helloForm");
%>

<div align="center">
<html:errors/>
<p>
<h3>Welcome <%=form.getName()%> !!!</h3>
</p>
<br><br>
<html:link action="/hello"><b>Baby one more time...!!!</b></html:link>
</div>
