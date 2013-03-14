<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>


<form action="${flowExecutionUrl}" method="POST">
    Site administrator
    <input type="submit" name="_eventId_previous" value="<fmt:message key='serverSettings.manageModules.previous'/>"/>
    <input type="submit" name="_eventId_next" value="<fmt:message key='serverSettings.manageModules.next'/>"/>
</form>
