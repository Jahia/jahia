<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>


<form action="${flowExecutionUrl}" method="POST">

    <fieldset>
        <div>
            <label for="title">Title</label>
            <input id="title" name="title" value="${siteBean.title}"/>
        </div>

        <div>
            <label for="serverName">Server name</label>
            <input id="serverName" name="serverName" value="${siteBean.serverName}"/>
        </div>

        <div>
            <label for="siteKey">Site key</label>
            <input id="siteKey" name="siteKey" value="${siteBean.siteKey}"/>
        </div>

        <div>
            <label for="description">Description</label>
            <textarea id="description" name="description">${siteBean.description}</textarea>
        </div>

        <div>
            <label for="defaultSite">Set as the default Web Project</label>
            <input type="checkbox" name="defaultSite" id="defaultSite" <c:if test="${siteBean.defaultSite}">checked="checked"</c:if> />
            <input type="hidden" name="_defaultSite"/>
        </div>
    </fieldset>

    <input type="submit" name="_eventId_skipAdmin" value="<fmt:message key='serverSettings.manageModules.skipAdmin'/>"/>
    <input type="submit" name="_eventId_next" value="<fmt:message key='serverSettings.manageModules.next'/>"/>
</form>
