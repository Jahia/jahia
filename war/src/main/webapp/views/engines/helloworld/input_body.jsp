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

<%@include file="/views/engines/common/taglibs.jsp" %>

<div align="center">
<html:errors/>
<h3>Be kind, enter your name :</h3>

<html:form action="/hello" focus="name" >
    <html:text   property="name" size="30" maxlength="30"/>
    <html:submit property="submit" value="Submit"/>
</html:form>
</div>
