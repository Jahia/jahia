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
<%@ page language="java" contentType="text/html;charset=UTF-8" %>

<%@ include file="declarations.jspf" %>
<div class="content">
    <div class="padding">
        <div class="right">
            <template:include page="modules/links/basicLinksDisplay.jsp">
                <template:param name="cssClassName" value="links"/>
            </template:include>
        </div>

        <ui:navigationMenu cssClassName="menu" onlyTop="true" hideActionMenus="true" requiredTitle="true"/>
        <br/>

        <template:include page="common/footer/footerDisplay.jsp"/>
    </div>
</div>