<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>
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
<%@ taglib uri="http://www.jahia.org/tags/templateLib" prefix="template" %>
<%@ taglib uri="http://www.jahia.org/tags/queryLib" prefix="query" %>

<!--<table>-->

    <%--<tr>--%>
        <%--<td colspan="2"><h2>About Pluto Portal Driver</h2></td>--%>
    <%--</tr>--%>

    <%--<tr>--%>
        <%--<td>Portal Name:</td>--%>
        <%--<td><c:out value="${driverConfig.portalName}"/></td>--%>
    <%--</tr>--%>
    <%--<tr>--%>
        <%--<td>Portal Version:</td>--%>
        <%--<td><c:out value="${driverConfig.portalVersion}"/></td>--%>
    <%--</tr>--%>
    <%--<tr>--%>
        <%--<td>Servlet Container:</td>--%>
        <%--<td><%= config.getServletContext().getServerInfo() %>--%>
        <%--</td>--%>
    <%--</tr>--%>
    <%--<tr>--%>
        <%--<td>Java Version:</td>--%>
        <%--<td><%= System.getProperty("java.version") %>  (<%= System.getProperty("java.vm.vendor") %>--%>
            <%--- <%= System.getProperty("java.vm.name") %> build <%= System.getProperty("java.vm.version") %>)--%>
        <%--</td>--%>
    <%--</tr>--%>
    <%--<tr>--%>
        <%--<td>Operating System:</td>--%>
        <%--<td><%= System.getProperty("os.name") %>  (<%= System.getProperty("os.arch") %>--%>
            <%--version <%= System.getProperty("os.version") %>)--%>
        <%--</td>--%>
    <%--</tr>--%>

    <%--<%--%>
        <%--Iterator attributeNameEnum = request.getAttributeNames();--%>
        <%--while (attributeNameEnum.hasNext()) {--%>
            <%--String curAttributeName = (String) attributeNameEnum.next();--%>
            <%--Object curAttributeObject = request.getAttribute(curAttributeName);--%>
    <%--%>--%>
    <%--<tr>--%>
        <%--<td><%=curAttributeName%>--%>
        <%--</td>--%>
        <%--<td><%=curAttributeObject.toString()%>--%>
        <%--</td>--%>
    <%--</tr>--%>
    <%--<%--%>
        <%--}--%>
    <%--%>--%>

<%--</table>--%>

<%--<div>--%>
    <%--<components:sitemap ajax="false"/>--%>
<%--</div>--%>

<div class="maincontentList">
    <template:containerList name="mainContentContainer" id="maincontentList" actionMenuNamePostFix="mainContents"
                           actionMenuNameLabelKey="mainContents.add">
        <template:container id="mainContent" actionMenuNamePostFix="mainContent"
                           actionMenuNameLabelKey="mainContent.update">
            <h3><template:field name='mainContentTitle' diffActive="true"/></h3>

            <p class="maincontent">
                <template:field name='mainContentAlign' display="false" var="mainContentAlign"/>
                <template:image file="mainContentImage" cssClassName="${mainContentAlign.value}"
                                        align="${mainContentAlign.value}"/>
                <template:field name="mainContentBody"/>
            </p>
            <br class="clear"/>
        </template:container>
    </template:containerList>
</div>
