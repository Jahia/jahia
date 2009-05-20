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
<%@ include file="../common/declarations.jspf" %>
<%--
Set theme for the actual site,
values for scope are :
- all : all users can change theme
- user : only authentified users can change theme
- site : only site admin can change theme.
todo : change site to siteAdmin
--%>
<div class="themeSelector">
    <c:if test="${requestScope.currentRequest.admin}">
        <fmt:message key='siteThemeSelector'/>: <ui:themeSelector scope="site"/>
    </c:if>
</div>
