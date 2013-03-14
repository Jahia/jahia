<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<form action="${flowExecutionUrl}" method="POST">

    <div>
        ${siteBean.siteKey}
    </div>
    <div>
        ${siteBean.title}
    </div>
    <div>
        ${siteBean.serverName}
    </div>
    <div>
        ${siteBean.description}
    </div>
    <div>
        ${siteBean.defaultSite}
    </div>
    <div>
        ${siteBean.templateSet}
    </div>
    <div>
        ${siteBean.modules}
    </div>
    <div>
        ${siteBean.language}
    </div>


    <input type="submit" name="_eventId_previous" value="<fmt:message key='serverSettings.manageModules.previous'/>"/>
    <input type="submit" name="_eventId_next" value="<fmt:message key='serverSettings.manageModules.next'/>"/>
</form>
