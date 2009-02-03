<%@ taglib uri="http://java.sun.com/jstl/core_rt" prefix="c" %>
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
                <template:field name='mainContentAlign' display="false" valueBeanID="mainContentAlign"/>
                <template:image file="mainContentImage" cssClassName="${mainContentAlign}"
                                        align="${mainContentAlign}"/>
                <template:field name="mainContentBody"/>
            </p>
            <br class="clear"/>
        </template:container>
    </template:containerList>
</div>
