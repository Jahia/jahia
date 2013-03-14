<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="functions" uri="http://www.jahia.org/tags/functions" %>

<jcr:node var="sites" path="/sites"/>
<table>
    <form action="${flowExecutionUrl}" method="POST">
        <input type="submit" name="_eventId_createSite" value="<fmt:message key='serverSettings.manageWebProjects.createWebProject' />" onclick=""/>
        <input type="submit" name="_eventId_exportSites" value="<fmt:message key='serverSettings.manageWebProjects.export' />" onclick=""/>
        <input type="submit" name="_eventId_exportStagingSites" value="<fmt:message key='serverSettings.manageWebProjects.exportStaging' />" onclick=""/>
        <input type="submit" name="_eventId_deleteSites" value="<fmt:message key='serverSettings.manageWebProjects.delete' />" onclick=""/>

    <tr>
        <th>
        </th>
        <th>
            Name
        </th>
        <th>
            Web project key
        </th>
        <th>
            Web project host name
        </th>
        <th>
            Templates set
        </th>
        <th>

        </th>
    </tr>

    <c:forEach items="${jcr:getChildrenOfType(sites,'jnt:virtualsite')}" var="site">
        <c:if test="${site.name ne 'systemsite'}">
            <tr>
                <td><input type="checkbox"/></td>
                <td>${site.title}</td>
                <td>${site.name}</td>
                <td>${site.serverName}</td>
                <td>${site.templateFolder}</td>
            </tr>
        </c:if>
    </c:forEach>
    </form>

</table>