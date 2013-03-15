<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>

<jcr:node var="sites" path="/sites"/>
<form action="${flowExecutionUrl}" method="POST">
<table>

        <tr>
            <th>
                <fmt:message key="serverSettings.manageWebProjects.webProject.title"/>
            </th>
            <th>
                <fmt:message key="serverSettings.manageWebProjects.webProject.siteKey"/>
            </th>
            <th>
                <fmt:message key="serverSettings.manageWebProjects.webProject.serverName"/>
            </th>
            <th>
                <fmt:message key="serverSettings.manageWebProjects.webProject.selectTemplateSet"/>
            </th>
            <th>

            </th>
        </tr>

        <input name="_sites" type="hidden"/>
        <c:forEach items="${webprojectHandler.sites}" var="site">
            <tr>
                <td>${site.title}</td>
                <td>${site.siteKey}</td>
                <td>${site.serverName}</td>
                <td>${site.templateFolder}</td>
            </tr>
        </c:forEach>

</table>
<input type="submit" name="_eventId_cancel" value="<fmt:message key='label.cancel' />" onclick=""/>
<input type="submit" name="_eventId_deleteSitesConfirmed" value="<fmt:message key='serverSettings.manageWebProjects.delete' />" onclick=""/>
</form>


