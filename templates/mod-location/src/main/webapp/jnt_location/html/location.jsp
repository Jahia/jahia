<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>


<div class="preferences"><!--start preferences-->
    <h2>${currentNode.properties.title.string}</h2>

    <p class="preference-item"><span class="preference-label"><fmt:message key="jahia.location.street"/> : </span>
        <span class="preference-value">${currentNode.properties.street.string}</span>
    </p>

    <p class="preference-item"><span class="preference-label"><fmt:message key="jahia.location.zipCode"/> : </span>
        <span class="preference-value">${currentNode.properties.zipCode.string}</span>
    </p>
    <p class="preference-item"><span class="preference-label"><fmt:message key="jahia.location.town"/> : </span>
        <span class="preference-value">${currentNode.properties.town.string}</span>
    </p>
    <p class="preference-item"><span class="preference-label"><fmt:message key="jahia.location.country"/> : </span>
        <span class="preference-value">${currentNode.properties.country.string}</span>
    </p>
    <p class="preference-item"><span class="preference-label"><fmt:message key="jahia.location.latitude"/> : </span>
        <span class="preference-value">${currentNode.properties.latitude.string}</span>
    </p>
    <p class="preference-item"><span class="preference-label"><fmt:message key="jahia.location.longitude"/> : </span>
        <span class="preference-value">${currentNode.properties.longitude.string}</span>
    </p>
</div>