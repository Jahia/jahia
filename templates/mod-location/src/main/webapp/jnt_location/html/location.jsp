<%@ taglib prefix="jcr" uri="http://www.jahia.org/tags/jcr" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="utility" uri="http://www.jahia.org/tags/utilityLib" %>
<%@ taglib prefix="template" uri="http://www.jahia.org/tags/templateLib" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<jcr:nodeProperty node="${currentNode}" name="street" var="street"/>
<jcr:nodeProperty node="${currentNode}" name="zipCode" var="zipCode"/>
<jcr:nodeProperty node="${currentNode}" name="town" var="town"/>
<jcr:nodeProperty node="${currentNode}" name="country" var="country"/>
<jcr:nodeProperty node="${currentNode}" name="latitude" var="latitude"/>
<jcr:nodeProperty node="${currentNode}" name="longitude" var="longitude"/>

<div class="preferences"><!--start preferences-->
    <h2>${title.string}</h2>

    <p class="preference-item"><span class="preference-label"><fmt:message key="jahia.location.street"/> : </span>
        <span class="preference-value">${street.string}</span>
    </p>

    <p class="preference-item"><span class="preference-label"><fmt:message key="jahia.location.zipCode"/> : </span>
        <span class="preference-value">${zipCode.string}</span>
    </p>

    <p class="preference-item"><span class="preference-label"><fmt:message key="jahia.location.town"/> : </span>
        <span class="preference-value">${town.string}</span>
    </p>

    <p class="preference-item"><span class="preference-label"><fmt:message key="jahia.location.country"/> : </span>
        <span class="preference-value">${country.string}</span>
    </p>
    <p class="preference-item"><span class="preference-label"><fmt:message key="jahia.location.latitude"/> : </span>
        <span class="preference-value">${latitude.string}</span>
    </p>
    <p class="preference-item"><span class="preference-label"><fmt:message key="jahia.location.longitude"/> : </span>
        <span class="preference-value">${longitude.string}</span>
    </p>
</div>