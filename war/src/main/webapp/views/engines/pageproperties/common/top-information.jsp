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

<table class="text" width="95%" align="center" border="0" cellspacing="0" cellpadding="0">
<tr>
    <td><img src="${pageContext.request.contextPath}<fmt:message key="org.jahia.pix.image"/>" width="1" height="18"></td>
    <td><b><fmt:message key="org.jahia.engines.pages.PageProperties_Engine.pageSettings.label"/></b></td>
    <td><b><fmt:message key="org.jahia.engines.pages.PageProperties_Engine.pageTitle.label"/></b>&nbsp;:&nbsp;<span class="text2"><bean:write name="pageproperties.pagetitle" scope="request" /></span></td>
    <td><b><fmt:message key="org.jahia.pageId.label"/></b>&nbsp;:&nbsp;[<bean:write name="pageproperties.pageid" scope="request" />]</td>
</tr>
</table>